package cognos.tm1.restapi.sessions;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ActiveSession {
	@JsonProperty("@odata.context")
	public String odata_context;
	
	@JsonProperty("ID")
	public int id;
	
	@JsonProperty("Context")
	public String context;
	
	@JsonProperty("Active")
	public boolean active;
}
