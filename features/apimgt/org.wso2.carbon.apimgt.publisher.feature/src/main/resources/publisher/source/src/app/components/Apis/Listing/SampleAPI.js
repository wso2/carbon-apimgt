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
"use strict";

import React, {Component} from 'react';

import Grid from 'material-ui/Grid';
import Paper from "material-ui/Paper";
import Typography from "material-ui/Typography";
import Button from "material-ui/Button";
import API from '../../../data/api';
import Alert from '../../Shared/Alert';
import Redirect from "react-router-dom/Redirect";
import CircularProgress from "material-ui/Progress/CircularProgress";
import {withStyles} from 'material-ui/styles';
import green from "material-ui/colors/green";

const styles = {
    buttonProgress: {
        color: green[500],
        position: 'relative',
    }
};

class SampleAPI extends Component {
    constructor(props) {
        super(props);
        this.state = {
            message: '',
            published: false,
            api: null,
            deploying: false
        };
        this.sampleApi = new API();
        this.handleDeploySample = this.handleDeploySample.bind(this);
        this._createSampleAPI = this._createSampleAPI.bind(this);
        this._publishSampleAPI = this._publishSampleAPI.bind(this);
        this._updatePolicies = this._updatePolicies.bind(this);
    }

    handleDeploySample() {
        this.setState({deploying: true});
        let sample_api = this._createSampleAPI();
        sample_api.then(this._updatePolicies).then(this._publishSampleAPI);
    }

    _createSampleAPI() {
        const url = "http://petstore.swagger.io/v2/swagger.json";
        let data = {};
        data.url = url;
        data.type = 'swagger-url';
        this.setState({message: "Creating sample Pet-Store API . . ."});
        return this.sampleApi.create(data)
            .then(response => response.obj)
            .catch(error => {
                console.error(error);
                this.setState({deploying: false});
                this.setState({message: error});
                let error_data = JSON.parse(error.data);
                let messageTxt = "Error[" + error_data.code + "]: " + error_data.description + " | " + error_data.message + ".";
                this.setState({message: messageTxt});
            });
    }

    _updatePolicies(api) {
        let uuid = api.id;
        let promisedApi = this.sampleApi.get(uuid);
        this.setState({message: 'API Created with UUID :' + uuid, api: api});
        return promisedApi.then(response => {
            this.setState({message: 'Updating API policies with Bronze, Unlimited & Gold. . .'});
            let api = response.obj;
            api.policies = ["Bronze", "Unlimited", "Gold"];
            console.info("Adding policies to the api", api.policies);
            let promised_update = this.sampleApi.update(api);
            return promised_update.then(response => {
                this.setState({message: 'Policies updated successfully!'});
                return response.obj;
            })
        });
    }

    _publishSampleAPI(api) {
        let promisedUpdate;
        const newState = "Published";
        const apiUUID = api.id;
        promisedUpdate = this.sampleApi.updateLcState(apiUUID, newState);
        promisedUpdate.then((response) => {
            const message = "Pet-Store API Published successfully";
            this.setState({message: message, published: true});
        }).catch(error => {
            console.error(error);
            this.setState({deploying: false});
            this.setState({message: error});
        });
    }

    render() {
        const {message, published, api, deploying} = this.state;
        const { classes } = this.props;

        if (published && api) {
            const url = "/apis/" + api.id + "/overview";
            return <Redirect to={url}/>
        }
        return (
            <Grid container spacing={16} justify="center">
                <Grid item xs={6} style={{textAlign: "center"}}>
                    <Alert message={message}/>
                    <Paper>
                        <Typography align="center" type="headline" gutterBottom>
                            Can't find any APIs!
                        </Typography>
                        <Button disabled={deploying} raised onClick={this.handleDeploySample} color="primary">
                            Deploy Sample API
                        </Button>
                        {deploying && <CircularProgress size={24} className={classes.buttonProgress}/>}
                    </Paper>
                </Grid>
            </Grid>
        );
    }
}

export default withStyles(styles)(SampleAPI)