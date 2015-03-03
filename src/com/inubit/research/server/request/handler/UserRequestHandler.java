/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.request.handler;

import com.inubit.research.server.HttpConstants;
import com.inubit.research.server.ProcessEditorServerHelper;
import com.inubit.research.server.errors.AccessViolationException;
import com.inubit.research.server.manager.ModelManager;
import com.inubit.research.server.multipart.MultiPartItem;
import com.inubit.research.server.multipart.MultiPartObject;
import com.inubit.research.server.multipart.SimpleMultipartParser;
import com.inubit.research.server.persistence.PersistenceConnector.ImageType;
import com.inubit.research.server.request.RequestFacade;
import com.inubit.research.server.request.RequestUtils;
import com.inubit.research.server.request.ResponseFacade;
import com.inubit.research.server.request.ResponseUtils;
import com.inubit.research.server.request.XMLHelper;
import com.inubit.research.server.user.Group;
import com.inubit.research.server.user.LoginableUser;
import com.inubit.research.server.user.SingleUser;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.imageio.ImageIO;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Handler for request concerning the user management
 * @author fel
 */
public class UserRequestHandler extends AbstractRequestHandler{
    public static final String SESSION_ATTRIBUTE = "PES_SESSION_ID";
//    private static IntegratedUserManager manager = IntegratedUserManager.getInstance();

    @Override
    public void handleGetRequest( RequestFacade req, ResponseFacade resp ) throws IOException, AccessViolationException {
        String requestUri = req.getRequestURI();
        Document response = null;
        LoginableUser user = null;

        if (requestUri.matches("/users/this")) {
            //return info belonging to this session's user
            if ( RequestUtils.checkForRedirect(req, resp) )
                return;

            user = RequestUtils.getCurrentUser(req);
            
            response = createUserInfo(user);
        } else if (requestUri.matches("/users/users(\\?.*)?")) {
            //return list of all users
            Set<String> users = ProcessEditorServerHelper.getUserManager().getUserNames();
            response = this.createList(users, "users", "user");
        } else if (requestUri.matches("/users/users/.+/img(\\?.+)?")) {
            //manual request to user image
            String userName = requestUri.split("/")[3];
            SingleUser u = ProcessEditorServerHelper.getUserManager().getUserForName(userName);

            if (u == null) {
                ResponseUtils.respondWithStatus(404, "User not found!", resp, true);
                return;
            }

            retrieveUserImage(req, resp, u);
            return;

        } else if (requestUri.matches("/users/users/.+")) {
            //depending on the requested content type, return either a user info or the user's image
            String userName = requestUri.replace("?" + req.getQuery(), "").split("/")[3];
            SingleUser u = ProcessEditorServerHelper.getUserManager().getUserForName(userName);

            String primaryAccept =
                    req.getHeader(HttpConstants.HEADER_KEY_ACCEPT).split(",")[0];

            if (primaryAccept.startsWith("image")) {
                retrieveUserImage( req, resp, u);
                return;
            } else {
                if (!this.checkAdminRights( req, resp ))
                    throw new AccessViolationException("You need admin rights to access this resource.");

                if (u == null) {
                    ResponseUtils.respondWithStatus(404, "User not found!", resp, true);
                    return;
                }

                response = this.createUserInfo(u);
            }

        } else if (requestUri.matches("/users/users/.+/img")) {
            //manual request to user image
            String userName = requestUri.split("/")[3];
            SingleUser u = ProcessEditorServerHelper.getUserManager().getUserForName(userName);

            if (u == null) {
                ResponseUtils.respondWithStatus(404, "User not found!", resp, true);
                return;
            }

            this.respondWithUserPicture(u, -1, -1, resp);
            return;

        }  else if (requestUri.matches("/users/groups")) {
            //return list of groups
            Set<String> groups = ProcessEditorServerHelper.getUserManager().getGroupNames();
            response = createList(groups, "groups", "group");
        } else if (requestUri.matches("/users/groups/.+")) {
            //retunr group info
            String groupName = requestUri.split("/")[3];
            try {
            if (!this.checkAdminRights( req, resp ))
                return;

            Group g = ProcessEditorServerHelper.getUserManager().getGroupForName(groupName);

            if (g == null) {
                ResponseUtils.respondWithStatus(404, "Group not found!", resp, true);
                return;
            }
            response = createGroupInfo(g);
            } catch ( Exception ex ) {
                ex.printStackTrace();
            }
        }

        if ( response != null ) {
            ResponseUtils.respondWithXML(resp, response, 200);
        }  else {
            ResponseUtils.respondWithStatus(404, "Requested Resource Not Found", resp, true);
        }
    }

