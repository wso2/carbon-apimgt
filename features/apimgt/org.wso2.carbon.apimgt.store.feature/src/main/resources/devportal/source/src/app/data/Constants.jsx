/**
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

const CONSTS = {
    HTTP_METHODS: ['get', 'put', 'post', 'delete', 'patch', 'head', 'options'],
    errorCodes: {
        INSUFFICIENT_PREVILEGES: '900403: Insufficient privileges to login',
        INVALID_TOKEN: '900401: Invalid token',
    },
    ISLOGINPERMITTED: 'isLoginPermitted',
    LOGINSTATUS: 'loginStatus',
    API_TYPES: {
        SSE: 'SSE',
        WS: 'WS',
        WEBSUB: 'WEBSUB'
    },
};

export default CONSTS;
