/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl.soaptorest.template;

import org.apache.velocity.VelocityContext;
import org.json.simple.JSONArray;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.soaptorest.util.SOAPToRESTConstants;
import org.wso2.carbon.apimgt.impl.template.APITemplateException;
import org.wso2.carbon.apimgt.impl.template.ConfigContext;

import java.util.Map;

/**
 * Velocity config context for the soap to rest mapping.
 */
class SOAPToRESTConfigContext extends ConfigContext {

    private String soapNamespace;
    private String method;
    private String soapAction;
    private String namespace;
    private String resourcePath;
    private Map<String, String> mappingObj;
    private JSONArray arrayElements;

    /**
     * Velocity config context used in generating api templates for in sequences
     *
     * @param mapping api resource method to sequence string mapping
     * @param method api resource method
     * @param soapAction soap action for the soap binding operation
     * @param namespace namespace of the soap operation
     * @param arrayElements array type parameters mapping, if exists
     */
    SOAPToRESTConfigContext(Map<String, String> mapping, String method, String soapAction, String namespace,
            String soapNamespace, JSONArray arrayElements) {
        this.mappingObj = mapping;
        this.method = method;
        this.soapAction = soapAction;
        this.namespace = namespace;
        this.soapNamespace = soapNamespace;
        this.arrayElements = arrayElements;
        init();
    }

    /**
     * Velocity config context for output sequences
     */
    SOAPToRESTConfigContext() {

    }

    // not implemented since parameters are different for in/out sequences
    @Override
    public void validate() throws APITemplateException, APIManagementException {

    }

    @Override
    public VelocityContext getContext() {
        VelocityContext context = new VelocityContext();
        context.put(SOAPToRESTConstants.Template.HTTP_METHOD, method);
        context.put(SOAPToRESTConstants.Template.SOAP_ACTION, soapAction);
        context.put(SOAPToRESTConstants.Template.NAMESPACE, namespace);
        context.put(SOAPToRESTConstants.Template.SOAP_NAMESPACE, soapNamespace);
        context.put(SOAPToRESTConstants.Template.RESOURCE_PATH, resourcePath);
        context.put(SOAPToRESTConstants.Template.MAPPING, mappingObj);
        context.put(SOAPToRESTConstants.Template.ARRAY_ELEMENTS, arrayElements);
        return context;
    }

    private void init() {
        resourcePath = soapAction.replaceAll(namespace, SOAPToRESTConstants.EMPTY_STRING);
        resourcePath = resourcePath.replaceAll(SOAPToRESTConstants.SequenceGen.PATH_SEPARATOR,
                SOAPToRESTConstants.EMPTY_STRING);
    }
}
