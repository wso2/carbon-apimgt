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
import {Row, Col} from 'antd';


class Permission extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            api: props.api
        }
    }

    render() {

        return (
            <div>
                <div>
                    <div className="wrapper wrapper-content">
                        <h2> API Name : {this.state.api.name} </h2>

                        <div className="divTable">
                            <div className="divTableBody">
                                <div className="gutter-example">
                                    <Row gutter={16}>
                                        <Col className="gutter-row" span={6}>
                                            <div className="gutter-box">Group Name</div>
                                        </Col>
                                        <Col className="gutter-row" span={6}>
                                            <div className="gutter-box">Read</div>
                                        </Col>
                                        <Col className="gutter-row" span={6}>
                                            <div className="gutter-box">Update</div>
                                        </Col>
                                        <Col className="gutter-row" span={6}>
                                            <div className="gutter-box">Delete</div>
                                        </Col>
                                    </Row>
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