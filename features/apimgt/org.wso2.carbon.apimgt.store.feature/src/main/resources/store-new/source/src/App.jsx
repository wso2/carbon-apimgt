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

import { Route, Switch } from 'react-router-dom';
import Loadable from 'react-loadable';
import Login from './app/components/Login/Login';
import Logout from './app/components/Logout';
import SignUp from './app/components/AnonymousView/SignUp';
import Progress from './app/components/Shared/Progress';
import { SettingsProvider } from './app/components/Shared/SettingsContext';
import AuthManager from './app/data/AuthManager';
import BrowserRouter from './app/CustomRouter/BrowserRouter';

const LoadableProtectedApp = Loadable({
    loader: () => import(// eslint-disable-line function-paren-newline
        /* webpackChunkName: "ProtectedApp" */
        /* webpackPrefetch: true */
        // eslint-disable-next-line function-paren-newline
        './app/ProtectedApp'),
    loading: Progress,
});


/**
 * Root Store component
 *
 * @class Store
 * @extends {React.Component}
 */
class Store extends React.Component {
    /**
     *Creates an instance of Store.
     * @param {*} props Properties passed from the parent component
     * @memberof Store
     */
    constructor(props) {
        super(props);
        LoadableProtectedApp.preload();
        this.state = {
            settings: null,
            tenantDomain: null,
        };
    }

    /**
     *  Mounting the components
     */
    componentDidMount() {
        AuthManager.setSettings().then((response) => {
            this.setState({ settings: response });
        }).catch((error) => {
            console.error(
                'Error while receiving settings : ',
                error,
            );
        });
    }

    /**
     * Set the tenant domain to state
     * @param {String} tenantDomain tenant domain
     * @memberof Store
     */
    setTenantDomain = (tenantDomain) => {
        this.setState({ tenantDomain });
    }

    /**
     * Reners the Store component
     * @returns {JSX} this is the description
     * @memberof Store
     */
    render() {
        const { settings, tenantDomain } = this.state;

        return (
            settings && (
                <SettingsProvider value={{ settings, tenantDomain, setTenantDomain: this.setTenantDomain }}>
                    <BrowserRouter basename='/store-new'>
                        <Switch>
                            <Route path='/login' render={() => <Login appName='store-new' appLabel='STORE' />} />
                            <Route path='/logout' component={Logout} />
                            <Route path='/sign-up' component={SignUp} />
                            <Route component={LoadableProtectedApp} />
                        </Switch>
                    </BrowserRouter>
                </SettingsProvider>
            )
        );
    }
}

export default Store;
