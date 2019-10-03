package cognos.tm1.restapi.processes;

import com.fasterxml.jackson.annotation.JsonProperty;

import cognos.tm1.restapi.ErrorLogFile;

public class ProcessExecuteResult {
	@JsonProperty("@odata.context")
	public String odata_context;
	
	@JsonProperty("ProcessExecuteStatusCode")
	public String status_code;
	
	@JsonProperty("ErrorLogFile")
	public ErrorLogFile error_log_file;
}