/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React from 'react';
import { withRouter, Switch, Route } from 'react-router-dom';
import SubscriptionThrottlingPolicies from 'AppComponents/Throttling/Subscription/List';
import AddEdit from 'AppComponents/Throttling/Subscription/AddEdit';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';

/**
 * Render a list
 * @returns {JSX} Header AppBar components.
 */
function Tasks() {
    return (
        <Switch>
            <Route exact path='/throttling/subscription' component={SubscriptionThrottlingPolicies} />
            <Route exact path='/throttling/subscription/add' component={AddEdit} />
            <Route path='/throttling/subscription/:id' component={AddEdit} />
            <Route component={ResourceNotFound} />
        </Switch>
    );
}

export default withRouter(Tasks);
