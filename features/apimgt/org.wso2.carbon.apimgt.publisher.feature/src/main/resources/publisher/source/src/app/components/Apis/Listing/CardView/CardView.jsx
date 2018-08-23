import React from 'react';
import PropTypes from 'prop-types';
import { Grid } from '@material-ui/core/';
import IconButton from '@material-ui/core/IconButton';
import DeleteIcon from '@material-ui/icons/Delete';

import ApiThumb from '../components/ApiThumb';

const CardView = ({ apis, handleApiDelete }) => {
    return (
        <Grid container justify='flex-start' spacing={8}>
            {apis.list.map((api) => {
                const deleteButton = (
                    <IconButton id={api.id} onClick={handleApiDelete} color='secondary' aria-label='Delete'>
                        <DeleteIcon />
                    </IconButton>
                );
                return <ApiThumb deleteButton={deleteButton} api={api} />;
            })}
        </Grid>
    );
};

CardView.propTypes = {
    apis: PropTypes.arrayOf(PropTypes.object).isRequired,
    handleApiDelete: PropTypes.func.isRequired,
};

export default CardView;
