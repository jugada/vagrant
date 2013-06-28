package controllers;

import play.Logger;
import play.libs.Json;
import play.mvc.*;
import models.*;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

public class Application extends Controller {
  
    public static Result index() {
        return ok("CYODM app standing by!.");
    }
    
    /**
     * Websocket imlementatioon
     * 
     * Our sockets will transport JSON messages
     * 
     * @return
     */
    public static WebSocket<JsonNode> story(final String user) {
        return new WebSocket<JsonNode>() {
            
            public void onReady(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) {

            	try{
            		
            		WebSocketModel.connect(user, in, out);
            		Logger.info("CONNECTED user "+user);
            		
            	}catch (Exception e){
            		
            		Logger.info("ERROR ON SOCKET CONNECTION");
            		e.printStackTrace();
            		
            	}
            }
        };
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

