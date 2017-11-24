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
import { MenuItem } from 'material-ui/Menu';
import { Delete, Edit, CreateNewFolder, Description  }from 'material-ui-icons';
import Table, { TableBody, TableCell, TableRow } from 'material-ui/Table';
import Select from 'material-ui/Select';
import 'react-select/dist/react-select.css';
import Subscriptions  from 'material-ui-icons/Subscriptions';
import { FormControl } from 'material-ui/Form';

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
            matDropValue: 'one',
            subscribedApplicationIds: [],
            options: [],
            tiers: [],
            applicationId: null,
            policyName: null
        };
        this.api_uuid = this.props.uuid;
        this.logChange = this.logChange.bind(this);
    }

    componentDidMount() {
        const api = new Api();
        let promised_api = api.getAPIById(this.api_uuid);
        promised_api.then(
            response => {
                console.info(response.obj);
                this.setState({api: response.obj});
                let apiTiers = response.obj.policies;
                let tiers = [];
                for (let i = 0; i < apiTiers.length; i++) {
                    let tierName = apiTiers[i];
                    tiers.push({value: tierName, label: tierName});
                }
                this.setState({tiers: tiers});
                if (tiers.length > 0) {
                    this.setState({policyName: tiers[0].value});
                }
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

        let existing_subscriptions = api.getSubscriptions(this.api_uuid, null);
        existing_subscriptions.then((response) => {
            let subscribedApplications = [];
            //get the application IDs of existing subscriptions
            response.body.list.map(element => subscribedApplications.push(element.applicationId));
            this.setState({subscribedApplicationIds: subscribedApplications});
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

        let promised_applications = api.getAllApplications();
        promised_applications.then(
            (response) => {
                let applicationResponseObj = response.body;
                let applications = [];
                for (let i = 0; i < applicationResponseObj.list.length; i++) {
                    let applicationId = applicationResponseObj.list[i].applicationId;
                    let applicationName = applicationResponseObj.list[i].name;
                    //include the application only if it does not has an existing subscriptions
                    if (this.state.subscribedApplicationIds.includes(applicationId)) {
                        continue;
                    } else {
                        applications.push({value: applicationId, label: applicationName});
                    }
                }
                this.setState({options: applications});
                if (options.length > 0) {
                    this.setState({applicationId: options[0].value});
                }
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

    handleChange = name => event => {
        this.setState({ [name]: event.target.value });
    };

    createSubscription = (e) => {
        e.preventDefault();
        let apiId = this.api_uuid;
        let applicationId = this.state.applicationId;
        let policy = this.state.policyName;
        let api = new Api();
        let promised_subscribe = api.subscribe(apiId, applicationId, policy);
        promised_subscribe.then(response => {
            console.log("Subscription created successfully with ID : " + response.body.subscriptionId);
        }).catch(
            function (error_response) {
                console.log("Error while creating the subscription.");
            }
        );
    };

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
                            <CardMedia image="/store/public/images/api/api-default.png" >
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
                                <TableRow>
                                    <TableCell>Rating</TableCell>
                                    <TableCell>
                                        <StarRatingBar apiIdProp = {this.api_uuid}></StarRatingBar>
                                    </TableCell>
                                </TableRow>


                            </TableBody>
                        </Table>
                    </Grid>
                    <Grid item xs={12} sm={6} md={6} lg={6} xl={8} style={{paddingLeft:"40px"}}>
                        <Typography type="subheading" gutterBottom>
                            Applications
                        </Typography>
                        {this.state.options &&
                        <FormControl style={{width:"100%",marginBottom:"20px"}}>
                            <Select
                                style={{width:"100%"}}
                                value={this.state.applicationId}
                                onChange={this.handleChange('applicationId')}
                            >
                                {this.state.options.map((option) => <MenuItem key={option.value} value={option.value}>{option.label}</MenuItem>)}
                            </Select>
                        </FormControl>
                        }
                        <Typography type="subheading" gutterBottom>
                            Tiers
                        </Typography>
                        {this.state.tiers &&
                        <FormControl style={{width:"100%"}}>
                            <Select
                                style={{width:"100%"}}
                                value={this.state.policyName}
                                onChange={this.handleChange('policyName')}
                            >
                                {this.state.tiers.map((tier) => <MenuItem key={tier.value} value={tier.value}>{tier.label}</MenuItem>)}
                            </Select>
                        </FormControl>
                        }
                        <br />
                        <Button onClick={this.createSubscription} raised color="primary" style={{paddingTop: '20px'}}>
                            <Subscriptions style={{paddingRight: '10px'}} /> Subscribe
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

class Star extends React.Component {
    constructor(props) {
        super(props);

        this.handleHoveringOver = this.handleHoveringOver.bind(this);
    }

    handleHoveringOver(event) {
        this.props.hoverOver(this.props.name);
    }

    render() {
        return this.props.isRated ?
            <span onMouseOver = {this.handleHoveringOver} style={{color: 'gold'}}>
                ★
            </span> :
            <span onMouseOver = {this.handleHoveringOver} style={{color: 'gold'}}>
                ☆
            </span>;
    }
}

class StarRatingBar extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
                        previousRating : 0,
                        rating : 0
                     };

        this.handleMouseOver = this.handleMouseOver.bind(this);
        this.handleRatingUpdate = this.handleRatingUpdate.bind(this);
        this.handleMouseOut = this.handleMouseOut.bind(this);
    }

    componentDidMount() {
        var api = new Api();
        let promised_api = api.getAPIById(this.props.apiIdProp);
        promised_api.then(
            response => {
            }
        );

        //get user rating
        let promised_rating = api.getRatingFromUser(this.props.apiIdProp, null);
        promised_rating.then(
            response => {
                this.setState({rating :response.obj.userRating});
                this.setState({previousRating :response.obj.userRating});
            }
        );
    }

    handleMouseOver(index) {
        this.setState({rating : index});
    }

    handleMouseOut() {
        this.setState({rating : this.state.previousRating});
    }

    handleRatingUpdate() {
        this.setState({previousRating : this.state.rating});
        this.setState({rating : this.state.rating});

        var api = new Api();
        let ratingInfo = {"rating" : this.state.rating};
        let promise = api.addRating(this.props.apiIdProp, ratingInfo);
        promise.then(
            response => {
                message.success("Rating updated successfully");
            }).catch (
                 error => {
                     message.error("Error occurred while adding ratings!");
                 }
             );
    }

    render() {
        return (<div onClick = {this.handleRatingUpdate} onMouseOut = {this.handleMouseOut}>
                <Star name = {1} isRated = {this.state.rating >= 1} hoverOver = {this.handleMouseOver} > </Star>
                <Star name = {2} isRated = {this.state.rating >= 2} hoverOver = {this.handleMouseOver} > </Star>
                <Star name = {3} isRated = {this.state.rating >= 3} hoverOver = {this.handleMouseOver} > </Star>
                <Star name = {4} isRated = {this.state.rating >= 4} hoverOver = {this.handleMouseOver} > </Star>
                <Star name = {5} isRated = {this.state.rating >= 5} hoverOver = {this.handleMouseOver} > </Star>
               </div>);
    }
}


export default BasicInfo
