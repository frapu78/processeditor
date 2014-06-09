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
import com.inubit.research.server.MonitoringUtils;
import com.inubit.research.server.ProcessEditorServerUtils;
import com.inubit.research.server.errors.AccessViolationException;
import com.inubit.research.server.request.RequestFacade;
import com.inubit.research.server.request.RequestUtils;
import com.inubit.research.server.request.ResponseFacade;
import com.inubit.research.server.request.ResponseUtils;
import com.inubit.research.server.user.LoginableUser;
import net.frapu.code.visualization.reporting.BarChart;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author fel
 */
public class AdminRequestHandler extends AbstractRequestHandler {

    @Override
    public void handleGetRequest( RequestFacade req, ResponseFacade resp ) throws IOException, AccessViolationException {
        LoginableUser user = RequestUtils.getCurrentUser(req);
        String requestUri = req.getRequestURI();

        if (!user.isAdmin()) 
            throw new AccessViolationException("Admin sites can only be accessed by administrators!");

        try {
            if (requestUri.matches("/admin")) {
                ResponseUtils.respondWithServerResource(HttpConstants.CONTENT_TYPE_TEXT_HTML, "/html/admin.html", resp, false);
                return;
            } else if (requestUri.matches("/admin/data/response_times/\\d+")) {

                final Pattern edgeRequest = Pattern.compile("/response_times/(\\d+)");
                final Matcher m = edgeRequest.matcher(requestUri);
                if (m.find()) 
                    respondWithRecentResponseTimes(m.group(1), resp);
                
                return;
            }
            Pattern jsRequest = Pattern.compile("/admin/(.+?\\.html)");
            Matcher m = jsRequest.matcher(requestUri);
            if (m.find()) {
                ResponseUtils.respondWithServerResource(HttpConstants.CONTENT_TYPE_TEXT_HTML, "/html/" + m.group(1), resp, false);
            } else {
                ResponseUtils.respondWithStatus(404, "Not found.", resp, true);
            }
        } catch (FileNotFoundException ex) {
            ResponseUtils.respondWithStatus(404, ex.getMessage(), resp, true);
        } catch (Exception ex) {
            ResponseUtils.respondWithStatus(500, ex.getMessage(), resp, true);
        }
    }

    @Override
    public void handleDeleteRequest(RequestFacade req, ResponseFacade resp) throws IOException, AccessViolationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void handlePostRequest(RequestFacade req, ResponseFacade resp) throws IOException, AccessViolationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void handlePutRequest(RequestFacade req, ResponseFacade resp) throws IOException, AccessViolationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void respondWithRecentResponseTimes( String type, ResponseFacade resp )  throws IOException {
        try {
            int intType = Integer.parseInt(type);
            BarChart times = new BarChart();
            times.setSize(400, 200);
            switch (intType) {
                case MonitoringUtils.RESPONSE_TEMP_NODE_IMAGES:
                    times.setProperty(BarChart.PROP_TEXT, "Recent Response Times for Temporary Node Images");
                    break;
                case MonitoringUtils.RESPONSE_TEMP_JSHANDLER:
                    times.setProperty(BarChart.PROP_TEXT, "Recent Response Times for static JavaScript Requests");
                    break;
                default:
                    times.setProperty(BarChart.PROP_TEXT, "Recent Response Times for Temporary Misc. Requests");
            }
            times.setProperty(BarChart.PROP_YLABEL, "ms");
            times.setProperty(BarChart.PROP_XLABEL, "Requests");

            MonitoringUtils mu = MonitoringUtils.getInstance();
            long[] data = mu.getRecentResponseTimes(intType);
            List<Integer> pData = new LinkedList<Integer>();
            for (int i = 0; i < data.length; i++) {
                pData.add((int) data[i]/1000000);
            }

            times.setData(pData);

            BufferedImage i = ProcessEditorServerUtils.createNodeImage(times);
            ResponseUtils.respondWithImage(resp, i);
        } catch (Exception ex) {
            ResponseUtils.respondWithStatus(500, ex.getMessage(), resp, true);
        }
    }
}
