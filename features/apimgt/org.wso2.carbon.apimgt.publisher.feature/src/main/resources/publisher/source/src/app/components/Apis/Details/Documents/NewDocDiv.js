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
import NewDocInfoDiv from './NewDocInfoDiv';
import NewDocSourceDiv from './NewDocSourceDiv';
import {Button, Row, Col} from 'antd';

class NewDocDiv extends Component {
    constructor(props) {
        super(props);
    }

    render() {
        return (
            <div>
                <div>
                    <Row type="flex" gutter={80} style={{paddingTop: 10}}>
                        <Col span={6}>
                            <NewDocInfoDiv
                                onDocInfoChange={this.props.onDocInfoChange}
                                docName={this.props.docName}
                                docSummary={this.props.docSummary}
                                docSourceURL={this.props.docSourceURL}
                                docFilePath={this.props.docFilePath}
                                selectedSourceType={this.props.docSourceType}
                                updatingDoc={this.props.updatingDoc}
                                addingNewDoc={this.props.addingNewDoc}
                            />
                        </Col>
                        <Col span={6}>
                            <NewDocSourceDiv
                                onDocInfoChange={this.props.onDocInfoChange}
                                selectedSourceType={this.props.selectedSourceType}
                                docFilePath={this.props.docFilePath}
                                docFile={this.props.docFile}
                                docSourceURL={this.props.docSourceURL}
                                updatingDoc={this.props.updatingDoc}
                            />
                        </Col>
                    </Row>
                </div>
                <div name="action-buttons" style={{paddingBottom: 20}}>
                    <Row gutter={1}>
                        <Col span={1}>
                            {
                                this.props.updatingDoc ? (
                                    <Button type="default" size="small"
                                            onClick={this.props.onSubmitUpdateDoc}>Update</Button>
                                ) : (
                                    <Button type="default" size="small"
                                            onClick={this.props.onSubmitAddNewDoc}>Add</Button>
                                )
                            }
                        </Col>
                        <Col span={1}>
                            <Button type="default" size="small"
                                    onClick={this.props.onCancelAddOrEditNewDoc}>Cancel</Button>
                        </Col>
                    </Row>
                </div>
            </div>
        );
    }
}

export default NewDocDiv;