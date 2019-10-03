package cognos.tm1.restapi.processes;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProcessParameters {
	@JsonProperty("Parameters")
	public List<Parameter> parameters = new ArrayList<Parameter>();
}
