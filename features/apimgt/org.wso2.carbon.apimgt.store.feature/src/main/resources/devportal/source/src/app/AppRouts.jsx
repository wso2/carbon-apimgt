/**
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import React from 'react';
import { Redirect, Route, Switch } from 'react-router-dom';
import { withStyles } from '@material-ui/core';
import TagCloudListing from 'AppComponents/Apis/Listing/TagCloudListing';
import Apis from './components/Apis/Apis';
import Applications from './components/Applications/Applications';
import Landing from './components/LandingPage/Landing';
import ApplicationCreate from './components/Shared/AppsAndKeys/ApplicationCreateForm';
import { PageNotFound, ScopeNotFound } from './components/Base/Errors';
import RedirectToLogin from './components/Login/RedirectToLogin';
import EditApp from './components/Applications/Edit/EditApp';
import SettingsBase from './components/Settings/SettingsBase';

/**
 * Handle redirection
 * @param {*} theme configuration
 * @returns {*}
 */
function getRedirectingPath(theme) {
    if (theme.custom.landingPage.active) {
        return '/home';
    } else if (theme.custom.landingPage.active === false && theme.custom.tagWiseMode) {
        return '/apiGroups';
    } else {
        return 'apis';
    }
}

/**
 * Handle routes
 * @param {*} props properties
 * @returns {*}
 */
function AppRouts(props) {
    const {
        isAuthenticated, isUserFound, theme,
    } = props;
    return (
        <Switch>
            <Redirect exact from='/' to={getRedirectingPath(theme)} />
            {<Route path='/home' component={Landing} />}
            {<Route path='/api-groups' component={TagCloudListing} />}
            {<Route path='/applications' component={Applications} />}
            <Route path='/(apis|api-products)' component={Apis} />
            <Route path='/settings' component={SettingsBase} />
            {isAuthenticated ? (
                <React.Fragment>
                    <Route path='/settings' component={SettingsBase} />
                    <Route path='/applications' component={Applications} />
                    <Route path='/application/create' component={ApplicationCreate} />
                    <Route path='/application/edit/:application_id' component={EditApp} />
                </React.Fragment>
            ) : (
                [
                    isUserFound ? (
                        <React.Fragment>
                            <Route path='/settings' component={RedirectToLogin} />
                            <Route path='/applications' component={ScopeNotFound} />
                            <Route path='/application/create' component={ScopeNotFound} />
                            <Route path='/application/edit/:application_id' component={ScopeNotFound} />
                        </React.Fragment>
                    ) : (
                        <React.Fragment>
                            <Route path='/settings' component={RedirectToLogin} />
                            <Route path='/applications' component={RedirectToLogin} />
                            <Route path='/application/create' component={RedirectToLogin} />
                            <Route path='/application/edit/:application_id' component={RedirectToLogin} />
                        </React.Fragment>
                    ),
                ]
            )}
            <Route component={PageNotFound} />
        </Switch>
    );
}

export default withStyles({}, { withTheme: true })(AppRouts);
