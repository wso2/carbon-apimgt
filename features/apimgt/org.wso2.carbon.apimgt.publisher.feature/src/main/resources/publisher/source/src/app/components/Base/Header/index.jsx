/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import React from 'react';
import { IconButton, Toolbar, AppBar } from '@material-ui/core';
import { withStyles } from '@material-ui/core/styles';
import MenuIcon from '@material-ui/icons/Menu';
import SearchIcon from '@material-ui/icons/SearchOutlined';
import Hidden from '@material-ui/core/Hidden';
import PropTypes from 'prop-types';
import { Link } from 'react-router-dom';
import VerticalDivider from 'AppComponents/Shared/VerticalDivider';
import SettingsButton from 'AppComponents/Base/Header/settings/SettingsButton';

import Avatar from './avatar/Avatar';
import HeaderSearch from './headersearch/HeaderSearch';
import GlobalNavBar from './navbar/GlobalNavBar';

const styles = (theme) => ({
    appBar: {
        position: 'relative',
        background: theme.palette.background.appBar,
    },
    typoRoot: {
        marginLeft: theme.spacing(3),
        marginRight: theme.spacing(3),
        textTransform: 'capitalize',
    },
    brandLink: {
        color: theme.palette.primary.contrastText,
    },
    toolbar: {
        minHeight: 56,
        [`${theme.breakpoints.up('xs')} and (orientation: landscape)`]: {
            minHeight: 48,
        },
        [theme.breakpoints.up('sm')]: {
            minHeight: 64,
        },
    },
    menuIcon: {
        color: theme.palette.getContrastText(theme.palette.background.appBar),
        fontSize: 35,
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
        const { openNavBar } = this.state;
        this.setState({ openNavBar: !openNavBar });
    }

    /**
     * Show search input in sm breakpoint or lower resolution
     */
    toggleSmSearch() {
        const { smScreen } = this.state;
        this.setState({ smScreen: !smScreen });
    }

    /**
     *
     * @inheritdoc
     * @returns {React.ComponentClass} @inheritdoc
     * @memberof Header
     */
    render() {
        const { openNavBar, smScreen } = this.state;
        const {
            classes, avatar, settings, theme,
        } = this.props;
        return (
            <>
                <AppBar className={classes.appBar} position='fixed'>
                    <Toolbar className={classes.toolbar}>
                        <Hidden mdUp>
                            <IconButton onClick={this.toggleGlobalNavBar}>
                                <MenuIcon className={classes.menuIcon} />
                            </IconButton>
                        </Hidden>
                        <Link to='/'>
                            <img
                                src={theme.custom.logo}
                                alt={theme.custom.title}
                                style={{ height: theme.custom.logoHeight, width: theme.custom.logoWidth }}
                            />
                        </Link>
                        <GlobalNavBar toggleGlobalNavBar={this.toggleGlobalNavBar} open={openNavBar} />
                        <VerticalDivider height={32} />
                        <Hidden smDown>
                            <HeaderSearch />
                        </Hidden>
                        <Hidden mdUp>
                            <IconButton onClick={this.toggleSmSearch} color='inherit'>
                                <SearchIcon className={classes.menuIcon} />
                            </IconButton>
                            {smScreen && <HeaderSearch toggleSmSearch={this.toggleSmSearch} smSearch={smScreen} />}
                        </Hidden>
                        {settings}
                        {avatar}
                    </Toolbar>
                </AppBar>
            </>
        );
    }
}
Header.defaultProps = {
    avatar: <Avatar />,
    settings: <SettingsButton />,
};

Header.propTypes = {
    classes: PropTypes.shape({
        appBar: PropTypes.string,
        menuIcon: PropTypes.string,
        toolbar: PropTypes.string,
    }).isRequired,
    avatar: PropTypes.element,
    settings: PropTypes.element,
    theme: PropTypes.shape({
        custom: PropTypes.shape({
            logo: PropTypes.string,
            title: PropTypes.string,
        }),
    }).isRequired,
};

export default withStyles(styles, { withTheme: true })(Header);
