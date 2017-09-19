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

class Overview extends Component {
    constructor(props) {
        super(props);
        this.state = {
            application: null,
            notFound: false
        };
    }

    componentDidMount() {
 	const client = new API();
        let promised_application = client.getApplication(this.props.match.params.applicationId);
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

  

    render() {
        let redirect_url = "/applications/" + this.props.match.params.application_uuid + "/overview";
        const application = this.state.application;
        if (this.state.notFound) {
            return <ResourceNotFound/>
        }
        if (!this.state.application) {
            return <Loading/>
        }
        return (
                <Grid container style={{paddingLeft:"40px"}}>
                    <Grid item xs={12} sm={6} md={9} lg={9} xl={10} >
                        <Paper>
                            <Table>
                                <TableBody>

                                    <TableRow>
                                        <TableCell style={{width:"100px"}}>Application Name</TableCell><TableCell>{application.name}</TableCell>
                                    </TableRow>
                                    <TableRow>
                                        <TableCell>Throttling Tier</TableCell><TableCell>{application.throttlingTier}</TableCell>
                                    </TableRow>
                                    <TableRow>
                                        <TableCell>Life Cycle State</TableCell><TableCell>{application.lifeCycleStatus}</TableCell>
                                    </TableRow>
				    <TableRow>
                                        <TableCell>Application Description</TableCell><TableCell>{application.description}</TableCell>
                                    </TableRow>
                                   
                                </TableBody>
                            </Table>
                        </Paper>

                    </Grid>
                </Grid>
        );
    }
}
export default Overview
