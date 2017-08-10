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
import { Input, Icon, Checkbox, Button, Card, Tag, Form } from 'antd';
import { Row, Col } from 'antd';

const FormItem = Form.Item;

function hasErrors(fieldsError) {
    return Object.keys(fieldsError).some(field => fieldsError[field]);
}
/*
 Property add form
 */
class PropertyAddForm extends React.Component {
    componentDidMount() {
        // To disabled submit button at the beginning.
        this.props.form.validateFields();
    }
    handleSubmit = (e) => {
        e.preventDefault();
        this.props.form.validateFields((err, values) => {
            if (!err) {
                console.log('Received values of form: ', values);
                this.props.propsSubmitHandler(values);
            }
        });
    };

    render() {
        const { getFieldDecorator, getFieldsError, getFieldError, isFieldTouched } = this.props.form;

        // Only show error after a field is touched.
        const propNameError = isFieldTouched('propName') && getFieldError('propName');
        return (
            <Form layout="inline" onSubmit={this.handleSubmit}>
                <FormItem
                    validateStatus={propNameError ? 'error' : ''}
                    help={propNameError || ''}
                >
                    {getFieldDecorator('propName', {
                        rules: [{ required: true, message: 'Please input your propName!' }],
                    })(
                        <Input prefix={<Icon type="user" style={{ fontSize: 13 }} />} placeholder="PropName" />
                    )}
                </FormItem>
                <FormItem>
                    <Button
                        type="primary"
                        htmlType="submit"
                        disabled={hasErrors(getFieldsError())}
                    >
                        Add Property
                    </Button>
                </FormItem>
            </Form>
        );
    }
}

const WrappedPropertyAddForm = Form.create()(PropertyAddForm);

/*
 * Inline editable form field
 * */

class InlineEditableField extends React.Component{
    constructor(props){
        super(props);
        this.state = {
            editable : false,
            newValue: ''
        };
        this.editInlineToggle = this.editInlineToggle.bind(this);
        this.handleValueChange = this.handleValueChange.bind(this);
    }
    editInlineToggle(){
        this.state.editable ? this.setState({editable:false}) : this.setState({editable:true});
    };
    handleValueChange(e){
        this.setState({newValue:e.target.value});
    }
    render(){
        if(this.state.editable) {
            let fieldIndex;
            if(typeof this.props.fieldIndex === "number"){
                fieldIndex = this.props.fieldIndex;
            } else{
                fieldIndex = false;
            }
            return <div>
                <input type="text" className="inline-edit-input" defaultValue={this.props.fieldValue} onChange={this.handleValueChange}/>
                <Button className="primary" onClick={() => this.props.saveFieldCallback(this.props.fieldName, this.state.newValue, fieldIndex)}><Icon
                    type="check"/></Button>
                <Button onClick={this.editInlineToggle}><Icon type="close-circle-o"/></Button>
            </div>
        } else {
            return <span onClick={this.editInlineToggle} className="fieldView">{this.props.fieldValue}</span>
        }

    }
}




