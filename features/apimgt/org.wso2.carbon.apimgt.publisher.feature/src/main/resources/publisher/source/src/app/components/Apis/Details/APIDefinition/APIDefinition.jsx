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

import React, { Suspense, lazy } from 'react';
import AppContext from 'AppComponents/Shared/AppContext';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Button from '@material-ui/core/Button';
import EditRounded from '@material-ui/icons/EditRounded';
import CloudDownloadRounded from '@material-ui/icons/CloudDownloadRounded';
import LockRounded from '@material-ui/icons/LockRounded';
import SwapHorizontalCircle from '@material-ui/icons/SwapHorizontalCircle';
import CustomSplitButton from 'AppComponents/Shared/CustomSplitButton';
import Dialog from '@material-ui/core/Dialog';
import Grid from '@material-ui/core/Grid';
import IconButton from '@material-ui/core/IconButton';
import Icon from '@material-ui/core/Icon';
import Paper from '@material-ui/core/Paper';
import { FormattedMessage, injectIntl } from 'react-intl';
import { Progress } from 'AppComponents/Shared';
import Typography from '@material-ui/core/Typography';
import Slide from '@material-ui/core/Slide';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import CircularProgress from '@material-ui/core/CircularProgress';
import YAML from 'js-yaml';
import Alert from 'AppComponents/Shared/Alert';
import API from 'AppData/api.js';
import { doRedirectToLogin } from 'AppComponents/Shared/RedirectToLogin';
import { withRouter } from 'react-router';
import { isRestricted } from 'AppData/AuthManager';
import ResourceNotFound from '../../../Base/Errors/ResourceNotFound';
import APISecurityAudit from './APISecurityAudit';
import ImportDefinition from './ImportDefinition';
import DefinitionOutdated from './DefinitionOutdated';

const EditorDialog = lazy(() => import('./SwaggerEditorDrawer' /* webpackChunkName: "EditorDialog" */));
const MonacoEditor = lazy(() => import('react-monaco-editor' /* webpackChunkName: "APIDefMonacoEditor" */));
const AsyncAPIEditor = lazy(() => import('./AsyncApiEditorDrawer'));

const styles = (theme) => ({
    titleWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
    },
    swaggerEditorWrapper: {
        height: '100vh',
        overflowY: 'auto',
    },
    buttonIcon: {
        marginRight: 10,
    },
    buttonWarningColor: {
        color: theme.palette.warning.light,
    },
    topBar: {
        display: 'flex',
        flexDirection: 'row',
        marginBottom: theme.spacing(2),
    },
    converterWrapper: {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'flex-end',
        flex: '1',
        fontSize: '0.6964285714285714rem',
    },
    downloadLink: {
        color: 'black',
    },
    button: {
        marginLeft: theme.spacing(1),
    },
    progressLoader: {
        marginLeft: theme.spacing(1),
    },
    updateApiWarning: {
        marginLeft: theme.spacing(5),
        color: theme.custom.serviceCatalog.onboarding.buttonText,
        borderColor: theme.custom.serviceCatalog.onboarding.buttonText,
    },
    warningIconStyle: {
        color: theme.custom.serviceCatalog.onboarding.buttonText,
    },
});
/**
 * This component holds the functionality of viewing the api definition content of an api. The initial view is a
 * read-only representation of the api definition file.
 * Users can either edit the content by clicking the 'Edit' button or upload a new api definition file by clicking
 * 'Import API Definition'.
 * */
