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

import React, { Component } from 'react';
import { Link } from 'react-router-dom';
import API from '../../../data/api'

import Button from 'material-ui/Button';
import { MenuItem } from 'material-ui/Menu';
import {Form} from 'material-ui/Form'
import Grid from 'material-ui/Grid';
import Typography from 'material-ui/Typography';
import { Delete, Edit, CreateNewFolder, Description  }from 'material-ui-icons';
import TextField from 'material-ui/TextField';

import Paper from 'material-ui/Paper';
import Input from 'material-ui/Input';
import Select from 'material-ui/Select';
import { FormControl } from 'material-ui/Form';

class ApplicationCreate extends Component {

    constructor(props) {
        super(props);
        this.state = {
            quota: "Unlimited",
            tiers: [],
            throttlingTier: null,
            description: null,
            name: null,
            callbackUrl: null
        };
        this.handleChange = this.handleChange.bind(this);
    }

    componentDidMount() {
        //Get all the tires to populate the drop down.
        const api = new API();
        let promised_tiers = api.getAllTiers("application");
        promised_tiers.then((response) => {
            let tierResponseObj = response.body;
            let tiers = [];
            tierResponseObj.list.map(item => tiers.push(item.name));
            this.setState({tiers: tiers});

            if (tiers.length > 0){
                console.info(tiers[0]);
                this.setState({quota: tiers[0]});
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
    }

    handleChange = name => event => {
        this.setState({ [name]: event.target.value });
    };

    handlePolicyChange = name => event => {
        this.setState({ [name]: event.target.value });
    };

    handleSubmit = (e) => {
        e.preventDefault();
        let application_data = {
            name: this.state.name,
            throttlingTier: this.state.quota,
            description: this.state.description,
            callbackUrl: "http://my.server.com/callback"
        };
        let new_api = new API();
        let promised_create = new_api.createApplication(application_data);
        promised_create.then(response => {
        let uuid = JSON.parse(response.data).applicationId;
	//Once application loading fixed this need to pass application ID and load app
        let redirect_url = "/applications/";
        this.props.history.push(redirect_url);
        console.log("Application created successfully.");
        }).catch(
            function (error_response) {
                console.log("Error while creating the application");
            });
    };

    render() {
        return (
            <Grid>
                <Grid item xs={12}>
                    <Paper style={{display:"flex"}}>
                        <Typography type="display2" gutterBottom className="page-title">
                            Applications
                        </Typography>
                        <Link to={"/applications"}>
                            <Button aria-owns="simple-menu" aria-haspopup="true" >
                                <CreateNewFolder /> Applications
                            </Button>
                        </Link>
                    </Paper>
                </Grid>
                <Grid item xs={12}  style={{paddingLeft:"40px", paddingRight:"20px  "}}>
                        <p className="help-text">
                            An application is a logical collection of APIs. Applications allow you to use a single access
                            token to invoke a collection of APIs and to subscribe to one API multiple times with different
                            SLA levels. The DefaultApplication is pre-created and allows unlimited access by default.
                        </p>
                    <Input name="applicationName" placeholder="Application Name" onChange={this.handleChange('name')}/>
                        <Typography type="caption" style={{marginTop:"20px"}} gutterBottom >
                            Per Token Quota
                        </Typography>
                        {this.state.tiers &&
                        <FormControl style={{width:"40%",marginBottom:"20px"}}>
                            <Select
                                style={{width:"50%"}}
                                value={this.state.quota}
                                onChange={this.handlePolicyChange('quota')}
                            >
                                {this.state.tiers.map((tier) => <MenuItem key={tier} value={tier}>{tier}</MenuItem>)}
                            </Select>
                        </FormControl>
                        }
                        <br />
                        <Typography type="caption" gutterBottom>
                            This feature allows you to assign an API request quota per access token. Allocated quota will be
                            shared among all the subscribed APIs of the application.
                        </Typography>
                        <br />
                        <Input name="description" placeholder="Application Description" onChange={this.handleChange('description')}/>

                        <br />
                    <Button onClick={this.handleSubmit} raised color="primary" style={{marginRight:"20px"}}>
                        Add Application
                    </Button>
                    <Button raised >
                        Cancel
                    </Button>
                </Grid>
            </Grid>
        );
    }
}


export default ApplicationCreate;
