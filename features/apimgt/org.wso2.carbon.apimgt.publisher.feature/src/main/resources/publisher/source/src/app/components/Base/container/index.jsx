import React from 'react';
import { withStyles } from '@material-ui/core/styles';
import PropTypes from 'prop-types';

const styles = (theme) => ({
    LeftMenu: {
        backgroundColor: theme.palette.background.leftMenu,
        width: theme.custom.leftMenuWidth,
        textAlign: 'center',
        fontFamily: theme.typography.fontFamily,
        position: 'absolute',
        bottom: 0,
        left: 0,
        top: 0,
    },
    content: {
        display: 'flex',
        flex: 1,
        flexDirection: 'column',
        marginLeft: theme.custom.leftMenuWidth,
        paddingBottom: theme.spacing(3),
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
        <>
            <div className={classes.LeftMenu}>{pageNav}</div>
            <div className={classes.content}>
                {pageTopMenu && <>{pageTopMenu}</>}
                {children}
            </div>
        </>
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
