/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.core.util;

import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.template.dto.GatewayConfigDTO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
/**
 * 
 * Test cases for {@link APIFileUtils}
 *
 */
public class APIFileUtilsTestCase {

    String tmpDirStr = System.getProperty("java.io.tmpdir");
    String uuid1 = UUID.randomUUID().toString();
    String uuid2 = UUID.randomUUID().toString();
    String fileName = "my-file";
    String dirPath = tmpDirStr + File.separatorChar + uuid1 + File.separatorChar + uuid2;

    @BeforeClass
    public void setup() throws IOException {
    }

    @AfterClass
    public void deleteTempDirectory() throws IOException {
        // FileUtils.deleteDirectory(new File(tmpDirStr + File.separatorChar + uuid1));
    }

    @Test
    public void testCreateDirectory() throws Exception {
        APIFileUtils.createDirectory(dirPath);
        Assert.assertTrue(Files.exists(Paths.get(dirPath)), "Directory doesn't exists");

        String filePath = dirPath + File.separatorChar + fileName;
        APIFileUtils.createFile(filePath);
        Assert.assertTrue(Files.exists(Paths.get(filePath)), "File doesn't exists");

        // write content to file
        APIFileUtils.writeToFile(dirPath + File.separatorChar + fileName, "This is some test content");

        // read content as text
        String contentRead = APIFileUtils.readFileContentAsText(dirPath + File.separatorChar + fileName);
        Assert.assertEquals(contentRead, "This is some test content");

        // read content as stream
        InputStream contentReadInputStream = APIFileUtils
                .readFileContentAsStream(dirPath + File.separatorChar + fileName);
        Assert.assertEquals(IOUtils.toString(contentReadInputStream), "This is some test content");

        // write stream to filesystem
        APIFileUtils.writeStreamToFile(dirPath + File.separatorChar + "write-stream-to-file", contentReadInputStream);
        Assert.assertTrue(Files.exists(Paths.get(dirPath + File.separatorChar + "write-stream-to-file")),
                "File doesn't exists");

        // create archive
        APIFileUtils.archiveDirectory(new File(dirPath).getAbsolutePath(),
                new File(tmpDirStr + File.separatorChar + uuid1 + File.separatorChar).getAbsolutePath(),
                "my-test-archive");
        Assert.assertTrue(
                Files.exists(
                        Paths.get(tmpDirStr + File.separatorChar + uuid1 + File.separatorChar + "my-test-archive.zip")),
                "File doesn't exists");

        // find in file system
        Assert.assertNotNull(APIFileUtils.findInFileSystem(new File(dirPath), fileName));

        // write object as json
        GatewayConfigDTO configDTO = new GatewayConfigDTO();
        configDTO.setApiName("api1");
        configDTO.setConfig("config");
        configDTO.setContext("context");
        APIFileUtils.writeObjectAsJsonToFile(configDTO, dirPath + File.separatorChar + "object.json");
        Assert.assertTrue(Files.exists(Paths.get(dirPath + File.separatorChar + "object.json")), "File doesn't exists");

        // write string as json
        APIFileUtils.writeStringAsJsonToFile("{\"config\":\"config\"}", dirPath + File.separatorChar + "object-2.json");
        Assert.assertTrue(Files.exists(Paths.get(dirPath + File.separatorChar + "object-2.json")),
                "File doesn't exists");

        // list directories
        Assert.assertTrue(APIFileUtils.getDirectoryList(tmpDirStr + File.separatorChar + uuid1).size() > 0);

        // delete file
        APIFileUtils.deleteFile(filePath);
        Assert.assertFalse(Files.exists(Paths.get(filePath)), "File has not deleted");

        // delete directory
        APIFileUtils.deleteDirectory(dirPath);
        Assert.assertFalse(Files.exists(Paths.get(dirPath)), "Directory has not deleted");
    }

    @Test(expectedExceptions = APIMgtDAOException.class)
    public void testDeleteInvalidFiles() throws APIMgtDAOException {
        APIFileUtils.deleteFile(dirPath + File.separatorChar + "nonexistingfile");
    }

    @Test(expectedExceptions = APIMgtDAOException.class)
    public void testReadInvalidContentAsText() throws APIMgtDAOException {
        APIFileUtils.readFileContentAsText(dirPath + File.separatorChar + "nonexistingfile");
    }

    @Test(expectedExceptions = APIMgtDAOException.class)
    public void testReadInvalidContentAsStream() throws APIMgtDAOException {
        APIFileUtils.readFileContentAsStream(dirPath + File.separatorChar + "nonexistingfile");
    }
}
