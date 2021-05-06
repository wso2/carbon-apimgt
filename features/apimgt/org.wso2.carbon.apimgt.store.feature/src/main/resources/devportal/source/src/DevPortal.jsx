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

import React, { Suspense, lazy } from 'react';
import { Route, Switch } from 'react-router-dom';
import { Helmet } from 'react-helmet';
import { StylesProvider as StylesProviderCore, jssPreset as jssPresetCore } from '@material-ui/core/styles';
import { StylesProvider as StylesProviderPlain, jssPreset as jssPresetPlain } from '@material-ui/styles';
import { IntlProvider } from 'react-intl';
import Configurations from 'Config';
import merge from 'lodash.merge';
import cloneDeep from 'lodash.clonedeep';
import { create } from 'jss';
import rtl from 'jss-rtl';
import { MuiThemeProvider as ThemeProviderWithMui, createMuiTheme } from '@material-ui/core/styles';
import { ThemeProvider as ThemeProviderPlain } from '@material-ui/core/styles';
import Utils from 'AppData/Utils';
import Settings from 'Settings';
import Logout from './app/components/Logout';
import Progress from './app/components/Shared/Progress';
import { SettingsProvider } from './app/components/Shared/SettingsContext';
import API from './app/data/api';
import BrowserRouter from './app/components/Base/CustomRouter/BrowserRouter';
import DefaultConfigurations from './defaultTheme';
import AuthManager from './app/data/AuthManager';
import Loading from './app/components/Base/Loading/Loading';
import CONSTS from './app/data/Constants';

const MuiThemeProvider = ThemeProviderPlain || ThemeProviderWithMui;
const StylesProvider = StylesProviderCore || StylesProviderPlain;
const jssPreset = jssPresetPlain || jssPresetCore;

const protectedApp = lazy(() => import('./app/ProtectedApp' /* webpackChunkName: "ProtectedApp" */));

// Configure JSS
const jss = create({ plugins: [...jssPreset().plugins, rtl()] });
/**
 * Root DevPortal component
 *
 * @class DevPortal
 * @extends {React.Component}
 */
class DevPortal extends React.Component {
    /**
     *Creates an instance of DevPortal.
     * @param {*} props Properties passed from the parent component
     * @memberof DevPortal
     */
    constructor(props) {
        super(props);
        this.state = {
            messages: {},
            settings: null,
            tenantDomain: null,
            theme: null,
            isNonAnonymous: false,
            lanuage: null,
            redirecting: false,
        };
        this.systemTheme = merge(cloneDeep(DefaultConfigurations), Configurations);
        this.setTenantTheme = this.setTenantTheme.bind(this);
        this.setSettings = this.setSettings.bind(this);
    }

    /**
     *  Mounting the components
     */
    componentDidMount() {
        const { app: { customUrl: { tenantDomain: customUrlEnabledDomain } } } = Settings;
        let tenant = null;
        const urlParams = new URLSearchParams(window.location.search);
        if (customUrlEnabledDomain !== 'null') {
            tenant = customUrlEnabledDomain;
        } else {
            tenant = urlParams.get('tenant')
        }
        const api = new API();
        const promisedSettings = api.getSettings();
        promisedSettings
            .then((response) => {
                this.setSettings(response.body);
                if (!this.state.settings.IsAnonymousModeEnabled) {
                    this.setState({ isNonAnonymous: true });
                }
                if (Settings.app.isPassive && !AuthManager.getUser()
                    && !sessionStorage.getItem(CONSTS.ISLOGINPERMITTED) && !this.state.isNonAnonymous) {
                    this.checkLoginUser();
                }
            })
            .catch((error) => {
                console.error(
                    'Error while receiving settings : ',
                    error,
                );
            });
        if (tenant === null || tenant === 'carbon.super') {
            const { custom: { publicTenantStore } } = this.systemTheme;
            if(publicTenantStore) {
                const { active: publicTenantStoreActive, redirectToIfInactive } = publicTenantStore;
                if( !publicTenantStoreActive ) {
                    window.location.href = redirectToIfInactive;
                    this.setState( { redirecting: true})
                } else {
                    this.updateLocale();
                    this.setState({ theme: this.systemTheme, redirecting: false });
                }
            } else {
                this.updateLocale();
                this.setState({ theme: this.systemTheme, redirecting: false });
            }
        } else {
            this.setTenantTheme(tenant);
        }
    }
    /**
    * Load locale file.
    *
    * @param {string} locale Locale name
    */
    loadLocale(locale = 'en') {
        fetch(`${Settings.app.context}/site/public/locales/${locale}.json`)
            .then((resp) => {
                if (resp.status === 200) {
                    return (resp.json());
                } else {
                    return {};
                }
            })
            .then((messages) => {
                // eslint-disable-next-line global-require, import/no-dynamic-require
                this.setState({ messages, language: locale });
            });
    }
    /**
     * Set the local settings
     *
     * @memberof DevPortal
     */
    updateLocale(localTheme = this.systemTheme) {
        //The above can be overriden by the language switcher
        let browserLocal = Utils.getBrowserLocal();
        const { direction: defaultDirection, custom: { languageSwitch: { active: languageSwitchActive, languages } } } = localTheme;
        let lanauageToLoad = null;
        if (languageSwitchActive) {
            const savedLanguage = localStorage.getItem('language');
            let direction = defaultDirection;
            let selectedLanuageObject = null;
            for (var i = 0; i < languages.length; i++) {
                if (savedLanguage && savedLanguage === languages[i].key) {
                    selectedLanuageObject = languages[i];
                } else if (!savedLanguage && browserLocal === languages[i].key) {
                    selectedLanuageObject = languages[i];
                }
            }
            if (selectedLanuageObject) {
                direction = selectedLanuageObject.direction || defaultDirection;
            }
            document.body.setAttribute('dir', direction);
            this.systemTheme.direction = direction;
            lanauageToLoad = savedLanguage || selectedLanuageObject.key || browserLocal;
        } else {
            // If the lanauage switch was disabled after setting a cookie we need to remove the cookie and 
            // force the selected lanuage to the browserLocal.
            lanauageToLoad = browserLocal;
            document.body.setAttribute('dir', localTheme.direction);
            this.systemTheme.direction = localTheme.direction;
        }
        this.loadLocale(lanauageToLoad);
    }
    /**
     * Set the tenant domain to state
     * @param {String} tenantDomain tenant domain
     * @memberof DevPortal
     */
    setTenantDomain = (tenantDomain) => {
        this.setState({ tenantDomain });
        if (tenantDomain === 'carbon.super') {
            this.setState({ theme: this.systemTheme });
        } else {
            this.setTenantTheme(tenantDomain);
        }
    }


