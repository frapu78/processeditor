/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server;

/**
 *
 * @author fpu
 */
public class MonitoringUtils {

    private static MonitoringUtils utils = new MonitoringUtils();

    public final static int RESPONSE_TEMP_NODE_IMAGES = 0;
    public final static int RESPONSE_TEMP_MISC = 1;
    public final static int RESPONSE_TEMP_JSHANDLER = 2;

    private long[][] responseTimes = new long[3][100];
    private int[] responseTimesPos = new int[3];

    public static MonitoringUtils getInstance() {
        return utils;
    }

    public void addResponseTime(int type, long time) {
        responseTimes[type][utils.responseTimesPos[type]++] = time;
        if (responseTimesPos[type]>=responseTimes[type].length) responseTimesPos[type]=0;
    }

    public long[] getRecentResponseTimes(int type) {
        long[] result = new long[responseTimes[type].length];
        int p = responseTimesPos[type];
        for (int i=0; i<result.length; i++) {
            result[i]=responseTimes[type][p++];
            if (p>=responseTimes[type].length) p=0;
        }
        return result;
    }

}
