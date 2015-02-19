package org.wso2.carbon.apimgt.impl.template;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

public class BAMMediatorConfigContext extends ConfigContextDecorator {

	private API api;
	private static Log log = LogFactory.getLog(BAMMediatorConfigContext.class);

	public BAMMediatorConfigContext(ConfigContext context, API api) {
		super(context);
		this.api = api;
	}
	
	public VelocityContext getContext() {
        VelocityContext context = super.getContext();
		APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
				getAPIManagerConfigurationService().getAPIManagerConfiguration();

		String destinationRequestsStreamName =
				config.getFirstProperty(APIConstants.API_DESTINATION_STREAM_NAME);
		String destinationRequestsStreamVersion =
				config.getFirstProperty(APIConstants.API_DESTINATION_STREAM_VERSION);
		String destinationStatsBAMProfileName =
				config.getFirstProperty(APIConstants.API_DESTINATION_BAM_PROFILE_NAME);

		if (destinationRequestsStreamName == null || destinationRequestsStreamVersion == null ||
		    destinationStatsBAMProfileName == null) {
			log.error(
					"Destination stream name, version or BAM profile name is null. Check api-manager.xml");
		}

        if (APIConstants.ENABLED.equalsIgnoreCase(api.getDestinationStatsEnabled())) {
            context.put("destinationBasedStatsEnabled", true);
	        context.put("profileName", destinationStatsBAMProfileName);
	        context.put("streamName", destinationRequestsStreamName);
	        context.put("streamVersion", destinationRequestsStreamVersion);
        } else {
            context.put("destinationBasedStatsEnabled", false);
        }

        return context;
    }

}
