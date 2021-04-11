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
import React, { useReducer, useState } from 'react';
import { makeStyles } from '@material-ui/core/styles';
import PropTypes from 'prop-types';
import Button from '@material-ui/core/Button';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogTitle from '@material-ui/core/DialogTitle';
import { FormattedMessage, useIntl } from 'react-intl';
import { Typography } from '@material-ui/core';
import CloudUploadRounded from '@material-ui/icons/CloudUploadRounded';
import Alert from 'AppComponents/Shared/Alert';
import API from 'AppData/api.js';
import { isRestricted } from 'AppData/AuthManager';
import { useAPI } from 'AppComponents/Apis/Details/components/ApiContext';
import CircularProgress from '@material-ui/core/CircularProgress';
import ProvideWSDL from 'AppComponents/Apis/Create/WSDL/Steps/ProvideWSDL';
import ProvideAsyncAPI from 'AppComponents/Apis/Create/AsyncAPI/Steps/ProvideAsyncAPI';
import ProvideOpenAPI from '../../Create/OpenAPI/Steps/ProvideOpenAPI';
import ProvideGraphQL from '../../Create/GraphQL/Steps/ProvideGraphQL';

const useStyles = makeStyles(() => ({
    importDefinitionDialogHeader: {
        fontWeight: '600',
    },
    buttonIcon: {
        marginRight: 10,
    },
}));

/**
 *
 *
 * @export
 * @param {*} props
 * @returns
 */
