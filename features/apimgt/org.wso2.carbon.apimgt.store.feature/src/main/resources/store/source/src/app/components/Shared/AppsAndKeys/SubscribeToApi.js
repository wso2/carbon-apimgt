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

import React, { Component } from 'react';
import MenuItem from '@material-ui/core/MenuItem';
import FormControl from '@material-ui/core/FormControl';
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';
import TextField from '@material-ui/core/TextField';
import Input from '@material-ui/core/Input';
import InputLabel from '@material-ui/core/InputLabel';
import Select from '@material-ui/core/Select';
import {withStyles} from '@material-ui/core/styles';
import PropTypes from 'prop-types';
import FormHelperText from '@material-ui/core/FormHelperText';
import ApiContext from '../../Apis/Details/ApiContext'
import Api from '../../../data/api'


const styles = theme => ({
    titleBar: {
        display: 'flex',
        justifyContent: 'space-between',
        borderBottomWidth: '1px',
        borderBottomStyle: 'solid',
        borderColor: theme.palette.text.secondary,
        marginBottom: 20,
    },
    buttonLeft: {
        alignSelf: 'flex-start',
        display: 'flex',
    },
    buttonRight: {
        alignSelf: 'flex-end',
        display: 'flex',
    },
    title: {
        display: 'inline-block',
        marginLeft: 20
    },
    buttonsWrapper: {
        marginTop: 40
    },
    legend: {
        marginBottom: 0,
        borderBottomStyle: 'none',
        marginTop: 20,
        fontSize: 12,
    },
    inputText: {
        marginTop: 20,
    },
    buttonRight: {
        marginLeft: 20,
    },
    buttonRightLink: {
        textDecoration: 'none',
    },
    FormControl: {
        padding: theme.spacing.unit*2,
        width: '100%',
    },
    FormControlOdd: {
        padding: theme.spacing.unit*2,
        backgroundColor: theme.palette.background.paper,
        width: '100%',
    },
    quotaHelp: {
        position:'relative',
    }
});

class SubscribeToApi extends Component {

    constructor(props) {
        super(props);
        this.state = {
            age: '',
            name: 'hai',   
            appSelected: null,
            tierSelected: null,
        };
    }
    handleChange = event => {
        this.setState({ [event.target.name]: event.target.value });
      };
    getTiers(api){
         //Getting the policies from api details
         let tiers = [];

         if(api && api.policies){
             let apiTiers = api.policies;
             for (let i = 0; i < apiTiers.length; i++) {
                 let tierName = apiTiers[i];
                 tiers.push({value: tierName, label: tierName});
             }
             
         }
         return tiers;        
    }
    
    createSubscription() {
        let api_uuid = this.props.api.id;

        let applicationId = this.state.appSelected;
        let policy = this.state.tierSelected;
        let api = new Api();
        let promised_subscribe = api.subscribe(api_uuid, applicationId, policy);
       return promised_subscribe;
    };
    componentDidMount() {
        const { newApp, applicationsAvailable, api } = this.props;
        if(newApp) {
            this.state.appSelected = this.props.newApp.value;
        } else {
            this.state.appSelected = applicationsAvailable[0].value;
        }
        this.state.tiers = this.getTiers(api);
        if (this.state.tiers.length > 0) {
            this.setState({tierSelected:this.state.tiers[0].value});
        }
    }
    render() {
        const { classes , applicationsAvailable, newApp } = this.props;
        if(newApp) {
            applicationsAvailable.push(newApp); // Add the new app to the applications available
        }
        
        return (
     
                
            <Grid container spacing={24} className={classes.root}>
                <Grid item xs={12} md={6}>
                    {this.state.appSelected &&
                    <FormControl className={classes.FormControl} disabled={this.props.newApp? true : false}>
                        <InputLabel shrink htmlFor="age-label-placeholder" className={classes.quotaHelp}>
                            Application
                        </InputLabel>
                        
                        <Select
                            value={this.state.appSelected}
                            onChange={this.handleChange}
                            input={<Input name="appSelected" id="app-label-placeholder" />}
                            displayEmpty
                            name="appSelected"
                            className={classes.selectEmpty}
                        >
                        {applicationsAvailable.map( (app) => 
                            <MenuItem value={app.value} key={app.value} >{app.label}</MenuItem>
                        )}
                        </Select>
                        <FormHelperText>Select an Application to subscribe</FormHelperText>
                    </FormControl> }
                        {this.state.tiers && 
                        <FormControl className={classes.FormControlOdd}>
                            <InputLabel shrink htmlFor="tier-label-placeholder" className={classes.quotaHelp}>
                                Throttling Tier
                            </InputLabel>
                            <Select
                                value={this.state.tierSelected}
                                onChange={this.handleChange}
                                input={<Input name="tierSelected" id="tier-label-placeholder" />}
                                displayEmpty
                                name="tierSelected"
                                className={classes.selectEmpty}
                            >
                            {this.state.tiers.map((tier) => 
                                <MenuItem value={tier.value}  key={tier.value}>{tier.label}</MenuItem>
                            )}
                            </Select>
                            <FormHelperText>Label + placeholder</FormHelperText>
                        </FormControl> }
                     
                    </Grid>
                </Grid>


        );
    }
}

SubscribeToApi.propTypes = {
    classes: PropTypes.object.isRequired,
};
  
export default withStyles(styles)(SubscribeToApi);