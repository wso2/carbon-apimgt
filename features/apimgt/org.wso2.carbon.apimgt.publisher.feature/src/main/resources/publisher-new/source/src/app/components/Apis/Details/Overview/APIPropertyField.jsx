import React from 'react';
import PropTypes from 'prop-types';
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';

const APIPropertyField = (props) => {
    const { name, children } = props;

    return (
        <Grid item>
            <Grid container spacing={16} direction='row' alignItems='center'>
                <Grid item style={{ flexGrow: 1 }}>
                    <Grid container direction='row' justify='space-between'>
                        <Grid item>
                            <Typography variant='subheading'>{name}</Typography>
                        </Grid>
                        <Grid item>
                            <Typography variant='subheading'>:</Typography>
                        </Grid>
                    </Grid>
                </Grid>
                <Grid item lg={10} md={10} sm={10} xs={10}>
                    {children}
                </Grid>
            </Grid>
        </Grid>
    );
};

APIPropertyField.propTypes = {
    name: PropTypes.string.isRequired,
    children: PropTypes.oneOfType([PropTypes.element, PropTypes.arrayOf(PropTypes.element)]).isRequired,
};

export default APIPropertyField;
