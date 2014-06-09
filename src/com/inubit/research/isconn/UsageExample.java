/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.isconn;

import java.util.Calendar;

/**
 * 
 * Shows an example of how to use the ModuleInformationService
 * @author ff
 *
 */
public class UsageExample {

    private static final String USERNAME = "lecturer";
    private static final String PASSWORD = "inubit";
    //private static final String WS_ISConnector_wsdl = "http://localhost:8000/ibis/ws/WS_ISConnector?wsdl";

    public static void main(String[] args) {
        ModuleInformationService _mis = new ModuleInformationService(USERNAME, PASSWORD);

        //calculating dates (only take entries of the system log entered in the last week)
        Calendar c = Calendar.getInstance();
        long end = c.getTimeInMillis();
        c.add(Calendar.DATE, -7);
        long start = c.getTimeInMillis();
        //send request (start and end are optional and can be replaced with null)
        ExecutionTimesResponse executionTimes = _mis.getExecutionTimes("16434023", start, end);
        System.out.print("\t Total Time: " + executionTimes.getTotalTime());
        System.out.print("\t Count: " + executionTimes.getCount());
        System.out.println("\t Avg Time: " + executionTimes.getAverageTime());


    }
}
