import React, { useState } from 'react';
import PropTypes from 'prop-types';
import {
    Button,
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
    button: {
        paddingTop: 30,
        paddingBottom: 30,
        display: 'inline-block',
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
    const [isEditable, setIsEditable] = useState(false);
    const [isChanged, setIsChanged] = useState(false);
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
        isEditable ?
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
                    value={isChanged ? secretKey : 'abcdefghijklmnopqrstuvwxyz'}
                    onChange={(event) => {
                        setIsChanged(true);
                        setSecretKey(event.target.value);
                    }}
                    onBlur={updateSecretKey}
                />
                <div className={classes.button}>
                    <Button>Save</Button>
                </div>
                <div className={classes.button}>
                    <Button onClick={() => setIsEditable(false)}>Cancel</Button>
                </div>
            </Grid> :
            <Grid item>
                <div className={classes.button}>
                    <Button onClick={() => setIsEditable(true)}>Edit</Button>
                </div>
            </Grid>
    );
}

Credentials.propTypes = {
    saveAPI: PropTypes.func.isRequired,
    epConfig: PropTypes.shape({}).isRequired,
    setEpConfig: PropTypes.func.isRequired,
};
