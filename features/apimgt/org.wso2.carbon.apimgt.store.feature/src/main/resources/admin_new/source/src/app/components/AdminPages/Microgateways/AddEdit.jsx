import React, { useReducer, useEffect } from 'react';
import TextField from '@material-ui/core/TextField';
import DialogContentText from '@material-ui/core/DialogContentText';
import { makeStyles } from '@material-ui/core/styles';
import FormDialogBase from 'AppComponents/AdminPages/Addons/FormDialogBase';
import Alert from 'AppComponents/Shared/Alert';

const useStyles = makeStyles((theme) => ({
    error: {
        color: theme.palette.error.dark,
    },
}));

// Mock API call to save or edit
function apiCall() {
    return new Promise(function (resolve, reject) {
        setTimeout(() => { resolve('Successfully did something') }, 2000);
    });
}

let initialState = {
    label: '',
    description: '',
};

function reducer(state, { field, value }) {
    return {
        ...state,
        [field]: value,
    }
}

export default function AddEdit({ updateList, dataRow, icon, triggerButtonText, title }) {
    const classes = useStyles();
    let id = null;
    // If the dataRow is there ( form is in edit mode ) else it's a new creation
    useEffect(() => {
        initialState = {
            label: '',
            description: '',
        }   
    }, [title]);

    if (dataRow) {
        const { label: originalLabel, description: originalDescription } = dataRow;
        id = dataRow.id;

        initialState = {
            label: originalLabel,
            description: originalDescription,
        }
    }
    const [state, dispatch] = useReducer(reducer, initialState);
    const { label, description } = state;

    const onChange = (e) => {
        dispatch({ field: e.target.name, value: e.target.value });
    }
    const hasErrors = (fieldName, value) => {
        let error = false;
        switch (fieldName) {
            case 'label':
                error = value === '' ? fieldName + ' is Empty' : false;
                break;
        }
        return error;
    }
    const getAllFormErrors = () => {
        let errorText = '';
        const labelErrors = hasErrors('label', label);
        if (labelErrors) {
            errorText += labelErrors + '\n';
        }
        return errorText;
    }

    const formSaveCallback = () => {
        const formErrors = getAllFormErrors();
        if (formErrors !== '') {
            Alert.error(formErrors);
            return (false);
        }
        // Do the API call
        let promiseAPICall = apiCall();
        if (id) {
            // assign the update promise to the promiseAPICall
        }
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
        title={title}
        saveButtonText='Save'
        icon={icon}
        triggerButtonText={triggerButtonText}
        formSaveCallback={formSaveCallback}
    >
        <DialogContentText>
            To subscribe to this website, please enter your email address here. We will send updates
            occasionally.
        </DialogContentText>
        <TextField
            autoFocus
            margin="dense"
            name="label"
            value={label}
            onChange={onChange}
            label={<span>Label <span className={classes.error}>*</span></span>}
            fullWidth
            error={hasErrors('label', label)}
            helperText={hasErrors('label', label) || 'Enter gateway label'}
            variant="outlined"
        />
        <TextField
            margin="dense"
            name="description"
            value={description}
            onChange={onChange}
            label="Description"
            fullWidth
            multiline={true}
            helperText='Enter description'
            variant="outlined"
        />
    </FormDialogBase>;
}
