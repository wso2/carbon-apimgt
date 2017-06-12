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
import LifeCycle from './LifeCycle'
import {PageNotFound} from '../../Base/Errors/index'
import Loading from '../../Base/Loading/Loading'

import Api from '../../../data/api'
import AuthCheck from "../../Base/Auth/AuthCheck";

export default class Details extends Component {
    constructor(props) {
        super(props);
        this.api_uuid = props.match.params.api_uuid;
        this.state = {
            response: null,
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
                if (process.env.NODE_ENV !== "production")
                    console.log(error);
                this.bindApi(error);
            }
        );
    }

    bindApi(api_response) {
        this.setState({response: api_response});
    }

    render() {
        let redirect_url = "/apis/" + this.props.match.params.api_uuid + "/overview";
        if (this.state.response) {
            return (
                <AuthCheck response={this.state.response} {...this.props}>
                    <div className="tab-content">
                    <NavBar {...this.props}/>
                        <Switch>
                            <Redirect exact from="/apis/:api_uuid"
                                      to={redirect_url}/>
                            <Route path="/apis/:api_uuid/overview"
                                   render={ props => <Overview api={this.state.response.obj} {...props} /> }/>
                            <Route path="/apis/:api_uuid/lifecycle" component={LifeCycle}/>
                            <Route component={PageNotFound}/>
                        </Switch>
                    </div>
                </AuthCheck>
            );
        } else {
            return (
                <Loading/>
            );
        }
    }
}
