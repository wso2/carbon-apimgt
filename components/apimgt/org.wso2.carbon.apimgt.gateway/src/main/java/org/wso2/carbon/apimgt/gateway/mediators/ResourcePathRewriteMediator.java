package org.wso2.carbon.apimgt.gateway.mediators;

import org.apache.commons.lang3.StringUtils;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResourcePathRewriteMediator extends AbstractMediator {
    private String resourcePath;
    private boolean includeQueryParams = true;

    @Override
    public boolean mediate(MessageContext messageContext) {
        if (log.isDebugEnabled()) {
            log.debug("Rewriting resource path");
        }

        boolean success = populatePathParams(messageContext);
        if (!success) {
            return false;
        }
        populateQueryParams(messageContext);

        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setProperty("REST_URL_POSTFIX", resourcePath);
        return true;
    }

    /**
     * Populates path params
     *
     * If the path parameters need to be extracted from the original REST URL postfix, the path param in the new
     * resourcePath should be defined in following format.
     * eg: {paramName}
     *
     * @param messageContext messageContext
     * @return true if all requested path params could be extracted
     */
    private boolean populatePathParams(MessageContext messageContext) {
        if (log.isDebugEnabled()) {
            log.debug("Before populating path params : " + resourcePath);
        }

        String pathParamRegex = "[^{}]*\\{([^{}]*)}";
        Pattern pattern =  Pattern.compile(pathParamRegex);
        Matcher matcher = pattern.matcher(resourcePath);
        while (matcher.find()) {
            String pathParam = matcher.group(1);
            String paramValue = (String) messageContext.getProperty(APIConstants.PATH_PARAM_PREFIX + pathParam);
            if (!StringUtils.isEmpty(paramValue)) {
                resourcePath = resourcePath.replace("{" + pathParam + "}", paramValue);
            } else {
                log.error("Path parameter '" + pathParam + "' was not found in the provided URL postfix");
                return false;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("After populating path params : " + resourcePath);
        }
        return true;
    }

    private void populateQueryParams(MessageContext messageContext) {
        String currentUrlPostfix = (String) ((Axis2MessageContext) messageContext).getAxis2MessageContext().
                getProperty("REST_URL_POSTFIX");

        String queryString;
        String[] urlSplit = currentUrlPostfix.split("\\?");
        if (includeQueryParams) {
            if (log.isDebugEnabled()) {
                log.debug("Before populating query params : " + resourcePath);
            }

            if (urlSplit.length > 1) {
                queryString = urlSplit[1];
                if (resourcePath.contains("?")) {
                    resourcePath = resourcePath.concat("&" + queryString);
                } else {
                    resourcePath = resourcePath.concat("?" + queryString);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("No query parameters found in resource path");
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("After populating query params : " + resourcePath);
            }
        } else {
            if (urlSplit.length > 1) {
                if (log.isDebugEnabled()) {
                    log.debug("Skipping query parameters");
                }
            }
        }
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public void setIncludeQueryParams(boolean includeQueryParams) {
        this.includeQueryParams = includeQueryParams;
    }
}
