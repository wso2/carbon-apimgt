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
import ApiConsole from './ApiConsole'
import Documentation from './Documentation'
import Documents from './Documents/Documents'
import Forum from './Forum'
import Sdk from './Sdk'
import BasicInfo from './BasicInfo'
import {PageNotFound} from '../../Base/Errors/index'
import AppBar from 'material-ui/AppBar';
import Tabs, { Tab } from 'material-ui/Tabs';
import PhoneIcon from 'material-ui-icons/Phone';
import FavoriteIcon from 'material-ui-icons/Favorite';
import PersonPinIcon from 'material-ui-icons/PersonPin';

export default class Details extends Component {
    constructor(props){
        super(props);
        this.state = {
            value: 'overview',
        };
    }

    handleChange = (event, value) => {
        this.setState({ value });
        this.props.history.push({pathname: "/apis/" + this.props.match.params.api_uuid + "/" + value});
    };

    render() {
        let redirect_url = "/apis/" + this.props.match.params.api_uuid + "/overview";
        return (
            <div className="tab-content">
                <BasicInfo uuid={this.props.match.params.api_uuid} />
                <AppBar position="static" color="default" style={{margin:"10px 0px 10px 35px"}}>
                    <Tabs
                        value={this.state.value}
                        onChange={this.handleChange}
                        fullWidth
                        indicatorColor="accent"
                        textColor="accent"
                    >
                        <Tab value="overview" icon={<PhoneIcon />} label="Overview" />
                        <Tab value="console" icon={<FavoriteIcon />} label="API Console" />
                        <Tab value="documentation" icon={<PersonPinIcon />} label="Documentation" />
                        <Tab value="forum" icon={<PersonPinIcon />} label="Forum" />
                        <Tab value="sdk" icon={<PersonPinIcon />} label="SDKs" />
                    </Tabs>
                </AppBar>
                <Switch>
                    <Redirect exact from="/apis/:api_uuid" to={redirect_url}/>
                    <Route path="/apis/:api_uuid/overview" component={Overview}/>
                    <Route path="/apis/:api_uuid/console" component={ApiConsole}/>
                    <Route path="/apis/:api_uuid/documentation" component={Documents}/>
                    <Route path="/apis/:api_uuid/forum" component={Forum}/>
                    <Route path="/apis/:api_uuid/sdk" component={Sdk}/>
                    <Route component={PageNotFound}/>
                </Switch>
            </div>
        );
    }
}
