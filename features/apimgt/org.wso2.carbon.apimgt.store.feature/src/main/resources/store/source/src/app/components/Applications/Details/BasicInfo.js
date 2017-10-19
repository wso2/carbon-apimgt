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
import {Link} from 'react-router-dom'
import {Col, Popconfirm, Row, Form, Dropdown, Tag, Menu, Badge, message} from 'antd';

const FormItem = Form.Item;
import Loading from '../../Base/Loading/Loading'
import ResourceNotFound from "../../Base/Errors/ResourceNotFound";
import API from '../../../data/api'

import Paper from 'material-ui/Paper';
import Grid from 'material-ui/Grid';
import Typography from 'material-ui/Typography';
import Button from 'material-ui/Button';
import Card, { CardActions, CardContent, CardMedia } from 'material-ui/Card';
import { Delete, Edit, CreateNewFolder, Description  }from 'material-ui-icons';
import Table, { TableBody, TableCell, TableRow } from 'material-ui/Table';
import Select from 'react-select';
import 'react-select/dist/react-select.css';
import Subscriptions  from 'material-ui-icons/Subscriptions';

class BasicInfo extends Component {
    constructor(props) {
        super(props);
        this.state = {
            application: null,
        };
        this.application_uuid = this.props.uuid;
    }

    componentDidMount() {
	const client = new API();
        let promised_application = client.getApplication(this.application_uuid);
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
        const formItemLayout = {
            labelCol: {span: 6},
            wrapperCol: {span: 18}
        };
        if (this.state.notFound) {
            return <ResourceNotFound />
        }
        return (
            this.state.application ?
                <Grid container>
                    <Grid item xs={12}>
                        <Paper style={{display:"flex"}}>
                            <Typography type="display2" gutterBottom className="page-title">
                                {this.state.application.name} - <span style={{fontSize:"50%"}}>Application Overview</span>
                            </Typography>
                        </Paper>
                    </Grid>
                    <Grid item xs={12} sm={6} md={3} lg={3} xl={2} style={{paddingLeft:"40px"}}>
                        <Table>
                            <TableBody>
				 <TableRow>
                                        <TableCell style={{width:"100px"}}>Application Name</TableCell><TableCell>{this.state.application.name}</TableCell>
                                    </TableRow>
                                    <TableRow>
                                        <TableCell>Throttling Tier</TableCell><TableCell>{this.state.application.throttlingTier}</TableCell>
                                    </TableRow>
                                    <TableRow>
                                        <TableCell>Life Cycle State</TableCell><TableCell>{this.state.application.lifeCycleStatus}</TableCell>
                                    </TableRow>
				    <TableRow>
                                        <TableCell>Application Description</TableCell><TableCell>{this.state.application.description}</TableCell>
                                    </TableRow>
                                   
                            </TableBody>
                        </Table>
                    </Grid>
                </Grid>
                : <Loading/>
        );
    }
}

export default BasicInfo
