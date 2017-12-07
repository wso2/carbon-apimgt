/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.handlers;

import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.indexing.IndexingHandler;

/**
 * This is special indexing handler, used to support
 * {@link org.wso2.carbon.apimgt.impl.indexing.indexer.CustomAPIIndexer}. This IndexingHandler, will check whether
 * the the request is coming from CustomAPIIndexer to make sure, it will not go on a loop.
 */
@SuppressWarnings("unused")
public class CustomAPIIndexHandler extends IndexingHandler {
    public void put(RequestContext requestContext) throws RegistryException {
        if (requestContext != null && requestContext.getResource() != null && requestContext.getResource().getProperty
                (APIConstants.CUSTOM_API_INDEXER_PROPERTY) != null) {
            return;
        }
        super.put(requestContext);
    }
}
