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

import React from 'react';
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';
import PropTypes from 'prop-types';
import 'swagger-ui-react/swagger-ui.css';
import TextField from '@material-ui/core/TextField';
import { withStyles } from '@material-ui/core/styles';
import IconButton from '@material-ui/core/IconButton';
import InputAdornment from '@material-ui/core/InputAdornment';
import Visibility from '@material-ui/icons/Visibility';
import VisibilityOff from '@material-ui/icons/VisibilityOff';
import AuthManager from 'AppData/AuthManager';
import Paper from '@material-ui/core/Paper';
import WarningIcon from '@material-ui/icons/Warning';
import Input from '@material-ui/core/Input';
import InputLabel from '@material-ui/core/InputLabel';
import MenuItem from '@material-ui/core/MenuItem';
import FormControl from '@material-ui/core/FormControl';
import Select from '@material-ui/core/Select';

import Progress from '../../../Shared/Progress';
import Api from '../../../../data/api';
import SwaggerUI from './SwaggerUI';

/**
 *
 *
 * @param {*} theme
 */
const styles = {
    inputAdornmentStart: {
        width: '100%',
    },
    inputText: {
        marginLeft: '40px',
        minWidth: '400px',
    },
    grid: {
        spacing: 20,
        marginTop: '30px',
        marginBottom: '30px',
        paddingLeft: '90px',
    },
    userNotificationPaper: {
        padding: '20px',
    },
};
/**
 *
 *
 * @class ApiConsole
 * @extends {React.Component}
 */
class ApiConsole extends React.Component {
    /**
     *Creates an instance of ApiConsole.
     * @param {*} props
     * @memberof ApiConsole
     */
    constructor(props) {
        super(props);
        this.state = { showToken: false };
        this.handleChanges = this.handleChanges.bind(this);
        this.accessTokenProvider = this.accessTokenProvider.bind(this);
        this.handleClickShowToken = this.handleClickShowToken.bind(this);
    }

    /**
     *
     *
     * @memberof ApiConsole
     */
    componentDidMount() {
        const api = new Api();
        const { match } = this.props;
        const apiID = match.params.api_uuid;
        const promisedSwagger = api.getSwaggerByAPIId(apiID);
        const promisedAPI = api.getAPIById(apiID);
        const promisedSubscriptions = api.getSubscriptions(apiID);

        Promise.all([promisedAPI, promisedSwagger, promisedSubscriptions.catch(error => console.error(error))])
            .then((responses) => {
                const data = responses.map(response => response && response.body);
                const apiObj = data[0];
                const swagger = data[1];
                const subscriptions = data[2];

                swagger.basePath = apiObj.context;
                swagger.host = 'localhost:8243';
                this.setState({ api: apiObj, swagger, subscriptions });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const { status } = error;
                if (status === 404) {
                    this.setState({ notFound: true });
                }
            });
    }

    /**
     *
     *
     * @memberof ApiConsole
     */
    handleClickShowToken() {
        const { showToken } = this.state;
        this.setState({ showToken: !showToken });
    }

    /**
     *
     *
     * @returns
     * @memberof ApiConsole
     */
    accessTokenProvider() {
        const { accessToken } = this.state;
        return accessToken;
    }

    /**
     *
     *
     * @memberof ApiConsole
     */
    handleChanges(event) {
        const target = event.currentTarget;
        const { name, value } = target;
        this.setState({ [name]: value });
    }

    /**
     *
     *
     * @returns
     * @memberof ApiConsole
     */
    render() {
        const { classes } = this.props;
        const {
            api, notFound, swagger, accessToken, showToken, subscriptions,
        } = this.state;
        const user = AuthManager.getUser();

        if (api == null || swagger == null) {
            return <Progress />;
        }
        if (notFound) {
            return 'API Not found !';
        }

        return (
            <React.Fragment>
                <Grid container className={classes.grid}>
                    {!user && (
                        <Grid item md={6}>
                            <Paper className={classes.userNotificationPaper}>
                                <Typography variant='h5' component='h3'>
                                    <WarningIcon />
                                    {' '}
Notice
                                </Typography>
                                <Typography component='p'>
                                    You require an access token to try the API. Please log in and subscribe to the API
                                    to generate an access token. If you already have an access token, please provide it
                                    below.
                                </Typography>
                            </Paper>
                        </Grid>
                    )}
                    {subscriptions && subscriptions.list.length > 0 && (
                        <Grid container>
                            <Grid item md={3}>
                                <Typography variant='subheading' gutterleft>
                                    Select Application
                                </Typography>
                            </Grid>
                            <Grid item md={9}>
                                <FormControl className={classes.formControl}>
                                    <InputLabel htmlFor='subscriptions-selection'>Subscribed applications</InputLabel>
                                    <Select
                                        autoWidth
                                        name='selectedSubscribedApplication'
                                        value={subscriptions.list[0].applicationInfo.applicationId}
                                        onChange={this.handleChanges}
                                        input={<Input name='subscription' id='subscriptions-selection' />}
                                    >
                                        {subscriptions.list.map(sub => (
                                            <MenuItem value={sub.applicationInfo.applicationId}>
                                                {sub.applicationInfo.name}
                                            </MenuItem>
                                        ))}
                                    </Select>
                                </FormControl>
                            </Grid>
                        </Grid>
                    )}

                    <Grid container>
                        <Grid item md={3}>
                            <Typography variant='subheading' gutterleft>
                                Set Request Header
                            </Typography>
                        </Grid>
                        <Grid item md={9}>
                            <TextField
                                margin='normal'
                                variant='outlined'
                                className={classes.inputText}
                                label='Access Token'
                                name='accessToken'
                                onChange={this.handleChanges}
                                type={showToken ? 'text' : 'password'}
                                value={accessToken}
                                helperText='Enter access token'
                                InputProps={{
                                    endAdornment: (
                                        <InputAdornment position='end'>
                                            <IconButton
                                                edge='end'
                                                aria-label='Toggle token visibility'
                                                onClick={this.handleClickShowToken}
                                            >
                                                {showToken ? <VisibilityOff /> : <Visibility />}
                                            </IconButton>
                                        </InputAdornment>
                                    ),
                                    startAdornment: (
                                        <InputAdornment className={classes.inputAdornmentStart} position='start'>
                                            {api.authorizationHeader ? api.authorizationHeader : 'Authorization'}
                                            {' '}
                                            :Bearer
                                        </InputAdornment>
                                    ),
                                }}
                            />
                        </Grid>
                    </Grid>
                </Grid>
                <SwaggerUI accessTokenProvider={this.accessTokenProvider} spec={swagger} />
            </React.Fragment>
        );
    }
}

ApiConsole.defaultProps = {
    // handleInputs: false,
};

ApiConsole.propTypes = {
    // handleInputs: PropTypes.oneOfType([PropTypes.func, PropTypes.bool]),
    classes: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(ApiConsole);