class APIDefinition extends React.Component {
    /**
     * @inheritDoc
     */
    constructor(props) {
        super(props);
        this.state = {
            openEditor: false,
            swagger: null,
            swaggerModified: null,
            graphQL: null,
            format: null,
            convertTo: null,
            isAuditApiClicked: false,
            securityAuditProperties: [],
            isSwaggerValid: true,
            isUpdating: false,
            asyncAPI: null,
            asyncAPIModified: null,
            isAsyncAPIValid: true,
        };
        this.handleNo = this.handleNo.bind(this);
        this.handleSave = this.handleSave.bind(this);
        this.handleSaveAndDeploy = this.handleSaveAndDeploy.bind(this);
        this.openEditor = this.openEditor.bind(this);
        this.transition = this.transition.bind(this);
        this.closeEditor = this.closeEditor.bind(this);
        this.hasJsonStructure = this.hasJsonStructure.bind(this);
        this.getConvertToFormat = this.getConvertToFormat.bind(this);
        this.onAuditApiClick = this.onAuditApiClick.bind(this);
        this.onChangeFormatClick = this.onChangeFormatClick.bind(this);
        this.openUpdateConfirmation = this.openUpdateConfirmation.bind(this);
        this.updateSwaggerDefinition = this.updateSwaggerDefinition.bind(this);
        this.updateAsyncAPIDefinitionAndDeploy = this.updateAsyncAPIDefinitionAndDeploy.bind(this);
        this.updateSwaggerDefinitionAndDeploy = this.updateSwaggerDefinitionAndDeploy.bind(this);
        this.onChangeSwaggerContent = this.onChangeSwaggerContent.bind(this);
        this.updateAsyncAPIDefinition = this.updateAsyncAPIDefinition.bind(this);
        this.onChangeAsyncAPIContent = this.onChangeAsyncAPIContent.bind(this);
    }

    /**
     * @inheritdoc
     */
    componentDidMount() {
        const { api } = this.props;
        const { settings } = this.context;
        let promisedApi;
        if (api.type === 'GRAPHQL') {
            promisedApi = api.getSchema(api.id);
        } else if (api.type === 'WS' || api.type === 'WEBSUB' || api.type === 'SSE') {
            promisedApi = api.getAsyncAPIDefinition(api.id);
        } else {
            promisedApi = api.getSwagger(api.id);
        }

        this.setState({ securityAuditProperties: settings.securityAuditProperties });

        promisedApi
            .then((response) => {
                if (api.type === 'GRAPHQL') {
                    this.setState({
                        graphQL: response.obj.schemaDefinition,
                        format: 'txt',
                    });
                } else if (api.type === 'WS' || api.type === 'WEBSUB' || api.type === 'SSE') {
                    this.setState({
                        asyncAPI: YAML.safeDump(YAML.safeLoad(response.data)),
                        asyncAPIModified: YAML.safeDump(YAML.safeLoad(response.data)),
                        format: 'yaml',
                        convertTo: this.getConvertToFormat('yaml'),
                    });
                } else {
                    this.setState({
                        swagger: YAML.safeDump(YAML.safeLoad(response.data)),
                        swaggerModified: YAML.safeDump(YAML.safeLoad(response.data)),
                        format: 'yaml',
                        convertTo: this.getConvertToFormat('yaml'),
                    });
                }
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const { status } = error;
                if (status === 404) {
                    this.setState({ notFound: true });
                } else if (status === 401) {
                    doRedirectToLogin();
                }
            });
    }


    /**
     * Method to handle asyncAPI content change
     *
     * @param {string} modifiedContent : The modified asyncAPI content.
     * */
    onChangeAsyncAPIContent(modifiedContent) {
        const { format } = this.state;
        /**
         * Validate for the basic json/ yaml format.
         * */
        try {
            if (format === 'json') {
                JSON.parse(modifiedContent, null);
            } else {
                YAML.load(modifiedContent);
            }
            this.setState({ isAsyncAPIValid: true, asyncAPIModified: modifiedContent });
        } catch (e) {
            this.setState({ isAsyncAPIValid: false, asyncAPIModified: modifiedContent });
        }
    }

    /**
     * Toggle the format of the api definition.
     * JSON -> YAML, YAML -> JSON
     */
    onChangeFormatClick() {
        const {
            format, swagger, convertTo, asyncAPI,
        } = this.state;
        let formattedString = '';
        if (asyncAPI === null) {
            if (convertTo === 'json') {
                formattedString = JSON.stringify(YAML.load(swagger), null, 1);
            } else {
                formattedString = YAML.safeDump(YAML.safeLoad(swagger));
            }
            this.setState({
                swagger: formattedString,
                swaggerModified: formattedString,
                format: convertTo,
                convertTo: format,
            });
        } else {
            if (convertTo === 'json') {
                formattedString = JSON.stringify(YAML.load(asyncAPI), null, 1);
            } else {
                formattedString = YAML.safeDump(YAML.safeLoad(asyncAPI));
            }
            this.setState({
                asyncAPI: formattedString,
                asyncAPIModified: formattedString,
                format: convertTo,
                convertTo: format,
            });
        }
    }

