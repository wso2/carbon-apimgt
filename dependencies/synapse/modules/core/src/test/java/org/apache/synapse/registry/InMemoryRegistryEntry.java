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

package org.apache.synapse.registry;

import java.util.Date;

public class InMemoryRegistryEntry implements RegistryEntry {

    private long cachableDuration;
    private long createdDate;
    private long lastModifiedDate;
    private String key;
    private Object value;

    public InMemoryRegistryEntry(String key) {
        this.key = key;
        this.createdDate = new Date().getTime();
        this.lastModifiedDate = createdDate;
    }

    public void setCachableDuration(long cachableDuration) {
        this.cachableDuration = cachableDuration;
    }

    public void setLastModifiedDate(long lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public long getCachableDuration() {
        return cachableDuration;
    }

    public long getCreated() {
        return createdDate;
    }

    public String getDescription() {
        return "Resource at: " + key;
    }

    public String getKey() {
        return key;
    }

    public long getLastModified() {
        return lastModifiedDate;
    }

    public String getName() {
        return key;
    }

    public String getType() {
        return "text/xml";
    }

    public long getVersion() {
        return lastModifiedDate;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
        this.lastModifiedDate = new Date().getTime();
    }
}
