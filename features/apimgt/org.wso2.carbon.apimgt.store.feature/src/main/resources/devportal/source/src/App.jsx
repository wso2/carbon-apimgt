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
import Configurations from 'Config';
import Settings from 'Settings';
import { MuiThemeProvider, createMuiTheme } from '@material-ui/core/styles';
import Login from './app/components/Login/Login';
import Logout from './app/components/Logout';
import SignUp from './app/components/AnonymousView/SignUp';
import Progress from './app/components/Shared/Progress';
import defaultTheme from './theme.json';
import { SettingsProvider } from './app/components/Shared/SettingsContext';
import AuthManager from './app/data/AuthManager';
import BrowserRouter from './app/components/Base/CustomRouter/BrowserRouter';

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
            istenantThemeAvailble: false,
            theme: null,
        };
        this.SetTenantTheme = this.SetTenantTheme.bind(this);
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
        const urlParams = new URLSearchParams(window.location.search);
        if (urlParams.get('tenant') === null || urlParams.get('tenant') === 'carbon.super') {
            this.setState({ theme: defaultTheme.themes.light });
        } else {
            this.SetTenantTheme(urlParams.get('tenant'));
        }
    }

    /**
     * Set the tenant domain to state
     * @param {String} tenantDomain tenant domain
     * @memberof Store
     */
    setTenantDomain = (tenantDomain) => {
        this.setState({ tenantDomain });
        if (tenantDomain === 'carbon.super') {
            this.setState({ theme: defaultTheme.themes.light });
        } else {
            this.SetTenantTheme(tenantDomain);
        }
    }

    /**
     * Load Theme file.
     *
     * @param {string} tenant tenant name
     */
    SetTenantTheme(tenant) {
        fetch(`${Settings.app.context}/site/public/tenant_themes/${tenant}/defaultTheme.json`)
            .then(resp => resp.json())
            .then((data) => {
                this.setState({ theme: data.themes.light });
                this.setState({ istenantThemeAvailble: true });
            })
            .catch(() => {
                this.setState({ theme: defaultTheme.themes.light });
            });
    }

    /**
     * Reners the Store component
     * @returns {JSX} this is the description
     * @memberof Store
     */
    render() {
        const {
            settings, tenantDomain, theme, istenantThemeAvailble,
        } = this.state;
        const { app: { context } } = Settings;
        if (theme && !istenantThemeAvailble) {
            Object.assign(theme, JSON.parse(JSON.stringify(Configurations.themes.light)));
        }
        return (
            settings && theme && (
                <SettingsProvider value={{ settings, tenantDomain, setTenantDomain: this.setTenantDomain }}>
                    <MuiThemeProvider theme={createMuiTheme(theme)}>
                        <BrowserRouter basename={context}>
                            <Switch>
                                <Route path='/login' render={() => <Login appName='store' appLabel='STORE' />} />
                                <Route path='/logout' component={Logout} />
                                <Route path='/sign-up' component={SignUp} />
                                <Route component={LoadableProtectedApp} />
                            </Switch>
                        </BrowserRouter>
                    </MuiThemeProvider>
                </SettingsProvider>
            )
        );
    }
}

export default Store;
