/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.apimgt.impl.utils;

import java.io.Serializable;



public class ClaimCacheKey implements Serializable {

    private static final long serialVersionUID = 1L;

    //TODO refactor caching implementation

    private String cacheKeyString;

    public ClaimCacheKey(String cacheKeyString) {
        this.cacheKeyString = cacheKeyString;
    }

    public String getCacheKeyString() {
        return cacheKeyString;
    }


    public boolean equals(Object o) {
        if (!(o instanceof ClaimCacheKey)) {
            return false;
        }
        return this.cacheKeyString.equals(((ClaimCacheKey) o).getCacheKeyString());
    }

    public int hashCode() {
        return cacheKeyString.hashCode();
    }
}
