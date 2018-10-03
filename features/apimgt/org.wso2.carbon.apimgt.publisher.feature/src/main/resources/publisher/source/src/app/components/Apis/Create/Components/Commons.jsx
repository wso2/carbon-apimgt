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
import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import { withStyles } from '@material-ui/core/styles';
import green from '@material-ui/core/colors/green';
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
    },
    paper: {
        padding: theme.spacing.unit * 2,
    },
    buttonProgress: {
        color: green[500],
        position: 'relative',
        marginTop: theme.spacing.unit * 5,
        marginLeft: theme.spacing.unit * 6.25,
    },
});

/**
 * Create API with inline Endpoint
 * @class Commons
 * @extends {Component}
 */
class Commons extends Component {
    /**
     * Creates an instance of Commons.
     * @param {any} props @inheritDoc
     * @memberof Commons
     */
    constructor(props) {
        super(props);
        this.state = {
            api: new API(),
            loading: false,
        };
        this.inputChange = this.inputChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    /**
     * Change input
     * @param {any} e Synthetic React Event
     * @memberof Commons
     */
    inputChange({ target }) {
        const { type } = this.props;
        const { name, value } = target;
        this.setState(({ api }) => {
            const changes = api;
            if (name === 'endpoint') {
                changes[name] = [
                    {
                        inline: {
                            name: `${api.name}_inline_prod`,
                            endpointConfig: {
                                list: [
                                    {
                                        url: value,
                                        timeout: '1000',
                                    },
                                ],
                                endpointType: 'SINGLE',
                            },
                            type,
                            endpointSecurity: {
                                enabled: false,
                            },
                        },
                        type: 'Production',
                    },
                    {
                        inline: {
                            name: `${api.name}_inline_sandbx`,
                            endpointConfig: {
                                list: [
                                    {
                                        url: value,
                                        timeout: '1000',
                                    },
                                ],
                                endpointType: 'SINGLE',
                            },
                            type,
                            endpointSecurity: {
                                enabled: false,
                            },
                        },
                        type: 'Sandbox',
                    },
                ];
            } else {
                changes[name] = value;
            }
            return { api: changes };
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
        api.version = 'v1.0.0';
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
     * @memberof Commons
     */
    render() {
        const { classes, type } = this.props;
        const { api, loading } = this.state;
        return (
            <Grid container className={classes.root} spacing={0} justify='center'>
                <Grid item md={10}>
                    <Paper className={classes.paper}>
                        <Typography type='title' gutterBottom>
                            <FormattedMessage
                                id={type === 'ws' ? 'create.new.websocket.api' : 'create.new.rest.api'}
                                defaultMessage='Create New REST API'
                            />
                        </Typography>
                        <Typography type='subheading' gutterBottom align='left'>
                            <FormattedMessage
                                id='fill.the.mandatory.fields'
                                defaultMessage={'Fill the mandatory fields (Name, Version, Context) '
                                    + 'and create the API. Configure advanced configurations later.'}
                            />
                        </Typography>
                        <form onSubmit={this.handleSubmit}>
                            <APIInputForm api={api} handleInputChange={this.inputChange} />
                            <Grid container direction='row' alignItems='flex-start' spacing={16}>
                                <Grid item>
                                    <ScopeValidation
                                        resourcePath={resourcePath.APIS}
                                        resourceMethod={resourceMethod.POST}
                                    >
                                        <div>
                                            <Button type='submit' variant='outlined' disabled={loading} color='primary'>
                                                <FormattedMessage id='create' defaultMessage='Create' />
                                            </Button>
                                            {loading && (
                                                <CircularProgress size={24} className={classes.buttonProgress} />
                                            )}
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
                    </Paper>
                </Grid>
            </Grid>
        );
    }
}

Commons.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    history: PropTypes.shape({
        push: PropTypes.func.isRequired,
    }).isRequired,
    type: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(Commons);
