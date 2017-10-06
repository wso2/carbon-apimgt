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

import React from 'react'
import {Link, withRouter} from "react-router-dom";
import AuthManager from '../../../data/AuthManager.js';
import ConfigManager from '../../../data/ConfigManager.js';
import qs from 'qs'

import AppBar from 'material-ui/AppBar';
import Toolbar from 'material-ui/Toolbar';
import IconButton from 'material-ui/IconButton';
import MenuIcon from 'material-ui-icons/Menu';
import Typography from 'material-ui/Typography';
import Button from 'material-ui/Button';
import Menu, { MenuItem } from 'material-ui/Menu';
import SearchIcon from 'material-ui-icons/Search';
import AppIcon from 'material-ui-icons/Apps';
import PlaylistAddIcon from 'material-ui-icons/PlaylistAdd';
import CloseIcon from 'material-ui-icons/Close';
import TextField from 'material-ui/TextField';
import InfoIcon from 'material-ui-icons/Info';

import InfoLightBulb from 'material-ui-icons/LightbulbOutline';
import List, {
    ListItem,
    ListItemIcon,
    ListItemText,
} from 'material-ui/List';

const helpTips = [
    "By API Name [Default]",
    "By API Provider [ Syntax - provider:xxxx ] or",
    "By API Version [ Syntax - version:xxxx ] or",
    "By Context [ Syntax - context:xxxx ] or",
    "By Description [ Syntax - description:xxxx ] or",
    "By Tags [ Syntax - tags:xxxx ] or",
    "By Sub-Context [ Syntax - subcontext:xxxx ] or",
    "By Documentation Content [ Syntax - doc:xxxx ]"
];

