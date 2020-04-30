import React from 'react';
import PropTypes from 'prop-types';
import clsx from 'clsx';
import { withStyles } from '@material-ui/core/styles';
import { injectIntl } from 'react-intl';
import Divider from '@material-ui/core/Divider';
import Drawer from '@material-ui/core/Drawer';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import RouteMenuMapping from 'AppComponents/Base/RouteMenuMapping';
import { Link as RouterLink, withRouter } from 'react-router-dom';
import Link from '@material-ui/core/Link';
import Configurations from 'Config';

const styles = (theme) => ({
    categoryHeader: {
        paddingTop: theme.spacing(2),
        paddingBottom: theme.spacing(2),
    },
    categoryHeaderPrimary: {
        color: theme.palette.common.white,
    },
    item: {
        paddingTop: 1,
        paddingBottom: 1,
        color: 'rgba(255, 255, 255, 0.7)',
        '&:hover,&:focus': {
            backgroundColor: 'rgba(255, 255, 255, 0.08)',
        },
    },
    itemCategory: {
        backgroundColor: '#232f3e',
        boxShadow: '0 -1px 0 #404854 inset',
        paddingTop: theme.spacing(2),
        paddingBottom: theme.spacing(2),
    },
    firebase: {
        fontSize: 24,
        color: theme.palette.common.white,
    },
    itemActiveItem: {
        color: '#4fc3f7',
    },
    itemPrimary: {
        fontSize: 'inherit',
    },
    itemIcon: {
        minWidth: 'auto',
        marginRight: theme.spacing(2),
    },
    divider: {
        marginTop: theme.spacing(2),
    },
});

/**
 * Render a list
 * @param {JSON} props .
 * @returns {JSX} Header AppBar components.
 */
function Navigator(props) {
    const {
        classes, intl, history, ...other
    } = props;
    const matchMenuPath = (currentRoute, pathToMatch) => {
        return (currentRoute.indexOf(pathToMatch) !== -1);
    };
    const routeMenuMapping = RouteMenuMapping(intl);
    const updateAllRoutePaths = (path) => {
        for (let i = 0; i < routeMenuMapping.length; i++) {
            const childRoutes = routeMenuMapping[i].children;
            if (childRoutes) {
                for (let j = 0; j < childRoutes.length; j++) {
                    if (matchMenuPath(path, childRoutes[j].path)) {
                        childRoutes[j].active = true;
                    } else {
                        childRoutes[j].active = false;
                    }
                }
            } else if (matchMenuPath(path, routeMenuMapping[i].path)) {
                routeMenuMapping[i].active = true;
            } else {
                routeMenuMapping[i].active = false;
            }
        }
    };
    history.listen((location) => {
        const { pathname } = location;
        updateAllRoutePaths(pathname);
    });
    const { location: { pathname: currentPath } } = history;
    updateAllRoutePaths(currentPath);
    return (
        // eslint-disable-next-line react/jsx-props-no-spreading
        <Drawer variant='permanent' {...other}>
            <List disablePadding>
                <ListItem className={clsx(classes.firebase, classes.item, classes.itemCategory)}>
                    <Link component={RouterLink} to='/'>
                        <img
                            alt='logo'
                            src={Configurations.app.context + '/site/public/images/logo.svg'}
                            width='180'
                        />
                    </Link>
                </ListItem>


                {routeMenuMapping.map(({
                    id, children, icon: parentIcon, path: parentPath, active: parentActive,
                }) => (
                    <>
                        {!children && (
                            <Link component={RouterLink} to={parentPath}>
                                <ListItem
                                    className={clsx(
                                        classes.item,
                                        classes.itemCategory,
                                        parentActive && classes.itemActiveItem,
                                    )}
                                >
                                    <ListItemIcon className={classes.itemIcon}>
                                        {parentIcon}
                                    </ListItemIcon>
                                    <ListItemText
                                        classes={{
                                            primary: classes.itemPrimary,
                                        }}
                                    >
                                        {id}
                                    </ListItemText>
                                </ListItem>
                            </Link>
                        )}
                        {children && (
                            <React.Fragment key={id}>
                                <ListItem className={classes.categoryHeader}>
                                    <ListItemText
                                        classes={{
                                            primary: classes.categoryHeaderPrimary,
                                        }}
                                    >
                                        {id}
                                    </ListItemText>
                                </ListItem>
                                {children.map(({
                                    id: childId, icon, path, active,
                                }) => (
                                    <Link component={RouterLink} to={path || '/'}>
                                        <ListItem
                                            key={childId}
                                            button
                                            className={clsx(classes.item, active && classes.itemActiveItem)}
                                        >
                                            <ListItemIcon className={classes.itemIcon}>{icon}</ListItemIcon>
                                            <ListItemText
                                                classes={{
                                                    primary: classes.itemPrimary,
                                                }}
                                            >
                                                {childId}
                                            </ListItemText>
                                        </ListItem>
                                    </Link>
                                ))}

                                <Divider className={classes.divider} />
                            </React.Fragment>
                        )}

                    </>
                ))}
            </List>
        </Drawer>
    );
}

Navigator.propTypes = {
    classes: PropTypes.shape({}).isRequired,
};

export default injectIntl(withRouter(withStyles(styles)(Navigator)));
