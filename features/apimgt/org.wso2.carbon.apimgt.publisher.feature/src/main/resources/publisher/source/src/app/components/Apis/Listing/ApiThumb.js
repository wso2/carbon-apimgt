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

import React from 'react'
import API from '../../../data/api'

import {Redirect, Switch} from 'react-router-dom'
import Typography from 'material-ui/Typography';
import Button from 'material-ui/Button';
import Grid from 'material-ui/Grid';
import NotificationSystem from 'react-notification-system';
import {resourceMethod, resourcePath, ScopeValidation} from "../../../data/ScopeValidation";
import Utils from "../../../data/Utils";
import ConfirmDialog from "../../Shared/ConfirmDialog";
import {withStyles} from 'material-ui/styles';
import * as icons from 'material-ui-icons';
import Delete from 'material-ui-icons/Delete';
import MoreHoriz from 'material-ui-icons/MoreHoriz';

import { MenuItem, MenuList } from 'material-ui/Menu';
import Grow from 'material-ui/transitions/Grow';
import { Manager, Target, Popper } from 'react-popper';
import ClickAwayListener from 'material-ui/utils/ClickAwayListener';
import classNames from 'classnames';
import Paper from 'material-ui/Paper'
import NavBar from '../Details/NavBar'


const styles = theme => ({
    lifeCycleState: {
        width: "1.5em",
        height: "1.5em",
        borderRadius: "50%",
        marginRight: "0.5em"
    },
    lifeCycleState_Created: {backgroundColor: "#0000ff"},
    lifeCycleState_Prototyped: {backgroundColor: "#42dfff"},
    lifeCycleState_Published: {backgroundColor: "#41830A"},
    lifeCycleState_Maintenance: {backgroundColor: "#cecece"},
    lifeCycleState_Deprecated: {backgroundColor: "#D7C850"},
    lifeCycleState_Retired: {backgroundColor: "#000000"},
    lifeCycleDisplay: {
        width: 95,
        height: 30,
        marginTop: -15,
        marginLeft: 10,
        backgroundColor: '#4cb050dd',
        color: '#fff',
        textAlign: 'center',
        lineHeight: '30px',
        position: 'absolute',
    },
    thumbContent: {
        width: 250,
        backgroundColor: '#fff',
        padding: 10
    },
    thumbLeft: {
        alignSelf: 'flex-start',
        flex: 1
    },
    thumbRight: {
        alignSelf: 'flex-end',
    },
    thumbInfo: {
        display: 'flex',
    },
    thumbHeader: {
        width: 250,
        whiteSpace: 'nowrap',
        overflow : 'hidden',
        textOverflow : 'ellipsis',
        cursor: 'pointer'
    },
    svgImage: {
        cursor: 'pointer'
    },
    descriptionOverlay: {
        content:'',
        width:'100%',
        height:'100%' ,
        position:'absolute',
        left:0,
        top:0,
        background:'linear-gradient(transparent 25px, white)',
    },
    descriptionWrapper: {
        position: 'relative',
        height: 50,
        overflow: 'hidden'
    },
    thumbDelete: {
        cursor: 'pointer',
        backgroundColor: '#ffffff9a',
        display: 'inline-block',
        position: 'absolute',
        top: 20,
        left: 224,
    },
    thumbWrapper: {
        position: 'relative',
        paddingTop: 20,
    },
    deleteIcon: {
        fill: 'red'
    },
    moreButton: {
        position: 'absolute',
        zIndex: 100,
        marginTop: -25,
        left: 170,

    }
});

