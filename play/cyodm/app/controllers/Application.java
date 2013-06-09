package controllers;

import play.libs.Json;
import play.mvc.*;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

public class Application extends Controller {
  
    public static Result index() {
        return ok("Your new application is ready.");
    }
    
    @BodyParser.Of(BodyParser.Json.class)
    public static Result sayTest() {
      JsonNode json = request().body().asJson();
      String name = json.findPath("name").getTextValue();
      if(name == null) {
        return badRequest("Missing parameter [name]");
      } else {
        return ok("Hello " + name);
      }
    }
    
    public static Result test(Long id) {
    	ObjectNode result = Json.newObject();
    	result.put("status", "OK");
        result.put("message", "Get method for id = " + id);
        return ok(result);
    }
  
}

