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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import React, { Component } from 'react';
/* MUI Imports */
import { Grid, Button, Typography } from '@material-ui/core';
import { withStyles } from '@material-ui/core/styles';
import Stepper from '@material-ui/core/Stepper';
import Step from '@material-ui/core/Step';
import StepLabel from '@material-ui/core/StepLabel';
import StepContent from '@material-ui/core/StepContent';
import Paper from '@material-ui/core/Paper';
import PropTypes from 'prop-types';
import API from 'AppData/api';
import Alert from 'AppComponents/Shared/Alert';
import InputForm from 'AppComponents/Apis/Create/Endpoint/APIInputForm';
import Progress from 'AppComponents/Shared/Progress';

import ProvideWSDL from './Steps/ProvideWSDL';
import BindingInfo from './BindingInfo';

const styles = theme => ({
    root: {
        flexGrow: 1,
    },
    paper: {
        padding: theme.spacing.unit * 2,
    },
    textWelcome: {
        'font-weight': 300,
    },
    stepLabel: {
        'font-weight': 300,
        'font-size': '24px',
        color: 'rgba(0, 0, 0, 0.87)',
        'line-height': '1.35417em',
    },
    optionAction: {
        'margin-top': '1.5em',
    },
    optionContent: {
        width: '100%',
        'margin-bottom': '1em',
        'margin-right': '.5em',
    },
    optionContent50: {
        width: '45%',
    },
});

/**
 * Component for creating an API using WSDL file or URL
 * @class ApiCreateWSDL
 * @extends {Component}
 */
class ApiCreateWSDL extends Component {
    /**
     * Creates an instance of ApiCreateWSDL.
     * @param {any} props @inheritDoc
     * @memberof ApiCreateWSDL
     */
    constructor(props) {
        super(props);
        this.state = {
            doValidate: false,
            wsdlBean: {},
            currentStep: 0,
            api: new API('', 'v1.0.0'),
            loading: false,
        };
        this.updateWSDLValidity = this.updateWSDLValidity.bind(this);
        this.updateApiInputs = this.updateApiInputs.bind(this);
        this.nextStep = this.nextStep.bind(this);
        this.stepBack = this.stepBack.bind(this);
        this.createWSDLAPI = this.createWSDLAPI.bind(this);
    }

    /**
     * Go to next step in mini wizard
     * @memberof ApiCreateWSDL
     */
    nextStep() {
        const { currentStep } = this.state;
        if (currentStep === 0) {
            this.setState({ doValidate: true });
        } else {
            this.setState({
                currentStep: this.state.currentStep + 1,
            });
        }
    }

    /**
     * Go step back word in the Wizard
     * @memberof ApiCreateWSDL
     */
    stepBack() {
        this.setState({
            currentStep: this.state.currentStep - 1,
        });
    }

    /**
     * Check the WSDL file or URL validity through REST API
     * @param {Boolean} validity Validity of the WSDL
     * @param {Object} wsdlBean Bean object holding WSDL file/url info
     * @memberof ApiCreateWSDL
     */
    updateWSDLValidity(validity, wsdlBean) {
        let { currentStep } = this.state;
        if (validity) {
            currentStep = 1;
        }
        this.setState({
            wsdlBean,
            doValidate: false,
            currentStep,
        });
    }

    /**
     * Update user inputs in the form with onChange event trigger
     * @param {React.SyntheticEvent} event Event containing user action
     * @memberof ApiCreateWSDL
     */
    updateApiInputs({ target }) {
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
                            type: 'soap',
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
                            type: 'soap',
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
     * Make API POST call and create send WSDL file or URL
     * @memberof ApiCreateWSDL
     */
    createWSDLAPI() {
        this.setState({ loading: true });
        const newApi = new API();
        const { wsdlBean, api } = this.state;
        const {
            name, version, context, endpoint, implementationType,
        } = api;
        const uploadMethod = wsdlBean.url ? 'url' : 'file';
        const apiAttributes = {
            name,
            version,
            context,
            endpoint,
        };
        const apiData = {
            additionalProperties: JSON.stringify(apiAttributes),
            implementationType,
            [uploadMethod]: wsdlBean[uploadMethod],
        };

        newApi
            .importWSDL(apiData)
            .then((response) => {
                Alert.success(`${name} API Created Successfully.`);
                const uuid = response.obj.id;
                const redirectURL = '/apis/' + uuid + '/overview';
                this.setState({ loading: false });
                this.props.history.push(redirectURL);
            })
            .catch((errorResponse) => {
                this.setState({ loading: false });
                console.error(errorResponse);
                const error = errorResponse.response.obj;
                const messageTxt = 'Error[' + error.code + ']: ' + error.description + ' | ' + error.message + '.';
                Alert.error(messageTxt);
            });
    }

    /**
     * @inheritDoc
     * @returns {React.Component} API Create using WSDL
     * @memberof ApiCreateWSDL
     */
    render() {
        const { classes } = this.props;
        const {
            doValidate, currentStep, wsdlBean, api, loading,
        } = this.state;
        const uploadMethod = wsdlBean.url ? 'url' : 'file';
        const provideWSDLProps = {
            uploadMethod,
            [uploadMethod]: wsdlBean[uploadMethod],
            updateWSDLValidity: this.updateWSDLValidity,
            validate: doValidate,
        };
        return (
            <Grid container className={classes.root} spacing={0} justify='center'>
                <Grid item md={10}>
                    <Paper className={classes.paper}>
                        <Typography type='display1' className={classes.textWelcome}>
                            Design a new REST API using WSDL
                        </Typography>

                        <Stepper activeStep={currentStep} orientation='vertical'>
                            <Step key='provideWSDL'>
                                <StepLabel>
                                    <Typography
                                        type='headline'
                                        gutterBottom
                                        className={classes.stepLabel}
                                        component='span'
                                    >
                                        Select WSDL
                                    </Typography>
                                </StepLabel>
                                <StepContent>
                                    <ProvideWSDL {...provideWSDLProps} />
                                    <div className={classes.optionAction}>
                                        <Button color='primary' onClick={this.nextStep}>
                                            Next
                                        </Button>
                                        <Button>Cancel</Button>
                                    </div>
                                </StepContent>
                            </Step>
                            <Step key='createAPI'>
                                <StepLabel className={classes.stepLabel}>
                                    <Typography
                                        type='headline'
                                        gutterBottom
                                        className={classes.stepLabel}
                                        component='span'
                                    >
                                        Create API
                                    </Typography>
                                </StepLabel>
                                <StepContent>
                                    <Typography type='caption' gutterBottom align='left'>
                                        You can configure the advanced configurations later
                                    </Typography>
                                    <InputForm api={api} handleInputChange={this.updateApiInputs} />
                                    <Grid item xs={10}>
                                        <BindingInfo
                                            updateApiInputs={this.updateApiInputs}
                                            wsdlBean={wsdlBean}
                                            classes={classes}
                                            api={api}
                                        />
                                    </Grid>
                                    <div className={classes.optionAction}>
                                        <Button disabled={loading} color='primary' onClick={this.createWSDLAPI}>
                                            {loading && <Progress />}
                                            Create
                                        </Button>
                                        <Button color='primary' onClick={this.stepBack}>
                                            Back
                                        </Button>
                                    </div>
                                </StepContent>
                            </Step>
                        </Stepper>
                    </Paper>
                </Grid>
            </Grid>
        );
    }
}

ApiCreateWSDL.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    history: PropTypes.shape({
        push: PropTypes.func,
    }).isRequired,
};

export default withStyles(styles)(ApiCreateWSDL);
