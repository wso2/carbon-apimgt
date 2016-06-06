/*
 *
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

package org.wso2.carbon.apimgt.samples.pizzashack.api;

import org.wso2.carbon.apimgt.samples.pizzashack.api.beans.Order;
import org.wso2.carbon.apimgt.samples.pizzashack.api.beans.ResponseMsg;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/order")
public class PizzaShackOrderAPI {			
	
	@POST
	@Consumes("application/json")
	@Produces("application/json")
	public Response placeOrder(Order order) {
		String orderId = OrderManager.getInstance().placeOrder(order);
		return Response.created(URI.create("order/" + orderId)).entity(order).build();
	}
	
	@GET
	@Produces("application/json")
	@Path("/{orderId}")
	public Response getOrder(@PathParam("orderId") String orderId) {
		Order order = OrderManager.getInstance().getOrder(orderId);
		if (order != null) {
			return Response.ok().entity(order).build();
		} else {
			ResponseMsg responseMsg = new ResponseMsg();
			responseMsg.setDescription("Order " + orderId +" not found");
			return Response.status(Response.Status.NOT_FOUND).entity(responseMsg).build();
		}
	}
	
	@DELETE
	@Produces("application/json")
	@Path("/{orderId}")
	public Response cancelOrder(@PathParam("orderId") String orderId) {
		boolean cancelled = OrderManager.getInstance().cancelOrder(orderId);
		if (cancelled) {
			return Response.ok().build();
		} else {
			ResponseMsg responseMsg = new ResponseMsg();
			responseMsg.setDescription("Order " + orderId +" not found");
			return Response.status(Response.Status.NOT_FOUND).entity(responseMsg).build();
		}
	}
	
	@PUT
	@Consumes("application/json")
	@Produces("application/json")
	@Path("/{orderId}")
	public Response updateOrder(@PathParam("orderId") String orderId, Order order) {
		boolean updated = OrderManager.getInstance().updateOrder(orderId, order);
		if (updated) {
			return Response.ok().entity(order).build();
		} else {
			ResponseMsg responseMsg = new ResponseMsg();
			responseMsg.setDescription("Order " + orderId +" not found");
			return Response.status(Response.Status.NOT_FOUND).entity(responseMsg).build();
		}
	}	

}

