import React, { useState } from 'react';
import PropTypes from 'prop-types';
import {
    Grid,
    TextField,
    makeStyles,
} from '@material-ui/core';

const useStyles = makeStyles(theme => ({
    textField: {
        marginLeft: theme.spacing(1),
        marginRight: theme.spacing(1),
        width: 300,
    },
}));

/**
 * The credentials component. This component holds the access and secret key pair.
 * @param {any} props The parameters to credentials component.
 * @returns {any} HTML view of the credentials component.
 */
export default function Credentials(props) {
    const { epConfig, setEpConfig, endpointsDispatcher } = props;
    const classes = useStyles();
    return (
        <Grid item>
            <TextField
                required
                id='outlined-required'
                label='Access Key'
                margin='normal'
                variant='outlined'
                className={classes.textField}
                value={epConfig.amznAccessKey}
                onChange={(event) => {
                    const newEpConfig = { ...epConfig };
                    newEpConfig.amznAccessKey = event.target.value;
                    setEpConfig(newEpConfig);
                    endpointsDispatcher({ action: 'set_awsCredentials', value: newEpConfig });
                }}
            />
            <TextField
                required
                id='outlined-password-input-required'
                label='Secret Key'
                type='password'
                margin='normal'
                variant='outlined'
                className={classes.textField}
                value={epConfig.amznSecretKey}
                onChange={(event) => {
                    const newEpConfig = { ...epConfig };
                    newEpConfig.amznSecretKey = event.target.value;
                    setEpConfig(newEpConfig);
                    endpointsDispatcher({ action: 'set_awsCredentials', value: newEpConfig });
                }}
            />
        </Grid>
    );
}

Credentials.propTypes = {
    epConfig: PropTypes.shape({}).isRequired,
    setEpConfig: PropTypes.func.isRequired,
    endpointsDispatcher: PropTypes.func.isRequired,
};
