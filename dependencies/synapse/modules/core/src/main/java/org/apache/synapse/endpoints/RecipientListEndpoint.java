/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.synapse.endpoints;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.clustering.Member;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.Value;
import org.apache.synapse.mediators.eip.EIPConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.apache.synapse.util.MessageHelper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import javax.xml.stream.XMLStreamException;

/**
 * @author nuwan
 * <p>
 * A Recipient List endpoint can contain multiple child endpoints or member elements. 
 * It routes cloned copies of messages to each child recipient. This will assume that 
 * all immediate child endpoints are identical in state (state is replicated) or state 
 * is not maintained at those endpoints.
 * </p>
 */
public class RecipientListEndpoint extends AbstractEndpoint {

    private static final Log log = LogFactory.getLog(RecipientListEndpoint.class);
    private static final String DELIMETER = ",";
    /**
	 * The list of members to which the message is delivered to
	 */
	private List<Member> members;

    private Map<String,Endpoint> dynamicEndpointPool ;
    private Value dynamicEnpointSet;
    public final static int DEFAULT_MAX_POOL = 20;
	/**
	 * Should this recipient list failover;
	 */
	private boolean failover;
    private int currentPool;

    private SynapseEnvironment env = null;

    public RecipientListEndpoint(int poolsize){
        dynamicEndpointPool = Collections.synchronizedMap(new DynamicEndpointPool<String, Endpoint>(poolsize));
        this.currentPool = poolsize;
    }

    public RecipientListEndpoint(){
        this.currentPool = DEFAULT_MAX_POOL;
    }

	@Override
	public void init(SynapseEnvironment synapseEnvironment) {
		if (!initialized) {
			super.init(synapseEnvironment);
		}
        this.env = synapseEnvironment;
        
        this.setContentAware(true);
	}

	@Override
	public void destroy() {
		super.destroy();
	}

	@Override
	public void send(MessageContext synCtx) {

		if (log.isDebugEnabled()) {
			log.debug("Sending using Recipient List " + toString());
		}

        if (getContext().isState(EndpointContext.ST_OFF)) {
            informFailure(synCtx, SynapseConstants.ENDPOINT_RL_NONE_READY,
                    "RecipientList endpoint : " + getName() != null ? getName() : SynapseConstants.ANONYMOUS_ENDPOINT + " - is inactive");
            return;
        }

		List<Endpoint> children = getChildren();
		
		//Service child endpoints
        if (children != null && !children.isEmpty()) {
            sendToEndpointList(synCtx, children);
        }
        //Service member elements if specified
        else if (members != null && !members.isEmpty()) {
            sendToApplicationMembers(synCtx);
        }
        else if (dynamicEnpointSet != null) {
            sendToDynamicMembers(synCtx);
        }
        else {
            String msg = "No child endpoints nor member elements available";
            log.error(msg);
            throw new SynapseException(msg);
        }
    }

    private void sendToEndpointList(MessageContext synCtx, List<Endpoint> children) {
        int i = 0;
        boolean foundEndpoint = false;
        
        //we should build the message, its should have the same behavior as clone mediator
        try {
	        RelayUtils.buildMessage(((Axis2MessageContext) synCtx).getAxis2MessageContext(),false);
        } catch (Exception e) {
        	  handleException("Error while building message", e);
        }

        for (Endpoint childEndpoint : children) {

            if (childEndpoint.readyToSend()) {
                foundEndpoint = true;
                MessageContext newCtx = null;
                try {
                    newCtx = MessageHelper.cloneMessageContext(synCtx);
                } catch (AxisFault e) {
                    handleException("Error cloning the message context", e);
                }

                //Used when aggregating responses
                newCtx.setProperty(EIPConstants.MESSAGE_SEQUENCE,
                                   String.valueOf(i++) + EIPConstants.MESSAGE_SEQUENCE_DELEMITER +
                                   children.size());

                // evaluate the endpoint properties
                evaluateProperties(newCtx);

                newCtx.pushFaultHandler(this);
                try {
                    childEndpoint.send(newCtx);
                } catch (SynapseException e) {

                    String msg ="Child Endpoint " + (childEndpoint.getName() != null ? childEndpoint.getName() : SynapseConstants.ANONYMOUS_ENDPOINT) +
                                " of Recipient List endpoint " + (getName() != null ? getName() : SynapseConstants.ANONYMOUS_ENDPOINT) + " encountered an error while sending the message";
                    log.warn(msg);
                    continue;        //continue sending message to rest of the child endpoints.
                }
            }
        }

        if (!foundEndpoint) {
            String msg = "Recipient List endpoint : " +
                         (getName() != null ? getName() : SynapseConstants.ANONYMOUS_ENDPOINT) +
                         " - no ready child endpoints";
            log.warn(msg);
            informFailure(synCtx, SynapseConstants.ENDPOINT_RL_NONE_READY, msg);
        }
    }

