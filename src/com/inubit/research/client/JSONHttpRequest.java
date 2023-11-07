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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import javax.xml.parsers.ParserConfigurationException;
import com.inubit.research.server.request.handler.UserRequestHandler;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * This class provides a synchronous HTTP call and returns the result
 * as a JSON object.
 *
 * @author fpu
 */
public class JSONHttpRequest {

    private long executionTime;
    private URI uri;
    private int lastStatus;
    private Map<String, String> headerProperties = new HashMap<String, String>();
    // nr of connection retries
    private int maxRetries = 10;
    private int currentRetries;
    private HttpURLConnection conn;

    public JSONHttpRequest(URI uri) {
        this.uri = uri;
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
     * Executes a DELETE request on the given URL (added for completeness)
     */
    public JSONObject executeDeleteRequest() throws IOException, JSONException {
        conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setFollowRedirects(false);
        conn.setRequestMethod(HttpConstants.REQUEST_DELETE);
        conn.setRequestProperty(HttpConstants.HEADER_KEY_CONTENT_TYPE, HttpConstants.CONTENT_TYPE_APPLICATION_JSON);
        // Set custom properties
        for (String key : headerProperties.keySet()) {
            conn.setRequestProperty(key, headerProperties.get(key));
        }
        lastStatus = conn.getResponseCode();
        return parseResponse();
    }

    /**
     * Executes a GET request on the given URL.
     */
    public JSONObject executeGetRequest() throws 
            IOException, ParserConfigurationException, JSONException {
        conn = getConnection();
        return parseResponse();
    }

    /**
     * Exexcutes a PUT request on the given URL
     * @param obj
     */
    public JSONObject executePutRequest(JSONObject obj) throws
            MalformedURLException, IOException, JSONException {
        return executePostPutRequest(obj, false);
    }
    /**
     * Exexcutes a POST request on the given URL
     * @param obj
     */
    public JSONObject executePostRequest(JSONObject obj) throws
            MalformedURLException, IOException, JSONException {
        return executePostPutRequest(obj, true);
    }

    /**
     * Executes a PUT or POST request on the given URL.
     * @param isPost If true, a POST is sent, otherwise a PUT
     */
    protected JSONObject executePostPutRequest(JSONObject obj, boolean isPost) throws
            MalformedURLException, IOException, JSONException {
        //System.out.println("PUT "+uri);
        conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setFollowRedirects(false);
        if (isPost) {
            conn.setRequestMethod(HttpConstants.REQUEST_POST);
        } else {
            conn.setRequestMethod(HttpConstants.REQUEST_PUT);
        }
        conn.setRequestProperty(HttpConstants.HEADER_KEY_CONTENT_TYPE, HttpConstants.CONTENT_TYPE_APPLICATION_JSON);
        // Set custom properties
        for (String key : headerProperties.keySet()) {
            conn.setRequestProperty(key, headerProperties.get(key));
        }
        // Set Accept property
        conn.setRequestProperty(HttpConstants.HEADER_KEY_ACCEPT,
                HttpConstants.CONTENT_TYPE_APPLICATION_JSON + ","
                + HttpConstants.CONTENT_TYPE_TEXT_PLAIN);
        if (obj != null) {
            conn.setDoOutput(true);
            PrintWriter pw = new PrintWriter(conn.getOutputStream());
            pw.print(obj.toString());
            pw.flush();
        }
        lastStatus = conn.getResponseCode();
        return parseResponse();
    }

    private JSONObject parseResponse() throws IOException, JSONException {
        long start = System.currentTimeMillis(); // start timing
        InputStream inputStream = conn.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String result = "";
        try {
            while (!br.ready()) {
                // Wait till stream becomes ready
                Thread.sleep(1);
            }
        } catch (InterruptedException e) {};
        while (br.ready()) {
            result += br.readLine();
        }
        lastStatus = conn.getResponseCode();
        long stop = System.currentTimeMillis(); // stop timing
        executionTime = (stop - start);
        //System.err.println("Request time: "+executionTime+"ms");
        // Try to parse JSON from result
        JSONObject obj = new JSONObject(result);
        return obj;
    }

    public static void dumpResponseHeaders(HttpURLConnection httpReq) {
        // Dump response headers
        for (String key : httpReq.getHeaderFields().keySet()) {
            System.out.print(key + ": ");
            for (String entry : httpReq.getHeaderFields().get(key)) {
                System.out.print(entry + ", ");
            }
            System.out.println();
        }
    }
}
