package cognos.tm1.tiext;

import static cognos.tm1.ti.ProcessReturnCode.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.internetresources.util.UnsafeSSLHelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.cognos.tm1.javati.JavaTI;
import com.ibm.cognos.tm1.javati.TM1UserThread;
import com.ibm.cognos.tm1.javati.logging.LogFactory;
import com.ibm.cognos.tm1.javati.logging.Logger;
import com.ibm.cognos.tm1.javati.ti.TIFunctions;
import com.ibm.cognos.tm1.javati.ti.WrongThreadException;

import cognos.tm1.restapi.RestError;
import cognos.tm1.restapi.processes.Parameter;
import cognos.tm1.restapi.processes.ProcessExecuteResult;
import cognos.tm1.restapi.processes.ProcessParameters;
import kong.unirest.Callback;
import kong.unirest.Headers;
import kong.unirest.HttpResponse;
import kong.unirest.JacksonObjectMapper;
import kong.unirest.RequestBodyEntity;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.UnirestParsingException;
import kong.unirest.apache.ApacheAsyncClient;
import kong.unirest.apache.ApacheClient;

@JavaTI
public class BasicRestRunTI {
	private static final Logger log = LogFactory.getLogger(BasicRestRunTI.class);

	private static long start;
	private static String tm1_user = "N/A";
	private static String process_name;

	public static double BasicRestRunTI(String[] args) {
		
		try {
			TIFunctions ti = TM1UserThread.getTIFunctions();
			tm1_user = ti.tm1user();
		} catch (WrongThreadException wte) {
		}
		
		//BasicRestRunTI(apiEndpoint [0], wait [1], user_name[2], password[3], process[4], parameters[5+]);
		//user_name or }Basic or }CAMNamespace
		//password or base64(user:password) or base64(user:password:namespace)
		if (args.length < 5 && args.length % 2 == 0) {
			new IllegalArgumentException(Arrays.toString(args));
		} else log.debug(Arrays.toString(args));
		
		process_name = args[4];

        try {
			// Reset Unirest configuration
			Unirest.config().reset();
	        
			// Create Apache HttpClient with disabled SSL check and timeout
			final RequestConfig rc = RequestConfig.custom().setSocketTimeout(0).build();
			
			CloseableHttpClient client = HttpClientBuilder.create()
					.setDefaultRequestConfig(rc)
					.setSSLContext(UnsafeSSLHelper.createUnsecureSSLContext())
					.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
					.build();
			
			CloseableHttpAsyncClient asyncClient = HttpAsyncClientBuilder.create()
					.setDefaultRequestConfig(rc)
					.setSSLContext(UnsafeSSLHelper.createUnsecureSSLContext())
					.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
					.build();
			
	        // Configure Unirest with the custom Apache HTTPClient
			Unirest.config()
				.httpClient(ApacheClient.builder(client))
				.asyncClient(ApacheAsyncClient.builder(asyncClient));
			
	        // Configure Unirest Authentication
	        if (args[2].startsWith("}"))
	        	Unirest.config().setDefaultHeader("Authorization", args[2].substring(1) + " " + args[3]);
	        else
	    		Unirest.config().setDefaultBasicAuth(args[2], args[3]);
	        
	        // Configure Unirest to send and accept JSON
	        Unirest.config()
	        	.setDefaultHeader("Content-Type", "application/json")
	        	.setObjectMapper(new JacksonObjectMapper());
	        
	        // Define the REST base URL
        	URL apiEndpoint = new URL(args[0] + "/api/v1");
        	
        	ProcessParameters pp = new ProcessParameters();
        	
        	for (int i = 5; i < args.length; i = i + 2) {
        		Parameter p = new Parameter(args[i], args[i + 1]);
        		pp.parameters.add(p);
        	}
        	
	    	// Define the process execution request
	    	RequestBodyEntity request = Unirest.post(apiEndpoint + "/Processes('{name}')/tm1.ExecuteWithReturn")
	    		.routeParam("name", process_name.replace(" ", ""))
	    		.queryString("$expand", "ErrorLogFile")
	    		.body(pp);
	    	
	    	if (args[1].equals("1")) {
	        	// Login to the remote system
	        	@SuppressWarnings("rawtypes")
				HttpResponse login = Unirest.get(apiEndpoint + "/ActiveSession").asEmpty();
	    		Headers headers = login.getHeaders();
	    		headers.all().forEach(header -> {
	    			log.trace(header.getName() + ": " + header.getValue());
	    		});
	    		
	        	int status = login.getStatus();
	        	log.debug(status + ": " + login.getStatusText());
	        	if (status >= 200 && status < 300) {
	        		log.info("Remote model login successful");
	        	} else {
	        		log.error("Remote process \"" + process_name + "\" aborted during initialization");
	        		return PROCESS_EXIT_ON_INIT.toInt();
	        	}
	        	
	    		start = System.currentTimeMillis();
	    		log.info("Process \"" + process_name + "\" remotely executed by user \"" + tm1_user +"\"");
	    		HttpResponse<ProcessExecuteResult> response = request.asObject(ProcessExecuteResult.class);
	    		int code = DecodeExecutionResult(response, process_name, tm1_user);
	    		
	    		// Logout from the remote system
	    		@SuppressWarnings({ "unused", "rawtypes" })
				HttpResponse none = Unirest.post(apiEndpoint + "/ActiveSession/tm1.Close").asEmpty();
	    		
				return code;
	    	} else {
	    		start = System.currentTimeMillis();
	    		asyncClient.start();
	    		@SuppressWarnings("unused")
				CompletableFuture<HttpResponse<ProcessExecuteResult>> future =
	    				request.asObjectAsync(ProcessExecuteResult.class, new Callback<ProcessExecuteResult>() {
							@Override
							public void completed(HttpResponse<ProcessExecuteResult> response) {
					    		Headers headers = response.getHeaders();
					    		String cookie = headers.getFirst("Set-Cookie").split(";")[0];
					    		headers.all().forEach(header -> {
					    			log.trace(header.getName() + ": " + header.getValue());
					    		});
					    		
					    		int code = DecodeExecutionResult(response, process_name, tm1_user);
					    		
								@SuppressWarnings("rawtypes")
								HttpResponse none =	Unirest.post(apiEndpoint + "/ActiveSession/tm1.Close").header("Cookie", cookie).asEmpty();
								
								try {
									asyncClient.close();
								} catch (IOException e) {
								}
					    		Unirest.shutDown();
							}
							@Override
							public void failed(UnirestException e) {
								log.error("Execution failed: " + e.getMessage());
							}
							@Override
							public void cancelled() {
								log.error("Execution cancelled!");
							}
						});
	    		log.info("Remote process \"" + process_name + "\" asyncly executed by user \"" + tm1_user +"\"");
	    		return PROCESS_IN_PROGRESS.toInt();
	    	}
		} catch (MalformedURLException mue) {
			log.error("API Endpoint error: " + mue.getMessage());
			// Abort: ProcessExitOnInit()
			return PROCESS_EXIT_ON_INIT.toInt();
		} catch (UnirestException ue) {
			Throwable cause = ue.getCause();
			if (cause != null && cause.getClass().equals(java.net.SocketTimeoutException.class)) {
				log.warn("In progress error: " + cause.getMessage());
				return EXECUTION_TIMEOUT.toInt();
			}
			log.error("Unirest error: " + ue.getMessage());
			// Abort: ProcessExitOnInit()
			return PROCESS_EXIT_ON_INIT.toInt();
		} finally {
			if (args[1].equals("1")) Unirest.shutDown();
		}
	}
	
