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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Stepper from '@material-ui/core/Stepper';
import Step from '@material-ui/core/Step';
import StepLabel from '@material-ui/core/StepLabel';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import { FormattedMessage } from 'react-intl';
import API from 'AppData/api';
import Alert from 'AppComponents/Shared/Alert';
import APIInputForm from 'AppComponents/Apis/Create/Components/APIInputForm';
import Progress from 'AppComponents/Shared/Progress';

import ProvideGraphQL from './Steps/ProvideGraphQL';

const styles = theme => ({
    instructions: {
        marginTop: theme.spacing.unit,
        marginBottom: theme.spacing.unit,
    },
    root: {
        flexGrow: 1,
        marginLeft: 0,
        marginTop: 0,
        paddingLeft: theme.spacing.unit * 4,
        paddingTop: theme.spacing.unit * 2,
        paddingBottom: theme.spacing.unit * 2,
        width: theme.custom.contentAreaWidth,
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
    stepper: {
        paddingLeft: 0,
        marginLeft: 0,
        width: 400,
    },
});

/**
 *
 *
 * @returns
 */
function getSteps() {
    const steps = [
        <FormattedMessage id='Apis.GraphQL.APICreateGraphQL.select.graphQL' defaultMessage='Select GraphQL' />,
        <FormattedMessage id='Apis.GraphQL.APICreateGraphQL.create.api' defaultMessage='Create API' />,
    ];
    return steps;
}

/**
 *
 * Simple util method to check whether provided object is empty
 * @param {Object} obj any
 * @returns {boolean} check
 */
function isEmpty(obj) {
    return Object.entries(obj).length === 0 && obj.constructor === Object;
}

/**
 *
 *
 * @class APICreateGraphQL
 * @extends {React.Component}
 */
class APICreateGraphQL extends React.Component {
    /**
     * Creates an instance of APICreateGraphQL.
     * @param {any} props @inheritDoc
     * @memberof APICreateGraphQL
     */
    constructor(props) {
        super(props);
        this.state = {
            doValidate: false,
            graphQLBean: {},
            activeStep: 0,
            api: null,
            loading: false,
            valid: {
                graphQLFile: { empty: false, invalidFile: false },
                name: { empty: false, alreadyExists: false },
                context: { empty: false, alreadyExists: false },
                version: { empty: false },
                endpointConfig: { empty: false },
            },
        };
        this.updateGraphQLBean = this.updateGraphQLBean.bind(this);
        this.updateApiInputs = this.updateApiInputs.bind(this);
        this.createGraphQLAPI = this.createGraphQLAPI.bind(this);
        this.updateFileErrors = this.updateFileErrors.bind(this);
        this.provideGraphQL = null;
    }

    /**
     * Check the GraphQL file validity through GraphQL
     * @param {Object} graphQLBean Bean object holding GraphQL file info
     * @memberof APICreateGraphQL
     */
    updateGraphQLBean(graphQLBean) {
        this.setState({
            graphQLBean,
        });
    }

    /**
     * Update user inputs in the form with onChange event trigger
     * @param {React.SyntheticEvent} event Event containing user action
     * @memberof APICreateGraphQL
     */
    updateApiInputs({ target }) {
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
            const validUpdated = valid;
            validUpdated.name.empty = !api.name;
            validUpdated.context.empty = !api.context;
            validUpdated.version.empty = !api.version;
            validUpdated.endpointConfig.empty = !api.endpointConfig;

            return { api: changes, valid: validUpdated };
        });
    }

    /**
     * Make API POST call and create send GraphQL file or URL
     * @memberof APICreateGraphQL
     */
    createGraphQLAPI() {
        this.setState({ loading: true });
        const newApi = new API();
        const { graphQLBean, api } = this.state;
        const {
            name, version, context, policies, endpointConfig, gatewayEnvironments, implementationType, operations,
        } = api;
        const uploadMethod = 'file';
        const apiAttributes = {
            name,
            version,
            context,
            endpointConfig,
            gatewayEnvironments,
            policies,
            operations,
        };
        const apiData = {
            additionalProperties: JSON.stringify(apiAttributes),
            implementationType,
            [uploadMethod]: graphQLBean[uploadMethod],
            file: graphQLBean.file,
        };

        newApi
            .importGraphQL(apiData)
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
    updateFileErrors(newValid) {
        this.setState({ valid: newValid });
    }

    handleNext = () => {
        const { activeStep, graphQLBean, valid } = this.state;
        const { uploadMethod } = 'file';

        const validNew = JSON.parse(JSON.stringify(valid));

        // Handling next ( getting graphQL file info and validating)
        if (activeStep === 0) {
            if (isEmpty(graphQLBean)) {
                if (uploadMethod === 'file') {
                    validNew.graphQLFile.empty = true;
                }
                this.setState({ valid: validNew });
                return;
            }
            // No errors so let's fill the inputs with the graphQLBean
            if (graphQLBean.info) {
                if (graphQLBean.info.operations) {
                    const changes = new API({
                        operations: graphQLBean.info.operations,
                        gatewayEnvironments: ['Production and Sandbox'],
                    });
                    this.setState({ api: changes });
                }
            }
            this.setState({
                activeStep: 1,
            });
        } else if (activeStep === 1) {
            // Handling Finish step ( validating the input fields )
            const { api: currentAPI } = this.state;
            if (!currentAPI.name || !currentAPI.context || !currentAPI.version || !currentAPI.endpointConfig) {
                // Checking the api name,version,context undefined or empty states
                this.setState((oldState) => {
                    const { valid: isValid, api } = oldState;
                    const validUpdated = isValid;
                    validUpdated.name.empty = !api.name;
                    validUpdated.context.empty = !api.context;
                    validUpdated.version.empty = !api.version;
                    validUpdated.endpointConfig.empty = !api.endpointConfig;
                    return { valid: validUpdated };
                });
                return;
            }
            this.createGraphQLAPI();
        }
    };

    handleBack = () => {
        this.setState(state => ({
            activeStep: state.activeStep - 1,
        }));
    };

    handleReset = () => {
        this.setState({
            activeStep: 0,
        });
    };

    /**
     *
     *
     * @returns
     * @memberof APICreateGraphQL
     */
    render() {
        const { classes } = this.props;
        const steps = getSteps();
        const {
            doValidate, activeStep, graphQLBean, api, valid, loading,
        } = this.state;
        const uploadMethod = 'file';
        const provideGraphQLProps = {
            [uploadMethod]: graphQLBean[uploadMethod],
            updateGraphQLBean: this.updateGraphQLBean,
            validate: doValidate,
            valid,
            updateFileErrors: this.updateFileErrors,
        };
        if (loading) {
            return <Progress />;
        }
        return (
            <React.Fragment>
                <Grid container spacing={7} className={classes.root}>
                    <Grid container spacing={24} className={classes.root}>
                        <Grid item xs={12} xl={6}>
                            <div className={classes.titleWrapper}>
                                <Typography variant='h4' align='left' className={classes.mainTitle}>
                                    <FormattedMessage
                                        id='Apis.GraphQL.APICreateGraphQL.using.SDL'
                                        defaultMessage='Import a GraphQL SDL Definition'
                                    />
                                </Typography>
                            </div>
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
                                    <ProvideGraphQL
                                        {...provideGraphQLProps}
                                    />
                                )}
                                {activeStep === 1 && (
                                    <React.Fragment>
                                        <APIInputForm
                                            api={api}
                                            handleInputChange={this.updateApiInputs}
                                            valid={valid}
                                        />
                                    </React.Fragment>
                                )}
                            </div>
                            <div>
                                <div>
                                    <div>
                                        <Button
                                            disabled={activeStep === 0}
                                            onClick={this.handleBack}
                                            className={classes.button}
                                        >Back
                                        </Button>
                                        <Button
                                            variant='contained'
                                            color='primary'
                                            onClick={this.handleNext}
                                            className={classes.button}
                                            disabled={
                                                (valid.graphQLFile.invalidFile && uploadMethod === 'file')
                                            }
                                        >
                                            {activeStep === steps.length - 1 ? (
                                                'Finish'
                                            ) : (
                                                <FormattedMessage
                                                    id='Apis.GraphQL.APICreateGraphQL.next'
                                                    defaultMessage='Next'
                                                />
                                            )}
                                        </Button>
                                    </div>
                                </div>
                            </div>
                        </Grid>
                    </Grid>
                </Grid>
            </React.Fragment>
        );
    }
}

APICreateGraphQL.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    history: PropTypes.shape({ push: PropTypes.func }).isRequired,
    intl: PropTypes.shape({
        formatMessage: PropTypes.func.isRequired,
    }).isRequired,
};

export default withStyles(styles)(APICreateGraphQL);
