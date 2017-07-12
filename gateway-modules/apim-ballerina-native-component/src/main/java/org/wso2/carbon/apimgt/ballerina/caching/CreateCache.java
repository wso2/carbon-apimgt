/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.ballerina.caching;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.concurrent.TimeUnit;
import javax.cache.CacheManager;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;



/**
 * Native function org.wso2.carbon.apimgt.ballerina.caching.{@link CreateCache}
 * This function will create cache as per user requirement.
 * Users can create cache with provided configurations.
 *
 * @since 0.10-SNAPSHOT
 */

@BallerinaFunction(
        packageName = "org.wso2.carbon.apimgt.ballerina.caching",
        functionName = "createCache",
        args = {@Argument(name = "cacheName", type = TypeEnum.STRING),
                @Argument(name = "cacheTimeout", type = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = " Create cache as per user requirement")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "cacheName",
        value = "Cache Manager name")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "cacheTimeout",
        value = "Cache Timeout value in minutes")})
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "string",
        value = "Cache Manager reference")})

public class CreateCache extends AbstractNativeFunction {
    @Override
    public BValue[] execute(Context context) {
        String cacheName = getStringArgument(context, 0);
        String cacheTimeoutString = getStringArgument(context, 1);
        //Default cache timeout is 15 minutes
        int cacheTimeout = 15;
        if (cacheTimeoutString != null && cacheTimeoutString.length() > 0) {
            cacheTimeout = ((cacheTimeout = Integer.parseInt(cacheTimeoutString)) > 0 ?
                    cacheTimeout : 15);
        }
        CacheManager cacheManager = CacheManagerHolder.getInstance().getCacheManager();
        if ((cacheManager).getCache(cacheName) == null) {
            Duration cacheExpiry = new Duration(TimeUnit.MINUTES, cacheTimeout);
            MutableConfiguration<String, String> config = new MutableConfiguration<>();
            config.setStoreByValue(true)
                    .setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(cacheExpiry))
                    .setStatisticsEnabled(false)
                    .setStoreByValue(false);
            cacheManager.createCache(cacheName, config);
        }
        return getBValues(new BString(cacheName));
    }
}
