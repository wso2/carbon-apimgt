/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import { FormattedMessage } from 'react-intl';
import Paper from '@material-ui/core/Paper';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import Footer from 'AppComponents/Base/Footer/Footer';

import './login.css';

/**
 * Auth method selector class
 * @class Selector
 * @extends {Component}
 */
class SelectLogin extends Component {
    /**
     * Creates an instance of SelectLogin.
     * @param {any} props @inheritDoc
     * @memberof SelectLogin
     */
    constructor(props) {
        super(props);
        this.doBasicLogin = this.doBasicLogin.bind(this);
        this.doIDPLogin = this.doIDPLogin.bind(this);
    }

    /**
     * Do Basic Authentication redirection
     * @param {React.SyntheticEvent} e Click event of the submit button
     */
    doBasicLogin = (e) => {
        e.preventDefault();
        window.location = '/publisher-new/login/basic';
    }

    /**
     * Do IDP Authentication redirection
     * @param {React.SyntheticEvent} e Click event of the submit button
     */
    doIDPLogin = (e) => {
        e.preventDefault();
        window.location = '/publisher-new/services/configs';
    }

    /**
     *
     *
     * @returns {React.Component} Render SelectLogin component
     * @memberof SelectLogin
     */
    render() {
        return (
            <div className='login-flex-container'>
                <Grid container justify='center' alignItems='center' spacing={0} style={{ height: '100vh' }}>
                    <Grid item lg={6} md={8} xs={10}>
                        <Grid container>
                            {/* Brand */}
                            <Grid item sm={3} xs={12}>
                                <Grid container direction='column'>
                                    <Grid item>
                                        <img
                                            className='brand'
                                            src='/publisher-new/site/public/images/logo.svg'
                                            alt='wso2-logo'
                                        />
                                    </Grid>
                                    <Grid item>
                                        <Typography type='subheading' align='right' gutterBottom>
                                            <FormattedMessage id='api.publisher' defaultMessage='API PUBLISHER' />
                                        </Typography>
                                    </Grid>
                                </Grid>
                            </Grid>
                            {/* Login Form */}
                            <Grid item sm={9} xs={12}>
                                <div className='login-main-content'>
                                    <Paper elevation={1} square className='login-paper'>
                                        <form onSubmit={this.doBasicLogin} className='login-form'>
                                            <Typography type='body1' gutterBottom>
                                                <FormattedMessage
                                                    id='login.using.basic'
                                                    defaultMessage='Login using Basic Authentication.'
                                                />
                                            </Typography>
                                            {/* Buttons */}
                                            <Button
                                                type='submit'
                                                variant='raised'
                                                color='primary'
                                                className='login-form-submit'
                                            >
                                                <FormattedMessage
                                                    id='login.using.basic'
                                                    defaultMessage='Login using Basic'
                                                />
                                            </Button>
                                        </form>
                                        <form onSubmit={this.doIDPLogin} className='login-form'>
                                            <Typography type='body1' gutterBottom>
                                                <FormattedMessage
                                                    id='login.using.idp'
                                                    defaultMessage='Login using IDP service.'
                                                />
                                            </Typography>
                                            {/* Buttons */}
                                            <Button
                                                type='submit'
                                                variant='raised'
                                                color='primary'
                                                className='login-form-submit'
                                            >
                                                <FormattedMessage
                                                    id='login.using.idp'
                                                    defaultMessage='Login using IDP'
                                                />
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
    }
}

export default SelectLogin;
