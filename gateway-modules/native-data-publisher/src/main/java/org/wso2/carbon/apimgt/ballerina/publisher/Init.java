/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.wso2.carbon.apimgt.ballerina.publisher;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.connectors.AbstractNativeAction;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.exception.TransportException;

import java.io.File;

/**
 * Native function org.wso2.carbon.apimgt.ballerina.publisher.Init.{@link
 * Init}
 * Ballerina action will init a data-bridge event publisher and can be used to publish an events.
 *
 * @since 7.0.34
 */
@BallerinaAction(packageName = "org.wso2.carbon.apimgt.ballerina.publisher", actionName = "init",
        connectorName = Constants.CONNECTOR_NAME, args = {
        @Argument(name = "c", type = TypeEnum.CONNECTOR) }, connectorArgs = {
        @Argument(name = "options", type = TypeEnum.MAP) })
@BallerinaAnnotation(annotationName = "Description", attributes = {
        @Attribute(name = "value", value = " deployment service") })
@BallerinaAnnotation(annotationName = "Param", attributes = {
        @Attribute(name = "type", value = "path to the service file") })
@BallerinaAnnotation(annotationName = "Param", attributes = {
        @Attribute(name = "receiverURLSet", value = "serviceName") })
@BallerinaAnnotation(annotationName = "Param", attributes = {
        @Attribute(name = "authURLSet", value = "ballerina source") })
@BallerinaAnnotation(annotationName = "Param", attributes = {
        @Attribute(name = "username", value = "ballerina package") })
@BallerinaAnnotation(annotationName = "Param", attributes = {
        @Attribute(name = "password", value = "ballerina package") })
@Component(name = "action.org.wso2.carbon.apimgt.ballerina.publisher.init", immediate = true,
        service = AbstractNativeAction.class)
public class Init extends AbstractEventPublisherAction implements BValue {

    private static final Logger log = LoggerFactory.getLogger(AbstractEventPublisherAction.class);

    @Override
    public BValue execute(Context context) {
        BConnector bConnector = (BConnector) getRefArgument(context, 0);
        BMap optionMap = (BMap) bConnector.getRefField(0);
        BMap sharedMap = (BMap) bConnector.getRefField(1);

        System.setProperty("javax.net.ssl.trustStore",
                System.getProperty("ballerina.home") + File.separator + "bre" + File.separator + "security"
                        + File.separator + "wso2carbon.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");

        String type = optionMap.get(Constants.TYPE).stringValue();
        String receiverURLSet = optionMap.get(Constants.RECEIVER_URL_SET).stringValue();
        String authURLSet = optionMap.get(Constants.AUTH_URL_SET).stringValue();
        String username = optionMap.get(Constants.USERNAME).stringValue();
        String password = optionMap.get(Constants.PASSWORD).stringValue();
        String configPath = optionMap.get(Constants.CONFIG_PATH).stringValue();
        configPath = System.getProperty("ballerina.home") + File.separator + configPath;
        try {
            initDataPublisher(type, receiverURLSet, authURLSet, username, password, configPath);
            sharedMap.put(Constants.PUBLISHER_INSTANCE, this);
        } catch (DataEndpointAuthenticationException e) {
            log.error("Error occurred while authenticating.", e);
        } catch (DataEndpointAgentConfigurationException e) {
            log.error("Error occurred while configuring the publisher.", e);
        } catch (TransportException e) {
            log.error("Transport level exception occurred.", e);
        } catch (DataEndpointException e) {
            log.error("Data endpoint exception occurred.", e);
        } catch (DataEndpointConfigurationException e) {
            log.error("Data endpoint configuration exception occurred.", e);
        }
        return null;
    }

    @Override
    public String stringValue() {
        return "publisher";
    }

    @Override
    public BType getType() {
        return null;
    }

    @Override
    public BValue copy() {
        return null;
    }
}

