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
import Button from 'material-ui/Button';
import Menu, { MenuItem } from 'material-ui/Menu';
import { Link } from 'react-router-dom';
import PropTypes from 'prop-types';

/**
 * Environment change dropdown menu used in Base Top navigation bar
 * @class EnvironmentMenu
 * @extends {React.Component}
 */
class EnvironmentMenu extends React.Component {
    /**
     * Creates an instance of EnvironmentMenu.
     * @param {any} props @inheritDoc
     * @memberof EnvironmentMenu
     */
    constructor(props) {
        super(props);
        this.state = {
            openEnvironmentMenu: false,
        };

        this.handleClickEnvironmentMenu = this.handleClickEnvironmentMenu.bind(this);
        this.handleRequestCloseEnvironmentMenu = this.handleRequestCloseEnvironmentMenu.bind(this);
        this.handleEnvironmentChange = this.handleEnvironmentChange.bind(this);
    }

    /**
     * Show the environments dropdown when click on menu
     * @param {React.SyntheticEvent} event User click event
     * @memberof EnvironmentMenu
     */
    handleClickEnvironmentMenu(event) {
        this.setState({ openEnvironmentMenu: true, anchorElEnvironmentMenu: event.currentTarget });
    }

    /**
     * Set the environment menu close state
     * @memberof EnvironmentMenu
     */
    handleRequestCloseEnvironmentMenu() {
        this.setState({ openEnvironmentMenu: false });
    }

    /**
     * Handle user selection of an environment, uplift the state through method passed through props from Base component
     * @param {React.SyntheticEvent} event user click event
     * @memberof EnvironmentMenu
     */
    handleEnvironmentChange(event) {
        this.props.handleEnvironmentChange(event);
        this.handleRequestCloseEnvironmentMenu(event);
    }

    /**
     * @inheritDoc
     * @returns {React.Component} Environment menu
     * @memberof EnvironmentMenu
     */
    render() {
        // Props list
        const { environments, environmentLabel } = this.props;
        const showEnvironments = environments && environments.length > 1;

        if (!showEnvironments) {
            return <div />;
        }

        return (
            <div style={{ display: 'flex' }}>
                <Button
                    aria-owns='simple-menu'
                    aria-haspopup='true'
                    onClick={this.handleClickEnvironmentMenu}
                    color='contrast'
                >
                    {environmentLabel}
                </Button>

                <Menu
                    id='simple-menu'
                    anchorEl={this.state.anchorElEnvironmentMenu}
                    open={this.state.openEnvironmentMenu}
                    onClose={this.handleRequestCloseEnvironmentMenu}
                    style={{ alignItems: 'center', justifyContent: 'center' }}
                >
                    {environments.map((environment, index) => (
                        <Link to='#' key={environment.host}>
                            <MenuItem onClick={this.handleEnvironmentChange} key={environment.host} id={index}>
                                {environment.label}
                            </MenuItem>
                        </Link>
                    ))}
                </Menu>
            </div>
        );
    }
}

EnvironmentMenu.propTypes = {
    handleEnvironmentChange: PropTypes.func.isRequired,
    environments: PropTypes.arrayOf(PropTypes.shape({
        host: PropTypes.string,
        label: PropTypes.string,
        loginTokenPath: PropTypes.string,
    })).isRequired,
    environmentLabel: PropTypes.string.isRequired,
};
export default EnvironmentMenu;
