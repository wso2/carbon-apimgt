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
import Button from '@material-ui/core/Button';
import Dialog from '@material-ui/core/Dialog';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import IconButton from '@material-ui/core/IconButton';
import Typography from '@material-ui/core/Typography';
import CloseIcon from '@material-ui/icons/Close';
import Slide from '@material-ui/core/Slide';
import { FormattedMessage, injectIntl } from 'react-intl';
import API from 'AppData/api';
import ApplicationCreateForm from 'AppComponents/Shared/AppsAndKeys/ApplicationCreateForm';
import Alert from 'AppComponents/Shared/Alert';
import { ScopeValidation, resourceMethods, resourcePaths } from 'AppComponents/Shared/ScopeValidation';

/**
 *
 * @inheritdoc
 * @param {*} theme theme object
 */
const styles = theme => ({
    appBar: {
        position: 'relative',
        backgroundColor: theme.palette.background.appBar,
        color: theme.palette.getContrastText(theme.palette.background.appBar),
    },
    flex: {
        flex: 1,
    },
    button: {
        marginRight: theme.spacing.unit * 2,
    },
    buttonWrapper: {
        paddingLeft: theme.spacing.unit * 7,
    },
    createFormWrapper: {
        paddingLeft: theme.spacing.unit * 5,
    },
});
/**
 * @param {*} props properties
 * @returns {Component}
 */
function Transition(props) {
    return <Slide direction='up' {...props} />;
}
/**
 * Component used to handle application creation
 * @class NewApp
 * @extends {React.Component}
 * @param {any} value @inheritDoc
 */
class NewApp extends React.Component {
    /**
     * @param {*} props properties
     */
    constructor(props) {
        super(props);
        this.state = {
            applicationRequest: {
                name: '',
                throttlingPolicy: '',
                description: '',
                tokenType: 'OAUTH',
                attributes: {},
            },
            isNameValid: true,
            throttlingPolicyList: [],
            allAppAttributes: null,
        };
    }

    /**
     * Get all the throttling Policies from backend and
     * update the state
     * @memberof NewApp
     */
    componentDidMount() {
        // Get all the tires to populate the drop down.
        const api = new API();
        const promiseTiers = api.getAllTiers('application');
        const promisedAttributes = api.getAllApplicationAttributes();
        Promise.all([promiseTiers, promisedAttributes])
            .then((response) => {
                const [tierResponse, allAttributes] = response;
                const { applicationRequest } = this.state;
                const throttlingPolicyList = tierResponse.body.list.map(item => item.name);
                const newRequest = { ...applicationRequest };
                if (throttlingPolicyList.length > 0) {
                    [newRequest.throttlingPolicy] = throttlingPolicyList;
                }
                const allAppAttributes = [];
                allAttributes.body.list.map(item => allAppAttributes.push(item));
                if (allAttributes.length > 0) {
                    newRequest.attributes = allAppAttributes.filter(item => !item.hidden);
                }
                this.setState({ applicationRequest: newRequest, throttlingPolicyList, allAppAttributes });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const { status } = error;
                if (status === 404) {
                    // eslint-disable-next-line react/no-unused-state
                    this.setState({ notFound: true });
                }
            });
    }

    /**
     * Update Application Request state
     * @param {Object} applicationRequest parameters requried for application
     */
    updateApplicationRequest = (applicationRequest) => {
        this.setState({ applicationRequest });
    }

    /**
     * @param {object} name application attribute name
     * @returns {void}
     * @memberof NewApp
     */
    handleAttributesChange = name => (event) => {
        const { applicationRequest } = this.state;
        applicationRequest.attributes[name] = event.target.value;
        this.setState({ applicationRequest });
    };

    /**
     * @param {object} name application attribute name
     * @returns {Object} attribute value
     * @memberof NewApp
     */
    getAttributeValue = (name) => {
        const { applicationRequest } = this.state;
        return applicationRequest.attributes[name];
    };

    /**
     * @param {object} name application attribute name
     * @returns {void}
     * @memberof NewApp
     */
    isRequiredAttribute = (name) => {
        const { allAppAttributes } = this.state;
        if (allAppAttributes) {
            for (let i = 0; i < allAppAttributes.length; i++) {
                if (allAppAttributes[i].attribute === name) {
                    return allAppAttributes[i].required === 'true';
                }
            }
        }
        return false;
    };

    /**
     * @param {object} attributes application attributes list
     * @returns {object}
     * @memberof EditApp
     */
    validateAttributes = (attributes) => {
        const { intl } = this.props;
        const { allAppAttributes } = this.state;
        let isValidAttribute = true;
        const attributeNameList = Object.keys(attributes);
        if (allAppAttributes.length > 0) {
            for (let i = 0; i < allAppAttributes.length; i++) {
                if (allAppAttributes[i].required === 'true' && allAppAttributes[i].hidden === 'false') {
                    if (attributeNameList.indexOf(allAppAttributes[i].attribute) === -1) {
                        isValidAttribute = false;
                    } else if (attributeNameList.indexOf(allAppAttributes[i].attribute) > -1
                    && (!attributes[allAppAttributes[i].attribute]
                        || attributes[allAppAttributes[i].attribute].trim() === '')) {
                        isValidAttribute = false;
                    }
                }
            }
        }
        if (!isValidAttribute) {
            return Promise.reject(new Error(intl.formatMessage({
                id: 'Applications.Edit.app.update.error.no.required.attribute',
                defaultMessage: 'Please fill all required application attributes',
            })));
        } else {
            return Promise.resolve(true);
        }
    };

