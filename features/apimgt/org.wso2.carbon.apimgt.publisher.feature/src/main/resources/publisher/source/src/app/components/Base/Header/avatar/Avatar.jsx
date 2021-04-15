import React, { Component } from 'react';
import {
    IconButton,
    Menu,
    MenuItem,
    Icon,
    Box,
} from '@material-ui/core';
import AccountCircle from '@material-ui/icons/AccountCircle';
import { withStyles } from '@material-ui/core/styles';
import { Link } from 'react-router-dom';
import PropTypes from 'prop-types';
import { FormattedMessage } from 'react-intl';
import Configurations from 'Config';
import ExitToAppIcon from '@material-ui/icons/ExitToApp';

const styles = (theme) => ({
    profileMenu: {
        zIndex: theme.zIndex.modal + 1,
        paddingTop: '5px',
    },
    userLink: {
        color: theme.palette.getContrastText(theme.palette.background.appBar),
        fontSize: theme.typography.fontSize,
        textTransform: 'uppercase',
        fontWeight: 'bold',
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
        this.state = { anchorEl: null };
        this.handleClick = this.handleClick.bind(this);
        this.handleClose = this.handleClose.bind(this);
    }

    /**
     * Do OIDC logout redirection
     * @param {React.SyntheticEvent} e Click event of the submit button
     */
    doOIDCLogout = (e) => {
        e.preventDefault();
        window.location = `${Configurations.app.context}/services/logout`;
    };

    /**
     *
     * Close Avatar dropdown menu
     * @memberof Avatar
     */
    handleClose() {
        this.setState({ anchorEl: null });
    }

    /**
     *
     * Open Avatar dropdown menu
     * @param {React.SyntheticEvent} event `click` event on Avatar
     * @memberof Avatar
     */
    handleClick(event) {
        this.setState({ anchorEl: event.currentTarget });
    }

    /**
     *
     * @inheritdoc
     * @returns {React.Component} @inheritdoc
     * @memberof Avatar
     */
    render() {
        const { classes, user } = this.props;
        let username = user.name;
        const count = (username.match(/@/g) || []).length;
        if (user.name.endsWith('@carbon.super') && count <= 1) {
            username = user.name.replace('@carbon.super', '');
        }
        const { anchorEl } = this.state;
        return (
            <>
                <IconButton
                    id='profile-menu-btn'
                    aria-owns='profile-menu-appbar'
                    aria-haspopup='true'
                    color='inherit'
                    onClick={this.handleClick}
                    className={classes.userLink}
                    disableFocusRipple
                    disableRipple
                >
                    <AccountCircle className={classes.accountIcon} />
                    {' '}
                    {username}
                    <Icon style={{ fontSize: '22px', marginLeft: '1px' }}>
                        keyboard_arrow_down
                    </Icon>
                </IconButton>
                <Menu
                    id='itest-logout-menu'
                    anchorEl={anchorEl}
                    keepMounted
                    open={Boolean(anchorEl)}
                    onClose={this.handleClose}
                    getContentAnchorEl={null}
                    anchorOrigin={{
                        vertical: 'bottom',
                        horizontal: 'center',
                    }}
                    transformOrigin={{
                        vertical: 'top',
                        horizontal: 'center',
                    }}
                    className={classes.profileMenu}
                >
                    <Link to={{ pathname: '/services/logout' }}>
                        <MenuItem onClick={this.doOIDCLogout} id='itest-logout'>
                            <Box mx={1} display='flex' alignItems='center' color='text.secondary'>
                                <ExitToAppIcon fontSize='small' />
                                <Box ml={1}>
                                    <FormattedMessage
                                        id='Base.Header.avatar.Avatar.logout'
                                        defaultMessage='Logout'
                                    />
                                </Box>
                            </Box>

                        </MenuItem>
                    </Link>
                </Menu>
            </>
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
