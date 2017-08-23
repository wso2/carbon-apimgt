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
 'use strict';

 import React, {Component} from 'react'
 import {Link} from 'react-router-dom'
 import {Table, Popconfirm, Button, Dropdown, Menu, message} from 'antd';

 import API from '../../data/api'

 //width of the table colum for the description section as a percentage
 const description_table_width = '60%';

 class TasksListing extends Component {
   constructor(props) {
       super(props);
       this.state = {
           workflows: null,
           selectedRowKeys: []
       };
       this.state.workflow_type = props.match.params.workflow_type;
       this.onSelectChange = this.onSelectChange.bind(this);
       this.handleWorkflowComplete = this.handleWorkflowComplete.bind(this);
   }


   handleWorkflowComplete(referenceId, status) {
       const hideMessage = message.loading("Completing worklow task ...", 0);
       const api = new API();

       let promised_update = api.completeWorkflow(referenceId, status);
       promised_update.then(
           response => {
             if (response.status !== 200) {
                 message.error("Something went wrong while updating the workflow task!");
                 hideMessage();
                 return;
             }
             let workflows = this.state.workflows;
             for (let workflowsIndex in workflows) {
                 if (workflows.hasOwnProperty(workflowsIndex) && workflows[workflowsIndex].referenceId === referenceId) {
                     workflows.splice(workflowsIndex, 1);
                     break;
                 }
             }
             this.setState({active: false, workflows: workflows});
             message.success("Updated successfully!");

             hideMessage();
           }
       );
   }

   //Since Same component is used for all the tasks, this is implemented to reload the tables
   componentWillReceiveProps(nextProps){

     if(nextProps.match.params.workflow_type !== this.state.workflow_type) {

       const api = new API();
       const type = "AM_" + nextProps.match.params.workflow_type.toUpperCase() ;

       const promised_workflows = api.getWorkflows(type);
       /* TODO: Handle catch case , auth errors and ect ~tmkb*/
       promised_workflows.then(
           response => {
              this.setState(
                {
                  workflows: response.obj.list,
                  workflow_type: nextProps.match.params.workflow_type
                });
           }
       );
     }
   }

   componentDidMount() {
       const api = new API();
       const type = "AM_" + this.state.workflow_type.toUpperCase() ;

       const promised_workflows = api.getWorkflows(type);
       /* TODO: Handle catch case , auth errors and ect ~tmkb*/
       promised_workflows.then(
           response => {
              this.setState({workflows: response.obj.list});
           }
       );
   }

   onSelectChange(selectedRowKeys) {
       this.setState({selectedRowKeys: selectedRowKeys});
   }

   render() {
       const {selectedRowKeys, workflows} = this.state;
       const columns = [{
           title: 'Description',
           dataIndex: 'description',
            width: description_table_width
       }, {
           title: 'Created Time',
           dataIndex: 'createdTime',
           sorter: (a, b) => a.maxTps - b.maxTps,
       }, {
           title: 'Action',
           key: 'action',
           render: (text, record) => {
               return (
                 <span>
                   <Button icon="check" onClick = {() => this.handleWorkflowComplete(text.referenceId, 'APPROVED')} >Approve</Button>
                   <span className="ant-divider" />
                   <Button type="danger" icon="close"
                          onClick = {() => this.handleWorkflowComplete(text.referenceId, 'REJECTED')}>Reject</Button>
                </span>
                 )
           }
       }];
       const rowSelection = {
           selectedRowKeys,
           onChange: this.onSelectChange,
       };


       return (
           <div>
               <Table rowSelection={rowSelection} loading={workflows === null} columns={columns}
                      dataSource={workflows}
                      rowKey="id"
                      size="middle"/>
           </div>
       );
   }
 }
export default TasksListing
