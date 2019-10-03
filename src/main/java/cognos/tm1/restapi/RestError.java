package cognos.tm1.restapi;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RestError {
	@JsonProperty("error")
	public Error error;
}
