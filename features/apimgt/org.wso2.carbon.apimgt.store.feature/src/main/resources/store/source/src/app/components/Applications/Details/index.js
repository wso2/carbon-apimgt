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
import BasicTabs from './NavTab'
import Overview from './Overview'
import ProductionKey from './ProductionKey'
import SandboxKey from './SandboxKey'
import Subscriptions from './Subscriptions'
import {Route, Switch, Redirect} from 'react-router-dom'
import API from '../../../data/api'
import {PageNotFound} from '../../Base/Errors/index'
import Grid from 'material-ui/Grid';
import Typography from 'material-ui/Typography';
import AppBar from 'material-ui/AppBar';
import Tabs, {Tab} from 'material-ui/Tabs';
import PhoneIcon from 'material-ui-icons/Phone';
import ComputerIcon from 'material-ui-icons/Computer';
import SubscriptionsIcon  from 'material-ui-icons/Subscriptions';
import KeysIcon  from 'material-ui-icons/VpnKey';
import FavoriteIcon from 'material-ui-icons/Favorite';
import BasicInfo from './BasicInfo'
import PersonPinIcon from 'material-ui-icons/PersonPin';
import Loading from '../../Base/Loading/Loading'
import ResourceNotFound from "../../Base/Errors/ResourceNotFound";
import Paper from 'material-ui/Paper';


export default class Details extends Component {

    constructor(props) {
        super(props);
        this.state = {
            application: null,
            value: 'overview',
        };
    }

    componentDidMount() {
        const client = new API();
        let promised_application = client.getApplication(this.props.match.params.application_uuid);
        promised_application.then(
            response => {
                this.setState({application: response.obj});
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
    }

    handleChange = (event, value) => {
        this.setState({value});
        this.props.history.push({pathname: "/applications/" + this.props.match.params.application_uuid + "/" + value});
    };

    render() {
        let redirect_url = "/applications/" + this.props.match.params.application_uuid + "/overview";

        if (this.state.notFound) {
            return <ResourceNotFound />
        } else if (!this.state.application) {
            return <Loading/>
        }

        return (
            <Grid container spacing={0} justify="center">
                <Grid item xs={12} sm={6} md={8} lg={8} xl={10} >
                    <Typography type="title" gutterBottom className="page-title">
                        {this.state.application.name}
                    </Typography>
                    <Paper>
                        <Tabs
                            value={this.state.value}
                            onChange={this.handleChange}
                            fullWidth
                            indicatorColor="primary"
                            textColor="primary"
                        >
                            <Tab value="overview" icon={<ComputerIcon />} label="Overview"/>
                            <Tab value="productionkeys" icon={<KeysIcon />} label="Production Keys"/>
                            <Tab value="sandBoxkeys" icon={<KeysIcon />} label="SandBox Keys"/>
                            <Tab value="subscriptions" icon={<SubscriptionsIcon />} label="Subscriptions"/>
                        </Tabs>
                    </Paper>    
                    <Switch>
                        <Redirect exact from="/applications/:applicationId" to={redirect_url}/>
                        <Route path="/applications/:applicationId/overview" component={Overview}/>
                        <Route path="/applications/:applicationId/productionkeys" component={ProductionKey}/>
                        <Route path="/applications/:applicationId/sandBoxkeys" component={SandboxKey}/>
                        <Route path="/applications/:applicationId/subscriptions" component={Subscriptions}/>
                        <Route component={PageNotFound}/>
                    </Switch>
                </Grid>
        </Grid>
        );
    }

}
