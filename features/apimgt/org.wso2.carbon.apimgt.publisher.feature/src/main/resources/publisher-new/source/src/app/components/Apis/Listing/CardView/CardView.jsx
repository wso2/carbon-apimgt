import React from 'react';
import PropTypes from 'prop-types';
import { Grid } from '@material-ui/core/';

import ApiThumb from '../components/ImageGenerator/ApiThumb';

const CardView = ({ apis, updateAPIsList }) => {
    return (
        <Grid container justify='flex-start' spacing={8}>
            {apis.list.map((api) => {
                return <ApiThumb key={api.id} updateAPIsList={updateAPIsList} api={api} apiType={apis.apiType} />;
            })}
        </Grid>
    );
};

CardView.propTypes = {
    apis: PropTypes.shape({ list: PropTypes.array, count: PropTypes.number, apiType: PropTypes.string }).isRequired,
    updateAPIsList: PropTypes.func.isRequired,
};

export default CardView;
