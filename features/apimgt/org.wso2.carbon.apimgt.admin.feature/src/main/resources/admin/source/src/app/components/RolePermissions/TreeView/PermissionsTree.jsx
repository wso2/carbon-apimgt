import React from 'react';
import Button from '@material-ui/core/Button';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogTitle from '@material-ui/core/DialogTitle';

import Tree from './Tree';


/**
 *
 *
 * @export
 * @returns
 */
export default function PermissionsTree(props) {
    const [appMappings, scopes] = props;
    const [open, setOpen] = React.useState(false);

    const handleClickOpen = () => {
        setOpen(true);
    };

    const handleClose = () => {
        setOpen(false);
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
                <DialogTitle id='form-dialog-title'>Select Permissions</DialogTitle>
                <DialogContent style={{ height: '90vh' }}>
                    <Tree scopes={scopes} appMappings={appMappings} />
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleClose}>Cancel</Button>
                    <Button
                        size='small'
                        variant='outlined'
                        color='primary'
                        onClick={handleClose}
                    >
                        Ok
                    </Button>
                </DialogActions>
            </Dialog>
        </div>
    );
}
