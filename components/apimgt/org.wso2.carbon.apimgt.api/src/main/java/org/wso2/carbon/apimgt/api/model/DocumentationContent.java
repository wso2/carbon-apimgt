/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.api.model;

public class DocumentationContent {
    ResourceFile resourceFile;
    String textContent;
    private ContentSourceType sourceType;

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public ContentSourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(ContentSourceType sourceType) {
        this.sourceType = sourceType;
    }

    public ResourceFile getResourceFile() {
        return resourceFile;
    }

    public void setResourceFile(ResourceFile resourceFile) {
        this.resourceFile = resourceFile;
    }

    public enum ContentSourceType {
        INLINE("In line"), MARKDOWN("Markdown"), FILE("File"), URL("URL");

        private String type;

        private ContentSourceType(String type) {
            this.type = type;
        }
    }

}
