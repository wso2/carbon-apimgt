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

package org.apache.synapse.startup.tasks;

public class RegistryResourceFetcherView implements RegistryResourceFetcherViewMBean {

    private RegistryResourceFetcher resourceFetcher = null;

    public RegistryResourceFetcherView(RegistryResourceFetcher resourceFetcher) {
        this.resourceFetcher = resourceFetcher;
    }

    public void activate() {
        resourceFetcher.setState(State.ACTIVE);
    }

    public void suspend() {
        resourceFetcher.setState(State.SUSPENDED);
    }

    public int getState() {
        return resourceFetcher.getState().getState();
    }

    public String getStateName() {
        return resourceFetcher.getState().getName();
    }

    public int getBackOffFactor() {
        return resourceFetcher.getBackOffFactor();
    }

    public int getMaxSuspendThreshold() {
        return resourceFetcher.getMaxSuspendThreshold();
    }

    public int getSuspendThreshold() {
        return resourceFetcher.getSuspendThreshold();
    }

    public void setBackOffFactor(int backOffFactor) {
        resourceFetcher.setBackOffFactor(backOffFactor);
    }

    public void setMaxSuspendThreshold(int maxSuspendThreshold) {
        resourceFetcher.setMaxSuspendThreshold(maxSuspendThreshold);
    }

    public void setSuspendThreshold(int suspendThreshold) {
        resourceFetcher.setSuspendThreshold(suspendThreshold);
    }
}
