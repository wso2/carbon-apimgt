import React from 'react';
import PropTypes from 'prop-types';
import { ListItem, ListItemIcon, ListItemText, withStyles } from '@material-ui/core/';
import { Link } from 'react-router-dom';

const styles = () => ({
    navLink: {
        display: 'contents',
    },
    selected: {
        backgroundColor: 'red',
    },
});

const NavItem = (props) => {
    const {
        listItemProps, listItemTextProps, iconProps, classes, ...other
    } = props;
    const {
        selected, name, onClick, linkTo, NavIcon,
    } = other;

    const listItem = (
        <ListItem
            selected={selected}
            {...listItemProps}
            onClick={onClick}
            button
        >
            <ListItemIcon>{NavIcon}</ListItemIcon>
            <ListItemText {...listItemTextProps} primary={name} />
        </ListItem>
    );
    return linkTo ? (
        <Link className={classes.navLink} to={linkTo}>
            {listItem}
        </Link>
    ) : (
        listItem
    );
};

NavItem.defaultProps = {
    listItemProps: {},
    listItemTextProps: {},
    iconProps: {},
    onClick: () => {},
    linkTo: undefined,
    selected: false,
};

NavItem.propTypes = {
    listItemProps: PropTypes.shape({}),
    listItemTextProps: PropTypes.shape({}),
    iconProps: PropTypes.shape({}),
    name: PropTypes.string.isRequired,
    onClick: PropTypes.func,
    linkTo: PropTypes.string,
    classes: PropTypes.shape({}).isRequired,
    selected: PropTypes.bool,
};

export default withStyles(styles)(NavItem);
