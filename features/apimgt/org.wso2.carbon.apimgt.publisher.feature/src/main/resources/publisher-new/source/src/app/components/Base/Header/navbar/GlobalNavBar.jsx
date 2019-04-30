import React from 'react';
import { ListItemIcon, Drawer, List, withStyles, ListItem, ListItemText } from '@material-ui/core';
import EndpointsIcon from '@material-ui/icons/ZoomOutMapOutlined';
import PropTypes from 'prop-types';
import { Link } from 'react-router-dom';
import CustomIcon from 'AppComponents/Shared/CustomIcon';

const styles = theme => ({
    list: {
        width: theme.custom.drawerWidth,
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
    leftLink_Icon: {
        color: theme.palette.getContrastText(theme.palette.background.leftMenu),
        fontSize: theme.custom.leftMenuIconSize + 'px',
    },
    listText: {
        color: theme.palette.getContrastText(theme.palette.background.drawer),
    },
});

const GlobalNavBar = (props) => {
    const {
        open, toggleGlobalNavBar, classes, theme,
    } = props;

    const commonStyle = {
        style: { top: 64 },
    };
    const paperStyles = {
        style: {
            backgroundColor: theme.palette.background.drawer,
            top: 64,
        },
    };
    const strokeColor = theme.palette.getContrastText(theme.palette.background.leftMenu);
    return (
        <div>
            <Drawer
                className={classes.drawerStyles}
                PaperProps={paperStyles}
                SlideProps={commonStyle}
                ModalProps={commonStyle}
                BackdropProps={commonStyle}
                open={open}
                onClose={toggleGlobalNavBar}
            >
                <div tabIndex={0} role='button' onClick={toggleGlobalNavBar} onKeyDown={toggleGlobalNavBar}>
                    <div className={classes.list}>
                        <List>
                            <Link to='/apis'>
                                <ListItem button>
                                    <ListItemIcon>
                                        <CustomIcon
                                            width={32}
                                            height={32}
                                            icon='api'
                                            className={classes.listText}
                                            strokeColor={strokeColor}
                                        />
                                    </ListItemIcon>
                                    <ListItemText classes={{ primary: classes.listText }} primary='APIs' />
                                </ListItem>
                            </Link>
                            <Link to='/endpoints'>
                                <ListItem button>
                                    <ListItemIcon>
                                        <EndpointsIcon className={classes.leftLink_Icon} />
                                    </ListItemIcon>
                                    <ListItemText classes={{ primary: classes.listText }} primary='Endpoints' />
                                </ListItem>
                            </Link>
                        </List>
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
    theme: PropTypes.shape({}).isRequired,
};

export default withStyles(styles, { withTheme: true })(GlobalNavBar);
