package cognos.tm1.restapi;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Error {
	@JsonProperty("code")
	public String code;
	
	@JsonProperty("message")
	public String message;
}
