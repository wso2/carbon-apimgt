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
import Api from '../../../data/api'

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
            api: null,
            applications: null,
            policies: null,
            dropDownApplications: null,
            dropDownPolicies: null,
            notFound: false,
            matDropVisible: false,
            matDropValue: 'one'
        };
        this.api_uuid = this.props.uuid;
        this.logChange = this.logChange.bind(this);
    }

    componentDidMount() {
        const api = new Api();
        let promised_api = api.getAPIById(this.api_uuid);
        promised_api.then(
            response => {
                console.info(response.obj)
                this.setState({api: response.obj});
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

        let promised_applications = api.getAllApplications();
        promised_applications.then(
            response => {
                this.setState({applications: response.obj.list});
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

        let promised_subscriptions = api.getSubscriptions(this.api_uuid, null);
        promised_subscriptions.then(
            response => {
                this.dropDownApplications = [<Option key="custom" onClick={this.handleClick} >New Application</Option>];

                for (var i = 0; i < this.api.policies.length; i++) {
                    this.dropDownPolicies.push(<Option key={this.api.policies[i]}>{this.api.policies[i]}</Option>);
                }
                var subscription = {};
                var subscriptions = response.obj.list;
                var application = {};
                var subscribedApp = false;
                for (var i = 0; i < this.applications.length; i++) {
                    subscribedApp = false;
                    application = applications[i];
                    if (application.lifeCycleStatus != "APPROVED") {
                        continue;
                    }
                    for (var j = 0; j < subscriptions.length; j++) {
                        subscription = subscriptions[j];
                        if (subscription.applicationId === application.applicationId) {
                            subscribedApp = true;
                            continue;
                        }
                    }
                    if(!subscribedApp) {
                        this.dropDownApplications.push(<Option key={application.id}>{application.name}</Option>);
                    }
                }
                this.policies = this.api.policies;
                console.info(this.api.policies)
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

    populateApplicationDropdown(){
        return this.dropDownApplications;
    }

    populatePolicyDropdown(){
        return this.dropDownPolicies;
    }

    handleClick(){
        this.setState({redirect: true});
    }

    selectChange(){
        this.setState({matDropVisible:!this.state.matDropVisible})
    }
    onBlur(e){
        console.info(document.activeElement);
        if ( !e.currentTarget.contains( document.activeElement ) ){
            this.setState({matDropVisible: false});
        }
    }
    selectOption(option){
        console.info(option);
        this.setState({selectOption: option});
    }
    logChange(val) {
        this.setState({matDropValue: val.value});
        console.log("Selected: " + JSON.stringify(val));
    }
    render() {
        const formItemLayout = {
            labelCol: {span: 6},
            wrapperCol: {span: 18}
        };
        if (this.state.notFound) {
            return <ResourceNotFound />
        }

        if (this.state.redirect) {
            return <Redirect push to="/application-create" />;
        }

        const api = this.state.api;
        const dropDownOptions = ["option1","option2","option3"];
        let options = [
            { value: 'one', label: 'One' },
            { value: 'two', label: 'Two' }
        ];



        return (
            this.state.api ?
                <Grid container>
                    <Grid item xs={12}>
                        <Paper style={{display:"flex"}}>
                            <Typography type="display2" gutterBottom className="page-title">
                                {api.name} - <span style={{fontSize:"50%"}}>Overview</span>
                            </Typography>
                            {/*<Button aria-owns="simple-menu" aria-haspopup="true" >
                                <Edit /> Edit
                            </Button>
                            <Button aria-owns="simple-menu" aria-haspopup="true" >
                                <CreateNewFolder /> Create New Version
                            </Button>
                            <Button aria-owns="simple-menu" aria-haspopup="true" >
                                <Description /> View Swagger
                            </Button>*/}
                        </Paper>
                    </Grid>
                    <Grid item xs={12} sm={12} md={3} lg={3} xl={2} style={{paddingLeft:"40px"}}>

                        <Card>
                            <CardMedia
                                image="/store/public/images/api/api-default.png"
                                title="Contemplative Reptile"
                            >
                                <img alt="API thumb" width="100%" src="/store/public/images/api/api-default.png"/>
                            </CardMedia>
                            <CardContent>
                                <div className="custom-card">
                                    <Badge status="processing" text={api.lifeCycleStatus}/>
                                    <p>11 Apps</p>
                                    <a href={"/store/apis/" + this.api_uuid} target="_blank" title="Store">View in store</a>
                                </div>
                            </CardContent>
                            <CardActions>
                                {api.lifeCycleStatus}

                                <Button dense color="primary">
                                    <a href={"/store/apis/" + this.api_uuid} target="_blank" title="Store">View in store</a>
                                </Button>
                            </CardActions>
                        </Card>
                    </Grid>
                    <Grid item xs={12} sm={6} md={3} lg={3} xl={2} style={{paddingLeft:"40px"}}>
                        <Table>
                            <TableBody>



                                <TableRow>
                                    <TableCell>Version</TableCell><TableCell>{api.version}</TableCell>
                                </TableRow>

                                <TableRow>
                                    <TableCell>Context</TableCell><TableCell>{api.context}</TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell>Context</TableCell><TableCell>{api.provider}</TableCell>
                                </TableRow>

                                <TableRow>
                                    <TableCell>Date Created</TableCell><TableCell>{api.createdTime}</TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell>Date Last Updated</TableCell><TableCell>{api.lastUpdatedTime}</TableCell>
                                </TableRow>

                                <TableRow>
                                    <TableCell>Default API Version</TableCell><TableCell>{api.isDefaultVersion}</TableCell>
                                </TableRow>
                                 <TableRow>
                                    <TableCell>Default API Version</TableCell><TableCell>{api.lifeCycleStatus}</TableCell>
                                </TableRow>


                            </TableBody>
                        </Table>
                    </Grid>
                    <Grid item xs={12} sm={6} md={6} lg={6} xl={8} style={{paddingLeft:"40px"}}>
                        <Typography type="subheading" gutterBottom>
                            Applications
                        </Typography>
                        <Select
                            name="form-field-name"
                            value={this.state.matDropValue}
                            options={options}
                            onChange={this.logChange}
                        />
                        <Typography type="subheading" gutterBottom>
                            Tiers
                        </Typography>
                        <Select
                            name="form-field-name"
                            value={this.state.matDropValue}
                            options={options}
                            onChange={this.logChange}
                        />
                        <br />
                        <Button raised color="primary" style={{paddingTop: '20px;'}}>
                            <Subscriptions style={{paddingRight: '10px;'}} /> Subscribe
                        </Button>

                            {/*<Select>
                                {this.populateApplicationDropdown()}
                            </Select>
                            <Select>
                                {this.populatePolicyDropdown()}
                            </Select>*/}

                    </Grid>
                </Grid>
                : <Loading/>
        );
    }
}

export default BasicInfo
