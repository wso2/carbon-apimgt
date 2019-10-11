import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import TextField from '@material-ui/core/TextField';
import MenuItem from '@material-ui/core/MenuItem';
import ListItemText from '@material-ui/core/ListItemText';
import Checkbox from '@material-ui/core/Checkbox';
import CircularProgress from '@material-ui/core/CircularProgress';

import API from 'AppData/api';

/**
 * Trottling Policies dropdown selector used in minimized API Create form
 * @export
 * @param {*} props
 * @returns {React.Component}
 */
export default function SelectPolicies(props) {
    const {
        onChange, policies: selectedPolicies, multiple, required, helperText, isAPIProduct,
    } = props;
    const [policies, setPolicies] = useState({});
    useEffect(() => {
        API.policies('subscription').then(response => setPolicies(response.body));
    }, []);

    if (!policies.list) {
        return <CircularProgress />;
    } else {
        return (
            <TextField
                required={required}
                fullWidth
                id='outlined-select-currency'
                select
                label='Business plan(s)'
                value={selectedPolicies}
                name='policies'
                onChange={onChange}
                SelectProps={{
                    multiple,
                    renderValue: selected => (Array.isArray(selected) ? selected.join(', ') : selected),
                }}
                helperText={isAPIProduct ? helperText + 'API Product' : helperText + 'API'}
                margin='normal'
                variant='outlined'
            >
                {policies.list.map(policy => (
                    <MenuItem dense disableGutters={multiple} key={policy.name} value={policy.displayName}>
                        {multiple && <Checkbox color='primary' checked={selectedPolicies.includes(policy.name)} />}
                        <ListItemText primary={policy.displayName} secondary={policy.description} />
                    </MenuItem>
                ))}
            </TextField>
        );
    }
}

SelectPolicies.defaultProps = {
    policies: [],
    multiple: true,
    required: false,
    isAPIProduct: PropTypes.bool.isRequired,
    helperText: 'Select one or more throttling policies for the ',
};
