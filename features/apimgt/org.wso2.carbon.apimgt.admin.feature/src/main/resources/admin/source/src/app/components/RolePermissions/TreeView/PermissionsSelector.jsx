import React from 'react';
import Button from '@material-ui/core/Button';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogTitle from '@material-ui/core/DialogTitle';
import Typography from '@material-ui/core/Typography';
import Box from '@material-ui/core/Box';
import PermissionAPI from 'AppData/PermissionScopes';

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
        appMappings, role, onCheck,
    } = props;
    const [open, setOpen] = React.useState(false);

    const handleClickOpen = () => {
        setOpen(true);
    };

    const handleClose = () => {
        // TODO: Need to reset the mapping to last saved state ~tmkb
        setOpen(false);
    };

    const handelOnSave = () => {
        const payload = [];
        for (const appScopes of Object.values(appMappings)) {
            for (const scope of appScopes) {
                payload.push({ ...scope, roles: scope.roles.join(',') });
            }
        }
        PermissionAPI.updateSystemScopes({ count: payload.length, list: payload }).then(() => {
            Alert.info('Permissions updated successfully');

            handleClose();
        }).catch((error) => {
            Alert.error('Something went wrong while updating the permissions');
            console.error(error);
        });
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
                        onClick={handelOnSave}
                    >
                        Save
                    </Button>
                </DialogActions>
            </Dialog>
        </div>
    );
}
