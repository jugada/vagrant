package models;

import java.util.ArrayList;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import play.Logger;
import play.mvc.*;
import play.libs.*;
import controllers.Node;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import play.libs.F.Callback;
import play.libs.F.Callback0;
import resources.GeneralMessages;
import akka.actor.*;

public class WebSocketModel extends UntypedActor {
	
	// Default actor.
    static ActorRef defaultSpace = Akka.system().actorOf(new Props(WebSocketModel.class));
    
	// people connected
	static Map<String, WebSocket.Out<JsonNode>> connected = new HashMap<String, WebSocket.Out<JsonNode>>();
	
	// individual stories reading
	static Map<Long, List<String>> storiesCount = new HashMap<Long, List<String>>();
	static Map<String, Long> storiesUser = new HashMap<String, Long>();
	
	// everytime a new websocket is opened we call this method to initializate the connection
	public static void connect(final String user, WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) throws Exception{
		
		// check if the user is already connected
		if(connected.containsKey(user)) {
			
			try {
				// create a json object to return the error. Is this the best way to do it?
				// if there is no easier way to create an object move this code to a custom class
				// so we can reuse it.
    			ObjectMapper mapper = new ObjectMapper();
    	        JsonFactory factory = mapper.getJsonFactory();
    	        JsonParser jp;
    	        
    	        jp = factory.createJsonParser("{\"error\":\"user already exists\"}");
    	        JsonNode actualObj;
    			actualObj = mapper.readTree(jp);
    			out.write(actualObj);
    		} catch (Exception e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
			
		} else {
			
			// all good, tell the actor that we want to join
			defaultSpace.tell(new Join(user, out), null);
			
			// set the listener to websocket messages
	        in.onMessage(new Callback<JsonNode>() {
	           public void invoke(JsonNode event) {
	        	   
	        	   // let the actor know we received a websocket message from a user
	               defaultSpace.tell(new Message(user, event), null);
	               
	           }
	           
	        });
	        
	        // When the socket is closed.
	        in.onClose(new Callback0() {
	           public void invoke() {
	               
	               // remove this dude
	        	   connected.remove(user);
	        	   // get the story where the user was
	        	   List<String> readers = new ArrayList<String>();
	        	   Long id = storiesUser.get(user);
	        	   readers = storiesCount.get(id);
	        	   readers.remove(readers.indexOf(user));
	        	   try {
					updateStoryCount(id);
					} catch (JsonParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        	   Logger.info("DISCONNECTED user "+user);
	               
	           }
	        });
	        
		}
	}

	// this is where the magic happens!
	@Override
	public void onReceive(Object message) throws Exception {
		// TODO Auto-generated method stub
		if(message instanceof Join) {
            
            // Received a Join message
            Join join = (Join)message;
            connected.put(join.user, join.out);
            
        }
		else if (message instanceof Message){
			
			// Received a message. Do something with it depending on the content of the json object
            Message mess = (Message)message;
			Logger.info("Received: "+mess.message.toString()+ " FROM: "+mess.user);
			Logger.info("Action: " + mess.message.get("action"));
			
			if (mess.message.get("action").asText().equals("save")){
				try
				{
					Logger.info("creating node");
					JsonNode node = mess.message.get("node");
					URI location = Node.createNode(node);
				    Logger.info("node created -> "+location.toString()+" title:"+node.findValuesAsText("title"));
				    
				    String path = location.getPath();
				    String id = path.substring(path.lastIndexOf('/') + 1);
				    
				    connected.get(mess.user).write(GeneralMessages.createJsonResponse("{\"id\":"+id+"}"));
				    
				}
				catch (Exception e) {
					Logger.info("node creation failed");
				}				
			}
			else if (mess.message.get("action").asText().equals("get")){
				
				int id = mess.message.get("node").asInt();	
				JsonNode node = Node.getNode(id);
    			connected.get(mess.user).write(node);
				
			}
			else if (mess.message.get("action").asText().equals("setStory")){
				
				Logger.info("GET: "+mess.message.toString());
				
				Long id = mess.message.get("id").asLong();
				
				List<String> readers = new ArrayList<String>();
				
				if(storiesCount.containsKey(id)) {
					
					readers = storiesCount.get(id);
					if (!readers.contains(mess.user)){
						readers.add(mess.user);
						updateStoryCount(id);
					}
					
				} else {
					readers.add(mess.user);
					storiesCount.put(id, readers);
					updateStoryCount(id);
				}
				
				storiesUser.put(mess.user, id);
				JsonNode node = Node.getNode(id.intValue());

    			connected.get(mess.user).write(GeneralMessages.generalMessage("{\"type\":\"dudes\",\"count\":\""+readers.size()+"\"}"));
    			
    			if (node == null ){
    				connected.get(mess.user).write(GeneralMessages.errorMessage("Not found"));
    			} else {
    				connected.get(mess.user).write(node);	
    			}
				
			}
			else if (mess.message.get("action").asText().equals("talk")){
				
				String username = mess.message.get("user").asText();
				String talk = mess.message.get("talk").asText();
    			connected.get(username).write(GeneralMessages.generalMessage("{\"type\":\"chat\",\"from\":\""+mess.user+"\",\"message\":\""+talk+"\"}"));
				
			}
			
		}
		
	}
	
	private static void updateStoryCount(Long id) throws JsonParseException, IOException{
		int size = storiesCount.get(id).size();
		for (String u: storiesCount.get(id)) {
			connected.get(u).write(GeneralMessages.generalMessage("{\"type\":\"dudes\",\"count\":\""+size+"\"}"));   
		}
	}
    
	// MESSAGES DEFINITIONS
	
    public static class Join {
        
        final String user;
        final WebSocket.Out<JsonNode> out;
        
        public Join(String user, WebSocket.Out<JsonNode> out) {
            this.user = user;
            this.out = out;
        }
        
    }
    
    public static class Message {
        
        final String user;
        final JsonNode message;
        
        public Message(String user, JsonNode message) {
            this.user = user;
            this.message = message;
        }
        
    }
    
}
