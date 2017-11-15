package org.wso2.carbon.apimgt.impl.handlers;

/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import javax.xml.stream.XMLStreamException;
import java.util.Iterator;

public class APIEndpointPasswordRegistryHandler extends Handler {

    private static Log log = LogFactory.getLog(APIEndpointPasswordRegistryHandler.class);

    /**
     * This method is called for a registry put operation
     *
     * @param requestContext - The request context
     * @throws RegistryException
     */
    public void put(RequestContext requestContext) throws RegistryException {
        Resource resource = requestContext.getResource();
        Object content = resource.getContent();
        String resourceContent;
        if (content instanceof String) {
            resourceContent = (String) resource.getContent();
        } else if (content instanceof byte[]) {
            resourceContent = RegistryUtils.decodeBytes((byte[]) resource.getContent());
        } else {
            log.warn("The resource content is not of expected type");
            return;
        }
        try {
            OMElement omElement = AXIOMUtil.stringToOM(resourceContent);
            Iterator mainChildIt = omElement.getChildren();
            while (mainChildIt.hasNext()) {
                Object childObj = mainChildIt.next();
                if ((childObj instanceof OMElement) && ((APIConstants.OVERVIEW_ELEMENT)
                        .equals(((OMElement) childObj).getLocalName()))) {
                    Iterator childIt = ((OMElement) childObj)
                            .getChildrenWithLocalName(APIConstants.ENDPOINT_PASSWORD_ELEMENT);
                    //There is only one ep-password, hence no iteration
                    if (childIt.hasNext()) {
                        OMElement child = (OMElement) childIt.next();
                        String pswd = child.getText();
                        //Password has been edited on put
                        if (!"".equals(pswd) && !((APIConstants.DEFAULT_MODIFIED_ENDPOINT_PASSWORD).equals(pswd))) {
                            resource.setProperty(APIConstants.REGISTRY_HIDDEN_ENDPOINT_PROPERTY, pswd);
                            child.setText(pswd);
                        } else if ((APIConstants.DEFAULT_MODIFIED_ENDPOINT_PASSWORD)
                                .equals(pswd)) { //Password not being changed
                            String actualPassword = resource
                                    .getProperty(APIConstants.REGISTRY_HIDDEN_ENDPOINT_PROPERTY);
                            child.setText(actualPassword);
                        }
                    }
                }
            }
            resource.setContent(RegistryUtils.encodeString(omElement.toString()));
            requestContext.setResource(resource);
            log.debug("Modified API resource content with actual password before put operation");
        } catch (XMLStreamException e) {
            log.error("There was an error in reading XML Stream during API update.");
            throw new RegistryException("There was an error updating the API resource.", e);
        }
    }

    /**
     * This method is called for a registry get operation
     *
     * @param requestContext - The request context
     * @return - returns the modified resource
     * @throws RegistryException
     */
    public Resource get(RequestContext requestContext) throws RegistryException {
        Resource resource = requestContext.getResource();
        if (resource != null) {
            String resourceContent = RegistryUtils.decodeBytes((byte[]) resource.getContent());
            try {
                OMElement omElement = AXIOMUtil.stringToOM(resourceContent);
                Iterator mainChildIt = omElement.getChildren();
                while (mainChildIt.hasNext()) {
                    Object childObj = mainChildIt.next();
                    if ((childObj instanceof OMElement) && ((APIConstants.OVERVIEW_ELEMENT)
                            .equals(((OMElement) childObj).getLocalName()))) {
                        Iterator childIt = ((OMElement) childObj)
                                .getChildrenWithLocalName(APIConstants.ENDPOINT_PASSWORD_ELEMENT);
                        //There is only one ep-password, hence no iteration
                        if (childIt.hasNext()) {
                            OMElement child = (OMElement) childIt.next();
                            String actualPassword = child.getText();
                            //Store the password in a hidden property to access in the PUT method.
                            if (!actualPassword.isEmpty()) {
                                resource.setProperty(APIConstants.REGISTRY_HIDDEN_ENDPOINT_PROPERTY, actualPassword);
                                child.setText(APIConstants.DEFAULT_MODIFIED_ENDPOINT_PASSWORD);
                            }
                        }
                    }
                }
                resource.setContent(RegistryUtils.encodeString(omElement.toString()));
                log.debug("Modified API resource content with default password before get operation");
            } catch (XMLStreamException e) {
                log.error("There was an error in reading XML Stream during API get.");
                throw new RegistryException("There was an error reading the API resource.", e);
            }
        }
        return resource;
    }
}
