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
import ComputerIcon from 'material-ui-icons/Computer';
import ChromeReaderModeIcon from 'material-ui-icons/ChromeReaderMode';
import Grid from 'material-ui/Grid';
import LibraryBooksIcon from 'material-ui-icons/LibraryBooks';
import ForumIcon from 'material-ui-icons/Forum';
import GavelIcon from 'material-ui-icons/Gavel';
import Typography from 'material-ui/Typography';
import Paper from 'material-ui/Paper';

export default class Details extends Component {
    constructor(props){
        super(props);
        this.state = {
            value: 'overview',
            api: null,
        };
        this.setDetailsAPI = this.setDetailsAPI.bind(this);
    }

    setDetailsAPI(api){
        this.setState({api: api});
    }

    handleChange = (event, value) => {
        this.setState({ value });
        this.props.history.push({pathname: "/apis/" + this.props.match.params.api_uuid + "/" + value});
    };

    render() {
        let redirect_url = "/apis/" + this.props.match.params.api_uuid + "/overview";
        const {api} = this.state;
        return (
            <Grid container spacing={0} justify="center">
                <Grid item xs={12} sm={6} md={8} lg={8} xl={10} >
                    <Typography type="title" gutterBottom className="page-title">
                        {api && api.name}  <span style={{fontSize: "50%"}}>{api && api.version} </span>
                    </Typography>
                    <Paper>
                        <Tabs
                            value={this.state.value}
                            onChange={this.handleChange}
                            fullWidth
                            indicatorColor="primary"
                            textColor="primary"
                        >
                            <Tab value="overview" icon={<ComputerIcon />} label="Overview" />
                            <Tab value="console" icon={<ChromeReaderModeIcon />} label="API Console" />
                            <Tab value="documentation" icon={<LibraryBooksIcon />} label="Documentation" />
                            <Tab value="forum" icon={<ForumIcon />} label="Forum" />
                            <Tab value="sdk" icon={<GavelIcon />} label="SDKs" />
                        </Tabs>
                    </Paper>
                    <Switch>
                        <Redirect exact from="/apis/:api_uuid" to={redirect_url}/>
                        <Route path="/apis/:api_uuid/overview" render={props => <Overview {...props} setDetailsAPI={this.setDetailsAPI}/>}/>
                        <Route path="/apis/:api_uuid/console" component={ApiConsole}/>
                        <Route path="/apis/:api_uuid/documentation" component={Documents}/>
                        <Route path="/apis/:api_uuid/forum" component={Forum}/>
                        <Route path="/apis/:api_uuid/sdk" component={Sdk}/>
                        <Route component={PageNotFound}/>
                    </Switch>
                </Grid>
            </Grid>
        );
    }
}
