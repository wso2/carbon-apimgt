/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import { Link, withRouter } from 'react-router-dom';
import AppBar from 'material-ui/AppBar';
import Toolbar from 'material-ui/Toolbar';
import IconButton from 'material-ui/IconButton';
import MenuIcon from '@material-ui/icons/Menu';
import Typography from 'material-ui/Typography';
import Button from 'material-ui/Button';
import Menu, { MenuItem } from 'material-ui/Menu';
import SearchIcon from '@material-ui/icons/Search';
import AppIcon from '@material-ui/icons/Apps';
import PlaylistAddIcon from '@material-ui/icons/PlaylistAdd';
import CloseIcon from '@material-ui/icons/Close';
import TextField from 'material-ui/TextField';
import InfoIcon from '@material-ui/icons/Info';
import InfoLightBulb from '@material-ui/icons/LightbulbOutline';
import List, { ListItem, ListItemIcon, ListItemText } from 'material-ui/List';
import PropTypes from 'prop-types';
import AuthManager from '../../../data/AuthManager.js';
import { resourceMethod, resourcePath, ScopeValidation } from '../../../data/ScopeValidation';
import ConfigManager from '../../../data/ConfigManager';
import Utils from '../../../data/Utils';
import EnvironmentMenu from './EnvironmentMenu';

const helpTips = [
    'By API Name [Default]',
    'By API Provider [ Syntax - provider:xxxx ] or',
    'By API Version [ Syntax - version:xxxx ] or',
    'By Context [ Syntax - context:xxxx ] or',
    'By Description [ Syntax - description:xxxx ] or',
    'By Tags [ Syntax - tags:xxxx ] or',
    'By Sub-Context [ Syntax - subcontext:xxxx ] or',
    'By Documentation Content [ Syntax - doc:xxxx ]',
];

/**
 * Common Header section for whole app
 * @class Header
 * @extends {React.Component}
 */
class Header extends React.Component {
    /**
     * Creates an instance of Header.
     * @param {any} props @inheritDoc
     * @memberof Header
     */
    constructor(props) {
        super(props);
        this.state = {
            anchorElUserMenu: undefined,
            anchorElAddMenu: undefined,
            anchorElMainMenu: undefined,
            anchorElTips: undefined,
            openUserMenu: false,
            openAddMenu: false,
            openMainMenu: false,
            searchVisible: false,
            openTips: false,
            showLeftMenu: this.props.showLeftMenu,
            environments: [],
        };
    }

    /**
     * @inheritDoc
     * @memberof Header
     */
    componentDidMount() {
        // Get Environments
        ConfigManager.getConfigs()
            .environments.then((response) => {
                this.setState({
                    environments: response.data.environments,
                });
            })
            .catch((error) => {
                console.error('Error while receiving environment configurations : ', error);
            });
    }

    /**
     * @inheritDoc
     * @memberof Header
     */
    componentWillReceiveProps(nextProps) {
        if (nextProps.showLeftMenu) {
            this.setState({ showLeftMenu: nextProps.showLeftMenu });
        }
    }

    handleClickUserMenu = (event) => {
        this.setState({ openUserMenu: true, anchorElUserMenu: event.currentTarget });
    };

    handleRequestCloseUserMenu = () => {
        this.setState({ openUserMenu: false });
    };

    handleEnvironmentChange = (event) => {
        // TODO: [rnk] Optimize Rendering.
        const environmentId = parseInt(event.target.id, 10);
        Utils.setEnvironment(this.state.environments[environmentId]);
    };

    handleClickAddMenu = (event) => {
        this.setState({ openAddMenu: true, anchorElAddMenu: event.currentTarget });
    };

    handleRequestCloseAddMenu = () => {
        this.setState({ openAddMenu: false });
    };
    handleClickMainMenu = (event) => {
        this.setState({ openMainMenu: true, anchorElMainMenu: event.currentTarget });
    };

    handleRequestCloseMainMenu = () => {
        this.setState({ openMainMenu: false });
    };
    toggleSearch = () => {
        this.setState({ searchVisible: !this.state.searchVisible });
    };
    handleClickTips = (event) => {
        this.setState({ openTips: true, anchorElTips: event.currentTarget });
    };
    handleRequestCloseTips = () => {
        this.setState({ openTips: false });
    };

