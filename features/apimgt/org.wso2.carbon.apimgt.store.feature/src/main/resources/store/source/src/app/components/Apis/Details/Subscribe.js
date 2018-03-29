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
import Loading from '../../Base/Loading/Loading'
import ResourceNotFound from "../../Base/Errors/ResourceNotFound";
import Api from '../../../data/api'
import { Link } from 'react-router-dom'

import {withStyles} from 'material-ui/styles';
import Button from 'material-ui/Button';
import Dialog, {DialogActions, DialogContent, DialogContentText, DialogTitle} from 'material-ui/Dialog';
import {MenuItem} from 'material-ui/Menu';
import Select from 'material-ui/Select';
import {FormControl} from 'material-ui/Form';
import NotificationSystem from 'react-notification-system';
import {InputLabel} from 'material-ui/Input';
import Slide from "material-ui/transitions/Slide";
import Typography from 'material-ui/Typography';
import ListSubheader from 'material-ui/List/ListSubheader';
import List, { ListItem, ListItemText, ListItemIcon } from 'material-ui/List';
import Dns from 'material-ui-icons/Dns'
import AppBar from 'material-ui/AppBar';
import Toolbar from 'material-ui/Toolbar';
import Divider from 'material-ui/Divider';
import IconButton from 'material-ui/IconButton';
import CloseIcon from 'material-ui-icons/Close';
import ApplicationCreate from '../../Applications/Create/ApplicationCreate'
import Grid from 'material-ui/Grid'
import PropTypes from 'prop-types';


const styles = theme => ({
    media: {
        height: 200,
    },
    root: {
        paddingLeft: 20,
        width: '100%',
        flex: 1,
    },
    listApps: {
        width: '100%',
        backgroundColor: theme.palette.background.paper,
        position: 'relative',
        overflow: 'auto',
        maxHeight: 300,
    },
        listSection: {
        backgroundColor: 'inherit',
    },
        ul: {
        backgroundColor: 'inherit',
        padding: 0,
    },
    appListItem: {
        paddingLeft: 0,
        marginLeft: 20,
    },
    appBar: {
        position: 'relative',
    },
    flex: {
        flex: 1,
    },
    applicationCreateRoot: {
        marginLeft: 40,
        marginRight: 40,
    },
    closeButton: {
        marginLeft: 10,
        marginRight: 10,
    },
    caption:{
        color: theme.palette.text.secondary,
    },
    applicationName: {
        color: theme.palette.text.primary,
    },
    formControl: {
        paddingRight: 20,
        marginBottom: 10,
        width: 200,
    },
    subtitle: {
        marginTop: 20,
    },
    appLink: {
        textDecoration: 'none',
    },
    viewAllLink: {
        color: theme.palette.text.secondary,
    }
});
function Transition(props) {
    return <Slide direction="up" {...props} />;
  }
class Subscribe extends Component {
    constructor(props) {
        super(props);
        this.state = {
            api: null,
            applications: null,
            policies: null,
            dropDownApplications: null,
            dropDownPolicies: null,
            notFound: false,
            openSubscribeMenu: false,
            matDropVisible: false,
            matDropValue: 'one',
            subscribedApplications: [],
            applicationsAvailable: [],
            tiers: [],
            applicationId: '',
            policyName: '',
            openPopup: false,
            anchorEl: null,
            createAppOpen: false,
            openSubsConfirm: false,
        };
        this.api_uuid = this.props.uuid;
        this.logChange = this.logChange.bind(this);
        this.openSubscribeMenu = this.openSubscribeMenu.bind(this);
        this.closeSubscribeMenu = this.closeSubscribeMenu.bind(this);
        this.handlePopupOpen = this.handlePopupOpen.bind(this);
        this.handlePopupClose = this.handlePopupClose.bind(this);
        this.updateSubscriptionData = this.updateSubscriptionData.bind(this);
    }