class Resource extends React.Component{
    constructor(props){
        super(props);
        this.state = {
            visible: false,
            method:this.props.methodData
        };
        console.info(this.props);
        this.propsSubmitHandler = this.propsSubmitHandler.bind(this);
        this.saveFieldCallback = this.saveFieldCallback.bind(this);
        this.toggleMethodData = this.toggleMethodData.bind(this);
        this.deleteResource = this.deleteResource.bind(this);
    }
    propsSubmitHandler(values){
        const defaultParams = {
            name: values.propName,
            description: "Request Body",
            required: "false",
            in: "body",
            schema: {
                type: "object"
            }
        };
        let tmpMethod = this.state.method;
        tmpMethod.parameters.push(defaultParams);
        this.setState({method: tmpMethod});
        this.props.updatePath(this.props.path,this.props.method,this.state.method);
    }
    deleteParam(i){
        let tmpPath = this.state.method;
        if(i>-1){
            tmpPath.parameters.splice(i,1);
        }
        this.setState({method: tmpPath});
        this.props.updatePath(this.props.path,this.props.method,this.state.method);
    }
    saveFieldCallback(fieldName,fieldValue,fieldIndex){
        if(fieldName.indexOf(".") !== -1 ){
            let multiLevelFieldName = fieldName.split("param.")[1];
            let tmpPath = this.state.method;
            tmpPath.parameters[fieldIndex][multiLevelFieldName] = fieldValue;
            this.setState({method:tmpPath});
        } else {
            let tmpPath = this.state.method;
            tmpPath[fieldName] = fieldValue;
            this.setState({method:tmpPath});
        }
        this.props.updatePath(this.props.path,this.props.method,this.state.method);
    }
    toggleMethodData(){
        this.setState({visible: !this.state.visible});
    }
    deleteResource(){
        /* We set null and call the update method of the Resources class */
        this.props.updatePath(this.props.path,this.props.method,null);
    }
    render(){
        return (
            <div>
                <Row type="flex" justify="start" className="resource-head">
                    <Col span={8}>
                        <a onClick={this.toggleMethodData}> <Tag color="#2db7f5">{this.props.method}</Tag>{this.props.path}</a>
                    </Col>
                    <Col span={8}>Description</Col>
                    <Col span={8} style={{textAlign:"right", cursor:"pointer"}} onClick={this.deleteResource}><Icon type="delete" /> </Col>
                </Row>
                {this.state.visible ?
                <Row type="flex" justify="start" className="resource-body">
                    <Col span={4}><strong>Description</strong></Col>
                    <Col span={20}>
                        <InlineEditableField saveFieldCallback={this.saveFieldCallback} fieldValue={this.state.method.description} fieldName="description" />
                    </Col>

                    <Col span={4}><strong>Produces</strong></Col>
                    <Col span={20}>
                        <InlineEditableField saveFieldCallback={this.saveFieldCallback} fieldValue={this.state.method.produces} fieldName="produces" />
                    </Col>

                    <Col span={4}><strong>Consumes</strong></Col>
                    <Col span={20}>
                        <InlineEditableField saveFieldCallback={this.saveFieldCallback} fieldValue={this.state.method.consumes} fieldName="consumes" />
                    </Col>

                    <Col span={24}>
                        <h3>Parameters:</h3>
                        <div className="parameter-add-wrapper">
                            <WrappedPropertyAddForm propsSubmitHandler={this.propsSubmitHandler} />

                        </div>
                        {this.state.method.parameters.length > 0 ?
                            <table className="parameter-table">
                                <tbody><tr>
                                    <th width="15%">Parameter Name</th>
                                    <th width="35%">Description</th>
                                    <th width="15%">Parameter Type</th>
                                    <th width="15%">Data Type</th>
                                    <th width="15%">Required</th>
                                    <th width="5%">Delete</th>
                                </tr>
                                {this.state.method.parameters.map(function (param,i) {
                                    return <tr>
                                        <td><InlineEditableField saveFieldCallback={this.saveFieldCallback} fieldIndex={i} fieldValue={param.name} fieldName="param.name" /></td>
                                        <td><InlineEditableField saveFieldCallback={this.saveFieldCallback} fieldIndex={i} fieldValue={param.description} fieldName="param.description" /></td>
                                        <td></td><td></td>
                                        {/*<td><InlineEditableField saveFieldCallback={this.saveFieldCallback} fieldIndex={i} fieldValue={param.schema.type} fieldName="param.schema.type" /></td>*/}
                                        {/*<td><InlineEditableField saveFieldCallback={this.saveFieldCallback} fieldIndex={i} fieldValue={param.schema.properties.payload.type} fieldName="param.schema.properties.payload.type" /></td>*/}
                                        <td><InlineEditableField saveFieldCallback={this.saveFieldCallback} fieldIndex={i} fieldValue={param.required} fieldName="param.required" /></td>
                                        <td>
                                            <a><Icon type="delete" onClick={()=>this.deleteParam(i)} /></a>
                                        </td>
                                    </tr>;
                                },this)}


                                </tbody>
                            </table>
                            : ''}
                    </Col>
                </Row> : null}

            </div>
        )
    }
}

export default Resource;