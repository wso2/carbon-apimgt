import React from 'react';
import PropTypes from 'prop-types';
import { Grid } from '@material-ui/core/';

import ApiThumb from '../components/ApiThumb';

const CardView = ({ apis }) => {
    return (
        <Grid container justify='center' spacing={8}>
            {apis.list.map((api) => {
                return <ApiThumb key={api.id} api={api} />;
            })}
        </Grid>
    );
};

CardView.propTypes = {
    apis: PropTypes.arrayOf(PropTypes.object).isRequired,
};

export default CardView;
