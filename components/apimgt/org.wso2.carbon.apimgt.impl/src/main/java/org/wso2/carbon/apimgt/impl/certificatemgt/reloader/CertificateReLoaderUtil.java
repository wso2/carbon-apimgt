/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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
package org.wso2.carbon.apimgt.impl.certificatemgt.reloader;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Utility to start CertificateReLoader
 */
public class CertificateReLoaderUtil {

    private static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    private CertificateReLoaderUtil() {

    }

    private static long lastUpdatedTimeStamp;

    public static void startCertificateReLoader() {

        CertificateReLoader certificateReLoader = new CertificateReLoader();
        executor.scheduleAtFixedRate(certificateReLoader, 60, getCertificateReLoaderInterval(), TimeUnit.SECONDS);
    }

    public static void shutDownCertificateReLoader() {

        executor.shutdown();
    }

    public static long getLastUpdatedTimeStamp() {

        return lastUpdatedTimeStamp;
    }

    public static void setLastUpdatedTimeStamp(long lastUpdatedTimeStamp) {

        CertificateReLoaderUtil.lastUpdatedTimeStamp = lastUpdatedTimeStamp;
    }

    public static long getCertificateReLoaderInterval() {

        String certificateReloaderPeriod =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration()
                        .getFirstProperty(APIConstants.CertificateReLoaderConfiguration.PERIOD);
        if (StringUtils.isNotEmpty(certificateReloaderPeriod)) {
            return Long.parseLong(certificateReloaderPeriod);
        }
        return 2L;
    }
}
