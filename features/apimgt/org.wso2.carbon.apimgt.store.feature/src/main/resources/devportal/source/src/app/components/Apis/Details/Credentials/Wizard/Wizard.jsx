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
import PropTypes from 'prop-types';
import Grid from '@material-ui/core/Grid';
import { Typography, Paper, Box } from '@material-ui/core';
import Stepper from '@material-ui/core/Stepper';
import Step from '@material-ui/core/Step';
import StepLabel from '@material-ui/core/StepLabel';
import { FormattedMessage, injectIntl } from 'react-intl';
import { withStyles } from '@material-ui/core/styles';
import CreateAppStep from './CreateAppStep';
import SubscribeToAppStep from './SubscribeToAppStep';
import GenerateKeysStep from './GenerateKeysStep';
import GenerateAccessTokenStep from './GenerateAccessTokenStep';
import CopyAccessTokenStep from './CopyAccessTokenStep';

const styles = (theme) => ({
    paper: {
        marginLeft: theme.spacing(3),
    },
    titleSub: {
        marginLeft: theme.spacing(3),
        paddingTop: theme.spacing(2),
        paddingBottom: theme.spacing(2),
        color: theme.palette.getContrastText(theme.palette.background.default),
    },
    boxWrapper: {
        '& h5, & label, & td, & li, & div, & input, & p.MuiFormHelperText-root': {
            color: theme.palette.getContrastText(theme.palette.background.paper),
        },
        '& .MuiButton-containedPrimary span.MuiButton-label': {
            color: theme.palette.getContrastText(theme.palette.primary.main),
        },
    },

});

const stepComponents = [CreateAppStep, SubscribeToAppStep, GenerateKeysStep,
    GenerateAccessTokenStep, CopyAccessTokenStep];

/**
 * Class used for wizard
 */
class Wizard extends Component {
    /**
     * @param {*} props properties
     */
    constructor(props) {
        super(props);
        const { intl } = this.props;
        this.steps = [
            intl.formatMessage({
                defaultMessage: 'Create application',
                id: 'Apis.Details.Credentials.Wizard.Wizard.create',
            }),
            intl.formatMessage({
                defaultMessage: 'Subscribe to new application',
                id: 'Apis.Details.Credentials.Wizard.Wizard.subscribe.to.new.application',
            }),
            intl.formatMessage({
                defaultMessage: 'Generate Keys',
                id: 'Apis.Details.Credentials.Wizard.Wizard.generate.keys',
            }),
            intl.formatMessage({
                defaultMessage: 'Generate Access Token',
                id: 'Apis.Details.Credentials.Wizard.Wizard.generate.access.token',
            }),
            intl.formatMessage({
                defaultMessage: 'Copy Access Token',
                id: 'Apis.Details.Credentials.Wizard.Wizard.copy.access.token',
            }),
        ];
        this.stepStatuses = {
            PROCEED: 'PROCEED',
            BLOCKED: 'BLOCKED',
        };
        this.state = {
            currentStep: 0,
            createdApp: null,
            createdToken: null,
            redirect: false,
            createdKeyType: '',
            createdSelectedTab: '',
            stepStatus: 'PROCEED',
        };
    }

    /**
     * Used to set the status retured after executing each step. Used in workflow
     * scenario to evaluate wheather we can proceed to next step
     * @param {*} stepStatus status
     */
    setStepStatus = (stepStatus) => {
        this.setState({ stepStatus });
    }

    /**
     * Set the created app from step 1
     * @param {*} createdApp app created
     */
    setCreatedApp = (createdApp) => {
        this.setState({ createdApp });
    }

    /**
     * Set the created token from step 4
     * @param {*} createdToken token created
     */
    setCreatedToken = (createdToken) => {
        this.setState({ createdToken });
    }

    /**
     * Set the created keytype from step 3
     * @param {*} createdKeyType token created
     */
    setCreatedKeyType = (createdKeyType) => {
        this.setState({ createdKeyType });
    }

    /**
     * Set the created selectedTab from step 3
     * @param {*} selectedTab token created
     */
    setCreatedSelectedTab = (createdSelectedTab) => {
        this.setState({ createdSelectedTab });
    }

    /**
     * Increment the current step or next step by 1
     */
    handleNext = () => {
        this.setState(({ currentStep }) => {
            return { currentStep: currentStep + 1 };
        });
    }

    /**
     * Rest the currentStep to 0 and bring wizard back to first step
     * @memberof Wizard
     */
    handleReset = () => {
        this.setState({
            currentStep: 0,
        });
    };

    /**
     * @inheritdoc
     */
    render() {
        const { classes } = this.props;
        const { currentStep, stepStatus } = this.state;
        const CurrentStepComponent = stepComponents[currentStep];
        return (
            <>
                <Typography variant='h4' component='h2' className={classes.titleSub}>
                    <FormattedMessage
                        id={'Apis.Details.Credentials.Credentials'
                    + '.api.credentials.generate'}
                        defaultMessage='Subscription &amp; Key Generation Wizard'
                    />
                </Typography>
                <Paper elevation={0} className={classes.paper}>
                    <Box py={1} mx='auto' display='flex'>
                        <Grid item xs={12} md={12}>
                            <Stepper activeStep={currentStep}>
                                {this.steps.map((label) => {
                                    return (
                                        <Step key={label}>
                                            <StepLabel>{label}</StepLabel>
                                        </Step>
                                    );
                                })}
                            </Stepper>
                        </Grid>
                    </Box>
                    <Box py={1} mx='auto' display='block' className={classes.boxWrapper}>
                        {stepStatus === this.stepStatuses.PROCEED && (
                            <>
                                <CurrentStepComponent
                                    {...this.state}
                                    incrementStep={this.handleNext}
                                    setStepStatus={this.setStepStatus}
                                    stepStatuses={this.stepStatuses}
                                    classes={classes}
                                    setCreatedApp={this.setCreatedApp}
                                    setCreatedKeyType={this.setCreatedKeyType}
                                    setCreatedSelectedTab={this.setCreatedSelectedTab}
                                    setCreatedToken={this.setCreatedToken}
                                    handleReset={this.handleReset}
                                />
                            </>
                        )}
                    </Box>
                    <Box py={1} mb={1} mx='auto' display='flex'>
                        {stepStatus === this.stepStatuses.BLOCKED && (
                            <Box pt={2} px={3} display='flex'>
                                <Typography variant='h5' component='label'>
                                    <FormattedMessage
                                        id={'Apis.Details.Credentials.Wizard.Wizard.approval.request.'
                                                + 'for.this.step.has'}
                                        defaultMessage='A request to register this step has been sent.'
                                    />
                                </Typography>
                            </Box>
                        )}
                    </Box>
                </Paper>
            </>
        );
    }
}

Wizard.propTypes = {
    classes: PropTypes.shape({
        appBar: PropTypes.string,
        toolbar: PropTypes.string,
        subscribeTitle: PropTypes.string,
        plainContent: PropTypes.string,
        root: PropTypes.string,
        instructions: PropTypes.string,
        button: PropTypes.string,
        wizardContent: PropTypes.string,
    }).isRequired,
    intl: PropTypes.func.isRequired,
    throttlingPolicyList: PropTypes.arrayOf(PropTypes.string).isRequired,
};

export default injectIntl(withStyles(styles)(Wizard));
