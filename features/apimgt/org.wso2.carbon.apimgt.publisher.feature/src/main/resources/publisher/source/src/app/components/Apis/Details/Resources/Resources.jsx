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

import React, {
    useReducer, useEffect, useState, useCallback, useMemo,
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
import SwaggerParser from '@apidevtools/swagger-parser';
import { isRestricted } from 'AppData/AuthManager';
import CONSTS from 'AppData/Constants';
import Operation from './components/Operation';
import GroupOfOperations from './components/GroupOfOperations';
import SpecErrors from './components/SpecErrors';
import AddOperation from './components/AddOperation';
import GoToDefinitionLink from './components/GoToDefinitionLink';
import APIRateLimiting from './components/APIRateLimiting';
import {
    extractPathParameters, isSelectAll, mapAPIOperations, getVersion, VERSIONS,
} from './operationUtils';
import OperationsSelector from './components/OperationsSelector';
import SaveOperations from './components/SaveOperations';

/**
 * This component handles the Resource page in API details though it's written in a sharable way
 * that anyone could use this to render resources in anywhere else if needed.
 *
 * @export
 * @returns {React.Component} @inheritdoc
 */
export default function Resources(props) {
    const {
        operationProps,
        disableRateLimiting,
        hideAPIDefinitionLink,
        disableMultiSelect,
        disableUpdate,
        disableAddOperation,
    } = props;

    const [api, updateAPI] = useAPI();
    const [pageError, setPageError] = useState(false);
    const [operationRateLimits, setOperationRateLimits] = useState([]);
    const [markedOperations, setSelectedOperation] = useState({});
    const [sharedScopes, setSharedScopes] = useState();
    const [sharedScopesByName, setSharedScopesByName] = useState();
    const [openAPISpec, setOpenAPISpec] = useState({});
    const [securityDefScopes, setSecurityDefScopes] = useState({});
    const [apiThrottlingPolicy, setApiThrottlingPolicy] = useState(api.apiThrottlingPolicy);
    const [arns, setArns] = useState([]);
    const [resolvedSpec, setResolvedSpec] = useState({ spec: {}, errors: [] });

    /**
     *
     *
     * @param {*} currentPolicies
     * @param {*} policyAction
     * @returns
     */
    function resourcePoliciesReducer(currentPolicies, policyAction) {
        const { action, data } = policyAction;
        const { value } = data || {}; // target, verb,
        let nextResourcePolicies = { ...currentPolicies };
        switch (action) {
            case 'init':
                nextResourcePolicies = value;
                break;
            case 'update':
                nextResourcePolicies[value.resourcePath][value.httpVerb][data.direction] = value;
                break;
            default:
                break;
        }
        return nextResourcePolicies;
    }
    const [resourcePolicies, resourcePoliciesDispatcher] = useReducer(resourcePoliciesReducer, null);

    /**
     *
     *
     * @param {*} currenPaths
     * @param {*} action
     */
    function operationsReducer(currentOperations, operationAction) {
        const { action, data } = operationAction;
        const { target, verb, value } = data || {};
        let updatedOperation;
        let addedOperations;
        if (target && verb) {
            updatedOperation = cloneDeep(currentOperations[target][verb]);
        } else {
            addedOperations = cloneDeep(currentOperations);
        }

        switch (action) {
            case 'init':
                setSelectedOperation({});
                return data || openAPISpec.paths;
            case 'description':
            case 'summary':
                updatedOperation[action] = value;
                break;
            case 'authType':
                updatedOperation['x-auth-type'] = value ? 'Any' : 'None';
                break;
            case 'parameter':
                if (updatedOperation.parameters) {
                    // Get the index to check whether the same parameter exists.
                    const index = updatedOperation.parameters.findIndex(
                        (e) => e.in === value.in && e.name === value.name,
                    );
                    if (index === -1) { // Parameter with name and in does not exists.
                        if (value.in === 'body') {
                            // Get the index of existing body param.
                            // This replaces if a new body parameter is added when another one exists.
                            const bodyIndex = updatedOperation.parameters.findIndex((parameter) => {
                                return parameter.in === 'body';
                            });
                            if (bodyIndex !== -1) {
                                updatedOperation.parameters[bodyIndex] = value;
                            } else {
                                updatedOperation.parameters.push(value);
                            }
                        } else {
                            updatedOperation.parameters.push(value);
                        }
                    } else {
                        updatedOperation.parameters[index] = value;
                    }
                } else {
                    updatedOperation.parameters = [value];
                }
                break;
            case 'requestBody':
                updatedOperation[action] = value;
                break;
            case 'deleteParameter':
                updatedOperation.parameters = updatedOperation.parameters.filter((parameter) => {
                    return !(parameter.in === value.in && parameter.name === value.name);
                });
                break;
            case 'throttlingPolicy':
                updatedOperation['x-throttling-tier'] = value;
                break;
            case 'amznResourceName':
                updatedOperation['x-amzn-resource-name'] = value;
                break;
            case 'amznResourceTimeout':
                updatedOperation['x-amzn-resource-timeout'] = value;
                break;
            case 'scopes': {
                if (!updatedOperation.security) {
                    updatedOperation.security = [{ default: [] }];
                } else if (!updatedOperation.security.find((item) => item.default)) {
                    updatedOperation.security.push({ default: [] });
                }
                const defValue = value[0];
                updatedOperation.security.find((item) => item.default).default = defValue;
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
            case 'add': {
                const parameters = extractPathParameters(data.target, openAPISpec);
                if (!addedOperations[data.target]) {
                    // If target is not there add an empty object
                    addedOperations[data.target] = {};
                }
                let alreadyExistCount = 0;
                for (const currentVerb of data.verbs) {
                    if (addedOperations[data.target][currentVerb]) {
                        const message = `Operation already exist with ${data.target} and ${currentVerb}`;
                        Alert.warning(message);
                        console.warn(message);
                        alreadyExistCount++;
                    } else {
                        // use else condition because continue is not allowed by es-lint rules
                        addedOperations[data.target][currentVerb] = {
                            'x-wso2-new': true, // This is to identify unsaved newly added operations, Remove when PUT
                            'x-auth-type': 'Application & Application User', // By default security is enabled
                            responses: { 200: { description: 'ok' } },
                            parameters,
                        };
                    }
                }
                if (alreadyExistCount === data.verbs.length) {
                    Alert.error('Operation(s) already exist!');
                    return currentOperations;
                }
                return addedOperations;
            }
            default:
                return currentOperations;
        }
        return { ...currentOperations, [target]: { ...currentOperations[target], [verb]: updatedOperation } };
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

    // can't depends on API id because we need to consider the changes in operations in api object
    // memoized (https://reactjs.org/docs/hooks-reference.html#usememo) to improve pref,
    // localized to inject local apiThrottlingPolicy data
    const localAPI = useMemo(
        () => ({
            id: api.id,
            apiThrottlingPolicy,
            scopes: api.scopes,
            operations: api.isAPIProduct() ? {} : mapAPIOperations(api.operations),
            endpointConfig: api.endpointConfig,
        }),
        [api, apiThrottlingPolicy],
    );

    /**
     * This method sets the securityDefinitionScopes from the spec
     * @param {Object} spec The original swagger content.
     */
    function setSecurityDefScopesFromSpec(spec) {
        const openAPIVersion = getVersion(spec);
        if (VERSIONS.V3.includes(openAPIVersion)) {
            if (spec.components && spec.components.securitySchemes && spec.components.securitySchemes.default) {
                const { flows } = spec.components.securitySchemes.default;
                if (flows.implicit.scopes) {
                    setSecurityDefScopes(cloneDeep(flows.implicit.scopes));
                }
            }
        } else if (VERSIONS.V2.includes(openAPIVersion)) {
            if (spec.securityDefinitions && spec.securityDefinitions.default) {
                if (spec.securityDefinitions.default.scopes) {
                    setSecurityDefScopes(cloneDeep(spec.securityDefinitions.default.scopes));
                }
            }
        }
    }

    /**
     * This method sets the scopes of the spec from the securityDefinitionScopes
     */
    function setSpecScopesFromSecurityDefScopes() {
        const openAPIVersion = getVersion(openAPISpec);
        if (VERSIONS.V3.includes(openAPIVersion)) {
            if (openAPISpec.components
                && openAPISpec.components.securitySchemes
                && openAPISpec.components.securitySchemes.default) {
                openAPISpec.components.securitySchemes.default.flows.implicit.scopes = securityDefScopes;
            }
        } else if (VERSIONS.V2.includes(openAPIVersion)) {
            if (openAPISpec.securityDefinitions && openAPISpec.securityDefinitions.default) {
                openAPISpec.securityDefinitions.default.scopes = securityDefScopes;
            }
        }
    }

    /**
     *
     * @param {*} rawSpec The original swagger content.
     * @returns {null}
     */
    function resolveAndUpdateSpec(rawSpec) {
        /*
         * Deep copying the spec.
         * Otherwise it will resolved to the original parameter passed (rawSpec) to the validate method.
         * We will not alter the provided spec.
         */
        const specCopy = cloneDeep(rawSpec);
        /*
        * Used SwaggerParser.validate() because we can get the errors as well.
        */
        SwaggerParser.validate(specCopy, (err, result) => {
            setResolvedSpec(() => {
                const errors = err ? [err] : [];
                return {
                    spec: result,
                    errors,
                };
            });
        });
        operationsDispatcher({ action: 'init', data: rawSpec.paths });
        setOpenAPISpec(rawSpec);
        setSecurityDefScopesFromSpec(rawSpec);
    }

    /**
     *
     * Update the swagger using /swagger PUT operation and then fetch the updated API Object doing a apis/{api-uuid} GET
     * @param {JSON} spec Updated full OpenAPI spec ready to PUT
     * @returns {Promise} Promise resolving to updated API object
     */
    function updateSwagger(spec) {
        return api
            .updateSwagger(spec)
            .then((response) => resolveAndUpdateSpec(response.body))
            .then(updateAPI)
            .catch((error) => {
                console.error(error);
                if (error.response) {
                    setPageError(error.response.body);
                    Alert.error('Error while updating the API');
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
            // Checking if the scope exists in operation and is a shared scope
            if (!isScopeExistsInOperation && (key in sharedScopesByName)) {
                delete securityDefScopes[key];
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
    function updateOpenAPI(type) {
        const copyOfOperations = cloneDeep(operations);
        switch (type) {
            case 'save':
                if (isSelectAll(markedOperations, copyOfOperations)) {
                    const message = 'At least one operation is required for the API';
                    Alert.warning(message);
                    return Promise.reject(new Error(message));
                }
                for (const [target, verbs] of Object.entries(markedOperations)) {
                    for (const verb of Object.keys(verbs)) {
                        delete copyOfOperations[target][verb];
                        if (isEmpty(copyOfOperations[target])) {
                            delete copyOfOperations[target];
                        }
                    }
                }
                // TODO: use better alternative (optimize performance) to identify newly added operations ~tmkb
                for (const [, verbs] of Object.entries(copyOfOperations)) {
                    for (const [, verbInfo] of Object.entries(verbs)) {
                        if (verbInfo['x-wso2-new']) {
                            delete verbInfo['x-wso2-new'];
                        }
                    }
                }
                break;
            default:
                return Promise.reject(new Error('Unsupported resource operation!'));
        }
        updateSecurityDefinition(copyOfOperations);
        setSpecScopesFromSecurityDefScopes();
        if (apiThrottlingPolicy !== api.apiThrottlingPolicy) {
            return updateAPI({ apiThrottlingPolicy })
                .catch((error) => {
                    console.error(error);
                    Alert.error('Error while updating the API');
                })
                .then(() => updateSwagger({ ...openAPISpec, paths: copyOfOperations }));
        } else {
            return updateSwagger({ ...openAPISpec, paths: copyOfOperations });
        }
    }

    useEffect(() => {
        API.getAmznResourceNames(api.id)
            .then((response) => {
                if (response.body && response.body.list) {
                    setArns(response.body.list);
                }
            });
    }, []);

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
        api.getSwagger()
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
        if (api.isSOAPToREST()) {
            const promisedInPolicies = api.getResourcePolicies('in');
            const promisedOutPolicies = api.getResourcePolicies('out');
            Promise.all([promisedInPolicies, promisedOutPolicies])
                .then(([inPolicies, outPolicies]) => {
                    const mappedPolicies = {};
                    for (const policy of inPolicies.body.list) {
                        const { resourcePath, httpVerb } = policy;
                        if (!mappedPolicies[resourcePath]) {
                            mappedPolicies[resourcePath] = {
                                [httpVerb]: { in: policy },
                            };
                        } else {
                            mappedPolicies[resourcePath][httpVerb] = { in: policy };
                        }
                    }
                    for (const policy of outPolicies.body.list) {
                        const { resourcePath, httpVerb } = policy;
                        if (!mappedPolicies[resourcePath]) {
                            mappedPolicies[resourcePath] = {
                                [httpVerb]: { out: policy },
                            };
                        } else {
                            mappedPolicies[resourcePath][httpVerb].out = policy;
                        }
                    }
                    resourcePoliciesDispatcher({ action: 'init', data: { value: mappedPolicies } });
                })
                .catch((error) => {
                    if (error.response) {
                        Alert.error(error.response.body.description);
                        setPageError(error.response.body);
                    }
                    setPageError(error.message);
                    console.error(error);
                });
        }
        // Fetch API level throttling policies only when the page get mounted for the first time `componentDidMount`
        API.policies('api').then((response) => {
            setOperationRateLimits(response.body.list);
        });
        // TODO: need to handle the error cases through catch ~tmkb
    }, [api.id]);

    // Note: Make sure not to use any hooks after/within this condition , because it returns conditionally
    // If you do so, You will probably get `Rendered more hooks than during the previous render.` exception
    if ((!pageError && isEmpty(openAPISpec)) || (resolvedSpec.errors.length === 0 && isEmpty(resolvedSpec.spec))) {
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
            {!disableRateLimiting && (
                <Grid item md={12}>
                    <APIRateLimiting
                        operationRateLimits={operationRateLimits}
                        value={apiThrottlingPolicy}
                        onChange={setApiThrottlingPolicy}
                        isAPIProduct={api.isAPIProduct()}
                    />
                </Grid>
            )}
            {!isRestricted(['apim:api_create'], api) && !disableAddOperation && (
                <Grid item md={12} xs={12}>
                    <AddOperation operationsDispatcher={operationsDispatcher} />
                </Grid>
            )}
            {resolvedSpec.errors.length > 0 && <SpecErrors specErrors={resolvedSpec.errors} />}
            <Grid item md={12}>
                <Paper>
                    {!disableMultiSelect && (
                        <OperationsSelector
                            operations={operations}
                            selectedOperations={markedOperations}
                            setSelectedOperation={setSelectedOperation}
                        />
                    )}
                    {Object.entries(operations).map(([target, verbObject]) => (
                        <Grid key={target} item md={12}>
                            <GroupOfOperations openAPI={openAPISpec} tag={target}>
                                <Grid
                                    container
                                    direction='column'
                                    justify='flex-start'
                                    spacing={1}
                                    alignItems='stretch'
                                >
                                    {Object.entries(verbObject).map(([verb, operation]) => {
                                        return CONSTS.HTTP_METHODS.includes(verb) ? (
                                            <Grid key={`${target}/${verb}`} item>
                                                <Operation
                                                    target={target}
                                                    verb={verb}
                                                    highlight
                                                    resourcePoliciesDispatcher={resourcePoliciesDispatcher}
                                                    resourcePolicy={
                                                        resourcePolicies
                                                        && resourcePolicies[target.slice(1)]
                                                        && resourcePolicies[target.slice(1)][verb]
                                                    }
                                                    operationsDispatcher={operationsDispatcher}
                                                    spec={openAPISpec}
                                                    operation={operation}
                                                    operationRateLimits={operationRateLimits}
                                                    api={localAPI}
                                                    markAsDelete={Boolean(markedOperations[target]
                                                        && markedOperations[target][verb])}
                                                    onMarkAsDelete={onMarkAsDelete}
                                                    disableUpdate={
                                                        disableUpdate || isRestricted(['apim:api_create'], api)
                                                    }
                                                    disableMultiSelect={disableMultiSelect}
                                                    arns={arns}
                                                    {...operationProps}
                                                    resolvedSpec={resolvedSpec.spec}
                                                    sharedScopes={sharedScopes}
                                                />
                                            </Grid>
                                        ) : null;
                                    })}
                                </Grid>
                            </GroupOfOperations>
                        </Grid>
                    ))}
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
                            updateOpenAPI={updateOpenAPI}
                            api={api}
                        />
                    )}
                    {!hideAPIDefinitionLink && <GoToDefinitionLink api={api} />}
                </Grid>
            </Grid>
        </Grid>
    );
}

Resources.defaultProps = {
    operationProps: { disableDelete: false },
    disableUpdate: false,
    disableRateLimiting: false,
    disableMultiSelect: false,
    hideAPIDefinitionLink: false,
    disableAddOperation: false,
};

Resources.propTypes = {
    disableRateLimiting: PropTypes.bool,
    hideAPIDefinitionLink: PropTypes.bool,
    disableMultiSelect: PropTypes.bool,
    disableAddOperation: PropTypes.bool,
    disableUpdate: PropTypes.bool,
    operationProps: PropTypes.shape({
        disableDelete: PropTypes.bool,
    }),
};
