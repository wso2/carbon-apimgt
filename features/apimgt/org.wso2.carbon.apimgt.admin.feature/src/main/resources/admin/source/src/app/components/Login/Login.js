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
import './login.css'
import {Switch, Redirect} from 'react-router-dom'
import AuthManager from '../../data/AuthManager'
import qs from 'qs'
import {Layout} from 'antd';
const {Header, Content, Footer} = Layout;
import {Form, Icon, Input, Button, Checkbox, message} from 'antd';
import {Card} from 'antd';
import {Row, Col} from 'antd';
const FormItem = Form.Item;


class NormalLoginForm extends Component {

    constructor(props) {
        super(props);
        this.authManager = new AuthManager();
        this.state = {
            isLogin: false,
            referrer: "/",
            userNameEmpty: true,
            loading: false
        };
    }

    handleSubmit = (e) => {
        e.preventDefault();
        this.setState({loading: true});
        this.props.form.validateFields((err, values) => {
            if (!err) {
                console.log('Received values of form: ', values);
                let username = values.userName;
                let password = values.password;
                let loginPromise = this.authManager.authenticateUser(username, password);
                loginPromise.then((response) => {
                    this.setState({isLogin: AuthManager.getUser(), loading: false});
                }).catch((error) => {
                        message.error("error");
                        console.log(error);
                        this.setState({loading: false});
                    }
                );
            } else {
            }
        });
    }

    componentDidMount() {
        let queryString = this.props.location.search;
        queryString = queryString.replace(/^\?/, '');
        /* With QS version up we can directly use {ignoreQueryPrefix: true} option */
        let params = qs.parse(queryString);
        if (params.referrer) {
            this.setState({referrer: params.referrer});
        }
    }

    emitEmpty = () => {
        this.setState({
            userNameEmpty: true
        });
    };

    render() {
        const {getFieldDecorator} = this.props.form;
        const makeEmptySuffix = this.state.userNameEmpty ? <Icon type="close-circle" onClick={this.emitEmpty}/> : '';

        if (!this.state.isLogin) { // If not logged in, go to login page
            return (
                <Form onSubmit={this.handleSubmit} className="login-form">
                    <FormItem>
                        {getFieldDecorator('userName', {
                            rules: [{required: true, message: 'Please input your username!'}],
                        })(
                            <Input prefix={<Icon type="user" style={{fontSize: 13}}/>} placeholder="Username"
                                   suffix={makeEmptySuffix}/>
                        )}
                    </FormItem>
                    <FormItem>
                        {getFieldDecorator('password', {
                            rules: [{required: true, message: 'Please input your Password!'}],
                        })(
                            <Input prefix={<Icon type="lock" style={{fontSize: 13}}/>} type="password"
                                   placeholder="Password"/>
                        )}
                    </FormItem>
                    <FormItem>
                        {getFieldDecorator('remember', {
                            valuePropName: 'checked',
                            initialValue: true,
                        })(
                            <Checkbox>Remember me</Checkbox>
                        )}
                        <Button loading={this.state.loading} type="primary" htmlType="submit"
                                className="login-form-button">
                            Log in
                        </Button>
                    </FormItem>
                </Form>
            );
        } else {// If logged in, redirect to /apis page
            return (
                <Switch>
                    <Redirect to={this.state.referrer}/>
                </Switch>
            );
        }
    }
}

const WrappedNormalLoginForm = Form.create()(NormalLoginForm);

class Login extends Component {

    render() {
        return (
            <Layout className="layout" style={{height: "100vh"}}>
                <Content>
                    <Row type="flex" justify="center" align="middle">
                        <Col>
                            <div className="login-card-wrapper">
                                <Card>
                                    <div className="login-card">
                                        <img className="brand" src="/admin/public/images/logo.svg" alt="wso2-logo"/>
                                        <p>API Admin Portal</p>
                                    </div>
                                    <WrappedNormalLoginForm location={this.props.location}/>
                                </Card >
                            </div>
                        </Col>
                    </Row>
                </Content>
                <Footer style={{textAlign: "left"}}>
                    WSO2 | © 2017
                    <a href="http://wso2.com/" target="_blank"><i
                        className="icon fw fw-wso2"/> Inc</a>.
                </Footer>
            </Layout>
        )

    }
}

export default Login;