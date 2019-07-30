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
import React from 'react';

import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import PropTypes from 'prop-types';
import { FormattedMessage, injectIntl, defineMessages } from 'react-intl';
import { withStyles } from '@material-ui/core/styles';

import API from 'AppData/api.js';
import Stepper from '@material-ui/core/Stepper';
import Step from '@material-ui/core/Step';
import StepLabel from '@material-ui/core/StepLabel';
import Button from '@material-ui/core/Button';

import APIInputForm from 'AppComponents/Apis/Create/Components/APIInputForm';
import Alert from 'AppComponents/Shared/Alert';
import ProvideOpenAPI from './Steps/ProvideOpenAPI';
import APICreateTopMenu from '../Components/APICreateTopMenu';

const styles = theme => ({
    root: {
        width: theme.custom.contentAreaWidth,
        flexGrow: 1,
        marginLeft: 0,
        marginTop: 0,
        paddingLeft: theme.spacing.unit * 4,
        paddingTop: theme.spacing.unit * 2,
    },
    paper: {
        padding: theme.spacing.unit * 2,
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
    subTitle: {
        color: theme.palette.grey[500],
    },
    FormControl: {
        padding: 0,
        width: '100%',
        marginTop: 0,
    },
    FormControlOdd: {
        padding: 0,
        backgroundColor: theme.palette.background.paper,
        width: '100%',
        marginTop: 0,
    },
    radioWrapper: {
        display: 'flex',
        flexDirection: 'row',
    },
    dropZoneInside: {},
    dropZone: {
        width: '100%',
        color: theme.palette.grey[500],
        border: 'dashed 1px ' + theme.palette.grey[500],
        background: theme.palette.grey[100],
        padding: theme.spacing.unit * 4,
        textAlign: 'center',
        cursor: 'pointer',
    },
    dropZoneIcon: {
        color: theme.palette.grey[500],
        width: 100,
        height: 100,
    },
    dropZoneError: {
        color: theme.palette.error.main,
    },
    dropZoneErrorBox: {
        border: 'dashed 1px ' + theme.palette.error.main,
    },
    fileNameWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        '& div': {
            display: 'flex',
            flexDirection: 'row',
            alignItems: 'center',
        },
    },
    buttonSection: {
        paddingTop: theme.spacing.unit * 2,
    },
    stepper: {
        paddingLeft: 0,
        marginLeft: 0,
        width: 400,
    },
});

/**
 * @inheritDoc
 * @class ApiCreateOpenAPI
 * @extends {React.Component}
 */
class ApiCreateOpenAPI extends React.Component {
    /**
     * Creates an instance of ApiCreateOpenAPI.
     * @param {any} props @inheritDoc
     * @memberof ApiCreateOpenAPI
     */
    constructor(props) {
        super(props);
        this.state = {
            api: new API(),
            activeStep: 0,
            valid: {
                openAPIUrl: { empty: false, invalidUrl: false },
                openAPIFile: { empty: false, invalidFile: false },
                name: { empty: false, alreadyExists: false },
                context: { empty: false, alreadyExists: false },
                version: { empty: false },
                endpointConfig: { empty: false },
            },
            validationInfo: {},
            openAPIFile: {},
            openAPIUrl: '',
        };
        this.inputChange = this.inputChange.bind(this);
    }

    getSteps = () => {
        return [
            <FormattedMessage
                id='Apis.Create.OpenAPI.ApiCreateOpenAPI.select.openapi'
                defaultMessage='Select OpenAPI Definition'
            />,
            <FormattedMessage
                id='Apis.Create.OpenAPI.ApiCreateOpenAPI.create.api'
                defaultMessage='Create API'
            />,
        ];
    };

    setOpenAPIFile = (openAPIFile) => {
        this.setState({ openAPIFile: openAPIFile[0] });
    };


    handleNext = () => {
        const { api, openAPIFile } = this.state;
        const { intl } = this.props;
        api.validateOpenAPIByFile(openAPIFile)
            .then((response) => {
                const { isValid, errors } = response.obj;
                if (isValid) {
                    this.setState({ activeStep: 1 });
                } else {
                    const messages = defineMessages({
                        openAPIValidationFailed: {
                            id: 'Apis.Create.OpenAPI.ApiCreateOpenAPI.openapi.validation.failed',
                            defaultMessage: 'OpenAPI definition validation failed. Reason(s): [{failedReasons}]',
                        },
                    });
                    let failedReasons = '';
                    errors.forEach((err, index) => {
                        failedReasons += err.description + (index < errors.length - 1 ? ', ' : '');
                    });
                    Alert.error(intl.formatMessage(messages.openAPIValidationFailed, { failedReasons }));
                }
            });
    };

