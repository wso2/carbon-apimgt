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

export default class LifeCycleHistory extends Component {
    render() {
        return (
            <div>
                <div className="page-header">
                    <h4 className="lead">Lifecycle History</h4>
                </div>
                ##each @params.lcHistory
                ##if this.previousState
                ##else
                ##if
                ##each
                <table className="lifeCycleTable" id="lifeCycleTable">
                    <thead>
                    </thead>
                    <tbody>
                    <tr>
                        <td><i className="glyphicon glyphicon-info-sign" title="date"/> <b><span
                            className="dateFull">##this.updatedTime</span></b>
                        </td>
                        <td>&nbsp;&nbsp;&nbsp; <i className="glyphicon glyphicon-user"
                                                  title="user"/>&nbsp;<a
                            href="/publisher/apis?query=provider:##this.user" title="user">##this.user</a>
                            changed the API status from ##this.previousState
                            to ##i18n this.postState
                        </td>
                    </tr>
                    <tr>
                        <td><i className="glyphicon glyphicon-info-sign" title="date"/> <b><span
                            className="dateFull">##this.updatedTime</span></b>
                        </td>
                        <td>&nbsp;&nbsp;&nbsp; <i className="glyphicon glyphicon-user"
                                                  title="user"/>&nbsp;<a
                            href="/publisher/apis?query=provider:##this.user" title="user">##this.user</a>
                            created the API.
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
        );
    }
}