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
import {Route, Switch} from 'react-router-dom'

import Listing from './Listing/Listing'
import Details from './Details/index'
import {PageNotFound} from '../Base/Errors'
import AppBar from 'material-ui/AppBar';
import Toolbar from 'material-ui/Toolbar';
import Button from 'material-ui/Button';
import Menu, { MenuItem } from 'material-ui/Menu';
import Input from 'material-ui/Input/Input';
import InfoIcon from 'material-ui-icons/Info';
import List, {
    ListItem,
    ListItemIcon,
    ListItemText,
} from 'material-ui/List';
import IconButton from 'material-ui/IconButton';


class Apis extends React.Component {
    state = {
        anchorEl: undefined,
        open: false,
        anchorElTips: undefined,
        openTips: false,
    };

    handleClick = event => {
        this.setState({ open: true, anchorEl: event.currentTarget });
    };

    handleRequestClose = () => {
        this.setState({ open: false });
    };
    handleClickTips = event => {
        this.setState({ openTips: true, anchorElTips: event.currentTarget });
    };

    handleRequestCloseTips = () => {
        this.setState({ openTips: false });
    };
    render() {
        return (
            <div>
                <AppBar position="static" color="default">
                    <Toolbar>
                        <div>
                            <Button aria-owns="simple-menu" aria-haspopup="true" onClick={this.handleClick}>
                                All
                            </Button>
                            <Menu
                                id="simple-menu"
                                anchorEl={this.state.anchorEl}
                                open={this.state.open}
                                onRequestClose={this.handleRequestClose}
                            >
                                <MenuItem onClick={this.handleRequestClose}>All</MenuItem>
                                <MenuItem onClick={this.handleRequestClose}>Production</MenuItem>
                                <MenuItem onClick={this.handleRequestClose}>Prototyped</MenuItem>
                            </Menu>
                            <Input placeholder="Search"  />
                            <IconButton style={{margin:"0 0 -30 0"}} aria-label="Info"
                                        aria-owns="tip-menu" aria-haspopup="true" onClick={this.handleClickTips}>
                                <InfoIcon />
                            </IconButton>
                            <Menu
                                id="tip-menu"
                                anchorEl={this.state.anchorElTips}
                                open={this.state.openTips}
                                onRequestClose={this.handleRequestCloseTips}
                            >
                                <div>
                                    <List dense={true}>
                                        <ListItem button>
                                            <ListItemIcon><InfoIcon /></ListItemIcon>
                                            <ListItemText  primary="By API Name [Default]" />
                                        </ListItem>
                                        <ListItem button>
                                            <ListItemIcon><InfoIcon /></ListItemIcon>
                                            <ListItemText  primary="By API Provider [ Syntax - provider:xxxx ] or" />
                                        </ListItem>
                                        <ListItem button>
                                            <ListItemIcon><InfoIcon /></ListItemIcon>
                                            <ListItemText  primary="By API Version [ Syntax - version:xxxx ] or" />
                                        </ListItem>
                                        <ListItem button>
                                            <ListItemIcon><InfoIcon /></ListItemIcon>
                                            <ListItemText  primary="By Context [ Syntax - context:xxxx ] or" />
                                        </ListItem>
                                        <ListItem button>
                                            <ListItemIcon><InfoIcon /></ListItemIcon>
                                            <ListItemText  primary="By Description [ Syntax - description:xxxx ] or" />
                                        </ListItem>
                                        <ListItem button>
                                            <ListItemIcon><InfoIcon /></ListItemIcon>
                                            <ListItemText  primary="By Tags [ Syntax - tags:xxxx ] or" />
                                        </ListItem>
                                        <ListItem button>
                                            <ListItemIcon><InfoIcon /></ListItemIcon>
                                            <ListItemText  primary="By Sub-Context [ Syntax - subcontext:xxxx ] or" />
                                        </ListItem>
                                        <ListItem button>
                                            <ListItemIcon><InfoIcon /></ListItemIcon>
                                            <ListItemText  primary="By Documentation Content [ Syntax - doc:xxxx ]" />
                                        </ListItem>
                                    </List>
                                </div>
                            </Menu>

                        </div>
                    </Toolbar>
                </AppBar>
                <Switch>
                    <Route exact path={"/apis"} component={Listing}/>
                    <Route path={"/apis/:api_uuid/"} render={ props => (
                        <Details {...props} setLeftMenu={this.props.setLeftMenu}/>)}/>
                    <Route component={PageNotFound}/>
                </Switch>
            </div>
        );
    }
}

export default Apis;
