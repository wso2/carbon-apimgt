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
 import { withStyles } from 'material-ui/styles';
 import Table, { TableBody, TableCell, TableHead, TableRow } from 'material-ui/Table';
 import Snackbar from 'material-ui/Snackbar';
 import Button from 'material-ui/Button';
 import Typography from 'material-ui/Typography';
 import Card, { CardActions, CardContent } from 'material-ui/Card';

 import API from '../../data/api'
 import Alert from '../Shared/Alert'

 const messages = {
   success: 'Updated successfully!',
   updateFailure: 'Error while updating task',
   error: 'Something went wrong while updating the workflow task!',
   retrieveFailure: 'Error while retrieving tasks'
 };
const styles = theme => ({
    card: {
        minWidth: 275,
        paddingBottom: 20,
    },
    pos: {
        marginBottom: 12,
        color: theme.palette.text.secondary,
    },
    createAppWrapper: {
        textDecoration: 'none',
    },
    divider: {
        marginBottom: 20,
    },
    titleWrapper: {
        display: 'flex',
    }
});

 class TasksListing extends Component {
   constructor(props) {
       super(props);
       this.state = {
           workflows: null,
           selectedRowKeys: [],

       };
       this.state.workflow_type = props.match.params.workflow_type;
       this.onSelectChange = this.onSelectChange.bind(this);
       this.handleWorkflowComplete = this.handleWorkflowComplete.bind(this);
   }


   handleWorkflowComplete(referenceId, status) {

       const api = new API();

       let promised_update = api.completeWorkflow(referenceId, status);
       promised_update.then(
           response => {
             if (response.status !== 200) {
                 this.msg.error(messages.error);
                 return;
             }
             let workflows = this.state.workflows;
             for (let workflowsIndex in workflows) {
                 if (workflows.hasOwnProperty(workflowsIndex) && workflows[workflowsIndex].referenceId === referenceId) {
                     workflows.splice(workflowsIndex, 1);
                     break;
                 }
             }
             Alert.info(messages.success);
             this.setState({active: false, workflows: workflows});

           }
       ).catch(
           error => {
             Alert.error(messages.updateFailure);
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
       ).catch(
           error => {
             Alert.error(messages.retrieveFailure);
           }
       );
     }
   }

   componentDidMount() {
       const api = new API();
       const type = "AM_" + this.state.workflow_type.toUpperCase() ;

       const promised_workflows = api.getWorkflows(type);
       promised_workflows.then(
           response => {
              this.setState({workflows: response.obj.list});
           }
       ).catch(
           error => {
             Alert.error(messages.retrieveFailure);
           }
       );
   }

   onSelectChange(selectedRowKeys) {
       this.setState({selectedRowKeys: selectedRowKeys});
   }

   render() {
       const {selectedRowKeys, workflows} = this.state;
       const rowSelection = {
           selectedRowKeys,
           onChange: this.onSelectChange,
       };
       const { classes } = this.props;
       let data = [];
       if(workflows) {
         data = workflows
       }

       return (
           <div>
               <div className={classes.titleWrapper}>
                   <Typography variant="display1" gutterBottom >
                       Pending Tasks
                   </Typography>
               </div>
               {data.length === 0 ? (
                   <div>

                       <Card className={classes.card}>
                           <CardContent>
                               <Typography className={classes.title}>
                                   There are no pending tasks to be approved</Typography>
                           </CardContent>
                       </Card>
                   </div>
               ) : (
             <Table>
               <TableHead>
                 <TableRow>
                   <TableCell>Description</TableCell>
                   <TableCell>Created Time</TableCell>
                   <TableCell>Action</TableCell>
                 </TableRow>
               </TableHead>
               <TableBody>
                 {data.map(n => {
                   return (
                     <TableRow key={n.createdTime}>
                       <TableCell>{n.description}</TableCell>
                       <TableCell>{n.createdTime}</TableCell>
                       <TableCell>
                       <span>
                          <Button color="primary" onClick = {
                            () => this.handleWorkflowComplete(n.referenceId, 'APPROVED')}>Approve</Button>
                          <Button color="secondary" onClick = {
                            () => this.handleWorkflowComplete(n.referenceId, 'REJECTED')}>Reject</Button>
                       </span>
                       </TableCell>
                     </TableRow>
                   );
                 })}
               </TableBody>
             </Table>
               )}
           </div>
       );
   }
 }
export default withStyles(styles)(TasksListing);
