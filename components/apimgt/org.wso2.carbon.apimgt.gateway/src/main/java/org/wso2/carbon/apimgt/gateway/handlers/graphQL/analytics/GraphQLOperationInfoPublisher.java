package org.wso2.carbon.apimgt.gateway.handlers.graphQL.analytics;

import org.wso2.am.analytics.publisher.exception.MetricCreationException;
import org.wso2.am.analytics.publisher.reporter.CounterMetric;
import org.wso2.carbon.apimgt.common.analytics.publishers.impl.AbstractRequestDataPublisher;
import org.wso2.carbon.apimgt.common.analytics.publishers.impl.AnalyticsDataPublisher;

import java.util.List;

public class GraphQLOperationInfoPublisher extends AbstractRequestDataPublisher{

    public CounterMetric getCounterMetric() {
        return null;
    }

    public List<CounterMetric> getMultipleCounterMetrics() {
        try {
            return AnalyticsDataPublisher.getInstance().getOperationInfoMetricReporters();
        } catch (MetricCreationException e) {
            return null;
        }
    }
}