export default function ImportDefinition(props) {
    const { setSchemaDefinition } = props;
    const classes = useStyles();
    const [openAPIDefinitionImport, setOpenAPIDefinitionImport] = useState(false);
    const [isImporting, setIsImporting] = useState(false);
    const [api] = useAPI();
    const intl = useIntl();
    const isGraphQL = api.isGraphql();
    const isSOAP = api.isSOAP();
    // const isWebSocket = api.isWebSocket();
    // const isWebSub = api.isWebSub();
    const isAsyncAPI = api && (api.type === 'WS' || api.type === 'WEBSUB' || api.type === 'SSE');
    const [asyncAPIDefinitionImport, setAsyncAPIDefinitionImport] = useState(false);

    const handleAPIDefinitionImportOpen = () => {
        // eslint-disable-next-line no-unused-expressions
        isAsyncAPI ? setAsyncAPIDefinitionImport(true) : setOpenAPIDefinitionImport(true);
        // isWebSocket || isWebSub ? setAsyncAPIDefinitionImport(true) : setOpenAPIDefinitionImport(true);
    };

    const handleAPIDefinitionImportCancel = () => {
        // eslint-disable-next-line no-unused-expressions
        isAsyncAPI ? setAsyncAPIDefinitionImport(false) : setOpenAPIDefinitionImport(false);
        // isWebSocket || isWebSub ? setAsyncAPIDefinitionImport(false) : setOpenAPIDefinitionImport(false);
    };

    function apiInputsReducer(currentState, inputAction) {
        const { action, value } = inputAction;
        switch (action) {
            case 'inputValue':
            case 'isFormValid':
                return { ...currentState, [action]: value };
            case 'inputType':
                return { ...currentState, [action]: value, inputValue: value === 'url' ? '' : null };
            case 'preSetAPI':
                return {
                    ...currentState,
                    content: value.content,
                };
            default:
                return currentState;
        }
    }

    const [apiInputs, inputsDispatcher] = useReducer(apiInputsReducer, {
        type: '',
        inputType: 'url',
        inputValue: '',
        formValidity: false,
        mode: 'update',
    });

    /**
     * Updates OpenAPI definition
     */
    function updateOASDefinition() {
        setIsImporting(true);
        const {
            inputValue, inputType, content,
        } = apiInputs;
        const isFileInput = inputType === 'file';
        if (isFileInput) {
            const reader = new FileReader();
            const contentType = inputValue.type.includes('yaml') ? 'yaml' : 'json';
            reader.onloadend = (event) => {
                setSchemaDefinition(event.currentTarget.result, contentType);
            };
            reader.readAsText(inputValue);
        }
        const newAPI = new API();
        const promisedResponse = isFileInput ? newAPI.updateAPIDefinitionByFile(api.id, inputValue)
            : newAPI.updateAPIDefinitionByUrl(api.id, inputValue);
        promisedResponse
            .then(() => {
                Alert.success(intl.formatMessage({
                    id: 'Apis.Details.APIDefinition.APIDefinition.api.definition.oas.updated.successfully',
                    defaultMessage: 'API Definition Updated Successfully',
                }));
                setOpenAPIDefinitionImport(false);
                if (!isFileInput) {
                    // Test to starting the content with'{' character ignoring white spaces
                    const isJSONRegex = RegExp(/^\s*{/); // TODO: not a solid test need to support from REST API ~tmkb
                    const contentType = isJSONRegex.test(content) ? 'json' : 'yaml';
                    setSchemaDefinition(content, contentType);
                }
            })
            .catch((error) => {
                console.error(error);
                Alert.error(intl.formatMessage({
                    id: 'Apis.Details.APIDefinition.APIDefinition.error.while.updating.import.api.definition',
                    defaultMessage: 'Error while updating the API Definition',
                }));
            }).finally(() => setIsImporting(false));
    }

    /**
     * Updates AsyncAPI definition
     */
    function updateAsyncAPIDefinition() {
        setIsImporting(true);
        const {
            inputValue, inputType, content,
        } = apiInputs;
        const isFileInput = inputType === 'file';
        if (isFileInput) {
            const reader = new FileReader();
            // eslint-disable-next-line no-nested-ternary
            const contentType = inputValue.type.includes('yaml') ? 'yaml'
                : inputValue.type.includes('yml') ? 'yml' : 'json';
            reader.onloadend = (event) => {
                setSchemaDefinition(event.currentTarget.result, contentType);
            };
            reader.readAsText(inputValue);
        }
        const newAPI = new API();
        const promisedResponse = isFileInput ? newAPI.updateAsyncAPIDefinitionByFile(api.id, inputValue)
            : newAPI.updateAsyncAPIDefinitionByUrl(api.id, inputValue);
        promisedResponse
            .then(() => {
                Alert.success(intl.formatMessage({
                    id: 'Apis.Details.APIDefinition.APIDefinition.async.api.import.definition.updated.successfully',
                    defaultMessage: 'API Definition Updated Successfully',
                }));
                setAsyncAPIDefinitionImport(false);
                if (!isFileInput) {
                    // Test to starting the content with'{' character ignoring white spaces
                    const isJSONRegex = RegExp(/^\s*{/); // TODO: not a solid test need to support from REST API ~tmkb
                    const contentType = isJSONRegex.test(content) ? 'json' : 'yaml';
                    setSchemaDefinition(content, contentType);
                }
            })
            .catch((error) => {
                console.error(error);
                Alert.error(intl.formatMessage({
                    id: 'Apis.Details.APIDefinition.APIDefinition.error.while.updating.import.async.api.definition',
                    defaultMessage: 'Error while updating the API Definition',
                }));
            }).finally(() => setIsImporting(false));
    }

    /**
     * Update the graphQL api with its operation
     * @param {*}  api
     * @param {*}  graphQLInfo
     */
    function updateGraphQLAPIDefinition(graphQLSchema) {
        const promisedAPI = api.updateGraphQLAPIDefinition(api.id, graphQLSchema);
        promisedAPI
            .then((response) => {
                api.operations = response.obj;
                Alert.success(intl.formatMessage({
                    id: 'Apis.Details.APIDefinition.APIDefinition.graphQLDefinition.updated.successfully',
                    defaultMessage: 'Schema Definition Updated Successfully',
                }));
                setOpenAPIDefinitionImport(false);
                setSchemaDefinition(graphQLSchema);
            })
            .catch((err) => {
                console.log(err);
                Alert.error(intl.formatMessage({
                    id: 'Apis.Details.APIDefinition.APIDefinition.error.updating.graphQL.schema',
                    defaultMessage: 'Error while updating GraphQL schema',
                }));
            });
    }

    /**
     * Updates GraphQL schema definition
     */
    function updateGraphQLSchema() {
        const {
            inputValue,
        } = apiInputs;

        const promisedValidation = API.validateGraphQLFile(inputValue);
        promisedValidation
            .then((response) => {
                const { isValid, graphQLInfo } = response.obj;
                if (isValid === true) {
                    updateGraphQLAPIDefinition(graphQLInfo.graphQLSchema.schemaDefinition);
                }
            })
            .catch((err) => {
                console.log(err);
                Alert.error(intl.formatMessage({
                    id: 'Error.while.validating.the.imported.graphQLSchema',
                    defaultMessage: 'Error while validating the imported schema',
                }));
            });
    }

    /**
     * Updates WSDL definition
     */
    function updateWSDL() {
        const {
            inputType, inputValue,
        } = apiInputs;
        const isFileInput = inputType === 'file';
        let promisedAPI;
        if (isFileInput) {
            promisedAPI = api.updateWSDLByFileOrArchive(api.id, inputValue);
        } else {
            promisedAPI = api.updateWSDLByUrl(api.id, inputValue);
        }
        promisedAPI
            .then(() => {
                Alert.success(intl.formatMessage({
                    id: 'Apis.Details.APIDefinition.ImportDefinition.WSDL.updated.successfully',
                    defaultMessage: 'WSDL Updated Successfully',
                }));
                setOpenAPIDefinitionImport(false);
                setSchemaDefinition(isFileInput && inputValue.type === 'application/zip');
            })
            .catch((err) => {
                console.log(err);
                Alert.error(intl.formatMessage({
                    id: 'Apis.Details.APIDefinition.ImportDefinition.error.updating.WSDL',
                    defaultMessage: 'Error while updating WSDL',
                }));
            });
    }

    /**
     * Handles API definition import
     */
    function importDefinition() {
        if (isGraphQL) {
            updateGraphQLSchema();
        } else if (isSOAP) {
            updateWSDL();
        // } else if (isWebSocket || isWebSub) {
        } else if (isAsyncAPI) {
            updateAsyncAPIDefinition();
        } else {
            updateOASDefinition();
        }
    }
    /**
     *
     * Set the validity of the API definition imports
     * @param {*} isValidForm
     * @param {*} validationState
     */
    function handleOnValidate(isFormValid) {
        inputsDispatcher({
            action: 'isFormValid',
            value: isFormValid,
        });
    }

    let dialogTitle = (
        <FormattedMessage
            id='Apis.Details.APIDefinition.APIDefinition.import.definition.oas'
            defaultMessage='Import OpenAPI Definition'
        />
    );
    let dialogContent = (
        <ProvideOpenAPI
            onValidate={handleOnValidate}
            apiInputs={apiInputs}
            inputsDispatcher={inputsDispatcher}
        />
    );
    let btnText = (
        <FormattedMessage
            id='Apis.Details.APIDefinition.APIDefinition.import.definition'
            defaultMessage='Import Definition'
        />
    );
    if (isGraphQL) {
        dialogTitle = (
            <FormattedMessage
                id='Apis.Details.APIDefinition.APIDefinition.import.definition.graphql'
                defaultMessage='Import GraphQL Schema Definition'
            />
        );
        dialogContent = (
            <ProvideGraphQL
                onValidate={handleOnValidate}
                apiInputs={apiInputs}
                inputsDispatcher={inputsDispatcher}
            />
        );
    }
    if (isSOAP) {
        dialogTitle = (
            <FormattedMessage
                id='Apis.Details.APIDefinition.APIDefinition.import.definition.wsdl'
                defaultMessage='Import WSDL'
            />
        );
        dialogContent = (
            <ProvideWSDL
                onValidate={handleOnValidate}
                apiInputs={apiInputs}
                inputsDispatcher={inputsDispatcher}
            />
        );
        btnText = (
            <FormattedMessage
                id='Apis.Details.APIDefinition.APIDefinition.import.wsdl'
                defaultMessage='Import WSDL'
            />
        );
    }
    // if (isWebSocket || isWebSub) {
    if (isAsyncAPI) {
        dialogTitle = (
            <FormattedMessage
                id='Apis.Details.APIDefinition.APIDefinition.import.definition.asyncApi'
                defaultMessage='Import AsyncAPI Definition'
            />
        );
        dialogContent = (
            <ProvideAsyncAPI
                onValidate={handleOnValidate}
                apiInputs={apiInputs}
                inputsDispatcher={inputsDispatcher}
            />
        );
        btnText = (
            <FormattedMessage
                id='Apis.Details.APIDefinition.import.asyncAPI'
                defaultMessage='Import AsyncAPI'
            />
        );
    }

    return (
        <>
            {!isAsyncAPI && (
                <Button
                    size='small'
                    className={classes.button}
                    onClick={handleAPIDefinitionImportOpen}
                    disabled={isRestricted(['apim:api_create'], api) || api.isRevision}
                >
                    <CloudUploadRounded className={classes.buttonIcon} />
                    {btnText}
                </Button>
            )}
            <Dialog
                onBackdropClick={isAsyncAPI ? setAsyncAPIDefinitionImport : setOpenAPIDefinitionImport}
                open={isAsyncAPI ? asyncAPIDefinitionImport : openAPIDefinitionImport}
            >
                <DialogTitle>
                    <Typography className={classes.importDefinitionDialogHeader}>
                        {dialogTitle}
                    </Typography>
                </DialogTitle>
                <DialogContent>
                    {dialogContent}
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleAPIDefinitionImportCancel}>
                        <FormattedMessage
                            id='Apis.Details.APIDefinition.APIDefinition.import.definition.cancel'
                            defaultMessage='Cancel'
                        />
                    </Button>
                    <Button
                        onClick={importDefinition}
                        variant='contained'
                        color='primary'
                        disabled={!apiInputs.isFormValid || isImporting || api.isRevision}
                    >
                        <FormattedMessage
                            id='Apis.Details.APIDefinition.APIDefinition.import.definition.import'
                            defaultMessage='Import'
                        />
                        {isImporting && <CircularProgress size={20} />}
                    </Button>
                </DialogActions>
            </Dialog>
        </>
    );
}

ImportDefinition.propTypes = {
    setSchemaDefinition: PropTypes.func.isRequired,
};
