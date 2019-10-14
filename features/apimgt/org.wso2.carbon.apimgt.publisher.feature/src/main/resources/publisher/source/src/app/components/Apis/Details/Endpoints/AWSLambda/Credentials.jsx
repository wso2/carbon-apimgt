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
    const { saveAPI, epConfig, setEpConfig } = props;
    const classes = useStyles();
    const [accessKey, setAccessKey] = useState(epConfig.amznAccessKey);
    const [secretKey, setSecretKey] = useState(epConfig.amznSecretKey);
    const updateAccessKey = () => {
        epConfig.amznAccessKey = accessKey;
        setEpConfig(epConfig);
        saveAPI();
    };
    const updateSecretKey = () => {
        epConfig.amznSecretKey = secretKey;
        setEpConfig(epConfig);
        saveAPI();
    };
    return (
        <Grid item>
            <TextField
                required
                id='outlined-required'
                label='Access Key'
                margin='normal'
                variant='outlined'
                className={classes.textField}
                value={accessKey}
                onChange={event => setAccessKey(event.target.value)}
                onBlur={updateAccessKey}
            />
            <TextField
                required
                id='outlined-password-input-required'
                label='Secret Key'
                type='password'
                margin='normal'
                variant='outlined'
                className={classes.textField}
                value={secretKey}
                onChange={event => setSecretKey(event.target.value)}
                onBlur={updateSecretKey}
            />
        </Grid>
    );
}

Credentials.propTypes = {
    saveAPI: PropTypes.func.isRequired,
    epConfig: PropTypes.shape({}).isRequired,
    setEpConfig: PropTypes.func.isRequired,
};
