/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.apimgt.impl.utils;


import org.wso2.balana.attr.BooleanAttribute;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.utils.xml.StringUtils;

import java.io.Serializable;
import java.util.Comparator;

public class TierNameComparator implements Comparator<Tier>, Serializable {

    @Override
    public int compare(Tier tier1, Tier tier2) {
        if (tier1.getDisplayName() != null && tier2.getDisplayName() != null) {
            return tier1.getDisplayName().compareToIgnoreCase(tier2.getDisplayName());
        }else if (tier1.getName() != null && tier2.getName() != null){
            return tier1.getName().compareToIgnoreCase(tier2.getName());
        }else{
            return 0;
        }

    }
}
