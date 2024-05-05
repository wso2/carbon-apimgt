package org.wso2.carbon.apimgt.gateway.handlers.graphQL.analytics;

import org.wso2.carbon.apimgt.common.analytics.Constants;
import org.wso2.carbon.apimgt.common.analytics.collectors.AnalyticsDataProvider;
import org.wso2.carbon.apimgt.common.analytics.collectors.RequestDataCollector;
import org.wso2.carbon.apimgt.common.analytics.collectors.impl.CommonRequestDataCollector;
import org.wso2.carbon.apimgt.common.analytics.exceptions.AnalyticsException;
import org.wso2.carbon.apimgt.common.analytics.publishers.RequestDataPublisher;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.*;

public class GraphQLOperationInfoCollector extends CommonRequestDataCollector implements RequestDataCollector {

    private RequestDataPublisher processor;
    private AnalyticsDataProvider provider;

    public GraphQLOperationInfoCollector(AnalyticsDataProvider provider, RequestDataPublisher processor) {
        super(provider);
        this.processor = processor;
        this.provider = provider;
    }

    public GraphQLOperationInfoCollector(AnalyticsDataProvider provider) {
        this(provider, new GraphQLOperationInfoPublisher());
    }

    @Override
    public void collectData() throws AnalyticsException {

        long requestInTime = provider.getRequestTime();
        String offsetDateTime = getTimeInISO(requestInTime);

        Event event = new Event();
        event.setProperties(provider.getOperationProperties());
        API api = provider.getApi();
        Operation operation = provider.getOperation();
        Target target = provider.getTarget();

        Application application;
        if (provider.isAnonymous()) {
            application = getAnonymousApp();
        } else {
            application = provider.getApplication();
        }
        Latencies latencies = provider.getLatencies();
        MetaInfo metaInfo = provider.getMetaInfo();
        String userAgent = provider.getUserAgentHeader();
        String userName = provider.getUserName();
        String userIp = provider.getEndUserIP();
        if (userIp == null) {
            userIp = Constants.UNKNOWN_VALUE;
        }
        if (userAgent == null) {
            userAgent = Constants.UNKNOWN_VALUE;
        }

        event.setApi(api);
        event.setOperation(operation);
        event.setTarget(target);
        event.setApplication(application);
        event.setLatencies(latencies);
        event.setProxyResponseCode(provider.getProxyResponseCode());
        event.setRequestTimestamp(offsetDateTime);
        event.setMetaInfo(metaInfo);
        event.setUserAgentHeader(userAgent);
        event.setUserName(userName);
        event.setUserIp(userIp);

        this.processor.publish(event);

    }
}