    updateSubscriptionData() {
        const api = new Api();
        let promised_api = api.getAPIById(this.api_uuid);
        let existing_subscriptions = api.getSubscriptions(this.api_uuid, null);
        let promised_applications = api.getAllApplications();
        
        Promise.all([ promised_api, existing_subscriptions, promised_applications] ).then(
            response => {
                let [api, subscriptions, applications] = response.map(data => data.obj);

                //Getting the policies from api details
                this.setState({api: api});
                if(api && api.policies){
                    let apiTiers = api.policies;
                    let tiers = [];
                    for (let i = 0; i < apiTiers.length; i++) {
                        let tierName = apiTiers[i];
                        tiers.push({value: tierName, label: tierName});
                    }
                    this.setState({tiers: tiers});
                    if (tiers.length > 0) {
                        this.setState({policyName: tiers[0].value});
                    }
                }
                
                let subscribedApplications = [];
                //get the application IDs of existing subscriptions
                subscriptions.list.map(element => subscribedApplications.push({value: element.applicationId, 
                    policy: element.policy}));
                this.setState({subscribedApplications: subscribedApplications});


                //Removing subscribed applications from all the applications and get the available applications to subscribe
                let applicationsAvailable = [];
                for (let i = 0; i < applications.list.length; i++) {
                    let applicationId = applications.list[i].applicationId;
                    let applicationName = applications.list[i].name;
                    //include the application only if it does not has an existing subscriptions
                    let applicationSubscribed = false;
                    for ( var j =0; j < subscribedApplications.length; j++ ){
                        if( subscribedApplications[j].value === applicationId ){
                            applicationSubscribed = true;
                            subscribedApplications[j].label = applicationName;
                        }
                    }
                    if (!applicationSubscribed) {
                        applicationsAvailable.push({value: applicationId, label: applicationName});
                    }
                }
                this.setState({applicationsAvailable});
                if (applicationsAvailable && applicationsAvailable.length > 0) {
                    this.setState({applicationId: applicationsAvailable[0].value});
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
        )
    }
    componentDidMount() {
        this.updateSubscriptionData();
    }
    handleChange = name => event => {
        this.setState({[name]: event.target.value});
    };

    addNotifications() {
        this.refs.notificationSystem.addNotification({
            message: 'Subscribe to API successfully',
            position: 'tc',
            level: 'success'
        });
    };

    createSubscription = (e) => {
        e.preventDefault();
        let apiId = this.api_uuid;
        let applicationId = this.state.applicationId;
        let policy = this.state.policyName;
        let api = new Api();
        let promised_subscribe = api.subscribe(apiId, applicationId, policy);
        promised_subscribe.then(response => {
            console.log("Subscription created successfully with ID : " + response.body.subscriptionId);
            
            //this.addNotifications();
            this.updateSubscriptionData();
            this.setState({openSubsConfirm:true})

        }).catch(error => {
                console.log("Error while creating the subscription.");
                console.error(error);
            }
        )
    };

    handleClick() {
        this.setState({redirect: true});
    }

    openSubscribeMenu() {
        this.setState({openSubscribeMenu: true});
    }

    closeSubscribeMenu() {
        this.setState({openSubscribeMenu: false});
    }

    handlePopupClose() {
        this.setState({openPopup: false});
    };

    handlePopupOpen(event) {
        this.setState({openPopup: true, anchorEl: event.currentTarget});
    };

    onBlur(e) {
        if (!e.currentTarget.contains(document.activeElement)) {
            this.setState({matDropVisible: false});
        }
    }

    logChange(val) {
        this.setState({matDropValue: val.value});
        console.log("Selected: " + JSON.stringify(val));
    }
    handleAppDialogOpen = () => {
        this.setState({ createAppOpen: true });
    };

    handleAppDialogClose = () => {
        this.setState({ createAppOpen: false });
    };
    handleCloseSubsConfirm = () => {
        this.setState({openSubsConfirm:false})
    }
    render() {
        if (this.state.notFound) {
            return <ResourceNotFound/>
        }
        if (this.state.redirect) {
            return <Redirect push to="/application-create"/>;
        }
        const {classes} = this.props;
        const api = this.state.api;

        return (
            this.state.api ?
                <div className={classes.root}>
                    {this.state.applications && this.state.applications.length > 0 ? 
                    <div>
                        <Typography variant="headline" className={classes.headline} >
                            Test this API?
                        </Typography>

                        <Typography gutterBottom>
                            {`
                            Create an Application and subscribe this API to that Application.
                            An application is a logical collection of APIs. 
                            Applications allow you to use a single access token to invoke a collection 
                            of APIs and to subscribe to one API multiple times with different SLA levels. 
                            The DefaultApplication is pre-created and allows unlimited access by default.
                            `}
                        </Typography>

                        <Button onClick={this.handleAppDialogOpen} color="primary" variant="raised"
                                    className="form-buttons full-width"> Create New Application</Button> 
                        </div> 
                    : <div>
                        <Typography variant="headline" className={classes.headline} >
                            Subscriptions
                        </Typography>
                        
                        {this.state.applicationsAvailable && this.state.applicationsAvailable.length > 0 ?
                            <div>
                                <FormControl 
                                            className={classes.formControl}>
                                    <InputLabel>Applications</InputLabel>
                                    <Select
                                        value={this.state.applicationId}
                                        onChange={this.handleChange('applicationId')}
                                    >
                                        {this.state.applicationsAvailable.map((app) => <option value={app.value} key={app.value}>
                                            {app.label}
                                        </option>)}
                                    </Select>
                                </FormControl>
                                {this.state.tiers &&
                                    <FormControl
                                            className={classes.formControl}>
                                        <Select
                                            value={this.state.policyName}
                                            onChange={this.handleChange('policyName')}
                                        >
                                            {this.state.tiers.map((tier) => <option value={tier.value}>{tier.label}</option>)}
                                        </Select>
                                    </FormControl>
                                }
                                <Button onClick={this.createSubscription} color="primary" variant="raised"
                                    className="form-buttons full-width"> Subscribed to this API</Button>   
                            </div>
                            
                            : <div>
                                <Typography gutterBottom>
                                    {`
                                    You have subscribed to all the available applications. You need to create a 
                                    new application to subscribe again to this API.
                                    `}
                                </Typography>
                                <Button onClick={this.handleAppDialogOpen} color="primary" variant="raised"
                                    className="form-buttons full-width"> Create New Application</Button>    
                            </div>
                            }
                            

                            {this.state.subscribedApplications  && this.state.subscribedApplications .length > 0 &&  
                                <div>
                                    <Typography variant="caption" className={classes.subtitle}>
                                        subscribed Applications
                                        { (this.state.subscribedApplications && this.state.subscribedApplications.length > 2) && 
                                            <span>
                                                - showing 2 out of {this.state.subscribedApplications.length} -
                                                <Link to="/applications" className={classes.viewAllLink}> View All</Link>
                                            </span>
                                        }
                                    </Typography>
                                        {this.state.subscribedApplications.slice(0).reverse().map( (app, index)  => 
                                        index < 2 && 
                                            <div className={classes.appListWrapper} key={index}>
                                                <Link to={"/applications/" + app.value} key={app.value} className={classes.appLink}>
                                                    <span className={classes.applicationName}>
                                                        {app.label} 
                                                    </span>
                                                    <span className={classes.caption}>
                                                        - ( {app.policy} ) 
                                                    </span>
                                                </Link>
                                            </div>
                                        )}
                                   
                                    
                                </div>
                            }


                    </div> }

                   




                    {/* Application creation container */}
                    <Dialog
                    fullScreen
                    open={this.state.createAppOpen}
                    onClose={this.handleClose}
                    transition={Transition}
                    >
                        <Grid container>
                            <Grid item xs={1} className={classes.closeButton} >
                                <IconButton color="inherit" onClick={this.handleAppDialogClose} aria-label="Close">
                                    <CloseIcon />
                                </IconButton>
                            </Grid>
                            <Grid item xs={11} className={classes.applicationCreateRoot} >
                                <ApplicationCreate 
                                    updateSubscriptionData={this.updateSubscriptionData} 
                                    handleAppDialogClose={this.handleAppDialogClose}  />
                            </Grid>
                            
                        </Grid>
                    </Dialog>

                    {/* Dialog to show once user have subscribed. */}
                    <Dialog
                        open={this.state.openSubsConfirm}
                        onClose={this.handleCloseSubsConfirm}
                        aria-labelledby="alert-dialog-title"
                        aria-describedby="alert-dialog-description"
                        >
                        <DialogTitle id="alert-dialog-title">{"Use Google's location service?"}</DialogTitle>
                        <DialogContent>
                            <DialogContentText id="alert-dialog-description">
                            successfully created application.
                            </DialogContentText>
                        </DialogContent>
                        <DialogActions>
                            <Link to={"/applications/" + this.state.applicationId}>
                                <Button color="primary">
                                Go to application page.
                                </Button>
                            </Link>
                            <Button onClick={this.handleCloseSubsConfirm} color="primary" autoFocus>
                            Stay on the API detials page.
                            </Button>
                        </DialogActions>
                    </Dialog>
                   
                </div>
                : <Loading/>
        );
    }
}



Subscribe.propTypes = {
    classes: PropTypes.object.isRequired,
};
  
export default withStyles(styles)(Subscribe);
