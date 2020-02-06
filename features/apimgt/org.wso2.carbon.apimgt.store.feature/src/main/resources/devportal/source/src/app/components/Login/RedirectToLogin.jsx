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
import Settings from 'Settings';
import { FormattedMessage } from 'react-intl';

const page = Settings.app.context + '/services/configs';

/**
 *
 * Just doing the redirection, If you want to trigger redirection to login page , import this util method and use.
 * Note: Don't use this method inside a render method. It will cause to cancel the initial request in Chrome
 * and re-trigger same request
 * Sample usage:
 * import { doRedirectToLogin } from 'AppComponents/Applications/Login/RedirectToLogin'
 *
 * componentDidMount() {
 *      doRedirectToLogin();
 * }
 * @export
 */
export function doRedirectToLogin() {
    window.location = page;
}

/**
 * This component is created to unify the login process from react UI.
 * If we need to change the login process in the future, Changing here will reflect
 * all the login redirection done in other places of the code
 * @class RedirectToLogin
 */
class RedirectToLogin extends React.Component {
    /**
     *
     * @inheritdoc
     * @memberof RedirectToLogin
     */
    componentDidMount() {
        doRedirectToLogin();
    }

    /**
     *
     * @inheritdoc
     * @returns {React.Component}
     * @memberof RedirectToLogin
     */
    render() {
        return (
            <FormattedMessage
                id='Login.RedirectToLogin.you.will.be.redirected.to'
                defaultMessage='You will be redirected to {page}'
                values={{ page }}
            />
        );
    }
}

export default RedirectToLogin;
