/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import SwaggerClient from 'swagger-client';
// Parse the swagger definition in the worker thread and
// pass the parsed plain JS object to main thread via postMessage
SwaggerClient.resolve({
    url: '/api/am/publisher/v2/swagger.yaml',
    requestInterceptor: (request) => {
        request.headers.Accept = 'text/yaml';
    },
}).then((spec) => {
    // disable following rule because linter is unaware of the worker source
    // eslint-disable-next-line no-restricted-globals
    self.postMessage(spec);
});
