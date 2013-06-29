package controllers;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

import play.Logger;
import play.mvc.Controller;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class Node extends Controller {

	private static final String nodeEntryPointUri = "http://localhost:7474/db/data/node";

	public static JsonNode getNode(int id) throws Exception {
		WebResource resource = Client.create()
		        .resource( nodeEntryPointUri + "/" + id + "/properties");
		// GET "node" from the node URI
		ClientResponse response = resource.accept( MediaType.APPLICATION_JSON )
		        .type( MediaType.APPLICATION_JSON )
		        .get( ClientResponse.class );
		
		ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getJsonFactory();
        JsonParser jp;
        JsonNode node;
		try {
			jp = factory.createJsonParser(response.getEntity(String.class));
			node = mapper.readTree(jp);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw (e);
		}
		response.close();
		
		return node;
	}
	
	public static URI createNode(JsonNode node) {
		Logger.info("Node is " + node.toString());
		WebResource resource = Client.create()
		        .resource( nodeEntryPointUri );
		// POST "node" to the node entry point URI
		ClientResponse response = resource.accept( MediaType.APPLICATION_JSON )
		        .type( MediaType.APPLICATION_JSON )
		        .entity( "{}" )
		        .post( ClientResponse.class );
		 
		final URI location = response.getLocation();
		
		resource = Client.create()
		        .resource( location.toString() + "/properties" );
		response = resource.accept( MediaType.APPLICATION_JSON )
		        .type( MediaType.APPLICATION_JSON )
		        .entity( node.toString() )
		        .put( ClientResponse.class );
		Logger.info( String.format(
		        "PUT [%s] to [%s], status code [%d]", node.toString(),
		        location.toString() + "/properties", response.getStatus()));
		response.close();
		 
		return location;
	}
	
	private static URI addRelationship( URI start, URI end, String type, String properties ) throws URISyntaxException {
	    URI fromUri = new URI( start.toString() + "/relationships" );
	    String relationshipJson = generateJsonRelationship( end, type, properties );
	 
	    WebResource resource = Client.create()
	            .resource( fromUri );
	    // POST JSON to the relationships URI
	    ClientResponse response = resource.accept( MediaType.APPLICATION_JSON )
	            .type( MediaType.APPLICATION_JSON )
	            .entity( relationshipJson )
	            .post( ClientResponse.class );
	 
	    final URI location = response.getLocation();
	    System.out.println( String.format(
	            "POST to [%s], status code [%d], location header [%s]",
	            fromUri, response.getStatus(), location.toString() ) );
	 
	    response.close();
	    return location;
	}

    private static String generateJsonRelationship( URI end, String type, String... properties ) {
        StringBuilder sb = new StringBuilder();
        sb.append( "{ \"to\" : \"" );
        sb.append( end.toString() );
        sb.append( "\", " );

        sb.append( "\"type\" : \"" );
        sb.append( type );
        if ( properties == null || properties.length < 1 )
        {
            sb.append( "\"" );
        }
        else
        {
            sb.append( "\", \"data\" : " );
            for ( int i = 0; i < properties.length; i++ )
            {
                sb.append( properties[i] );
                if ( i < properties.length - 1 )
                { // Miss off the final comma
                    sb.append( ", " );
                }
            }
        }

        sb.append( " }" );
        return sb.toString();
    }	
}