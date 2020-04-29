import React, { useState } from 'react';
import Button from '@material-ui/core/Button';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogTitle from '@material-ui/core/DialogTitle';
import IconButton from '@material-ui/core/IconButton';
import CircularProgress from '@material-ui/core/CircularProgress';
import Alert from 'AppComponents/Shared/Alert';

export default function FormDialogBase({ title, children, icon, triggerButtonText, saveButtonText, formSaveCallback }) {
    const [open, setOpen] = React.useState(false);
    const [saving, setSaving] = useState(false);

    const handleClickOpen = () => {
        setOpen(true);
    };

    const handleClose = () => {
        setOpen(false);
    };

    const saveTriggerd = () => {
        const savedPromise = formSaveCallback();
        if (savedPromise) {
            setSaving(true);

            savedPromise.then((data) => {
                Alert.success(data);
            }).catch((e) => {
                Alert.error(e);
            }).finally(() => {
                setSaving(false);
                handleClose();
            })
        }

    }

    return (
        <>
            {icon && (<IconButton color="primary" component="span" onClick={handleClickOpen}>
                {icon}
            </IconButton>)}
            {triggerButtonText && (<Button variant="contained" color="primary" onClick={handleClickOpen}>
                {triggerButtonText}
            </Button>)}

            <Dialog open={open} onClose={handleClose} aria-labelledby="form-dialog-title">
                <DialogTitle id="form-dialog-title">{title}</DialogTitle>
                <DialogContent>
                    {children}
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleClose}>
                        Cancel
                    </Button>
                    <Button onClick={saveTriggerd} color="primary" variant="contained" disabled={saving}>
                        {saving ? (<CircularProgress size={16} />) : (<>{saveButtonText}</>)}
                    </Button>
                </DialogActions>
            </Dialog>
        </>
    );
}