    /**
      * Set isAuditApiClicked to true when Audit API is clicked
      */
    onAuditApiClick() {
        this.setState({ isAuditApiClicked: true });
    }

    /**
     * Method to handle swagger content change
     *
     * @param {string} modifiedContent : The modified swagger content.
     * */
    onChangeSwaggerContent(modifiedContent) {
        const { format } = this.state;
        /**
         * Validate for the basic json/ yaml format.
         * */
        try {
            if (format === 'json') {
                JSON.parse(modifiedContent, null);
            } else {
                YAML.load(modifiedContent);
            }
            this.setState({ isSwaggerValid: true, swaggerModified: modifiedContent });
        } catch (e) {
            this.setState({ isSwaggerValid: false, swaggerModified: modifiedContent });
        }
    }

    setSchemaDefinition = (schemaContent, contentType) => {
        const { api } = this.props;
        const isGraphql = api.isGraphql();
        const isWebSocket = api.isWebSocket();
        const isWebSub = api.isWebSub();
        if (isGraphql) {
            this.setState({ graphQL: schemaContent });
        } else if (isWebSocket || isWebSub) {
            this.setState({
                asyncAPI: schemaContent,
                asyncAPIModified: schemaContent,
                convertTo: this.getConvertToFormat(contentType),
                format: contentType,
            });
        } else {
            this.setState({
                swagger: schemaContent,
                swaggerModified: schemaContent,
                convertTo: this.getConvertToFormat(contentType),
                format: contentType,
            });
        }
    };

    /**
     * Util function to get the format which the definition can be converted to.
     * @param {*} format : The current format of definition.
     * @returns {string} The possible conversion format.
     */
    getConvertToFormat(format) {
        return format === 'json' ? 'yaml' : 'json';
    }

    /**
     * Handles the No button action of the save api definition confirmation dialog box.
     */
    handleNo() {
        this.setState({ openDialog: false });
    }

    /**
     * Handles the yes button action of the save api definition confirmation dialog box.
     */
    handleSave() {
        const { swaggerModified, asyncAPIModified } = this.state;
        if (asyncAPIModified !== null) {
            this.setState({ openDialog: false }, () => this.updateAsyncAPIDefinition(asyncAPIModified, '', ''));
        } else {
            this.setState({ openDialog: false }, () => this.updateSwaggerDefinition(swaggerModified, '', ''));
        }
    }

    handleSaveAndDeploy() {
        const { swaggerModified, asyncAPIModified } = this.state;
        const { api, history } = this.props;
        if (asyncAPIModified !== null) {
            this.updateAsyncAPIDefinitionAndDeploy(asyncAPIModified, '', '');
            history.push({
                pathname: api.isAPIProduct() ? `/api-products/${api.id}/deployments`
                    : `/apis/${api.id}/deployments`,
                state: 'deploy',
            });
        } else {
            this.updateSwaggerDefinitionAndDeploy(swaggerModified, '', '');
            history.push({
                pathname: api.isAPIProduct() ? `/api-products/${api.id}/deployments`
                    : `/apis/${api.id}/deployments`,
                state: 'deploy',
            });
        }
    }

    /**
     * Checks whether the swagger content is json type.
     * @param {string} definition The swagger string.
     * @return {boolean} Whether the content is a json or not.
     * */
    hasJsonStructure(definition) {
        if (typeof definition !== 'string') return false;
        try {
            const result = JSON.parse(definition);
            return result && typeof result === 'object';
        } catch (err) {
            return false;
        }
    }

    /**
     * Method to set the state for opening the swagger editor drawer.
     * Swagger editor loads the definition content from the local storage. Hence we set the swagger content to the
     * local storage.
     * */
    openEditor() {
        this.setState({ openEditor: true });
    }

