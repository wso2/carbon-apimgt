import React, { useState } from 'react';
import Button from '@material-ui/core/Button';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogTitle from '@material-ui/core/DialogTitle';
import Typography from '@material-ui/core/Typography';
import Box from '@material-ui/core/Box';
import CircularProgress from '@material-ui/core/CircularProgress';

import Alert from 'AppComponents/Shared/Alert';
import PermissionTree from './PermissionTree';


/**
 *
 *
 * @export
 * @returns
 */
export default function PermissionsSelector(props) {
    const {
        appMappings, role, onCheck, onSave,
    } = props;
    const [open, setOpen] = useState(false);
    const [isSaving, setIsSaving] = useState(false);

    const handleClickOpen = () => {
        setOpen(true);
    };

    const handleClose = () => {
        // TODO: Need to reset the mapping to last saved state ~tmkb
        setOpen(false);
    };
    const handleSave = () => {
        setIsSaving(true);
        onSave(appMappings)
            .then(() => {
                Alert.info(
                    <>
                        Update permissions for
                        {' '}
                        <b>{role}</b>
                        {' '}
                        successfully
                    </>,
                );
                handleClose();
            })
            .catch((error) => {
                Alert.error('Something went wrong while updating the permissions');
                console.error(error);
            })
            .finally(() => setIsSaving(false));
    };
    return (
        <div>
            <Button
                onClick={handleClickOpen}
                size='small'
                variant='outlined'
                color='primary'
            >
                Permissions
            </Button>
            <Dialog
                fullWidth
                maxWidth='md'
                open={open}
                disableBackdropClick={isSaving}
                onClose={handleClose}
                aria-labelledby='form-dialog-title'
            >
                <DialogTitle id='form-dialog-title'>
                    <Typography variant='h5' display='block' gutterBottom>
                        {role}
                        <Box display='inline' pl={1}>
                            <Typography variant='caption' gutterBottom>Select Permissions</Typography>
                        </Box>
                    </Typography>
                </DialogTitle>
                <DialogContent style={{ height: '90vh' }}>
                    <Box pl={5}>
                        <PermissionTree onCheck={onCheck} role={role} appMappings={appMappings} />
                    </Box>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleClose}>Cancel</Button>
                    <Button
                        size='small'
                        variant='outlined'
                        color='primary'
                        onClick={handleSave}
                    >
                        {isSaving && <CircularProgress size={16} />}
                        Save
                    </Button>
                </DialogActions>
            </Dialog>
        </div>
    );
}
