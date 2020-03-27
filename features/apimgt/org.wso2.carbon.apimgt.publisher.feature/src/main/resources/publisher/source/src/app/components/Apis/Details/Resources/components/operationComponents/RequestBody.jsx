/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

import React from 'react';
import PropTypes from 'prop-types';
import { FormattedMessage } from 'react-intl';
import TableCell from '@material-ui/core/TableCell';
import Tooltip from '@material-ui/core/Tooltip';
import IconButton from '@material-ui/core/IconButton';
import EditIcon from '@material-ui/icons/Edit';
import DeleteIcon from '@material-ui/icons/Delete';
import TableRow from '@material-ui/core/TableRow';

/**
 * Component to render the Request b
 *
 * */
function RequestBody(props) {
    const {
        contentType, content, operation, disableUpdate, hideParameterEdit, operationsDispatcher, target, verb,
    } = props;
    return (
        <TableRow key={contentType}>
            <TableCell align='left'>
                <FormattedMessage
                    id='Apis.Details.Resources.components.operationComponents.ListParameter.body'
                    defaultMessage='Body'
                />
            </TableCell>
            <TableCell>{contentType}</TableCell>
            <TableCell align='left'>{content.schema.type}</TableCell>
            <TableCell align='left'>
                {operation.requestBody.required
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
                    {hideParameterEdit && (
                        <Tooltip title='Edit'>
                            <IconButton onClick={() => {}} fontSize='small'>
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
                            onClick={() => {
                                operationsDispatcher({
                                    action: 'requestBody',
                                    data: {
                                        target,
                                        verb,
                                        value: {
                                            description: '',
                                            required: false,
                                            content: {},
                                        },
                                    },
                                });
                            }}
                            fontSize='small'
                        >
                            <DeleteIcon fontSize='small' />
                        </IconButton>
                    </Tooltip>
                </TableCell>
            )}
        </TableRow>
    );
}

RequestBody.propTypes = {
    contentType: PropTypes.string.isRequired,
    content: PropTypes.shape({}).isRequired,
    operation: PropTypes.shape({}).isRequired,
    disableUpdate: PropTypes.bool.isRequired,
    hideParameterEdit: PropTypes.bool.isRequired,
    operationsDispatcher: PropTypes.func.isRequired,
    target: PropTypes.string.isRequired,
    verb: PropTypes.string.isRequired,
};

export default RequestBody;
