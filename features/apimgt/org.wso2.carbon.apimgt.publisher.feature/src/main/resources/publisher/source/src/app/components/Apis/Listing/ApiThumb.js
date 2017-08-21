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
import {Link} from 'react-router-dom'
import {Card, Col, Button, Icon, Popconfirm, message} from 'antd'
import API from '../../../data/api'
import {ScopeValidation, resourcePath, resourceMethod} from '../../../data/ScopeValidation'

class ApiThumb extends React.Component {
    constructor(props) {
        super(props);
        this.state = {active: true, loading: false};
    }

    render() {
        let details_link = "/apis/" + this.props.api.id;
        const {name, version, context} = this.props.api;
        if (!this.state.active) { // Controls the delete state, We set the state to inactive on delete success call
            return null;
        }
        return (
            <Col xs={24} sm={12} md={8} lg={6} xl={4}>
                <Card className="custom-card" bodyStyle={{padding: 0}}>
                    <div className="custom-image">
                        <img alt="example" width="100%" src="/publisher/public/images/api/api-default.png"/>
                    </div>
                    <div className="custom-card">
                        <h3>{name}</h3>
                        <p>{version}</p>
                        <p>{context}</p>
                        <p className="description">{this.props.api.description}</p>
                        <div className="api-action-container">
                            <Link to={details_link}>More... <Icon type="edit"/></Link>
                        </div>
                    </div>
                </Card>
            </Col>
        );
    }
}
export default ApiThumb
