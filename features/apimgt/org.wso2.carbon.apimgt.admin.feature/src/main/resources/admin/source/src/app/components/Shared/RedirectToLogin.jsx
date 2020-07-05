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
import Configurations from 'Config';
import { FormattedMessage } from 'react-intl';
import AuthManager from 'AppData/AuthManager';
import ErrorPage from '../../errorPages/ErrorPage';

const page = Configurations.app.context + '/services/auth/login';

/**
 * This component is created to unify the login process from react UI.
 * If we need to change the login process in the future, Changing here will reflect
 * all the login redirection done in other places of the code
 * @class RedirectToLogin
 */
class RedirectToLogin extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            isError: false,
            errorCode: '',
        };
    }

    /**
     *
     * @inheritdoc
     * @memberof RedirectToLogin
     */
    componentDidMount() {
        const queryString = window.location.search;
        if (queryString.includes('?code=')) {
            const code = queryString !== undefined ? queryString.split('=') : '';
            this.setState({ errorCode: code[1] });
            this.setState({ isError: true });
        } else {
            AuthManager.discardUser();
            window.location = page;
            this.setState({ isError: false });
            return page;
        }
        return '';
    }

    /**
     *
     * @inheritdoc
     * @returns {React.Component}
     * @memberof RedirectToLogin
     */
    render() {
        const { isError, errorCode } = this.state;
        return (
            isError ? (
                <ErrorPage errorCode={errorCode} />

            ) : (
                <FormattedMessage
                    id='Apis.Shared.RedirectToLogin.you.will.be.redirected.to'
                    defaultMessage='You will be redirected to {page}'
                    values={{ page }}
                />
            )
        );
    }
}

export default RedirectToLogin;
