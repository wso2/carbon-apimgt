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

import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.solr.common.SolrException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.registry.indexing.AsyncIndexer;
import org.wso2.carbon.registry.indexing.IndexingConstants;
import org.wso2.carbon.registry.indexing.solr.IndexDocument;

import java.io.IOException;
import java.io.InputStream;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ MSWordIndexer.class, WordExtractor.class })
public class MSWordIndexerTest {
    private AsyncIndexer.File2Index file2Index;

    @Before
    public void setup() {
        file2Index = new AsyncIndexer.File2Index("".getBytes(),
                null, "", -1234, "");
    }

    @Test
    public void testShouldReturnIndexedDocumentWhenParameterCorrect() throws Exception {
        POIFSFileSystem poiFS = Mockito.mock(POIFSFileSystem.class);
        WordExtractor wordExtractor = Mockito.mock(WordExtractor.class);
        XWPFWordExtractor xwpfExtractor = Mockito.mock(XWPFWordExtractor.class);
        XWPFDocument xwpfDocument = Mockito.mock(XWPFDocument.class);
        PowerMockito.whenNew(POIFSFileSystem.class).withParameterTypes(InputStream.class)
                .withArguments(Mockito.any(InputStream.class))
                .thenThrow(OfficeXmlFileException.class)
                .thenReturn(poiFS)
                .thenThrow(APIManagementException.class);
        PowerMockito.whenNew(WordExtractor.class).withArguments(poiFS).thenReturn(wordExtractor);
        PowerMockito.whenNew(XWPFDocument.class).withParameterTypes(InputStream.class)
                .withArguments(Mockito.any())
                .thenReturn(xwpfDocument);
        PowerMockito.whenNew(XWPFWordExtractor.class).withArguments(xwpfDocument).thenReturn(xwpfExtractor);
        Mockito.when(wordExtractor.getText()).thenReturn("");
        Mockito.when(xwpfExtractor.getText()).thenReturn("");
        MSWordIndexer indexer = new MSWordIndexer();

        IndexDocument wordDoc = indexer.getIndexedDocument(file2Index);

        // should return the default media type when media type is not defined in file2Index
        if (!"application/pdf".equals(wordDoc.getFields().get(IndexingConstants.FIELD_MEDIA_TYPE).get(0))) {
            Assert.fail();
        }

        // should return the media type we have set in the file2Index
        file2Index.mediaType = "text/html";
        wordDoc = indexer.getIndexedDocument(file2Index);
        if (!"text/html".equals(wordDoc.getFields().get(IndexingConstants.FIELD_MEDIA_TYPE).get(0))) {
            Assert.fail();
        }

        // should return the media type we have set in the file2Index even if exception occurred while reading the file
        file2Index.mediaType = "text/html";
        wordDoc = indexer.getIndexedDocument(file2Index);
        if (!"text/html".equals(wordDoc.getFields().get(IndexingConstants.FIELD_MEDIA_TYPE).get(0))) {
            Assert.fail();
        }
    }

    @Test(expected = SolrException.class)
    public void testShouldThrowExceptionWhenFailToReadFile() throws Exception {
        PowerMockito.whenNew(POIFSFileSystem.class).withParameterTypes(InputStream.class)
                .withArguments(Mockito.any(InputStream.class))
                .thenThrow(OfficeXmlFileException.class);
        PowerMockito.whenNew(XWPFDocument.class).withParameterTypes(InputStream.class)
                .withArguments(Mockito.any())
                .thenThrow(IOException.class);

        // SolrException is expected
        MSWordIndexer indexer = new MSWordIndexer();
        indexer.getIndexedDocument(file2Index);
    }
}