class ApiThumb extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            active: true,
            loading: false,
            open: false,
            overview_link: '',
            isRedirect: false,
            openDeleteConfirmDialog: false,
            openRedirectConfirmDialog: false,
            redirectConfirmDialogDetails: {},
            openMoreMenu: false,
        };

        this.handleApiDelete = this.handleApiDelete.bind(this);
        this.handleRedirectToAPIOverview = this.handleRedirectToAPIOverview.bind(this);
        this.deleteConfirmDialogCallback = this.deleteConfirmDialogCallback.bind(this);
        this.redirectConfirmDialogCallback = this.redirectConfirmDialogCallback.bind(this);
    }

    handleRequestClose = () => {
        this.setState({openDeleteConfirmDialog: false});
    };

    openDeleteConfirmDialog = () => {
        this.setState({openDeleteConfirmDialog: true});
    };

    handleApiDelete() {
        this.setState({loading: true});
        const api = new API();
        const api_uuid = this.props.api.id;
        const name = this.props.api.name;
        let promised_delete = api.deleteAPI(api_uuid);
        promised_delete.then(
            response => {
                if (response.status !== 200) {
                    console.log(response);
                    this.refs.notificationSystem.addNotification({
                        message: 'Something went wrong while deleting the ' + name + ' API!', position: 'tc',
                        level: 'error'
                    });
                    this.setState({open: false, openDeleteConfirmDialog: false});
                    return;
                }
                this.refs.notificationSystem.addNotification({
                    message: name + ' API deleted Successfully', position: 'tc', level: 'success'
                });
                this.props.updateApi(api_uuid);
                this.setState({active: false, loading: false});
            }
        );
    }

    handleRedirectToAPIOverview = (page) => {
        const {api, environmentName, rootAPI} = this.props;
        const currentEnvironmentName = Utils.getCurrentEnvironment().label;
        // If environment name or version is not defined then consider as same environment or version.
        const isSameEnvironment = !environmentName || environmentName === currentEnvironmentName;
        const isSameVersion = !rootAPI || rootAPI.version === api.version;

        if (isSameEnvironment && isSameVersion) {
            this.setState({
                overview_link: `/apis/${api.id}`,
                isRedirect: true
            });
        } else { // Ask for confirmation to switch environment or version of the API
            const redirectConfirmDialogDetails = ApiThumb.getRedirectConfirmDialogDetails({
                api, rootAPI, environmentName, currentEnvironmentName, isSameEnvironment, isSameVersion
            });

            this.setState({
                overview_link: `/apis/${api.id}/overview?environment=${environmentName}`,
                isRedirect: false,
                openRedirectConfirmDialog: true,
                redirectConfirmDialogDetails,
            });
        }
    }

    deleteConfirmDialogCallback(result) {
        this.setState({
            openDeleteConfirmDialog: false
        });
        if(result) this.handleApiDelete();
    }

    redirectConfirmDialogCallback(result) {
        this.setState({
            isRedirect: result,
            openRedirectConfirmDialog: false
        });
    }

    handleClickMoreMenu = () => {
        this.setState({ openMoreMenu: true });
    };

    handleCloseMoreMenu = () => {
        this.setState({ openMoreMenu: false });
    };

    render() {
        const {api, environmentOverview, classes} = this.props;
        const gridItemSizes = environmentOverview ?
            {xs: 6, sm: 4, md: 3, lg: 2, xl: 2} : {xs: 6, sm: 4, md: 3, lg: 2, xl: 2};

        if (!this.state.active) { // Controls the delete state, We set the state to inactive on delete success call
            return null;
        }

        if (this.state.isRedirect) {
            return (
                <Switch>
                    <Redirect to={this.state.overview_link}/>
                </Switch>
            );
        }


        const colorPairs = [
            {prime: 0x8f6bcaff, sub:0x4fc2f8ff },
            {prime: 0xf47f16ff, sub:0xcddc39ff },
            {prime: 0xf44236ff, sub:0xfec107ff },
            {prime: 0x2196f3ff, sub:0xaeea00ff },
            {prime: 0xff9700ff, sub:0xffeb3cff },
            {prime: 0xff9700ff, sub:0xfe5722ff },
        ];
        const thumbnailBox = {
            width: 250,
            height: 200
        };

        const thumbnailBoxChild = {
            width: 50,
            height: 50
        };
        //Get a random color pair
        let allChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_&!#$";
        let str = api.name;
        let iconIndex = 0;
        let colorIndex = str.length;
        for(let i=0; i< str.length; i++){
            iconIndex += allChars.indexOf(str[i]);
        }
        while(colorIndex > 5 ){
            colorIndex  -= 6;
        }
        //let colorIndex = Math.floor(Math.random() * Math.floor(colorPairs.length)); //Get a random color combination
        let colorPair = colorPairs[colorIndex];
        //let iconIndex = Math.floor(Math.random() * Math.floor(Object.keys(icons).length)); // Get a random icon index
        let tmpIndex = 0;
        let icon = null;


        for( let i in icons){
            if(icons.hasOwnProperty(i)){
                tmpIndex++;
                if(tmpIndex === iconIndex ){
                    icon = icons[i];
                }
            }
        }
        let rects = [];
        for( let i=0; i <= 4; i++ ){
            for( let j=0; j <= 4; j++ ) {

                rects.push(<rect
                    {...thumbnailBoxChild}
                    fill={"#" + (colorPair.sub - 0x00000025 * i - j*0x00000015).toString(16)}
                    x={200 - i * 54}
                    y={54*j}
                />)
            }
        }
        const Icon = icon;
        return (
            <Grid item {...gridItemSizes} className={classes.thumbWrapper}>
                {api &&  <div
                    className={
                        `${classes.lifeCycleDisplay} ${classes[`lifeCycleState_${api.lifeCycleStatus}`]}`
                    }
                    >{api.lifeCycleStatus}</div> }
                {/*Do not render for environment overview page*/}
                {!environmentOverview &&
                    <ScopeValidation resourcePath={resourcePath.SINGLE_API}
                                     resourceMethod={resourceMethod.DELETE}>
                        <a className={classes.thumbDelete} onClick={this.openDeleteConfirmDialog}>
                            <Delete className={classes.deleteIcon} />
                        </a>
                    </ScopeValidation>
                }
                <svg width="250" height="190" onClick={this.handleRedirectToAPIOverview} className={classes.svgImage}>
                    <rect
                        {...thumbnailBox}
                        fill={"#" + colorPair.prime.toString(16)}
                    />
                    {rects}
                    <Icon />
                </svg>

                <Manager className={classes.moreButton}>
                    <Target>
                        <Button
                            aria-owns={this.state.openMoreMenu ? 'menu-list' : null}
                            aria-haspopup="true"
                            onClick={this.handleClickMoreMenu}
                            variant="raised" size="small" color="default"
                        >
                            <MoreHoriz />
                        </Button>
                    </Target>
                    <Popper
                        placement="bottom-start"
                        eventsEnabled={this.state.openMoreMenu}
                        className={classNames({ [classes.popperClose]: !this.state.openMoreMenu })}
                    >
                        <ClickAwayListener onClickAway={this.handleCloseMoreMenu}>
                            <Grow in={this.state.openMoreMenu} id="menu-list" style={{ transformOrigin: '0 0 0' }}>
                                <Paper>
                                    <MenuList role="menu">
                                        <MenuItem onClick={this.handleRedirectToAPIOverview}>Profile</MenuItem>
                                        <MenuItem onClick={this.handleRedirectToAPIOverview}>My account</MenuItem>
                                        <MenuItem onClick={this.handleRedirectToAPIOverview}>Logout</MenuItem>
                                    </MenuList>
                                </Paper>
                            </Grow>
                        </ClickAwayListener>
                    </Popper>
                </Manager>
                <div className={classes.thumbContent}>
                    <Typography className={classes.thumbHeader} variant="display1" gutterBottom
                                onClick={this.handleRedirectToAPIOverview}>
                        {environmentOverview ? <span>{api.version}</span> : <span>{api.name}</span> }
                    </Typography>
                    <div className={classes.thumbInfo}>
                        <div className={classes.thumbLeft}>
                            <Typography variant="display1">
                                {environmentOverview ? <span>{api.name}</span> : <span>{api.version}</span> }
                            </Typography>
                            <Typography variant="caption" gutterBottom align="left">
                                {environmentOverview ? <span>Name</span> : <span>Version</span> }
                            </Typography>
                        </div>
                        <div className={classes.thumbRight}>
                            <Typography variant="display1" align="right">{api.context}</Typography>
                            <Typography variant="caption" gutterBottom align="right">
                                Context
                            </Typography>
                        </div>
                    </div>
                    <div className={classes.descriptionWrapper}>
                        {api.description}
                        <div className={classes.descriptionOverlay} />
                        </div>

                </div>
                <NotificationSystem ref="notificationSystem"/>

                <ConfirmDialog
                    title={`Delete API "${api.name} - ${api.version}"?`}
                    message={"Are you sure you want to delete the API?"}
                    labelOk={"Delete"}
                    callback={this.deleteConfirmDialogCallback}
                    open={this.state.openDeleteConfirmDialog}
                />
                <ConfirmDialog
                    {...this.state.redirectConfirmDialogDetails}
                    callback={this.redirectConfirmDialogCallback}
                    open={this.state.openRedirectConfirmDialog}
                />
            </Grid>
        );
    }

    static getRedirectConfirmDialogDetails(details) {
        const {api, rootAPI, environmentName, currentEnvironmentName, isSameEnvironment, isSameVersion} = details;

        let title = `Switch to ${api.name} ${api.version}` +
            `${isSameEnvironment ? '?' : ` in ${environmentName} Environment?`}`;
        let message = 'We are going to switch the ' +
            `${isSameEnvironment ? '' : `environment "${currentEnvironmentName}" to "${environmentName}"`}` +
            `${!isSameEnvironment && !isSameVersion ? ' and ' : ''}` +
            `${isSameVersion ? '' : `API version "${rootAPI.version}" to "${api.version}"`}`;
        let labelCancel = 'Cancel';
        let labelOk = 'Switch';

        return {title, message, labelCancel, labelOk};
    }
}

export default withStyles(styles)(ApiThumb);
