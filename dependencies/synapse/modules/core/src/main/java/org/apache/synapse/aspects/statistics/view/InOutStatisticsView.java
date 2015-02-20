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
package org.apache.synapse.aspects.statistics.view;

import org.apache.synapse.aspects.ComponentType;

/**
 * View of statistics as in and out
 */
public class InOutStatisticsView {

    private final Statistics inStatistics;
    private final Statistics outStatistics;
    private String resourceId;
    private String owner;
    private ComponentType componentType;

    public InOutStatisticsView(String id, String owner, ComponentType type) {
        this.resourceId = id;
        this.owner = owner;
        this.componentType = type;
        this.inStatistics = new Statistics(id);
        // endpoints doesn't contain an out view since it is just sending the message to one side
        if (ComponentType.ENDPOINT.equals(type)) {
            this.outStatistics = null;
        } else {
            this.outStatistics = new Statistics(id);
        }
    }

    public Statistics getInStatistics() {
        return inStatistics;
    }

    public Statistics getOutStatistics() {
        return outStatistics;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getOwner() {
        return owner;
    }

    public ComponentType getComponentType() {
        return componentType;
    }

    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append("[Statistics Category : ").append(componentType).append(" ]");
        sb.append("[ Owner Id :").append(owner).append(" ][ Resource ID : ")
                .append(resourceId).append(" ]");

        if (inStatistics.getCount() != 0) {
            sb.append(" [ InFlow :  ")
                    .append(inStatistics.toString()).append(" ]");
        }

        if (outStatistics != null && outStatistics.getCount() != 0) {
            sb.append("[ OutFlow :  ").append(outStatistics.toString()).append(" ]");
        }
        return sb.toString();
    }
}
