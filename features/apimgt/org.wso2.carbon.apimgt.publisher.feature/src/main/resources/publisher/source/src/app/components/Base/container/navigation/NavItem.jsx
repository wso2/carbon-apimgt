import React from 'react';
import PropTypes from 'prop-types';
import { ListItem, ListItemIcon, ListItemText, withStyles } from '@material-ui/core/';
import { Link } from 'react-router-dom';

const styles = () => {};

const NavItem = (props) => {
    const {
        listItemProps, listItemTextProps, iconProps, ...other
    } = props;
    const {
        name, onClick, linkTo, NavIcon,
    } = other;

    const listItem = (
        <ListItem {...listItemProps} onClick={onClick} button>
            <ListItemIcon>{NavIcon}</ListItemIcon>
            <ListItemText {...listItemTextProps} primary={name} />
        </ListItem>
    );
    return linkTo ? <Link to={linkTo}> {listItem} </Link> : listItem;
};

NavItem.defaultProps = {
    listItemProps: {},
    listItemTextProps: {},
    iconProps: {},
    onClick: {},
    linkTo: undefined,
};

NavItem.propTypes = {
    listItemProps: PropTypes.string,
    listItemTextProps: PropTypes.string,
    iconProps: PropTypes.string,
    name: PropTypes.string.isRequired,
    onClick: PropTypes.string,
    linkTo: PropTypes.string,
    classes: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(NavItem);