    /**
     * @inheritDoc
     * @returns {React.Component} Return header component
     * @memberof Header
     */
    render() {
        const user = AuthManager.getUser();
        const focusUsernameInputField = (input) => {
            if (input) {
                input.focus();
            }
        };
        return (
            <AppBar position='static'>
                {this.state.searchVisible ? (
                    <Toolbar>
                        <IconButton aria-label='Search' color='default'>
                            <CloseIcon onClick={this.toggleSearch} />
                        </IconButton>
                        <TextField
                            id='placeholder'
                            InputProps={{ placeholder: 'Placeholder' }}
                            helperText='Search By Name'
                            fullWidth
                            margin='normal'
                            color='contrast'
                            inputRef={focusUsernameInputField}
                        />
                        <IconButton aria-label='Search Info' color='default'>
                            <InfoLightBulb onClick={this.handleClickTips} />
                        </IconButton>
                        <Menu
                            id='tip-menu'
                            anchorEl={this.state.anchorElTips}
                            open={this.state.openTips}
                            onClose={this.handleRequestCloseTips}
                        >
                            <List dense>
                                {helpTips.map((tip) => {
                                    return (
                                        <ListItem button key={tip}>
                                            <ListItemIcon>
                                                <InfoIcon />
                                            </ListItemIcon>
                                            <ListItemText primary={tip} />
                                        </ListItem>
                                    );
                                })}
                            </List>
                        </Menu>
                    </Toolbar>
                ) : (
                    <Toolbar>
                        {this.state.showLeftMenu ? (
                            <IconButton color='default' aria-label='Menu'>
                                <MenuIcon color='contrast' onClick={this.props.toggleDrawer} />
                            </IconButton>
                        ) : (
                            <span />
                        )}
                        <Typography type='title' color='inherit' style={{ flex: 1 }}>
                            <Link to='/' style={{ textDecoration: 'none' }}>
                                <Button color='primary'>
                                    <img
                                        className='brand'
                                        src='/publisher/public/app/images/logo.svg'
                                        alt='wso2-logo'
                                    />
                                    <span color='contrast' style={{ fontSize: '15px', color: '#fff' }}>
                                        APIM Publisher
                                    </span>
                                </Button>
                            </Link>
                        </Typography>
                        {user ? (
                            <div style={{ display: 'flex' }}>
                                <IconButton aria-label='Search' onClick={this.toggleSearch} color='default'>
                                    <SearchIcon />
                                </IconButton>
                                <ScopeValidation resourcePath={resourcePath.APIS} resourceMethod={resourceMethod.POST}>
                                    <Button
                                        aria-owns='simple-menu'
                                        aria-haspopup='true'
                                        onClick={this.handleClickAddMenu}
                                        color='contrast'
                                    >
                                        <PlaylistAddIcon />
                                    </Button>
                                </ScopeValidation>
                                {/* API add menu */}
                                {/* enable "create API" menu depending on user scopes */}
                                <Menu
                                    id='simple-menu'
                                    anchorEl={this.state.anchorElAddMenu}
                                    open={this.state.openAddMenu}
                                    onClose={this.handleRequestCloseAddMenu}
                                    style={{ alignItems: 'center', justifyContent: 'center' }}
                                >
                                    <Link to='/api/create/home'>
                                        <MenuItem onClick={this.handleRequestCloseAddMenu}>Create new API</MenuItem>
                                    </Link>
                                    <Link to='/api/create/swagger'>
                                        <MenuItem onClick={this.handleRequestCloseAddMenu}>
                                            Create new API with Swagger
                                        </MenuItem>
                                    </Link>
                                </Menu>
                                {/* Main menu */}
                                <Button
                                    aria-owns='simple-menu'
                                    aria-haspopup='true'
                                    onClick={this.handleClickMainMenu}
                                    color='contrast'
                                >
                                    <AppIcon />
                                </Button>
                                <Menu
                                    id='simple-menu'
                                    anchorEl={this.state.anchorElMainMenu}
                                    open={this.state.openMainMenu}
                                    onClose={this.handleRequestCloseMainMenu}
                                    style={{ alignItems: 'center', justifyContent: 'center' }}
                                >
                                    <Link to='/'>
                                        <MenuItem onClick={this.handleRequestCloseMainMenu}>List API</MenuItem>
                                    </Link>
                                    <Link to='/endpoints'>
                                        <MenuItem onClick={this.handleRequestCloseMainMenu}>Endpoints</MenuItem>
                                    </Link>
                                </Menu>
                                {/* Environment menu */}
                                <EnvironmentMenu
                                    environments={this.state.environments}
                                    environmentLabel={Utils.getCurrentEnvironment().label}
                                    handleEnvironmentChange={this.handleEnvironmentChange}
                                />
                                {/* User menu */}
                                <Button
                                    aria-owns='simple-menu'
                                    aria-haspopup='true'
                                    onClick={this.handleClickUserMenu}
                                    color='contrast'
                                >
                                    {user.name}
                                </Button>
                                <Menu
                                    id='simple-menu'
                                    anchorEl={this.state.anchorElUserMenu}
                                    open={this.state.openUserMenu}
                                    onClose={this.handleRequestCloseUserMenu}
                                    style={{ alignItems: 'center', justifyContent: 'center' }}
                                >
                                    <MenuItem onClick={this.handleRequestCloseUserMenu}>Change Password</MenuItem>

                                    <Link to='/logout'>
                                        <MenuItem onClick={this.handleRequestCloseUserMenu}>Logout</MenuItem>
                                    </Link>
                                </Menu>
                            </div>
                        ) : (
                            <div />
                        )}
                    </Toolbar>
                )}
            </AppBar>
        );
    }
}

Header.propTypes = {
    showLeftMenu: PropTypes.bool.isRequired,
    toggleDrawer: PropTypes.func.isRequired,
};
export default withRouter(Header);
