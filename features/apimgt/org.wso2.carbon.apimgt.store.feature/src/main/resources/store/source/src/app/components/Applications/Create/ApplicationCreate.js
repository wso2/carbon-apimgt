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
import IconButton from 'material-ui/IconButton';
import Paper from 'material-ui/Paper';
import Input, { InputLabel } from 'material-ui/Input';
import Select from 'material-ui/Select';
import { FormControl, FormHelperText } from 'material-ui/Form';
import BackIcon from 'material-ui-icons/ArrowBack';

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
            description: this.state.description
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
            <Grid container justify="center" alignItems="center" className="full-width">
                <Grid item xs={12} sm={12} md={8} lg={8} xl={8} >
                    <Typography type="display1" gutterBottom>
                        <Link to={"/applications"}>
                            <IconButton aria-label="Back">
                                <BackIcon/>
                            </IconButton>
                        </Link>Create Applications
                    </Typography>
                    <Paper className="add-form-padding">
                        <TextField
                            required
                            label="Application Name"
                            margin="normal"
                            onChange={this.handleChange('name')}
                        />
                        <br />
                        {this.state.tiers &&
                        <FormControl margin="normal">
                            <InputLabel htmlFor="quota-helper">Per Token Quota</InputLabel>
                            <Select
                                value={this.state.quota}
                                onChange={this.handlePolicyChange('quota')}
                                input={<Input name="quota" id="quota-helper" />}
                            >
                                {this.state.tiers.map((tier) => <MenuItem key={tier} value={tier}>{tier}</MenuItem>)}
                            </Select>
                            <FormHelperText>Assign API request quota per access token. Allocated quota will be
                            shared among all the subscribed APIs of the application.</FormHelperText>
                        </FormControl>
                        }
                        <TextField
                            label="Application Description"
                            margin="normal"
                            onChange={this.handleChange('description')}
                            fullWidth
                        />
                        <div className="form-buttons">
                            <Button onClick={this.handleSubmit} raised color="primary" style={{marginRight:"20px"}}>
                                Add Application
                            </Button>
                            <Button raised >
                                Cancel
                            </Button>
                        </div>    
                    </Paper>
                </Grid>
            </Grid>
        );
    }
}


export default ApplicationCreate;
