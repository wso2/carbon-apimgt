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

class Permission extends React.Component{
    constructor(props){
        super(props);
        this.state = {
            api: props.api
        }
        console.log(this.state);
    }
    render(){
        return (
            <div>
                <div>
                    <div className="wrapper wrapper-content">
                        <h2> API Name : {this.state.api.name} </h2>
                        <div className="divTable">
                            <div className="divTableBody">
                                <div className="divTableRow">
                                    <div className="divTableCell">Group Name</div>
                                    <div className="divTableCell">Read</div>
                                    <div className="divTableCell">Update</div>
                                    <div className="divTableCell">Delete</div>
                                </div>
                                <div className="divTableRow">
                                    <div className="divTableCell">&nbsp;</div>
                                    <div className="divTableCell">&nbsp;</div>
                                    <div className="divTableCell">&nbsp;</div>
                                    <div className="divTableCell">&nbsp;</div>
                                </div>
                                <div className="divTableRow">
                                    <div className="divTableCell">&nbsp;</div>
                                    <div className="divTableCell">&nbsp;</div>
                                    <div className="divTableCell">&nbsp;</div>
                                    <div className="divTableCell">&nbsp;</div>
                                </div>

                            </div>
                        </div>
                    </div>
                </div>

            </div>
        )
    }
}

export default Permission