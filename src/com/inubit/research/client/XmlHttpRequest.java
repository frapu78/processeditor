/**
 *
 * Process Editor - inubit Client Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.client;

import com.inubit.research.server.HttpConstants;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import com.inubit.research.server.ProcessEditorServerUtils;
import com.inubit.research.server.request.handler.UserRequestHandler;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * This class provides a synchronous HTTP call and returns the result
 * as a DOM object.
 *
 * @author fpu
 */
public class XmlHttpRequest {

    private long executionTime;
    private URI uri;
    private int lastStatus;
    private Map<String, String> headerProperties = new HashMap<String, String>();
    // nr of connection retries
    private int maxRetries = 10;
    private int currentRetries;
    private HttpURLConnection conn;

    public XmlHttpRequest(URI uri) {
        this.uri = uri;
        //just added to guarantee workbench functionality during development of user management
        //will be removed later
        //this.setRequestProperty("Cookie", UserHandler.SESSION_ATTRIBUTE+"=" + UserManager.MAGIG_SESSION_ID);
    }

    public int getLastStatus() {
        return lastStatus;
    }

    public Map<String, List<String>> getResponseHeaderFields() {
        if (conn != null) {
            return conn.getHeaderFields();
        }
        return null;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setRequestProperty(String property, String value) {
        headerProperties.put(property, value);
    }

    public void addCredentials(UserCredentials credentials) throws InvalidUserCredentialsException {
        if (credentials != null) {
            try {
                setRequestProperty(HttpConstants.HEADER_KEY_COOKIE, UserRequestHandler.SESSION_ATTRIBUTE + "=" + credentials.getSessionId());
            } catch (Exception e) {
                throw new InvalidUserCredentialsException("");
            }
        }
    }

    public HttpURLConnection getConnection() throws MalformedURLException, ProtocolException, IOException {
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod(HttpConstants.REQUEST_GET);
        // Set custom properties
        for (String key : headerProperties.keySet()) {
            conn.setRequestProperty(key, headerProperties.get(key));
        }
        // Set Accept property if not already filled
        if (conn.getRequestProperty(HttpConstants.HEADER_KEY_ACCEPT) == null) {
            conn.setRequestProperty(HttpConstants.HEADER_KEY_ACCEPT, HttpConstants.CONTENT_TYPE_APPLICATION_PROCESSMODEL);
        }
        conn.setDoOutput(false);
        // Save last status
        //  boolean statusReceived = false;
        currentRetries = 0;
        //lastStatus = conn.getResponseCode();
        return conn;
    }

    /**
     * Executes a GET request on the given URL.
     */
    public Document executeGetRequest() throws IOException, ParserConfigurationException, XMLHttpRequestException {
        conn = getConnection();
        DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
        xmlFactory.setNamespaceAware(false);
        DocumentBuilder builder = xmlFactory.newDocumentBuilder();
        long start = System.currentTimeMillis(); // start timing
        // start timing
        InputStream inputStream = conn.getInputStream();
        try {
            Document xmlDoc = builder.parse(inputStream);
            lastStatus = conn.getResponseCode();
            long stop = System.currentTimeMillis(); // stop timing
            executionTime = (stop - start);
            return xmlDoc;
        } catch (Exception ex) {
            ex.printStackTrace();
            if (conn.getResponseCode() < 200 || conn.getResponseCode() > 299) {
                throw new XMLHttpRequestException("Server error: " + conn.getResponseMessage() + " (" + lastStatus + ")", lastStatus);
            } else {
                throw new XMLHttpRequestException("Internal error: " + ex.getMessage(), conn.getResponseCode());
            }
        }
    }

    public Document executePostRequest(Document doc) throws MalformedURLException, IOException, XMLHttpRequestException {
        conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod(HttpConstants.REQUEST_POST);
        conn.setRequestProperty(HttpConstants.HEADER_KEY_CONTENT_TYPE, HttpConstants.CONTENT_TYPE_APPLICATION_XML);
        // Set custom properties
        for (String key : headerProperties.keySet()) {
            conn.setRequestProperty(key, headerProperties.get(key));
        }
        // Set Accept property if not filled
        if (conn.getRequestProperty(HttpConstants.HEADER_KEY_ACCEPT) == null) {
            conn.setRequestProperty(HttpConstants.HEADER_KEY_ACCEPT,
                    HttpConstants.CONTENT_TYPE_TEXT_XML + ","
                    + HttpConstants.CONTENT_TYPE_APPLICATION_XML);
        }
        conn.setDoOutput(true);
        boolean received = false;
        Document xmlDoc = null;
        currentRetries = 0;
        while (!received & currentRetries < maxRetries) {
            try {
                currentRetries++;
                long start = System.currentTimeMillis(); // start timing
                OutputStreamWriter osw = null;
                //if (doc!=null) {
                osw = new OutputStreamWriter(conn.getOutputStream(), "UTF8");
                ProcessEditorServerUtils.writeXMLtoStream(osw, doc);
                osw.flush();
                //}
                // Save last status
                lastStatus = conn.getResponseCode();
                received = true;
                // Get the response
                DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
                xmlFactory.setNamespaceAware(false);
                DocumentBuilder builder = xmlFactory.newDocumentBuilder();
                try {
                    xmlDoc = builder.parse(conn.getInputStream());
                } catch (SAXException sax) {
                    received = true;
                } finally {
                    /*if (doc!=null)*/ osw.close();
                }
                long stop = System.currentTimeMillis(); // stop timing
                executionTime = (stop - start);
            } catch (Exception ex) {
                if (conn.getResponseCode() < 200 || conn.getResponseCode() > 299) {
                    throw new XMLHttpRequestException("Server error: " + conn.getResponseMessage() + " (" + lastStatus + ")", lastStatus);
                } else {
                    throw new XMLHttpRequestException("Internal error: " + ex.getMessage(), conn.getResponseCode());
                }
            }
        }
        return xmlDoc;
    }

    public Document executePutRequest(Document doc) throws MalformedURLException, IOException, XMLHttpRequestException {
        conn.setFollowRedirects(false);
        conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod(HttpConstants.REQUEST_PUT);
        conn.setRequestProperty(HttpConstants.HEADER_KEY_CONTENT_TYPE, HttpConstants.CONTENT_TYPE_APPLICATION_XML);
        // Set custom properties
        for (String key : headerProperties.keySet()) {
            conn.setRequestProperty(key, headerProperties.get(key));
        }
        // Set Accept property
        conn.setRequestProperty(HttpConstants.HEADER_KEY_ACCEPT,
                HttpConstants.CONTENT_TYPE_TEXT_XML + ","
                + HttpConstants.CONTENT_TYPE_APPLICATION_XML);
        conn.setDoOutput(true);
        boolean received = false;
        Document xmlDoc = null;
        currentRetries = 0;
        InputStream inputStream;
        while (!received & currentRetries < maxRetries) {
            currentRetries++;
            try {                
                long start = System.currentTimeMillis(); // start timing
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream(), "UTF8");
                ProcessEditorServerUtils.writeXMLtoStream(osw, doc);
                osw.flush();
                received = true;

                //System.out.println("SENT: " + conn.getResponseCode());
                dumpResponseHeaders(conn);

                lastStatus = conn.getResponseCode();
                if (lastStatus != 204) { // No content
                    // Get the response
                    boolean xmlFound = false;
                    // Check if content type is application/xml
                        if (conn.getHeaderFields().get("Content-Type") != null) {
                        for (String type : conn.getHeaderFields().get("Content-Type")) {
                            if (type.contains("xml")) {
                                xmlFound = true;
                            }
                        }
                    }

                    if (xmlFound==true) {
                        // Response is XML
                        DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
                        xmlFactory.setNamespaceAware(false);
                        DocumentBuilder builder = xmlFactory.newDocumentBuilder();
                        inputStream = conn.getInputStream();
                        xmlDoc = builder.parse(inputStream);
                        osw.close();
                    } else {
                        // Response is something else (what should we do here in XML(!)HttpRequest? --> DUMP to console
                        //BufferedReader reader= new BufferedReader( new InputStreamReader(conn.getInputStream()));
                        //while (reader.ready()) {
                            //System.out.println(">"+reader.readLine());
                        //}
                        //osw.close();
                    }
                }
                long stop = System.currentTimeMillis(); // stop timing
                executionTime = (stop - start);
                // Check if response is within 200 range
                if (conn.getResponseCode() < 200 || conn.getResponseCode() > 299) {
                    throw new XMLHttpRequestException("Server error: " + conn.getResponseMessage() + " (" + lastStatus + ")", lastStatus);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                if (conn.getResponseCode() < 200 || conn.getResponseCode() > 299) {
                    throw new XMLHttpRequestException("Server error: " + conn.getResponseMessage() + " (" + lastStatus + ")", lastStatus);
                } else {
                    throw new XMLHttpRequestException("Internal error: " + ex.getMessage(), conn.getResponseCode());
                }
            }
        }
        return xmlDoc;
    }

    public void executeDeleteRequest() throws MalformedURLException, IOException {
        conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod(HttpConstants.REQUEST_DELETE);

        for (String key : headerProperties.keySet()) {
            conn.setRequestProperty(key, headerProperties.get(key));
        }

        conn.connect();

        lastStatus = conn.getResponseCode();
    }

    /**
     * Return the number of retries the Request needed until it received an answer
     * @return
     */
    public int getCurrentRetries() {
        return currentRetries;
    }

    public static void dumpResponseHeaders(HttpURLConnection httpReq) {
        // Dump response headers
        for (String key : httpReq.getHeaderFields().keySet()) {
            //System.out.print(key + ": ");
            for (String entry : httpReq.getHeaderFields().get(key)) {
               // System.out.print(entry + ", ");
            }
            //System.out.println();
        }
    }
}
