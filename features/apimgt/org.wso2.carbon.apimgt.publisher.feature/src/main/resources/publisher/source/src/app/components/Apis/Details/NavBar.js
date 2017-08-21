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
import { ListItem, ListItemIcon, ListItemText } from 'material-ui/List';
import { Description, DonutLarge, Games,FilterNone,
    LockOutline, InsertDriveFile, Tune, Code, Subscriptions  } from 'material-ui-icons';

class NavBar extends Component {

    static get CONST() {
        return {
            OVERVIEW: "overview",
            LIFECYCLE: "lifecycle",
            ENDPOINTS: "endpoints",
            RESOURCES: "resources",
            PERMISSION: "permission",
            DOCUMENTS: "documents",
            MEDIATION: "mediation",
            SCRIPTING: "scripting",
            SUBSCRIPTIONS: "subscriptions"
        }
    }

    render() {
        /* TODO: This could have been done easily with match object containing api_uuid value , But
         Due to a bug (https://github.com/ReactTraining/react-router/issues/4649) in the latest version(4.1.1),
         it's not working as expected, Hence doing this hack, revert to following with react-router upgrade
         const api_uuid = this.props.match.params.api_uuid; ~tmkb */
        const pathSegments = this.props.location.pathname.split('/');
        // This assume that last segment and segment before it contains detail page action and API UUID
        const [active_tab, api_uuid] = pathSegments.reverse();
        return (
            <div>
                <ListItem style={{width:"250px"}}>
                    <ListItemIcon>
                        <Description />
                    </ListItemIcon>
                    <Link name="overview" to={"/apis/" + api_uuid + "/overview"}><ListItemText primary="overview" /></Link>
                </ListItem>
                <ListItem style={{width:"250px"}}>
                    <ListItemIcon>
                        <DonutLarge />
                    </ListItemIcon>
                    <Link name="lifecycle" to={"/apis/" + api_uuid + "/lifecycle"}><ListItemText primary="lifecycle" /></Link>
                </ListItem>
                <ListItem style={{width:"250px"}}>
                    <ListItemIcon>
                        <Games />
                    </ListItemIcon>
                    <Link name="endpoints" to={"/apis/" + api_uuid + "/endpoints"}><ListItemText primary="endpoints" /></Link>
                </ListItem>
                <ListItem style={{width:"250px"}}>
                    <ListItemIcon>
                        <FilterNone />
                    </ListItemIcon>
                    <Link name="resources" to={"/apis/" + api_uuid + "/resources"}><ListItemText primary="resources" /></Link>
                </ListItem>
                <ListItem style={{width:"250px"}}>
                    <ListItemIcon>
                        <LockOutline />
                    </ListItemIcon>
                    <Link name="permission" to={"/apis/" + api_uuid + "/permission"}><ListItemText primary="permission" /></Link>
                </ListItem>
                <ListItem style={{width:"250px"}}>
                    <ListItemIcon>
                        <InsertDriveFile />
                    </ListItemIcon>
                    <Link name="documents" to={"/apis/" + api_uuid + "/documents"}><ListItemText primary="documents" /></Link>
                </ListItem>
                <ListItem style={{width:"250px"}}>
                    <ListItemIcon>
                        <Tune />
                    </ListItemIcon>
                    <Link name="mediation" to={"/apis/" + api_uuid + "/mediation"}><ListItemText primary="mediation" /></Link>
                </ListItem>
                <ListItem style={{width:"250px"}}>
                    <ListItemIcon>
                        <Code />
                    </ListItemIcon>
                    <Link name="scripting" to={"/apis/" + api_uuid + "/scripting"}><ListItemText primary="scripting" /></Link>
                </ListItem>
                <ListItem style={{width:"250px"}}>
                    <ListItemIcon>
                        <Subscriptions />
                    </ListItemIcon>
                    <Link name="subscriptions" to={"/apis/" + api_uuid + "/subscriptions"}><ListItemText primary="subscriptions" /></Link>
                </ListItem>
            </div>
        )
    }
}

// Using `withRouter` helper from React-Router-Dom to get the current user location to be used with logout action,
// To identify which tab user is currently viewing we need to know their location information
// DOC: https://github.com/ReactTraining/react-router/blob/master/packages/react-router/docs/api/withRouter.md
export default withRouter(NavBar)