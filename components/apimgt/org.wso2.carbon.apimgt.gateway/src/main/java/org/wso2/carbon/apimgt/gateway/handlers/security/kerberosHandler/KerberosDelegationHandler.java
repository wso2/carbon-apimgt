/*
 *
 *  * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  * WSO2 Inc. licenses this file to you under the Apache License,
 *  * Version 2.0 (the "License"); you may not use this file except
 *  * in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied. See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 *
 */

package org.wso2.carbon.apimgt.gateway.handlers.security.kerberosHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2Sender;
import org.apache.synapse.rest.AbstractHandler;
import org.wso2.carbon.apimgt.gateway.handlers.security.kerberosHandler.utils.KerberosDelegator;
import org.wso2.carbon.apimgt.gateway.handlers.security.kerberosHandler.utils.User;
import org.wso2.carbon.utils.CarbonUtils;

import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;
import javax.security.auth.login.Configuration;

public class KerberosDelegationHandler extends AbstractHandler {
    private static final Log log = LogFactory.getLog(KerberosDelegationHandler.class);
    private static final boolean IS_DEBUG_ENABLED = Boolean.parseBoolean(System.getProperty("debug", "false"));

    private String targetSPN;

    public String getTargetSPN() {
        return targetSPN;
    }

    public void setTargetSPN(String targetSPN) {
        this.targetSPN = targetSPN;
    }

    public boolean handleRequest(MessageContext messageContext) {

        if (IS_DEBUG_ENABLED) {
            log.info("krb5 debug logs enabled.");
            System.setProperty("sun.security.krb5.debug", "true");
            System.setProperty("sun.security.jgss.debug", "true");
        }

        Map headers = getTransportHeaders(messageContext);
        if (getKerberosHeader(headers) == null) {
            return unAuthorizedUser(headers, messageContext, null);
        } else {

            String jaasConfigPathold = System.getProperty(KerberosConstants.LOGIN_CONFIG_PROPERTY);
            String krb5ConfigPathold = System.getProperty(KerberosConstants.KERBEROS_CONFIG_PROPERTY);

            String kerberosConfPath = Paths.get(CarbonUtils.getCarbonConfigDirPath(), "security", "kerberos")
                    .toString();

            String jaasConfigPath = Paths.get(kerberosConfPath, KerberosConstants.LOGIN_CONF_FILE_NAME).toString();
            System.setProperty(KerberosConstants.LOGIN_CONFIG_PROPERTY, jaasConfigPath);

            String krb5ConfigPath = Paths.get(kerberosConfPath, KerberosConstants.KERBEROS_CONF_FILE_NAME).toString();
            System.setProperty(KerberosConstants.KERBEROS_CONFIG_PROPERTY, krb5ConfigPath);
            String loginContextName = System.getProperty(KerberosConstants.LOGIN_CONTEXT_NAME, "KrbLogin");

            if (log.isDebugEnabled()) {
                log.debug("Kerberos jaas.conf file path set to : " + jaasConfigPath);
                log.debug("Kerberos krb5.conf file path set to : " + krb5ConfigPath);
            }

            User self = new User();
            try {
                String kerberosTicket;
                self.login(loginContextName);
                kerberosTicket = getDelegatedTicket(self, headers);
                setKerberosHeader(headers, kerberosTicket);
            } catch (Exception e) {
                throw new RuntimeException("Kerberos constrained delegation failed.", e);
            } finally {
                self.logout();
                // Revert back to previous configs
                Configuration.setConfiguration(null);
                if (jaasConfigPathold != null) {
                    System.setProperty(KerberosConstants.LOGIN_CONFIG_PROPERTY, jaasConfigPathold);
                }
                if (krb5ConfigPathold != null) {
                    System.setProperty(KerberosConstants.KERBEROS_CONFIG_PROPERTY, krb5ConfigPathold);
                }
            }
            return true;
        }

    }

    private String getDelegatedTicket(User user, Map headers) throws Exception {
        KerberosDelegator kerberosDelegator = new KerberosDelegator(user.getSubject());
        String clientKerberosTicket = getKerberosHeader(headers);
        if (log.isDebugEnabled()) {
            log.info("Acquired Client's Kerberos ticket: " + clientKerberosTicket);
            log.info("Initiating constrained delegation for SPN: " + targetSPN);
        }
        byte[] delegatedTicket = kerberosDelegator
                .delegate(Base64.getDecoder().decode(clientKerberosTicket.split(" ")[1]), targetSPN);
        String delegatedKerberosTicket = Base64.getEncoder().encodeToString(delegatedTicket);
        if (log.isDebugEnabled()) {
            log.info("Acquired delegated Kerberos ticket: " + delegatedKerberosTicket);
        }
        return delegatedKerberosTicket;

    }

    public boolean handleResponse(MessageContext messageContext) {
        return true;
    }

    private String getKerberosHeader(Map headers) {
        return (String) headers.get(HttpHeaders.AUTHORIZATION);
    }

    private void setKerberosHeader(Map headers, String delegatedKerberosTicket) {
        headers.put(HttpHeaders.AUTHORIZATION, KerberosConstants.NEGOTIATE + " " + delegatedKerberosTicket);
    }

    private Map getTransportHeaders(MessageContext messageContext) {
        return (Map) ((Axis2MessageContext) messageContext).getAxis2MessageContext().
                getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
    }

    private boolean unAuthorizedUser(Map headersMap, MessageContext messageContext, byte[] serverToken) {
        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext();
        String outServerTokenString = null;
        headersMap.clear();
        try {
            if (serverToken != null) {
                outServerTokenString = Base64.getEncoder().encodeToString(serverToken);
            }
            axis2MessageContext.setProperty("HTTP_SC", "401");
            if (outServerTokenString != null) {
                headersMap.put(KerberosConstants.AUTHENTICATE_HEADER,
                        KerberosConstants.NEGOTIATE + " " + outServerTokenString);
            } else {
                headersMap.put(KerberosConstants.AUTHENTICATE_HEADER, KerberosConstants.NEGOTIATE);
            }
            axis2MessageContext.setProperty("NO_ENTITY_BODY", new Boolean("true"));
            messageContext.setProperty("RESPONSE", "true");
            messageContext.setTo(null);
            Axis2Sender.sendBack(messageContext);
            return false;

        } catch (Exception e) {
            return false;
        }
    }

}
