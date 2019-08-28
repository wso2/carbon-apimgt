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
import Icon from '@material-ui/core/Icon';
import Slide from '@material-ui/core/Slide';
import { FormattedMessage, injectIntl } from 'react-intl';
import { Link } from 'react-router-dom';
import API from 'AppData/api';
import ApplicationCreateForm from 'AppComponents/Shared/AppsAndKeys/ApplicationCreateForm';
import Alert from 'AppComponents/Shared/Alert';
import Application from 'AppData/Application';
import Settings from 'AppComponents/Shared/SettingsContext';

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
    buttonRight: {
        textDecoration: 'none',
        color: 'white',
    },
    flex: {
        flex: 1,
    },
    button: {
        marginRight: theme.spacing.unit * 2,
    },
    link: {
        textDecoration: 'none',
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
 * Component used to handle application editing
 * @class EditApp
 * @extends {React.Component}
 * @param {any} value @inheritDoc
 */
class EditApp extends React.Component {
    static contextType = Settings;

    /**
     * @param {*} props properties
     */
    constructor(props) {
        super(props);
        this.state = {
            open: true,
            applicationRequest: {
                applicationId: null,
                name: '',
                throttlingPolicy: '',
                description: '',
                groups: null,
                tokenType: null,
                attributes: {},
            },
            isNameValid: true,
            throttlingPolicyList: [],
            allAppAttributes: [],
            isApplicationSharingEnabled: true,
        };
    }

    /**
     * Get all the throttling Policies from backend and
     * update the state
     * @memberof EditApp
     */
    componentDidMount() {
        const { match } = this.props;
        const { applicationRequest } = this.state;
        const promisedApplication = Application.get(match.params.application_id);
        // Get all the tires to populate the drop down.
        const api = new API();
        const promiseTiers = api.getAllTiers('application');
        const promisedAttributes = api.getAllApplicationAttributes();
        Promise.all([promisedApplication, promiseTiers, promisedAttributes])
            .then((response) => {
                const [application, tierResponse, allAttributes] = response;
                const throttlingPolicyList = [];
                tierResponse.body.list.map(item => throttlingPolicyList.push(item.name));
                const allAppAttributes = [];
                allAttributes.body.list.map(item => allAppAttributes.push(item));
                const newRequest = { ...applicationRequest };
                newRequest.applicationId = application.applicationId;
                newRequest.name = application.name;
                newRequest.throttlingPolicy = application.throttlingPolicy;
                newRequest.description = application.description;
                newRequest.groups = application.groups;
                newRequest.tokenType = application.tokenType;
                newRequest.attributes = application.attributes;
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
        this.isApplicationGroupSharingEnabled();
    }

    /**
     * Update keyRequest state
     * @param {Object} applicationRequest parameters requried for application
     * create request
     */
    updateApplicationRequest = (applicationRequest) => {
        this.setState({ applicationRequest });
    }

    /**
     * @param {object} name application attribute name
     * @returns {void}
     * @memberof EditApp
     */
    handleAttributesChange = name => (event) => {
        const { applicationRequest } = this.state;
        applicationRequest.attributes[name] = event.target.value;
        this.setState({ applicationRequest });
    };

    /**
     * @param {object} name application attribute name
     * @returns {Object} attribute value
     * @memberof EditApp
     */
    getAttributeValue = (name) => {
        const { applicationRequest } = this.state;
        return applicationRequest.attributes[name];
    };

    /**
     * @param {object} name application attribute name
     * @returns {void}
     * @memberof EditApp
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
     * @param {Object} event the event object
     * @memberof EditApp
     */
    handleSubmit = () => {
        const { applicationRequest } = this.state;
        const {
            history, intl,
        } = this.props;
        const api = new API();
        this.validateName(applicationRequest.name)
            .then(() => this.validateAttributes(applicationRequest.attributes))
            .then(() => api.updateApplication(applicationRequest, null))
            .then((response) => {
                const appId = response.body.applicationId;
                const redirectUrl = '/applications/' + appId;
                history.push(redirectUrl);
                Alert.info(intl.formatMessage({
                    id: 'Applications.Edit.app.updated.success',
                    defaultMessage: 'Application updated successfully',
                }));
                console.log('Application updated successfully.');
            })
            .catch((error) => {
                const { response } = error;
                if (response && response.body) {
                    const message = response.body.description || 'Error while updating the application';
                    Alert.error(message);
                } else {
                    Alert.error(error.message);
                }
                console.error('Error while updating the application');
            });
    };

    /**
     * @memberof EditApp
     */
    handleClose = () => {
        this.setState({ open: false });
    };

    /**
     * @memberof EditApp
     */
    handleClickOpen = () => {
        this.setState({ open: true });
    };

    validateName = (value) => {
        const { intl } = this.props;
        if (!value || value.trim() === '') {
            this.setState({ isNameValid: false });
            return Promise.reject(new Error(intl.formatMessage({
                id: 'Applications.Edit.app.name.required',
                defaultMessage: 'Application name is required',
            })));
        }
        this.setState({ isNameValid: true });
        return Promise.resolve(true);
    };

    /**
     * add a new group function
     * @param {*} chip newly added group
     * @param {*} appGroups already existing groups
     */
    handleAddChip = (chip, appGroups) => {
        const { applicationRequest } = this.state;
        const newRequest = { ...applicationRequest };
        let values = appGroups || [];
        values = values.slice();
        values.push(chip);
        newRequest.groups = values;
        this.setState({ applicationRequest: newRequest });
    }

    /**
     * remove a group from already existing groups function
     * @param {*} chip selected group to be removed
     * @param {*} index selected group index to be removed
     * @param {*} appGroups already existing groups
     */
    handleDeleteChip = (chip, index, appGroups) => {
        const { applicationRequest } = this.state;
        const newRequest = { ...applicationRequest };
        let values = appGroups || [];
        values = values.filter(v => v !== chip);
        newRequest.groups = values;
        this.setState({ applicationRequest: newRequest });
    }

    /**
     * retrieve Settings from the context and check the application sharing enabled
     */
    isApplicationGroupSharingEnabled = () => {
        const settingsContext = this.context;
        const enabled = settingsContext.settings.applicationSharingEnabled;
        this.setState({ isApplicationSharingEnabled: enabled });
    }

    /**
     * @inheritdoc
     * @memberof EditApp
     */
    render() {
        const {
            throttlingPolicyList, applicationRequest, isNameValid, open, allAppAttributes,
            isApplicationSharingEnabled,
        } = this.state;
        const { classes } = this.props;
        return (
            <React.Fragment>
                <Dialog fullScreen open={open} onClose={this.handleClose} TransitionComponent={Transition}>
                    <AppBar className={classes.appBar}>
                        <Toolbar>
                            <Link to='/applications' className={classes.buttonRight}>
                                <IconButton color='inherit' onClick={this.handleClose} aria-label='Close'>
                                    <Icon>close</Icon>
                                </IconButton>
                            </Link>
                            <Typography variant='title' color='inherit' className={classes.flex}>
                                <FormattedMessage id='Applications.Edit.edit.app' defaultMessage='Edit Application' />
                            </Typography>
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
                            isApplicationSharingEnabled={isApplicationSharingEnabled}
                            handleDeleteChip={this.handleDeleteChip}
                            handleAddChip={this.handleAddChip}
                        />
                    </div>
                    <div className={classes.buttonWrapper}>
                        <Link to='/applications' className={classes.link}>
                            <Button
                                variant='outlined'
                                className={classes.button}
                            >
                                <FormattedMessage
                                    id='Applications.Edit.cancel'
                                    defaultMessage='CANCEL'
                                />
                            </Button>
                        </Link>
                        <Button
                            variant='contained'
                            className={classes.button}
                            color='primary'
                            onClick={this.handleSubmit}
                        >
                            <FormattedMessage
                                id='Applications.Edit.update.app'
                                defaultMessage='UPDATE APPLICATION'
                            />
                        </Button>
                    </div>
                </Dialog>
            </React.Fragment>
        );
    }
}

EditApp.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({}).isRequired,
    match: PropTypes.shape({
        params: PropTypes.shape({
            application_uuid: PropTypes.string.isRequired,
        }).isRequired,
    }).isRequired,
    history: PropTypes.shape({
        push: PropTypes.func.isRequired,
    }).isRequired,
};

export default injectIntl(withStyles(styles)(EditApp));
