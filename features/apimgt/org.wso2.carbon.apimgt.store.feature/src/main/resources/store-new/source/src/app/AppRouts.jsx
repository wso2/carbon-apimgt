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
            {isAuthenticated ? (
                <React.Fragment>
                    <Route path='/applications' component={Applications} />
                    <Route path='/application/create' component={ApplicationCreate} />
                    <Route path='/application/edit/:application_id' component={EditApp} />
                </React.Fragment>
            ) : (
                [
                    isUserFound ? (
                        <React.Fragment>
                            <Route path='/applications' component={ScopeNotFound} />
                            <Route path='/application/create' component={ScopeNotFound} />
                            <Route path='/application/edit/:application_id' component={ScopeNotFound} />
                        </React.Fragment>
                    ) : (
                        <React.Fragment>
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
