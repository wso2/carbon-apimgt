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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class OrderManager {
	
	private Map<String, Order> orders = new ConcurrentHashMap<String, Order>();

	private static final OrderManager instance = new OrderManager();
	
	private OrderManager() {
		
	}
	
	public static OrderManager getInstance() {
		return instance;
	}
	
	public String placeOrder(Order order) {
		String orderId = UUID.randomUUID().toString();
		order.setOrderId(orderId);
		orders.put(orderId, order);
		return orderId;
	}
	
	public Order getOrder(String orderId) {
		return orders.get(orderId);
	}
	
	public boolean updateOrder(String orderId, Order order) {
		if (orders.containsKey(orderId)) {
			order.setOrderId(orderId);
			orders.put(orderId, order);
			return true;
		}
		return false;
	}
	
	public boolean cancelOrder(String orderId) {
		if (orders.containsKey(orderId)) {
			orders.remove(orderId);
			return true;
		}
		return false;
	}
	
	public Order[] listOrders() {
		return orders.values().toArray(new Order[orders.size()]);
	}
	
	public boolean deliverOrder(String orderId) {
		Order order = orders.get(orderId);
		if (order != null) {
			order.setOrderId(orderId);
			order.setDelivered(true);
			return true;
		}
		return false;
	}
}
