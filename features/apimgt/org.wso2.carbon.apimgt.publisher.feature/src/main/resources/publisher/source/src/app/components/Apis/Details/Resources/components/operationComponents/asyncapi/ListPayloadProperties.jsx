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
import { FormattedMessage } from 'react-intl';
import EditPayloadProperty from './EditPayloadProperty';

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
export default function ListPayloadProperties(props) {
    const {
        operation, operationsDispatcher, target, verb, disableUpdate,
    } = props;
    const classes = useStyles();
    const [editingProperty, setEditingProperty] = useState(null);
    const properties = (operation && operation[verb] && operation[verb].message && operation[verb].message.payload
            && operation[verb].message.payload.properties) ? operation[verb].message.payload.properties : { };

    return (
        <>
            {editingProperty !== null && (
                <EditPayloadProperty
                    operationsDispatcher={operationsDispatcher}
                    target={target}
                    verb={verb}
                    editingProperty={editingProperty}
                    setEditingProperty={setEditingProperty}
                />
            )}
            <Table className={classes.table} aria-label='parameters list'>
                <TableHead>
                    <TableRow>
                        <TableCell>
                            <FormattedMessage
                                id='Apis.Details.Topics.components.operationComponents.ListPayloadProps.name'
                                defaultMessage='Name'
                            />
                        </TableCell>
                        <TableCell align='left'>
                            <FormattedMessage
                                id='Apis.Details.Topics.components.operationComponents.ListPayloadProps.data.type'
                                defaultMessage='Data Type'
                            />
                        </TableCell>
                        <TableCell align='left'>
                            <FormattedMessage
                                id='Apis.Details.Topics.components.operationComponents.ListPayloadProps.description'
                                defaultMessage='Description'
                            />
                        </TableCell>
                        <TableCell align='left'>
                            <FormattedMessage
                                id='Apis.Details.Topics.components.operationComponents.ListPayloadProps.actions'
                                defaultMessage='Actions'
                            />
                        </TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {
                        properties && Object.entries(properties).map(([k, v]) => {
                            return (
                                <TableRow key={k}>
                                    <TableCell align='left'>{k}</TableCell>
                                    <TableCell align='left'>{v.type}</TableCell>
                                    <TableCell align='left'>{v.description}</TableCell>
                                    <TableCell align='left'>
                                        <Tooltip title={(
                                            <FormattedMessage
                                                id={'Apis.Details.Resources.components.operationComponents.'
                                            + 'ListPayloadProperties.edit'}
                                                defaultMessage='Edit'
                                            />
                                        )}
                                        >
                                            <IconButton
                                                onClick={() => setEditingProperty({ name: k, ...v })}
                                                fontSize='small'
                                            >
                                                <EditIcon fontSize='small' />
                                            </IconButton>
                                        </Tooltip>
                                        <Tooltip title={(
                                            <FormattedMessage
                                                id={'Apis.Details.Resources.components.operationComponents'
                                            + '.ListPayloadProps.delete'}
                                                defaultMessage='Delete'
                                            />
                                        )}
                                        >
                                            <IconButton
                                                disabled={disableUpdate}
                                                onClick={() => operationsDispatcher({
                                                    action: 'deletePayloadProperty',
                                                    data: { target, verb, value: k },
                                                })}
                                                fontSize='small'
                                            >
                                                <DeleteIcon fontSize='small' />
                                            </IconButton>
                                        </Tooltip>
                                    </TableCell>
                                </TableRow>
                            );
                        })
                    }
                </TableBody>
            </Table>
        </>
    );
}

ListPayloadProperties.defaultProps = {
    disableUpdate: false,
};
ListPayloadProperties.propTypes = {
    operation: PropTypes.shape({}).isRequired,
    spec: PropTypes.shape({}).isRequired,
    operationsDispatcher: PropTypes.func.isRequired,
    target: PropTypes.string.isRequired,
    verb: PropTypes.string.isRequired,
    disableUpdate: PropTypes.bool,
    resolvedSpec: PropTypes.shape({}).isRequired,
};
