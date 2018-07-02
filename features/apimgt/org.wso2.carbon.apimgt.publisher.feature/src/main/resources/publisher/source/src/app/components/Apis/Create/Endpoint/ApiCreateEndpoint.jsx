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
import 'react-toastify/dist/ReactToastify.min.css';
import Button from 'material-ui/Button';
import Paper from 'material-ui/Paper';
import Typography from 'material-ui/Typography';
import Grid from 'material-ui/Grid';
import { withStyles } from 'material-ui/styles';
import green from 'material-ui/colors/green';
import { CircularProgress } from 'material-ui/Progress';
import PropTypes from 'prop-types';

import InputForm from './InputForm';
import API from '../../../../data/api.js';
import { ScopeValidation, resourceMethod, resourcePath } from '../../../../data/ScopeValidation';
import Alert from '../../../Shared/Alert';

const styles = theme => ({
    root: {
        flexGrow: 1,
        marginTop: 30,
    },
    paper: {
        padding: theme.spacing.unit * 2,
    },
    subHeadings: {
        fontWeight: '300',
        padding: '10px 0 10px 30px',
        margin: '0px',
    },
    buttonProgress: {
        color: green[500],
        position: 'relative',
        marginTop: -20,
        marginLeft: -50,
    },
});

/**
 * Create API with inline Endpoint
 * @class ApiCreateEndpoint
 * @extends {Component}
 */
class ApiCreateEndpoint extends Component {
    /**
     * Creates an instance of ApiCreateEndpoint.
     * @param {any} props @inheritDoc
     * @memberof ApiCreateEndpoint
     */
    constructor(props) {
        super(props);
        this.api = new API();
        this.state = {
            apiFields: {
                apiVersion: '1.0.0',
            },
            loading: false,
        };
        this.inputChange = this.inputChange.bind(this);
    }

    /**
     * Change input
     * @param {any} e Synthetic React Event
     * @memberof ApiCreateEndpoint
     */
    inputChange(e) {
        let name = 'selectedPolicies';
        let value = e;
        if (!Array.isArray(e)) {
            ({ name, value } = e.target);
        }
        const { apiFields } = this.state;
        this.setState({ apiFields });
        apiFields[name] = value;
    }

    createAPICallback = (response) => {
        const uuid = JSON.parse(response.data).id;
        const redirectURL = '/apis/' + uuid + '/overview';
        this.props.history.push(redirectURL);
    };

    /**
     * Do create API from either swagger URL or swagger file upload.In case of URL pre fetch the swagger file and make
     * a blob
     * and the send it over REST API.
     * @param e {Event}
     */
    handleSubmit = (e) => {
        e.preventDefault();
        const values = this.state.apiFields;
        // Check for form errors manually
        if (!values.apiName || !values.apiVersion || !values.apiContext) {
            Alert.warning('Please fill all required fields');
            return;
        }
        this.setState({ loading: true });

        const apiData = {
            name: values.apiName,
            context: values.apiContext,
            version: values.apiVersion,
        };
        if (values.apiEndpoint) {
            apiData.endpoint = API.getEndpoints(apiData.name, apiData.version, values.apiEndpoint);
        }
        const newAPI = new API();
        const promisedCreate = newAPI.create(apiData);
        promisedCreate
            .then((createResponse) => {
                const uuid = JSON.parse(createResponse.data).id;
                const promisedApi = this.api.get(uuid);
                Alert.info('API Created with UUID :' + uuid);
                promisedApi.then((apiResponse) => {
                    Alert.info('Updating API policies . . .');
                    const apiJSON = JSON.parse(apiResponse.data);
                    apiJSON.policies = this.state.apiFields.selectedPolicies;
                    console.info('Adding policies to the api', this.state.apiFields.selectedPolicies);
                    const promisedUpdate = this.api.update(apiJSON);
                    promisedUpdate.then((response) => {
                        Alert.info('Policies updated successfully!');
                        this.createAPICallback(response);
                    });
                });
            })
            .catch((error) => {
                console.error(error);
                this.setState({ loading: false });
                Alert.error('Error occurred while creating the API');
                if (error.response && error.response.obj) {
                    const data = error.response.obj;
                    const message = '[' + data.code + ']: ' + data.description;
                    Alert.error(message);
                }
            });

        console.log('Send this in a POST request:', apiData);
    };

    /**
     * @inheritDoc
     * @returns {React.Component} Render API Create with endpoint UI
     * @memberof ApiCreateEndpoint
     */
    render() {
        const { classes } = this.props;
        const { apiFields, loading } = this.state;

        return (
            <div className={classes.root}>
                <Grid container spacing={0} justify='center'>
                    <Grid item md={10}>
                        <Paper className={classes.paper}>
                            <Typography type='headline' gutterBottom>
                                Create New API
                            </Typography>
                            <Typography type='subheading' gutterBottom align='left' className={classes.subHeadings}>
                                Fill the mandatory fields (Name, Version, Context) and create the API. Configure
                                advanced configurations later.
                            </Typography>
                            <form onSubmit={this.handleSubmit}>
                                <InputForm apiFields={apiFields} handleInputChange={this.inputChange} />
                                <Grid container direction='row' justify='flex-end' alignItems='flex-end' spacing={16}>
                                    <Grid item>
                                        <Button raised onClick={() => this.props.history.push('/api/create/home')}>
                                            Cancel
                                        </Button>
                                    </Grid>
                                    <Grid item>
                                        <ScopeValidation
                                            resourcePath={resourcePath.APIS}
                                            resourceMethod={resourceMethod.POST}
                                        >
                                            <div>
                                                <Button
                                                    disabled={loading}
                                                    raised
                                                    color='primary'
                                                    id='action-create'
                                                    type='primary'
                                                >
                                                    Create
                                                </Button>
                                                {loading && (
                                                    <CircularProgress size={24} className={classes.buttonProgress} />
                                                )}
                                            </div>
                                        </ScopeValidation>
                                    </Grid>
                                </Grid>
                            </form>
                        </Paper>
                    </Grid>
                </Grid>
            </div>
        );
    }
}

ApiCreateEndpoint.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    history: PropTypes.shape({
        push: PropTypes.func.isRequired,
    }).isRequired,
};

export default withStyles(styles)(ApiCreateEndpoint);
