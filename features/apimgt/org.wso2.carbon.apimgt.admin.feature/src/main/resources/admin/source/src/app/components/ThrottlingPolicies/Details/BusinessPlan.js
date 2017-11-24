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
   success: 'Update business plan successfully',
   failure: 'Error while updating business plan',
   retrieveError: 'Error while retrieving business plan'
 };

 class BusinessPlan extends Component{
        state = {
          policy: {
            id: '',
            policyName: '',
            displayName: '',
            description: '',
            isDeployed:true,
            rateLimitCount: 0,
            rateLimitTimeUnit:'sec',
            stopOnQuotaReach: true,
            billingPlan:'FREE',
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
            customAttributes:[]
          }
        };

        componentDidMount() {
            const api = new API();

            const promised_policy = api.getSubscriptionLevelPolicy(this.props.match.params.policy_uuid);
            promised_policy.then(
                response => {
                   var policy = response.obj
                   //there is either bandwidthLimit or requestCountLimit in the response. add missing one
                   if("RequestCountLimit" == policy.defaultLimit.type) {
                     policy.defaultLimit.bandwidthLimit = this.state.policy.defaultLimit.bandwidthLimit;
                   } else {
                     policy.defaultLimit.requestCountLimit = this.state.policy.defaultLimit.requestCountLimit;
                   }
                   this.setState({policy: policy});
                }
            ).catch(
                error => {
                  this.msg.error(message.retrieveError);
                }
            );
        }

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

        handleAttributeChange = (attributes) => {
          var policy = this.state.policy;
          policy.customAttributes = attributes;
          this.setState({
            policy: policy
          });
        }

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

        handlePolicyUpdate = () => {
          const api = new API();
          var uuid = this.props.match.params.policy_uuid;
          const promised_policies = api.updateSubscriptionLevelPolicy(uuid, this.state.policy);
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
                         <Link to={"/policies/business_plans"}>
                              <Button color="contrast">Go Back</Button>
                         </Link>
                     </Toolbar>
                 </AppBar>
                <Message ref={a => this.msg = a}/>
                <Paper>
                    <Grid container className="root" direction="column">
                       <Grid item xs={12} className="grid-item">
                         <Typography className="page-title" type="display1" gutterBottom>
                            Edit Business Plan
                         </Typography>
                       </Grid>
                       <GeneralDetails policy={this.state.policy} handleChangeChild={this.handleChangeChild} />

                       <QuotaLimits policy={this.state.policy} setBandwithDataUnit={this.setBandwithDataUnit}
                               handleLimitTypeRadioButtonChild={this.handleLimitTypeRadioButtonChild}
                               handleDefaultQuotaChangeChild={this.handleDefaultQuotaChangeChild}
                               setRateLimitUnit={this.setRateLimitUnit} />

                       <BurstControl policy={this.state.policy} handleChangeChild={this.handleChangeChild} />

                       <PolicyFlags policy={this.state.policy} handleChangeChild={this.handleChangeChild} />

                       <CustomAttributes attributes={this.state.policy.customAttributes}
                               handleAttributeChange={this.handleAttributeChange}/>

                       <Paper elevation ={20}>
                           <Grid item xs={6} className="grid-item">
                               <Divider />
                               <div >
                                <Button raised color="primary" onClick = {
                                  () => this.handlePolicyUpdate()}>
                                Update
                                </Button>
                                <Link to={"/policies/business_plans"}>
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

 export default BusinessPlan;
