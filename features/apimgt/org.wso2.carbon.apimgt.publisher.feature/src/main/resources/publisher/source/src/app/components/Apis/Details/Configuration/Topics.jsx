/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React, {
    useReducer, useEffect, useState, useCallback,
} from 'react';
import Grid from '@material-ui/core/Grid';
import Paper from '@material-ui/core/Paper';
import { useAPI } from 'AppComponents/Apis/Details/components/ApiContext';
import cloneDeep from 'lodash.clonedeep';
import isEmpty from 'lodash/isEmpty';
import Alert from 'AppComponents/Shared/Alert';
import Banner from 'AppComponents/Shared/Banner';
import API from 'AppData/api';
import CircularProgress from '@material-ui/core/CircularProgress';
import PropTypes from 'prop-types';
import { isRestricted } from 'AppData/AuthManager';
import AsyncOperation from '../Resources/components/AsyncOperation';
import GroupOfOperations from '../Resources/components/operationComponents/asyncapi/GroupOfOperations';
import AddOperation from '../Resources/components/AddOperation';
import SubscriptionConfig from '../Resources/components/operationComponents/asyncapi/SubscriptionConfig';
import { extractAsyncAPIPathParameters } from '../Resources/operationUtils';
import SaveOperations from '../Resources/components/SaveOperations';

const verbMap = {
    sub: 'subscribe',
    pub: 'publish',
};

/**
 * This component handles the Resource page in API details though it's written in a sharable way
 * that anyone could use this to render resources in anywhere else if needed.
 *
 * @export
 * @returns {React.Component} @inheritdoc
 */