class Header extends React.Component {
    constructor(props){
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
            availableEnv:[],
            value: localStorage.getItem('currentEnv')
        }
        this.handleChange = this.handleChange.bind(this);
    }
    componentDidMount(){
        let envdetails = new ConfigManager();
        envdetails.env_response.then((response) =>{
            let allAvailableEnv = response.data.environments;
            console.log(allAvailableEnv);
            this.setState({availableEnv: allAvailableEnv});

        })

        this.setState({value: localStorage.getItem("currentEnv")});
        console.log(localStorage.getItem("currentEnv"));
    }

    handleClickUserMenu = event => {
        this.setState({ openUserMenu: true, anchorElUserMenu: event.currentTarget });
    };

    handleRequestCloseUserMenu = () => {
        this.setState({ openUserMenu: false });
    };
    handleClickAddMenu = event => {
        this.setState({ openAddMenu: true, anchorElAddMenu: event.currentTarget });
    };

    handleRequestCloseAddMenu = () => {
        this.setState({ openAddMenu: false });
    };
    handleClickMainMenu = event => {
        this.setState({ openMainMenu: true, anchorElMainMenu: event.currentTarget });
    };

    handleRequestCloseMainMenu = () => {
        this.setState({ openMainMenu: false });
    };
    toggleSearch = () => {
        this.setState({searchVisible:!this.state.searchVisible});
    }
    handleClickTips = event => {

        this.setState({ openTips: true, anchorElTips: event.currentTarget });
    };
    handleRequestCloseTips = () => {
        this.setState({ openTips: false });
    };
    handleChange(event) {
        let envalue = event.target.value;
        localStorage.setItem("currentEnv",envalue);
        this.setState({value: event.target.value});
        location.reload();
    };
    componentWillReceiveProps(nextProps){
        if(nextProps.showLeftMenu){
            this.setState({showLeftMenu:nextProps.showLeftMenu});
        }
    }

    render(props) {
        const intialValue = this.state.value;
        const environmentLength = this.state.availableEnv.length;
        let styles = {
            background: "#3f51b5",
            border: 0
        };
        let user = AuthManager.getUser();
        const focusUsernameInputField = input => {
            input && input.focus();
        };
        return (
            <AppBar position="static">
                {this.state.searchVisible ?
                    <Toolbar>

                        <IconButton aria-label="Search" color="contrast">
                            <CloseIcon onClick={this.toggleSearch}/>
                        </IconButton>
                        <TextField
                            id="placeholder"
                            InputProps={{ placeholder: 'Placeholder' }}
                            helperText="Search By Name"
                            fullWidth
                            margin="normal"
                            color = "contrast"
                            inputRef={focusUsernameInputField}
                        />
                        <IconButton aria-label="Search Info" color="contrast">
                            <InfoLightBulb onClick={this.handleClickTips}/>
                        </IconButton>
                        <Menu
                            id="tip-menu"
                            anchorEl={this.state.anchorElTips}
                            open={this.state.openTips}
                            onRequestClose={this.handleRequestCloseTips}
                        >
                            <List dense={true}>
                                {helpTips.map((tip) => {
                                    return <ListItem button key={tip}>
                                        <ListItemIcon><InfoIcon /></ListItemIcon>
                                        <ListItemText  primary={tip} />
                                    </ListItem>
                                })}
                            </List>
                        </Menu>
                    </Toolbar>
                    :
                    <Toolbar>
                        {this.state.showLeftMenu ?
                        <IconButton color="contrast" aria-label="Menu">
                            <MenuIcon color="contrast" onClick={this.props.toggleDrawer}/>
                        </IconButton> : <span></span> }
                        <Typography type="title" color="inherit" style={{flex: 1}}>
                            <Link to="/" style={{textDecoration: 'none'}}>
                                <Button color="primary">
                                    <img className="brand" src="/store_new/public/images/logo.svg" alt="wso2-logo"/>
                                    <span color="contrast" style={{fontSize: "15px", color:"#fff"}}>APIM Publisher</span>
                                </Button>

                            </Link>
                        </Typography>
                        <select value={intialValue} onChange={this.handleChange} style={styles}>
                            {this.state.availableEnv.map(environment => <option
                                value={environment.env}>{environment.env}</option>)}
                        </select>
                        { user ?
                            <div style={{display:"flex"}}>
                                <IconButton aria-label="Search" onClick={this.toggleSearch} color="contrast">
                                    <SearchIcon />
                                </IconButton>
                                {/* API add menu */}
                                <Button aria-owns="simple-menu" aria-haspopup="true" onClick={this.handleClickAddMenu}
                                        color="contrast">
                                    <PlaylistAddIcon />
                                </Button>
                                <Menu
                                    id="simple-menu"
                                    anchorEl={this.state.anchorElAddMenu}
                                    open={this.state.openAddMenu}
                                    onRequestClose={this.handleRequestCloseAddMenu}
                                    style={{alignItems: "center", justifyContent: "center"}}
                                >

                                    <MenuItem onClick={this.handleRequestCloseAddMenu}>
                                        <Link to="/api/create/rest" style={{color: "#000", textDecoration: 'none'}}>Create new API</Link>
                                    </MenuItem>
                                    <MenuItem onClick={this.handleRequestCloseAddMenu}>
                                        <Link to="/api/create/swagger" style={{color: "#000", textDecoration: 'none'}}>Create new API with Swagger</Link>
                                    </MenuItem>
                                </Menu>
                                {/* Main menu */}
                                <Button aria-owns="simple-menu" aria-haspopup="true" onClick={this.handleClickMainMenu}
                                        color="contrast">
                                    <AppIcon />
                                </Button>
                                <Menu
                                    id="simple-menu"
                                    anchorEl={this.state.anchorElMainMenu}
                                    open={this.state.openMainMenu}
                                    onRequestClose={this.handleRequestCloseMainMenu}
                                    style={{alignItems: "center", justifyContent: "center"}}
                                >

                                    <MenuItem onClick={this.handleRequestCloseMainMenu}>
                                        <Link to="/" style={{color: "#000", textDecoration: 'none'}}>List API</Link>
                                    </MenuItem>
                                    <MenuItem onClick={this.handleRequestCloseMainMenu}>
                                        <Link to="/endpoints" style={{color: "#000", textDecoration: 'none'}}>Endpoints</Link>
                                    </MenuItem>
                                </Menu>
                                {/* User menu */}
                                <Button aria-owns="simple-menu" aria-haspopup="true" onClick={this.handleClickUserMenu}
                                        color="contrast">
                                    {user.name}
                                </Button>
                                <Menu
                                    id="simple-menu"
                                    anchorEl={this.state.anchorElUserMenu}
                                    open={this.state.openUserMenu}
                                    onRequestClose={this.handleRequestCloseUserMenu}
                                    style={{alignItems: "center", justifyContent: "center"}}
                                >
                                    <MenuItem onClick={this.handleRequestCloseUserMenu}>Change Password</MenuItem>

                                    <MenuItem onClick={this.handleRequestCloseUserMenu}>
                                        <Link to="/logout" style={{color: "#000", textDecoration: 'none'}}>Logout</Link>
                                    </MenuItem>
                                </Menu>
                            </div>
                            :
                            <div></div> }

                    </Toolbar>
                }
            </AppBar>
        );
    }
}


export default withRouter(Header)