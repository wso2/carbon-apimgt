/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import { withStyles } from '@material-ui/core/styles';
import CircularProgress from '@material-ui/core/CircularProgress';
import PropTypes from 'prop-types';
import Alert from 'AppComponents/Shared/Alert';
import API from 'AppData/api.js';
import { FormattedMessage } from 'react-intl';
import { ScopeValidation, resourceMethod, resourcePath } from 'AppData/ScopeValidation';
import APIInputForm from 'AppComponents/Apis/Create/Endpoint/APIInputForm';

const styles = theme => ({
    root: {
        flexGrow: 1,
        marginLeft: 0,
        marginTop: 0,
        paddingLeft: theme.spacing.unit * 4,
        paddingTop: theme.spacing.unit * 2,
    },
    buttonProgress: {
        position: 'relative',
        marginTop: theme.spacing.unit * 5,
        marginLeft: theme.spacing.unit * 6.25,
    },
    button: {
        marginTop: theme.spacing.unit * 2,
        marginRight: theme.spacing.unit,
    },
    buttonSection: {
        paddingTop: theme.spacing.unit * 2,
    },
    subTitle: {
        color: theme.palette.grey[500],
    },
});

/**
 * Create API with inline Endpoint
 * @class APICreateForm
 * @extends {Component}
 */
class APICreateForm extends Component {
    /**
     * Creates an instance of APICreateForm.
     * @param {any} props @inheritDoc
     * @memberof APICreateForm
     */
    constructor(props) {
        super(props);
        this.state = {
            api: new API(),
            loading: false,
            valid: {
                name: { empty: false, alreadyExists: false },
                context: { empty: false, alreadyExists: false },
                version: { empty: false },
                endpoint: { empty: false },
            },
        };
        this.inputChange = this.inputChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    /**
     * Change input
     * @param {any} e Synthetic React Event
     * @memberof APICreateForm
     */
    inputChange({ target }) {
        const { type } = this.props;
        const { name, value } = target;
        this.setState(({ api, valid }) => {
            const changes = api;
            if (name === 'endpoint') {
                changes[name] = [
                    {
                        inline: {
                            name: `${api.name}_inline_production`,
                            endpointConfig: {
                                list: [
                                    {
                                        url: value,
                                        timeout: '1000',
                                    },
                                ],
                                endpointType: 'SINGLE',
                            },
                            endpointSecurity: {
                                enabled: false,
                                type: 'basic',
                                username: 'basic',
                                password: 'basic',
                            },
                            type: 'http',
                        },
                        type: 'production_endpoints'
                    },
                    {
                        inline: {
                            name: `${api.name}_inline_sandbox`,
                            endpointConfig: {
                                list: [
                                    {
                                        url: value,
                                        timeout: '1000',
                                    },
                                ],
                                endpointType: 'SINGLE',
                            },
                            endpointSecurity: {
                                enabled: false,
                                type: 'basic',
                                username: 'basic',
                                password: 'basic',
                            },
                            type: 'http',
                        },
                        type: 'sandbox_endpoints'
                    }
                ];
            } else {
                changes[name] = value;
            }
            // Checking validity.
            const validUpdated = valid;
            validUpdated.name.empty = !api.name;
            validUpdated.context.empty = !api.context;
            validUpdated.version.empty = !api.version;
            validUpdated.endpoint.empty = !api.endpoint;
            // TODO we need to add the already existing error for (context) by doing an api call ( the swagger definition does not contain such api call)
            return { api: changes, valid: validUpdated };
        });
    }

    /**
     * Do create API from either swagger URL or swagger file upload.In case of URL pre fetch the swagger file and make
     * a blob
     * and the send it over REST API.
     * @param e {Event}
     */
    handleSubmit(e) {
        e.preventDefault();
        const { api } = this.state;
        if (!api.name || !api.context || !api.version || !api.endpoint) {
            // Checking the api name,version,context undefined or empty states
            this.setState((oldState) => {
                const { valid, api } = oldState;
                const validUpdated = valid;
                validUpdated.name.empty = !api.name;
                validUpdated.context.empty = !api.context;
                validUpdated.version.empty = !api.version;
                validUpdated.endpoint.empty = !api.endpoint;
                return { valid: validUpdated };
            });
            return;
        }
        api.save()
            .then((newAPI) => {
                const redirectURL = '/apis/' + newAPI.id + '/overview';
                Alert.info(`${newAPI.name} created.`);
                this.props.history.push(redirectURL);
            })
            .catch((error) => {
                console.error(error);
                if (error.response) {
                    Alert.error(error.response.body.message);
                } else {
                    Alert.error(`Something went wrong while creating ${api.name}`);
                }
            });
    }

    /**
     * @inheritDoc
     * @returns {React.Component} Render API Create with endpoint UI
     * @memberof APICreateForm
     */
    render() {
        const { classes, type } = this.props;
        const { api, loading, valid } = this.state;
        return (
            <Grid container spacing={24} className={classes.root}>
                <Grid item xs={12} md={6}>
                    <div className={classes.titleWrapper}>
                        <Typography variant='h4' align='left' className={classes.mainTitle}>
                            <FormattedMessage id={type === 'ws' ? 'create.new.websocket.api' : 'create.new.rest.api'} defaultMessage='New REST API' />
                        </Typography>
                        <Typography variant='h5' align='left' className={classes.subTitle}>
                            GATEWAY-URL/
                            {api.version ? api.version : '{apiVersion}'}/{api.context ? api.context : '{context}'}
                        </Typography>
                    </div>
                    <form onSubmit={this.handleSubmit}>
                        <APIInputForm api={api} handleInputChange={this.inputChange} valid={valid} />
                        <Grid container direction='row' alignItems='flex-start' spacing={16} className={classes.buttonSection}>
                            <Grid item>
                                <ScopeValidation resourcePath={resourcePath.APIS} resourceMethod={resourceMethod.POST}>
                                    <div>
                                        <Button type='submit' disabled={loading} variant='contained' color='primary'>
                                            <FormattedMessage id='create' defaultMessage='Create' />
                                        </Button>
                                        {loading && <CircularProgress size={24} className={classes.buttonProgress} />}
                                    </div>
                                </ScopeValidation>
                            </Grid>
                            <Grid item>
                                <Button onClick={() => this.props.history.push('/apis')}>
                                    <FormattedMessage id='cancel' defaultMessage='Cancel' />
                                </Button>
                            </Grid>
                        </Grid>
                    </form>
                </Grid>
            </Grid>
        );
    }
}

APICreateForm.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    history: PropTypes.shape({
        push: PropTypes.func.isRequired,
    }).isRequired,
    type: PropTypes.shape({}).isRequired,
    valid: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(APICreateForm);
