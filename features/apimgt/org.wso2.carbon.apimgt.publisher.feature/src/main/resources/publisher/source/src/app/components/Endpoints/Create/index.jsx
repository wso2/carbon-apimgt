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
import Radio, { RadioGroup } from 'material-ui/Radio';
import Button from 'material-ui/Button';
import Typography from 'material-ui/Typography';
import TextField from 'material-ui/TextField';
import { Link } from 'react-router-dom';
import PropTypes from 'prop-types';
import { withStyles } from 'material-ui/styles';
import { FormControlLabel, FormLabel } from 'material-ui/Form';
import Switch from 'material-ui/Switch';
import Grid from 'material-ui/Grid';
import ArrowBack from '@material-ui/icons/ArrowBack';
import Alert from '../../Shared/Alert';

import API from '../../../data/api';
import EndpointForm from './EndpointForm';

const styles = theme => ({
    titleBar: {
        display: 'flex',
        justifyContent: 'space-between',
        borderBottomWidth: '1px',
        borderBottomStyle: 'solid',
        borderColor: theme.palette.text.secondary,
        marginBottom: 20,
    },
    buttonLeft: {
        alignSelf: 'flex-start',
        display: 'flex',
    },
    buttonRight: {
        alignSelf: 'flex-end',
        display: 'flex',
    },
    title: {
        display: 'inline-block',
        marginRight: 50,
    },
    buttonBack: {
        marginRight: 20,
    },
    filterWrapper: {
        display: 'flex',
    },
    formControl: {
        marginTop: 21,
    },
    textField: {
        marginLeft: 20,
    },
    group: {
        display: 'flex',
    },
    legend: {
        marginBottom: 0,
        borderBottomStyle: 'none',
        marginTop: 20,
        fontSize: 12,
    },
    inputText: {
        marginTop: 20,
    },
    secured: {
        marginTop: 40,
    },
    button: {
        marginRight: 20,
    },
    buttonsWrapper: {
        marginTop: 40,
    },
});
/**
 * Endpoint create form
 * @class EndpointCreate
 * @extends {Component}
 */
class EndpointCreate extends Component {
    /**
     * Creates an instance of EndpointCreate.
     * @param {any} props @inheritDoc
     * @memberof EndpointCreate
     */
    constructor(props) {
        super(props);
        this.state = {
            endpointType: 'http',
            endpointSecurity: false,
            endpointSecurityType: null,
            endpointMaxTPS: 10,
        };
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleInputs = this.handleInputs.bind(this);
    }

    /**
     * Handle endpoint form inputs
     * @param {React.SyntheticEvent} event triggered by user inputs
     * @memberof EndpointCreate
     */
    handleInputs(event) {
        const target = event.currentTarget ? event.currentTarget : event.target;
        const { name } = target;
        let { value } = target;
        if (name === 'endpointSecurity') {
            value = event.currentTarget.checked;
        }
        this.setState({ [name]: value });
    }

    /**
     * Send endpoint create POST call via REST API
     * @returns {void}
     * @memberof EndpointCreate
     */
    handleSubmit() {
        let endpointSecurityObject = { enabled: false };
        const {
            endpointSecurity,
            endpointSecurityUsername,
            endpointSecurityPassword,
            endpointSecurityType,
            endpointType,
            endpointServiceUrl,
            endpointMaxTPS,
            endpointName,
        } = this.state;
        if (endpointSecurity) {
            endpointSecurityObject = {
                enabled: true,
                username: endpointSecurityUsername,
                password: endpointSecurityPassword,
                type: endpointSecurityType,
            };
        }
        const endpointDefinition = {
            endpointConfig: JSON.stringify({ serviceUrl: endpointType + '://' + endpointServiceUrl }),
            endpointSecurity: endpointSecurityObject,
            type: endpointType,
            name: endpointName,
            maxTps: endpointMaxTPS,
        };
        const api = new API();
        const promisedEndpoint = api.addEndpoint(endpointDefinition);
        return promisedEndpoint
            .then((response) => {
                const { name, id } = response.obj;
                Alert.info('New endpoint ' + name + ' created successfully');
                const redirectURL = '/endpoints/' + id + '/';
                this.props.history.push(redirectURL);
            })
            .catch((error) => {
                console.error(error);
                Alert.info('Error occurred while creating the endpoint!');
            });
    }

    /**
     * @inheritDoc
     * @returns {React.Component} return component
     * @memberof EndpointCreate
     */
    render() {
        const { classes } = this.props;
        return (
            <Grid container spacing={0} justify='flex-start'>
                <Grid item xs={12} className={classes.titleBar}>
                    <div className={classes.buttonLeft}>
                        <Link to='/endpoints/'>
                            <Button variant='raised' size='small' className={classes.buttonBack} color='default'>
                                <ArrowBack />
                            </Button>
                        </Link>
                        <div className={classes.title}>
                            <Typography variant='display2'>Add new Global Endpoint</Typography>
                        </div>
                    </div>
                </Grid>
                <Grid item xs={12} lg={6} xl={4}>
                    <EndpointForm handleInputs={this.handleInputs} {...this.state} />
                    <div className={classes.buttonsWrapper}>
                        <Button variant='raised' color='primary' className={classes.button} onClick={this.handleSubmit}>
                            Create
                        </Button>
                        <Link to='/endpoints/'>
                            <Button variant='raised' className={classes.button}>
                                Cancel
                            </Button>
                        </Link>
                    </div>
                </Grid>
            </Grid>
        );
    }
}

EndpointCreate.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    history: PropTypes.shape({
        push: PropTypes.func,
    }).isRequired,
};

export default withStyles(styles)(EndpointCreate);
