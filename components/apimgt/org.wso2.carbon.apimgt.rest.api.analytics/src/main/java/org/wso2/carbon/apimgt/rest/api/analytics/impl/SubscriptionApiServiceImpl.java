package org.wso2.carbon.apimgt.rest.api.analytics.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.Analyzer;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.analytics.SubscriptionCount;
import org.wso2.carbon.apimgt.core.models.analytics.SubscriptionInfo;
import org.wso2.carbon.apimgt.rest.api.analytics.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.analytics.SubscriptionApiService;
import org.wso2.carbon.apimgt.rest.api.analytics.dto.SubscriptionCountListDTO;
import org.wso2.carbon.apimgt.rest.api.analytics.dto.SubscriptionInfoListDTO;
import org.wso2.carbon.apimgt.rest.api.analytics.mappings.AnalyticsMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.util.List;


/**
 * Subscription API implementation.
 */
public class SubscriptionApiServiceImpl extends SubscriptionApiService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionApiServiceImpl.class);

    @Override
    public Response subscriptionSubscriptionInfoGet(String from, String to, String apiFilter, Request request)
            throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Retrieving subscriptions info. [created by: " + apiFilter +
                        " From: " + from + " To: " + to + "]");
            }
            Analyzer analyzer = RestApiUtil.getAnalyzer(username);
            List<SubscriptionInfo> subscriptionInfoList = analyzer.getSubscriptionInfo(apiFilter, from, to);
            SubscriptionInfoListDTO subscriptionInfoListDTO = AnalyticsMappingUtil
                    .fromSubscriptionInfoListToDTO(subscriptionInfoList);
            return Response.ok().entity(subscriptionInfoListDTO).build();

        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving subscription information";
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    @Override
    public Response subscriptionSubscriptionsCreatedOverTimeGet(String from, String to, String apiFilter,
                                                                Request request) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Retrieving subscriptions created over time. [created by: " + apiFilter +
                        " From: " + from + " To: " + to + "]");
            }
            Analyzer analyzer = RestApiUtil.getAnalyzer(username);
            List<SubscriptionCount> subscriptionCount = analyzer.getSubscriptionCount(apiFilter, from, to);
            SubscriptionCountListDTO subscriptionListDTO = AnalyticsMappingUtil
                    .fromSubscriptionCountListToDTO(subscriptionCount);
            return Response.ok().entity(subscriptionListDTO).build();

        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving Subscription Count";
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }
}
