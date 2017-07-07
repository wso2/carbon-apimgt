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
import Api from '../../../../data/SingleClient'
import { Form, Icon, Input, Button } from 'antd'
const FormItem = Form.Item

class Endpoint extends Component {

    constructor(props) {
        super(props);
        this.api = new Api();
    }

    render() {
        return (
            <Form layout="inline" onSubmit={this.handleSubmit}>
                <FormItem>
                        <Input prefix={<Icon type="user" style={{ fontSize: 13 }} />} placeholder="Username" />
                </FormItem>
                <FormItem>
                        <Input prefix={<Icon type="lock" style={{ fontSize: 13 }} />} type="password" placeholder="Password" />
                </FormItem>
                <FormItem>
                    <Button
                        type="primary"
                        htmlType="submit">
                        Log in
                    </Button>
                </FormItem>
            </Form>


        );
    }
}

export default Endpoint