    /**
     * Sets the state to close the swagger-editor drawer.
     * */
    closeEditor() {
        this.setState({ openEditor: false });
        const { intl, api, history } = this.props;
        const { isAuditApiClicked } = this.state;
        if (isAuditApiClicked === true) {
            Alert.info(intl.formatMessage({
                id: 'Apis.Details.APIDefinition.info.updating.auditapi',
                defaultMessage: 'To reflect the changes made, you need to click Audit API',
            }));
            const redirectUrl = '/apis/' + api.id + '/api definition';
            history.push(redirectUrl);
        }
    }

    /**
     * Handles the transition of the drawer.
     * @param {object} props list of props
     * @return {object} The Slide transition component
     * */
    transition(props) {
        return <Slide direction='up' {...props} />;
    }

    /**
     * Updates swagger content in the local storage.
     * */
    openUpdateConfirmation() {
        this.setState({ openDialog: true });
    }

    /**
     * Updates swagger definition of the api.
     * @param {string} swaggerContent The swagger file that needs to be updated.
     * @param {string} specFormat The current format of the definition
     * @param {string} toFormat The format it can be converted to.
     * */
    updateSwaggerDefinition(swaggerContent, specFormat, toFormat) {
        const { api, intl, updateAPI } = this.props;
        this.setState({ isUpdating: true });
        let parsedContent = {};
        if (this.hasJsonStructure(swaggerContent)) {
            parsedContent = JSON.parse(swaggerContent);
        } else {
            try {
                parsedContent = YAML.load(swaggerContent);
            } catch (err) {
                console.log(err);
                Alert.error(intl.formatMessage({
                    id: 'Apis.Details.APIDefinition.APIDefinition.error.while.parsing.api.definition',
                    defaultMessage: 'Error occurred while updating the API Definition',
                }));
                return;
            }
        }
        const promise = api.updateSwagger(parsedContent);
        promise
            .then((response) => {
                const { endpointImplementationType } = api;
                if (endpointImplementationType === 'INLINE') {
                    api.generateMockScripts(api.id);
                }
                if (response) {
                    Alert.success(intl.formatMessage({
                        id: 'Apis.Details.APIDefinition.APIDefinition.api.definition.updated.successfully',
                        defaultMessage: 'API Definition updated successfully',
                    }));
                    if (specFormat && toFormat) {
                        this.setState({ swagger: swaggerContent, format: specFormat, convertTo: toFormat });
                    } else {
                        this.setState({ swagger: swaggerContent });
                    }
                }
                /*
                 *updateAPI() will make a /GET call to get the latest api once the swagger definition is updated.
                 *Otherwise, we need to refresh the page to get changes.
                 */
                updateAPI();
                this.setState({ isUpdating: false });
            })
            .catch((err) => {
                console.log(err);
                const { response: { body: { description, message } } } = err;
                if (description && message) {
                    Alert.error(`${message} ${description}`);
                } else {
                    Alert.error(intl.formatMessage({
                        id: 'Apis.Details.APIDefinition.APIDefinition.error.while.updating.api.definition',
                        defaultMessage: 'Error occurred while updating the API Definition',
                    }));
                }

                this.setState({ isUpdating: false });
            });
    }

