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
import Button from '@material-ui/core/Button';
import Menu from '@material-ui/core/Menu';
import MenuItem from '@material-ui/core/MenuItem';
import { FormattedMessage } from 'react-intl';

/**
 * Renders the Enviorment menu to select ( dev, prod, aq etc..)
 *
 * @class EnvironmentMenu
 * @extends {React.Component}
 */
class EnvironmentMenu extends React.Component {
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
     *
     *
     * @param {*} event
     * @memberof EnvironmentMenu
     */
    handleClickEnvironmentMenu(event) {
        this.setState({ openEnvironmentMenu: true, anchorElEnvironmentMenu: event.currentTarget });
    }

    /**
     *
     *
     * @memberof EnvironmentMenu
     */
    handleRequestCloseEnvironmentMenu() {
        this.setState({ openEnvironmentMenu: false });
        // TODO: [rnk] Temporary Fix: Reload the web page when environment changed
        document.location.reload();
    }

    /**
     *
     *
     * @param {*} event
     * @memberof EnvironmentMenu
     */
    handleEnvironmentChange(event) {
        this.props.handleEnvironmentChange(event);
        this.handleRequestCloseEnvironmentMenu(event);
    }

    /**
     * Renders the UI
     *
     * @returns {JSX}
     * @memberof EnvironmentMenu
     */
    render() {
        // Props list
        const { environments, environmentLabel } = this.props;
        const { anchorElEnvironmentMenu, openEnvironmentMenu } = this.state;
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
                    color='default'
                >
                    <FormattedMessage
                        id='Base.Header.EnvironmentMenu.environment.label'
                        defaultMessage='{environmentLabel}'
                        values={{ environmentLabel }}
                    />
                </Button>

                <Menu
                    id='simple-menu'
                    anchorEl={anchorElEnvironmentMenu}
                    open={openEnvironmentMenu}
                    onRequestClose={this.handleRequestCloseEnvironmentMenu}
                    style={{ alignItems: 'center', justifyContent: 'center' }}
                >
                    {environments.map((environment, index) => (
                        <MenuItem onClick={this.handleEnvironmentChange} key={index} id={index}>
                            {environment.label}
                        </MenuItem>
                    ))}
                </Menu>
            </div>
        );
    }
}

export default EnvironmentMenu;
