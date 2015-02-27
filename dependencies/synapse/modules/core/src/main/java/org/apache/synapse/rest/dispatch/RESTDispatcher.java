/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.apache.synapse.rest.dispatch;

import org.apache.synapse.MessageContext;
import org.apache.synapse.rest.Resource;

import java.util.Collection;

/**
 * Interface for finding a Resource through which a given request can be mediated.
 * Implementations of this interface should attempt to find a Resource out of the
 * provided collection using which the given message can br further processed.
 */
public interface RESTDispatcher {

    /**
     * Find a Resource instance suitable for processing the given message
     *
     * @param synCtx MessageContext to be processed through a Resource
     * @param resources Collection of available Resource instances
     * @return A matching Resource instance or null
     */
    public Resource findResource(MessageContext synCtx, Collection<Resource> resources);

}
