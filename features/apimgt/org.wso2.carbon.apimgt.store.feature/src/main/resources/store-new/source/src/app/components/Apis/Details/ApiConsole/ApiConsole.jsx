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
import SwaggerUI from 'swagger-ui';
import 'swagger-ui/dist/swagger-ui.css';
import Select from '@material-ui/core/Select';
import TextField from '@material-ui/core/TextField';
import { withStyles } from '@material-ui/core/styles';

import Progress from '../../../Shared/Progress';
import Api from '../../../../data/api';
import AuthManager from '../../../../data/AuthManager';

/**
 *
 *
 * @param {*} theme
 */
const styles = {
    authHeader: {
        marginLeft: '105px',
        color: '#555555',
        backgroundColor: '#eeeeee',
        border: '1px solid #ccc',
    },
    inputText: {
        marginLeft: '40px',
        minWidth: '400px',
    },
    gatewaySelect: {
        marginLeft: '173px',
        minWidth: '400px',
    },
    credentialSelect: {
        marginLeft: '150px',
        minWidth: '400px',
        marginRight: '10px',
    },
    grid: {
        spacing: 20,
        marginTop: '30px',
        marginBottom: '30px',
        paddingLeft: '90px',
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
        this.state = {
            apiGateways: null,
            apiGateway: '',
            credentialTypes: [],
            credentialType: '',
            accessToken: '',
            setSwagger: null,
        };
    }

    /**
     *
     *
     * @memberof ApiConsole
     */
    componentDidMount() {
        const api = new Api();
        const { match } = this.props;

        const disableAuthorizeAndInfoPlugin = function () {
            return {
                wrapComponents: {
                    authorizeBtn: () => () => null,
                    info: () => () => null,
                },
            };
        };

        const credentialTypes = [];
        const apiGateways = [];
        credentialTypes.push('OAuth2');
        apiGateways.push('Default');

        this.setState({
            apiGateways,
            apiGateway: apiGateways[0],
            credentialTypes,
            credentialType: credentialTypes[0],
        });

        const promisedSwagger = api.getSwaggerByAPIId(match.params.api_uuid);

        promisedSwagger
            .then((swagger) => {
                const { url } = swagger;
                this.setState({ setSwagger: true });

                SwaggerUI({
                    dom_id: '#ui',
                    spec: swagger.body,
                    requestInterceptor: (req) => {
                        // Only set Authorization header if the request matches the spec URL
                        if (req.url === url) {
                            req.headers.Authorization = 'Bearer ' + AuthManager.getUser().getPartialToken();
                        }
                        return req;
                    },
                    presets: [SwaggerUI.presets.apis, disableAuthorizeAndInfoPlugin],
                    plugins: [SwaggerUI.plugins.DownloadUrl],
                });
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
    handleChange = name => (event) => {
        this.setState({ [name]: event.target.value });
    };

    /**
     *
     *
     * @returns
     * @memberof ApiConsole
     */
    render() {
        const { classes } = this.props;
        const { setSwagger, apiGateway } = this.state;

        if (setSwagger == null) {
            return <Progress />;
        }

        return (
            <React.Fragment>
                <Grid container className={classes.grid}>
                    <Grid item md={12}>
                        <div>
                            <Typography variant='subheading'>
                                API Gateway
                                <Select
                                    value={apiGateway}
                                    onChange={this.handleChange('apiGateway')}
                                    className={classes.gatewaySelect}
                                >
                                    {this.state.apiGateways.map(apiGateway => (
                                        <option value={apiGateway} key={apiGateway}>
                                            {apiGateway}
                                        </option>
                                    ))}
                                </Select>
                            </Typography>
                        </div>
                        <div>
                            <Typography variant='subheading' gutterleft>
                                API Credentials
                                <Select
                                    value={this.state.credentialType}
                                    onChange={this.handleChange('credentialType')}
                                    className={classes.credentialSelect}
                                >
                                    {this.state.credentialTypes.map(type => (
                                        <option value={type} key={type}>
                                            {type}
                                        </option>
                                    ))}
                                </Select>
                            </Typography>
                        </div>
                        <div>
                            <Typography variant='subheading' gutterleft>
                                Set Request Header
                                <span className={classes.authHeader}>Authorization : Bearer</span>
                                <TextField
                                    InputLabelProps={{
                                        shrink: true,
                                    }}
                                    className={classes.inputText}
                                    label='Access Token'
                                    name='accessToken'
                                    defaultValue=''
                                    onChange={this.handleChange('accessToken')}
                                />
                            </Typography>
                        </div>
                    </Grid>
                </Grid>
                <div id='ui' />
            </React.Fragment>
        );
    }
}

ApiConsole.defaultProps = {
    handleInputs: false,
};

ApiConsole.propTypes = {
    handleInputs: PropTypes.oneOfType([PropTypes.func, PropTypes.bool]),
    classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(ApiConsole);
