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

import {Link} from 'react-router-dom'
import Grid from 'material-ui/Grid';
import Paper from 'material-ui/Paper';
import Typography from 'material-ui/Typography';
import Table, { TableBody, TableCell, TableHead, TableRow } from 'material-ui/Table';
import AppBar from 'material-ui/AppBar';
import Toolbar from 'material-ui/Toolbar';;
import IconButton from 'material-ui/IconButton';
import Divider from 'material-ui/Divider';
import Button from 'material-ui/Button';
import MenuIcon from 'material-ui-icons/Menu';
import TextField from 'material-ui/TextField';
import {withStyles} from 'material-ui/styles';

import API from '../../data/api'
import Message from '../Shared/Message'
import Confirm from '../Shared/Confirm'

const messages = {
  success: 'Deleted business plan successfully',
  failure: 'Error while deleting business plan',
  retrieveError: 'Error while retrieving business plans'
};

const styles = theme => ({
    divider: {
        marginBottom: 20,
    },
    createButton:{
        textDecoration: 'none',
        display: 'inline-block',
        marginLeft: 20,
        alignSelf: 'flex-start',
    },
    titleWrapper: {
        display: 'flex',
    }
});

class BusinessPlans extends Component {
    constructor(props) {
        super(props);
        this.state = {
            policies: null,
            selectedRowKeys: [],
            open:false,
            message: ''
        };

    }
    deletePolicy(id) {
      const api = new API();
      const promised_policies = api.deleteSubscriptionLevelPolicy(id);
      promised_policies.then(
          response => {
            this.msg.info(messages.success);
          }
      ).catch(
          error => {
            this.msg.error(messages.failure);
          }
      );
    }

    componentDidMount() {
        const api = new API();

        const promised_policies = api.getSubscriptionLevelPolicies();
        /* TODO: Handle catch case , auth errors and ect ~tmkb*/
        promised_policies.then(
            response => {
               this.setState({policies: response.obj.list});
            }
        ).catch(
            error => {
              this.msg.error(messages.retrieveError);
            }
        );
    }
    render() {
        /*TODO implement search and pagination*/
        const tiers = this.state.policies;
        const {classes} = this.props;
        let data = [];
        if(tiers) {
          data = tiers;
        }

        return (
            <div>
                <Message ref={a => this.msg = a}/>
                <Grid container justify="center" alignItems="center">
                    <Grid item xs={12}>
                    
                        <div className={classes.titleWrapper}>
                            <Typography variant="display1" gutterBottom >
                                    Business Plans
                            </Typography>
                            <Link to={"/policies/business_plans/create/"} className={classes.createButton}>
                                <Button variant="raised" color="primary" className={classes.button}>
                                Add Plan
                                </Button>
                            </Link>
                            <Typography type="caption" gutterBottom align="left" className="page-title-help">
                            Discription goes here.
                            </Typography>                          
                        </div>
                        <Divider className={classes.divider} />
                    
                       
                    </Grid>
                    <Grid item xs={12} className="page-content">
                          <Paper>
                            <Table>
                              <TableHead>
                                <TableRow>
                                  <TableCell>Name</TableCell>
                                  <TableCell>Quota Policy</TableCell>
                                  <TableCell>Quota</TableCell>
                                  <TableCell>Unit Time</TableCell>
                                  <TableCell>Rate Limit</TableCell>
                                  <TableCell>Time Unit</TableCell>
                                  <TableCell></TableCell>
                                </TableRow>
                              </TableHead>
                              <TableBody>
                                {data.map(n => {
                                  return (
                                    <TableRow key={n.id}>
                                      <TableCell>{n.policyName}</TableCell>
                                      <TableCell>{n.defaultLimit.type}</TableCell>
                                      <TableCell>{n.defaultLimit.type == "RequestCountLimit"
                                            ? n.defaultLimit.requestCountLimit.requestCount
                                            : n.defaultLimit.bandwidthLimit.dataAmount + " "
                                                + n.defaultLimit.bandwidthLimit.dataUnit}</TableCell>
                                      <TableCell>{n.defaultLimit.unitTime} {n.defaultLimit.timeUnit}</TableCell>
                                      <TableCell>{n.rateLimitCount}</TableCell>
                                      <TableCell>{n.rateLimitTimeUnit}</TableCell>
                                      <TableCell>
                                      <span>
                                         <Link to={"/policies/business_plans/" + n.id}>
                                              <Button color="primary">Edit</Button>
                                         </Link>
                                         <Button color="accent"
                                              onClick={() => this.deletePolicy(n.id)} >Delete</Button>
                                      </span>
                                      </TableCell>
                                    </TableRow>
                                  );
                                })}
                              </TableBody>
                            </Table>
                          </Paper>
                    </Grid>
                </Grid>
            </div>
        );
    }
}

export default withStyles(styles)(BusinessPlans)
