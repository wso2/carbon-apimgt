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
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Stepper from '@material-ui/core/Stepper';
import Step from '@material-ui/core/Step';
import StepLabel from '@material-ui/core/StepLabel';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import { FormattedMessage } from 'react-intl';
import Paper from '@material-ui/core/Paper';
import API from 'AppData/api';
import Alert from 'AppComponents/Shared/Alert';
import APIInputForm from 'AppComponents/Apis/Create/Components/APIInputForm';
import Progress from 'AppComponents/Shared/Progress';

import ProvideWSDL from './Steps/ProvideWSDL';
import BindingInfo from './BindingInfo';
import APICreateTopMenu from '../Components/APICreateTopMenu';

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
        marginBottom: theme.spacing.unit * 2,
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
        <FormattedMessage id='select.wsdl' defaultMessage='Select WSDL' />,
        <FormattedMessage id='create.api' defaultMessage='Create API' />,
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
 * @class APICreateWSDL
 * @extends {React.Component}
 */
class APICreateWSDL extends React.Component {
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
            activeStep: 0,
            api: new API('', 'v1.0.0'),
            loading: false,
            valid: {
                wsdlUrl: { empty: false, invalidUrl: false },
                wsdlFile: { empty: false, invalidFile: false },
                name: { empty: false, alreadyExists: false },
                context: { empty: false, alreadyExists: false },
                version: { empty: false },
                endpoint: { empty: false },
            },
        };
        this.updateWSDLBean = this.updateWSDLBean.bind(this);
        this.updateApiInputs = this.updateApiInputs.bind(this);
        this.createWSDLAPI = this.createWSDLAPI.bind(this);
        this.updateFileErrors = this.updateFileErrors.bind(this);
        this.provideWSDL = null;
    }

    /**
     * Check the WSDL file or URL validity through REST API
     * @param {Object} wsdlBean Bean object holding WSDL file/url info
     * @memberof ApiCreateWSDL
     */
    updateWSDLBean(wsdlBean) {
        console.info(wsdlBean);
        this.setState({
            wsdlBean,
        });
    }

    /**
     * Update user inputs in the form with onChange event trigger
     * @param {React.SyntheticEvent} event Event containing user action
     * @memberof ApiCreateWSDL
     */
    updateApiInputs({ target }) {
        const { name, value } = target;
        this.setState(({ api, valid }) => {
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

            // Checking validity.
            const validUpdated = valid;
            validUpdated.name.empty = !api.name;
            validUpdated.context.empty = !api.context;
            validUpdated.version.empty = !api.version;
            validUpdated.endpoint.empty = !api.endpoint;
            // TODO we need to add the already existing error for
            // (context) by doing an api call ( the swagger definition does not contain such api call)
            return { api: changes, valid: validUpdated };
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
    updateFileErrors(newValid) {
        this.setState({ valid: newValid });
    }
    handleNext = () => {
        const { activeStep, wsdlBean, valid } = this.state;
        let uploadMethod;
        if (this.provideWSDL) {
            uploadMethod = this.provideWSDL.getUploadMethod();
        } else if (wsdlBean.file) {
            uploadMethod = 'file';
        } else {
            uploadMethod = 'url';
        }
        const validNew = JSON.parse(JSON.stringify(valid));

        // Handling next ( getting wsdl file/url info and validating)
        if (activeStep === 0) {
            if (isEmpty(wsdlBean)) {
                if (uploadMethod === 'file') {
                    validNew.wsdlFile.empty = true;
                } else {
                    validNew.wsdlUrl.empty = true;
                }
                this.setState({ valid: validNew });
                return;
            } else {
                if (wsdlBean.file && uploadMethod === 'url') {
                    validNew.wsdlUrl.empty = true;
                    this.setState({ valid: validNew });
                    return;
                }
                if (wsdlBean.url && uploadMethod === 'file') {
                    validNew.wsdlFile.empty = true;
                    this.setState({ valid: validNew });
                    return;
                }
            }
            // No errors so let's fill the inputs with the wsdlBean
            if (wsdlBean.info) {
                if (wsdlBean.info.version) {
                    this.updateApiInputs({ target: { name: 'version', value: wsdlBean.info.version } });
                }
                if (wsdlBean.info.endpoints && wsdlBean.info.endpoints.length > 0) {
                    this.updateApiInputs({ target: { name: 'endpoint', value: wsdlBean.info.endpoints[0].location } });
                }
            }
            this.setState({
                activeStep: activeStep + 1,
            });
        } else if (activeStep === 1) {
            // Handling Finish step ( validating the input fields )
            const { api: currentAPI } = this.state;
            if (!currentAPI.name || !currentAPI.context || !currentAPI.version || !currentAPI.endpoint) {
                // Checking the api name,version,context undefined or empty states
                this.setState((oldState) => {
                    const { valid: isValid, api } = oldState;
                    const validUpdated = isValid;
                    validUpdated.name.empty = !api.name;
                    validUpdated.context.empty = !api.context;
                    validUpdated.version.empty = !api.version;
                    validUpdated.endpoint.empty = !api.endpoint;
                    return { valid: validUpdated };
                });
                return;
            }
            this.createWSDLAPI();
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
     * @memberof APICreateWSDL
     */
    render() {
        const { classes } = this.props;
        const steps = getSteps();
        const {
            doValidate, activeStep, wsdlBean, api, valid, loading,
        } = this.state;
        const uploadMethod = wsdlBean.url ? 'url' : 'file';
        const provideWSDLProps = {
            uploadMethod,
            [uploadMethod]: wsdlBean[uploadMethod],
            updateWSDLBean: this.updateWSDLBean,
            validate: doValidate,
            valid,
            updateFileErrors: this.updateFileErrors,
        };
        if (loading) {
            return <Progress />;
        }
        return (
            <React.Fragment>
                <APICreateTopMenu />
                <Grid container spacing={7} className={classes.root}>
                    <Grid item xs={12}>
                        <div className={classes.titleWrapper}>
                            <Typography variant='h4' align='left' className={classes.mainTitle}>
                                <FormattedMessage
                                    id='design.a.new.rest.api.using.wsdl'
                                    defaultMessage='Design a new REST API using WSDL'
                                />
                            </Typography>
                            <Typography variant='caption' align='left' className={classes.subTitle} component='div'>
                                {uploadMethod === 'file' && (
                                    <FormattedMessage
                                        id='design.a.new.rest.api.using.wsdl.help.file'
                                        defaultMessage='Provide an api definition file'
                                    />
                                )}
                                {uploadMethod === 'url' && (
                                    <FormattedMessage
                                        id='design.a.new.rest.api.using.wsdl.help.url'
                                        defaultMessage='Provide a url for the api deninition'
                                    />
                                )}
                            </Typography>
                        </div>
                        <Paper>
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
                        </Paper>
                        <div>
                            {activeStep === 0 && (
                                <ProvideWSDL
                                    {...provideWSDLProps}
                                    innerRef={(instance) => {
                                        this.provideWSDL = instance;
                                    }}
                                />
                            )}
                            {activeStep === 1 && (
                                <React.Fragment>
                                    <APIInputForm api={api} handleInputChange={this.updateApiInputs} valid={valid} />
                                    <BindingInfo
                                        updateApiInputs={this.updateApiInputs}
                                        wsdlBean={wsdlBean}
                                        classes={classes}
                                        api={api}
                                    />
                                </React.Fragment>
                            )}
                        </div>
                        <div>
                            {activeStep === steps.length ? (
                                <div>
                                    <Typography className={classes.instructions}>
                                        All steps completed - you&quot;re finished
                                    </Typography>
                                    <Button onClick={this.handleReset} className={classes.button}>
                                        Reset
                                    </Button>
                                </div>
                            ) : (
                                <div>
                                    <div>
                                        <Button
                                            disabled={activeStep === 0}
                                            onClick={this.handleBack}
                                            className={classes.button}
                                        >
                                            Back
                                        </Button>
                                        <Button
                                            variant='contained'
                                            color='primary'
                                            onClick={this.handleNext}
                                            className={classes.button}
                                            disabled={
                                                (valid.wsdlFile.invalidFile && uploadMethod === 'file') ||
                                                (valid.wsdlUrl.invalidUrl && uploadMethod === 'url')
                                            }
                                        >
                                            {activeStep === steps.length - 1 ? (
                                                'Finish'
                                            ) : (
                                                <FormattedMessage id='next' defaultMessage='Next' />
                                            )}
                                        </Button>
                                    </div>
                                </div>
                            )}
                        </div>
                    </Grid>
                </Grid>
            </React.Fragment>
        );
    }
}

APICreateWSDL.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    history: PropTypes.shape({ push: PropTypes.func }).isRequired,
};

export default withStyles(styles)(APICreateWSDL);
