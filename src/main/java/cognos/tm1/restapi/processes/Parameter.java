package cognos.tm1.restapi.processes;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Parameter {
	@JsonProperty("Name")
	public String name;
	
	@JsonProperty("Value")
	public String value;
	
	public Parameter(String name, String value) {
		this.name = name;
		this.value = value;
	}
}
