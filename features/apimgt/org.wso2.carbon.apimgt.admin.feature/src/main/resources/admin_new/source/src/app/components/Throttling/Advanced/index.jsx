import React from 'react';
import { withRouter, Switch, Route } from 'react-router-dom';
import List from 'AppComponents/Throttling/Advanced/List';
import AddEdit from 'AppComponents/Throttling/Advanced/AddEdit';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';

/**
 * Render a list
 * @returns {JSX} Header AppBar components.
 */
function AdvancedThrottlePolicies() {
    return (
        <Switch>
            <Route exact path='/throttling/advanced' component={List} />
            <Route path='/throttling/advanced/:id' component={AddEdit} />
            <Route path='/throttling/advanced/create' component={AddEdit} />
            <Route component={ResourceNotFound} />
        </Switch>
    );
}

export default withRouter(AdvancedThrottlePolicies);