    handleFinish = () => {
        const { api, openAPIFile } = this.state;
        const { intl, history } = this.props;
        api.importOpenAPIByFile(openAPIFile)
            .then((response) => {
                const newAPI = response.body;
                const redirectURL = '/apis/' + newAPI.id + '/overview';
                const apiAndVersion = newAPI.name + ':' + newAPI.version;
                const messages = defineMessages({
                    apiCreated: {
                        id: 'Apis.Create.OpenAPI.ApiCreateOpenAPI.create.success',
                        defaultMessage: '{apiAndVersion} created successfully.',
                    },
                });
                Alert.info(intl.formatMessage(messages.apiCreated, { apiAndVersion }));
                history.push(redirectURL);
            })
            .catch((error) => {
                const messages = defineMessages({
                    apiCreateFailed: {
                        id: 'Apis.Create.OpenAPI.ApiCreateOpenAPI.create.failed',
                        defaultMessage: 'API creation failed. Code: {errCode}, Reason(s): "{errMessage}"',
                    },
                });
                const errResponse = error.response;
                const errMessage = errResponse.body.description;
                const errCode = errResponse.status;
                Alert.error(intl.formatMessage(messages.apiCreateFailed, { errCode, errMessage }));
            });
    };

    /**
     * Change input
     * @param {any} e Synthetic React Event
     * @memberof APICreateForm
     */
    inputChange({ target }) {
        const { name, value } = target;
        this.setState(({ api, valid }) => {
            const changes = api;
            if (name === 'endpoint') {
                changes.endpointConfig = {
                    endpoint_type: 'http',
                    sandbox_endpoints: {
                        url: value,
                    },
                    production_endpoints: {
                        url: value,
                    },
                };
            } else {
                changes[name] = value;
            }
            // Checking validity.
            const validUpdated = { ...valid };
            validUpdated.name.empty = !api.name;
            validUpdated.context.empty = !api.context;
            validUpdated.version.empty = !api.version;
            validUpdated.endpointConfig.empty = !api.endpointConfig;
            return { api: changes, valid: validUpdated };
        });
    }

    /**
     *
     * @returns {React.Component} @inheritDoc
     * @memberof ApiCreateOpenAPI
     */
    render() {
        const {
            uploadMethod, valid, activeStep, api, openAPIFile,
        } = this.state;
        const steps = this.getSteps();
        const { classes } = this.props;
        return (
            <React.Fragment>
                <APICreateTopMenu />
                <Grid container spacing={24} className={classes.root}>
                    <Grid item xs={12} md={6}>
                        <Stepper activeStep={activeStep} className={classes.stepper}>
                            {steps.map((label) => {
                                const props = {};
                                const labelProps = {};

                                return (
                                    <Step key={label} {...props}>
                                        <StepLabel {...labelProps}>{label}</StepLabel>
                                    </Step>
                                );
                            })}
                        </Stepper>
                        <div>
                            {activeStep === 0 && (
                                <ProvideOpenAPI valid={valid} setOpenAPIFile={this.setOpenAPIFile} />
                            )}
                            {activeStep === 1 && (
                                <APIInputForm api={api} handleInputChange={this.inputChange} valid={valid} />
                            )}
                        </div>
                        <div>
                            <Button
                                disabled={activeStep === 0}
                                onClick={this.handleBack}
                                className={classes.button}
                            >
                                Back
                            </Button>
                            {activeStep !== steps.length - 1 ? (
                                <Button
                                    variant='contained'
                                    color='primary'
                                    onClick={this.handleNext}
                                    className={classes.button}
                                    disabled={
                                        (valid.openAPIFile.invalidFile && uploadMethod === 'file') ||
                                        (valid.openAPIUrl.invalidUrl && uploadMethod === 'url')
                                    }
                                >
                                    <FormattedMessage
                                        id='Apis.Create.OpenAPI.ApiCreateOpenAPI.next'
                                        defaultMessage='Next'
                                    />
                                </Button>
                            ) : (
                                <Button
                                    variant='contained'
                                    color='primary'
                                    onClick={this.handleFinish}
                                    className={classes.button}
                                    disabled={
                                        valid.name.empty || valid.version.empty || valid.context.empty
                                    }
                                >
                                    <FormattedMessage
                                        id='Apis.Create.OpenAPI.ApiCreateOpenAPI.finish'
                                        defaultMessage='Finish'
                                    />
                                </Button>
                            )}
                        </div>
                    </Grid>
                </Grid>
            </React.Fragment>
        );
    }
}

ApiCreateOpenAPI.propTypes = {
    intl: PropTypes.shape({
        formatMessage: PropTypes.func,
    }).isRequired,
    history: PropTypes.shape({
        push: PropTypes.func.isRequired,
    }).isRequired,
    classes: PropTypes.shape({}).isRequired,
    valid: PropTypes.shape({}).isRequired,
};

export default injectIntl(withStyles(styles)(ApiCreateOpenAPI));