export default function Topics(props) {
    const {
        disableUpdate,
        disableAddOperation,
    } = props;

    const [api, updateAPI] = useAPI();
    const [pageError, setPageError] = useState(false);
    const [sharedScopes, setSharedScopes] = useState();
    const [sharedScopesByName, setSharedScopesByName] = useState();
    const [asyncAPISpec, setAsyncAPISpec] = useState({});
    const [securityDefScopes, setSecurityDefScopes] = useState({});
    const isAsyncAPI = api.type === 'WEBSUB' || api.type === 'WS' || api.type === 'SSE';
    const [markedOperations, setSelectedOperation] = useState({});

    /**
     *
     * @param {*} spec
     * @param {*} ref
     */
    function getRefTarget(spec, ref) {
        const arr = ref.split('/');
        const i = (arr[0] === '#') ? 1 : 0;
        let target = spec;
        for (let j = i; j < arr.length; j++) {
            target = target[arr[j]];
        }
        return target;
    }

    /**
     *
     * @param {*} spec
     * @param {*} parent
     */
    function resolveSpec(spec, source) {
        if (typeof source === 'object') {
            let o = {};
            Object.entries(source).forEach(([k, v]) => {
                if (v !== null) {
                    if (k !== '$ref') {
                        o[k] = resolveSpec(spec, v);
                    } else {
                        const resolvedRef = resolveSpec(spec, getRefTarget(spec, v));
                        o = { ...o, ...resolvedRef };
                    }
                }
            });
            return o;
        }
        return source;
    }

    /**
     *
     * @param {*} state
     * @param {*} configAction
     */
    function websubSubscriptionConfigReducer(state, configAction) {
        const { action, value } = configAction;
        const nextState = { ...state };
        switch (action) {
            case 'enable':
                nextState.enable = value;
                if (!value) {
                    nextState.secret = '';
                }
                break;
            case 'signingAlgorithm':
            case 'signatureHeader':
            case 'secret':
                nextState[action] = value;
                break;
            default:
                return nextState;
        }
        return nextState;
    }
    const initialWebsubSubscriptionConfig = api.websubSubscriptionConfiguration || {
        enable: false,
        signingAlgorithm: '',
        signatureHeader: '',
        secret: '',
    };

    const [websubSubscriptionConfiguration, websubSubscriptionConfigDispatcher] = useReducer(
        websubSubscriptionConfigReducer, initialWebsubSubscriptionConfig,
    );

    /**
     *
     *
     * @param {*} currenPaths
     * @param {*} action
     */
    function operationsReducer(currentOperations, operationAction) {
        const { action, data } = operationAction;
        const { target, verb, value } = data || {};
        const addedOperations = cloneDeep(currentOperations);
        let updatedOperation;
        if (target) {
            updatedOperation = cloneDeep(currentOperations[target]);
        }

        switch (action) {
            case 'init':
                setSelectedOperation({});
                return data || asyncAPISpec.channels;
            case 'description':
                updatedOperation[action] = value;
                return {
                    ...currentOperations,
                    [target]: { ...currentOperations[target], description: updatedOperation.description },
                };
            case 'uriMapping':
                return {
                    ...currentOperations,
                    [target]: {
                        ...currentOperations[target],
                        [verb]: {
                            ...currentOperations[target][verb],
                            'x-uri-mapping': value,
                        },
                    },
                };
            case 'authType':
                updatedOperation['x-auth-type'] = value ? 'Any' : 'None';
                return {
                    ...currentOperations,
                    [target]: { ...currentOperations[target], 'x-auth-type': updatedOperation['x-auth-type'] },
                };
            case 'add':
                // If target is not there add an empty object
                if (!addedOperations[data.target]) {
                    addedOperations[data.target] = {};
                }
                addedOperations[data.target].parameters = extractAsyncAPIPathParameters(data.target);
                // eslint-disable-next-line no-case-declarations
                let alreadyExistCount = 0;
                for (let currentVerb of data.verbs) {
                    currentVerb = verbMap[currentVerb];
                    if (addedOperations[data.target][currentVerb]) {
                        const message = `Operation already exist with ${data.target} and ${currentVerb}`;
                        Alert.warning(message);
                        console.warn(message);
                        alreadyExistCount++;
                    } else {
                        addedOperations[data.target][currentVerb] = { };
                    }
                }
                if (alreadyExistCount === data.verbs.length) {
                    Alert.error('Operation(s) already exist!');
                    return currentOperations;
                }
                return addedOperations;
            case 'parameter':
                updatedOperation.parameters = updatedOperation.parameters || { };
                updatedOperation.parameters[value.name] = { ...value };
                delete updatedOperation.parameters[value.name].name;
                return {
                    ...currentOperations,
                    [target]: { ...currentOperations[target], parameters: updatedOperation.parameters },
                };
            case 'addPayloadProperty':
                updatedOperation[verb].message = updatedOperation[verb].message || { };
                updatedOperation[verb].message.payload = updatedOperation[verb].message.payload || { };
                updatedOperation[verb].message.payload.type = 'object';
                updatedOperation[verb].message.payload.properties = updatedOperation[verb].message.payload.properties
                    || { };
                updatedOperation[verb].message.payload.properties[value.name] = {
                    description: value.description,
                    type: value.type,
                };
                break;
            case 'deletePayloadProperty':
                delete updatedOperation[verb].message.payload.properties[value];
                break;
            case 'payloadProperty':
                updatedOperation[verb].message.payload.properties[value.name] = value;
                break;
            case 'scopes': {
                const defValue = value[0];
                updatedOperation[verb]['x-scopes'] = [];
                for (let i = 0; i < defValue.length; i++) {
                    updatedOperation[verb]['x-scopes'].push(defValue[i]);
                }

                for (const selectedScope of defValue) {
                    if (selectedScope
                        && !securityDefScopes[selectedScope]
                        && securityDefScopes[selectedScope] !== '') {
                        let scopeDescription = '';
                        if (selectedScope in sharedScopesByName) {
                            if (sharedScopesByName[selectedScope].scope.description !== null) {
                                scopeDescription = sharedScopesByName[selectedScope].scope.description;
                            }
                            securityDefScopes[selectedScope] = scopeDescription;
                        }
                        setSecurityDefScopes(securityDefScopes);
                    }
                }
                break;
            }
            default:
                return currentOperations;
        }
        return {
            ...currentOperations,
            [target]: { ...currentOperations[target], [verb]: updatedOperation[verb] },
        };
    }
    const [operations, operationsDispatcher] = useReducer(operationsReducer, {});

    /**
     *
     *
     * @param {*} operation
     * @param {*} checked
     */
    function onOperationSelectM(operation, checked) {
        const { target, verb } = operation;
        setSelectedOperation((currentSelections) => {
            const nextSelectedOperations = cloneDeep(currentSelections);
            if (!nextSelectedOperations[target]) {
                nextSelectedOperations[target] = {};
            }
            if (checked) {
                nextSelectedOperations[target][verb] = checked;
            } else {
                delete nextSelectedOperations[target][verb];
            }
            if (isEmpty(nextSelectedOperations[target])) {
                delete nextSelectedOperations[target];
            }
            return nextSelectedOperations;
        });
    }
    const onMarkAsDelete = useCallback(onOperationSelectM, [setSelectedOperation]);

    /**
     *
     * @param {*} spec
     */
    function verifySecurityScheme(spec) {
        /* eslint-disable no-param-reassign */
        spec.components = spec.components || {};
        spec.components.securitySchemes = spec.components.securitySchemes || {};
        spec.components.securitySchemes.oauth2 = spec.components.securitySchemes.oauth2 || { type: 'oauth2' };
        spec.components.securitySchemes.oauth2.flows = spec.components.securitySchemes.oauth2.flows || {};
        spec.components.securitySchemes.oauth2.flows.implicit = spec.components.securitySchemes.oauth2.flows.implicit
            || {};
        spec.components.securitySchemes.oauth2.flows.implicit.scopes = spec.components.securitySchemes.oauth2.flows
            .implicit.scopes || {};
        /* eslint-enable no-param-reassign */
    }

    /**
     * This method sets the securityDefinitionScopes from the spec
     * @param {Object} spec The original swagger content.
     */
    function setSecurityDefScopesFromSpec(spec) {
        verifySecurityScheme(spec);
        setSecurityDefScopes(cloneDeep(spec.components.securitySchemes.oauth2.flows.implicit.scopes));
    }

    /**
     * This method sets the scopes of the spec from the securityDefinitionScopes
     */
    function setSpecScopesFromSecurityDefScopes() {
        verifySecurityScheme(asyncAPISpec);
        asyncAPISpec.components.securitySchemes.oauth2.flows.implicit.scopes = securityDefScopes;
    }

    /**
     *
     * @param {*} rawSpec The original swagger content.
     * @returns {null}
     */
    function resolveAndUpdateSpec(rawSpec) {
        const resolvedChannels = resolveSpec(rawSpec, rawSpec);
        const resolvedSpec = { ...rawSpec, channels: resolvedChannels.channels };
        operationsDispatcher({ action: 'init', data: resolvedSpec.channels });
        setAsyncAPISpec(resolvedSpec);
        setSecurityDefScopesFromSpec(rawSpec);
    }

    /**
     *
     * Update the asyncapi using /asyncapi PUT operation and then fetch the updated API Object doing a apis/{api-uuid}
     * GET
     * @param {JSON} spec Updated full AsyncAPI spec ready to PUT
     * @returns {Promise} Promise resolving to updated API object
     */
    function updateAsyncAPIDefinition(spec) {
        return api
            .updateAsyncAPIDefinition(spec)
            .then((response) => resolveAndUpdateSpec(response.body))
            .then(updateAPI)
            .catch((error) => {
                console.error(error);
                if (error.response) {
                    setPageError(error.response.body);
                } else {
                    Alert.error('Error while updating the definition');
                }
            });
    }

    /**
     *
     * This method modifies the security definition scopes by removing the scopes which are not present
     * in operations and which are shared scopes
     * @param {Array} apiOperations Operations list
     */
    function updateSecurityDefinition(apiOperations) {
        Object.keys(securityDefScopes).forEach((key) => {
            let isScopeExistsInOperation = false;
            for (const [, verbs] of Object.entries(apiOperations)) {
                for (const [, verbInfo] of Object.entries(verbs)) {
                    // Checking if the scope resides in the operation
                    for (const secDef of verbInfo.security || []) {
                        if (secDef
                            && secDef.default
                            && secDef.default.includes(key)) {
                            isScopeExistsInOperation = true;
                            break;
                        }
                    }

                    if (isScopeExistsInOperation) {
                        break;
                    }
                }
                if (isScopeExistsInOperation) {
                    break;
                }
            }
        });
        setSecurityDefScopes(securityDefScopes);
    }

    /**
     *
     * Save the OpenAPI changes using REST API, type parameter is required to
     * identify the locally created data structured, i:e type `operation` will assume that `data` contains the
     * object structure of locally created operation object which is a combination of REST API
     * response `operations` field and OpenAPI spec operation information
     * @param {String} type Type of data object
     * @param {Object} data Data object
     * @returns {Promise|null} A promise object which resolve to Swagger PUT response body.
     */
    function updateAsyncAPI() {
        const copyOfOperations = cloneDeep(operations);
        for (const [target, verbs] of Object.entries(markedOperations)) {
            for (const verb of Object.keys(verbs)) {
                delete copyOfOperations[target][verb];
                if (!copyOfOperations[target].publish && !copyOfOperations[target].subscribe) {
                    delete copyOfOperations[target];
                }
            }
        }

        updateSecurityDefinition(copyOfOperations);
        setSpecScopesFromSecurityDefScopes();
        if (websubSubscriptionConfiguration !== api.websubSubscriptionConfiguration) {
            return updateAPI({ websubSubscriptionConfiguration })
                .catch((error) => {
                    console.error(error);
                    Alert.error('Error while updating the API');
                })
                .then(() => updateAsyncAPIDefinition({ ...asyncAPISpec, channels: copyOfOperations }));
        } else {
            return updateAsyncAPIDefinition({ ...asyncAPISpec, channels: copyOfOperations });
        }
    }

    useEffect(() => {
        if (api.apitype !== 'APIProduct') {
            API.getAllScopes()
                .then((response) => {
                    if (response.body && response.body.list) {
                        const sharedScopesList = [];
                        const sharedScopesByNameList = {};
                        const shared = true;
                        for (const scope of response.body.list) {
                            const modifiedScope = {};
                            modifiedScope.scope = scope;
                            modifiedScope.shared = shared;
                            sharedScopesList.push(modifiedScope);
                            sharedScopesByNameList[scope.name] = modifiedScope;
                        }
                        setSharedScopes(sharedScopesList);
                        setSharedScopesByName(sharedScopesByNameList);
                    }
                });
        }
    }, []);

    useEffect(() => {
        // Update the Swagger spec object when API object gets changed
        api.getAsyncAPIDefinition()
            .then((response) => {
                resolveAndUpdateSpec(response.body);
            })
            .catch((error) => {
                if (error.response) {
                    Alert.error(error.response.body.description);
                    setPageError(error.response.body);
                }
                console.error(error);
            });
    }, [api.id]);

    // Note: Make sure not to use any hooks after/within this condition , because it returns conditionally
    // If you do so, You will probably get `Rendered more hooks than during the previous render.` exception
    // if ((!pageError && isEmpty(openAPISpec)) || (resolvedSpec.errors.length === 0 && isEmpty(resolvedSpec.spec))) {
    if ((!pageError && isEmpty(asyncAPISpec))) {
        return (
            <Grid container direction='row' justify='center' alignItems='center'>
                <Grid item>
                    <CircularProgress disableShrink />
                </Grid>
            </Grid>
        );
    }

    return (
        <Grid container direction='row' justify='flex-start' spacing={2} alignItems='stretch'>
            {pageError && (
                <Grid item md={12}>
                    <Banner onClose={() => setPageError(null)} disableActions type='error' message={pageError} />
                </Grid>
            )}
            {!isRestricted(['apim:api_create'], api) && !disableAddOperation && api.type === 'WEBSUB' && (
                <Grid item md={12} xs={12}>
                    <SubscriptionConfig
                        websubSubscriptionConfigDispatcher={websubSubscriptionConfigDispatcher}
                        websubSubscriptionConfiguration={websubSubscriptionConfiguration}
                    />
                </Grid>
            )}
            {!isRestricted(['apim:api_create'], api) && !disableAddOperation && (
                <Grid item md={12} xs={12}>
                    <AddOperation operationsDispatcher={operationsDispatcher} isAsyncAPI={isAsyncAPI} api={api} />
                </Grid>
            )}
            <Grid item md={12}>
                <Paper>
                    {
                        operations && Object.entries(operations).map(([target, operation]) => (
                            <Grid key={target} item md={12}>
                                <GroupOfOperations tag={target} operation={operation}>
                                    <Grid
                                        container
                                        direction='column'
                                        justify='flex-start'
                                        spacing={1}
                                        alignItems='stretch'
                                    >
                                        {operation.subscribe && (
                                            <Grid key={target + '_subscribe'} item md={12}>
                                                <AsyncOperation
                                                    target={target}
                                                    verb='subscribe'
                                                    highlight
                                                    operation={operation}
                                                    spec={asyncAPISpec}
                                                    api={api}
                                                    operationsDispatcher={operationsDispatcher}
                                                    sharedScopes={sharedScopes}
                                                    markAsDelete={Boolean(markedOperations[target]
                                                        && markedOperations[target].subscribe)}
                                                    onMarkAsDelete={onMarkAsDelete}
                                                />
                                            </Grid>
                                        )}
                                        {operation.publish && (
                                            <Grid key={target + '_publish'} item md={12}>
                                                <AsyncOperation
                                                    target={target}
                                                    verb='publish'
                                                    highlight
                                                    operation={operation}
                                                    spec={asyncAPISpec}
                                                    api={api}
                                                    operationsDispatcher={operationsDispatcher}
                                                    sharedScopes={sharedScopes}
                                                    markAsDelete={Boolean(markedOperations[target]
                                                        && markedOperations[target].publish)}
                                                    onMarkAsDelete={onMarkAsDelete}
                                                />
                                            </Grid>
                                        )}
                                    </Grid>
                                </GroupOfOperations>
                            </Grid>
                        ))
                    }
                </Paper>
                <Grid
                    style={{ marginTop: '25px' }}
                    container
                    direction='row'
                    justify='space-between'
                    alignItems='center'
                >
                    {!disableUpdate && (
                        <SaveOperations
                            operationsDispatcher={operationsDispatcher}
                            updateAsyncAPI={updateAsyncAPI}
                            api={api}
                        />
                    )}
                </Grid>
            </Grid>
        </Grid>
    );
}

Topics.defaultProps = {
    operationProps: { disableDelete: false },
    disableUpdate: false,
    disableAddOperation: false,
};

Topics.propTypes = {
    disableAddOperation: PropTypes.bool,
    disableUpdate: PropTypes.bool,
    operationProps: PropTypes.shape({
        disableDelete: PropTypes.bool,
    }),
};
