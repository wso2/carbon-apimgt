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
import { Grid, Button, Typography, withStyles } from 'material-ui';
import Stepper, { Step, StepLabel, StepContent } from 'material-ui/Stepper';
import Card, { CardContent } from 'material-ui/Card';
import PropTypes from 'prop-types';

import InputForm from '../Endpoint/InputForm';
import ProvideWSDL from './Steps/ProvideWSDL';
import API from '../../../../data/api';
import Alert from '../../../Shared/Alert';
import BindingInfo from './BindingInfo';

const styles = theme => ({
    button: {
        margin: theme.spacing.unit,
    },
    leftIcon: {
        marginRight: theme.spacing.unit,
    },
    rightIcon: {
        marginLeft: theme.spacing.unit,
    },
    paper: {
        padding: theme.spacing.unit * 2,
    },
    actionsContainer: {
        marginTop: theme.spacing.unit,
        marginBottom: theme.spacing.unit,
    },
    resetContainer: {
        marginTop: 0,
        padding: theme.spacing.unit * 3,
    },
    mainCol: {
        'margin-top': '6em',
    },
    textWelcome: {
        'font-weight': 300,
    },
    apiCreate: {
        'margin-top': '30px',
    },
    stepLabel: {
        'font-weight': 300,
        'font-size': '24px',
        color: 'rgba(0, 0, 0, 0.87)',
        'line-height': '1.35417em',
    },
    radioGroup: {
        'margin-left': '0px',
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
            apiFields: {
                apiVersion: '1.0.0',
            },
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
    updateApiInputs(event) {
        let name = 'selectedPolicies';
        let value = event;
        if (!Array.isArray(event)) {
            ({ name, value } = event.target.name);
        }
        const { apiFields } = this.state;
        apiFields[name] = value;
        this.setState({ apiFields });
    }

    /**
     * Make API POST call and create send WSDL file or URL
     * @memberof ApiCreateWSDL
     */
    createWSDLAPI() {
        const newApi = new API();
        const { wsdlBean, apiFields } = this.state;
        const {
            apiName, apiVersion, apiContext, apiEndpoint, implementationType,
        } = apiFields;
        const uploadMethod = wsdlBean.url ? 'url' : 'file';
        const apiAttributes = {
            name: apiName,
            version: apiVersion,
            context: apiContext,
            endpoint: API.getEndpoints(apiName, apiVersion, apiEndpoint),
        };
        const apiData = {
            additionalProperties: JSON.stringify(apiAttributes),
            implementationType,
            [uploadMethod]: wsdlBean[uploadMethod],
        };

        newApi
            .importWSDL(apiData)
            .then((response) => {
                Alert.success('API Created Successfully. Now you can add/modify resources, define endpoints etc..');
                const uuid = response.obj.id;
                const redirectURL = '/apis/' + uuid + '/overview';
                this.props.history.push(redirectURL);
            })
            .catch((errorResponse) => {
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
            doValidate, currentStep, wsdlBean, apiFields,
        } = this.state;
        const uploadMethod = wsdlBean.url ? 'url' : 'file';
        const provideWSDLProps = {
            uploadMethod,
            [uploadMethod]: wsdlBean[uploadMethod],
            updateWSDLValidity: this.updateWSDLValidity,
            validate: doValidate,
        };
        return (
            <Grid container spacing={0} justify='center'>
                <Grid item xs={8}>
                    <Typography type='display1' className={classes.textWelcome}>
                        Design a new REST API using WSDL
                    </Typography>
                    <Card className={classes.apiCreate}>
                        <CardContent>
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
                                            <Button>Cancel</Button>
                                            <Button raised color='primary' onClick={this.nextStep}>
                                                Next
                                            </Button>
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
                                        <InputForm apiFields={apiFields} handleInputChange={this.updateApiInputs} />
                                        <Grid item xs={10}>
                                            <BindingInfo wsdlBean={wsdlBean} classes={classes} apiFields={apiFields} />
                                        </Grid>
                                        <div className={classes.optionAction}>
                                            <Button color='primary' onClick={this.stepBack}>
                                                Back
                                            </Button>
                                            <Button raised color='primary' onClick={this.createWSDLAPI}>
                                                Create
                                            </Button>
                                        </div>
                                    </StepContent>
                                </Step>
                            </Stepper>
                        </CardContent>
                    </Card>
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
