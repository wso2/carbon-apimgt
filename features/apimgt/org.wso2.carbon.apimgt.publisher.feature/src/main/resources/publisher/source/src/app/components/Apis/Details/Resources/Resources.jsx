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

import React, { useReducer, useEffect, useState } from 'react';
import Grid from '@material-ui/core/Grid';
import { useAPI } from 'AppComponents/Apis/Details/components/ApiContext';
import cloneDeep from 'lodash.clonedeep';
import Swagger from 'swagger-client';
import isEmpty from 'lodash/isEmpty';
import Alert from 'AppComponents/Shared/Alert';
import API from 'AppData/api';
import CircularProgress from '@material-ui/core/CircularProgress';
import PropTypes from 'prop-types';
import Operation from './components/Operation';
import GroupOfOperations from './components/GroupOfOperations';
import SpecErrors from './components/SpecErrors';
import AddOperation from './components/AddOperation';
import GoToDefinitionLink from './components/GoToDefinitionLink';
import APIRateLimiting from './components/APIRateLimiting';
import { getTaggedOperations } from './operationUtils';

/**
 * This component handles the Resource page in API details though it's written in a sharable way
 * that anyone could use this to render resources in anywhere else if needed.
 *
 * @export
 * @returns {React.Component} @inheritdoc
 */
export default function Resources(props) {
    const {
        disableAddOperation, operationProps, disableRateLimiting, hideAPIDefinitionLink,
    } = props;
    /**
     *
     * Reducer to handle actions related to OpenAPI specification
     * @param {Object} openAPISpec Contains the /apis/{apiId}/swagger response body
     * @param {Object} triggeredAction action triggered by
     * @returns {Object} Next state
     */
    function openAPIActionsReducer(openAPISpec, triggeredAction) {
        const { action, event } = triggeredAction;
        switch (action) {
            case 'initState':
                return event.value;
            default:
                break;
        }
        return openAPISpec;
    }
    const [api, updateAPI] = useAPI();
    const [operationRateLimits, setOperationRateLimits] = useState([]);
    const [specErrors, setSpecErrors] = useState([]);
    const [openAPI, openAPIActionsDispatcher] = useReducer(openAPIActionsReducer, {});

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
            openAPIActionsDispatcher({ action: 'initState', event: { value } });
            setSpecErrors(errors);
        });
    }

    /**
     *
     *
     * @param {*} data
     */
    function updateSwagger(targetOperation, spec) {
        return api
            .updateSwagger(spec)
            .then(response => resolveAndUpdateSpec(response.body))
            .then(() => updateAPI())
            .catch((error) => {
                console.error(error);
                Alert.error('Error while updating the operation with ' +
                        `path ${targetOperation.target} verb ${targetOperation.verb} `);
            });
    }

    /**
     *
     *
     * @param {*} data
     */
    function updateAPIOperations(targetOperation, apiOperation) {
        const updatedOperations = api.operations.map((operation) => {
            if (operation.target === targetOperation.target && operation.verb === targetOperation.verb) {
                return apiOperation;
            } else {
                return operation;
            }
        });
        updateAPI({ operations: updatedOperations });
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
                return updateSwagger(data, copyOfOpenAPI).then(() => updateAPIOperations(data, apiOperation));
            case 'add':
                if (copyOfOpenAPI.paths[data.target] && copyOfOpenAPI.paths[data.target][data.verb.toLowerCase()]) {
                    const message = 'Operation already exist !!';
                    Alert.error(message);
                    return Promise.reject(new Error(message));
                } else if (!copyOfOpenAPI.paths[data.target]) {
                    // If target is not there add an empty object
                    copyOfOpenAPI.paths[data.target] = {};
                }
                copyOfOpenAPI.paths[data.target][data.verb.toLowerCase()] = {
                    responses: { 200: { description: 'ok' } },
                };
                return updateSwagger(data, copyOfOpenAPI);
            case 'delete':
                delete copyOfOpenAPI.paths[data.target][data.verb.toLowerCase()];
                if (isEmpty(copyOfOpenAPI.paths[data.target])) {
                    delete copyOfOpenAPI.paths[data.target];
                }
                return updateSwagger(data, copyOfOpenAPI);
            default:
                break;
        }
        return Promise.reject(new Error());
    }

    useEffect(() => {
        // Update the Swagger spec object when API object gets changed
        api.getSwagger().then((response) => {
            resolveAndUpdateSpec(response.body);
        });

        // Fetch API level throttling policies only when the page get mounted for the first time `componentDidMount`
        API.policies('api').then((response) => {
            setOperationRateLimits(response.body.list);
        });
        // TODO: need to handle the error cases through catch ~tmkb
    }, [api]);

    if (isEmpty(openAPI)) {
        return <CircularProgress />;
    }
    const taggedOperations = getTaggedOperations(api, openAPI);

    return (
        <Grid container direction='row' justify='flex-start' spacing={2} alignItems='stretch'>
            {!disableRateLimiting && (
                <Grid item md={12}>
                    <APIRateLimiting operationRateLimits={operationRateLimits} api={api} updateAPI={updateAPI} />
                </Grid>
            )}
            {!disableAddOperation && (
                <Grid item md={12}>
                    <AddOperation updateOpenAPI={updateOpenAPI} />
                </Grid>
            )}
            {specErrors.length > 0 && <SpecErrors specErrors={specErrors} />}
            <Grid item md={12}>
                {Object.entries(taggedOperations).map(([tag, operations]) =>
                    !!operations.length && (
                        <Grid key={tag} item md={12}>
                            <GroupOfOperations updateOpenAPI={updateOpenAPI} openAPI={openAPI} tag={tag}>
                                <Grid
                                    container
                                    direction='column'
                                    justify='flex-start'
                                    spacing={1}
                                    alignItems='stretch'
                                >
                                    {operations.map(operation => (
                                        <Grid key={`${operation.target}/${operation.verb}`} item>
                                            <Operation
                                                highlight
                                                updateOpenAPI={updateOpenAPI}
                                                openAPI={openAPI}
                                                operation={operation}
                                                operationRateLimits={operationRateLimits}
                                                api={api}
                                                {...operationProps}
                                            />
                                        </Grid>
                                    ))}
                                </Grid>
                            </GroupOfOperations>
                        </Grid>
                    ))}
            </Grid>

            {!hideAPIDefinitionLink && <GoToDefinitionLink api={api} />}
        </Grid>
    );
}

Resources.defaultProps = {
    operationProps: { disableUpdate: false, disableDelete: false },
    disableRateLimiting: false,
    hideAPIDefinitionLink: false,
    disableAddOperation: false,
};

Resources.propTypes = {
    disableRateLimiting: PropTypes.bool,
    hideAPIDefinitionLink: PropTypes.bool,
    disableAddOperation: PropTypes.bool,
    operationProps: PropTypes.shape({
        disableUpdate: PropTypes.bool,
        disableDelete: PropTypes.bool,
    }),
};
