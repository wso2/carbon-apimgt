/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.notification.event;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tenant Management Event.
 */

public class TenantManagementEvent {

    @SerializedName("iss")
    private String iss;

    @SerializedName("jti")
    private String jti;

    @SerializedName("iat")
    private long iat;

    @SerializedName("events")
    private Map<String, EventDetail> events;


    public String getIss() {
        return iss;
    }

    public void setIss(String iss) {
        this.iss = iss;
    }

    public String getJti() {
        return jti;
    }

    public void setJti(String jti) {
        this.jti = jti;
    }

    public long getIat() {
        return iat;
    }

    public void setIat(long iat) {
        this.iat = iat;
    }

    public Map<String, EventDetail> getEvents() {
        if (events == null) {
            return null;
        }
        return new HashMap<>(events);
    }

    public void setEvents(Map<String, EventDetail> events) {
        if (events == null) {
            this.events = null;
        } else {
            this.events = new HashMap<>(events);
        }
    }

    /**
     * Represents the detailed information contained within a specific event type.
     */
    public static class EventDetail {

        @SerializedName("initiatorType")
        private String initiatorType;

        @SerializedName("tenant")
        private Tenant tenant;

        @SerializedName("action")
        private String action;


        public String getInitiatorType() {
            return initiatorType;
        }

        public void setInitiatorType(String initiatorType) {
            this.initiatorType = initiatorType;
        }

        public Tenant getTenant() {
            return (this.tenant != null) ? new Tenant(this.tenant) : null;
        }

        public void setTenant(Tenant tenant) {
            this.tenant = (tenant != null) ? new Tenant(tenant) : null;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }
    }

    /**
     * Represents the tenant object. This class includes all possible fields from all
     * event types. Fields not present in a specific JSON will be null.
     */
    public static class Tenant {

        @SerializedName("id")
        private String id;

        @SerializedName("domain")
        private String domain;

        @SerializedName("owners")
        private List<Owner> owners;

        @SerializedName("lifecycleStatus")
        private LifecycleStatus lifecycleStatus;

        @SerializedName("ref")
        private String ref;

        public Tenant() {
            
        }

        public Tenant(Tenant other) {
            this.id = other.id;
            this.domain = other.domain;
            this.ref = other.ref;
            this.setOwners(other.owners);
            this.setLifecycleStatus(other.lifecycleStatus); 
        }
        
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String name) {
            this.domain = name;
        }

        public List<Owner> getOwners() {
            if (this.owners == null) {
                return null;
            }
            return new ArrayList<>(this.owners);
        }

        public void setOwners(List<Owner> owners) {
            if (owners == null) {
                this.owners = null;
            } else {
                this.owners = new ArrayList<>(owners);
            }
        }

        public LifecycleStatus getLifecycleStatus() {
            return (this.lifecycleStatus != null) ? new LifecycleStatus(this.lifecycleStatus) : null;
        }

        public void setLifecycleStatus(LifecycleStatus lifecycleStatus) {
            this.lifecycleStatus = (lifecycleStatus != null) ? new LifecycleStatus(lifecycleStatus) : null;
        }

        public String getRef() {
            return ref;
        }

        public void setRef(String ref) {
            this.ref = ref;
        }
    }

    /**
     * Represents an owner of a tenant.
     */
    public static class Owner {

        @SerializedName("username")
        private String username;

        @SerializedName("password")
        private String password;

        @SerializedName("email")
        private String email;

        @SerializedName("firstname")
        private String firstname;

        @SerializedName("lastname")
        private String lastname;

        
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFirstname() {
            return firstname;
        }

        public void setFirstname(String firstname) {
            this.firstname = firstname;
        }

        public String getLastname() {
            return lastname;
        }

        public void setLastname(String lastname) {
            this.lastname = lastname;
        }
    }

    /**
     * Represents the lifecycle status of a tenant, used in activation/deactivation events.
     */
    public static class LifecycleStatus {

        @SerializedName("activated")
        private boolean activated;
        
        public LifecycleStatus() {
            
        }

        public LifecycleStatus(LifecycleStatus other) {
            this.activated = other.activated;
        }
        
        public boolean isActivated() {
            return activated;
        }

        public void setActivated(boolean activated) {
            this.activated = activated;
        }
    }
}
