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

import React, { useState } from 'react';
import PropTypes from 'prop-types';
import Button from '@material-ui/core/Button';
import Box from '@material-ui/core/Box';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogTitle from '@material-ui/core/DialogTitle';
import CircularProgress from '@material-ui/core/CircularProgress';

/**
 * Render a pop-up dialog to add/edit an Microgateway label
 * @param {JSON} props .
 * @returns {JSX}.
 */
function AddItem(props) {
    const {
        buttonText, title, children, onSave, disabled, isSaving, saveButtonText,
    } = props;
    const [isOpen, setIsOpen] = useState(false);
    const handleDialogClose = () => setIsOpen(false);
    return (
        <>
            <Button
                variant='contained'
                color='primary'
                disabled={disabled}
                onClick={() => setIsOpen(true)}
            >
                {buttonText}
            </Button>

            <Dialog
                fullWidth
                maxWidth='sm'
                open={isOpen}
                onClose={handleDialogClose}
                aria-labelledby='form-dialog-title'
            >
                <DialogTitle id='form-dialog-title'>{title}</DialogTitle>
                <DialogContent dividers>
                    <Box minHeight={190}>
                        {children}
                    </Box>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleDialogClose}>
                        Cancel
                    </Button>
                    <Button onClick={onSave} color='primary' variant='contained' disabled={disabled}>
                        {isSaving ? <CircularProgress size={16} /> : <>{saveButtonText}</>}
                    </Button>
                </DialogActions>
            </Dialog>
        </>
    );
}

AddItem.defaultProps = {
    isSaving: false,
    disabled: false,
    saveButtonText: 'Save',
};

AddItem.propTypes = {
    buttonText: PropTypes.shape({}).isRequired,
    title: PropTypes.shape({}).isRequired,
    onSave: PropTypes.func.isRequired,
    isSaving: PropTypes.bool,
    disabled: PropTypes.bool,
    saveButtonText: PropTypes.string,
};

export default AddItem;
