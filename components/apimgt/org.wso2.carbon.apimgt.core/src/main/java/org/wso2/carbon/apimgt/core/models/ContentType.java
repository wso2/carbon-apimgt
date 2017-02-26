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

package org.wso2.carbon.apimgt.core.models;

import javax.ws.rs.core.MediaType;

/**
 * Enum for selecting different contentType(or MediaType) options
 */
public enum ContentType {
    TEXT_XML(MediaType.TEXT_XML), APPLICATION_JSON(MediaType.APPLICATION_JSON), TEXT_PLAIN(MediaType.TEXT_PLAIN),
    APPLICATION_XML(MediaType.APPLICATION_XML), TEXT_HTML(MediaType.TEXT_HTML),
    CHARSET_PARAMETER(MediaType.CHARSET_PARAMETER);

    private String mediaType;

    ContentType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getMediaType() {
        return mediaType;
    }
}

