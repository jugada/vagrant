package resources;

import java.io.IOException;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

public class GeneralMessages {

	public static JsonNode createJsonResponse(String message) throws JsonParseException, IOException{
		
		ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getJsonFactory();
        JsonParser jp;
        
        jp = factory.createJsonParser(message);
        JsonNode actualObj;
		actualObj = mapper.readTree(jp);
		
		return actualObj;
		
	}
	
	// retrieves an error mesage
	public static JsonNode errorMessage(String reason) throws JsonParseException, IOException{
		
		String message = "{\"status\":\"error\",\"reason\":\""+reason+"\"}";
		return createJsonResponse(message);
		
	}
	
	
	public static JsonNode successMessage(String reason) throws JsonParseException, IOException{
		
		String message = "{\"status\":\"success\",\"reason\":\""+reason+"\"}";
		return createJsonResponse(message);
		
	}
	
	// creates general message
	public static JsonNode generalMessage(String message) throws JsonParseException, IOException{
	
		return createJsonResponse(message);
		
	}
	
	
}
