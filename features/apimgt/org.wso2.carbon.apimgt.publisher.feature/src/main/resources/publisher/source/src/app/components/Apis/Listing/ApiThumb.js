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
import { Link } from 'react-router-dom'
import { Card, Col,Button } from 'antd';
const ButtonGroup = Button.Group;

class ApiThumb extends React.Component {

    render() {
        let details_link = "/apis/" + this.props.api.id;

        return(
            <Col xs={12} sm={8} md={6} lg={4} xl={3}>
                <Card className="custom-card" bodyStyle={{ padding: 0 }}>
                    <div className="custom-image">
                        <img alt="example" width="100%" src="/publisher/public/images/api/api-default.png" />
                    </div>
                    <div className="custom-card">
                        <h3>{this.props.api.name}</h3>
                        <p>{this.props.api.version}</p>
                        <p>{this.props.api.context}</p>
                        <p className="description">{this.props.api.description}</p>
                        <div style={{textAlign:'center'}}>
                            <ButtonGroup>
                                <Link to={details_link}><Button type="primary" icon="edit">More...</Button></Link>
                                <Button type="default" icon="delete" />
                            </ButtonGroup>
                        </div>
                    </div>
                </Card>
            </Col>
        );
    }
}
export default ApiThumb