    /**
     * Updates swagger definition of the api.
     * @param {string} swaggerContent The swagger file that needs to be updated.
     * @param {string} specFormat The current format of the definition
     * @param {string} toFormat The format it can be converted to.
     * */
    updateSwaggerDefinitionAndDeploy(swaggerContent, specFormat, toFormat) {
        const { api, intl } = this.props;
        this.setState({ isUpdating: true });
        let parsedContent = {};
        if (this.hasJsonStructure(swaggerContent)) {
            parsedContent = JSON.parse(swaggerContent);
        } else {
            try {
                parsedContent = YAML.load(swaggerContent);
            } catch (err) {
                console.log(err);
                Alert.error(intl.formatMessage({
                    id: 'Apis.Details.APIDefinition.APIDefinition.error.while.parsing.api.definition',
                    defaultMessage: 'Error occurred while updating the API Definition',
                }));
                return;
            }
        }
        const promise = api.updateSwagger(parsedContent);
        promise
            .then((response) => {
                const { endpointImplementationType } = api;
                if (endpointImplementationType === 'INLINE') {
                    api.generateMockScripts(api.id);
                }
                if (response) {
                    Alert.success(intl.formatMessage({
                        id: 'Apis.Details.APIDefinition.APIDefinition.api.definition.updated.successfully',
                        defaultMessage: 'API Definition updated successfully',
                    }));
                    if (specFormat && toFormat) {
                        this.setState({ swagger: swaggerContent, format: specFormat, convertTo: toFormat });
                    } else {
                        this.setState({ swagger: swaggerContent });
                    }
                }
            })
            .catch((err) => {
                console.log(err);
                const { response: { body: { description, message } } } = err;
                if (description && message) {
                    Alert.error(`${message} ${description}`);
                } else {
                    Alert.error(intl.formatMessage({
                        id: 'Apis.Details.APIDefinition.APIDefinition.error.while.updating.api.definition',
                        defaultMessage: 'Error occurred while updating the API Definition',
                    }));
                }
            });
    }


    /**
     * Updates asyncAPI definition of the API
     * @param {string} asyncAPIContent The AsyncAPi file that needs to be updated.
     * @param {string} specFormat The current format of the definition
     * @param {string} toFormat The format it can be converted to.
     */
    updateAsyncAPIDefinitionAndDeploy(asyncAPIContent, specFormat, toFormat) {
        const { api, intl } = this.props;
        this.setState({ isUpdating: true });
        let parsedContent = {};
        if (this.hasJsonStructure(asyncAPIContent)) {
            parsedContent = JSON.parse(asyncAPIContent);
        } else {
            try {
                parsedContent = YAML.load(asyncAPIContent);
            } catch (err) {
                console.log(err);
                Alert.error(intl.formatMessage({
                    id: 'Apis.Details.APIDefinition.APIDefinition.error.while.updating.async.api.definition',
                    defaultMessage: 'Error occurred while updating the API Definition',
                }));
                return;
            }
        }
        const promise = api.updateAsyncAPIDefinition(parsedContent);
        promise
            .then((response) => {
                /* const { endpointImplementationType } = api; */
                /* if (endpointImplementationType === 'INLINE') {
                    api.generateMockScripts(api.id);
                } */
                if (response) {
                    Alert.success(intl.formatMessage({
                        id: 'Apis.Details.APIDefinition.APIDefinition.async.api.definition.updated.successfully',
                        defaultMessage: 'API Definition updated successfully',
                    }));
                    if (specFormat && toFormat) {
                        this.setState({ asyncAPI: asyncAPIContent, format: specFormat, convertTo: toFormat });
                    } else {
                        this.setState({ asyncAPI: asyncAPIContent });
                    }
                }
            })
            .catch((err) => {
                console.log(err);
                const { response: { body: { description, message } } } = err;
                if (description && message) {
                    Alert.error(`${message} ${description}`);
                } else {
                    Alert.error(intl.formatMessage({
                        id: 'Apis.Details.APIDefinition.APIDefinition.error.while.updating.async.api.definition',
                        defaultMessage: 'Error occurred while updating the API Definition',
                    }));
                }
                this.setState({ isUpdating: false });
            });
    }

