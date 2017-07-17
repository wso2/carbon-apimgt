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
import {Col, Input, Row} from 'antd';

class NewDocInfoDiv extends Component {
    constructor(props) {
        super(props);
        this.handleInputChange = this.handleInputChange.bind(this);
    }

    handleInputChange(e) {
        this.props.onDocInfoChange(e);
    }

    render() {
        return (
            <div>
                <Row gutter={45} type="flex" style={{paddingBottom: 5, paddingLeft: 10}}
                     justify="space-around">
                    <Col>
                        <h4>Name*</h4>
                    </Col>
                    <Col span={30}>
                        {this.props.addingNewDoc ? (
                            <Input type="text" name="docName" onChange={this.handleInputChange}/>
                        ) : (
                            <Input type="text" name="docName" value={this.props.docName}
                                   readOnly/>
                        )
                        }
                    </Col>
                </Row>
                <Row gutter={45} type="flex" style={{paddingBottom: 20, paddingLeft: 10}}
                     justify="space-around">
                    <Col>
                        <h4>Summary</h4>
                    </Col>
                    <Col span={30}>
                        {this.props.addingNewDoc ? (
                            <Input type="textarea" cols={20} rows={4} name="docSummary"
                                   onChange={this.handleInputChange}></Input>
                        ) : ( <Input type="textarea" cols={20} rows={4} name="docSummary"
                                     onChange={this.handleInputChange}
                                     value={this.props.docSummary}></Input>)
                        }
                    </Col>
                </Row>
            </div>
        )
    }
}

export default NewDocInfoDiv;