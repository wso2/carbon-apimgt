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

import React, {Component} from 'react'
import {Link, withRouter} from 'react-router-dom'
import {ListItem, ListItemIcon, ListItemText} from 'material-ui/List';
import {
    Description, DonutLarge, Games, FilterNone,
    LockOutline, InsertDriveFile, Tune, Code, Subscriptions, ChromeReaderMode, VerifiedUser
} from 'material-ui-icons';
import Divider from 'material-ui/Divider';
import Drawer from 'material-ui/Drawer';
import {withStyles} from 'material-ui/styles';
import List from 'material-ui/List';
import blue from 'material-ui/colors/blue';


const styles = {
    active: {
        backgroundColor: blue[500]
    },
    drawerPaper: {
        position: 'relative',
        height: '100%'
    }
};


class NavBar extends Component {

    constructor(props) {
        super(props);
        this.state = {anchor: 'left', open: true};
    }

    static get TABS() {
        return [
            {name: "overview", icon: <Description />},
            {name: "environment view", icon: <Description />},
            {name: "lifecycle", icon: <DonutLarge />},
            {name: "endpoints", icon: <Games />},
            {name: "resources", icon: <FilterNone />},
            {name: "scopes", icon: <FilterNone />},
            {name: "documents", icon: <InsertDriveFile />},
            {name: "permission", icon: <LockOutline />},
            {name: "mediation", icon: <Tune />},
            {name: "scripting", icon: <Code />},
            {name: "subscriptions", icon: <Subscriptions />},
            {name: "security", icon: <VerifiedUser />},
        ]
    }

    render() {
        /* TODO: This could have been done easily with match object containing api_uuid value , But
         Due to a bug (https://github.com/ReactTraining/react-router/issues/4649) in the latest version(4.1.1),
         it's not working as expected, Hence doing this hack, revert to following with react-router upgrade
         const api_uuid = this.props.match.params.api_uuid; ~tmkb */
        const pathSegments = this.props.location.pathname.split('/');
        // This assume that last segment and segment before it contains detail page action and API UUID
        const [active_tab, api_uuid] = pathSegments.reverse();
        const {classes} = this.props;
        const {anchor, open} = this.state;

        function showActiveTab(tab) {
            return (tab === active_tab) ? {root: classes.active} : {};
        }

        return (
            // <Drawer type="persistent" classes={{paper: classes.drawerPaper,}} anchor={anchor} open={open}>
                <List className={classes.list}>
                    <ListItem>
                        <ListItemIcon>
                            <ChromeReaderMode />
                        </ListItemIcon>
                        <Link name="listing" className="api-details-title" to={"/"}>API Details</Link>
                    </ListItem>
                    <Divider />
                    {NavBar.TABS.map(tab =>
                        (<ListItem key={tab.name} classes={showActiveTab(tab.name)}>
                            <ListItemIcon>
                                {tab.icon}
                            </ListItemIcon>
                            <Link name={tab.name} to={"/apis/" + api_uuid + "/" + tab.name}>
                                <ListItemText primary={tab.name}/></Link>
                        </ListItem>)
                    )}
                </List>
            // </Drawer>
        )
    }
}

// Using `withRouter` helper from React-Router-Dom to get the current user location to be used with logout action,
// To identify which tab user is currently viewing we need to know their location information
// DOC: https://github.com/ReactTraining/react-router/blob/master/packages/react-router/docs/api/withRouter.md
export default withStyles(styles)(withRouter(NavBar));