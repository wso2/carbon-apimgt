/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import { BrowserRouter as Router, Redirect, Route, Switch } from 'react-router-dom';
import Loadable from 'react-loadable';
import Login from 'AppComponents/Login/Login';
import AuthManager from 'AppData/AuthManager';
import qs from 'qs';
import Utils from 'AppData/Utils';
import Logout from 'AppComponents/Logout';
import AppErrorBoundary from 'AppComponents/Shared/AppErrorBoundary';
import Progress from 'AppComponents/Shared/Progress';
// Localization
import { IntlProvider, addLocaleData, defineMessages } from 'react-intl';

const LoadableProtectedApp = Loadable({
    loader: () =>
        import(// eslint-disable-line function-paren-newline
        /* webpackChunkName: "ProtectedApp" */
        /* webpackPrefetch: true */
            './app/ProtectedApp'),
    loading: Progress,
});

/**
 * Language.
 * @type {string}
 */
const language = (navigator.languages && navigator.languages[0])
    || navigator.language
    || navigator.userLanguage;

/**
 * Language without region code.
 */
const languageWithoutRegionCode = language.toLowerCase().split(/[_-]+/)[0];

/**
 * Define base routes for the application
 * @returns {React.Component} base routes for the application
 */
class Publisher extends React.Component {
    /**
     *Creates an instance of Publisher.
     * @param {*} props
     * @memberof Publisher
     */
    constructor(props) {
        super(props);
        const { search } = window.location;
        const queryString = search.replace(/^\?/, '');
        /* With QS version up we can directly use {ignoreQueryPrefix: true} option */
        const queryParams = qs.parse(queryString);
        const { environment = Utils.getCurrentEnvironment().label } = queryParams;

        this.state = {
            user: AuthManager.getUser(environment),
            messages: {},
        };
        this.updateUser = this.updateUser.bind(this);
        this.loadLocale = this.loadLocale.bind(this);
        /* TODO: Do not need to preload if webpack magic comment `webpackPrefetch` working accordingly ,remove preload
        once it's fixed  */
        LoadableProtectedApp.preload();
    }

    /**
     * Initialize i18n.
     */
    componentWillMount() {
        const locale = languageWithoutRegionCode || language;
        this.loadLocale(locale);
    }

    /**
     *
     *
     * @param {User} user
     * @memberof Publisher
     */
    updateUser(user) {
        this.setState({ user });
    }

    /**
     * Load locale file.
     *
     * @param {string} locale Locale name
     */
    loadLocale(locale = 'en') {
        fetch(`${Utils.CONST.CONTEXT_PATH}/public/app/locales/${locale}.json`)
            .then(resp => resp.json())
            .then((data) => {
                // eslint-disable-next-line global-require, import/no-dynamic-require
                addLocaleData(require(`react-intl/locale-data/${locale}`));
                this.setState({ messages: defineMessages(data) });
            });
    }

    /**
     *
     *
     * @returns {React.Component} Render complete app component
     * @memberof Publisher
     */
    render() {
        const { user } = this.state;
        const { pathname } = window.location;
        const params = qs.stringify({
            referrer: pathname.split('/').reduce((acc, cv, ci) => (ci <= 1 ? '' : acc + '/' + cv)),
        });
        return (
            <IntlProvider locale={language} messages={this.state.messages}>
                <AppErrorBoundary appName='Publisher Application'>
                    <Router basename='/publisher'>
                        <Switch>
                            <Route path='/login' render={props => <Login {...props} updateUser={this.updateUser} />} />
                            <Route path='/logout' component={Logout} />
                            {!user && <Redirect to={{ pathname: '/login', search: params }} />}
                            <Route render={() => <LoadableProtectedApp user={user} />} />
                        </Switch>
                    </Router>
                </AppErrorBoundary>
            </IntlProvider>
        );
    }
}

export default Publisher;
