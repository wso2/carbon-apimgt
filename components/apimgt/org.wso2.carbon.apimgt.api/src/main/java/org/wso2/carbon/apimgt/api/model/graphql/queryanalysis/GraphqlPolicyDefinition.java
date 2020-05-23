/*
 *
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.api.model.graphql.queryanalysis;

import java.util.ArrayList;
import java.util.List;

public class GraphqlPolicyDefinition {

    /**
     * Depth related details
     */
    private int maxDepth;

    /**
     * Complexity related details
     */
    private int maxComplexity;
    private GraphqlComplexityInfo graphqlComplexityInfo;


    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public int getMaxComplexity() {
        return maxComplexity;
    }

    public void setMaxComplexity(int maxComplexity) {
        this.maxComplexity = maxComplexity;
    }


    public GraphqlComplexityInfo getGraphqlComplexityInfo() {
        return graphqlComplexityInfo;
    }

    public void setGraphqlComplexityInfo(GraphqlComplexityInfo graphqlComplexityInfo) {
        this.graphqlComplexityInfo = graphqlComplexityInfo;
    }
}
