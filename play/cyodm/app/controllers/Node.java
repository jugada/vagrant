package controllers;


import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.JsonNode;

import play.mvc.Controller;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class Node extends Controller {

	private static final String nodeEntryPointUri = "http://localhost:7474/db/data/node";

	public static URI createNode(JsonNode node) {
		WebResource resource = Client.create()
		        .resource( nodeEntryPointUri );
		// POST "node" to the node entry point URI
		ClientResponse response = resource.accept( MediaType.APPLICATION_JSON )
		        .type( MediaType.APPLICATION_JSON )
		        .entity( node.asText() )
		        .post( ClientResponse.class );
		 
		final URI location = response.getLocation();
		System.out.println( String.format(
		        "POST to [%s], status code [%d], location header [%s]",
		        nodeEntryPointUri, response.getStatus(), location.toString() ) );
		response.close();
		 
		return location;
	}
	
	public static void addProperty() {
		
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
}