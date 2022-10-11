/*
 *   Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.apk.apimgt.impl.definitions;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import io.swagger.models.Model;
import io.swagger.models.properties.Property;

/**
 * Used to ignore "responseSchema" objects when generating the swagger
 */
public abstract class ResponseSchemaMixin {
    public ResponseSchemaMixin() {
    }

    @JsonIgnore
    public abstract Property getSchema();

    @JsonIgnore
    public abstract void setSchema(Property var1);

    @JsonGetter("schema")
    public abstract Model getResponseSchema();

    @JsonSetter("schema")
    public abstract void setResponseSchema(Model var1);
}
