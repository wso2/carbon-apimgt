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

import Button from 'material-ui/Button';
import { MenuItem } from 'material-ui/Menu';
import {Form} from 'material-ui/Form'
import { FormGroup, FormControlLabel, FormControl, FormHelperText, FormLabel } from 'material-ui/Form';
import Grid from 'material-ui/Grid';
import Typography from 'material-ui/Typography';
import { Delete, Edit, CreateNewFolder, Description  }from 'material-ui-icons';
import TextField from 'material-ui/TextField';
import IconButton from 'material-ui/IconButton';
import Paper from 'material-ui/Paper';
import Input, { InputLabel } from 'material-ui/Input';
import Select from 'material-ui/Select';
import ArrowBack from 'material-ui-icons/ArrowBack';
import Divider from 'material-ui/Divider';
import {withStyles} from 'material-ui/styles';
import PropTypes from 'prop-types';
import Alert from '../../Shared/Alert';

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

    handleSubmit = (e) => {
        e.preventDefault();
        let application_data = {
            name: this.state.name,
            throttlingTier: this.state.quota,
            description: this.state.description
        };
        let updateSubscriptionData = this.props.updateSubscriptionData;
        let handleAppDialogClose = this.props.handleAppDialogClose;
        let new_api = new API();
        let promised_create = new_api.createApplication(application_data);
        promised_create.then(response => {
            let uuid = JSON.parse(response.data).applicationId;
            //Once application loading fixed this need to pass application ID and load app
            
            if(updateSubscriptionData){
                handleAppDialogClose();
                updateSubscriptionData();   
            } else {
                let redirect_url = "/applications/";
                this.props.history.push(redirect_url);
                console.log("Application created successfully.");
            }
            
        }).catch(
            function (error_response) {
                Alert.error('Application already exists.');
                console.log("Error while creating the application");
            });
    };

    render() {
        const { classes, updateSubscriptionData } = this.props;
        
        return (
            <Grid container spacing={0} justify="flex-start">
                {/* Show the heading only for the normal application creation page */}
                {!updateSubscriptionData &&
                <Grid item xs={12} sm={12} md={12} lg={11} xl={10} className={classes.titleBar}>
                    <div className={classes.buttonLeft}>
                        <Link to={"/applications/"}>
                            <Button  variant="raised" size="small" className={classes.buttonBack}
                                    color="default">
                                <ArrowBack />
                            </Button>
                        </Link>
                        <div className={classes.title}>
                            <Typography variant="display2">
                                Add New Application
                            </Typography>
                        </div>
                    </div>
                </Grid>
                }
                <Grid item xs={12} lg={6} xl={4}>
                    <form className={classes.container} noValidate autoComplete="off">
                        <TextField
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
            
                     
                        {this.state.tiers &&
                        <FormControl margin="normal">
                            <InputLabel htmlFor="quota-helper">Per Token Quota</InputLabel>
                            <Select
                                value={this.state.quota}
                                onChange={this.handlePolicyChange('quota')}
                                input={<Input name="quota" id="quota-helper" />}
                            >
                                {this.state.tiers.map((tier) => <MenuItem key={tier} value={tier}>{tier}</MenuItem>)}
                            </Select>
                            <FormHelperText>Assign API request quota per access token. Allocated quota will be
                            shared among all the subscribed APIs of the application.</FormHelperText>
                        </FormControl>
                        }

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
                        <div className={classes.buttonsWrapper}>
                            <Button variant="raised" color="primary"  onClick={this.handleSubmit}>
                                Create
                            </Button>
                            {updateSubscriptionData ?
                                <Button variant="raised" className={classes.buttonRight} onClick={this.props.handleAppDialogClose}>
                                    Cancel
                                </Button>
                            :
                                <Link to={"/applications/"} className={classes.buttonRightLink}>
                                    <Button variant="raised" className={classes.buttonRight}>
                                        Cancel
                                    </Button>
                                </Link>
                             }
                        </div>
                    </form>
                </Grid>
            </Grid>
        );
    }
}

ApplicationCreate.propTypes = {
    classes: PropTypes.object.isRequired,
};
  
export default withStyles(styles)(ApplicationCreate);