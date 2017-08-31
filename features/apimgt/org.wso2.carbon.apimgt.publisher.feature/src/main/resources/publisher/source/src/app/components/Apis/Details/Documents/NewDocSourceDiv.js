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
import {Form, Input} from 'antd';

const FormItem = Form.Item;

class NewDocSourceDiv extends Component {
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
                <h4>Source</h4>
                <Form layout="vertical" style={{paddingTop: 5}}>
                    <FormItem style={{margin: 0}}>
                        <label>
                            <input type="radio" name="docSourceType" value="INLINE"
                                   checked={this.props.selectedSourceType === 'INLINE'}
                                   onClick={this.handleInputChange}/>
                            Inline
                        </label>
                    </FormItem>
                    <FormItem style={{margin: 0}}>
                        <label>
                            <input type="radio" name="docSourceType" value="URL"
                                   checked={this.props.selectedSourceType === 'URL'}
                                   onClick={this.handleInputChange}/>
                            URL
                        </label>
                        {this.props.selectedSourceType === "URL" &&
                        <Input type="text" name="docSourceURL" placeholder="eg: http://wso2.com/api-management"
                               onChange={this.handleInputChange}
                               value={this.props.docSourceURL}/>
                        }
                    </FormItem>
                    <FormItem style={{margin: 0}}>
                        <label>
                            <input type="radio" name="docSourceType" value="FILE"
                                   checked={this.props.selectedSourceType === 'FILE'}
                                   onClick={this.handleInputChange}/>
                            File
                        </label>
                        {this.props.selectedSourceType === "FILE" &&
                        <div marginWidth={10}>
                            <form>
                                <div>
                                    <input type="file" name="docFilePath"
                                           onChange={this.handleInputChange}/>
                                </div>
                            </form>
                        </div>
                        }
                    </FormItem>
                </Form>
            </div>
        );
    }
}

export default NewDocSourceDiv;