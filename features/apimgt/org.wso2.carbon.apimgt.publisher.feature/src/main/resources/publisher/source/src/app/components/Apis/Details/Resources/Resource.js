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
import { Input, Icon, Form } from 'antd';
import Select from 'material-ui/Select';
import {MenuItem} from 'material-ui/Menu';
const FormItem = Form.Item;
import Chip from 'material-ui/Chip';
import List, {
    ListItem,
    ListItemText,
    ListItemSecondaryAction
} from 'material-ui/List';
import { withStyles } from 'material-ui/styles';
import Paper from 'material-ui/Paper';
import PropTypes from 'prop-types';
import Table, { TableBody, TableCell, TableHead, TableRow } from 'material-ui/Table';
import Delete from '@material-ui/icons/Delete';
import Grid from 'material-ui/Grid';
import Button from 'material-ui/Button';
import { FormGroup, FormControlLabel } from 'material-ui/Form';
import Checkbox from 'material-ui/Checkbox';
import Collapse from 'material-ui/transitions/Collapse';

const styles = theme => ({
    root: {
        flexGrow: 1,
        marginTop: 10,
    },
    container: {
        display: 'flex',
        flexWrap: 'wrap',
    },
    textField: {
        marginLeft: theme.spacing.unit,
        marginRight: theme.spacing.unit,
        width: 400,
    },
    mainTitle: {
        paddingLeft: 20
    },
    scopes: {
        width: 400
    },
    divider: {
        marginTop: 20,
        marginBottom: 20
    },
    chip: {
        margin: theme.spacing.unit,
        color: theme.palette.text.secondary,
        minWidth: 100,
    },
    chipActive: {
        margin: theme.spacing.unit,
        color: theme.palette.text.secondary,
        background: theme.palette.background.active,
        minWidth: 100,
    },
    paper: {
        padding:20
    },
    link: {
        cursor: 'pointer'
    }
});

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
        let tempScopes = [];
        if(this.props.methodData.security && this.props.methodData.security.length!== 0){
            this.props.methodData.security.map(function(object, i){
                if(object.OAuth2Security){
                    tempScopes =  object.OAuth2Security;
                }
            });
        }
        this.state = {
            visible: false,
            method:this.props.methodData,
            scopes:tempScopes,
            deleteChecked: false
        };
        this.propsSubmitHandler = this.propsSubmitHandler.bind(this);
        this.saveFieldCallback = this.saveFieldCallback.bind(this);
        this.toggleMethodData = this.toggleMethodData.bind(this);
        this.deleteResource = this.deleteResource.bind(this);
        this.handleScopeChange = this.handleScopeChange.bind(this);

    }
    componentDidMount() {
        this.props.onRef(this);
    }
    componentWillUnmount() {
        this.props.onRef(undefined);
    }
    handleScopeChange(e) {
        this.setState({scopes: e.target.value});
        this.handleScopeChangeInSwaggerRoot(e.target.value);

    }
    handleScopeChangeInSwaggerRoot(scopes){
        let tempMethod = this.props.methodData;
        tempMethod.security.map(function(object, i){
            if(object.OAuth2Security){
                object.OAuth2Security = scopes;
            }
        });
        this.setState({method: tempMethod});
        this.props.updatePath(this.props.path,this.props.method,this.state.method);
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
    toggleDeleteCheck (checkState) {
        this.setState({deleteChecked:checkState});
        this.forceUpdate();
    }
    handleDeleteCheck = (path,method) => event => {
        this.setState({deleteChecked: event.target.checked});
        this.props.addRemoveToDeleteList(path,method);
    }

    render(){
        const { classes } = this.props;
        return (
            <div>
                <ListItem >
                    <FormControlLabel
                        control={
                            <Checkbox
                                checked={this.state.deleteChecked}
                                onChange={this.handleDeleteCheck(this.props.path, this.props.method)}
                                value=""
                            />
                        }
                        label=""
                    />
                    <a onClick={this.toggleMethodData} className={classes.link}>
                        <Chip label={this.props.method}
                              className={classes.chipActive} />
                    </a>
                    <a onClick={this.toggleMethodData}>
                    <ListItemText
                        className="foo"
                        primary={this.props.path}
                        secondary="Description" />
                    </a>
                    <ListItemSecondaryAction>
                        <a  onClick={this.deleteResource}>
                            <Delete className={classes.rightIcon} />
                        </a>
                    </ListItemSecondaryAction>

                </ListItem>


                {this.state.visible &&



                    <Paper className={classes.paper}>
                        <Grid container spacing={24}>
                            <Grid item xs={2}><strong>Description</strong></Grid>
                            <Grid item xs={10}>
                                <InlineEditableField saveFieldCallback={this.saveFieldCallback} fieldValue={this.state.method.description} fieldName="description" />
                            </Grid>

                            <Grid item xs={2}><strong>Produces</strong></Grid>
                            <Grid item xs={10}>
                                <InlineEditableField saveFieldCallback={this.saveFieldCallback} fieldValue={this.state.method.produces} fieldName="produces" />
                            </Grid>

                            <Grid item xs={2}><strong>Consumes</strong></Grid>
                            <Grid item xs={10}>
                                <InlineEditableField saveFieldCallback={this.saveFieldCallback} fieldValue={this.state.method.consumes} fieldName="consumes" />
                            </Grid>
                            <Grid item xs={2}><strong>Scopes</strong></Grid>
                            <Grid item xs={10}>
                                <Select
                                    margin="none"
                                    multiple
                                    value={this.state.scopes}
                                    onChange={this.handleScopeChange}
                                    MenuProps={{
                                        PaperProps: {
                                            style: {
                                                width: 200,
                                            },
                                        },
                                    }}>
                                    {this.props.apiScopes.list.map(tempScope => (
                                        <MenuItem
                                            key={tempScope.name}
                                            value={tempScope.name}
                                            style={{
                                                fontWeight: this.state.scopes.indexOf(tempScope.name) !== -1 ? '500' : '400',
                                            }}
                                        >
                                            {tempScope.name}
                                        </MenuItem>
                                    ))}
                                </Select>
                            </Grid>
                            <Grid item xs={12}>
                                <h3>Parameters:</h3>
                                <div className="parameter-add-wrapper">
                                    <WrappedPropertyAddForm propsSubmitHandler={this.propsSubmitHandler} />

                                </div>
                                { this.state.method.parameters.length > 0 &&
                                <Table className="parameter-table">
                                    <TableHead>
                                        <TableRow>
                                            <TableCell>Parameter Name</TableCell>
                                            <TableCell>Description</TableCell>
                                            <TableCell>Parameter Type</TableCell>
                                            <TableCell>Data Type</TableCell>
                                            <TableCell>Required</TableCell>
                                            <TableCell>Delete</TableCell>
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        {this.state.method.parameters.map(function (param,i) {
                                            return <TableRow>
                                                <TableCell><InlineEditableField saveFieldCallback={this.saveFieldCallback} fieldIndex={i} fieldValue={param.name} fieldName="param.name" /></TableCell>
                                                <TableCell><InlineEditableField saveFieldCallback={this.saveFieldCallback} fieldIndex={i} fieldValue={param.description} fieldName="param.description" /></TableCell>
                                                <TableCell></TableCell><TableCell></TableCell>
                                                {/*<td><InlineEditableField saveFieldCallback={this.saveFieldCallback} fieldIndex={i} fieldValue={param.schema.type} fieldName="param.schema.type" /></td>*/}
                                                {/*<td><InlineEditableField saveFieldCallback={this.saveFieldCallback} fieldIndex={i} fieldValue={param.schema.properties.payload.type} fieldName="param.schema.properties.payload.type" /></td>*/}
                                                <TableCell><InlineEditableField saveFieldCallback={this.saveFieldCallback} fieldIndex={i} fieldValue={param.required} fieldName="param.required" /></TableCell>
                                                <TableCell>
                                                    <a><Icon type="delete" onClick={()=>this.deleteParam(i)} /></a>
                                                </TableCell>
                                            </TableRow>;
                                        },this)}


                                    </TableBody>
                                </Table>
                                }
                            </Grid>
                        </Grid>
                    </Paper>
                }
            </div>
        )
    }
}
Resource.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(Resource);