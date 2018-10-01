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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.json.simple.JSONArray;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.soaptorest.util.SOAPToRESTConstants;
import org.wso2.carbon.apimgt.impl.template.ConfigContext;

import java.io.File;
import java.io.StringWriter;
import java.util.Map;

/**
 * Velocity template to generate in sequences and out sequences.
 */
public class RESTToSOAPMsgTemplate {

    private static final Log log = LogFactory.getLog(RESTToSOAPMsgTemplate.class);

    private String velocityLogPath = null;
    private static final String IN_SEQ_TEMPLATE_FILE = "soap_to_rest_in_seq_template";
    private static final String OUT_SEQ_TEMPLATE_FILE = "soap_to_rest_out_seq_template";
    private static final String VELOCITY_RUNTIME_LOG_CLASS = "org.apache.velocity.runtime.log.Log4JLogChute";
    private static final String VELOCITY_RUNTIME_LOG_PROPERTY = "runtime.log.logsystem.log4j.logger";

    /**
     * gets in sequence for the soap operation
     *
     * @param mapping       soap to rest mapping json
     * @param method        http method for the resource
     * @param soapAction    soap action for the operation
     * @param namespace     soap namespace for the operation
     * @param soapNamespace soap namespace for the soap version
     * @return in sequence string
     */
    public String getMappingInSequence(Map<String, String> mapping, String method, String soapAction, String namespace,
            String soapNamespace, JSONArray array) {

        ConfigContext configcontext = new SOAPToRESTConfigContext(mapping, method, soapAction, namespace, soapNamespace,
                array);
        StringWriter writer = new StringWriter();
        try {
            VelocityContext context = configcontext.getContext();
            context.internalGetKeys();

            VelocityEngine velocityengine = new VelocityEngine();
            if (!SOAPToRESTConstants.Template.NOT_DEFINED.equalsIgnoreCase(getVelocityLogger())) {
                velocityengine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, VELOCITY_RUNTIME_LOG_CLASS);
                velocityengine.setProperty(VELOCITY_RUNTIME_LOG_PROPERTY, getVelocityLogger());
            }
            velocityengine.init();
            org.apache.velocity.Template t = velocityengine.getTemplate(this.getInSeqTemplatePath());
            t.merge(context, writer);
        } catch (Exception e) {
            log.error("Velocity Error", e);
        }
        return writer.toString();
    }

    /**
     * gets out sequence for the soap operation
     *
     * @return out sequence for the converted soap operation
     */
    public String getMappingOutSequence() {

        ConfigContext configcontext = new SOAPToRESTConfigContext();
        StringWriter writer = new StringWriter();
        try {
            VelocityContext context = configcontext.getContext();
            context.internalGetKeys();

            VelocityEngine velocityengine = new VelocityEngine();
            if (!SOAPToRESTConstants.Template.NOT_DEFINED.equalsIgnoreCase(getVelocityLogger())) {
                velocityengine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, VELOCITY_RUNTIME_LOG_CLASS);
                velocityengine.setProperty(VELOCITY_RUNTIME_LOG_PROPERTY, getVelocityLogger());
            }

            velocityengine.init();
            org.apache.velocity.Template template = velocityengine.getTemplate(this.getOutSeqTemplatePath());

            template.merge(context, writer);
        } catch (Exception e) {
            log.error("Velocity Error", e);
        }
        return writer.toString();
    }

    private String getInSeqTemplatePath() {
        return "repository" + File.separator + "resources" + File.separator + "api_templates" + File.separator
                + IN_SEQ_TEMPLATE_FILE + ".xml";
    }

    private String getOutSeqTemplatePath() {
        return "repository" + File.separator + "resources" + File.separator + "api_templates" + File.separator
                + OUT_SEQ_TEMPLATE_FILE + ".xml";
    }

    private String getVelocityLogger() {
        if (this.velocityLogPath != null) {
            return this.velocityLogPath;
        } else {
            APIManagerConfigurationService config = ServiceReferenceHolder.getInstance()
                    .getAPIManagerConfigurationService();
            String velocityLogPath = config.getAPIManagerConfiguration().getFirstProperty(APIConstants.VELOCITY_LOGGER);
            if (velocityLogPath != null && velocityLogPath.length() > 1) {
                this.velocityLogPath = velocityLogPath;
            } else {
                this.velocityLogPath = SOAPToRESTConstants.Template.NOT_DEFINED;
            }
            return this.velocityLogPath;
        }
    }
}
