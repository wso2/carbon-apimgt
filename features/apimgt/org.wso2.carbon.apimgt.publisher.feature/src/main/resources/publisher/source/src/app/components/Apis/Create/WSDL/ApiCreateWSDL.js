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
import React, {Component} from 'react';
/* MUI Imports */
import Arrow from 'material-ui-icons/ArrowForward'
import {withStyles} from 'material-ui'
import {Grid, Paper, Button, Divider, Typography} from 'material-ui'
import Stepper, {Step, StepLabel, StepContent} from 'material-ui/Stepper';
import {FormControl, Input, InputLabel, FormHelperText, FormControlLabel} from 'material-ui'
import InputForm from '../Endpoint/InputForm'
import PropTypes from 'prop-types';
import Alert from '../../../Shared/Alert'
import ProvideWSDL from './Steps/ProvideWSDL'
import WSDLFillAPIInfoStep from './Steps/WSDLFillAPIInfoStep'
import API from '../../../../data/api.js'


import Card, {CardActions, CardContent} from 'material-ui/Card';
import Radio, {RadioGroup} from 'material-ui/Radio';


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
        padding: theme.spacing.unit * 3
    },
    mainCol: {
        'margin-top': '6em'
    },
    textWelcome: {
        'font-weight': 300
    },
    apiCreate: {
        'margin-top': '30px'
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
    }
});

class ApiCreateWSDL extends Component {
    constructor(props) {
        super(props);
        this.state = {
            validWSDL: false,
            doValidate: false,
            wsdlBean: null,
            currentStep: 0,
            apiFields: {
                apiVersion: "1.0.0"
            }
        };
        this.submitWSDL = this.submitWSDL.bind(this);
        this.updateWSDLValidity = this.updateWSDLValidity.bind(this);
        this.updateApiInputs = this.updateApiInputs.bind(this);
        this.nextStep = this.nextStep.bind(this);
        this.stepBack = this.stepBack.bind(this);
    }

    nextStep() {
        this.setState({
            currentStep: this.state.currentStep + 1,
        });
    }

    stepBack() {
        this.setState({
            currentStep: this.state.currentStep - 1,
        });
    }

    submitWSDL() {
        this.setState({doValidate: true});
    }

    updateWSDLValidity(validity, wsdlBean) {
        let currentStep = this.state.currentStep;
        if (validity) {
            currentStep = 2;
        }
        this.setState({validWSDL: validity, wsdlBean: wsdlBean, doValidate: false, currentStep: currentStep});
    }

    updateApiInputs(event) {
        let field = 'selectedPolicies';
        let value = event;
        if (event.constructor.name === 'SyntheticEvent') {
            field = event.target.name;
            value = event.target.value;
        }
        const apiFields = this.state.apiFields;
        apiFields[field] = value;
        this.setState({apiFields: apiFields});
    }

    render() {
        const {classes} = this.props;
        const {doValidate, currentStep, wsdlBean, apiFields} = this.state;

        /*return (
         <div>
         <Grid container spacing={0} justify="center">
         <Grid item xs={10}>
         <Paper className={classes.paper}>
         <Typography type="headline" gutterBottom>
         Create API Using WSDL
         </Typography>
         <Divider />
         {currentStep === 1 ? (
         <ProvideWSDL updateWSDLValidity={this.updateWSDLValidity} validate={doValidate}/>
         ) : (
         <InputForm apiFields={apiFields} handleInputChange={this.updateApiInputs}/>
         )}

         <Divider />
         <Grid container spacing={0} justify="flex-end">
         <Button className={classes.button} onClick={this.submitWSDL} raised color="primary">
         Next
         <Arrow className={classes.rightIcon}>send</Arrow>
         </Button>
         </Grid>
         </Paper>
         </Grid>
         </Grid>
         </div>
         );*/

        return (
            <Grid container spacing={0} justify="center">
                <Grid item xs={8}>
                    <Typography type="display1" className={classes.textWelcome}>
                        Design new REST API using WSDL
                    </Typography>
                    <Card className={classes.apiCreate}>
                        <CardContent>
                            <Stepper activeStep={currentStep} orientation="vertical">
                                <Step key='provideWSDL'>
                                    <StepLabel >
                                        <Typography type="headline" gutterBottom className={classes.stepLabel}
                                                    component='span'>
                                            Select WSDL
                                        </Typography>
                                    </StepLabel>
                                    <StepContent>
                                        <ProvideWSDL updateWSDLValidity={this.updateWSDLValidity}
                                                        validate={doValidate}/>
                                        <div className={classes.optionAction}>
                                            <Button>
                                                Cancel
                                            </Button>
                                            <Button raised color="primary" onClick={this.nextStep}>
                                                Next
                                            </Button>
                                        </div>
                                    </StepContent>
                                </Step>
                                <Step key='createAPI'>
                                    <StepLabel className={classes.stepLabel}>
                                        <Typography type="headline" gutterBottom className={classes.stepLabel}
                                                    component='span'>
                                            Create API
                                        </Typography>
                                    </StepLabel>
                                    <StepContent>
                                        <Typography type="caption" gutterBottom align="left">
                                            You can configure the advance configurations later
                                        </Typography>
                                        <InputForm apiFields={apiFields}
                                                   handleInputChange={this.updateApiInputs}/>
                                        <div className={classes.optionAction}>
                                            <Button color="primary" onClick={this.stepBack}>
                                                Back
                                            </Button>
                                            <Button raised color="primary" onClick={this.nextStep}>
                                                Create
                                            </Button>
                                        </div>
                                    </StepContent>
                                </Step>
                            </Stepper>
                        </CardContent>
                    </Card>
                </Grid>
            </Grid>)
    }
}

export default withStyles(styles)(ApiCreateWSDL)