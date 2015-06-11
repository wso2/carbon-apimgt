/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.apimgt.api.model;

import java.io.Serializable;

@SuppressWarnings("unused")
public final class FileData implements Serializable{

    private static final long serialVersionUID = 8159854241542883627L;
    
    private final byte[] content;
    private final String fileName;
    private final String contentType;
    private final String filePath;
    
    public FileData(byte[] content, String fileName, String contentType, String filePath) {
	super();
	this.content = content;
	this.fileName = fileName;
	this.contentType = contentType;
	this.filePath = filePath;
    }

    public byte[] getContent() {
        return content;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public String getFilePath() {
        return filePath;
    }
    
    

}
