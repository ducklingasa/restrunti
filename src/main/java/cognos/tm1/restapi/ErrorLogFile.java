package cognos.tm1.restapi;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorLogFile {
	@JsonProperty("Filename")
	public String filename;
	
	@JsonProperty("Content")
	public String content;
}
