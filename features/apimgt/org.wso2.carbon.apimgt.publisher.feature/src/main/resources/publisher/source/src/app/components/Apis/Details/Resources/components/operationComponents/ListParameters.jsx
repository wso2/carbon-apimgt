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

import React, { useState } from 'react';
import PropTypes from 'prop-types';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import { makeStyles } from '@material-ui/core/styles';
import EditIcon from '@material-ui/icons/Edit';
import DeleteIcon from '@material-ui/icons/Delete';
import IconButton from '@material-ui/core/IconButton';
import Tooltip from '@material-ui/core/Tooltip';
import { capitalizeFirstLetter } from 'AppData/stringFormatter';
import { FormattedMessage } from 'react-intl';
import { isRef } from 'AppComponents/Apis/Details/Resources/operationUtils';
import RequestBody from 'AppComponents/Apis/Details/Resources/components/operationComponents/RequestBody';
import Banner from 'AppComponents/Shared/Banner';
import EditParameter from './EditParameter';

const useStyles = makeStyles({
    root: {
        width: '100%',
        overflowX: 'auto',
    },
    table: {
        minWidth: 650,
    },
});

/**
 *
 * Renders the operation parameters section
 * @export
 * @param {*} props
 * @returns
 */
export default function ListParameters(props) {
    const {
        operation, operationsDispatcher, target, verb, disableUpdate, hideParameterEdit, specVersion, resolvedSpec,
    } = props;
    const classes = useStyles();
    const [editingParameter, setEditingParameter] = useState(null);
    if (!resolvedSpec) {
        return (
            <Banner
                disableActions
                dense
                type='error'
                message={(
                    <FormattedMessage
                        id='Apis.Details.Resources.components.operationComponents.ListParameter.parameter.spec.error'
                        defaultMessage='Error in resolving the definition!'
                    />
                )}
            />
        );
    }
    return (
        <>
            {editingParameter !== null && (
                <EditParameter
                    operationsDispatcher={operationsDispatcher}
                    target={target}
                    verb={verb}
                    editingParameter={editingParameter}
                    setEditingParameter={setEditingParameter}
                    version={specVersion}
                />
            )}
            <Table className={classes.table} aria-label='parameters list'>
                <TableHead>
                    <TableRow>
                        <TableCell align='left'>
                            <FormattedMessage
                                id='Apis.Details.Resources.components.operationComponents.ListParameter.parameter.type'
                                defaultMessage='Parameter Type'
                            />
                        </TableCell>
                        <TableCell>
                            <FormattedMessage
                                id='Apis.Details.Resources.components.operationComponents.ListParameter.parameter.name'
                                defaultMessage='Name/Content Type'
                            />
                        </TableCell>
                        <TableCell align='left'>
                            <FormattedMessage
                                id='Apis.Details.Resources.components.operationComponents.ListParameter.data.type'
                                defaultMessage='Data Type'
                            />
                        </TableCell>
                        <TableCell align='left'>
                            <FormattedMessage
                                id='Apis.Details.Resources.components.operationComponents.ListParameter.required'
                                defaultMessage='Required'
                            />
                        </TableCell>
                        {!disableUpdate && (
                            <TableCell align='left'>
                                <FormattedMessage
                                    id='Apis.Details.Resources.components.operationComponents.ListParameter.actions'
                                    defaultMessage='Actions'
                                />
                            </TableCell>
                        )}
                    </TableRow>
                </TableHead>
                <TableBody>
                    {operation.parameters
                        && operation.parameters.map((parameter, index) => {
                            const isRefParam = isRef(parameter);
                            const paramCopy = isRefParam
                                ? resolvedSpec.paths[target][verb].parameters[index] : parameter;
                            return (
                                <TableRow key={paramCopy.name}>
                                    <TableCell align='left'>{capitalizeFirstLetter(paramCopy.in)}</TableCell>
                                    <TableCell align='left'>{paramCopy.name}</TableCell>
                                    <TableCell align='left'>
                                        {capitalizeFirstLetter(paramCopy.schema
                                            ? paramCopy.schema.type : paramCopy.type)}
                                    </TableCell>
                                    <TableCell align='left'>
                                        {paramCopy.required
                                            ? (
                                                <FormattedMessage
                                                    id={'Apis.Details.Resources.components.operationComponents'
                                                        + '.ListParameter.yes'}
                                                    defaultMessage='Yes'
                                                />
                                            )
                                            : (
                                                <FormattedMessage
                                                    id={'Apis.Details.Resources.components.operationComponents'
                                                        + '.ListParameter.no'}
                                                    defaultMessage='No'
                                                />
                                            )}
                                    </TableCell>
                                    {!disableUpdate && (
                                        <TableCell align='left'>
                                            {!isRefParam && (
                                                <Tooltip title={(
                                                    <FormattedMessage
                                                        id={'Apis.Details.Resources.components.operationComponents.'
                                                            + 'ListParameter.edit'}
                                                        defaultMessage='Edit'
                                                    />
                                                )}
                                                >
                                                    <IconButton
                                                        onClick={() => setEditingParameter(parameter)}
                                                        fontSize='small'
                                                    >
                                                        <EditIcon fontSize='small' />
                                                    </IconButton>
                                                </Tooltip>
                                            )}
                                            <Tooltip title={(
                                                <FormattedMessage
                                                    id={'Apis.Details.Resources.components.operationComponents'
                                                        + '.ListParameter.delete'}
                                                    defaultMessage='Delete'
                                                />
                                            )}
                                            >
                                                <IconButton
                                                    disabled={disableUpdate}
                                                    onClick={() => operationsDispatcher({
                                                        action: 'deleteParameter',
                                                        data: { target, verb, value: paramCopy },
                                                    })}
                                                    fontSize='small'
                                                >
                                                    <DeleteIcon fontSize='small' />
                                                </IconButton>
                                            </Tooltip>
                                        </TableCell>
                                    )}
                                </TableRow>
                            );
                        })}
                    {operation.requestBody && (isRef(operation.requestBody)
                        ? Object.entries(resolvedSpec.paths[target][verb].requestBody.content).map(
                            ([contentType, content]) => {
                                return (
                                    <RequestBody
                                        contentType={contentType}
                                        content={content}
                                        operation={operation}
                                        disableUpdate={disableUpdate}
                                        hideParameterEdit={hideParameterEdit}
                                        operationsDispatcher={operationsDispatcher}
                                        target={target}
                                        verb={verb}
                                    />
                                );
                            },
                        ) : Object.entries(operation.requestBody.content).map(([contentType, content]) => {
                            return (
                                <RequestBody
                                    contentType={contentType}
                                    content={content}
                                    operation={operation}
                                    disableUpdate={disableUpdate}
                                    hideParameterEdit={hideParameterEdit}
                                    operationsDispatcher={operationsDispatcher}
                                    target={target}
                                    verb={verb}
                                />
                            );
                        }))}
                </TableBody>
            </Table>
        </>
    );
}

ListParameters.defaultProps = {
    hideParameterEdit: false,
    disableUpdate: false,
};
ListParameters.propTypes = {
    operation: PropTypes.shape({}).isRequired,
    spec: PropTypes.shape({}).isRequired,
    hideParameterEdit: PropTypes.bool,
    operationsDispatcher: PropTypes.func.isRequired,
    target: PropTypes.string.isRequired,
    verb: PropTypes.string.isRequired,
    disableUpdate: PropTypes.bool,
    specVersion: PropTypes.string.isRequired,
    resolvedSpec: PropTypes.shape({}).isRequired,
};
