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

import React from "react";
import Button from 'material-ui/Button';
import Menu, {MenuItem} from 'material-ui/Menu';

class EnvironmentMenu extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            openEnvironmentMenu: false
        };

        this.handleClickEnvironmentMenu = this.handleClickEnvironmentMenu.bind(this);
        this.handleRequestCloseEnvironmentMenu = this.handleRequestCloseEnvironmentMenu.bind(this);
        this.handleEnvironmentChange = this.handleEnvironmentChange.bind(this);
    }

    handleClickEnvironmentMenu(event) {
        this.setState({openEnvironmentMenu: true, anchorElEnvironmentMenu: event.currentTarget});
    };

    handleRequestCloseEnvironmentMenu() {
        this.setState({openEnvironmentMenu: false});
        //TODO: [rnk] Temporary Fix: Reload the web page when environment changed
        document.location.reload();
    };

    handleEnvironmentChange(event) {
        this.props.handleEnvironmentChange(event);
        this.handleRequestCloseEnvironmentMenu(event);
    }

    render() {
        //Props list
        const environments = this.props.environments;
        const environmentLabel = this.props.environmentLabel;

        let showEnvironments = environments && environments.length > 1;

        if (!showEnvironments) {
            return <div/>;
        }

        return (
            <div style={{display: "flex"}}>
                <Button aria-owns="simple-menu" aria-haspopup="true"
                        onClick={this.handleClickEnvironmentMenu}
                        color="default">
                    {environmentLabel}
                </Button>

                <Menu
                    id="simple-menu"
                    anchorEl={this.state.anchorElEnvironmentMenu}
                    open={this.state.openEnvironmentMenu}
                    onRequestClose={this.handleRequestCloseEnvironmentMenu}
                    style={{alignItems: "center", justifyContent: "center"}}
                >
                    {environments.map((environment, index) =>
                        <MenuItem onClick={this.handleEnvironmentChange} key={index}
                                  id={index}>{environment.label}</MenuItem>
                    )}
                </Menu>
            </div>
        );
    }
}

export default EnvironmentMenu;