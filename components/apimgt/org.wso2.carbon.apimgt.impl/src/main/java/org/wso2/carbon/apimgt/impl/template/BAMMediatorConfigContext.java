package org.wso2.carbon.apimgt.impl.template;

import org.apache.velocity.VelocityContext;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerAnalyticsConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

public class BAMMediatorConfigContext extends ConfigContextDecorator {

    private API api;

    public BAMMediatorConfigContext(ConfigContext context, API api) {
        super(context);
        this.api = api;
    }

    public VelocityContext getContext() {
        VelocityContext context = super.getContext();
        boolean enabledStats = APIUtil.isAnalyticsEnabled();
        if (enabledStats) {
            context.put("statsEnabled", Boolean.TRUE);
        } else {
            context.put("statsEnabled", Boolean.FALSE);
        }
        if (APIConstants.ENABLED.equalsIgnoreCase(api.getDestinationStatsEnabled())) {
            context.put("destinationBasedStatsEnabled", Boolean.TRUE);
            context.put("profileName", APIConstants.API_MANAGER_DESTINATION_STATS_BAM_PROFILE_NAME);
            context.put("streamName", APIConstants.API_MANAGER_DESTINATION_REQUESTS_STREAM_NAME);
            context.put("streamVersion", APIConstants.API_MANAGER_DESTINATION_REQUESTS_STREAM_VERSION);
        } else {
            context.put("destinationBasedStatsEnabled", Boolean.FALSE);
        }

        return context;
    }

}
