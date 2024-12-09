package org.wso2.carbon.apimgt.gateway.inbound.websocket;

import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.common.gateway.constants.GraphQLConstants;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketApiConstants;
import org.wso2.carbon.apimgt.gateway.dto.InboundProcessorResponseError;

/**
 * Extended DTO class to hold response information during execution of GraphQL subscription Inbound processors.
 */
public class GraphQLProcessorResponseDTO extends InboundProcessorResponseDTO {

    String id; // operation ID
    InboundProcessorResponseError inboundProcessorResponseError;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getErrorResponseString() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_ID, id);
        jsonObject.put(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_TYPE,
                GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_TYPE_ERROR);
        JSONObject payload = new JSONObject();
        payload.put(WebSocketApiConstants.FrameErrorConstants.ERROR_MESSAGE, errorMessage);
        payload.put(WebSocketApiConstants.FrameErrorConstants.ERROR_CODE, errorCode);
        // return error payload as an array
        JSONArray errorPayloads = new JSONArray();
        errorPayloads.put(payload);
        jsonObject.put(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_PAYLOAD, errorPayloads);
        return jsonObject.toString();
    }

    public InboundProcessorResponseError getInboundProcessorResponseError() {
        return inboundProcessorResponseError;
    }

    public void setInboundProcessorResponseError(InboundProcessorResponseError inboundProcessorResponseError) {
        this.inboundProcessorResponseError = inboundProcessorResponseError;
    }
}
