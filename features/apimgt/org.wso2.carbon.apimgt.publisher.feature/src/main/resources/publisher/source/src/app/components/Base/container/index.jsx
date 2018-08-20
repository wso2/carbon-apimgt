import React, { Fragment } from 'react';
import { Grid } from '@material-ui/core';
import { withStyles } from '@material-ui/core/styles';
import PropTypes from 'prop-types';

const styles = theme => ({
    pageNav: {},
    pageContent: {
        padding: theme.spacing.unit * 3,
        background: theme.palette.background.container,
    },
});

const Container = (props) => {
    const { classes, pageNav, children } = props;
    return (
        <Fragment>
            <Grid item xs={2} sm={1} md={2} lg={1}>
                {pageNav}
            </Grid>
            <Grid className={classes.pageContent} item xs={10} sm={11} md={10} lg={11}>
                {children}
            </Grid>
        </Fragment>
    );
};

Container.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    pageNav: PropTypes.element.isRequired,
    children: PropTypes.element.isRequired,
};

export default withStyles(styles)(Container);
