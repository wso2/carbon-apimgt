import React from 'react';
import PropTypes from 'prop-types';
import Grid from '@material-ui/core/Grid';
import TextField from '@material-ui/core/TextField';


/**
 *
 *
 * @export
 * @param {*} props
 * @returns
 */
export default function Description(props) {
    const { api, configDispatcher } = props;
    return (
        <Grid container spacing={1} alignItems='flex-start'>
            <Grid item xs={4}>
                <TextField
                    id='outlined-multiline-static'
                    label='Description'
                    multiline
                    rows='10'
                    value={api.description}
                    margin='normal'
                    fullWidth
                    variant='outlined'
                    onChange={e => configDispatcher({ action: 'description', value: e.target.value })}
                />
            </Grid>
        </Grid>
    );
}

Description.propTypes = {
    api: PropTypes.shape({}).isRequired,
    configDispatcher: PropTypes.func.isRequired,
};
