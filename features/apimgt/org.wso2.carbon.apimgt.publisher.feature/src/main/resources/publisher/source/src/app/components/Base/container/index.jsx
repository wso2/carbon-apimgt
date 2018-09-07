import React, { Fragment } from 'react';
import { Grid } from '@material-ui/core';
import { withStyles } from '@material-ui/core/styles';
import PropTypes from 'prop-types';

const styles = theme => ({
    pageNav: {},
    pageContent: {
        padding: theme.spacing.unit * 3,
    },
    pageContainer: {
        background: theme.palette.background.container,
    },
    pageTopMenu: {
        boxSizing: 'border-box',
        height: '100px',
        borderBottom: '1px solid',
        padding: '10px 10px 0px 10px',
    },
});

/**
 * Base container for all the pages in the APP
 * @param {Object} props
 * pageNav : Default left side page navigation bar
 * children : Page content
 * pageTopMenu : if available
 * @returns {React.Component} page container
 *
 * */
const Container = (props) => {
    const {
        classes, pageNav, children, pageTopMenu,
    } = props;
    return (
        <Fragment>
            <Grid item xs={2} sm={1} md={2} lg={2}>
                {pageNav}
            </Grid>
            <Grid item className={classes.pageContainer} xs={10} sm={11} md={10} lg={10}>
                <Grid className={classes.pageContainer} container>
                    {pageTopMenu && (
                        <Grid className={classes.pageTopMenu} item container xs={12} sm={12} md={12} lg={12}>
                            {pageTopMenu}
                        </Grid>
                    )}
                    <Grid className={classes.pageContent} item xs={12} sm={12} md={12} lg={12}>
                        {children}
                    </Grid>
                </Grid>
            </Grid>
        </Fragment>
    );
};
Container.defaultProps = {
    pageTopMenu: null,
};

Container.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    pageNav: PropTypes.element.isRequired,
    children: PropTypes.element.isRequired,
    pageTopMenu: PropTypes.element,
};

export default withStyles(styles)(Container);