    @Override
    public void handlePostRequest(RequestFacade req, ResponseFacade resp ) throws IOException, AccessViolationException {
        String requestUri = req.getRequestURI();
        String response = "";
        String contentType = HttpConstants.CONTENT_TYPE_TEXT_XML;
        int statusCode = 200;

        if (requestUri.matches("/users/login")) {
            //login a certain user
            String sessionID = null;
            String pwd;
            String name;
            boolean success = false;

            boolean respondWithXML = req.getHeader(HttpConstants.HEADER_KEY_ACCEPT).split(",")[0].contains("xml");
            boolean requestContentIsXML = !req.getHeader(HttpConstants.HEADER_KEY_CONTENT_TYPE).contains(HttpConstants.CONTENT_TYPE_APPLICATION_JSON);

            try {
                if (requestContentIsXML ) {
                    Document doc = RequestUtils.getXML(req);
                    Map<String, String> props = XMLHelper.parseProperties(doc.getDocumentElement());
                    name = props.get("name");
                    pwd = props.get("pwd");
                } else {
                    JSONObject json = RequestUtils.getJSON(req);
                    name = json.getString("name");
                    pwd = json.getString("pwd");
                }

                sessionID = ProcessEditorServerHelper.getUserManager().login( name, pwd );
                if (sessionID != null) {
                    resp.addCookie(UserRequestHandler.SESSION_ATTRIBUTE, sessionID, "/");
                    success = true;
                } 
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            statusCode = success?200:400;
            
            if ( respondWithXML ) {
                response = "<login><access>" + success + "</access>"+
                        ( success ? "<id>" + sessionID + "</id>" : "" ) +
                        "</login>";
            } else {
                Map map = new HashMap();
                map.put("success", success);
                JSONObject jsonResponse = new JSONObject(map);
                ResponseUtils.respondWithJSONAsText(resp, jsonResponse, statusCode);
                return;
            }
        } else if (requestUri.matches("/users/logout")) {
            //logout the user belonging to the current session
            boolean loggedOut = false;
            String sessionID = req.getCookieByName(UserRequestHandler.SESSION_ATTRIBUTE);
            
            if (sessionID != null) {
                ProcessEditorServerHelper.getUserManager().logout(sessionID );
                loggedOut = true;
                resp.addCookie(UserRequestHandler.SESSION_ATTRIBUTE, "null", "/");
            }

            response = "<logout>" + loggedOut + "</logout>";
        } else if (requestUri.matches("/users/users")) {
            //create a new user
            try {
                BufferedInputStream is = new BufferedInputStream( req.getInputStream() );
                is.mark(Integer.MAX_VALUE);

                SimpleMultipartParser smp = new SimpleMultipartParser();
                MultiPartObject mpo = smp.parseSource(is);

                String uName = mpo.getItemByName("alias").getContent().trim();
                String uPwd = mpo.getItemByName("pwd").getContent().trim();
                is.reset();
                boolean success = ProcessEditorServerHelper.getUserManager().addUser(uName, uPwd);
                if (success) {
                    String uMail = mpo.getItemByName("mail").getContent().trim();
                    ProcessEditorServerHelper.getUserManager().setMail(uName, uMail, true);

                    String picId = this.savePicture(mpo.getItemByName("pic"), is);
                    ProcessEditorServerHelper.getUserManager().setPictureId(uName, picId, true);

                    String realName = mpo.getItemByName("realname").getContent().trim();
                    ProcessEditorServerHelper.getUserManager().setRealName(uName, realName, false);
                }

                contentType = HttpConstants.CONTENT_TYPE_TEXT_HTML;
                response = "{success:" + success + "}";
            } catch (Exception e) {
                e.printStackTrace();
                statusCode = 500;
                response = "<error>Error while parsing XML</error>";
            }
        } else if (requestUri.matches("/users/groups")) {
            //create a new group
            if (!this.checkAdminRights( req, resp ))
                return;

            try {
                Document doc = RequestUtils.getXML( req );
                Node group = doc.getDocumentElement();
                String name = group.getAttributes().getNamedItem("name").getNodeValue();

                boolean success = ProcessEditorServerHelper.getUserManager().addGroup(name);
                response = "<success>" + success + "</success>";
            } catch (Exception e) {
                e.printStackTrace();
                statusCode = 500;
                response = "<error>Error while parsing XML</error>";
            }
        }
        //SHOULD BE PUT actually, but ExtJS does not allow PUTting forms
        else if ( requestUri.matches("/users/users/[^/]+?/img") ) {
            if ( !this.checkAdminRights(req, resp) )
                throw new AccessViolationException("This functionality can be used by administrators only!");

            BufferedInputStream is = new BufferedInputStream(req.getInputStream());
            is.mark(Integer.MAX_VALUE);

            SimpleMultipartParser smp = new SimpleMultipartParser();
            MultiPartObject mpo = smp.parseSource(is);

            String userName = requestUri.split("/")[3];
            SingleUser u = ProcessEditorServerHelper.getUserManager().getUserForName(userName);

            is.reset();
            if ( u.getPictureId() != null && !u.getPictureId().isEmpty()) {
                ImageType type = null;
                if ( mpo.getItemByName("pic").getContent().trim().equals(""))
                    if ( (type = this.getImageType( mpo.getItemByName("pic").getContentType()) ) != null )
                        ProcessEditorServerHelper.getPersistenceConnector().saveUserImage(u.getPictureId(), type, smp.parseItemContentAsByteArray(is, "pic"));
            } else {
                System.out.println("NEW IMAGE");
                String picId = this.savePicture(mpo.getItemByName("pic"), is);
                ProcessEditorServerHelper.getUserManager().setPictureId(u.getName(), picId, false);
            }

            resp.setContentType(HttpConstants.CONTENT_TYPE_TEXT_HTML);
            ResponseUtils.respondWithStatus(200, "{success:true}", resp, false);
            return;
        }

        ResponseUtils.respondWithStatus(statusCode, response, contentType, resp, false);
    }

    @Override
    public void handlePutRequest( RequestFacade req, ResponseFacade resp ) throws IOException, AccessViolationException {
        String requestUri = req.getRequestURI();

        int statusCode = 200;
        String response = "";
        boolean error = false;

        try {
            if(requestUri.matches("/users/users/.+")) {
                //grant admin rights to a certain user
                String userName = requestUri.split("/")[3];

                //for granting admin rights to a certain user, you have to be admin yourself
                if (!this.checkAdminRights( req, resp ))
                    throw new AccessViolationException("This functionality can be used by administrators only!");

                SingleUser u = ProcessEditorServerHelper.getUserManager().getUserForName(userName);

                if (u == null) {
                    ResponseUtils.respondWithStatus(404, "User not found!", resp , true);
                    return;
                }
                Document doc = RequestUtils.getXML( req );
                Map<String, String> props = XMLHelper.parseProperties(doc.getDocumentElement());

                if(requestUri.matches("/users/users/[^/]+/password+/?")){
                    ProcessEditorServerHelper.getUserManager().setPwd(u.getName(), props.get("password"), false);
                } else {
                    ProcessEditorServerHelper.getUserManager().setRealName(u.getName(), props.get("realname"), true);
                    ProcessEditorServerHelper.getUserManager().setMail(u.getName(), props.get("email"), false);
//                manager.setAdmin(userName, Boolean.valueOf(props.get("isadmin")));
                }
            } else if (requestUri.matches("/users/groups/.+")) {
                //set members and subgroups of a certain group
                String groupName = requestUri.split("/")[3];

                if (!this.checkAdminRights( req, resp ))
                    return;

                Group g = ProcessEditorServerHelper.getUserManager().getGroupForName(groupName);

                if (g == null) {
                    ResponseUtils.respondWithStatus(404, "Group not found!", resp, true);
                    return;
                }

                Document doc = RequestUtils.getXML( req );
                Map<String, String> props = XMLHelper.parseProperties(doc.getDocumentElement());

                Set<String> members = this.fromCSItem(props.get("members"));
                Set<String> subgroups = this.fromCSItem(props.get("subgroups"));

                ProcessEditorServerHelper.getUserManager().setGroupMembers(groupName, members);
                ProcessEditorServerHelper.getUserManager().setSubgroups(groupName, subgroups);
                
            }
        } catch ( AccessViolationException e ) {
            throw e;
        } catch ( Exception  e) {
            e.printStackTrace();
            statusCode = 500;
            response = "<error>Error while parsing XML</error>";
        }

        ResponseUtils.respondWithStatus(statusCode, response, resp, error);
    }

    @Override
    public void handleDeleteRequest(RequestFacade req, ResponseFacade resp) throws IOException, AccessViolationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private Document createGroupInfo(Group g) {
        Document doc = XMLHelper.newDocument();

        Element docEL = XMLHelper.addDocumentElement(doc, "group");
        Properties props = new Properties();

        props.setProperty("name", g.getName());
        props.setProperty("members", this.createCSList(g.getMembers()));
        props.setProperty("subgroups", this.createCSList(g.getSubGroups()));

        XMLHelper.addPropertyList(doc, docEL, props);

        return doc;
    }

    private Document createList( Set<String> entries, String root, String child ) {
        Document doc = XMLHelper.newDocument();
        Element rootEl = XMLHelper.addDocumentElement( doc, root );

        for (String u : entries) {
            Element el = XMLHelper.addElement(doc, rootEl , child );
            el.setAttribute("name", u);
        }

        return doc;
    }

    private Set<String> fromCSItem( String item ) {
        if (item != null && !item.equals("")) {
            String[] memberArray = item.split(",");
            return new HashSet<String>(Arrays.asList(memberArray));
        } else {
            return new HashSet<String>();
        }
    }

    private String createCSList( Collection<String> elements ) {
        StringBuilder b = new StringBuilder( elements.size() * 10 );
        int i = 0;
        for (String el : elements) {
            b.append(el);
            i++;
            if (i < elements.size()) {
                b.append(",");
            }
        }

        return b.toString();
    }

    private void retrieveUserImage( RequestFacade req, ResponseFacade resp, SingleUser u) throws IOException {
        Map<String, String> queryParams = RequestUtils.getQueryParameters( req );
        int width = -1;
        int height = -1;

        if (queryParams.get("width") != null && queryParams.get("height") != null) {
            try {
                width = Integer.parseInt(queryParams.get("width"));
                height = Integer.parseInt(queryParams.get("height"));
            } catch (NumberFormatException e) {
            }
        }
        this.respondWithUserPicture(u, width, height, resp);
    }

    private Document createUserInfo(LoginableUser user) {
        Document doc = XMLHelper.newDocument();
        Element docEl = XMLHelper.addDocumentElement(doc, "user");
        Properties props = new Properties();

        props.setProperty("name", user.getName());
        props.setProperty("isadmin", String.valueOf(user.isAdmin()));
        if ( user.getRealName() != null )
            props.setProperty( "realname", user.getRealName() );

        if ( user.getMail() != null )
            props.setProperty( "mail", user.getMail() );

        if ( user instanceof SingleUser ) {
            SingleUser u = ( SingleUser ) user;
            props.setProperty( "filesave", String.valueOf(u.isAllowedToSaveToFileSystem()));
            props.setProperty( "home", ModelManager.getInstance().getHomeLocation(u));
        }

        XMLHelper.addPropertyList(doc, docEl, props);

        return doc;
    }

    private void respondWithUserPicture( SingleUser u, int width, int height, ResponseFacade resp ) throws IOException {
        BufferedImage img = null;
        if ( u != null ) {
            img = ProcessEditorServerHelper.getUserManager().loadUserImage( u );
        }

        if (img == null) {
            URL url = resp.getResource("/symbols/question.png");
            img = ImageIO.read(url);
        }
        if (width < 0 || height < 0)
            ResponseUtils.respondWithImage(resp, img);
        else {
            int imgWidth = img.getWidth();
            int imgHeight = img.getHeight();
            
            double widthRatio = ((double) width) / ((double) imgWidth);
            double heightRatio = ((double) height) / ((double) imgHeight);

            double ratio = Math.min(widthRatio, heightRatio);

            width = (int)(imgWidth * ratio);
            height = (int) (imgHeight * ratio);
            Image scaled = img.getScaledInstance(width, height, BufferedImage.SCALE_DEFAULT);

            BufferedImage scaledImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            scaledImg.getGraphics().drawImage(scaled, 0, 0, null);

            ResponseUtils.respondWithImage(resp, scaledImg);
        }
    }

    private String savePicture( MultiPartItem picItem, BufferedInputStream is  ) {
        if (!picItem.getContent().trim().equals("") && picItem.getContentType() != null) {
            ImageType type = this.getImageType( picItem.getContentType() );

            if ( type != null ) {
                byte[] pic = new SimpleMultipartParser().parseItemContentAsByteArray(is, "pic");
                return ProcessEditorServerHelper.getPersistenceConnector().saveUserImage( null, type, pic );
            }
        }

        return null;
    }

    
    private ImageType getImageType( String contentType ) {
        if (contentType.equals(HttpConstants.CONTENT_TYPE_IMAGE_PNG))
            return ImageType.PNG;
        else if (contentType.equals(HttpConstants.CONTENT_TYPE_IMAGE_JPEG))
            return ImageType.JPG;
        else
            return null;
    }

    private boolean checkAdminRights( RequestFacade req, ResponseFacade resp) throws IOException {
        if (!RequestUtils.getCurrentUser(req).isAdmin())  {
            ResponseUtils.respondWithStatus(403, "No admin logged in", resp, true);
            return false;
        }

        return true;
    }
 }