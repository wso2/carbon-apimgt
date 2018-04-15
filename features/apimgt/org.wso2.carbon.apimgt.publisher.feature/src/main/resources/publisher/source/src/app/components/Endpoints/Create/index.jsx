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
import ArrowBack from 'material-ui-icons/ArrowBack';
import Alert from '../../Shared/Alert';

import API from '../../../data/api';

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
            secured: false,
            securityType: null,
            maxTPS: 10,
        };
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleInputs = this.handleInputs.bind(this);
    }

    handleChange = name => (event, checked) => {
        this.setState({ [name]: checked });
    };

    handleInputs(e) {
        this.setState({ [e.target.name]: e.target.value });
    }

    handleSubmit() {
        let endpointSecurity = { enabled: false };
        if (this.state.secured) {
            endpointSecurity = {
                enabled: true,
                username: this.state.username,
                password: this.state.password,
                type: this.state.securityType,
            };
        }
        const endpointDefinition = {
            endpointConfig: JSON.stringify({ serviceUrl: this.state.endpointType + '://' + this.state.serviceUrl }),
            endpointSecurity,
            type: this.state.endpointType,
            name: this.state.name,
            maxTps: this.state.maxTPS,
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
            <Grid container spacing={0} justify='left'>
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
                    <form className={classes.container} noValidate autoComplete='off'>
                        <TextField
                            label='Endpoint Name'
                            InputLabelProps={{
                                shrink: true,
                            }}
                            helperText='Enter a name to identify the endpoint. You will be able to pick this endpoint
                                        when creating/editing APIs '
                            fullWidth
                            name='name'
                            onChange={this.handleInputs}
                            placeholder='Endpoint Name'
                            autoFocus
                        />
                        <FormLabel component='legend' className={classes.legend}>
                            Endpoint Type
                        </FormLabel>
                        <RadioGroup
                            row
                            aria-label='type'
                            className={classes.group}
                            value={this.state.endpointType}
                            onChange={(e) => {
                                e.target.name = 'endpointType';
                                this.handleInputs(e);
                            }}
                        >
                            <FormControlLabel value='http' control={<Radio color='primary' />} label='HTTP' />
                            <FormControlLabel value='https' control={<Radio color='primary' />} label='HTTPS' />
                        </RadioGroup>
                        <TextField
                            className={classes.inputText}
                            label='Max TPS'
                            InputLabelProps={{
                                shrink: true,
                            }}
                            helperText='Max Transactions per second'
                            fullWidth
                            name='name'
                            onChange={(e) => {
                                e.target.name = 'maxTPS';
                                this.handleInputs(e);
                            }}
                            placeholder='100'
                        />
                        <TextField
                            className={classes.inputText}
                            name='serviceUrl'
                            label='Service URL'
                            InputLabelProps={{
                                shrink: true,
                            }}
                            helperText='Provide Service URL'
                            onChange={this.handleInputs}
                            placeholder='https://forecast-v3.weather.gov'
                            fullWidth
                        />
                        <FormControlLabel
                            className={classes.inputText}
                            control={
                                <Switch
                                    checked={this.state.secured}
                                    onChange={this.handleChange('secured')}
                                    value='secured'
                                    color='primary'
                                />
                            }
                            label='Secured'
                        />
                        {this.state.secured && (
                            <div className={classes.secured}>
                                Type
                                <RadioGroup
                                    row
                                    value={this.state.securityType}
                                    onChange={(e) => {
                                        e.target.name = 'securityType';
                                        this.handleInputs(e);
                                    }}
                                >
                                    <FormControlLabel value='basic' control={<Radio />} label='Basic' />
                                    <FormControlLabel value='digest' control={<Radio />} label='Digest' />
                                </RadioGroup>
                                <TextField
                                    className={classes.inputText}
                                    label='Username'
                                    InputLabelProps={{
                                        shrink: true,
                                    }}
                                    helperText='Enter the Username'
                                    fullWidth
                                    margin='normal'
                                    name='username'
                                    onChange={this.handleInputs}
                                    placeholder='Username'
                                />
                                <TextField
                                    className={classes.inputText}
                                    label='Password'
                                    InputLabelProps={{
                                        shrink: true,
                                    }}
                                    helperText='Enter the Password'
                                    fullWidth
                                    name='password'
                                    onChange={this.handleInputs}
                                    placeholder='Password'
                                />
                            </div>
                        )}
                        <div className={classes.buttonsWrapper}>
                            <Button
                                variant='raised'
                                color='primary'
                                className={classes.button}
                                onClick={this.handleSubmit}
                            >
                                Create
                            </Button>
                            <Link to='/endpoints/'>
                                <Button variant='raised' className={classes.button}>
                                    Cancel
                                </Button>
                            </Link>
                        </div>
                    </form>
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
