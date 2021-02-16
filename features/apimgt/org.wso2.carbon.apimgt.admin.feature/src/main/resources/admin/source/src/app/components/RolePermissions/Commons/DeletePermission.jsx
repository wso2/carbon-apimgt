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
import { useIntl } from 'react-intl';
import Button from '@material-ui/core/Button';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogTitle from '@material-ui/core/DialogTitle';
import Typography from '@material-ui/core/Typography';
import Box from '@material-ui/core/Box';
import CircularProgress from '@material-ui/core/CircularProgress';

import Alert from 'AppComponents/Shared/Alert';


/**
 *
 *
 * @export
 * @returns
 */
export default function DeletePermission(props) {
    const {
        onDelete, role, isAlias,
    } = props;
    const [open, setOpen] = useState(false);
    const [isDeleting, setIsDeleting] = useState(false);
    const intl = useIntl();

    const handleClickOpen = () => {
        setOpen(true);
    };

    const handleClose = () => {
        setOpen(false);
    };
    const handleConfirmation = (event) => {
        const { id } = event.currentTarget;
        setIsDeleting(true);
        Promise.resolve(onDelete(id, isAlias))
            .then(() => {
                Alert.info(
                    <span>
                        Scope Assignments
                        {' '}
                        <b>{role}</b>
                        {' '}
                        deleted successfully
                    </span>,
                );
                handleClose();
            })
            .catch((error) => {
                Alert.error(
                    intl.formatMessage({
                        id: 'RolePermissions.Common.DeletePermission.delete.scope.error',
                        defaultMessage: 'Something went wrong while deleting the scope assignments',
                    }),
                );
                console.error(error);
            })
            .finally(() => setIsDeleting(false));
    };
    return (
        <>
            <Button
                onClick={handleClickOpen}
                size='small'
                variant='outlined'
            >
                Delete
            </Button>
            <Dialog
                fullWidth
                maxWidth='xs'
                open={open}
                disableBackdropClick={isDeleting}
                onClose={handleClose}
                aria-labelledby='delete-confirmation'
            >
                <DialogTitle id='delete-confirmation'>
                    Delete scope assignments of
                    {' '}
                    <Typography display='inline' variant='subtitle2'>{role}</Typography>
                    {' '}
                    ?
                </DialogTitle>
                <DialogContent dividers>
                    <Box pl={5} mt={2} mb={2}>
                        Are you sure you want to delete scope assignments for
                        {' '}
                        <b>{role}</b>
                        {' '}
                        ?
                    </Box>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleClose}>Cancel</Button>
                    <Button
                        size='small'
                        variant='contained'
                        color='primary'
                        onClick={handleConfirmation}
                        id={role}
                        disabled={isDeleting}
                    >
                        {isDeleting && <CircularProgress size={16} />}
                        Delete
                    </Button>
                </DialogActions>
            </Dialog>
        </>
    );
}
