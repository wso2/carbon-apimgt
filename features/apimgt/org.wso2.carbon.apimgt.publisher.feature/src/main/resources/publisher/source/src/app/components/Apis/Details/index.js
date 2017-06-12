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

import React, {Component} from 'react'
import {Route, Switch, Redirect} from 'react-router-dom'

import Overview from './Overview'
import NavBar from './NavBar'
import LifeCycle from './LifeCycle/LifeCycle'
import Documents from './Documents/Documents'
import {PageNotFound} from '../../Base/Errors/index'
import Resources from './Resources/Resources'
import PermissionFormWrapper from './Permission'
import Endpoints from './Endpoints'
import Subscriptions from './Subscriptions/Subscriptions'

export default class Details extends Component {
    componentDidMount() {
        this.props.setLeftMenu(true);
    }

    componentWillUnmount() {
        /* Hide the left side nav bar when detail page is unmount ,
         since the left nav bar is currently only associated with details page*/
        this.props.setLeftMenu(false);
    }

    render() {
        let redirect_url = "/apis/" + this.props.match.params.api_uuid + "/overview";
        return (
            <div className="tab-content">
                <Switch>
                    <Redirect exact from="/apis/:api_uuid" to={redirect_url}/>
                    <Route path="/apis/:api_uuid/overview" component={Overview}/>
                    <Route path="/apis/:api_uuid/lifecycle" component={LifeCycle}/>
                    <Route path="/apis/:api_uuid/resources" component={Resources}/>
                    <Route path="/apis/:api_uuid/permission" component={PermissionFormWrapper}/>
                    <Route path="/apis/:api_uuid/documents" component={Documents}/>
                    <Route path="/apis/:api_uuid/endpoints" component={Endpoints}/>
                    <Route path="/apis/:api_uuid/subscriptions" component={Subscriptions}/>
                    <Route component={PageNotFound}/>
                </Switch>
            </div>
        );
    }
}
