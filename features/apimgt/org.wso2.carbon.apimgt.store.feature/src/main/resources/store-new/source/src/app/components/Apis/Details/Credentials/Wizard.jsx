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
import { Redirect } from 'react-router-dom';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Stepper from '@material-ui/core/Stepper';
import Step from '@material-ui/core/Step';
import StepLabel from '@material-ui/core/StepLabel';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';

import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';

import ApplicationCreate from '../../../Shared/AppsAndKeys/ApplicationCreate';
import SubscribeToApi from '../../../Shared/AppsAndKeys/SubscribeToApi';
import Keys from '../../../Shared/AppsAndKeys/KeyConfiguration';
import Tokens from '../../../Shared/AppsAndKeys/Tokens';
import ViewToken from '../../../Shared/AppsAndKeys/ViewToken';

import { ApiContext } from '../ApiContext';

/**
 *
 *
 * @returns
 */
function getSteps() {
    return ['Create application', 'Subscribe to new application', 'Generate Keys', 'Generate Access Token', 'Copy Access Token'];
}

/**
 *
 *
 * @param {*} theme
 */
const styles = theme => ({
    group: {
        display: 'flex',
        flexDirection: 'row',
    },
    instructions: {
        marginTop: theme.spacing.unit,
        marginBottom: theme.spacing.unit,
    },
    root: {
        paddingLeft: theme.spacing.unit,
    },
    wizardContent: {
        paddingLeft: theme.spacing.unit,
    },
    button: {
        marginTop: theme.spacing.unit * 2,
        marginRight: theme.spacing.unit,
    },
    wizardButtons: {
        paddingLeft: theme.spacing.unit * 2,
    },
});
/**
 *
 *
 * @class Wizard
 * @extends {React.Component}
 */
class Wizard extends React.Component {
    constructor(props) {
        super(props);
        this.getStepContent = this.getStepContent.bind(this);
        this.handleNext = this.handleNext.bind(this);
        this.handleRedirectTest = this.handleRedirectTest.bind(this);
    }

    state = {
        value: 'wizard',
        activeStep: 0,
        appId: null,
        tab: 0,
        redirect: false,
    };

    /**
     *
     *
     * @memberof Wizard
     */
    handleTabChange = (event, tab) => {
        this.setState({ tab });
    };

    /**
     *
     *
     * @param {*} step
     * @param {*} api
     * @param {*} applicationsAvailable
     * @returns
     * @memberof Wizard
     */
    getStepContent(step, api, applicationsAvailable) {
        switch (step) {
            case 0:
                return <ApplicationCreate innerRef={node => (this.applicationCreate = node)} />;
            case 1:
                return <SubscribeToApi innerRef={node => (this.subscribeToApi = node)} newApp={this.newApp} api={api} applicationsAvailable={applicationsAvailable} />;
            case 2:
                return (
                    <React.Fragment>
                        <Tabs value={this.state.tab} onChange={this.handleTabChange} fullWidth indicatorColor='secondary' textColor='secondary'>
                            <Tab label='PRODUCTION' />
                            <Tab label='SANDBOX' />
                        </Tabs>
                        {this.state.tab === 0 && (
                            <div>
                                <Keys innerRef={node => (this.keys = node)} selectedApp={this.newApp} keyType='PRODUCTION' />
                            </div>
                        )}
                        {this.state.tab === 1 && (
                            <div>
                                <Keys innerRef={node => (this.keys = node)} selectedApp={this.newApp} keyType='SANDBOX' />
                            </div>
                        )}
                    </React.Fragment>
                );
            case 3:
                return (
                    <React.Fragment>
                        <Tabs
                            value={this.state.tab}
                            // onChange={this.handleTabChange}
                            fullWidth
                            indicatorColor='secondary'
                            textColor='secondary'
                        >
                            <Tab label='PRODUCTION' />
                            <Tab label='SANDBOX' />
                        </Tabs>
                        {this.state.tab === 0 && (
                            <div>
                                <Tokens innerRef={node => (this.tokens = node)} selectedApp={this.newApp} keyType='PRODUCTION' />
                            </div>
                        )}
                        {this.state.tab === 1 && (
                            <div>
                                <Tokens innerRef={node => (this.tokens = node)} selectedApp={this.newApp} keyType='SANDBOX' />
                            </div>
                        )}
                    </React.Fragment>
                );
            case 4:
                return <ViewToken token={this.token} />;
            default:
                return 'Unknown step';
        }
    }

    /**
     *
     *
     * @memberof Wizard
     */
    isStepOptional = (step) => {
        return step === 1;
    };

