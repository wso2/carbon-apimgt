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
package org.wso2.carbon.apimgt.lifecycle.manager.core.constants;

/**
 * This class contains constants related to element specified in lifecycle configuration.
 */
public class LifecycleConstants {

    /**
     * Defines name attribute
     */
    public static final String NAME = "name";

    /**
     * Defines "forEvent" attribute name
     */
    public static final String FOR_TARGET_ATTRIBUTE = "forTarget";

    public static final String LIFECYCLE_SCXML_ELEMENT_PATH = "//scxml";

    public static final String LIFECYCLE_STATE_ELEMENT_WITH_NAME_PATH = "//state[@id='";

    public static final String LIFECYCLE_DATA_ELEMENT_PATH = "//datamodel//data[@name='";

    public static final String LIFECYCLE_TRANSITION_INPUT_ATTRIBUTE = "transitionInput";

    public static final String LIFECYCLE_TRANSITION_EXECUTION_ATTRIBUTE = "transitionExecution";

    public static final String LIFECYCLE_TRANSITION_PERMISSION_ATTRIBUTE = "transitionPermission";

    public static final String LIFECYCLE_CHECKLIST_ITEM_ATTRIBUTE = "checkItems";

    public static final String LIFECYCLE_TRANSITION_ELEMENT = "//transition";

    public static final String LIFECYCLE_EVENT_ATTRIBUTE = "event";

    public static final String LIFECYCLE_TARGET_ATTRIBUTE = "target";

    public static final String LIFECYCLE_INITIAL_STATE_ATTRIBUTE = "initialstate";

    public static final String LIFECYCLE_ROLES_ATTRIBUTE = "roles";

    public static final String ASPECT = "aspect";

    public static final String STATE_TAG = "state";

    public static final String TRANSITION_ATTRIBUTE = "transition";

    public static final String TARGET_ATTRIBUTE = "target";

    public static final String VALUE_ATTRIBUTE = "value";

    public static final String ID_ATTRIBUTE = "id";

    public static final String CLASS_ATTRIBUTE = "class";

    public static final String REQUIRED = "required";

    public static final String LABEL = "label";

    public static final String PLACE_HOLDER = "placeHolder";

    public static final String REGEX = "regex";

    public static final String TOOLTIP = "tooltip";

    public static final String VALUES = "values";
}
