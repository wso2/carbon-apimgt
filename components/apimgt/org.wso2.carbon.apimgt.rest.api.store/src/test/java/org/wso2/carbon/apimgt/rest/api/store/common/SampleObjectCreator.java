/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.rest.api.store.common;

import org.wso2.carbon.apimgt.core.models.CompositeAPI;
import org.wso2.carbon.apimgt.core.models.Label;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class SampleObjectCreator {
    private static final String ACCESS_URL = "dummyUrl";
    public static CompositeAPI.Builder createCompositeAPIModelBuilder() {
        CompositeAPI.Builder compositeAPIBuilder = new CompositeAPI.Builder();
        compositeAPIBuilder.id(UUID.randomUUID().toString()).name("CompisteAPI").apiDefinition("definition").
                            applicationId(UUID.randomUUID().toString()).context("testcontext").provider("provider")
                            .version("1.0.0").context("testcontext").description("testdesc").labels(new HashSet<>());
        return compositeAPIBuilder;
    }

    public static Label.Builder createLabel(String name) {

        List<String> accessUrls = new ArrayList<>();
        accessUrls.add(ACCESS_URL + name);
        return new Label.Builder().
                id(UUID.randomUUID().toString()).
                name(name).
                accessUrls(accessUrls);
    }
}
