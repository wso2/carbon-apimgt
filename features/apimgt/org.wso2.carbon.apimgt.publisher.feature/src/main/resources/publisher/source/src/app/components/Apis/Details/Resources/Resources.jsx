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

import React, { useReducer, useEffect, useState, useCallback, useMemo } from 'react';
import Grid from '@material-ui/core/Grid';
import Paper from '@material-ui/core/Paper';
import { useAPI } from 'AppComponents/Apis/Details/components/ApiContext';
import cloneDeep from 'lodash.clonedeep';
import Swagger from 'swagger-client';
import isEmpty from 'lodash/isEmpty';
import Alert from 'AppComponents/Shared/Alert';
import Banner from 'AppComponents/Shared/Banner';
import API from 'AppData/api';
import CircularProgress from '@material-ui/core/CircularProgress';
import PropTypes from 'prop-types';
import { isRestricted } from 'AppData/AuthManager';
import Operation from './components/Operation';
import GroupOfOperations from './components/GroupOfOperations';
import SpecErrors from './components/SpecErrors';
import AddOperation from './components/AddOperation';
import GoToDefinitionLink from './components/GoToDefinitionLink';
import APIRateLimiting from './components/APIRateLimiting';
import { extractPathParameters, isSelectAll } from './operationUtils';
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
    const [specErrors, setSpecErrors] = useState([]);
    const [markedOperations, setSelectedOperation] = useState({});
    const [openAPISpec, setOpenAPISpec] = useState({});
    const [apiThrottlingPolicy, setApiThrottlingPolicy] = useState(api.apiThrottlingPolicy);

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
                if (!updatedOperation.parameters) {
                    updatedOperation.parameters = [value];
                } else {
                    updatedOperation.parameters.push(value);
                }
                break;
            case 'deleteParameter':
                updatedOperation.parameters = updatedOperation.parameters.filter((parameter) => {
                    return parameter.in !== value.in && parameter.name !== value.name;
                });
                break;
            case 'throttlingPolicy':
                updatedOperation['x-throttling-tier'] = value;
                break;
            case 'scopes':
                if (!updatedOperation.security) {
                    updatedOperation.security = [{ default: [] }];
                } else if (!updatedOperation.security.find(item => item.default)) {
                    updatedOperation.security.push({ default: [] });
                }
                updatedOperation.security.find(item => item.default).default = value;
                break;
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
    // memoized (https://reactjs.org/docs/hooks-reference.html#usememo) to improve pref,
    // localized to inject local apiThrottlingPolicy data
    const localApi = useMemo(() => ({ id: api.id, apiThrottlingPolicy, scopes: api.scopes }), [
        api,
        apiThrottlingPolicy,
    ]);
    /**
     *
     *
     * @param {*} response
     * @returns
     */
    function resolveAndUpdateSpec(rawSpec) {
        return Swagger.resolve({ spec: rawSpec }).then(({ spec, errors }) => {
            const value = spec;
            delete value.$$normalized;
            operationsDispatcher({ action: 'init', data: value.paths });
            setOpenAPISpec(value);
            setSpecErrors(errors);
        });
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
            .then(response => resolveAndUpdateSpec(response.body))
            .then(updateAPI)
            .catch((error) => {
                console.error(error);
                Alert.error('Error while updating the definition');
            });
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
                    const message = "Can't delete all the operations, Please keep at least one operation.";
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

        // Fetch API level throttling policies only when the page get mounted for the first time `componentDidMount`
        API.policies('api').then((response) => {
            setOperationRateLimits(response.body.list);
        });
        // TODO: need to handle the error cases through catch ~tmkb
    }, [api.id]);

    if (pageError) {
        return <Banner type='error' message={pageError} />;
    }

    // Note: Make sure not to use any hooks after/within this condition , because it returns conditionally
    // If you do so, You will probably get `Rendered more hooks than during the previous render.` exception
    if (isEmpty(openAPISpec)) {
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
                <Grid item md={12}>
                    <AddOperation operationsDispatcher={operationsDispatcher} />
                </Grid>
            )}
            {specErrors.length > 0 && <SpecErrors specErrors={specErrors} />}
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
                                        return (
                                            <Grid key={`${target}/${verb}`} item>
                                                <Operation
                                                    target={target}
                                                    verb={verb}
                                                    highlight
                                                    operationsDispatcher={operationsDispatcher}
                                                    spec={openAPISpec}
                                                    operation={operation}
                                                    operationRateLimits={operationRateLimits}
                                                    api={localApi}
                                                    markAsDelete={Boolean(markedOperations[target]
                                                        && markedOperations[target][verb])}
                                                    onMarkAsDelete={onMarkAsDelete}
                                                    disableUpdate={disableUpdate}
                                                    disableMultiSelect={disableMultiSelect}
                                                    {...operationProps}
                                                />
                                            </Grid>
                                        );
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
                        <SaveOperations operationsDispatcher={operationsDispatcher} updateOpenAPI={updateOpenAPI} />
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
