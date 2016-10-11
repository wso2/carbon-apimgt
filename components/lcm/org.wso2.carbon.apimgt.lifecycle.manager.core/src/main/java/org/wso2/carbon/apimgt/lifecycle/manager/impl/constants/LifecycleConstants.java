/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
package org.wso2.carbon.apimgt.lifecycle.manager.impl.constants;

import javax.swing.plaf.PanelUI;
import javax.xml.soap.SAAJResult;

public class LifecycleConstants {

    /**
     * Defines name attribute
     */
    public static final String NAME = "name";

    /**
     * Defines "forEvent" attribute name
     */
    public static final String FOR_EVENT_ATTRIBUTE = "forEvent";

    public static final String ID_ATTRIBUTE = "id";

    public static final String LIFECYCLE_SCXML_ELEMENT_PATH = "aspect/configuration/lifecycle/*[local-name()='scxml']";

    public static final String LIFECYCLE_STATE_ELEMENT_PATH = "aspect/configuration/lifecycle/*[local-name()='scxml']/*[local-name()='state']";

    public static final String LIFECYCLE_STATE_ELEMENT_WITH_NAME_PATH = "aspect/configuration/lifecycle/*[local-name"
            + "()='scxml']/*[local-name()='state' and @id='";

    public static final String LIFECYCLE_DATA_ELEMENT_PATH = "/*[local-name()='datamodel']/*[local-name()='data' and "
            + "@name='";

    public static final String LIFECYCLE_TRANSITION_INPUT_ATTRIBUTE = "transitionInput";

    public static final String LIFECYCLE_TRANSITION_EXECUTION_ATTRIBUTE = "transitionExecution";

    public static final String LIFECYCLE_TRANSITION_ELEMENT = "/*[local-name()='transition']";

    public static final String LIFECYCLE_EVENT_ATTRIBUTE = "event";

    public static final String LIFECYCLE_TARGET_ATTRIBUTE = "target";

    public static final String LIFECYCLE_INITIAL_STATE_ATTRIBUTE = "initialstate";
}
