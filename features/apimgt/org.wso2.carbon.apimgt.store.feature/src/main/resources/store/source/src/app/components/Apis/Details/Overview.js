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
import {Col, Popconfirm, Row, Form, Select, Dropdown, Tag, Menu, Badge, message} from 'antd';

const FormItem = Form.Item;
import Loading from '../../Base/Loading/Loading'
import ResourceNotFound from "../../Base/Errors/ResourceNotFound";
import Api from '../../../data/api'

import Paper from 'material-ui/Paper';
import Grid from 'material-ui/Grid';
import Typography from 'material-ui/Typography';
import Button from 'material-ui/Button';
import Card, { CardActions, CardContent, CardMedia } from 'material-ui/Card';
import Table, { TableBody, TableCell, TableRow } from 'material-ui/Table';
import { Delete, Edit, CreateNewFolder, Description  }from 'material-ui-icons';
import Tabs, { Tab } from 'material-ui/Tabs';
import AppBar from 'material-ui/AppBar';
import AddIcon from 'material-ui-icons/';

class Overview extends Component {
    constructor(props) {
        super(props);
        this.state = {
            api: null,
            applications: null,
            policies: null,
            dropDownApplications: null,
            dropDownPolicies: null,
            notFound: false,
            tabValue: "Social Sites",
            comment: '',
            commentList:null
        };
        this.api_uuid = this.props.match.params.api_uuid;
        this.handleTabChange = this.handleTabChange.bind(this);
        this.updateCommentString = this.updateCommentString.bind(this);
        this.handleAddComment = this.handleAddComment.bind(this);
        this.handleGetAllComments = this.handleGetAllComments.bind(this);
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

                for (let i = 0; i < this.api.policies.length; i++) {
                    this.dropDownPolicies.push(<Option key={this.api.policies[i]}>{this.api.policies[i]}</Option>);
                }
                let subscription = {};
                let subscriptions = response.obj.list;
                let application = {};
                let subscribedApp = false;
                for (let i = 0; i < this.applications.length; i++) {
                    subscribedApp = false;
                    application = applications[i];
                    if (application.lifeCycleStatus != "APPROVED") {
                        continue;
                    }
                    for (let j = 0; j < subscriptions.length; j++) {
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

        this.handleGetAllComments();
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
    handleTabChange = (event, tabValue) => {
        this.setState({ tabValue : tabValue });
    };

    updateCommentString(event) {
        this.setState({comment : event.target.value});
    }

    handleAddComment() {
        var api = new Api();
        let commentInfo = {"commentText" : this.state.comment};
        let promise = api.addComment(this.api_uuid, commentInfo);
        promise.then(
            response => {
                this.handleGetAllComments();
                this.setState({comment : ''});
                message.success("Comment added successfully");
            }).catch (
                 error => {
                     message.error("Error occurred while adding comments!");
                 }
             );
    }

    handleGetAllComments() {
        var api = new Api();
        let promise_get = api.getAllComments(this.api_uuid);
        promise_get.then(
            response => {
                var index = 0;
                var comments = [];
                this.setState({commentList : response.obj.list});
            }).catch (
                 error => {
                     message.error("Error occurred while retrieving comments!");
                 }
            );
    }



    render() {
        const formItemLayout = {
            labelCol: {span: 6},
            wrapperCol: {span: 18}
        };
        if (this.state.notFound) {
            return <ResourceNotFound/>
        }

        if (this.state.redirect) {
            return <Redirect push to="/application-create" />;
        }
        const api = this.state.api;

        return (
            this.state.api ?
                <Grid container style={{paddingLeft:"40px"}}>

                    <Grid item xs={12} sm={6} md={9} lg={9} xl={10} >
                        <Paper style={{paddingLeft:"40px"}}>
                            <Typography type="headline" gutterBottom>
                                Production and Sandbox Endpoints
                            </Typography>
                            <Typography type="title" gutterBottom>
                                Production and Sandbox URLs:
                            </Typography>

                            { api.endpoint ?
                                api.endpoint.map( ep => <div>
                                    <span>{ep.type}</span>
                                    <span>{ep.inline ? ep.inline.endpointConfig.serviceUrl : ''}</span>
                                </div>)
                                : <span>....</span>
                            }

                            <Typography type="headline" gutterBottom>
                                Share
                            </Typography>
                            <AppBar position="static">
                                <Tabs
                                    value={this.state.tabValue}
                                    indicatorColor="primary"
                                    textColor="primary"
                                    onChange={this.handleTabChange}
                                >
                                    <Tab  value="Social Sites"  label="Social Sites" color="contrast" />
                                    <Tab value="Embed" label="Embed" color="contrast"  />
                                    <Tab value="Email" label="Email" color="contrast" />
                                </Tabs>
                            </AppBar>
                            {this.state.tabValue === 'Social Sites' && <div className="tab-container">

                                <div id="share_div_social" className="share_dives">
                                    {/* Facebook */}
                                    <a className="social_links" id="facebook"
                                       href="http://www.facebook.com/sharer.php?u=https%3A%2F%2F172.17.0.1%3A9444%2Fstore%2Fapis%2Finfo%3Fname%3Dfoo%26version%3D1.0.0%26provider%3Dadmin"
                                       target="_blank" title="facebook">
                                        <img src="/store/public/images/social/facebook.png" alt="Facebook" />
                                    </a>
                                    {/* Twitter */}
                                    <a className="social_links" id="twitter"
                                       href="http://twitter.com/share?url=https%3A%2F%2F172.17.0.1%3A9444%2Fstore%2Fapis%2Finfo%3Fname%3Dfoo%26version%3D1.0.0%26provider%3Dadmin&text=API%20Store%20-%20foo%20%3A%20try%20this%20API%20at%20https%3A%2F%2F172.17.0.1%3A9444%2Fstore%2Fapis%2Finfo%3Fname%3Dfoo%26version%3D1.0.0%26provider%3Dadmin"
                                       target="_blank" title="twitter">
                                        <img src="/store/public/images/social/twitter.png" alt="Twitter" /></a>
                                    {/* Google+ */}
                                    <a className="social_links" id="googleplus"
                                       href="https://plus.google.com/share?url=https%3A%2F%2F172.17.0.1%3A9444%2Fstore%2Fapis%2Finfo%3Fname%3Dfoo%26version%3D1.0.0%26provider%3Dadmin"
                                       target="_blank" title="googleplus">
                                        <img src="/store/public/images/social/google.png" alt="Google" /></a>
                                    {/* Digg */}
                                    <a className="social_links" id="digg"
                                       href="http://www.digg.com/submit?url=https%3A%2F%2F172.17.0.1%3A9444%2Fstore%2Fapis%2Finfo%3Fname%3Dfoo%26version%3D1.0.0%26provider%3Dadmin"
                                       target="_blank" title="digg">
                                        <img src="/store/public/images/social/diggit.png" alt="Digg" /></a>
                                    <div className="clearfix">
                                    </div>
                                </div>
                            </div>}
                            {this.state.tabValue === 'Embed' && <div>{'Item Two'}</div>}
                            {this.state.tabValue === 'Email' && <div>{'Item Three'}</div>}

                            <div>
                                <p>Comments</p>
                                <Row>
                                    <textarea cols="180" rows="4" value={this.state.comment} onChange={this.updateCommentString}> </textarea>
                                </Row>
                                <Row>
                                    <Button onClick={this.handleAddComment}>Add</Button>
                                </Row>
                            </div>
                            <div>
                                {this.state.commentList ? this.state.commentList.map((comment) => {
                                    return <div><Row>
                                            <Card bodyStyle={{padding: 5}} style={{background : "#e0d9d8"}}><p>{comment.commentText}</p></Card>
                                           </Row>

                                           <Row>
                                           <p>Posted By {comment.createdBy} at {comment.createdTime}</p>
                                          </Row><br /></div>
                                    }) : <br></br>
                                }
                            </div>
                        </Paper>

                    </Grid>
                </Grid>
                : <Loading/>
        );
    }
}

export default Overview
