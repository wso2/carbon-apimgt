package org.wso2.carbon.apimgt.ballerina.publisher;
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

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.connectors.AbstractNativeAction;
import org.json.JSONArray;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;

/**
 * Native function org.wso2.carbon.apimgt.ballerina.publisher.Publish.{@link
 * Publish}
 * Ballerina action publish a event to receivers.
 *
 * @since 7.0.34
 */
@BallerinaAction(packageName = "org.wso2.carbon.apimgt.ballerina.publisher", actionName = "publish",
        connectorName = Constants.CONNECTOR_NAME, args = {
        @Argument(name = "c", type = TypeEnum.CONNECTOR),
        @Argument(name = "event", type = TypeEnum.JSON) }, connectorArgs = {
        @Argument(name = "options", type = TypeEnum.MAP) })
@BallerinaAnnotation(annotationName = "Description", attributes = {
        @Attribute(name = "value", value = " deployment service") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "c", value = "Connector"),
        @Attribute(name = "event", value = "aEvent") })
@Component(name = "action.org.wso2.carbon.apimgt.ballerina.publisher.publish", immediate = true,
        service = AbstractNativeAction.class)
public class Publish extends AbstractEventPublisherAction {
    private static final Logger log = LoggerFactory.getLogger(Publish.class);

    @Override
    public BValue execute(Context context) {
        log.info("publishing event to DAS");
        BConnector bConnector = (BConnector) getRefArgument(context, 0);
        BJSON json = (BJSON) getRefArgument(context, 1);
        String streamName = json.value().get(Constants.STREAM_NAME).asText();
        String streamVersion = json.value().get(Constants.STREAM_VERSION).asText();
        ArrayNode metaData = (ArrayNode) json.value().get(Constants.META_DATA);
        ArrayNode correlationData = (ArrayNode) json.value().get(Constants.CORRELATION_DATA);
        ArrayNode payloadData = (ArrayNode) json.value().get(Constants.PAYLOAD_DATA);

        Object[] metaDataArr = new Object[metaData.size()];
        JSONArray jsonMetaData = new JSONArray(metaData.toString());
        for (int i = 0; i < jsonMetaData.length(); i++) {
            metaDataArr[i] = jsonMetaData.get(i);
        }

        Object[] correlationDataArr = new Object[correlationData.size()];
        JSONArray jsonCorrelationData = new JSONArray(correlationData.toString());
        for (int i = 0; i < jsonCorrelationData.length(); i++) {
            correlationDataArr[i] = jsonCorrelationData.get(i);
        }

        Object[] payloadDataArr = new Object[payloadData.size()];
        JSONArray jsonPayloadData = new JSONArray(payloadData.toString());
        for (int i = 0; i < jsonPayloadData.length(); i++) {
            payloadDataArr[i] = jsonPayloadData.get(i);
        }

        BMap sharedMap = (BMap) bConnector.getRefField(1);
        EventPublisher pub = (EventPublisher) sharedMap.get(Constants.PUBLISHER_INSTANCE);
        Event event = new Event();
        event.setStreamId(DataBridgeCommonsUtils.generateStreamId(streamName, streamVersion));
        event.setMetaData(metaDataArr);
        event.setCorrelationData(correlationDataArr);
        event.setPayloadData(payloadDataArr);
        pub.publish(event);
        return null;
    }
}
