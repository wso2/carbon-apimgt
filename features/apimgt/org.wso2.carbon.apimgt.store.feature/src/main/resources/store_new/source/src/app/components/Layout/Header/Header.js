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
import qs from 'qs'

import AppBar from 'material-ui/AppBar';
import Toolbar from 'material-ui/Toolbar';
import IconButton from 'material-ui/IconButton';
import MenuIcon from 'material-ui-icons/Menu';
import Typography from 'material-ui/Typography';
import Button from 'material-ui/Button';
import Menu, { MenuItem } from 'material-ui/Menu';


class Header extends React.Component {
    constructor(props){
        super(props);
        this.state = {
            anchorEl: undefined,
            open: false,
        }
    }
    handleClick = event => {
        this.setState({ open: true, anchorEl: event.currentTarget });
    };

    handleRequestClose = () => {
        this.setState({ open: false });
    };
    leftMenuToggle = () => {

    }
    render(props) {
        let params = {};

        return (
            <AppBar position="static">
                <Toolbar>
                    <IconButton color="contrast" aria-label="Menu">
                        <MenuIcon onClick={this.leftMenuToggle} />
                    </IconButton>
                    <Typography type="title" color="inherit" style={{flex:1}}>
                        <Link to="/" style={{ textDecoration: 'none' }}>
                            <Button color="primary">
                                <img className="brand" src="/store_new/public/images/logo.svg" alt="wso2-logo"/>
                                <span style={{fontSize:"20px"}}>APIM Store</span>
                            </Button>

                        </Link>
                    </Typography>
                    { AuthManager.getUser() ?
                        <div>
                            <Button aria-owns="simple-menu" aria-haspopup="true" onClick={this.handleClick}>
                                Open Menu
                            </Button>
                            <Menu
                                id="simple-menu"
                                anchorEl={this.state.anchorEl}
                                open={this.state.open}
                                onRequestClose={this.handleRequestClose}
                            >
                                <MenuItem onClick={this.handleRequestClose}>Change Password</MenuItem>

                                <MenuItem onClick={this.handleRequestClose}>
                                    <Link to="/logout" style={{color:"#000",textDecoration: 'none'}}>Logout</Link>
                                </MenuItem>
                            </Menu>
                        </div>
                        :
                        <div>
                            <Button color="contrast">Sign Up</Button>
                            <Button color="contrast">Sign In</Button>
                        </div> }

                </Toolbar>
            </AppBar>
        );
    }
}


export default withRouter(Header)