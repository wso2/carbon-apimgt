/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React from 'react';
import PropTypes from 'prop-types';
import { FormattedMessage } from 'react-intl';
import DialogContentText from '@material-ui/core/DialogContentText';
import DeleteIcon from '@material-ui/icons/Delete';
import FormDialogBase from 'AppComponents/AdminPages/Addons/FormDialogBase';
import CON_CONSTS from 'AppComponents/Throttling/Advanced/CON_CONSTS';

/**
 * Render delete dialog box.
 * @param {JSON} props component props.
 * @returns {JSX} Loading animation.
 */
function DeleteCondition({ row, item, callBack }) {
    const formSaveCallback = () => {
        return (setOpen) => {
            callBack(item);
            setOpen(false);
        };
    };
    return (
        <FormDialogBase
            title={row.name}
            saveButtonText='Delete'
            icon={<DeleteIcon />}
            formSaveCallback={formSaveCallback}
        >
            <DialogContentText>

                {item.type === CON_CONSTS.IPCONDITION && (item.ipConditionType === CON_CONSTS.IPCONDITION_IPRANGE) && (
                    <FormattedMessage
                        id='Throttling.Advanced.Delete.ip.iprange'
                        defaultMessage='IP Range'
                    />
                ) }
                {item.type === CON_CONSTS.IPCONDITION
                && (item.ipConditionType
                    === CON_CONSTS.IPCONDITION_IPSPECIFIC)
                    && (
                        <FormattedMessage
                            id='Throttling.Advanced.Delete.ip.specific'
                            defaultMessage='Specific IP'
                        />
                    ) }
                {item.type === CON_CONSTS.HEADERCONDITION
                                                        && item.headerName }
                {item.type === CON_CONSTS.QUERYPARAMETERCONDITION
                                                        && item.parameterName }
                {item.type === CON_CONSTS.JWTCLAIMSCONDITION
                                                        && item.claimUrl }
                <FormattedMessage
                    id='Throttling.Advanced.Delete.will.be.deleted'
                    defaultMessage=' will be deleted.'
                />


            </DialogContentText>
        </FormDialogBase>
    );
}
DeleteCondition.propTypes = {
    row: PropTypes.shape({}).isRequired,
    callBack: PropTypes.func.isRequired,
    item: PropTypes.shape({}).isRequired,
};
export default DeleteCondition;
