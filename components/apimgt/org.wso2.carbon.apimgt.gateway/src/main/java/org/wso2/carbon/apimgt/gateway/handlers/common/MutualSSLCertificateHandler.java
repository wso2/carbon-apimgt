/*
 *   Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.gateway.handlers.common;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.Map;

import javax.security.cert.CertificateEncodingException;
import javax.security.cert.X509Certificate;

public class MutualSSLCertificateHandler extends AbstractHandler {

    private static final Log log = LogFactory.getLog(MutualSSLCertificateHandler.class);

    @Override
    public boolean handleRequest(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MsgContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        Map headers =
                (Map) axis2MsgContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        try {
            X509Certificate clientCertificate = Utils.getClientCertificate(axis2MsgContext);
            headers.remove(Utils.getClientCertificateHeader());
            if (clientCertificate != null) {
                headers.put(Utils.getClientCertificateHeader(), Utils.getEncodedClientCertificate(clientCertificate));
            }
        } catch (APIManagementException | CertificateEncodingException e) {
            log.error("Error while converting client certificate", e);
        }
        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) {

        return true;
    }
}
