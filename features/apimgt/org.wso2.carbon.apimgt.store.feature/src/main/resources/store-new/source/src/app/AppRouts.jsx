import React from 'react';
import { Redirect, Route, Switch } from 'react-router-dom';
import { withStyles } from '@material-ui/core';
import Apis from './components/Apis/Apis';
import Applications from './components/Applications/Applications';
import Landing from './components/LandingPage/Landing';
import ApplicationCreate from './components/Shared/AppsAndKeys/ApplicationCreateForm';
import { PageNotFound, ScopeNotFound } from './components/Base/Errors';
import EditApp from './components/Applications/Edit/EditApp';

/**
 * Handle routes
 * @param {*} props properties
 * @returns {*}
 */
function AppRouts(props) {
    const { isAuthenticated, isUserFound, theme } = props;
    return (
        <Switch>
            <Redirect exact from='/' to={theme.custom.landingPage.active ? '/home' : '/apis'} />
            {theme.custom.landingPage.active && <Route path='/home' component={Landing} /> }
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
                            <Route path='/applications' component={PageNotFound} />
                            <Route path='/application/create' component={PageNotFound} />
                            <Route path='/application/edit/:application_id' component={PageNotFound} />
                        </React.Fragment>
                    ),
                ]
            )}
            <Route component={PageNotFound} />
        </Switch>
    );
}

export default withStyles({}, { withTheme: true })(AppRouts);