    /**
     * Updates asyncAPI definition of the API
     * @param {string} asyncAPIContent The AsyncAPi file that needs to be updated.
     * @param {string} specFormat The current format of the definition
     * @param {string} toFormat The format it can be converted to.
     */
    updateAsyncAPIDefinition(asyncAPIContent, specFormat, toFormat) {
        const { api, intl, updateAPI } = this.props;
        this.setState({ isUpdating: true });
        let parsedContent = {};
        if (this.hasJsonStructure(asyncAPIContent)) {
            parsedContent = JSON.parse(asyncAPIContent);
        } else {
            try {
                parsedContent = YAML.load(asyncAPIContent);
            } catch (err) {
                console.log(err);
                Alert.error(intl.formatMessage({
                    id: 'Apis.Details.APIDefinition.APIDefinition.error.while.updating.async.api.definition',
                    defaultMessage: 'Error occurred while updating the API Definition',
                }));
                return;
            }
        }
        const promise = api.updateAsyncAPIDefinition(parsedContent);
        promise
            .then((response) => {
                /* const { endpointImplementationType } = api; */
                /* if (endpointImplementationType === 'INLINE') {
                    api.generateMockScripts(api.id);
                } */
                if (response) {
                    Alert.success(intl.formatMessage({
                        id: 'Apis.Details.APIDefinition.APIDefinition.async.api.definition.updated.successfully',
                        defaultMessage: 'API Definition updated successfully',
                    }));
                    if (specFormat && toFormat) {
                        this.setState({ asyncAPI: asyncAPIContent, format: specFormat, convertTo: toFormat });
                    } else {
                        this.setState({ asyncAPI: asyncAPIContent });
                    }
                }
                /*
                 * updateAPI() will make a /GET call to get the latest api once the asyncAPI definition is updated.
                 * Otherwise, we need to refresh the page to get changes.
                 */
                updateAPI();
                this.setState({ isUpdating: false });
            })
            .catch((err) => {
                console.log(err);
                const { response: { body: { description, message } } } = err;
                if (description && message) {
                    Alert.error(`${message} ${description}`);
                } else {
                    Alert.error(intl.formatMessage({
                        id: 'Apis.Details.APIDefinition.APIDefinition.error.while.updating.async.api.definition',
                        defaultMessage: 'Error occurred while updating the API Definition',
                    }));
                }
                this.setState({ isUpdating: false });
            });
    }


