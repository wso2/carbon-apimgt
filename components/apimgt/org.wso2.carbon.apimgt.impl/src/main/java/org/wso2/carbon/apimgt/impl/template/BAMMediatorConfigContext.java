package org.wso2.carbon.apimgt.impl.template;

import org.apache.velocity.VelocityContext;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.impl.APIConstants;

public class BAMMediatorConfigContext extends ConfigContextDecorator {

	private API api;

	public BAMMediatorConfigContext(ConfigContext context, API api) {
		super(context);
		this.api = api;
	}
	
	public VelocityContext getContext() {
        VelocityContext context = super.getContext();

        if (APIConstants.ENABLED.equalsIgnoreCase(api.getDestinationStatsEnabled())) {
            context.put("destinationBasedStatsEnabled", true);
            context.put("profileName", APIConstants.API_MANAGER_DESTINATION_STATS_BAM_PROFILE_NAME);
            context.put("streamName", APIConstants.API_MANAGER_DESTINATION_REQUESTS_STREAM_NAME);
            context.put("streamVersion", APIConstants.API_MANAGER_DESTINATION_REQUESTS_STREAM_VERSION);
        } else {
            context.put("destinationBasedStatsEnabled", false);
        }

        return context;
    }

}
