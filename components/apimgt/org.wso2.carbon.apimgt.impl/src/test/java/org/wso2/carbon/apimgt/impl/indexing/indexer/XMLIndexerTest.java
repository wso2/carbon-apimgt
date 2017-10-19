/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.impl.indexing.indexer;

import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.indexing.AsyncIndexer;
import org.wso2.carbon.registry.indexing.solr.IndexDocument;

public class XMLIndexerTest {

    @Test
    public void testShouldReturnIndexedDocumentWhenParameterCorrect() throws RegistryException {
        String mediaType = "text/xml";
        final String MEDIA_TYPE = "mediaType";
        AsyncIndexer.File2Index file2Index = new AsyncIndexer.File2Index("".getBytes(),
                null, "", -1234, "");
        XMLIndexer indexer = new XMLIndexer();

        // should return the the default media type when media type is not defined in file2Index
        IndexDocument xml = indexer.getIndexedDocument(file2Index);
        if (xml.getFields().get(MEDIA_TYPE) != null) {
            Assert.fail();
        }

        // should return the media type we have set in the file2Index
        file2Index.mediaType = mediaType;
        xml = indexer.getIndexedDocument(file2Index);
        if (!mediaType.equals(xml.getFields().get(MEDIA_TYPE).get(0))) {
            Assert.fail();
        }

    }
}