    /**
     * @inheritdoc
     */
    render() {
        const {
            swagger, graphQL, openEditor, openDialog, format, convertTo, notFound, isAuditApiClicked,
            securityAuditProperties, isSwaggerValid, swaggerModified, isUpdating,
            asyncAPI, asyncAPIModified, isAsyncAPIValid,
        } = this.state;

        const {
            classes, resourceNotFountMessage, api,
        } = this.props;

        let downloadLink;
        let fileName;
        let isGraphQL = 0;

        if (graphQL !== null) {
            downloadLink = 'data:text/' + format + ';charset=utf-8,' + encodeURIComponent(graphQL);
            fileName = api.provider + '-' + api.name + '-' + api.version + '.graphql';
            isGraphQL = 1;
        } else if (asyncAPI !== null) {
            downloadLink = 'data:text/' + format + ';charset=utf-8,' + encodeURIComponent(asyncAPI);
            fileName = 'asyncapi.' + format;
        } else {
            downloadLink = 'data:text/' + format + ';charset=utf-8,' + encodeURIComponent(swagger);
            fileName = 'swagger.' + format;
        }
        const editorOptions = {
            selectOnLineNumbers: true,
            readOnly: true,
            smoothScrolling: true,
            wordWrap: 'on',
        };

        if (notFound) {
            return <ResourceNotFound message={resourceNotFountMessage} />;
        }
        if (!swagger && !graphQL && !asyncAPI && api === 'undefined') {
            return <Progress />;
        }

        return (
            <>
                <div className={classes.topBar}>
                    <div className={classes.titleWrapper}>
                        <Typography id='itest-api-details-api-definition-head' variant='h4'>
                            {/* eslint-disable-next-line no-nested-ternary */}
                            {graphQL ? (
                                <FormattedMessage
                                    id='Apis.Details.APIDefinition.APIDefinition.schema.definition'
                                    defaultMessage='Schema Definition'
                                />
                            ) : asyncAPI ? (
                                <FormattedMessage
                                    id='Apis.Details.APIDefinition.APIDefinition.asyncAPI.definition'
                                    defaultMessage='AsyncAPI Definition'
                                />
                            ) : (
                                <FormattedMessage
                                    id='Apis.Details.APIDefinition.APIDefinition.api.definition'
                                    defaultMessage='API Definition'
                                />
                            )}
                        </Typography>
                        {asyncAPI ? (
                            <Button
                                size='small'
                                className={classes.button}
                                onClick={this.openEditor}
                                disabled={isRestricted(['apim:api_create'], api)}
                            >
                                <EditRounded className={classes.buttonIcon} />
                                <FormattedMessage
                                    id='Apis.Details.APIDefinition.APIDefinition.edit'
                                    defaultMessage='Edit'
                                />
                            </Button>
                        ) : (
                            !(graphQL || api.type === API.CONSTS.APIProduct) && (
                                <Button
                                    size='small'
                                    className={classes.button}
                                    onClick={this.openEditor}
                                    disabled={isRestricted(['apim:api_create'], api) || api.isRevision}
                                >
                                    <EditRounded className={classes.buttonIcon} />
                                    <FormattedMessage
                                        id='Apis.Details.APIDefinition.APIDefinition.edit'
                                        defaultMessage='Edit'
                                    />
                                </Button>
                            )
                        )}
                        {api.type !== API.CONSTS.APIProduct && (
                            <ImportDefinition setSchemaDefinition={this.setSchemaDefinition} />
                        )}
                        {api.serviceInfo && api.serviceInfo.outdated && (
                            <DefinitionOutdated
                                api={api}
                                classes={classes}
                            />
                        )}
                        <a className={classes.downloadLink} href={downloadLink} download={fileName}>
                            <Button size='small' className={classes.button}>
                                <CloudDownloadRounded className={classes.buttonIcon} />
                                <FormattedMessage
                                    id='Apis.Details.APIDefinition.APIDefinition.download.definition'
                                    defaultMessage='Download Definition'
                                />
                            </Button>
                        </a>

                        {(securityAuditProperties.apiToken && securityAuditProperties.collectionId
                        && api.type !== 'GRAPHQL' && !asyncAPI)
                            && (
                                <Button size='small' className={classes.button} onClick={this.onAuditApiClick}>
                                    <LockRounded className={classes.buttonIcon} />
                                    <FormattedMessage
                                        id='Apis.Details.APIDefinition.APIDefinition.audit.api'
                                        defaultMessage='Audit API'
                                    />
                                </Button>
                            )}

                        {isRestricted(['apim:api_create'], api) && (
                            <Typography variant='body2' color='primary'>
                                <FormattedMessage
                                    id='Apis.Details.APIDefinition.APIDefinition.update.not.allowed'
                                    defaultMessage='Unauthorized: Insufficient permissions to update API Definition'
                                />
                            </Typography>
                        )}
                    </div>
                    {isGraphQL === 0 && (
                        <div className={classes.titleWrapper}>
                            <Button size='small' className={classes.button} onClick={this.onChangeFormatClick}>
                                <SwapHorizontalCircle className={classes.buttonIcon} />
                                <FormattedMessage
                                    id='Apis.Details.APIDefinition.APIDefinition.convert.to'
                                    defaultMessage='Convert to'
                                />
                                {' '}
                                {convertTo}
                            </Button>
                        </div>
                    )}
                </div>
                <div>
                    <Suspense fallback={<Progress />}>
                        {isAuditApiClicked ? (
                            <APISecurityAudit apiId={api.id} />
                        ) : (
                            <MonacoEditor
                                language={format}
                                width='100%'
                                height='calc(100vh - 51px)'
                                theme='vs-dark'
                                /* eslint-disable-next-line no-nested-ternary */
                                value={swagger !== null ? swagger : asyncAPI !== null ? asyncAPI : graphQL}
                                options={editorOptions}
                            />
                        )}
                    </Suspense>
                </div>
                <Dialog fullScreen open={openEditor} onClose={this.closeEditor} TransitionComponent={this.transition}>
                    <Paper square className={classes.popupHeader}>
                        <IconButton
                            className={classes.button}
                            color='inherit'
                            onClick={this.closeEditor}
                            aria-label={(
                                <FormattedMessage
                                    id='Apis.Details.APIDefinition.APIDefinition.btn.close'
                                    defaultMessage='Close'
                                />
                            )}
                        >
                            <Icon>close</Icon>
                        </IconButton>

                        <Button
                            className={classes.button}
                            variant='contained'
                            color='primary'
                            onClick={this.openUpdateConfirmation}
                            disabled={(!isSwaggerValid || isUpdating) || (!isAsyncAPIValid || isUpdating)}
                        >
                            <FormattedMessage
                                id='Apis.Details.APIDefinition.APIDefinition.documents.swagger.editor.update.content'
                                defaultMessage='Update Content'
                            />
                            {isUpdating && <CircularProgress className={classes.progressLoader} size={24} />}
                        </Button>
                    </Paper>
                    <Suspense
                        fallback={(
                            <Progress />
                        )}
                    >
                        {swagger ? (
                            <EditorDialog
                                swagger={swaggerModified}
                                language={format}
                                onEditContent={this.onChangeSwaggerContent}
                            />
                        ) : (
                            <AsyncAPIEditor
                                asyncAPI={asyncAPIModified}
                                language={format}
                                onEditContent={this.onChangeAsyncAPIContent}
                            />
                        )}
                    </Suspense>
                </Dialog>
                <Dialog
                    open={openDialog}
                    onClose={this.handleNo}
                    aria-labelledby='alert-dialog-title'
                    aria-describedby='alert-dialog-description'
                >
                    <DialogTitle id='alert-dialog-title'>
                        <Typography align='left'>
                            <FormattedMessage
                                id='Apis.Details.APIDefinition.APIDefinition.save.api.definition'
                                defaultMessage='Save API Definition'
                            />
                        </Typography>
                    </DialogTitle>
                    <DialogContent>
                        <DialogContentText id='alert-dialog-description'>
                            <FormattedMessage
                                id='Apis.Details.APIDefinition.APIDefinition.api.definition.save.confirmation'
                                defaultMessage={
                                    'Are you sure you want to save the API Definition? This might affect the'
                                    + ' existing resources.'
                                }
                            />
                        </DialogContentText>
                    </DialogContent>
                    <DialogActions>
                        <Grid
                            container
                            direction='row'
                            alignItems='flex-start'
                            spacing={1}
                        >
                            <Grid item>
                                <Button onClick={this.handleNo} color='primary'>
                                    <FormattedMessage
                                        id='Apis.Details.APIDefinition.APIDefinition.btn.no'
                                        defaultMessage='CANCEL'
                                    />
                                </Button>
                            </Grid>
                            <Grid item>
                                <CustomSplitButton
                                    handleSave={this.handleSave}
                                    handleSaveAndDeploy={this.handleSaveAndDeploy}
                                    isUpdating={isUpdating}
                                />
                            </Grid>
                        </Grid>
                    </DialogActions>
                </Dialog>
            </>
        );
    }
}

