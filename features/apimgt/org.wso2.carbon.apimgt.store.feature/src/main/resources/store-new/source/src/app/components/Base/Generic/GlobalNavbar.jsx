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
import React, { Component } from 'react';
import { Link } from 'react-router-dom';
import {
    ListItemIcon, Drawer, List, withStyles, ListItem, ListItemText,
} from '@material-ui/core';
import CustomIcon from '../../Shared/CustomIcon';
/**
 *
 *
 * @param {*} theme
 */
const styles = theme => ({
    list: {
        width: theme.custom.drawerWidth,
    },
    drawerStyles: {
        top: theme.mixins.toolbar['@media (min-width:600px)'].minHeight,
    },
    listText: {
        color: theme.palette.getContrastText(theme.palette.background.drawer),
    },
});
/**
 *
 *
 * @class GlobalNavBar
 * @extends {Component}
 */
class GlobalNavBar extends Component {
    render() {
        const {
            open, toggleGlobalNavBar, classes, theme,
        } = this.props;
        // TODO: Refer to fix: https://github.com/mui-org/material-ui/issues/10076#issuecomment-361232810 ~tmkb
        const commonStyle = {
            style: { top: 64 },
        };
        const paperStyles = {
            style: {
                backgroundColor: theme.palette.background.drawer,
                top: 64,
            },
        };
        const strokeColor = theme.palette.getContrastText(theme.palette.background.leftMenu);
        return (
            <div>
                <Drawer className={classes.drawerStyles} PaperProps={paperStyles} SlideProps={commonStyle} ModalProps={commonStyle} BackdropProps={commonStyle} open={open} onClose={toggleGlobalNavBar}>
                    <div tabIndex={0} role='button' onClick={toggleGlobalNavBar} onKeyDown={toggleGlobalNavBar}>
                        <div className={classes.list}>
                            <List>
                                <Link to='/apis'>
                                    <ListItem button>
                                        <ListItemIcon>
                                            <CustomIcon width={32} height={32} icon='api' className={classes.listText} strokeColor={strokeColor} />
                                        </ListItemIcon>
                                        <ListItemText classes={{ primary: classes.listText }} primary='APIs' />
                                    </ListItem>
                                </Link>
                                <Link to='/applications'>
                                    <ListItem button>
                                        <ListItemIcon>
                                            <CustomIcon width={32} height={32} icon='applications' className={classes.listText} strokeColor={strokeColor} />
                                        </ListItemIcon>
                                        <ListItemText classes={{ primary: classes.listText }} primary='Applications' />
                                    </ListItem>
                                </Link>
                            </List>
                        </div>
                    </div>
                </Drawer>
            </div>
        );
    }
}

export default withStyles(styles, { withTheme: true })(GlobalNavBar);