    /**
     *
     * for more information about this pattern
     * reffer https://reactjs.org/docs/context.html#updating-context-from-a-nested-component
     * @param {Object} settings set the settings state in the APP state, which will implesitly
     * set in the Settings context
     * @memberof DevPortal
     */
    setSettings(settings) {
        this.setState({ settings });
    }

    /**
     * Load Theme file.
     *
     * @param {string} tenant tenant name
     */
    setTenantTheme(tenant) {
        if (tenant && tenant !== 'INVALID') {
            fetch(`${Settings.app.context}/site/public/tenant_themes/${tenant}/apim/defaultTheme.json`)
                .then(response => {
                    if (!response.ok) {
                        throw new Error("HTTP error " + response.status);
                    }
                    return response.json();
                })
                .then((data) => {
                    // Merging with the system theme.
                    const tenantMergedTheme = merge(cloneDeep(DefaultConfigurations), Configurations, data);
                    this.updateLocale(tenantMergedTheme);
                    this.setState({ theme: tenantMergedTheme });
                })
                .catch(() => {
                    console.log('Error loading teant theme. Loading the default theme.');
                    this.updateLocale();
                    this.setState({ theme: this.systemTheme });
                });
        } else {
            this.updateLocale();
            this.setState({ theme: this.systemTheme });
        }
    }

    /**
     * Add two numbers.
     * @param {object} theme object.
     * @returns {JSX} link dom tag.
     */
    loadCustomCSS(theme) {
        const { custom: { tenantCustomCss } } = theme;
        const { tenantDomain } = this.state;
        let cssUrlWithTenant = tenantCustomCss;
        if (tenantDomain && tenantCustomCss) {
            cssUrlWithTenant = tenantCustomCss.replace('<tenant-domain>', tenantDomain);
        }
        if (cssUrlWithTenant) {
            let url = cssUrlWithTenant;
            
            if(Settings.app.context === ''){
                if(/^\//.test(cssUrlWithTenant)){
                    url = cssUrlWithTenant.substr(1);
                } else {
                    url = cssUrlWithTenant;
                }
            } else {
                url = Settings.app.context + '/' + cssUrlWithTenant;
            }
            return (
                <link
                    rel='stylesheet'
                    type='text/css'
                    href={url}
                />
            );
        } else {
            return '';
        }
    }

    /**
     * Add two numbers.
     * @param {object} theme object.
     * @returns {JSX} link dom tag.
     */
    getTitle(theme) {
        const {
            custom: {
                title: {
                    prefix, sufix,
                },
            },
        } = theme;
        return (prefix + sufix);
    }

    /**
     * If the passive mode is enabled then this method will check whether
     * a user is already logged into the publisher.
     */
    checkLoginUser() {
        if (!sessionStorage.getItem(CONSTS.LOGINSTATUS)) {
            sessionStorage.setItem(CONSTS.LOGINSTATUS, 'check-Login-status');
            window.location = Settings.app.context + '/services/configs?loginPrompt=false';
        } else if (sessionStorage.getItem(CONSTS.LOGINSTATUS)) {
            sessionStorage.removeItem(CONSTS.LOGINSTATUS);
        }
    }

    /**
     * Reners the DevPortal component
     * @returns {JSX} this is the description
     * @memberof DevPortal
     */
    render() {
        const { settings, tenantDomain, theme, messages, language, redirecting } = this.state;
        const { app: { context } } = Settings;
        if(redirecting) {
            return (
                <Progress />
            )
        }
        if (settings && theme && messages && language) {
            return (
                <SettingsProvider value={{
                    settings,
                    setSettings: this.setSettings,
                    tenantDomain,
                    setTenantDomain: this.setTenantDomain,
                }}
                >
                    <Helmet>
                        <title>{this.getTitle(theme)}</title>
                    </Helmet>
                    <MuiThemeProvider theme={createMuiTheme(theme)}>
                        <StylesProvider jss={jss}>
                            {this.loadCustomCSS(theme)}
                            <BrowserRouter basename={context}>
                                <Suspense fallback={<Progress />}>
                                    <IntlProvider locale={language} messages={messages}>
                                        <Switch>
                                            <Route path='/logout' component={Logout} />
                                            <Route component={protectedApp} />
                                        </Switch>
                                    </IntlProvider>
                                </Suspense>
                            </BrowserRouter>
                        </StylesProvider>
                    </MuiThemeProvider>
                </SettingsProvider>
            );
        } else {
            return (
                <Progress />
            )
        }

    }
}

export default DevPortal;
