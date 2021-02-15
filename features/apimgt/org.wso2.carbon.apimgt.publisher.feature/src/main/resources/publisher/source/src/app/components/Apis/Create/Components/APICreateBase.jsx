import React from 'react';
import PropTypes from 'prop-types';
import Paper from '@material-ui/core/Paper';
import Grid from '@material-ui/core/Grid';

/**
 * Base component for all API create forms
 *
 * @param {Object} props title and children components are expected
 * @returns {React.Component} Base element
 */
export default function APICreateBase(props) {
    const { title, children } = props;
    return (
        <Grid container spacing={3}>
            <Grid item xs={12} />
            {/*
            Following two grids control the placement of whole create page
            For centering the content better use `container` props, but instead used an empty grid item for flexibility
             */}
            <Grid item sm={0} md={3} />
            <Grid item sm={12} md={6}>
                <Grid container spacing={5}>
                    <Grid item md={12}>
                        {title}
                    </Grid>
                    <Grid item xs={12} md={12}>
                        <Paper elevation={0}>{children}</Paper>
                    </Grid>
                </Grid>
            </Grid>
        </Grid>
    );
}
APICreateBase.propTypes = {
    title: PropTypes.element.isRequired,
    children: PropTypes.element.isRequired,
};
