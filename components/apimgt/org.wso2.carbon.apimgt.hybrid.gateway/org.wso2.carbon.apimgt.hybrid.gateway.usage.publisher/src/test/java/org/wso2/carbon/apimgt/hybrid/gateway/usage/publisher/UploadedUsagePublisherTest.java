/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.constants.Constants;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.dao.UploadedUsageFileInfoDAO;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.dto.UploadedFileInfoDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.util.UsagePublisherUtils;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.util.gzip.GZIPUtils;
import org.wso2.carbon.databridge.agent.DataPublisher;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static org.mockito.Matchers.any;

/**
 * UploadedUsagePublisher Test Class
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ UsagePublisherUtils.class, GZIPUtils.class,
        UploadedUsageFileInfoDAO.class, FileUtils.class }) public class UploadedUsagePublisherTest {

    private final String tenantDomain = "ccc2222";
    private final String compressedFileName = "api-usage-data.dat.1517296920006.gz";

    @Before
    public void setUp() throws Exception {
        TestUtil util = new TestUtil();
        util.setupCarbonHome();
    }

    @Test
    public void run() throws Exception {
        PowerMockito.mockStatic(GZIPUtils.class);
        PowerMockito.mockStatic(FileUtils.class);
        PowerMockito.mockStatic(UsagePublisherUtils.class);
        DataPublisher dataPublisher = Mockito.mock(DataPublisher.class);
        String fileDir =
                System.getProperty(Constants.CARBON_HOME) + File.separator + Constants.API_USAGE_DATA + File.separator
                        + compressedFileName;
        InputStream anyInputStream = new FileInputStream(fileDir);
        PowerMockito.mockStatic(UploadedUsageFileInfoDAO.class);
        PowerMockito.when(UploadedUsageFileInfoDAO.class, "getFileContent", any(UploadedFileInfoDTO.class))
                .thenReturn(anyInputStream);
        PowerMockito.doNothing()
                .when(UploadedUsageFileInfoDAO.class, "updateCompletion", any(UploadedFileInfoDTO.class));
        PowerMockito.when(UsagePublisherUtils.getDataPublisher()).thenReturn(dataPublisher);
        UploadedFileInfoDTO infoDTO = new UploadedFileInfoDTO(tenantDomain, compressedFileName, 121232);
        UploadedUsagePublisher uploadedUsagePublisher = new UploadedUsagePublisher(infoDTO);
        uploadedUsagePublisher.run();
    }

    @Test(expected = Exception.class)
    public void run_throwsExceptionWhileCreatingPayload() throws Exception {
        PowerMockito.mockStatic(GZIPUtils.class);
        PowerMockito.mockStatic(FileUtils.class);
        PowerMockito.mockStatic(UsagePublisherUtils.class);
        PowerMockito.when(UsagePublisherUtils.getDataPublisher()).thenThrow(Exception.class);
        String fileDir = System.getProperty(Constants.CARBON_HOME) + File.separator + Constants.API_USAGE_DATA;
        PowerMockito.when(UsagePublisherUtils.getUploadedFileDirPath(any(String.class), any(String.class)))
                .thenReturn(fileDir);
        UploadedFileInfoDTO infoDTO = new UploadedFileInfoDTO(tenantDomain, compressedFileName, 1213232);
        UploadedUsagePublisher uploadedUsagePublisher = new UploadedUsagePublisher(infoDTO);
        uploadedUsagePublisher.run();
    }

    @Test
    public void run_throwsExceptionWhileDecompressingFile() throws Exception {
        PowerMockito.mockStatic(GZIPUtils.class);
        PowerMockito.mockStatic(FileUtils.class);
        PowerMockito.mockStatic(UsagePublisherUtils.class);
        DataPublisher dataPublisher = Mockito.mock(DataPublisher.class);
        PowerMockito.when(UsagePublisherUtils.getDataPublisher()).thenReturn(dataPublisher);
        PowerMockito.mockStatic(UploadedUsageFileInfoDAO.class);
        InputStream anyInputStream = new ByteArrayInputStream("test data".getBytes());
        PowerMockito.when(UploadedUsageFileInfoDAO.class, "getFileContent", any(UploadedFileInfoDTO.class))
                .thenReturn(anyInputStream);
        PowerMockito.doNothing()
                .when(UploadedUsageFileInfoDAO.class, "updateCompletion", any(UploadedFileInfoDTO.class));
        UploadedFileInfoDTO infoDTO = new UploadedFileInfoDTO(tenantDomain, compressedFileName, 1213232);
        UploadedUsagePublisher uploadedUsagePublisher = new UploadedUsagePublisher(infoDTO);
        uploadedUsagePublisher.run();
    }
}
