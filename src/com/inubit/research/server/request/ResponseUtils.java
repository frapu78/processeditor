/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.request;

import com.inubit.research.server.HttpConstants;
import com.inubit.research.server.ProcessEditorServerHelper;
import com.inubit.research.server.ProcessEditorServerUtils;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Set;
import java.util.zip.GZIPOutputStream;
import javax.imageio.ImageIO;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;

/**
 *
 * @author fel
 */
public class ResponseUtils {

    private static volatile HashMap<String, File> gzipCache = new HashMap<String, File>();
    private static final String PORTLET_MODE_EXTENSION = "_notabs";

    public static void respondWithStatus(int status, String text, String contentType, ResponseFacade resp, boolean error) throws IOException {
        if (error) {
            text = "<html><head><title>Error</title></head><body><h1>" + status + " - " + text + "</h1><p>Please contact your local administrator.</p></body></html>";
            contentType = HttpConstants.CONTENT_TYPE_TEXT_HTML;
        }
        respondWithString(text, status, contentType, resp);
    }

    public static void respondWithStatus(int status, String text, ResponseFacade resp, boolean error) throws IOException {
        if (error) {
            text = "<html><head><title>Error</title></head><body><h1>" + status + " - " + text + "</h1><p>Please contact your local administrator.</p></body></html>";
        }

        respondWithString(text, status, HttpConstants.CONTENT_TYPE_TEXT_XML, resp);
    }

    public static void respondWithServerResource(String contentType, String path, ResponseFacade resp, Boolean compress) throws IOException {
        if (path != null) {

            // Search in local file system first
            File f = new File("www" + path);

            if (!f.exists()) {
                f = new File("pics" + path);
            }
            if (f.exists()) {
                if (compress) {
                    f = getCompressedFile(f);
                    resp.setHeader(HttpConstants.HEADER_KEY_CONTENT_ENCODING, HttpConstants.CONTENT_CODING_GZIP);
                }

                respondWithStream(new FileInputStream(f), 200, contentType, resp);
                return;
            } else {
                if (ProcessEditorServerHelper.isDebugMode()) {
                    respondWithString("The requested resource was not found on this server!", 404, HttpConstants.CONTENT_TYPE_TEXT_PLAIN, resp);
                    return;
                }
            }

            // Try to fetch from JAR
            URL url = resp.getResource(path);
            if (url != null) {
                InputStream is = url.openStream();
                respondWithStream(is, 200, contentType, resp);
                return;
            } else {
                Set<File> additionalDirs = ProcessEditorServerHelper.getAdditionalResourceDirectories();
                for (File dir : additionalDirs) {
                    f = new File(dir, path);
                    if (f.exists()) {
                        respondWithStream(new FileInputStream(f), 200, contentType, resp);
                        return;
                    }
                }
                // Respond with error
                respondWithString("The requested resource was not found on this server!", 404, HttpConstants.CONTENT_TYPE_TEXT_PLAIN, resp);                
            }
        }
    }

    private static File getCompressedFile(File f) throws IOException {
        // Check if found in cache
        String fileName = f.getPath();
        if (gzipCache.containsKey(fileName)) {
            File f_comp = gzipCache.get(fileName);
            // Check if still exists (might be removed by os!)
            if (f_comp.exists()) {
                // Check if original ressource is older
                long orgDate = f.lastModified();
                long cacheDate = f_comp.lastModified();
                if (orgDate < cacheDate) {
                    // Cache hit
                    return f_comp;
                } else {
                    // Delete outdated cache file
                    f_comp.delete();
                }
            }
            // Cache miss, content outdated (--> clear cache entry)
            gzipCache.remove(fileName);
        }

        // Compress input file if required
        File f_comp = File.createTempFile("tmp_", ".gzip");
        // Add to cache
        gzipCache.put(fileName, f_comp);
        f_comp.deleteOnExit();
        BufferedReader inb = new BufferedReader(
                new FileReader(f));
        BufferedOutputStream outb = new BufferedOutputStream(
                new GZIPOutputStream(new FileOutputStream(f_comp)));
        int c;
        while ((c = inb.read()) != -1) {
            outb.write(c);
        }
        inb.close();
        outb.close();

        return f_comp;
    }

    /**
     * Negotiate resource based on server configuration. By now, this is used to
     * determine if resources for portlet-mode have to be returned
     *
     * @param contentType
     * @param path
     * @param resp
     * @throws IOException
     */
    public static void respondWithNegotiatedServerResource(String contentType, String path, ResponseFacade resp) throws IOException {
        if (ProcessEditorServerHelper.isPortletModeConfigured()) {
            int dotIndex = path.lastIndexOf(".");
            String extension = path.substring(path.lastIndexOf("."));

            path = path.substring(0, dotIndex) + PORTLET_MODE_EXTENSION + extension;
        }

        respondWithServerResource(contentType, path, resp, false);
    }

    public static void respondWithImage(ResponseFacade resp, BufferedImage img) throws IOException {
        resp.setContentType(HttpConstants.CONTENT_TYPE_IMAGE_PNG);
        resp.setStatus(200);
        OutputStream os = resp.getOutputStream();

        //Write image to stream
        ImageIO.write(img, "png", os);
        os.close();
    }

    public static void respondWithXML(ResponseFacade resp, Document xmlDoc, int statusCode) throws IOException {
        resp.setContentType(HttpConstants.CONTENT_TYPE_APPLICATION_XML);
        resp.setStatus(statusCode);

        OutputStream os = resp.getOutputStream();
        ProcessEditorServerUtils.writeXMLtoStream(os, xmlDoc);
        os.close();
    }

    public static void respondWithJSON(ResponseFacade resp, JSONObject json, int statusCode) throws IOException {
        respondWithString(json.toString(), statusCode, HttpConstants.CONTENT_TYPE_APPLICATION_JSON, resp);
    }

    public static void respondWithJSONAsText(ResponseFacade resp, JSONObject json, int statusCode) throws IOException {
        respondWithString(json.toString(), statusCode, HttpConstants.CONTENT_TYPE_TEXT_HTML, resp);
    }

    public static void respondWithJSON(ResponseFacade resp, JSONArray json, int statusCode) throws IOException {
        respondWithString(json.toString(), statusCode, HttpConstants.CONTENT_TYPE_APPLICATION_JSON, resp);
    }

    public static void respondWithPDF(ResponseFacade resp, File f) throws IOException {
        InputStream r = new FileInputStream(f);
        respondWithStream(r, 200, HttpConstants.CONTENT_TYPE_APPLICATION_PDF, resp);
    }

    public static void respondWithFile(String contentType, File file, ResponseFacade resp) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        respondWithStream(fis, 200, contentType, resp);
    }

    private static void respondWithStream(InputStream is, int statusCode, String contentType, ResponseFacade resp) throws IOException {
        byte[] buf = new byte[2048];
        int len = 0;

        if (contentType != null) {
            resp.setContentType(contentType);
        }

        resp.setStatus(statusCode);

        OutputStream os = resp.getOutputStream();

        while ((len = is.read(buf)) > -1) {
            os.write(buf, 0, len);
        }

        os.close();
    }

    private static void respondWithString(String s, int statusCode, String contentType, ResponseFacade resp) throws IOException {
        respondWithStream(new ByteArrayInputStream(s.getBytes()), statusCode, contentType, resp);
    }
}
