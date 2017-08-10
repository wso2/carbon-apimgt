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
import Api from '../../../../data/api'
import LifeCycleUpdate from './LifeCycleUpdate'
import Loading from "../../../Base/Loading/Loading"
import LifeCycleHistory from "./LifeCycleHistory"
import Policies from "./Policies"
import {Card, Col, Row} from 'antd'

class LifeCycle extends Component {
    constructor(props) {
        super(props);
        this.api = new Api();
        this.state = {};
        this.api_uuid = props.match.params.api_uuid;
        this.updateData = this.updateData.bind(this);
    }

    componentWillMount() {
        this.updateData();
    }

    updateData() {
        let promised_api = this.api.get(this.api_uuid);
        let promised_tiers = this.api.policies('api');
        let promised_lcState = this.api.getLcState(this.api_uuid);
        let promised_lcHistory = this.api.getLcHistory(this.api_uuid);
        let promised_labels = this.api.labels();
        Promise.all([promised_api, promised_tiers, promised_lcState, promised_lcHistory, promised_labels])
            .then(response => {
                let [api, tiers, lcState, lcHistory, labels] = response.map(data => data.obj);
                this.setState({api: api, policies: tiers, lcState: lcState, lcHistory: lcHistory, labels: labels});
            });
    }

    render() {
        if (this.state.api) {
            return (
                <div style={{padding: '30px'}}>
                    <Row gutter={16}>
                        <Col span={12}>
                            <Card title="Change Lifecycle" bordered={false} style={{margin: '5px'}}>
                                <LifeCycleUpdate handleUpdate={this.updateData} lcState={this.state.lcState}
                                                 api={this.state.api}/>
                            </Card>
                        </Col>
                    </Row>
                    <Row gutter={16}>
                        <Col span={12}>
                            <Card title="History" bordered={false} style={{margin: '5px'}}><LifeCycleHistory
                                lcHistory={this.state.lcHistory}/></Card>
                        </Col>
                    </Row>
                </div>
            );
        } else {
            return <Loading/>
        }
    }
}

export default LifeCycle