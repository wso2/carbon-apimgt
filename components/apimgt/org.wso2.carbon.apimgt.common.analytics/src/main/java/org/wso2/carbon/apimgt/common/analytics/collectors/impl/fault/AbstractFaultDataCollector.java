/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.common.analytics.collectors.impl.fault;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.common.analytics.collectors.AnalyticsDataProvider;
import org.wso2.carbon.apimgt.common.analytics.collectors.FaultDataCollector;
import org.wso2.carbon.apimgt.common.analytics.collectors.impl.CommonRequestDataCollector;
import org.wso2.carbon.apimgt.common.analytics.exceptions.InvalidCategoryException;
import org.wso2.carbon.apimgt.common.analytics.publishers.RequestDataPublisher;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.Error;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.Event;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.enums.FaultCategory;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.enums.FaultSubCategories;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.enums.FaultSubCategory;

/**
 * Abstract faulty request data collector.
 */
public abstract class AbstractFaultDataCollector extends CommonRequestDataCollector implements FaultDataCollector {
    private static final Log log = LogFactory.getLog(AbstractFaultDataCollector.class);

    protected FaultCategory subType;
    private RequestDataPublisher processor;
    private AnalyticsDataProvider provider;

    public AbstractFaultDataCollector(AnalyticsDataProvider provider, FaultCategory subType,
                                      RequestDataPublisher processor) {

        super(provider);
        this.provider = provider;
        this.subType = subType;
        this.processor = processor;
    }

    protected final void processRequest(Event faultyEvent) throws InvalidCategoryException {
        Error error = provider.getError(this.subType);
        if (!isValidSubCategory(error.getErrorMessage())) {
            log.warn("Invalid sub-category for fault type: " + this.subType + ", error: " + 
                     (faultyEvent.getError() != null ? faultyEvent.getError().getErrorMessage() : "null"));
            throw new InvalidCategoryException(this.subType, faultyEvent.getError().getErrorMessage().toString());
        }
        faultyEvent.setErrorType(this.subType.name());
        faultyEvent.setError(error);
        if (log.isDebugEnabled()) {
            log.debug("Processing fault event for category: " + this.subType.name());
        }
        this.processor.publish(faultyEvent);
    }

    private boolean isValidSubCategory(FaultSubCategory subCategory) {

        if (FaultCategory.AUTH == this.subType) {
            return subCategory instanceof FaultSubCategories.Authentication;
        } else if (FaultCategory.THROTTLED == this.subType) {
            return subCategory instanceof FaultSubCategories.Throttling;
        } else if (FaultCategory.TARGET_CONNECTIVITY == this.subType) {
            return subCategory instanceof FaultSubCategories.TargetConnectivity;
        } else if (FaultCategory.OTHER == this.subType) {
            return subCategory instanceof FaultSubCategories.Other;
        }

        return false;
    }
}
