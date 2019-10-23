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
import Apis from 'AppComponents/Apis/Apis';
import Landing from 'AppComponents/LandingPage/Landing';
import ApplicationFormHandler from 'AppComponents/Applications/ApplicationFormHandler';
import { PageNotFound, ScopeNotFound } from 'AppComponents/Base/Errors';
import RedirectToLogin from 'AppComponents/Login/RedirectToLogin';
import SettingsBase from 'AppComponents/Settings/SettingsBase';
import Listing from 'AppComponents/Applications/Listing/Listing';
import Details from 'AppComponents/Applications/Details/index';
/**
 * Handle redirection
 * @param {*} theme configuration
 * @returns {*}
 */
function getRedirectingPath(theme) {
    if (theme.custom.landingPage.active) {
        return '/home';
    } else if (theme.custom.landingPage.active === false && theme.custom.tagWise.active && theme.custom.tagWise.style === 'page') {
        return '/api-groups';
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
            <Route path='/home' component={Landing} />
            <Route path='/api-groups' component={TagCloudListing} />
            <Route path='/(apis|api-products)' component={Apis} />
            {isAuthenticated ? (
                [
                    <Route path='/settings' component={SettingsBase} />,
                    <Route exact path='/applications' component={Listing} />,
                    <Route path='/applications/create' component={ApplicationFormHandler} />,
                    <Route path='/applications/:application_id/edit' component={ApplicationFormHandler} />,
                    <Route path='/applications/:application_uuid/' component={Details} />,
                ]
            ) : (
                [
                    isUserFound ? (
                        [
                            <Route path='/settings' component={RedirectToLogin} />,
                            <Route path='/applications' component={ScopeNotFound} />,
                            <Route path='/application/create' component={ScopeNotFound} />,

                        ]
                    ) : (
                        [
                            <Route path='/settings' component={RedirectToLogin} />,
                            <Route path='/applications' component={RedirectToLogin} />,
                        ]
                    ),
                ]
            )}
            <Route component={PageNotFound} />
        </Switch>
    );
}

export default withStyles({}, { withTheme: true })(AppRouts);
