package models;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import play.Logger;
import play.mvc.*;
import play.libs.*;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;

import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;

import play.libs.F.Callback;
import play.libs.F.Callback0;

import akka.actor.*;

public class WebSocketModel extends UntypedActor {
	
	// the neo4j database
	public static String DB_PATH= "data/graph.db/" ; 
	private static final GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);
	static{
		registerShutdownHook( graphDb );
	}
	
	// Default actor.
    static ActorRef defaultSpace = Akka.system().actorOf(new Props(WebSocketModel.class));
    
	// people connected
	static Map<String, WebSocket.Out<JsonNode>> connected = new HashMap<String, WebSocket.Out<JsonNode>>();
	
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
            
            try {
    			ObjectMapper mapper = new ObjectMapper();
    	        JsonFactory factory = mapper.getJsonFactory();
    	        JsonParser jp;
    	        
    	        jp = factory.createJsonParser("{\"connected\":\""+connected.size()+"\"}");
    	        JsonNode actualObj;
    			actualObj = mapper.readTree(jp);
    			join.out.write(actualObj);
    		} catch (JsonProcessingException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
            
        }
		else if (message instanceof Message){
			
			// Received a message. Do something with it depending on the content of the json object
            Message mess = (Message)message;
			Logger.info("Received: "+mess.message.toString()+ " FROM: "+mess.user);
			
			
			if (mess.message.get("action").asText().equals("save")){
				
				Transaction tx = graphDb.beginTx();
				try
				{
					Logger.info("creating node");
					// create or retrieve the index
					IndexManager index = graphDb.index();
					Index<Node> stories = index.forNodes("stories");
					Node newNode = graphDb.createNode();
					
					JsonNode node = mess.message.get("node");
					
					Iterator<Entry<String, JsonNode>> elementsIterator = node.getFields();  
				    while (elementsIterator.hasNext())  
				    { 
				    	Entry<String, JsonNode> element = elementsIterator.next();  
				    	newNode.setProperty(element.getKey(), element.getValue().asText());
				    }
				    
				    // add the node to the index
				    stories.add(newNode, "id", newNode.getProperty("title")+""+newNode.getId());
					
					/*Relationship relationship = firstNode.createRelationshipTo( secondNode, RelTypes.SONOF );
					relationship.setProperty( "message", "brave Neo4j " );
					*/
					
				    tx.success();
				    Logger.info("node created ->"+newNode.toString()+" title:"+newNode.getProperty("title"));
				    
				}
				finally
				{
				    tx.finish();
				}
				
			}
			else if (mess.message.get("action").asText().equals("get")){
				
				int id = mess.message.get("node").asInt();
				String title = mess.message.get("title").asText();
				
				IndexManager index = graphDb.index();
				Index<Node> stories = index.forNodes("stories");
				IndexHits<Node> hits = stories.get("id", title+""+id);
				
				Node node = hits.getSingle();
				
				ObjectMapper mapper = new ObjectMapper();
    	        JsonFactory factory = mapper.getJsonFactory();
    	        JsonParser jp;
    	        
    	        jp = factory.createJsonParser("{\"NODE\":\""+node.getProperty("text")+"\"}");
    	        JsonNode actualObj;
    			actualObj = mapper.readTree(jp);
    			connected.get(mess.user).write(actualObj);
				
			}
			
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
    
    // shut down neo4j locks
    private static void registerShutdownHook( final GraphDatabaseService graphDb )
    {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
            	Logger.info("Shutting down Neo4J");
                graphDb.shutdown();
            }
        } );
    }
    
    private static enum RelTypes implements RelationshipType
    {
        SONOF,
        ALTERNATIVEOF
    }
    
}
