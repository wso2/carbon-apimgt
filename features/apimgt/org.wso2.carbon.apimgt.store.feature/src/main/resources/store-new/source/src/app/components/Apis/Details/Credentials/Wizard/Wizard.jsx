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
import { withStyles } from '@material-ui/core/styles';
import Toolbar from '@material-ui/core/Toolbar';
import IconButton from '@material-ui/core/IconButton';
import CloseIcon from '@material-ui/icons/Close';
import Grid from '@material-ui/core/Grid';
import AppBar from '@material-ui/core/AppBar';
import { Typography } from '@material-ui/core';
import Stepper from '@material-ui/core/Stepper';
import Step from '@material-ui/core/Step';
import StepLabel from '@material-ui/core/StepLabel';
import Button from '@material-ui/core/Button';
import { Redirect } from 'react-router-dom';
import { FormattedMessage, injectIntl } from 'react-intl';
import CreateAppStep from './CreateAppStep';
import SubscribeToAppStep from './SubscribeToAppStep';
import GenerateKeysStep from './GenerateKeysStep';
import GenerateAccessTokenStep from './GenerateAccessTokenStep';
import CopyAccessTokenStep from './CopyAccessTokenStep';

const styles = theme => ({
    appBar: {
        background: theme.palette.background.paper,
        color: theme.palette.getContrastText(theme.palette.background.paper),
    },
    toolbar: {
        marginLeft: theme.spacing.unit * 2,
    },
    subscribeTitle: {
        flex: 1,
    },
    plainContent: {
        paddingTop: 80,
        paddingLeft: theme.spacing.unit * 2,
    },
    button: {
        marginTop: theme.spacing.unit * 2,
        marginRight: theme.spacing.unit,
    },
    group: {
        display: 'flex',
        flexDirection: 'row',
    },
    instructions: {
        marginTop: theme.spacing.unit,
        marginBottom: theme.spacing.unit,
        'font-size': theme.spacing.unit * 2,
    },
    root: {
        paddingLeft: theme.spacing.unit,
    },
    wizardContent: {
        paddingLeft: theme.spacing.unit,
    },
    wizardButtons: {
        paddingLeft: theme.spacing.unit * 2,
    },
});

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
            nextStep: 0,
            createdApp: null,
            createdToken: null,
            redirect: false,
            createdKeyType: '',
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
     * Increment the current step or next step by 1
     * @param {*} type step type
     */
    handleNext = (type) => {
        switch (type) {
            case 'current':
                this.setState(({ currentStep }) => {
                    return { currentStep: currentStep + 1 };
                });
                break;
            case 'next':
                this.setState(({ nextStep }) => {
                    return { nextStep: nextStep + 1 };
                });
                break;
            default:
                break;
        }
    }

    /**
     * Decrement the next step by 1
     */
    handleBack = () => {
        this.setState(({ nextStep }) => {
            return { nextStep: nextStep - 1 };
        });
    }

    handleReset = () => {
        this.setState({
            currentStep: 0,
            nextStep: 0,
        });
    };

    /**
     * Set state.redirect to true to redirect to the API console page
     * @memberof Wizard
     */
    handleRedirectTest = () => {
        this.setState({ redirect: true });
    }

    /**
     * @inheritdoc
     */
    render() {
        const {
            classes, updateSubscriptionData, apiId, handleClickToggle,
            throttlingPolicyList,
        } = this.props;
        const {
            currentStep, createdApp, createdToken, redirect, nextStep, createdKeyType, stepStatus,
        } = this.state;

        if (redirect) {
            return <Redirect push to={'/apis/' + apiId + '/test'} />;
        }
        return (
            <React.Fragment>
                <AppBar className={classes.appBar}>
                    <Grid container spacing={0}>
                        <Grid item xs={6}>
                            <Toolbar className={classes.toolbar}>
                                <IconButton
                                    color='inherit'
                                    onClick={() => handleClickToggle('openNew', updateSubscriptionData)}
                                    aria-label='Close'
                                >
                                    <CloseIcon />
                                </IconButton>
                                <div className={classes.subscribeTitle}>
                                    <Typography variant='h6'>
                                        <FormattedMessage
                                            id='Apis.Details.Credentials.Wizard.Wizard.subscribe.to.new.application'
                                            defaultMessage='Subscribe to new Application'
                                        />
                                    </Typography>
                                </div>
                            </Toolbar>
                        </Grid>
                    </Grid>
                </AppBar>
                <div className={classes.plainContent}>
                    <div className={classes.root}>
                        <Stepper activeStep={currentStep}>
                            {this.steps.map((label) => {
                                return (
                                    <Step key={label}>
                                        <StepLabel>{label}</StepLabel>
                                    </Step>
                                );
                            })}
                        </Stepper>
                    </div>
                    <div>
                        {nextStep === this.steps.length ? (
                            <div>
                                <Typography className={classes.instructions}>
                                    <FormattedMessage
                                        id='Apis.Details.Credentials.Wizard.Wizard.all.steps.completed'
                                        defaultMessage='All steps completed!'
                                    />
                                </Typography>
                                <Button
                                    onClick={this.handleReset}
                                    className={classes.button}
                                >
                                    <FormattedMessage
                                        id='Apis.Details.Credentials.Wizard.Wizard.rest'
                                        defaultMessage='Reset'
                                    />
                                </Button>
                            </div>
                        ) : (
                            <div className={classes.wizardContent}>
                                {stepStatus === this.stepStatuses.PROCEED && (
                                    <React.Fragment>
                                        <CreateAppStep
                                            currentStep={currentStep}
                                            setCreatedApp={this.setCreatedApp}
                                            nextStep={nextStep}
                                            incrementStep={this.handleNext}
                                            decrementStep={this.handleBack}
                                            setStepStatus={this.setStepStatus}
                                            stepStatuses={this.stepStatuses}
                                        />
                                        <SubscribeToAppStep
                                            throttlingPolicyList={throttlingPolicyList}
                                            currentStep={currentStep}
                                            createdApp={createdApp}
                                            apiId={apiId}
                                            nextStep={nextStep}
                                            incrementStep={this.handleNext}
                                            decrementStep={this.handleBack}
                                            setStepStatus={this.setStepStatus}
                                            stepStatuses={this.stepStatuses}
                                        />
                                        <GenerateKeysStep
                                            currentStep={currentStep}
                                            createdApp={createdApp}
                                            nextStep={nextStep}
                                            incrementStep={this.handleNext}
                                            decrementStep={this.handleBack}
                                            setCreatedKeyType={this.setCreatedKeyType}
                                            setStepStatus={this.setStepStatus}
                                            stepStatuses={this.stepStatuses}
                                        />
                                        <GenerateAccessTokenStep
                                            currentStep={currentStep}
                                            createdApp={createdApp}
                                            setCreatedToke={this.setCreatedToken}
                                            nextStep={nextStep}
                                            incrementStep={this.handleNext}
                                            decrementStep={this.handleBack}
                                            createdKeyType={createdKeyType}
                                        />
                                        <CopyAccessTokenStep
                                            currentStep={currentStep}
                                            createdToken={createdToken}
                                            handleClickToggle={handleClickToggle}
                                            updateSubscriptionData={updateSubscriptionData}
                                            nextStep={nextStep}
                                            incrementStep={this.handleNext}
                                            stepStatus={stepStatus}
                                        />
                                        <div className={classes.wizardButtons}>
                                            <Button
                                                disabled={currentStep < this.steps.length - 1}
                                                onClick={this.handleRedirectTest}
                                                className={classes.button}
                                                variant='outlined'
                                            >
                                                <FormattedMessage
                                                    id='Apis.Details.Credentials.Wizard.Wizard.test'
                                                    defaultMessage='Test'
                                                />
                                            </Button>
                                            <Button
                                                variant='contained'
                                                color='primary'
                                                onClick={() => this.handleNext('next')}
                                                className={classes.button}
                                            >
                                                {currentStep === this.steps.length - 1
                                                    ? (
                                                        <FormattedMessage
                                                            id='Apis.Details.Credentials.Wizard.Wizard.finish'
                                                            defaultMessage='Finish'
                                                        />
                                                    )
                                                    : (
                                                        <FormattedMessage
                                                            id='Apis.Details.Credentials.Wizard.Wizard.next'
                                                            defaultMessage='Next'
                                                        />
                                                    )}
                                            </Button>
                                        </div>
                                    </React.Fragment>
                                )}
                                {stepStatus === this.stepStatuses.BLOCKED && (
                                    <Typography variant='h4'>
                                        <FormattedMessage
                                            id='Apis.Details.Credentials.Wizard.Wizard.approval.request.for.this.step'
                                            defaultMessage='Approval request for this step has been Sent'
                                        />
                                    </Typography>
                                )}
                            </div>
                        )}
                    </div>
                </div>
            </React.Fragment>
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
        wizardButtons: PropTypes.string,
    }).isRequired,
    updateSubscriptionData: PropTypes.func.isRequired,
    handleClickToggle: PropTypes.func.isRequired,
    intl: PropTypes.func.isRequired,
    apiId: PropTypes.string.isRequired,
    throttlingPolicyList: PropTypes.arrayOf(PropTypes.string).isRequired,
};

export default injectIntl(withStyles(styles)(Wizard));
