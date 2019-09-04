import React, { useState, useEffect } from 'react';
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
    const { onChange, policies: currentPolicies } = props;
    const [policies, setPolicies] = useState({});
    useEffect(() => {
        API.policies('subscription').then(response => setPolicies(response.body));
    }, []);

    if (!policies.list) {
        return <CircularProgress />;
    } else {
        return (
            <TextField
                fullWidth
                id='outlined-select-currency'
                select
                label='Business plan(s)'
                value={currentPolicies}
                name='policies'
                onChange={onChange}
                SelectProps={{
                    multiple: true,
                    renderValue: selected => selected.join(', '),
                }}
                helperText='Select one or multiple throttling policy for the API'
                margin='normal'
                variant='outlined'
            >
                {policies.list.map(policy => (
                    <MenuItem dense disableGutters key={policy.name} value={policy.displayName}>
                        <Checkbox checked={currentPolicies.includes(policy.name)} />
                        <ListItemText primary={policy.displayName} secondary={policy.description} />
                    </MenuItem>
                ))}
            </TextField>
        );
    }
}

SelectPolicies.defaultProps = {
    policies: [],
};
