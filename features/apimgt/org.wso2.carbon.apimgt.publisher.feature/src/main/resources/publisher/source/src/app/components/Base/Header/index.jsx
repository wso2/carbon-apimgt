import React from 'react';
import { IconButton, Toolbar, AppBar, Typography } from '@material-ui/core';
import { withStyles } from '@material-ui/core/styles';
import { Menu as MenuIcon } from '@material-ui/icons';
import SearchIcon from '@material-ui/icons/SearchOutlined';
import Hidden from '@material-ui/core/Hidden';
import PropTypes from 'prop-types';

import Avatar from './avatar/Avatar';
import HeaderSearch from './headersearch/HeaderSearch';
import GlobalNavBar from './navbar/GlobalNavBar';
import User from '../../../data/User';

const styles = theme => ({
    appBar: {
        zIndex: theme.zIndex.modal + 1,
    },
    typoRoot: {
        marginLeft: theme.spacing.unit * 3,
        marginRight: theme.spacing.unit * 3,
    },
});

/**
 * Construct the Global AppBar header section
 * @class Header
 * @extends {React.Component}
 */
class Header extends React.Component {
    /**
     *Creates an instance of Header.
     * @param {Object} props @inheritdoc
     * @memberof Header
     */
    constructor(props) {
        super(props);
        this.state = {
            openNavBar: false,
            smScreen: false,
        };
        this.toggleGlobalNavBar = this.toggleGlobalNavBar.bind(this);
        this.toggleSmSearch = this.toggleSmSearch.bind(this);
    }

    /**
     * Toggle the Global LHS Navbar visibility
     *
     * @memberof Header
     */
    toggleGlobalNavBar() {
        this.setState({ openNavBar: !this.state.openNavBar });
    }

    /**
     * Show search input in sm breakpoint or lower resolution
     */
    toggleSmSearch() {
        this.setState({ smScreen: !this.state.smScreen });
    }

    /**
     *
     * @inheritdoc
     * @returns {React.ComponentClass} @inheritdoc
     * @memberof Header
     */
    render() {
        const { openNavBar, smScreen } = this.state;
        const { classes, avatar, user } = this.props;
        return (
            <div>
                <AppBar className={classes.appBar} position='fixed'>
                    <Toolbar>
                        <IconButton onClick={this.toggleGlobalNavBar} color='inherit'>
                            <MenuIcon style={{ fontSize: 35 }} />
                        </IconButton>
                        <Typography style={{ flexGrow: '1' }} color='inherit' variant='title'>
                            WSO2 API Publisher
                        </Typography>
                        <Hidden smDown>
                            <HeaderSearch />
                        </Hidden>
                        <Hidden mdUp>
                            <IconButton onClick={this.toggleSmSearch} color='inherit'>
                                <SearchIcon style={{ fontSize: 35 }} />
                            </IconButton>
                            {smScreen && <HeaderSearch toggleSmSearch={this.toggleSmSearch} smSearch={smScreen} />}
                        </Hidden>
                        <div className={classes.typoRoot}>{user.name}</div>
                        {avatar}
                    </Toolbar>
                </AppBar>
                <GlobalNavBar toggleGlobalNavBar={this.toggleGlobalNavBar} open={openNavBar} />
            </div>
        );
    }
}
Header.defaultProps = {
    avatar: <Avatar />,
};

Header.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    avatar: PropTypes.element,
    user: PropTypes.instanceOf(User).isRequired,
};

export default withStyles(styles)(Header);
