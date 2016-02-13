package org.wso2.carbon.hostobjects.sso;

import java.util.UUID;


import org.apache.axis2.clustering.ClusteringCommand;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.hostobjects.web.SessionHostObject;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;


public class SessionClusterMessage extends ClusteringMessage {
    private static final Log log = LogFactory.getLog(SessionClusterMessage.class);
    private static final long serialVersionUID = -711222207322463831L;

    private String sessionIndex;
    private UUID messageId;

    @Override
    public void execute(ConfigurationContext configurationContext) throws ClusteringFault {
        if (log.isDebugEnabled()) {
            log.debug("Executing the received cluster message with session index:"+sessionIndex);
        }
        SAMLSSORelyingPartyObject.ssho.handleClusterLogout(sessionIndex);

    }

    @Override
    public ClusteringCommand getResponse() {
        return null;
    }

    public void setSessionIndex(String sessionIndex) {
        this.sessionIndex = sessionIndex;
    }

    public void setMessageId(UUID messageId) {
        this.messageId = messageId;
    }

    @Override
    public String toString() {
        return "SSOSessionInvalidationClusterMessage{sessionIndex=" + sessionIndex + "}";
    }
}
