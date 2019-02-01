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


import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Redirect, Route, Switch } from 'react-router-dom';
import MuiThemeProvider from '@material-ui/core/styles/MuiThemeProvider';
import createMuiTheme from '@material-ui/core/styles/createMuiTheme';
import qs from 'qs';
import Log from 'log4javascript';
import ConfigManager from 'AppData/ConfigManager';
// import MaterialDesignCustomTheme from 'AppComponents/Shared/CustomTheme';
import { PageNotFound } from 'AppComponents/Base/Errors';
import Apis from 'AppComponents/Apis/Apis';
import Endpoints from 'AppComponents/Endpoints';
import Base from 'AppComponents/Base';
import AuthManager from 'AppData/AuthManager';
import Header from 'AppComponents/Base/Header';
import Avatar from 'AppComponents/Base/Header/avatar/Avatar';
import Configurations from 'Config';
import AppErrorBoundaryStyled from 'AppComponents/Shared/AppErrorBoundaryStyled';

const themes = [];

themes.push(createMuiTheme(Configurations.themes.light));
themes.push(createMuiTheme(Configurations.themes.dark));

/**
 * Render protected application paths, Implements container presenter pattern
 */
export default class Protected extends Component {
    /**
     * Creates an instance of Protected.
     * @param {any} props @inheritDoc
     * @memberof Protected
     */
    constructor(props) {
        super(props);
        this.state = {
            themeIndex: 1,
        };
        this.environments = [];
        this.toggleTheme = this.toggleTheme.bind(this);
    }

    /**
     * @inheritDoc
     * @memberof Protected
     */
    componentDidMount() {
        const storedThemeIndex = localStorage.getItem('themeIndex');
        if (storedThemeIndex) {
            this.setState({ themeIndex: parseInt(storedThemeIndex, 10) });
        }
        ConfigManager.getConfigs()
            .environments.then((response) => {
                this.environments = response.data.environments;
                // this.handleEnvironmentQueryParam(); todo: do we really need to handle environment query params here ?
            })
            .catch((error) => {
                Log.error('Error while receiving environment configurations : ', error);
            });
    }

    /**
     * Change the theme index incrementally
     */
    toggleTheme() {
        this.state.themeIndex++;
        localStorage.setItem('themeIndex', this.state.themeIndex);
    }

    /**
     * @returns {React.Component} @inheritDoc
     * @memberof Protected
     */
    render() {
        const user = AuthManager.getUser();
        const header = <Header avatar={<Avatar toggleTheme={this.toggleTheme} user={user} />} user={user} />;

        if (!user) {
            const { pathname } = window.location;
            const params = qs.stringify({
                referrer: pathname.split('/').reduce((acc, cv, ci) => (ci <= 1 ? '' : acc + '/' + cv)),
            });
            return (
                <Switch>
                    <Redirect to={{ pathname: '/login', search: params }} />
                </Switch>
            );
        }
        return (
            <MuiThemeProvider theme={themes[this.state.themeIndex % 2]}>
                <AppErrorBoundaryStyled>
                    <Base header={header}>
                        <Switch>
                            <Redirect exact from='/' to='/apis' />
                            <Route path='/apis' component={Apis} />
                            <Route path='/endpoints' component={Endpoints} />
                            <Route component={PageNotFound} />
                        </Switch>
                    </Base>
                </AppErrorBoundaryStyled>
            </MuiThemeProvider>
        );
    }
}

Protected.propTypes = {
    user: PropTypes.shape({}).isRequired,
};
