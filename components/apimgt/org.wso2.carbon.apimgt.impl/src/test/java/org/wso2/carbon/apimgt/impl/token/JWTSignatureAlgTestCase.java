/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.apimgt.impl.token;

import junit.framework.Assert;
import junit.framework.TestCase;

public class JWTSignatureAlgTestCase extends TestCase {

    public void testJwtSignatureAlgorithm(){

        JWTGenerator jwtGenerator = new JWTGenerator();

        String noneAlg = jwtGenerator.getJWSCompliantAlgorithmCode(null);

        Assert.assertTrue("Expected 'none', but was " + noneAlg, "none".equals(noneAlg));

        noneAlg = jwtGenerator.getJWSCompliantAlgorithmCode("NONE");

        Assert.assertTrue("Expected 'none', but was " + noneAlg, "none".equals(noneAlg));

        String shaWithRsa256Code = jwtGenerator.getJWSCompliantAlgorithmCode("SHA256withRSA");

        Assert.assertTrue("Expected 'RS256' but was " + shaWithRsa256Code, "RS256".equals(shaWithRsa256Code));

        shaWithRsa256Code = jwtGenerator.getJWSCompliantAlgorithmCode("RS256");

        Assert.assertTrue("Expected 'RS256' but was " + shaWithRsa256Code, "RS256".equals(shaWithRsa256Code));
    }
}
