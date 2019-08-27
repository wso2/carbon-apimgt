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
package org.wso2.carbon.apimgt.impl.wsdl.template;

import org.apache.velocity.VelocityContext;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.impl.wsdl.util.SOAPToRESTConstants;
import org.wso2.carbon.apimgt.impl.template.ConfigContext;
import org.wso2.carbon.apimgt.impl.template.ConfigContextDecorator;

/**
 * Velocity config context to generate api sequence.
 */
public class SOAPToRESTAPIConfigContext extends ConfigContextDecorator {

    private JSONObject inSequences;
    private JSONObject outSequences;
    private String seqType;

    public SOAPToRESTAPIConfigContext(ConfigContext context, JSONObject sequences, String seqType) {
        super(context);
        if (SOAPToRESTConstants.Template.IN_SEQUENCES.equals(seqType)) {
            this.inSequences = sequences;
        } else if (SOAPToRESTConstants.Template.OUT_SEQUENCES.equals(seqType)) {
            this.outSequences = sequences;
        }
        this.seqType = seqType;
    }

    public VelocityContext getContext() {
        VelocityContext context = super.getContext();

        context.put(SOAPToRESTConstants.Template.IS_SOAP_TO_REST_MODE, true);
        if (SOAPToRESTConstants.Template.IN_SEQUENCES.equals(seqType)) {
            context.put(SOAPToRESTConstants.Template.IN_SEQUENCES, inSequences);
        } else if (SOAPToRESTConstants.Template.OUT_SEQUENCES.equals(seqType)) {
            context.put(SOAPToRESTConstants.Template.OUT_SEQUENCES, outSequences);
        }
        return context;
    }
}
