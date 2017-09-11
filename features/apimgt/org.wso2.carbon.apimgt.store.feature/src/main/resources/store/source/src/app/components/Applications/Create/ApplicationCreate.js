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
import Button from 'material-ui/Button';
import API from '../../../data/api'

import Grid from 'material-ui/Grid';
import Typography from 'material-ui/Typography';
import { Delete, Edit, CreateNewFolder, Description  }from 'material-ui-icons';
import TextField from 'material-ui/TextField';

import Paper from 'material-ui/Paper';
import Select from 'react-select';

class ApplicationCreate extends Component {

    constructor(props) {
        super(props);
        this.state = {
           quota: "unlimited",
            tires: []
        };
        this.quotaChange = this.quotaChange.bind(this);
    }

    componentDidMount() {
        //Get all the tires to populate the drop down.
        const api = new API();
        let promised_tires = api.getAllTiers("application");
        promised_tires.then(
            response => {
                let tires = [];
                for(let i=0; i<response.obj.count;i++) {
                    let tier = {};
                    tier.name = response.obj.list[i].name;
                    tier.description = response.obj.list[i].description;
                    tires.push(tier);
                }
                this.setState({tiers: tires});
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
    quotaChange(val){
            this.setState({quota: val.value});
    }
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
                        <TextField
                            label="Application Name"
                            placeholder="Name"
                            style={{width:"100%"}}
                        />
                        <Typography type="caption" style={{marginTop:"20px"}} gutterBottom >
                            Per Token Quota
                        </Typography>
                        {this.state.tires &&
                            <Select
                                label="Per Token Quota"
                                name="form-field-name"
                                value={this.state.quota}
                                options={this.state.tires}
                                onChange={this.quotaChange}
                            />
                        }
                        <br />
                        <Typography type="caption" gutterBottom>
                            This feature allows you to assign an API request quota per access token. Allocated quota will be
                            shared among all the subscribed APIs of the application.
                        </Typography>
                        <br />

                        <TextField
                                id="multiline-flexible"
                                label="Description"
                                multiline
                                rowsMax="4"
                                margin="normal"
                                style={{width:"100%"}}
                        />
                        <br />
                    <Button raised color="primary" style={{marginRight:"20px"}}>
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