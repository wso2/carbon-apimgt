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
import { StylesProvider, jssPreset } from '@material-ui/core/styles';
import { addLocaleData, IntlProvider } from 'react-intl';
import Configurations from 'Config';
import merge from 'lodash.merge';
import cloneDeep from 'lodash.clonedeep';
import { create } from 'jss';
import rtl from 'jss-rtl';
import { MuiThemeProvider, createMuiTheme } from '@material-ui/core/styles';
import Settings from 'Settings';
import Logout from './app/components/Logout';
import Progress from './app/components/Shared/Progress';
import { SettingsProvider } from './app/components/Shared/SettingsContext';
import API from './app/data/api';
import BrowserRouter from './app/components/Base/CustomRouter/BrowserRouter';
import DefaultConfigurations from './defaultTheme';
const protectedApp = lazy(() => import('./app/ProtectedApp' /* webpackChunkName: "ProtectedApp" */));

/**
 * Language.
 * @type {string}
 */
const language = (navigator.languages && navigator.languages[0]) || navigator.language || navigator.userLanguage;

/**
 * Language without region code.
 */
const languageWithoutRegionCode = language.toLowerCase().split(/[_-]+/)[0];


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
        };
        this.systemTheme = merge(cloneDeep(DefaultConfigurations), Configurations);
        this.setTenantTheme = this.setTenantTheme.bind(this);
        this.setSettings = this.setSettings.bind(this);
    }

    /**
     *  Mounting the components
     */
    componentDidMount() {
        const api = new API();
        const promisedSettings = api.getSettings();
        promisedSettings
            .then((response) => {
                this.setSettings(response.body);
            }).catch((error) => {
                console.error(
                    'Error while receiving settings : ',
                    error,
                );
            });
        const urlParams = new URLSearchParams(window.location.search);
        if (urlParams.get('tenant') === null || urlParams.get('tenant') === 'carbon.super') {
            this.updateLocale();
            this.setState({ theme: this.systemTheme });
        } else {
            this.setTenantTheme(urlParams.get('tenant'));
        }

        
    }
     /**
     * Load locale file.
     *
     * @param {string} locale Locale name
     */
    loadLocale(locale = 'en') {
        fetch(`${Settings.app.context}/site/public/locales/${locale}.json`)
            .then((resp) => resp.json())
            .then((messages) => {
                // eslint-disable-next-line global-require, import/no-dynamic-require
                addLocaleData(require(`react-intl/locale-data/${locale}`));
                this.setState({ messages });
            });
    }
    /**
     * Set the local settings
     *
     * @memberof DevPortal
     */
    updateLocale(localTheme = this.systemTheme) {
        let locale = languageWithoutRegionCode || language;
        //The above can be overriden by the language switcher
        const { direction: defaultDirection, custom: { languageSwitch: { active: languageSwitchActive, languages } } } = localTheme;
        if(languageSwitchActive){
            let selectedLanguage = localStorage.getItem('language');
            let direction = defaultDirection;
            if(!selectedLanguage && languages && languages.length > 0){
                selectedLanguage = languages[0].key;
            }
            for(var i=0; i < languages.length; i++){
                if(selectedLanguage === languages[i].key && languages[i].direction){
                    direction = languages[i].direction;
                    document.body.setAttribute('dir',direction);
                    this.systemTheme.direction = direction;
                }
            }
            if(selectedLanguage) {
                locale = selectedLanguage;
            }
        } else {
            const oldTheme = merge(DefaultConfigurations, Configurations);
            const oldLangDirection = oldTheme.direction;
            if(oldLangDirection !== this.systemTheme.direction){
                document.body.setAttribute('dir',oldLangDirection);
                this.systemTheme.direction = oldLangDirection;
            }
            
        }
        this.loadLocale(locale);
        window.addEventListener('message', this.handleMessage);
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
        if(tenant && tenant !== "INVALID"){
            fetch(`${Settings.app.context}/site/public/tenant_themes/${tenant}/apim/defaultTheme.json`)
            .then((resp) => resp.json())
            .then((data) => {
                // Merging with the system theme.
                const tenantMergedTheme = merge(cloneDeep(this.systemTheme), data);
                this.updateLocale(tenantMergedTheme);
                this.setState({ theme: tenantMergedTheme });
            })
            .catch(() => {
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
            return (
                <link
                    rel='stylesheet'
                    type='text/css'
                    href={`${Settings.app.context}/${cssUrlWithTenant}`}
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
     * Reners the DevPortal component
     * @returns {JSX} this is the description
     * @memberof DevPortal
     */
    render() {
        const { settings, tenantDomain, theme, messages } = this.state;
        const { app: { context } } = Settings;
        return (
            settings && theme && (
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
            )
        );
    }
}

export default DevPortal;
