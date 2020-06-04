import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import TextField from '@material-ui/core/TextField';
import MenuItem from '@material-ui/core/MenuItem';
import ListItemText from '@material-ui/core/ListItemText';
import CircularProgress from '@material-ui/core/CircularProgress';
import { FormattedMessage } from 'react-intl';
import API from 'AppData/api';
import { makeStyles } from '@material-ui/core/styles';
import { useAppContext } from 'AppComponents/Shared/AppContext';

const useStyles = makeStyles((theme) => ({
    mandatoryStar: {
        color: theme.palette.error.main,
        marginLeft: theme.spacing(0.1),
    },
}));

/**
 * Trottling Policies dropdown selector used in minimized API Create form
 * @export
 * @param {*} props
 * @returns {React.Component}
 */
export default function SelectKeyManager(props) {
    const {
        onChange, keyManager: keyManager
    } = props;
    const { settings } = useAppContext();
    const classes = useStyles();
    const handleValidateAndChange = ({ target: { value, name } }) => {
        validate('policies', value);
        onChange({ target: { name, value } });
    };
    if (!settings.keyManagerConfiguration) {
        return <CircularProgress />;
    } else {
        return (
            <TextField
                fullWidth
                select
                label={(
                    <>
                        <FormattedMessage
                            id='Apis.Create.Components.SelectPolicies.business.plans'
                            defaultMessage='Business plan(s)'
                        />
                        {(<sup className={classes.mandatoryStar}>*</sup>)}
                    </>
                )}
                value={keyManager}
                name='keyManager'
                onChange={handleValidateAndChange}
                SelectProps={{
                    multiple,
                    renderValue: (selected) => (Array.isArray(selected) ? selected.join(', ') : selected),
                }}
                helperText={isAPIProduct ? helperText + 'API Product' : helperText + 'API'}
                margin='normal'
                variant='outlined'
                InputProps={{
                    id: 'itest-id-apipolicies-input',
                }}
            >
                {settings.keyManagerConfiguration.map((keyManagerConnector) => (
                    <MenuItem
                        dense
                        id={keyManagerConnector.type}
                        key={keyManagerConnector.type}
                        value={keyManagerConnector.type}
                    >
                        <ListItemText primary={keyManagerConnector.type} checked={keyManager.includes(keyManagerConnector.type)}/>
                    </MenuItem>
                ))}
            </TextField>
        );
    }
}

SelectKeyManager.defaultProps = {
    keyManager: '',
    required: false,
    helperText: 'Select Key Manager Connector Type',
};