    /**
     *
     *
     * @memberof Wizard
     */
    handleNext = () => {
        const { activeStep } = this.state;

        const that = this;
        if (activeStep === 0) {
            // create application step
            const promised_create = this.applicationCreate.handleSubmit();
            if (promised_create) {
                promised_create
                    .then((response) => {
                        const appCreated = JSON.parse(response.data);
                        that.newApp = { value: appCreated.applicationId, label: appCreated.name };
                        // Once application loading fixed this need to pass application ID and load app
                        that.setState({
                            activeStep: activeStep + 1,
                        });
                        console.log('Application created successfully.');
                    })
                    .catch((error_response) => {
                        Alert.error('Application already exists.');
                        console.log('Error while creating the application');
                    });
            }
        } else if (activeStep === 1) {
            // subscribe step
            const promised_subscribe = this.subscribeToApi.createSubscription();
            if (promised_subscribe) {
                promised_subscribe
                    .then((response) => {
                        console.log('Subscription created successfully with ID : ' + response.body.subscriptionId);

                        that.setState({
                            activeStep: activeStep + 1,
                        });
                    })
                    .catch((error) => {
                        console.log('Error while creating the subscription.');
                        console.error(error);
                    });
            }

            //
        } else if (activeStep === 2) {
            // Generate keys
            const promiseGenerate = this.keys.keygenWrapper();
            promiseGenerate
                .then(
                    (response) => {
                        console.log('Keys generated successfully with ID : ' + response);
                        that.setState({
                            activeStep: activeStep + 1,
                        });
                    },
                    // () => application.generateToken(this.key_type).then(() => this.setState({ application: application }))
                )
                .catch((error) => {
                    if (process.env.NODE_ENV !== 'production') {
                        console.log(error);
                    }
                    const status = error.status;
                    if (status === 404) {
                        this.setState({ notFound: true });
                    }
                });
        } else if (activeStep === 3) {
            // Generate tokens
            const promisseTokens = this.tokens.generateToken();
            promisseTokens
                .then(
                    (response) => {
                        console.log('token generated successfully : ', response);
                        that.token = response;
                        that.setState({
                            activeStep: activeStep + 1,
                        });
                    },
                    // () => application.generateToken(this.key_type).then(() => this.setState({ application: application }))
                )
                .catch((error) => {
                    if (process.env.NODE_ENV !== 'production') {
                        console.log(error);
                    }
                    const status = error.status;
                    if (status === 404) {
                        this.setState({ notFound: true });
                    }
                });
        } else if (activeStep === 4) {
            const { onClickFunction, updateSubscriptionData } = this.props;
            if (onClickFunction) {
                onClickFunction('openNew', updateSubscriptionData);
            }
        }
    };

    /**
     *
     *
     * @memberof Wizard
     */
    handleBack = () => {
        const { activeStep } = this.state;
        this.setState({
            activeStep: activeStep - 1,
        });
    };

    /**
     *
     *
     * @memberof Wizard
     */
    handleReset = () => {
        this.setState({
            activeStep: 0,
        });
    };

    /**
     *
     *
     * @memberof Wizard
     */
    handleChange = (event) => {
        this.setState({ value: event.target.value });
    };

    /**
     *
     * Set state.redirect to true to redirect to the API console page
     * @memberof Wizard
     */
    handleRedirectTest() {
        this.setState({ redirect: true });

    };

    /**
     *
     *
     * @returns
     * @memberof Wizard
     */
    render() {
        if (this.state.redirect) {
            return <Redirect push to={'/apis/' + this.props.apiId + '/test'} />;
        }

        const { classes } = this.props;
        const steps = getSteps();
        const { activeStep } = this.state;
        return (
            <ApiContext.Consumer>
                {({ api, applicationsAvailable }) => (
                    <div className={classes.root}>
                        <div>
                            <Stepper activeStep={activeStep}>
                                {steps.map((label, index) => {
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
                                {activeStep === steps.length ? (
                                    <div>
                                        <Typography className={classes.instructions}>All steps completed - you&quot;re finished</Typography>
                                        <Button onClick={this.handleReset} className={classes.button}>
                                            Reset
                                        </Button>
                                    </div>
                                ) : (
                                    <div className={classes.wizardContent}>
                                        {this.getStepContent(activeStep, api, applicationsAvailable)}
                                        <div className={classes.wizardButtons}>
                                            <Button disabled={activeStep === 0} onClick={this.handleBack} className={classes.button} variant='outlined'>
                                                Back
                                            </Button>
                                            <Button disabled={activeStep < steps.length - 1} onClick={this.handleRedirectTest} className={classes.button} variant='outlined'>
                                                Test
                                            </Button>
                                            <Button variant='contained' color='primary' onClick={() => this.handleNext()} className={classes.button}>
                                                {activeStep === steps.length - 1 ? 'Finish' : 'Next'}
                                            </Button>
                                        </div>
                                    </div>
                                )}
                            </div>
                        </div>
                    </div>
                )}
            </ApiContext.Consumer>
        );
    }
}

Wizard.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(Wizard);
