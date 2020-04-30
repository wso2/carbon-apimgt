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
import DialogContentText from '@material-ui/core/DialogContentText';
import DeleteForeverIcon from '@material-ui/icons/DeleteForever';
import FormDialogBase from 'AppComponents/AdminPages/Addons/FormDialogBase';

/**
 * Mock API call
 * @param {number} id uuid of the item to delete.
 * @returns {Promise}.
 */
function apiCall(id) {
    return new Promise(((resolve) => {
        setTimeout(() => { resolve('Successfully deleted' + id); }, 2000);
    }));
}

/**
 * Render delete dialog box.
 * @param {JSON} props component props.
 * @returns {JSX} Loading animation.
 */
function Delete({ updateList, dataRow }) {
    const { id } = dataRow;

    const formSaveCallback = () => {
        // Do the API call
        const promiseAPICall = apiCall(id);

        promiseAPICall.then((data) => {
            updateList();
            return (data);
        })
            .catch((e) => {
                return (e);
            });
        return (promiseAPICall);
    };

    return (
        <FormDialogBase
            title='Delete Gateway Label?'
            saveButtonText='Delete'
            icon={<DeleteForeverIcon />}
            formSaveCallback={formSaveCallback}
        >
            <DialogContentText>
                This gateway label will be deleted.
                And some more info about what is going
                to happen to its hosts etc...
            </DialogContentText>
        </FormDialogBase>
    );
}
Delete.propTypes = {
    updateList: PropTypes.number.isRequired,
    dataRow: PropTypes.shape({
        id: PropTypes.number.isRequired,
    }).isRequired,
};
export default Delete;