	private static int DecodeExecutionResult(HttpResponse<ProcessExecuteResult> response, String process_name, String tm1_user) {
		long timeElapsed = System.currentTimeMillis() - start;
		int status = response.getStatus();
		log.debug(status + ": " + response.getStatusText());
		if (status < 200 || status >= 300) {
    		log.error("Remote process \"" + process_name + "\" aborted during initialization");
    		return PROCESS_EXIT_ON_INIT.toInt();
    	}
    	
		ProcessExecuteResult result = response.getBody();
		
		if (result == null) {
			UnirestParsingException ex = response.getParsingError().get();
			ObjectMapper mapper = new ObjectMapper();
			try {
				RestError re = mapper.readValue(ex.getOriginalBody(), RestError.class);
				log.error("Remote process \"" + process_name + "\" execution was aborted: " + re.error.message);
			} catch (Exception e) {
				log.error("Remote process \"" + process_name + "\" execution was aborted: N/A");
			}
			return PROCESS_EXIT_SERIOUS_ERROR.toInt();
		} else
			switch (result.status_code) {
	    		case "CompletedSuccessfully":
		    		log.info("Remote process \"" + process_name + "\":  Finished executing normally, elapsed time " + timeElapsed / 1000.0  + " seconds");
		    		return PROCESS_EXIT_NORMAL.toInt(); // OK, Break
	       		case "Aborted":
		    		log.fatal("Remote process \"" + process_name + "\": Execution was aborted. Error file: <" + result.error_log_file.filename + ">");
		    		return PROCESS_EXIT_SERIOUS_ERROR.toInt(); // Major
	       		case "HasMinorErrors":
		    		log.error("Remote process \"" + process_name + "\":  Finished executing with errors. Error file: <" + result.error_log_file.filename + ">");
		    		return PROCESS_EXIT_MINOR_ERROR.toInt(); // Minor
	    		case "QuitCalled":
		    		log.error("Remote process \"" + process_name + "\": Execution was aborted by ProcessQuit Function");
		    		return PROCESS_EXIT_BY_QUIT.toInt(); // ProcessQuit, ChoreQuit
	       		case "CompletedWithMessages":
		    		log.warn("Remote process \"" + process_name + "\":  Finished executing normally. Check new message in the file: <" + result.error_log_file.filename + ">");
		    		return PROCESS_EXIT_WITH_MESSAGE.toInt(); // Reject
	       		case "RollbackCalled":
		    		log.warn("Remote process \"" + process_name + "\":  Execution was cancelled. Rollback called");
		    		return PROCESS_EXIT_ROLLBACK.toInt(); // CancelOperation
	    		};
	    return PROCESS_EXIT_UNKNOWN.toInt();
	}
}
