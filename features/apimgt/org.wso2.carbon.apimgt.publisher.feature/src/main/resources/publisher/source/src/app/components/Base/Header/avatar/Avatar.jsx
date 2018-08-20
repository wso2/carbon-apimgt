import React, { Component } from 'react';
import {
    IconButton,
    Popper,
    Paper,
    ClickAwayListener,
    MenuItem,
    MenuList,
    Fade,
    ListItemIcon,
    ListItemText,
    Divider,
} from '@material-ui/core';
import AccountCircle from '@material-ui/icons/Person';
import NightMode from '@material-ui/icons/Brightness2';
import { withStyles } from '@material-ui/core/styles';

const styles = theme => ({
    profileCircle: {
        width: '50px',
        'border-radius': '50%',
        'vertical-align': 'middle',
        border: '1px solid #eee',
    },
    profileMenu: {
        zIndex: theme.zIndex.modal + 1,
        paddingTop: '5px',
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
        this.setState({
            openMenu: !this.state.openMenu,
            profileIcon: event.currentTarget,
        });
    }

    /**
     *
     * @inheritdoc
     * @returns {React.Component} @inheritdoc
     * @memberof Avatar
     */
    render() {
        const { classes } = this.props;
        const { openMenu, profileIcon } = this.state;
        return (
            <div>
                <IconButton
                    aria-owns='profile-menu-appbar'
                    aria-haspopup='true'
                    color='inherit'
                    onClick={this.toggleMenu}
                >
                    <AccountCircle className={classes.profileCircle} style={{ fontSize: '45' }} />
                </IconButton>
                <Popper className={classes.profileMenu} open={openMenu} anchorEl={profileIcon} transition>
                    {({ TransitionProps }) => (
                        <Fade in={openMenu} {...TransitionProps} id='profile-menu-appbar'>
                            <Paper>
                                <ClickAwayListener onClickAway={this.toggleMenu}>
                                    <MenuList>
                                        <MenuItem onClick={this.toggleMenu}>Profile</MenuItem>
                                        <MenuItem onClick={this.toggleMenu}>My account</MenuItem>
                                        <MenuItem onClick={this.toggleMenu}>Logout</MenuItem>
                                        <Divider />
                                        <MenuItem className={classes.menuItem} onClick={this.props.toggleTheme}>
                                            <ListItemText primary='Night Mode' />
                                            <ListItemIcon className={classes.icon}>
                                                <NightMode />
                                            </ListItemIcon>
                                        </MenuItem>
                                    </MenuList>
                                </ClickAwayListener>
                            </Paper>
                        </Fade>
                    )}
                </Popper>
            </div>
        );
    }
}

export default withStyles(styles)(Avatar);
