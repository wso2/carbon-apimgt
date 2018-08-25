import React from 'react';
import PropTypes from 'prop-types';
import { Grid } from '@material-ui/core/';

import ApiThumb from '../components/ApiThumb';

const CardView = ({ apis, updateAPIsList }) => {
    return (
        <Grid container justify='flex-start' spacing={8}>
            {apis.list.map((api) => {
                return <ApiThumb updateAPIsList={updateAPIsList} api={api} />;
            })}
        </Grid>
    );
};

CardView.propTypes = {
    apis: PropTypes.arrayOf(PropTypes.object).isRequired,
    updateAPIsList: PropTypes.func.isRequired,
};

export default CardView;
