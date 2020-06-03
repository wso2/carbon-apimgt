import React from 'react';
import { withRouter, Switch, Route } from 'react-router-dom';
import List from 'AppComponents/Throttling/Custom/List';
import AddEdit from 'AppComponents/Throttling/Custom/AddEdit';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';

/**
 * Render a list
 * @returns {JSX} Header AppBar components.
 */
function CustomThrottlePolicies() {
    return (
        <Switch>
            <Route exact path='/throttling/custom' component={List} />
            <Route path='/throttling/custom/:policyId' component={AddEdit} />
            <Route path='/throttling/custom/create' component={AddEdit} />
            <Route component={ResourceNotFound} />
        </Switch>
    );
}

export default withRouter(CustomThrottlePolicies);
