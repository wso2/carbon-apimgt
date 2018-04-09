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
import './login.css';
import { Redirect, Switch, withRouter } from 'react-router-dom';
import AuthManager from '../../data/AuthManager';
import TextField from 'material-ui/TextField';
import Paper from 'material-ui/Paper';
import Button from 'material-ui/Button';
import Typography from 'material-ui/Typography';
import Snackbar from 'material-ui/Snackbar';
import Footer from '../Base/Footer/Footer';
import ConfigManager from '../../data/ConfigManager';
import Utils from '../../data/Utils';
import Input, { InputLabel } from 'material-ui/Input';
import Select from 'material-ui/Select';
import { FormControl } from 'material-ui/Form';
import { MenuItem } from 'material-ui/Menu';
import Grid from 'material-ui/Grid';

class Login extends Component {
    constructor(props) {
        super(props);
        this.authManager = new AuthManager();
        this.state = {
            isLogin: false,
            referrer: '/',
            loading: false,
            username: '',
            password: '',
            validate: false,
            messageOpen: false,
            message: '',
            environments: [],
            environmentId: 0,
            loginStatusEnvironments: [],
        };
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleInputChange = this.handleInputChange.bind(this);
        this.handleEnvironmentChange = this.handleEnvironmentChange.bind(this);
        this.handleRequestClose = this.handleRequestClose.bind(this);
    }

    componentDidMount() {
        const { appName } = this.props;

        // Get Environments
        ConfigManager.getConfigs()
            .environments.then((response) => {
                const environments = response.data.environments;
                let environmentId = Utils.getEnvironmentID(environments);
                if (environmentId === -1) {
                    environmentId = 0;
                }
                this.setState({ environments, environmentId });

                // Update environment to discard default environment configuration
                const environment = environments[environmentId];
                Utils.setEnvironment(environment);

                // Set authentication status of environments
                this.setLoginStatusOfEnvironments(environments);
            })
            .catch((error) => {
                console.error('Error while receiving environment configurations : ', error);
            });
    }

    setLoginStatusOfEnvironments(environments) {
        const loginStatusEnvironments = environments.map(environment => AuthManager.getUser(environment.label) !== null);
        this.setState({ loginStatusEnvironments });
    }

    handleSubmit(e) {
        e.preventDefault();
        this.setState({ loading: true });
        this.setState({ validate: true });
        const username = this.state.username;
        const password = this.state.password;
        const environment = this.state.environments[this.state.environmentId];

        if (!username || !password) {
            this.setState({ messageOpen: true });
            this.setState({ message: 'Please fill both username and password fields' });
            return;
        }

        const loginPromise = this.authManager.authenticateUser(username, password, environment);
        loginPromise
            .then((response) => {
                this.setState({ isLogin: AuthManager.getUser(), loading: false });
            })
            .catch((error) => {
                this.setState({ messageOpen: true });
                if (error.response.data.code === 900964) {
                    // Error while generating a new access token.
                    this.setState({ message: 'Invalid Username or Password.' });
                } else {
                    this.setState({ message: error.message });
                }
                console.log(error);
                this.setState({ loading: false });
            });
    }

    handleInputChange(event) {
        const target = event.target;
        const value = target.type === 'checkbox' ? target.checked : target.value;
        const name = target.id;

        this.setState({
            [name]: value,
        });
    }

    handleEnvironmentChange(event) {
        const environmentId = event.target.value;
        const environment = this.state.environments[environmentId];

        this.setState({
            environmentId,
        });
        Utils.setEnvironment(environment);
        this.setState({ environmentId, isLogin });
    }

    handleRequestClose() {
        this.setState({ messageOpen: false });
    }

    render() {
        const isMoreThanTwoEnvironments = this.state.environments && this.state.environments.length > 1;
        const { appName, appLabel } = this.props;

        // Show login page if sso disabled or more than two environments
        if (!this.state.isLogin) {
            // If not logged in, go to login page
            return (
                <div className='login-flex-container'>
                    <Snackbar
                        anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
                        open={this.state.messageOpen}
                        onClose={this.handleRequestClose}
                        autoHideDuration={3000}
                        SnackbarContentProps={{
                            'aria-describedby': 'message-id',
                        }}
                        message={<span id='message-id'>{this.state.message}</span>}
                    />
                    <Grid container justify='center' alignItems='center' spacing={0} style={{ height: '100vh' }}>
                        <Grid item lg={6} md={8} xs={10}>
                            <Grid container>
                                {/* Brand */}
                                <Grid item sm={3} xs={12}>
                                    <Grid container direction='column'>
                                        <Grid item>
                                            <img
                                                className='brand'
                                                src={`/${appName}/public/app/images/logo.svg`}
                                                alt='wso2-logo'
                                            />
                                        </Grid>
                                        <Grid item>
                                            <Typography type='subheading' align='right' gutterBottom>
                                                {`API ${appLabel}`}
                                            </Typography>
                                        </Grid>
                                    </Grid>
                                </Grid>

                                {/* Login Form */}
                                <Grid item sm={9} xs={12}>
                                    <div className='login-main-content'>
                                        <Paper elevation={1} square className='login-paper'>
                                            <form onSubmit={this.handleSubmit} className='login-form'>
                                                <Typography type='body1' gutterBottom>
                                                    Sign in to your account
                                                </Typography>

                                                {/* Environments */}
                                                {isMoreThanTwoEnvironments && (
                                                    <FormControl style={{ width: '100%', marginTop: '2%' }}>
                                                        <InputLabel htmlFor='environment'>Environment</InputLabel>
                                                        <Select
                                                            onChange={this.handleEnvironmentChange}
                                                            value={this.state.environmentId}
                                                            input={<Input id='environment' />}
                                                        >
                                                            {this.state.environments.map((environment, index) => (
                                                                <MenuItem value={index} key={index}>
                                                                    {environment.label}
                                                                </MenuItem>
                                                            ))}
                                                        </Select>
                                                    </FormControl>
                                                )}

                                                <FormControl style={{ width: '100%' }}>
                                                    <TextField
                                                        error={!this.state.username && this.state.validate}
                                                        id='username'
                                                        label='Username'
                                                        type='text'
                                                        autoComplete='username'
                                                        margin='normal'
                                                        style={{ width: '100%' }}
                                                        onChange={this.handleInputChange}
                                                    />
                                                    <TextField
                                                        error={!this.state.password && this.state.validate}
                                                        id='password'
                                                        label='Password'
                                                        type='password'
                                                        autoComplete='current-password'
                                                        margin='normal'
                                                        style={{ width: '100%' }}
                                                        onChange={this.handleInputChange}
                                                    />
                                                </FormControl>

                                                {/* Buttons */}
                                                <Button
                                                    type='submit'
                                                    raised
                                                    color='primary'
                                                    className='login-form-submit'
                                                >
                                                    {'Login'}
                                                </Button>
                                            </form>
                                        </Paper>
                                    </div>
                                </Grid>
                            </Grid>
                        </Grid>
                    </Grid>
                    <Footer />
                </div>
            );
        } else {
            // If logged in, redirect to /apis page
            return (
                <Switch>
                    <Redirect to={this.state.referrer} />
                </Switch>
            );
        }
    }
}

export default withRouter(Login);
