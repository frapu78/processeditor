/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.isconn;

/**
 * Needs a running Inubit Integration Server with the workflow
 * "WS-ModuleStatistics", which can be found in the folder workflows , installed.
 * @author ff
 *
 */
public class ModuleInformationService {
	
	private static final String SIMULATION_INFORMATION_PROVIDER_URL = "http://localhost:8000/ibis/servlet/IBISHTTPUploadServlet/";
	private static final String GET_EXECUTION_TIMES_PART = "WS-MSGetExecutionTimes";
	private static final String GET_WAITING_MODULES_PART = "WS-MSGetWaitingModules";

	private String f_user = null;
	private String f_password = null;
	
	/**
	 * 
	 */
	public ModuleInformationService(String user, String password) {
		f_user = user;
		f_password = password;
	}
	
	public ExecutionTimesResponse getExecutionTimes(String moduleID,Long startTime,Long endTime) {
		String _params = getParams(moduleID);
		if(startTime != null) {
			_params += "&startTime="+startTime+"&endTime="+endTime;
		}
		String _response = HttpRequest.sendGetRequest(SIMULATION_INFORMATION_PROVIDER_URL+GET_EXECUTION_TIMES_PART, getParams(moduleID));
		return new ExecutionTimesResponse(_response);
	}
	
	public int getNumberOfWaitingModules(String moduleID) {
		String _response = HttpRequest.sendGetRequest(SIMULATION_INFORMATION_PROVIDER_URL+GET_WAITING_MODULES_PART, getParams(moduleID));
		_response = filterXMLTags(_response);
		try {
			return Integer.parseInt(_response);
		}catch(Exception ex) {
			return 0;
		}
	}

	private String filterXMLTags(String string) {
		return string.replaceAll("<.*?>", "");
	}

	/**
	 * @param moduleID
	 * @return
	 */
	private String getParams(String moduleID) {
		return "id="+moduleID.replace(" ", "%20")+"&user="+f_user+"&password="+f_password;
	}

	public String getUser() {
		return f_user;
	}
	
	public void setUser(String f_user) {
		this.f_user = f_user;
	}
	
	public String getPassword() {
		return f_password;
	}
	
	public void setPassword(String f_password) {
		this.f_password = f_password;
	}

}
