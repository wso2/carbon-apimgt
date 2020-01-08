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

import org.apache.poi.hslf.extractor.PowerPointExtractor;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xslf.extractor.XSLFPowerPointExtractor;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
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
@PrepareForTest({ MSPowerpointIndexer.class, PowerPointExtractor.class })
public class MSPowerpointIndexerTest {
    private AsyncIndexer.File2Index file2Index;

    @Before
    public void setup() {
        file2Index = new AsyncIndexer.File2Index("".getBytes(),
                null, "", -1234, "");
    }

    @Test
    public void testShouldReturnIndexedDocumentWhenParameterCorrect() throws Exception {
        POIFSFileSystem ppExtractor = Mockito.mock(POIFSFileSystem.class);
        PowerPointExtractor powerPointExtractor = Mockito.mock(PowerPointExtractor.class);
        XSLFPowerPointExtractor xslfExtractor = Mockito.mock(XSLFPowerPointExtractor.class);
        XMLSlideShow xmlSlideShow = Mockito.mock(XMLSlideShow.class);
        PowerMockito.whenNew(POIFSFileSystem.class).withParameterTypes(InputStream.class)
                .withArguments(Mockito.any(InputStream.class))
                .thenThrow(OfficeXmlFileException.class)
                .thenReturn(ppExtractor)
                .thenThrow(APIManagementException.class);
        PowerMockito.whenNew(PowerPointExtractor.class).withParameterTypes(POIFSFileSystem.class)
                .withArguments(ppExtractor).thenReturn(powerPointExtractor);
        PowerMockito.whenNew(XMLSlideShow.class).withParameterTypes(InputStream.class)
                .withArguments(Mockito.any())
                .thenReturn(xmlSlideShow);
        PowerMockito.whenNew(XSLFPowerPointExtractor.class).withArguments(xmlSlideShow).thenReturn(xslfExtractor);
        Mockito.when(powerPointExtractor.getText()).thenReturn("");
        Mockito.when(xslfExtractor.getText()).thenReturn("");
        MSPowerpointIndexer indexer = new MSPowerpointIndexer();

        IndexDocument ppDoc = indexer.getIndexedDocument(file2Index);

        // should return the default media type when media type is not defined in file2Index
        if (!"application/vnd.ms-powerpoint".equals(ppDoc.getFields().get(IndexingConstants.FIELD_MEDIA_TYPE).get(0))) {
            Assert.fail();
        }

        // should return the media type we have set in the file2Index
        file2Index.mediaType = "text/html";
        ppDoc = indexer.getIndexedDocument(file2Index);
        if (!"text/html".equals(ppDoc.getFields().get(IndexingConstants.FIELD_MEDIA_TYPE).get(0))) {
            Assert.fail();
        }

        // should return the media type we have set in the file2Index even if exception occurred while reading the file
        ppDoc = indexer.getIndexedDocument(file2Index);
        if (!"text/html".equals(ppDoc.getFields().get(IndexingConstants.FIELD_MEDIA_TYPE).get(0))) {
            Assert.fail();
        }
    }

    @Test(expected = SolrException.class)
    public void testShouldThrowExceptionWhenFailToReadFile() throws Exception {
        PowerMockito.whenNew(POIFSFileSystem.class).withParameterTypes(InputStream.class)
                .withArguments(Mockito.any(InputStream.class))
                .thenThrow(OfficeXmlFileException.class);
        PowerMockito.whenNew(XMLSlideShow.class).withParameterTypes(InputStream.class)
                .withArguments(Mockito.any())
                .thenThrow(IOException.class);

        // SolrException is expected
        MSPowerpointIndexer indexer = new MSPowerpointIndexer();
        indexer.getIndexedDocument(file2Index);
    }
}
