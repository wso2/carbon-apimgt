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
 import AppBar from 'material-ui/AppBar';
 import Toolbar from 'material-ui/Toolbar';;
 import IconButton from 'material-ui/IconButton';
 import Button from 'material-ui/Button';
 import MenuIcon from 'material-ui-icons/Menu';
 import Menu, { MenuItem } from 'material-ui/Menu';
 import Typography from 'material-ui/Typography';
 import Divider from 'material-ui/Divider';
 import Grid from 'material-ui/Grid';
 import Paper from 'material-ui/Paper';

 import GeneralDetails from '../Shared/GeneralDetails'
 import QuotaLimits from '../Shared/QuotaLimits'
 import BurstControl from '../Shared/BurstControl'
 import PolicyFlags from '../Shared/PolicyFlags'
 import CustomAttributes from '../Shared/CustomAttributes'

 import API from '../../../data/api'
 import Message from '../../Shared/Message'
 import '../Shared/Shared.css'

 const messages = {
   success: 'Created API rate limit successfully',
   failure: 'Error while creating API rate limit'
 };

 class CreateAPIPolicy extends Component{
         state = {
           policy: {            
             policyName: '',
             displayName: '',
             description: '',
             isDeployed:true,
             defaultLimit: {
               bandwidthLimit: {
                 dataAmount: 0,
                 dataUnit:'MB'
               },
               requestCountLimit: {
                 requestCount: 0
               },
               type: 'RequestCountLimit',
               timeUnit: "min",
               unitTime: 0
             },
             conditionalGroups:[]
           }
         };


        setBandwithDataUnit = (value) => {
          var policy = this.state.policy;
          policy.defaultLimit.bandwidthLimit.dataUnit = value;
          this.setState({policy: policy});
        };

        setRateLimitUnit = (value) => {
          var policy = this.state.policy;
          policy.defaultLimit.timeUnit = value;
          this.setState({policy: policy});
        }

        handleLimitTypeRadioButtonChild = (value) => {
          var policy = this.state.policy;
          policy.defaultLimit.type = value;
          this.setState({ policy: policy });
        };

        handleChangeChild = (name, value) => {
          var policy = this.state.policy;
          var intValue = parseInt(value);
          policy[name] = isNaN(intValue)? value: intValue;
          if(name == "policyName"){
            policy['displayName'] = value
          }
          this.setState({
            policy: policy
          });
        };

        handleDefaultQuotaChangeChild = (name, value) => {
          var policy = this.state.policy;
          var intValue = parseInt(value);
          var value = isNaN(intValue)? value: intValue;
          if("RequestCountLimit" == name) {
              policy.defaultLimit.requestCountLimit.requestCount = value;
          } else if ("BandwidthLimit" == name) {
              policy.defaultLimit.bandwidthLimit.dataAmount = value;
          } else if ("unitTime" == name) {
              policy.defaultLimit.unitTime = value;
          }
          this.setState({
            policy: policy
          });
        }

        handleAttributeChange = (attributes) => {
          var policy = this.state.policy;
          policy.customAttributes = attributes;
          this.setState({
            policy: policy
          });
        }

        handlePolicySave = () => {
          const api = new API();
          const promised_policies = api.createAPILevelPolicy(this.state.policy);
          var props = this.props;
          promised_policies.then(
              response => {
                this.msg.info(messages.success);
              }
          ).catch(
              error => {
                this.msg.error(messages.failure);
              }
          );
        };

        render() {
           return (
            <div>
                 <AppBar position="static" >
                     <Toolbar style={{minHeight:'30px'}}>
                         <IconButton color="contrast" aria-label="Menu">
                             <MenuIcon />
                         </IconButton>
                         <Link to={"/policies/api_policies"}>
                              <Button color="contrast">Go Back</Button>
                         </Link>
                     </Toolbar>
                 </AppBar>
                <Message ref={a => this.msg = a}/>
                <Paper>
                    <Grid container className="root" direction="column">
                       <Grid item xs={12} className="grid-item">
                         <Typography className="page-title" type="display1" gutterBottom>
                            Create API Rate Limit
                         </Typography>
                       </Grid>
                       <GeneralDetails policy={this.state.policy} handleChangeChild={this.handleChangeChild} />

                       <QuotaLimits policy={this.state.policy} setBandwithDataUnit={this.setBandwithDataUnit}
                               handleLimitTypeRadioButtonChild={this.handleLimitTypeRadioButtonChild}
                               handleDefaultQuotaChangeChild={this.handleDefaultQuotaChangeChild}
                               setRateLimitUnit={this.setRateLimitUnit} />


                       <Paper elevation ={20}>
                           <Grid item xs={6} className="grid-item">
                               <Divider />
                               <div >
                                <Button raised color="primary" onClick = {
                                  () => this.handlePolicySave()}>
                                Save
                                </Button>
                                <Link to={"/policies/api_policies"}>
                                     <Button raised>Cancel</Button>
                                </Link>
                               </div>
                           </Grid>
                       </Paper>
                    </Grid>
               </Paper>
           </div>
         );
     }
 }

 export default CreateAPIPolicy;
