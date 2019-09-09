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

import React, { useReducer, useEffect } from 'react';
import Grid from '@material-ui/core/Grid';
import { useAPI } from 'AppComponents/Apis/Details/components/ApiContext';
import cloneDeep from 'lodash.clonedeep';
import isEmpty from 'lodash/isEmpty';
import CircularProgress from '@material-ui/core/CircularProgress';

import Operation from './components/Operation';
import GroupOfOperations from './components/GroupOfOperations';
import Alert from 'AppComponents/Shared/Alert';

/**
 * This component handles the Resource page in API details though it's written in a sharable way
 * that anyone could use this to render resources in anywhere else if needed.
 *
 * @export
 * @returns {React.Component} @inheritdoc
 */
export default function Resources() {
    /**
     *
     * Reducer to handle actions related to OpenAPI specification
     * @param {Object} currentState Contains the /apis/{apiId}/swagger response body
     * @param {Object} triggeredAction action triggered by
     * @returns {Object} Next state
     */
    function openAPIActionsReducer(currentState, triggeredAction) {
        const { action, event } = triggeredAction;
        switch (action) {
            case 'initState':
                return event.value;
            default:
                break;
        }
        return currentState;
    }
    const [api, updateAPI] = useAPI();
    const [openAPI, openAPIActionsDispatcher] = useReducer(openAPIActionsReducer, {});
    useEffect(() => {
        // Update the Swagger spec object when API object gets changed
        api.getSwagger().then(response =>
            openAPIActionsDispatcher({ action: 'initState', event: { value: response.body } }));
    }, [api]);

    // We don't give a * If openAPI object is null
    if (isEmpty(openAPI)) {
        return <CircularProgress />;
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
    function updateOpenAPI(type, data) {
        const copyOfOpenAPI = cloneDeep(openAPI);
        const { spec, ...apiOperation } = data;
        switch (type) {
            case 'operation':
                copyOfOpenAPI.paths[data.target][data.verb.toLowerCase()] = spec;
                return api
                    .updateSwagger(copyOfOpenAPI)
                    .then((response) => {
                        const { body: value } = response; // Rename response body as value (updated swagger)
                        openAPIActionsDispatcher({ action: 'initState', event: { value } });
                        return value;
                    })
                    .catch((error) => {
                        console.error(error);
                        Alert.error(`Error while updating the operation with path ${data.target} verb ${data.verb} `);
                    })
                    .then((openAPIResponse) => {
                        const updatedOperations = api.operations.map((operation) => {
                            if (operation.target === data.target && operation.verb === data.verb) {
                                return apiOperation;
                            } else {
                                return operation;
                            }
                        });
                        updateAPI({ operations: updatedOperations });
                    });
            default:
                break;
        }
        return null;
    }

    const taggedOperations = { Default: [] };
    api.operations.map((apiOperation) => {
        const { target, verb } = apiOperation;
        const openAPIOperation = openAPI.paths[target][verb.toLowerCase()];
        if (!openAPIOperation) {
            console.warn(`Could not find target = ${target} ` +
                    `verb (lower cased) = ${verb.toLowerCase()} operation in OpenAPI definition`);
        }
        const operationInfo = { spec: openAPIOperation, ...apiOperation };
        if (openAPIOperation.tags) {
            openAPIOperation.tags.map((tag) => {
                if (!taggedOperations[tag]) {
                    taggedOperations[tag] = [];
                }
                taggedOperations[tag].push(operationInfo);
                return operationInfo; // Just to satisfy an es-lint rule or could use `for ... of ...`
            });
        } else {
            taggedOperations.Default.push(operationInfo);
        }
        return operationInfo; // Just to satisfy an es-lint rule
    });
    // if (openAPI.paths) {
    //     for (const [path, verbs] of Object.entries(openAPI.paths)) {
    //         for (const [verb, operationInfo] of Object.entries(verbs)) {
    //             const operation = { path, verb, operationInfo };
    //             if (operationInfo.tags) {
    //                 operationInfo.tags.map((tag) => {
    //                     if (!taggedOperations[tag]) {
    //                         taggedOperations[tag] = [];
    //                     }
    //                     taggedOperations[tag].push(operation);
    //                     return operation; // Just to satisfy an es-lint rule
    //                 });
    //             } else {
    //                 taggedOperations.Default.push(operation);
    //             }
    //         }
    //     }
    // }

    return (
        <Grid container direction='column' justify='flex-start' spacing={2} alignItems='stretch'>
            {Object.entries(taggedOperations).map(([tag, operations]) =>
                !!operations.length && (
                    <Grid item>
                        <GroupOfOperations updateOpenAPI={updateOpenAPI} openAPI={openAPI} tag={tag}>
                            <Grid
                                container
                                direction='column'
                                justify='flex-start'
                                spacing={1}
                                alignItems='stretch'
                            >
                                {operations.map(operation => (
                                    <Grid item>
                                        <Operation
                                            updateOpenAPI={updateOpenAPI}
                                            openAPI={openAPI}
                                            operation={operation}
                                        />
                                    </Grid>
                                ))}
                            </Grid>
                        </GroupOfOperations>
                    </Grid>
                ))}
        </Grid>
    );
}
