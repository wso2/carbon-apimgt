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
import { Link } from 'react-router-dom';
import API from '../../../data/api'

import Button from '@material-ui/core/Button';
import MenuItem from '@material-ui/core/MenuItem';
import FormControl from '@material-ui/core/FormControl';
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';
import TextField from '@material-ui/core/TextField';
import Input from '@material-ui/core/Input';
import InputLabel from '@material-ui/core/InputLabel';
import Select from '@material-ui/core/Select';
import ArrowBack from '@material-ui/icons/ArrowBack';
import {withStyles} from '@material-ui/core/styles';
import PropTypes from 'prop-types';
import Alert from '../Alert';

const styles = theme => ({
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

class ApplicationCreate extends Component {

    constructor(props) {
        super(props);
        this.state = {
            quota: "Unlimited",
            tiers: [],
            throttlingTier: null,
            description: null,
            name: null,
            callbackUrl: null
        };
        this.handleChange = this.handleChange.bind(this);
    }

    componentDidMount() {
        //Get all the tires to populate the drop down.
        const api = new API();
        let promised_tiers = api.getAllTiers("application");
        promised_tiers.then((response) => {
            let tierResponseObj = response.body;
            let tiers = [];
            tierResponseObj.list.map(item => tiers.push(item.name));
            this.setState({tiers: tiers});

            if (tiers.length > 0){
                this.setState({quota: tiers[0]});
            }
        }
        ).catch(
            error => {
                if (process.env.NODE_ENV !== "production") {
                    console.log(error);
                }
                let status = error.status;
                if (status === 404) {
                    this.setState({notFound: true});
                }
            }
        );
    }

    handleChange = name => event => {
        this.setState({ [name]: event.target.value });
    };

    handlePolicyChange = name => event => {
        this.setState({ [name]: event.target.value });
    };
    getAlert() {
        alert('getAlert from Child');
    }
    
    handleSubmit() {
        let promised_create;
        if (!this.state.name) {
            Alert.error("Application name is required");
        } else {
            let application_data = {
                name: this.state.name,
                throttlingTier: this.state.quota,
                description: this.state.description
            };
            let new_api = new API();
            promised_create = new_api.createApplication(application_data);            
        }
        return promised_create;

    };

    render() {
        const { classes } = this.props;
        
        return (
            <form className={classes.container} noValidate autoComplete="off">
            <Grid container spacing={24} className={classes.root}>
                <Grid item xs={12} md={6}>
                        <FormControl margin="normal" className={classes.FormControl}>
                            <TextField
                                required
                                label="Application Name"
                                InputLabelProps={{
                                    shrink: true,
                                }}
                                helperText="Enter a name to identify the Application. You will be able to pick this application
                                            when subscribing to APIs "
                                fullWidth
                                name="name"
                                onChange={this.handleChange('name')}
                                placeholder="My Mobile Application"
                                autoFocus={true}
                                className={classes.inputText}
                            />
                        </FormControl>
                     
                        {this.state.tiers &&
                        <FormControl margin="normal" className={classes.FormControlOdd}>
                            <InputLabel htmlFor="quota-helper" className={classes.quotaHelp}>Per Token Quota</InputLabel>
                            <Select
                                value={this.state.quota}
                                onChange={this.handlePolicyChange('quota')}
                                input={<Input name="quota" id="quota-helper" />}
                            >
                                {this.state.tiers.map((tier) => <MenuItem key={tier} value={tier}>{tier}</MenuItem>)}
                            </Select>
                            <Typography variant="caption">Assign API request quota per access token. Allocated quota will be
                            shared among all the subscribed APIs of the application.</Typography>
                        </FormControl>
                        }
                        <FormControl margin="normal" className={classes.FormControl}>
                            <TextField
                                label="Application Description"
                                InputLabelProps={{
                                    shrink: true,
                                }}
                                helperText="Describe the application"
                                fullWidth
                                multiline
                                rowsMax="4"
                                name="description"
                                onChange={this.handleChange('description')}
                                placeholder="This application is grouping apis for my mobile application"
                                className={classes.inputText}
                            />
                        </FormControl>
                    </Grid>
                </Grid>
            </form>

        );
    }
}

ApplicationCreate.propTypes = {
    classes: PropTypes.object.isRequired,
};
  
export default withStyles(styles)(ApplicationCreate);