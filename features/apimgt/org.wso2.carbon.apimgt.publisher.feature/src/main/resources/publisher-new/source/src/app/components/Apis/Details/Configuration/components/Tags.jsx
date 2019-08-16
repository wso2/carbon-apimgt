import React from 'react';
import PropTypes from 'prop-types';
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';
import ChipInput from 'material-ui-chip-input';

/**
 *
 *
 * @export
 * @param {*} props
 * @returns
 */
export default function Tags(props) {
    const { api, configDispatcher } = props;
    return (
        <Grid container spacing={1} alignItems='flex-start'>
            <Grid item>
                <Typography variant='subtitle1'>Tags</Typography>
            </Grid>
            <Grid item xs={4}>
                <ChipInput
                    value={api.tags}
                    onAdd={(tag) => {
                        configDispatcher({ action: 'tags', value: [...api.tags, tag] });
                    }}
                    onDelete={(tag) => {
                        configDispatcher({ action: 'tags', value: api.tags.filter(oldTag => oldTag !== tag) });
                    }}
                />
            </Grid>
        </Grid>
    );
}

Tags.propTypes = {
    api: PropTypes.shape({}).isRequired,
    configDispatcher: PropTypes.func.isRequired,
};
