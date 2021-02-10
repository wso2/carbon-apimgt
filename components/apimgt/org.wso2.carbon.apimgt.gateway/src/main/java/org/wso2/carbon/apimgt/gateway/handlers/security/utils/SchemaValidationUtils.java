package org.wso2.carbon.apimgt.gateway.handlers.security.utils;

import com.google.common.net.HttpHeaders;
import org.apache.axiom.om.OMElement;
import org.apache.commons.io.IOUtils;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;

public class SchemaValidationUtils {

    public static final String XML_CONTENT_TYPE = "application/xml";
    public static final String XML_DECLARATION =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";

    public static Optional<String> buildMessagePayload(org.apache.axis2.context.MessageContext axis2MC, Map headers)
            throws APIManagementException {

        String requestPayload = null;
        boolean isMessageContextBuilt = isMessageContextBuilt(axis2MC);
        if (!isMessageContextBuilt) {
            // Build Axis2 Message.
            try {
                RelayUtils.buildMessage(axis2MC);
            } catch (IOException | XMLStreamException e) {
                throw new APIManagementException("Unable to build axis2 message", e);
            }
        }

        if (headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
            if (headers.get(HttpHeaders.CONTENT_TYPE).toString().contains(XML_CONTENT_TYPE)) {

                OMElement xmlPayload = axis2MC.getEnvelope().getBody().getFirstElement();
                if (xmlPayload != null) {
                    requestPayload = XML_DECLARATION + xmlPayload.toString();
                }
            } else {
                // Get JSON Stream and cast to string
                try {
                    InputStream jsonPayload = JsonUtil.getJsonPayload(axis2MC);
                    if (jsonPayload != null) {
                        requestPayload = IOUtils.toString(JsonUtil.getJsonPayload(axis2MC),
                                StandardCharsets.UTF_8.name());
                    }

                } catch (IOException e) {
                    throw new APIManagementException("Unable to read payload stream", e);
                }
            }
        }
        return Optional.ofNullable(requestPayload);
    }

    public static boolean isMessageContextBuilt(org.apache.axis2.context.MessageContext axis2MC) {

        boolean isMessageContextBuilt = false;
        Object messageContextBuilt = axis2MC.getProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED);
        if (messageContextBuilt != null) {
            isMessageContextBuilt = (Boolean) messageContextBuilt;
        }

        return isMessageContextBuilt;
    }

    public static Collection<String> getFromMapOrEmptyList(Map<String, Collection<String>> map, String name) {

        if (name != null && map.containsKey(name)) {

            return map.get(name).stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
        } else {
            return Collections.emptyList();
        }

    }
    public static String getRestSubRequestPath(String subResourcePath) {
        if (subResourcePath.contains("?")) {
            return subResourcePath.split("\\?")[0];
        } else if (subResourcePath.contains("#")) {
            return subResourcePath.split("#")[0];
        } else  {
            return subResourcePath;
        }
    }

}
