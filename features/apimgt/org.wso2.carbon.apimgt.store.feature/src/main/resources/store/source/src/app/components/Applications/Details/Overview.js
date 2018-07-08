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
import API from '../../../data/api.js'
import {PageNotFound} from '../../Base/Errors/index'
import AppBar from 'material-ui/AppBar';
import Tabs, { Tab } from 'material-ui/Tabs'
import PhoneIcon from 'material-ui-icons/Phone'
import FavoriteIcon from 'material-ui-icons/Favorite'
import PersonPinIcon from 'material-ui-icons/PersonPin'
import Loading from '../../Base/Loading/Loading'
import Grid from 'material-ui/Grid'
import Card, { CardActions, CardContent, CardMedia } from 'material-ui/Card'
import Typography from 'material-ui/Typography'
import Paper from 'material-ui/Paper'
import Table, { TableBody, TableCell, TableHead, TableRow } from 'material-ui/Table'
import ResourceNotFound from "../../Base/Errors/ResourceNotFound";

class Overview extends Component {
    constructor(props) {
        super(props);
        this.state = {
            application: null,
            notFound: false,
            tierDescription: null
        };
    }

    componentDidMount() {
        const client = new API();
        // Get application
        let promised_application = client.getApplication(this.props.match.params.applicationId);
        promised_application.then(
            response => {
                let promised_tier = client.getTierByName(response.obj.throttlingTier,"application");
                return Promise.all([response, promised_tier]);
            }).then(
                response => {
                    let [application, tier] = response.map(data => data.obj);
                    this.setState({ application, tierDescription: tier.description });
                }).catch(
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

    render() {
        let redirect_url = "/applications/" + this.props.match.params.application_uuid + "/overview";
        const { application, tierDescription, notFound } = this.state;
        if (notFound) {
            return <ResourceNotFound/>
        }
        if (!application) {
            return <Loading/>
        }
        return (
            <Paper>
                <Grid container className="tab-grid" spacing={0} >
                    <Grid>
                        <Table>
                            <TableBody>
                                <TableRow>
                                    <TableCell>Throttling Tier</TableCell><TableCell>{application.throttlingTier}   {tierDescription}</TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell>Life Cycle State</TableCell><TableCell>{application.lifeCycleStatus}</TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell>Application Description</TableCell><TableCell>{application.description}</TableCell>
                                </TableRow>
                            </TableBody>
                        </Table>
                    </Grid>
                </Grid>
            </Paper>
        );
    }
}
export default Overview