APIDefinition.contextType = AppContext;
APIDefinition.propTypes = {
    classes: PropTypes.shape({
        button: PropTypes.shape({}),
        popupHeader: PropTypes.shape({}),
        buttonIcon: PropTypes.shape({}),
        root: PropTypes.shape({}),
        topBar: PropTypes.shape({}),
        titleWrapper: PropTypes.shape({}),
        mainTitle: PropTypes.shape({}),
        converterWrapper: PropTypes.shape({}),
        dropzone: PropTypes.shape({}),
        downloadLink: PropTypes.shape({}),
    }).isRequired,
    api: PropTypes.shape({
        updateSwagger: PropTypes.func,
        getSwagger: PropTypes.func,
        id: PropTypes.string,
        apiType: PropTypes.oneOf([API.CONSTS.API, API.CONSTS.APIProduct]),
    }).isRequired,
    history: PropTypes.shape({
        push: PropTypes.shape({}),
    }).isRequired,
    location: PropTypes.shape({
        pathname: PropTypes.shape({}),
    }).isRequired,
    resourceNotFountMessage: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({
        formatMessage: PropTypes.func,
    }).isRequired,
    updateAPI: PropTypes.func.isRequired,
};
export default withRouter(injectIntl(withStyles(styles, { withTheme: true })(APIDefinition)));