    /**
     * Validate and send the application create
     * request to the backend
     * @memberof NewApp
     */
    saveApplication = () => {
        const { applicationRequest } = this.state;
        const { updateApps, handleClose, intl } = this.props;
        const api = new API();
        this.validateName(applicationRequest.name)
            .then(() => this.validateAttributes(applicationRequest.attributes))
            .then(() => api.createApplication(applicationRequest))
            .then(() => {
                console.log('Application created successfully.');
                handleClose();
                updateApps();
            })
            .catch((error) => {
                const { response } = error;
                if (response && response.body) {
                    const message = response.body.description || intl.formatMessage({
                        defaultMessage: 'Error while creating the application',
                        id: 'Applications.Create.NewApp.error.while.creating.the.application',
                    });
                    Alert.error(message);
                } else {
                    Alert.error(error.message);
                }
                console.error('Error while creating the application');
            });
    };


    validateName = (value) => {
        const { intl } = this.props;
        if (!value || value.trim() === '') {
            this.setState({ isNameValid: false });
            return Promise.reject(new Error(intl.formatMessage({
                id: 'Applications.Create.NewApp.app.name.required',
                defaultMessage: 'Application name is required',
            })));
        }
        this.setState({ isNameValid: true });
        return Promise.resolve(true);
    };

    /**
     * @inheritdoc
     * @memberof NewApp
     */
    render() {
        const {
            throttlingPolicyList, applicationRequest, isNameValid, allAppAttributes,
        } = this.state;
        const {
            classes, open, handleClickOpen, handleClose,
        } = this.props;
        return (
            <React.Fragment>
                <ScopeValidation resourcePath={resourcePaths.APPLICATIONS} resourceMethod={resourceMethods.POST}>
                    <Button
                        variant='contained'
                        color='primary'
                        className={classes.button}
                        onClick={handleClickOpen}
                    >
                        <FormattedMessage
                            id='Applications.Create.NewApp.add.new.application'
                            defaultMessage='ADD NEW APPLICATION'
                        />
                    </Button>
                </ScopeValidation>
                <Dialog fullScreen open={open} onClose={handleClose} TransitionComponent={Transition}>
                    <AppBar className={classes.appBar}>
                        <Toolbar>
                            <IconButton color='inherit' onClick={handleClose} aria-label='Close'>
                                <CloseIcon />
                            </IconButton>
                            <Typography variant='title' color='inherit' className={classes.flex}>
                                <FormattedMessage
                                    id='Applications.Create.NewApp.create.new.application'
                                    defaultMessage='Create New Application'
                                />
                            </Typography>
                            <Button color='inherit' onClick={handleClose}>
                                <FormattedMessage
                                    id='Applications.Create.NewApp.save'
                                    defaultMessage='save'
                                />
                            </Button>
                        </Toolbar>
                    </AppBar>
                    <div className={classes.createFormWrapper}>
                        <ApplicationCreateForm
                            throttlingPolicyList={throttlingPolicyList}
                            applicationRequest={applicationRequest}
                            updateApplicationRequest={this.updateApplicationRequest}
                            validateName={this.validateName}
                            isNameValid={isNameValid}
                            allAppAttributes={allAppAttributes}
                            handleAttributesChange={this.handleAttributesChange}
                            isRequiredAttribute={this.isRequiredAttribute}
                            getAttributeValue={this.getAttributeValue}
                        />
                    </div>
                    <div className={classes.buttonWrapper}>
                        <Button variant='outlined' className={classes.button} onClick={handleClose}>
                            <FormattedMessage
                                id='Applications.Create.NewApp.cancel'
                                defaultMessage='Cancel'
                            />
                        </Button>
                        <Button
                            variant='contained'
                            color='primary'
                            className={classes.button}
                            onClick={this.saveApplication}
                        >
                            <FormattedMessage
                                id='Applications.Create.NewApp.add.new.application.button'
                                defaultMessage='ADD NEW APPLICATION'
                            />
                        </Button>
                    </div>
                </Dialog>
            </React.Fragment>
        );
    }
}

NewApp.propTypes = {
    classes: PropTypes.shape({
        appBar: PropTypes.string,
        flex: PropTypes.string,
        createFormWrapper: PropTypes.string,
        buttonWrapper: PropTypes.string,
        button: PropTypes.string,
    }).isRequired,
    updateApps: PropTypes.func.isRequired,
    handleClose: PropTypes.func.isRequired,
    intl: PropTypes.func.isRequired,
    handleClickOpen: PropTypes.func.isRequired,
    open: PropTypes.bool.isRequired,
};

export default injectIntl(withStyles(styles)(NewApp));
