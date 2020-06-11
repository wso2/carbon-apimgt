/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.apimgt.impl.workflow;

public class WorkflowConstants {

    public static final String WF_TYPE_AM_USER_SIGNUP = "AM_USER_SIGNUP";

    public static final String WF_TYPE_AM_SUBSCRIPTION_CREATION = "AM_SUBSCRIPTION_CREATION";

    public static final String WF_TYPE_AM_SUBSCRIPTION_UPDATE = "AM_SUBSCRIPTION_UPDATE";

    public static final String WF_TYPE_AM_SUBSCRIPTION_DELETION = "AM_SUBSCRIPTION_DELETION";

    public static final String WF_TYPE_AM_APPLICATION_CREATION = "AM_APPLICATION_CREATION";

    public static final String WF_TYPE_AM_APPLICATION_DELETION = "AM_APPLICATION_DELETION";
    
    public static final String WF_TYPE_AM_API_STATE = "AM_API_STATE";

    public static final String WF_TYPE_AM_APPLICATION_REGISTRATION_PRODUCTION =
            "AM_APPLICATION_REGISTRATION_PRODUCTION";

    public static final String WF_TYPE_AM_APPLICATION_REGISTRATION_SANDBOX = "AM_APPLICATION_REGISTRATION_SANDBOX";

    public static final String WF_TYPE_AM_COMMENTS_ADD = "AM_COMMENTS_ADD";

    public static final String API_MANAGER = "APIManager";

    public static final String EXECUTOR = "executor";

    public static final String WORKFLOW_EXTENSIONS = "WorkFlowExtensions";

    public static final String PASSWORD = "Password";

    public static final String PASSWORD_ = "password";

    public static final String APPLICATION_CREATION = "ApplicationCreation";

    public static final String PRODUCTION_APPLICATION_REGISTRATION = "ProductionApplicationRegistration";

    public static final String SANDBOX_APPLICATION_REGISTRATION = "SandboxApplicationRegistration";

    public static final String USER_SIGN_UP = "UserSignUp";

    public static final String SUBSCRIPTION_CREATION = "SubscriptionCreation";

    public static final String SUBSCRIPTION_UPDATE = "SubscriptionUpdate";

    public static final String SUBSCRIPTION_DELETION = "SubscriptionDeletion";

    public static final String APPLICATION_DELETION = "ApplicationDeletion";
    
    public static final String API_STATE_CHANGE = "APIStateChange";
    
    public static final String API_WF_SCOPE = "apim:api_workflow_approve";

    public static final String REGISTER_USER_WS_ACTION = "http://workflow.registeruser.apimgt.carbon.wso2.org/initiate";

    public static final String CREATE_SUBSCRIPTION_WS_ACTION = "http://workflow.subscription.apimgt.carbon.wso2" +
            ".org/initiate";
    public static final String UPDATE_SUBSCRIPTION_WS_ACTION = "http://workflow.subscription.apimgt.carbon.wso2" +
            ".org/initiate";

    public static final String CREATE_APPLICATION_WS_ACTION = "http://workflow.application.apimgt.carbon.wso2" +
            ".org/initiate";

    public static final String CREATE_REGISTRATION_WS_ACTION = "http://workflow.application.apimgt.carbon.wso2" +
            ".org/initiate";

    public static final String DELETE_SUBSCRIPTION_WS_ACTION = "http://workflow.subscription.apimgt.carbon.wso2" +
            ".org/cancel";

    public static final String DELETE_APPLICATION_WS_ACTION = "http://workflow.application.apimgt.carbon.wso2" +
            ".org/cancel";

    public static final String DELETE_REGISTRATION_WS_ACTION = "http://workflow.application.apimgt.carbon.wso2" +
            ".org/cancel";

    public static final String DELETE_USER_WS_ACTION = "http://workflow.registeruser.apimgt.carbon.wso2.org/cancel";

    public static final String REGISTER_USER_PAYLOAD =
            "	  <wor:UserSignupProcessRequest xmlns:wor=\"http://workflow.registeruser.apimgt.carbon.wso2.org\">\n" +
                    "         <wor:userName>$1</wor:userName>\n" +
                    "         <wor:tenantDomain>$2</wor:tenantDomain>\n" +
                    "         <wor:workflowExternalRef>$3</wor:workflowExternalRef>\n" +
                    "         <wor:callBackURL>$4</wor:callBackURL>\n" +
                    "      </wor:UserSignupProcessRequest>";
    
    public static final String DEFAULT_EXECUTOR_API_STATE_CHANGE = 
            "org.wso2.carbon.apimgt.impl.workflow.APIStateChangeSimpleWorkflowExecutor";
    
    public static final String WORKFLOW_OAUTH_APP_NAME = "workflow_app";
    public static final String WORKFLOW_OAUTH_APP_GRANT_TYPES = "client_credentials password refresh_token";
    
    public static class PayloadConstants {
        public static final String PROCESS_DEF_KEY = "processDefinitionKey";
        public static final String TENANT_ID = "tenantId";
        public static final String BUSINESS_KEY = "businessKey";
        public static final String VARIABLES = "variables";
        public static final String VARIABLE_NAME = "name";
        public static final String VARIABLE_VALUE = "value";
        public static final String VARIABLE_CLIENTID = "clientId";
        public static final String VARIABLE_CLIENTSECRET = "clientSecret";
        public static final String VARIABLE_SCOPE = "scope";
        public static final String VARIABLE_TOKENAPI = "tokenAPI";
        public static final String VARIABLE_APISTATE = "apiCurrentState";
        public static final String VARIABLE_API_LC_ACTION = "apiLCAction";
        public static final String VARIABLE_APINAME = "apiName";
        public static final String VARIABLE_APIVERSION = "apiVersion";
        public static final String VARIABLE_APIPROVIDER = "apiProvider";
        public static final String VARIABLE_CALLBACKURL = "callbackUrl";
        public static final String VARIABLE_WFREF = "wfReference";
        public static final String VARIABLE_INVOKER = "invoker";
        
        public static final String DATA = "data";
        public static final String ID = "id";
        
        public static final String KEY_OAUTH_APPNAME = "clientName";
        public static final String KEY_OAUTH_OWNER = "owner";
        public static final String KEY_OAUTH_SAASAPP = "saasApp";
        public static final String KEY_OAUTH_GRANT_TYPES = "grantType";
        
        private PayloadConstants(){
            
        }
        
    }

}
