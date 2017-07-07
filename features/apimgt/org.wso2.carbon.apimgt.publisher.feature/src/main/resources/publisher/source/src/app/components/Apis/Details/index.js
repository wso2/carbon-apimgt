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
import {PageNotFound} from '../../Base/Errors/index'
import Loading from '../../Base/Loading/Loading'
import Resources from './Resources'
import Permission from './Permission'
import Endpoint from './LifeCycle/Endpoint'

import Api from '../../../data/api'
import ResourceNotFound from "../../Base/Errors/ResourceNotFound";


export default class Details extends Component {
    constructor(props) {
        super(props);
        this.api_uuid = props.match.params.api_uuid;
        this.state = {
            api_response: null,
            notFound: false
        }
    }

    componentDidMount() {
        const api = new Api();
        let promised_api = api.get(this.api_uuid);
        promised_api.then(
            response => {
                this.bindApi(response);
            }
        ).catch(
            error => {
                if (process.env.NODE_ENV !== "production") {
                    console.log(error);
                }
                let status = error.status;
                if (status === 404) {
                    this.setState({notFound: true});
                }
            }
        );
        this.props.setLeftMenu(true);
    }

    componentWillUnmount() {
        /* Hide the left side nav bar when detail page is unmount ,
        since the left nav bar is currently only associated with details page*/
        this.props.setLeftMenu(false);
    }

    bindApi(api_response) {
        this.setState({api_response: api_response});
    }

    render() {
        let redirect_url = "/apis/" + this.props.match.params.api_uuid + "/" + NavBar.CONST.OVERVIEW;
        if (this.state.notFound) {
            return <ResourceNotFound/>
        }
        if (this.state.api_response) {
            return (
                <div className="tab-content">
                    <Switch>
                        <Redirect exact from="/apis/:api_uuid"
                                  to={redirect_url}/>
                        <Route path="/apis/:api_uuid/overview"
                               render={ props => <Overview api={this.state.api_response.obj} {...this.props} /> }/>
                        <Route path="/apis/:api_uuid/lifecycle"
                               render={ props => <LifeCycle api={this.state.api_response.obj} {...this.props} /> }/>
                        <Route path="/apis/:api_uuid/resources"
                               render={ props => <Resources api={this.state.api_response.obj} {...this.props} /> }/>
                        <Route path="/apis/:api_uuid/endpoints"
                               render={ props => <Endpoint api={this.state.api_response.obj} {...this.props} /> }/>
                        <Route path="/apis/:api_uuid/permission"
                               render={ props => <Permission api={this.state.api_response.obj} {...props} /> }/>/>
                        <Route component={PageNotFound}/>
                    </Switch>
                </div>
            );
        } else {
            return (
                <Loading/>
            );
        }
    }
}
