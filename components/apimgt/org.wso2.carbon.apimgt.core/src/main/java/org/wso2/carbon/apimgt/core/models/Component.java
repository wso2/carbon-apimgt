/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.core.models;

/**
 * Enum for selecting Component options. Each component can have several events associated with it.
 *
 * @see org.wso2.carbon.apimgt.core.models.Event
 */
public enum Component {
    API_PUBLISHER("API_PUBLISHER"), API_STORE("API_STORE");

    private String component;

    /**
     * Constructor.
     *
     * @param component Component name
     */
    Component(String component) {
        this.component = component;
    }

    /**
     * To get String value of Component.
     *
     * @return String value of Component
     */
    public String getComponentAsString() {
        return component;
    }
}
