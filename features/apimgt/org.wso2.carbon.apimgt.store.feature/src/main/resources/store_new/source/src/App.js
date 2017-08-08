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

import {BrowserRouter as Router, Route, Switch, Redirect} from 'react-router-dom'
import {Apis, Base, Login, Logout, Endpoints,Layout} from './app/components'
import {PageNotFound} from './app/components/Base/Errors'
import AuthManager from './app/data/AuthManager'
import qs from 'qs'
import Listing2 from './app/components/Listing2'

import './fonts/index.css';
import './App.css'


/**
 * Render protected application paths
 */
class Protected extends Component {
    constructor(props) {
        super(props);
    }



    render() {
        // Note: AuthManager.getUser() method is a passive check, which simply check the user availability in browser storage,
        // Not actively check validity of access token from backend
        if (AuthManager.getUser()) {
            return (
                <Base>
                    <Switch>
                        <Route component={PageNotFound}/>
                    </Switch>
                </Base>
            );
        }
        let params = qs.stringify({referrer: this.props.location.pathname});
        return (
            <Redirect to={{pathname: '/login', search: params}}/>
        );
    }

}

/**
 * Define base routes for the application
 */
class Publisher extends Component {
    constructor(props){
        super(props);
        this.authManager = new AuthManager();
        this.state = {showLeftMenu: false};
        this.setLeftMenu = this.setLeftMenu.bind(this);
    }
    /**
     * Change the visibility state of left side navigation menu bar
     * @param {boolean} status : Whether or not to show or hide left side navigation menu
     */
    componentWillMount(){
        console.info("component did mount ... ");
        let loginPromise = this.authManager.authenticateUser("admin", "admin");
        loginPromise.then((response) => {
            console.info(response);
        }).catch((error) => {
                console.log(error);
            }
        );
    }
    setLeftMenu(status) {
        this.setState({
            showLeftMenu: status
        });
    }

    render() {
        return (
            <Router basename="/store_new">
                <Layout>
                    <Switch>
                        <Redirect exact from="/" to="/apis"/>
                        <Route path={"/login"} component={Login}/>
                        <Route path={"/logout"} component={Logout}/>
                        <Route path={"/apis"} showLeftMenu={this.state.showLeftMenu} render={ props => (<Apis setLeftMenu={this.setLeftMenu}/>)}/>
                        <Route path={"/apis2"} component={Listing2}/>
                        <Route component={Protected}/>
                    </Switch>
                </Layout>
            </Router>
        );
    }
}

export default Publisher;
