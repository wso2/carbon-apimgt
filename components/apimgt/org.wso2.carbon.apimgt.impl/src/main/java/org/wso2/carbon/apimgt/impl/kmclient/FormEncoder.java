/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.kmclient;

import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

public class FormEncoder implements Encoder {
    @Override
    public void encode(Object o, Type type, RequestTemplate requestTemplate) throws EncodeException {
        Map<String, Object> params = (Map<String, Object>) o;
        String paramString = params.entrySet().stream()
                .map(this::urlEncodeKeyValuePair)
                .collect(Collectors.joining("&"));
        requestTemplate.body(paramString);
    }

    private String urlEncodeKeyValuePair(Map.Entry<String, Object> entry) {
        try {
            return URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString()) + '='
                    + URLEncoder.encode(String.valueOf(entry.getValue()), StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new EncodeException("Error occurred while URL encoding message", ex);
        }
    }
}
