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

package org.apache.synapse.endpoints.dispatch;


import org.apache.synapse.endpoints.Endpoint;
import org.apache.axis2.clustering.Member;

import java.io.Serializable;
import java.util.List;

/**
 * DataStructure for session information
 */
public class SessionInformation implements Serializable {

    private static final long serialVersionUID = -3771579091940569938L;
    private String id;
    private String rootEndpointName;
    private List<String> path;
    private long expiryTime;
    private long expireTimeWindow;
    private transient List<Endpoint> endpointList;
    private transient Member member;

    public SessionInformation(String id, List<Endpoint> endpointList, long expiryTime) {
        this.id = id;
        this.endpointList = endpointList;
        this.expiryTime = expiryTime;
    }

    public SessionInformation(String id, Member member, long expiryTime, long expireTimeWindow) {
        this.id = id;
        this.member = member;
        this.expiryTime = expiryTime;
        this.expireTimeWindow = expireTimeWindow;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getPath() {
        return path;
    }

    public void setPath(List<String> path) {
        this.path = path;
    }

    public long getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(long expiryTime) {
        this.expiryTime = expiryTime;
    }

    public void updateExpiryTime(){
        this.expiryTime = System.currentTimeMillis() + expireTimeWindow;    
    }

    public List<Endpoint> getEndpointList() {
        return endpointList;
    }

    public String getRootEndpointName() {
        return rootEndpointName;
    }

    public void setRootEndpointName(String rootEndpointName) {
        this.rootEndpointName = rootEndpointName;
    }

    public boolean isExpired() {
        return expiryTime < System.currentTimeMillis();
    }

    public Member getMember(){
        return member;
    }
    
    public void setMember(Member member) {
    	this.member = member;
    }
}

