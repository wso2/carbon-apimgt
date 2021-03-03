package org.wso2.carbon.apimgt.gateway.handlers;

import org.apache.commons.lang3.StringUtils;
import org.apache.synapse.AbstractSynapseHandler;
import org.apache.synapse.MessageContext;
import org.apache.synapse.api.ApiUtils;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.keymgt.SubscriptionDataHolder;
import org.wso2.carbon.apimgt.keymgt.model.SubscriptionDataStore;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DefaultAPIHandler extends AbstractSynapseHandler {
    @Override
    public boolean handleRequestInFlow(MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        SubscriptionDataStore tenantSubscriptionStore =
                SubscriptionDataHolder.getInstance().getTenantSubscriptionStore(GatewayUtils.getTenantDomain());
        if (tenantSubscriptionStore != null) {
            Map<String, API> contextAPIMap = tenantSubscriptionStore.getAllAPIsByContextList();
            String path = ApiUtils.getFullRequestPath(messageContext);
            List<API> selectedAPIS = new ArrayList<>();
            if (contextAPIMap != null) {
                contextAPIMap.forEach((context, api) -> {
                    if (ApiUtils.matchApiPath(path, context)) {
                        selectedAPIS.add(api);
                    }
                });
                if (selectedAPIS.size() > 0) {
                    Object transportInUrl = axis2MessageContext.getProperty(APIConstants.TRANSPORT_URL_IN);
                    if (selectedAPIS.size() == 1) {
                        API selectedAPI = selectedAPIS.get(0);
                        if (selectedAPI.isDefaultVersion()) {
                            String defaultContext =
                                    selectedAPI.getContext().replace("/" + selectedAPI.getApiVersion(), "");
                            if (transportInUrl instanceof String && StringUtils.isNotEmpty((String) transportInUrl)) {
                                String updatedTransportInUrl = ((String) transportInUrl).replaceFirst(defaultContext,
                                        selectedAPI.getContext());
                                axis2MessageContext.setProperty(APIConstants.TRANSPORT_URL_IN, updatedTransportInUrl);
                            }
                            messageContext.getPropertyKeySet().remove(RESTConstants.REST_FULL_REQUEST_PATH);
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean handleRequestOutFlow(MessageContext messageContext) {
        return true;
    }

    @Override
    public boolean handleResponseInFlow(MessageContext messageContext) {
        return true;
    }

    @Override
    public boolean handleResponseOutFlow(MessageContext messageContext) {
        return true;
    }
}
