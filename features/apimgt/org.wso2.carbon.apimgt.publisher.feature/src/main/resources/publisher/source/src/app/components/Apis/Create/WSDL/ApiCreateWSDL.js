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
import {Grid, Paper, Button, Divider, Typography} from 'material-ui'
import Arrow from 'material-ui-icons/ArrowForward'
import {withStyles} from 'material-ui'
import {FormControl, Input, InputLabel, FormHelperText} from 'material-ui'
import CreateAPI from '../Endpoint/ApiCreateEndpoint'

import Alert from '../../../Shared/Alert'
import WSDLValidation from './Steps/WSDLValidation'
import WSDLFillAPIInfoStep from './Steps/WSDLFillAPIInfoStep'
import API from '../../../../data/api.js'

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
    }
});

class ApiCreateWSDL extends Component {
    constructor(props) {
        super(props);
        this.state = {
            validWSDL: false,
            doValidate: false,
            wsdlBean: null,
            currentStep: 1
        };
        this.submitWSDL = this.submitWSDL.bind(this);
        this.updateWSDLValidity = this.updateWSDLValidity.bind(this);
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

    render() {
        const {classes} = this.props;
        const {doValidate, currentStep, wsdlBean} = this.state;

        return (
            <div>
                <Grid container spacing={0} justify="center">
                    <Grid item xs={10}>
                        <Paper className={classes.paper}>
                            <Typography type="headline" gutterBottom>
                                Create API Using WSDL
                            </Typography>
                            <Divider />
                            {currentStep === 1 ? (
                                <WSDLValidation updateWSDLValidity={this.updateWSDLValidity} validate={doValidate}/>
                            ) : (
                                <CreateAPI/>
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
        );
    }
}

export default withStyles(styles)(ApiCreateWSDL)