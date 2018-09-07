import React from 'react';
import { ListItemIcon, Drawer, List, Divider, withStyles, ListItem, ListItemText } from '@material-ui/core';
import APIsIcon from '@material-ui/icons/Power';
import EndpointsIcon from '@material-ui/icons/ZoomOutMapOutlined';
import HomeIcon from '@material-ui/icons/Home';
import withWidth, { isWidthDown } from '@material-ui/core/withWidth';
import PropTypes from 'prop-types';
import { Link } from 'react-router-dom';

const styles = theme => ({
    list: {
        width: 250,
    },
    drawerStyles: {
        top: 56, // Based on https://github.com/mui-org/material-ui/issues/10076#issuecomment-361232810
        [`${theme.breakpoints.up('xs')} and (orientation: landscape)`]: {
            top: 48,
        },
        [theme.breakpoints.up('sm')]: {
            top: 64,
        },
    },
});

const homeIcon = (
    <Link to='/'>
        <ListItem button>
            <ListItemIcon>
                <HomeIcon />
            </ListItemIcon>
            <ListItemText primary='Home' />
        </ListItem>
    </Link>
);

const globalPages = (
    <React.Fragment>
        <Link to='/apis'>
            <ListItem button>
                <ListItemIcon>
                    <APIsIcon />
                </ListItemIcon>
                <ListItemText primary='APIs' />
            </ListItem>
        </Link>
        <Link to='/endpoints'>
            <ListItem button>
                <ListItemIcon>
                    <EndpointsIcon />
                </ListItemIcon>
                <ListItemText primary='Endpoints' />
            </ListItem>
        </Link>
    </React.Fragment>
);

const GlobalNavBar = (props) => {
    const {
        open, toggleGlobalNavBar, classes, width,
    } = props;

    let top = 64;
    if (isWidthDown('sm', width)) {
        top = 56;
    } else if (isWidthDown('xs', width)) {
        top = 48;
    }
    // TODO: Refer to fix: https://github.com/mui-org/material-ui/issues/10076#issuecomment-361232810 ~tmkb
    const commonStyle = { style: { top } };
    return (
        <div>
            <Drawer
                className={classes.drawerStyles}
                PaperProps={commonStyle}
                SlideProps={commonStyle}
                ModalProps={commonStyle}
                BackdropProps={commonStyle}
                open={open}
                onClose={toggleGlobalNavBar}
            >
                <div tabIndex={0} role='button' onClick={toggleGlobalNavBar} onKeyDown={toggleGlobalNavBar}>
                    <div className={classes.list}>
                        <List>{homeIcon}</List>
                        <Divider />
                        <List>{globalPages}</List>
                    </div>
                </div>
            </Drawer>
        </div>
    );
};

GlobalNavBar.propTypes = {
    open: PropTypes.bool.isRequired,
    toggleGlobalNavBar: PropTypes.func.isRequired,
    classes: PropTypes.shape({}).isRequired,
    width: PropTypes.string.isRequired,
};

export default withStyles(styles)(withWidth()(GlobalNavBar));
