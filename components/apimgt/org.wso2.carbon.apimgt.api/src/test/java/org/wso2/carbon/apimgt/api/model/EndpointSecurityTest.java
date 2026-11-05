/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.api.model;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link EndpointSecurity}, focused on the copy constructor propagating the GCP
 * {@code serviceAccountKey}. The security object is cloned in several flows (e.g. copying production
 * config to sandbox), so a dropped field would silently strip Vertex AI credentials.
 */
public class EndpointSecurityTest {

    @Test
    public void testCopyConstructorPreservesServiceAccountKey() {

        EndpointSecurity original = new EndpointSecurity();
        original.setType("gcp");
        original.setEnabled(true);
        original.setServiceAccountKey("chunk:v1:abc;def");

        EndpointSecurity copy = new EndpointSecurity(original);

        Assert.assertEquals("The service-account key must survive the copy",
                "chunk:v1:abc;def", copy.getServiceAccountKey());
        Assert.assertEquals("gcp", copy.getType());
        Assert.assertTrue(copy.isEnabled());
    }

    @Test
    public void testCopyConstructorWithNoServiceAccountKey() {

        EndpointSecurity original = new EndpointSecurity();
        original.setType("gcp");

        EndpointSecurity copy = new EndpointSecurity(original);

        Assert.assertNull("A keyless GCP security object must copy with no service-account key",
                copy.getServiceAccountKey());
    }
}
