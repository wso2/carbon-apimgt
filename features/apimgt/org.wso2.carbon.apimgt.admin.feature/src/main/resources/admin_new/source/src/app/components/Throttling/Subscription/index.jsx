import React from 'react';
import { withRouter, Switch, Route } from 'react-router-dom';
import SubscriptionThrottlingPolicies from 'AppComponents/Throttling/Subscription/List';
import AddEdit from 'AppComponents/Throttling/Subscription/AddEdit';


/**
 * Render a list
 * @returns {JSX} Header AppBar components.
 */
function Tasks() {
    return (
        <Switch>
            <Route exact path='/throttling/subscription' component={SubscriptionThrottlingPolicies} />
            <Route exact path='/throttling/subscription/add' component={AddEdit} />
        </Switch>
    );
}

export default withRouter(Tasks);
