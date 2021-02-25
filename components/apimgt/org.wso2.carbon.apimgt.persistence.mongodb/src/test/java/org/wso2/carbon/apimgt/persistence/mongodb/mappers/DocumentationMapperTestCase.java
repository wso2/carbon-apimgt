/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.carbon.apimgt.persistence.mongodb.mappers;

import junit.framework.Assert;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.wso2.carbon.apimgt.persistence.dto.Documentation;
import org.wso2.carbon.apimgt.persistence.dto.DocumentationType;
import org.wso2.carbon.apimgt.persistence.mongodb.dto.APIDocumentation;

public class DocumentationMapperTestCase {

    @Test
    public void testToAPIDocumentation() {
        ObjectId id = new ObjectId();
        APIDocumentation apiDocumentation = new APIDocumentation();
        apiDocumentation.setName("test");
        apiDocumentation.setSummary("test summary");
        apiDocumentation.setId(id);
        apiDocumentation.setType(DocumentationType.SAMPLES);
        Documentation documentation = DocumentationMapper.INSTANCE.toDocumentation(apiDocumentation);

        Assert.assertEquals("Mapped document name does not match", apiDocumentation.getName(),
                documentation.getName());
        Assert.assertEquals("Mapped document summary does not match", apiDocumentation.getSummary(),
                documentation.getSummary());
        Assert.assertEquals("Mapped document id does not match", apiDocumentation.getId().toHexString(),
                documentation.getId());
        Assert.assertEquals("Mapped document id does not match", apiDocumentation.getType(),
                documentation.getType());
    }

    @Test
    public void testToDocumentation() {
        ObjectId id = new ObjectId();
        Documentation documentation = new Documentation(DocumentationType.HOWTO,"test");
        documentation.setName("test");
        documentation.setSummary("test summary");
        documentation.setId(id.toHexString());
        APIDocumentation apiDocumentation = DocumentationMapper.INSTANCE.toAPIDocumentation(documentation);

        Assert.assertEquals("Mapped document name does not match", apiDocumentation.getName(),
                documentation.getName());
        Assert.assertEquals("Mapped document summary does not match", apiDocumentation.getSummary(),
                documentation.getSummary());
        Assert.assertEquals("Mapped document id does not match", apiDocumentation.getId().toHexString(),
                documentation.getId());
    }
}
