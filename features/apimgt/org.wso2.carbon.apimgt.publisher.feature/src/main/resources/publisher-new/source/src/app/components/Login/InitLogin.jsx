/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React from 'react';

/**
 * This component is created to unify the login process from react UI.
 * If we need to change the login process in the future, Changing here will reflect
 * all the login redirection done in other places of the code
 * @class InitLogin
 */

const page = '/publisher-new/services/auth/login';
class InitLogin extends React.Component {
    componentDidMount() {
        window.location = page;
    }

    render() {
        return `You will be redirected to ${page}`;
    }
}
export default InitLogin;
