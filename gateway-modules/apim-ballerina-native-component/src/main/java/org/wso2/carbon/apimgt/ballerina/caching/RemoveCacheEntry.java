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

/**
 *This class will handle cache remove operation.
 */

@BallerinaFunction(
        packageName = "org.wso2.carbon.apimgt.ballerina.caching",
        functionName = "removeCacheEntry",
        args = {@Argument(name = "cacheName", type = TypeEnum.STRING),
                @Argument(name = "cacheKey", type = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Remove cache entry by providing cacheName and cacheKey") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "cacheName",
        value = "Cache Manager name") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "cacheKey",
        value = "String cache key") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "string",
        value = "Cache Manager reference") })

public class RemoveCacheEntry extends AbstractNativeFunction {
    @Override
    public BValue[] execute(Context context) {
        String cacheName = getStringArgument(context, 0);
        String cacheKey = getStringArgument(context, 1);
        CacheManagerHolder.getInstance().getCacheManager().getCache(cacheName).remove(cacheKey);
        return getBValues(new BString(cacheName));
    }
}