    private void sendToDynamicMembers(MessageContext synCtx) {
        String dynamicUrlStr = dynamicEnpointSet.evaluateValue(synCtx);
        String[] dynamicUrlSet = dynamicUrlStr.split(DELIMETER);
        if (dynamicUrlSet.length == 0) {
            log.warn("No recipient/s was derived from the expression : " + dynamicEnpointSet.toString());
            return;
        }
        List<Endpoint> children = new ArrayList<Endpoint>();
        for (String url : dynamicUrlSet) {
            if (url != null && !"".equals(url.trim())) {
                //get an Endpoint from the pool
                Endpoint epFromPool = dynamicEndpointPool.get(url);
                if (epFromPool == null) {
                    AddressEndpoint endpoint = new AddressEndpoint();
                    endpoint.setEnableMBeanStats(false);
                    endpoint.setName("DYNAMIC_RECIPIENT_LIST_EP_" + UUID.randomUUID());
                    EndpointDefinition definition = new EndpointDefinition();
                    definition.setReplicationDisabled(true);
                    definition.setAddress(url);
                    endpoint.setDefinition(definition);
                    endpoint.init(env);
                    //finally add the newly created endpoint to the Pool
                    dynamicEndpointPool.put(url, endpoint);
                    children.add(endpoint);
                } else {
                    //do nothing endpoint is already in the pool
                    children.add(epFromPool);
                }
            }
        }
        if (children.size() > 0) {
            sendToEndpointList(synCtx, children);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Halted sending messages to recipients. No recipient found !!! : " + dynamicUrlStr);
            }
        }
    }

    /**<p>Iterates the <b>members</b> list, creates Address Endpoints
	 * from each member element and routes cloned copies of the message
	 * to each Address Endpoint.</p>
	 * @param synCtx - The Original Message received by Synapse
	 */
	private void sendToApplicationMembers(MessageContext synCtx){
		
		int i = 0;
		boolean foundEndpoint = false;
		
		for (Member member : members) {
			
			org.apache.axis2.context.MessageContext axis2MsgCtx = ((Axis2MessageContext) synCtx)
					.getAxis2MessageContext();

			String transport = axis2MsgCtx.getTransportIn().getName();

			//If the transport is not HTTP nor HTTPS
			if (!transport.equals("http") && !transport.equals("https")) {
				//Skip member.
				log.error("Cannot deliver for non-HTTP/S transport " + transport);
				continue;
			}	
			
			MessageContext newCtx = null;

			try {
				newCtx = MessageHelper.cloneMessageContext(synCtx);
			} catch (AxisFault e) {
				handleException("Error cloning the message context", e);
			}

			// Used when aggregating responses
			newCtx.setProperty(
					EIPConstants.MESSAGE_SEQUENCE,
					String.valueOf(i++)
							+ EIPConstants.MESSAGE_SEQUENCE_DELEMITER
							+ members.size());

			// evaluate the endpoint properties
			evaluateProperties(newCtx);

			// URL rewrite
			String address = newCtx.getTo().getAddress();
			if (address.indexOf(":") != -1) {
				try {
					address = new URL(address).getPath();
				} catch (MalformedURLException e) {
					String msg = "URL " + address + " is malformed";
					log.error(msg, e);
					throw new SynapseException(msg, e);
				}
			}

			EndpointReference epr = new EndpointReference(transport
					+ "://"
					+ member.getHostName()
					+ ":"
					+ ("http".equals(transport) ? member.getHttpPort()
							: member.getHttpsPort()) + address);

			newCtx.setTo(epr);
			newCtx.pushFaultHandler(this);

			AddressEndpoint endpoint = new AddressEndpoint();
			EndpointDefinition definition = new EndpointDefinition();
			endpoint.setDefinition(definition);
			endpoint.init(newCtx.getEnvironment());
			
			if(endpoint.readyToSend()){
				foundEndpoint = true;
				endpoint.send(newCtx);
			}
		}
		
		if(!foundEndpoint){
			String msg = "Recipient List endpoint : " +
                    (getName() != null ? getName() : SynapseConstants.ANONYMOUS_ENDPOINT) +
                    " - no ready child members";
            log.warn(msg);
            informFailure(synCtx, SynapseConstants.ENDPOINT_RL_NONE_READY, msg);
		}
	}

	@Override
	public boolean readyToSend(){
        if (getContext().isState(EndpointContext.ST_OFF)) {
            return false;
        }

		for(Endpoint endpoint : getChildren()){
			if(endpoint.readyToSend()){
				if (log.isDebugEnabled()) {
                    log.debug("Recipient List " + this.toString()
                            + " has at least one endpoint at ready state");
                }
                return true;
			}
		}
		return false;
	}

    public void onChildEndpointFail(Endpoint endpoint, MessageContext synMessageContext) {
        //we just log the failed recipient here
        logOnChildEndpointFail(endpoint, synMessageContext);
        String msg = "";
        if (log.isDebugEnabled()) {
            msg = "Recipient List endpoint : " +
                         (getName() != null ? getName() : SynapseConstants.ANONYMOUS_ENDPOINT) +
                         " - one of the recipients encounterd an error while sending the message ";
            log.debug(msg);
        }
        informFailure(synMessageContext,SynapseConstants.ENDPOINT_RL_NONE_READY, msg);
    }


	public List<Member> getMembers() {
		return members;
	}

	public void setMembers(List<Member> members) {
		this.members = members;
	}

	public boolean isFailover() {
		return failover;
	}

	public void setFailover(boolean failover) {
		this.failover = failover;
	}

    public Value getDynamicEnpointSet() {
        return dynamicEnpointSet;
    }

    public void setDynamicEnpointSet(Value dynamicEnpointSet) {
        this.dynamicEnpointSet = dynamicEnpointSet;
    }

    public int getCurrentPoolSize() {
        return currentPool;
    }

    /**
     * create a simple LRU cached Endpoint pool for dynamic endpoints
     */
    private static class DynamicEndpointPool<String, Endpoint> extends LinkedHashMap<String, Endpoint> {

        private final int maxPoolSize;

        public DynamicEndpointPool(final int max) {
            super(max + 1, 1.0f, true);
            this.maxPoolSize = max;
        }

        @Override
        protected boolean removeEldestEntry(final Map.Entry<String, Endpoint> eldest) {
            return super.size() > maxPoolSize;
        }


    }

}
