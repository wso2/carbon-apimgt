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

import React from 'react';
import {
    Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle,
} from '@material-ui/core/';
import Slide from '@material-ui/core/Slide';
import { FormattedMessage } from 'react-intl';
import PropTypes from 'prop-types';

const DeleteConfirmation = (props) => {
    const { handleAppDelete, isDeleteOpen, toggleDeleteConfirmation } = props;
    return (
        <Dialog open={isDeleteOpen} transition={Slide} role='alertdialog'>
            <DialogTitle>
                <FormattedMessage
                    id='Applications.Listing.DeleteConfirmation.dialog.title'
                    defaultMessage='Delete Application'
                />
            </DialogTitle>
            <DialogContent>
                <DialogContentText>
                    <FormattedMessage
                        id='Applications.Listing.DeleteConfirmation.dialog.text.description'
                        defaultMessage='The application will be removed'
                    />
                </DialogContentText>
            </DialogContent>
            <DialogActions>
                <Button dense onClick={toggleDeleteConfirmation}>
                    <FormattedMessage
                        id='Applications.Listing.DeleteConfirmation.dialog.cancel'
                        defaultMessage='Cancel'
                    />
                </Button>
                <Button
                    id='itest-confirm-application-delete'
                    size='small'
                    variant='outlined'
                    color='primary'
                    onClick={handleAppDelete}
                >
                    <FormattedMessage
                        id='Applications.Listing.DeleteConfirmation.dialog,delete'
                        defaultMessage='Delete'
                    />
                </Button>
            </DialogActions>
        </Dialog>
    );
};
DeleteConfirmation.propTypes = {
    handleAppDelete: PropTypes.func.isRequired,
    isDeleteOpen: PropTypes.bool.isRequired,
    toggleDeleteConfirmation: PropTypes.func.isRequired,
};
export default DeleteConfirmation;
