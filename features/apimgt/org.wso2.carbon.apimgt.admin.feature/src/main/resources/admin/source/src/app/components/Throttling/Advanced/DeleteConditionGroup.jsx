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
import Box from '@material-ui/core/Box';
import DeleteIcon from '@material-ui/icons/Delete';
import FormDialogBase from 'AppComponents/AdminPages/Addons/FormDialogBase';

/**
 * Render delete dialog box.
 * @param {JSON} props component props.
 * @returns {JSX} Loading animation.
 */
function DeleteConditionGroup({ deleteThisGroup }) {
    const formSaveCallback = () => {
        return (setOpen) => {
            deleteThisGroup();
            setOpen(false);
        };
    };
    return (
        <Box textAlign='right'>
            <FormDialogBase
                triggerButtonProps={{
                    variant: 'outlined',
                    size: 'small',
                }}
                triggerButtonText={(
                    <>
                        <DeleteIcon />
                        <FormattedMessage
                            id='Throttling.Advanced.DeleteConditionGroup.title'
                            defaultMessage='Delete Condition Group'
                        />
                    </>
                )}
                title={(
                    <FormattedMessage
                        id='Throttling.Advanced.DeleteConditionGroup.title'
                        defaultMessage='Delete Condition Group'
                    />
                )}
                saveButtonText='Delete'
                // icon={<DeleteIcon />}
                formSaveCallback={formSaveCallback}
            >
                <DialogContentText>
                    <FormattedMessage
                        id='Throttling.Advanced.DeleteConditionGroup.question'
                        defaultMessage='Do you want to remove this condition group?'
                    />


                </DialogContentText>
            </FormDialogBase>
        </Box>

    );
}
DeleteConditionGroup.propTypes = {
    row: PropTypes.shape({}).isRequired,
    deleteThisGroup: PropTypes.func.isRequired,
    item: PropTypes.shape({}).isRequired,
};
export default DeleteConditionGroup;
