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
import {Tabs} from 'antd'
const TabPane = Tabs.TabPane;

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
            <Tabs defaultActiveKey={active_tab} tabPosition={'left'} style={{height: '100vh'}}>
                {Object.entries(NavBar.CONST).map(
                    ([key, val]) => {
                        return (
                            <TabPane
                                tab={ <Link name={val} to={"/apis/" + api_uuid + "/" + val}>{val}</Link> } key={val}/>
                        );
                    }
                )}
            </Tabs>
        )
    }
}

// Using `withRouter` helper from React-Router-Dom to get the current user location to be used with logout action,
// To identify which tab user is currently viewing we need to know their location information
// DOC: https://github.com/ReactTraining/react-router/blob/master/packages/react-router/docs/api/withRouter.md
export default withRouter(NavBar)