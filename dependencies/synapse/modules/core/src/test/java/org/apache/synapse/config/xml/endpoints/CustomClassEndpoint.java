package org.apache.synapse.config.xml.endpoints;

import org.apache.synapse.MessageContext;
import org.apache.synapse.endpoints.AbstractEndpoint;

/**
 * Sample custom endpoint class used by ClassEndpointSerializationTest class
 *
 */
public class CustomClassEndpoint extends AbstractEndpoint{
	public void setFoo( String param){
		//TODO
	}
	public String getFoo(){
		return null;
		
	}
	public void onSuccess() {
		//TODO
	}
	public void send(MessageContext synMessageContext) {
		//TODO
	}
}
