import React, { Component } from 'react';
import {
    IconButton,
    Popper,
    Paper,
    ClickAwayListener,
    MenuItem,
    MenuList,
    Fade,
    // ListItemIcon,
    // ListItemText,
    // Divider,
} from '@material-ui/core';
import AccountCircle from '@material-ui/icons/AccountCircle';
// import NightMode from '@material-ui/icons/Brightness2';
import { withStyles } from '@material-ui/core/styles';
import { Link } from 'react-router-dom';
// import qs from 'qs';
import PropTypes from 'prop-types';
import { FormattedMessage } from 'react-intl';

const styles = theme => ({
    profileMenu: {
        zIndex: theme.zIndex.modal + 1,
        paddingTop: '5px',
    },
    userLink: {
        color: theme.palette.getContrastText(theme.palette.background.appBar),
        fontSize: theme.typography.fontSize,
        textTransform: 'uppercase',
    },
    accountIcon: {
        marginRight: 10,
    },
});

/**
 * Render the User Avatar with their name inside the Top AppBar component
 *
 * @class Avatar
 * @extends {Component}
 */
class Avatar extends Component {
    /**
     *Creates an instance of Avatar.
     * @param {Object} props @inheritdoc
     * @memberof Avatar
     */
    constructor(props) {
        super(props);
        this.state = {
            openMenu: false,
            profileIcon: null,
        };
        this.toggleMenu = this.toggleMenu.bind(this);
    }

    /**
     *
     * Open and Close (Toggle) Avatar dropdown menu
     * @param {React.SyntheticEvent} event `click` event on Avatar
     * @memberof Avatar
     */
    toggleMenu(event) {
        if (event.currentTarget.type === 'button') {
            this.setState({
                openMenu: !this.state.openMenu,
                profileIcon: event.currentTarget,
            });
        } else {
            this.setState({
                openMenu: false,
                profileIcon: this.state.profileIcon,
            });
        }
    }

    /**
     * Do OIDC logout redirection
     * @param {React.SyntheticEvent} e Click event of the submit button
     */
    doOIDCLogout = (e) => {
        e.preventDefault();
        window.location = '/publisher/services/logout';
    };

    /**
     *
     * @inheritdoc
     * @returns {React.Component} @inheritdoc
     * @memberof Avatar
     */
    render() {
        const { classes, user } = this.props;
        // const { pathname } = window.location;
        // const params = qs.stringify({
        //     referrer: pathname.split('/').reduce((acc, cv, ci) => (ci <= 1 ? '' : acc + '/' + cv)),
        // });
        const { openMenu, profileIcon } = this.state;
        return (
            <React.Fragment>
                <IconButton
                    id='profile-menu-btn'
                    aria-owns='profile-menu-appbar'
                    aria-haspopup='true'
                    color='inherit'
                    onClick={this.toggleMenu}
                    className={classes.userLink}
                >
                    <AccountCircle className={classes.accountIcon} /> {user.name}
                </IconButton>
                <Popper className={classes.profileMenu} open={openMenu} anchorEl={profileIcon} transition>
                    {({ TransitionProps }) => (
                        <Fade in={openMenu} {...TransitionProps} id='profile-menu-appbar'>
                            <Paper>
                                <ClickAwayListener onClickAway={this.toggleMenu}>
                                    <MenuList>
                                        {/* TODO: uncomment when component run without errors */}
                                        {/* <MenuItem onClick={this.toggleMenu}>Profile</MenuItem>
                                         <MenuItem onClick={this.toggleMenu}>My account</MenuItem> */}
                                        <Link to={{ pathname: '/services/logout' }}>
                                            <MenuItem onClick={this.doOIDCLogout} id='logout'>
                                                <FormattedMessage
                                                    id='Base.Header.avatar.Avatar.logout'
                                                    defaultMessage='Logout'
                                                />
                                            </MenuItem>
                                        </Link>
                                        {/* TODO: uncomment when component run without errors */}
                                        {/* <Divider />
                                         <MenuItem className={classes.menuItem} onClick={this.props.toggleTheme}>
                                         <ListItemText primary='Night Mode' />
                                         <ListItemIcon className={classes.icon}>
                                         <NightMode />
                                         </ListItemIcon>
                                         </MenuItem> */}
                                    </MenuList>
                                </ClickAwayListener>
                            </Paper>
                        </Fade>
                    )}
                </Popper>
            </React.Fragment>
        );
    }
}
Avatar.propTypes = {
    classes: PropTypes.shape({
        userLink: PropTypes.string,
        profileMenu: PropTypes.string,
        accountIcon: PropTypes.string,
    }).isRequired,
    user: PropTypes.shape({ name: PropTypes.string.isRequired }).isRequired,
};

export default withStyles(styles)(Avatar);
