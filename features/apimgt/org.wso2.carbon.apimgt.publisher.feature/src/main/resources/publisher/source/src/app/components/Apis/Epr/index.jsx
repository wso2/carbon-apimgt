import React from 'react';
import {
    withRouter, Switch, Route, Redirect,
} from 'react-router-dom';
import EprList from 'AppComponents/Apis/Epr/EprList';
import Dashboard from 'AppComponents/Apis/Epr/Dashboard';
import EprCreate from 'AppComponents/Apis/Epr/EprCreate';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';

/**
 * Routes the endpoint registry sub components.
 * @returns {JSX} Switch component.
 */
function Epr() {
    return (

        <Switch>
            <Redirect exact from='/endpoint-registry/' to='/endpoint-registry/dashboard' />
            <Route exact path='/endpoint-registry/dashboard' component={Dashboard} />
            <Route exact path='/endpoint-registry/list' component={EprList} />
            <Route exact path='/endpoint-registry/create' component={EprCreate} />
            <Route component={ResourceNotFound} />
        </Switch>
    );
}

export default withRouter(Epr);
