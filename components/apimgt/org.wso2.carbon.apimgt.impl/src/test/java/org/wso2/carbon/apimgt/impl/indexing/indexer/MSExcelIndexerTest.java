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

import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.solr.common.SolrException;
import org.apache.synapse.unittest.testcase.data.classes.AssertNotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.wso2.carbon.apimgt.impl.indexing.indexer.util.MSExcelIndexerWrapper;
import org.wso2.carbon.registry.indexing.AsyncIndexer;

import java.io.IOException;

import static org.junit.Assert.fail;

public class MSExcelIndexerTest {
    private ExcelExtractor excelExtractor;
    private XSSFExcelExtractor xssfExtractor;
    private MSExcelIndexer msExcelIndexer;
    private AsyncIndexer.File2Index file2Index;

    @Before
    public void setup() {
        excelExtractor = Mockito.mock(ExcelExtractor.class);
        xssfExtractor = Mockito.mock(XSSFExcelExtractor.class);
        msExcelIndexer = new MSExcelIndexerWrapper(xssfExtractor, excelExtractor);
        file2Index = new AsyncIndexer.File2Index("".getBytes(),
                "", "", -1234, "");
    }

    @Test
    public void testShouldReturnIndexedDocmentWhenParameterCorrect() {
        String excelText = "excel";
        Mockito.when(xssfExtractor.getText()).thenReturn(excelText);

        try {
            // retrieving indexed document with ExcelExtractor
            msExcelIndexer.getIndexedDocument(file2Index);

            // switching the mediaType null check
            file2Index = new AsyncIndexer.File2Index("".getBytes(),
                    null, "", -1234, "");

            // retrieving indexed document with MSExcelIndexer
            Assert.assertNotNull(msExcelIndexer.getIndexedDocument(file2Index));

        } catch (Exception e) {
            Assert.fail("Should not throw any exceptions");
        }
    }
}
