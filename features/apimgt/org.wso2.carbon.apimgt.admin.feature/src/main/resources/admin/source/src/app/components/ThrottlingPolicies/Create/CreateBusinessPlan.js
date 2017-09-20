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
 import { Route, Redirect } from 'react-router'

 import {Link} from 'react-router-dom'
 import AppBar from 'material-ui/AppBar';
 import Toolbar from 'material-ui/Toolbar';;
 import IconButton from 'material-ui/IconButton';
 import Button from 'material-ui/Button';
 import MenuIcon from 'material-ui-icons/Menu';
 import Menu, { MenuItem } from 'material-ui/Menu';

 import PropTypes from 'prop-types';
 import { withStyles } from 'material-ui/styles';
 import TextField from 'material-ui/TextField';
 import Typography from 'material-ui/Typography';
 import Divider from 'material-ui/Divider';
 import Grid from 'material-ui/Grid';
 import { FormControl, FormLabel, FormGroup, FormControlLabel } from 'material-ui/Form';
 import Radio, { RadioGroup } from 'material-ui/Radio';
 import Paper from 'material-ui/Paper';
 import Checkbox from 'material-ui/Checkbox';
 import List, { ListItem, ListItemText } from 'material-ui/List';

 import GeneralDetails from '../Shared/GeneralDetails'
 import QuotaLimits from '../Shared/QuotaLimits'

 import API from '../../../data/api'
 import Message from '../../Shared/Message'
 import '../Shared/Shared.css'

  const billingOptions = [
   'Free',
   'Commercial'
  ];

  const bandwidthUnits = [
   'KB',
   'MB'
  ];
  const burstControlUnits = [
   'Request/s',
   'Request/min'
  ];
  const burstControlUnitsMap = [
   'sec',
   'min'
  ];


 class CreateBusinessPlan extends Component{
        state = {
          value:'',
          anchorElBilling: null,
          openBilling: false,
          selectedIndexBilling: 0,
          anchorElQuota: null,
          openQuota: false,
          selectedIndexQuota: 1,
          anchorElBurst: null,
          openBurst: false,
          selectedIndexBurst: 0,
          policy: {
            policyName: '',
            displayName: '',
            description: '',
            isDeployed:true,
            rateLimitCount: 0,
            rateLimitTimeUnit:'sec',
            stopOnQuotaReach: true,
            billingPlan:'Free',
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




        handleChange = name => event => {
          this.setState({
            [name]: event.target.value,
          });
        };

        button = undefined;

        handleQuotaClickListItem = event => {
          this.setState({ openQuota: true, anchorElQuota: event.currentTarget });
        };

        handleQuotaMenuItemClick = (event, index) => {
          var policy = this.state.policy;
          policy.defaultLimit.bandwidthLimit.dataUnit = bandwidthUnits[index];
          this.setState({ selectedIndexQuota: index, openQuota: false , policy: policy});
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

        handleQuotaRequestClose = () => {
          this.setState({ openQuota: false });
        };


        handleBillingClickListItem = event => {
          this.setState({ openBilling: true, anchorElBilling: event.currentTarget });
        };

        handleBillingMenuItemClick = (event, index) => {
          this.setState({ selectedIndexBilling: index, openBilling: false });
        };

        handleBillingRequestClose = () => {
          this.setState({ openBilling: false });
        };

        handleBurstClickListItem = event => {
          this.setState({ openBurst: true, anchorElBurst: event.currentTarget });
        };

        handleBurstMenuItemClick = (event, index) => {
          var policy = this.state.policy;
          policy.rateLimitTimeUnit = burstControlUnitsMap[index];
          this.setState({ selectedIndexBurst: index, openBurst: false, policy: policy });
        };

        handleBurstRequestClose = () => {
          this.setState({ openBurst: false });
        };
        handleLimitTypeRadioButton = (event, value) => {
          var policy = this.state.policy;
          policy.defaultLimit.type = value;
          this.setState({ policy: policy });
        };

        handleLimitTypeRadioButtonChild = (value) => {
          var policy = this.state.policy;
          policy.defaultLimit.type = value;
          this.setState({ policy: policy });
        };


        handleChange = name => event => {
          var policy = this.state.policy;
          var intValue = parseInt(event.target.value);
          policy[name] = isNaN(intValue)? event.target.value: intValue;
          if(name == "policyName"){
            policy['displayName'] = event.target.value
          }
          this.setState({
            policy: policy
          });
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

        handleDefaultQuotaChange = name => event => {
          var policy = this.state.policy;
          var intValue = parseInt(event.target.value);
          var value = isNaN(intValue)? event.target.value: intValue;
          if("requestCountLimit" == name) {
              policy.defaultLimit.requestCountLimit.requestCount = value;
          } else if ("bandwidthLimit" == name) {
              policy.defaultLimit.bandwidthLimit.dataAmount = value;
          }
          this.setState({
            policy: policy
          });
        };

        handleDefaultQuotaChangeChild = (name, value) => {
          var policy = this.state.policy;
          var intValue = parseInt(value);
          var value = isNaN(intValue)? value: intValue;
          if("requestCountLimit" == name) {
              policy.defaultLimit.requestCountLimit.requestCount = value;
          } else if ("bandwidthLimit" == name) {
              policy.defaultLimit.bandwidthLimit.dataAmount = value;
          } else if ("unitTime" == name) {
              policy.defaultLimit.unitTime = value;
          }
          this.setState({
            policy: policy
          });
        }


        handlePolicyFlageChange = name => (event, checked) => {
          var policy = this.state.policy;
          policy.stopOnQuotaReach = checked;
          this.setState({ policy : policy });
        };

        handlePolicySave = () => {

          const api = new API();

          const promised_policies = api.createSubscriptionLevelPolicy(this.state.policy);
          /* TODO: Handle catch case , auth errors and ect ~tmkb*/
          promised_policies.then(
              response => {
                this.msg.info("Created business plan successfully");
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
                            Create Business Plan
                         </Typography>
                       </Grid>
                       <GeneralDetails policy={this.state.policy} handleChangeChild={this.handleChangeChild} />

                       <QuotaLimits policy={this.state.policy} setBandwithDataUnit={this.setBandwithDataUnit}
                               handleLimitTypeRadioButtonChild={this.handleLimitTypeRadioButtonChild}
                               handleDefaultQuotaChangeChild={this.handleDefaultQuotaChangeChild}
                               setRateLimitUnit={this.setRateLimitUnit} />


                       <Paper elevation ={20}>
                           <Grid item xs={12}>
                               <Typography className="page-title" type="subheading" gutterBottom>
                                  Burst Conrtol (Rate Limiting)
                               </Typography>

                           </Grid>
                           <Grid item xs={6} className="grid-item">
                               <Divider />
                               <div className="container">
                                   <TextField
                                     id="burstContromReqCount"
                                     label="Request Count"
                                     value={this.state.policy.rateLimitCount}
                                     onChange={this.handleChange('rateLimitCount')}
                                     className="text-field-half"
                                     margin="normal"
                                   />

                                   <List>
                                     <ListItem
                                       button
                                       aria-haspopup="true"
                                       aria-controls="lock-menu"
                                       aria-label="Burst Control Units"
                                       onClick={this.handleBurstClickListItem}
                                     >
                                       <ListItemText
                                         primary={burstControlUnits[this.state.selectedIndexBurst]}
                                       />
                                     </ListItem>
                                   </List>
                                   <Menu
                                     id="lock-menu"
                                     anchorEl={this.state.anchorElBurst}
                                     open={this.state.openBurst}
                                     onRequestClose={this.handleBurstRequestClose}
                                   >
                                     {burstControlUnits.map((option, index) => (
                                       <MenuItem
                                         key={option}
                                         selected={index === this.state.selectedIndexBurst}
                                         onClick={event => this.handleBurstMenuItemClick(event, index)}
                                       >
                                         {option}
                                       </MenuItem>
                                     ))}
                                   </Menu>
                                </div>
                           </Grid>
                        </Paper>
                        <Paper elevation ={20}>
                           <Grid item xs={12}>
                               <Typography className="page-title" type="subheading" gutterBottom>
                                  Policy Flags
                               </Typography>

                           </Grid>
                           <Grid item xs={6} className="grid-item">
                               <Divider />
                               <FormControlLabel
                                    control={
                                      <Checkbox
                                         checked={this.state.policy.stopOnQuotaReach}
                                         onChange={this.handlePolicyFlageChange('stopOnQuotaReach')}
                                         value="stopOnQuotaReach"
                                      />
                                    }
                                    label="Stop On Quota Reach"
                                />
                           </Grid>
                           <Grid item xs={6} className="grid-item">
                               <List>
                                 <ListItem
                                   button
                                   aria-haspopup="true"
                                   aria-controls="lock-menu"
                                   aria-label="Billing Plan"
                                   onClick={this.handleBillingClickListItem}
                                 >
                                   <ListItemText
                                     primary="Billing Plan"
                                     secondary={billingOptions[this.state.selectedIndexBilling]}
                                   />
                                 </ListItem>
                               </List>
                               <Menu
                                 id="lock-menu"
                                 anchorEl={this.state.anchorElBilling}
                                 open={this.state.openBilling}
                                 onRequestClose={this.handleBillingRequestClose}
                               >
                                 {billingOptions.map((option, index) => (
                                   <MenuItem
                                     key={option}
                                     selected={index === this.state.selectedIndexBilling}
                                     onClick={event => this.handleBillingMenuItemClick(event, index)}
                                   >
                                     {option}
                                   </MenuItem>
                                 ))}
                               </Menu>
                           </Grid>

                           <Grid item xs={6} className="grid-item">
                               <Divider />
                               <div >
                                <Button raised color="primary" onClick = {
                                  () => this.handlePolicySave()}>
                                Save
                                </Button>
                                <Button raised >
                                  Cancel
                                </Button>
                               </div>
                           </Grid>

                       </Paper>

                    </Grid>

               </Paper>
           </div>

         );

     }
 }


 export default CreateBusinessPlan;
