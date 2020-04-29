import React from 'react';
import DialogContentText from '@material-ui/core/DialogContentText';
import DeleteForeverIcon from '@material-ui/icons/DeleteForever';
import FormDialogBase from 'AppComponents/AdminPages/Addons/FormDialogBase';

// Mock API call to save or edit
function apiCall() {
    return new Promise(function (resolve, reject) {
        setTimeout(() => { resolve('Successfully did something') }, 2000);
    });
}

export default function Delete({ updateList, dataRow }) {
    const { id } = dataRow;

    const formSaveCallback = () => {
        // Do the API call
        let promiseAPICall = apiCall();
       
        promiseAPICall.then((data) => {
            updateList();
            return (data);
        })
        .catch((e) => {
            return (e);
        });
        return (promiseAPICall);
    }

    return <FormDialogBase
        title='Delete Gateway Label?'
        saveButtonText='Delete'
        icon={<DeleteForeverIcon />}
        formSaveCallback={formSaveCallback}
    >
        <DialogContentText>
            This gateway label will be deleted. And some more info about what is going to happen to it's hosts etc...   
        </DialogContentText>
    </FormDialogBase>;
}