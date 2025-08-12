/*
 *   Copyright (c) {2025}, WSO2 LLC. (https://www.wso2.com)
 *
 *   WSO2 LLC. licenses this file to you under the Apache License,
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
package org.wso2.carbon.apimgt.spec.parser.definitions;

import io.swagger.v3.oas.models.SpecVersion;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.Assert;
import org.junit.Test;

public class SchemaProcessorFactoryTest {

    @Test
    public void testGetProcessor() {

        Schema<?> schema = new Schema<>();
        schema.setSpecVersion(SpecVersion.V30);
        SchemaProcessor schemaProcessor = SchemaProcessorFactory.getProcessor(schema);
        Assert.assertTrue(schemaProcessor instanceof OpenAPI30SchemaProcessor);

        schema.setSpecVersion(SpecVersion.V31);
        schemaProcessor = SchemaProcessorFactory.getProcessor(schema);
        Assert.assertTrue(schemaProcessor instanceof OpenAPI31To30SchemaProcessor);

        schema.setSpecVersion(null);
        try {
            SchemaProcessorFactory.getProcessor(schema);
        } catch (IllegalArgumentException exception) {
            Assert.assertNotNull(exception);
        }
    }
}
