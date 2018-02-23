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
import {Link, withRouter} from 'react-router-dom'
import Button from 'material-ui/Button';
import {
    ChromeReaderMode,
    Code,
    Description,
    DonutLarge,
    FilterNone,
    Games,
    InsertDriveFile,
    LockOutline,
    Subscriptions,
    Tune,
    VerifiedUser,
    ArrowDropDown
} from 'material-ui-icons';
import {withStyles} from 'material-ui/styles';
import blue from 'material-ui/colors/blue';
import Utils from "../../../data/Utils";
import {ArrowBack, OpenInNew } from "material-ui-icons"
import Api from '../../../data/api'
import classNames from 'classnames';
import { MenuItem, MenuList } from 'material-ui/Menu';
import Grow from 'material-ui/transitions/Grow';
import Paper from 'material-ui/Paper';
import Typography from 'material-ui/Typography';
import { Manager, Target, Popper } from 'react-popper';
import ClickAwayListener from 'material-ui/utils/ClickAwayListener';
import DeleteApi from './DeleteApi'


const styles = {
    root: {
        display: 'flex',
        alignItems: 'center',
        flexWrap: 'wrap',
        marginBottom: 20,
    },
    active: {
        backgroundColor: blue[500]
    },
    drawerPaper: {
        position: 'relative',
        height: '100%'
    },
    icon: {
        marginRight: 10,
    },
    manager: {
        display: 'inline-block',
        marginLeft: 20,
        zIndex: 1203,
    },
    activeMoreTab: {
        color: '#efefef',
        fontSize: 10,
    },
    heading: {
        display: 'inline-block',
        margin: '0 20px 0 0',
    },
    contentShifter: {
        flex: 1,
    },
    storeButton: {
        display: 'flex',
        justifyContent: 'center',
    },
    storeButtonText: {
        marginRight: 10,
        lineHeight: '24px',
    }
};


class NavBar extends Component {

    constructor(props) {
        super(props);
        this.state = {
            api: null,
            openMore: false,
            windowWidth: 0,
        };
        this.updateWindowSize = this.updateWindowSize.bind(this);
    }
    handleClickMore = () => {
        this.setState({ openMore: !this.state.openMore });
    };
    handleCloseMore = () => {
        if (!this.state.openMore) {
            return;
        }

        // setTimeout to ensure a close event comes after a target click event
        this.timeout = setTimeout(() => {
            this.setState({ openMore: false });
        });
    };
    updateWindowSize() {
        this.setState({windowWidth: window.innerWidth});
    }
    componentDidMount() {
        let api = new Api();
        let promised_api = api.get(this.props.api_uuid);
        promised_api.then(
            response => {
                this.setState({api: response.obj});
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
        this.updateWindowSize();
        window.addEventListener('resize',this.updateWindowSize);
    }
    componentWillUnmount() {
        clearTimeout(this.timeout);
        window.removeEventListener('resize',this.updateWindowSize);

    }
    render() {
        /* TODO: This could have been done easily with match object containing api_uuid value , But
         Due to a bug (https://github.com/ReactTraining/react-router/issues/4649) in the latest version(4.1.1),
         it's not working as expected, Hence doing this hack, revert to following with react-router upgrade
         const api_uuid = this.props.match.params.api_uuid; ~tmkb */
        const pathSegments = this.props.location.pathname.split('/');
        // This assume that last segment and segment before it contains detail page action and API UUID
        const [active_tab, api_uuid] = pathSegments.reverse();
        const {classes} = this.props;
        const {openMore, api, windowWidth} = this.state;

        const tabs = [
            {name: "lifecycle", icon: <DonutLarge  className={classes.icon}/>, important: true},
            {name: "endpoints", icon: <Games  className={classes.icon}/>, important: true},
            {name: "resources", icon: <FilterNone  className={classes.icon}/>, important: true},
            {name: "scopes", icon: <FilterNone  className={classes.icon}/>},
            {name: "documents", icon: <InsertDriveFile  className={classes.icon}/>},
            {name: "permission", icon: <LockOutline  className={classes.icon}/>},
            {name: "mediation", icon: <Tune  className={classes.icon}/>},
            {name: "scripting", icon: <Code  className={classes.icon}/>},
            {name: "subscriptions", icon: <Subscriptions  className={classes.icon}/>},
            {name: "security", icon: <VerifiedUser  className={classes.icon}/>},
        ];
        if (Utils.isMultiEnvironmentOverviewEnabled()) {
            tabs.splice(1, 0, {name: "environment view", icon: <Description/>});
        }

        function highlightActiveTab(tab) {
            return (tab === active_tab) ? "primary" : "default";
        }

        function showMoreActiveTab() {
            let activeMoreElement = "";
            for( let i=0; i < tabs.length; i++){
                if( tabs[i].name === active_tab && !tabs[i].important){
                    activeMoreElement = " ( " + active_tab + " ) ";
                }
            }
            return activeMoreElement;
        }
        return (
            <div className={classes.root}>
                <Link to={"/apis/" + api_uuid + "/overview" }>
                <Typography variant="display1" gutterBottom className={classes.heading}>
                    { api && <span>{api.name}</span> }
                </Typography>
                </Link>
                    { tabs.map(tab =>
                        ( tab.important &&
                            <Link name={tab.name} to={"/apis/" + api_uuid + "/" + tab.name}>
                                <Button key={tab.name} variant="raised" size="small"
                                        color={highlightActiveTab(tab.name)} className={classes.button}>
                                    {tab.icon} { ( windowWidth > 1300 ) && <span>{tab.name}</span> }
                                </Button>
                        </Link>
                        )
                    )}
                {api && <DeleteApi api={api} />}
                <Manager className={classes.manager}>
                        <Target>
                            <Button
                                aria-owns={openMore ? 'menu-list' : null}
                                aria-haspopup="true"
                                onClick={this.handleClickMore}
                                variant="raised" size="small"
                                className={classes.moreButton}
                                color={(showMoreActiveTab() === "")? "default" : "primary"}
                            >
                                <ArrowDropDown />
                                More
                                <div className={classes.activeMoreTab}>{showMoreActiveTab()}</div>
                            </Button>
                        </Target>
                        <Popper
                            placement="bottom-start"
                            eventsEnabled={openMore}
                            className={classNames({ [classes.popperClose]: !openMore })}
                        >
                            <ClickAwayListener onClickAway={this.handleCloseMore}>
                                <Grow in={openMore} id="menu-list" style={{ transformOrigin: '0 0 0' }}>
                                    <Paper>
                                        <MenuList role="menu">
                                            {tabs.map(tab =>
                                                ( !tab.important &&
                                                    <Link name={tab.name} to={"/apis/" + api_uuid + "/" + tab.name}>
                                                        <MenuItem onClick={this.handleCloseMore}
                                                                  key={tab.name} variant="raised" size="small"
                                                                  color={highlightActiveTab(tab.name)} className={classes.button}>
                                                            {tab.icon} {tab.name}
                                                        </MenuItem>

                                                    </Link>
                                                ))}
                                        </MenuList>
                                    </Paper>
                                </Grow>
                            </ClickAwayListener>
                        </Popper>
                    </Manager>
                    <div className={classes.contentShifter}></div>
                    <a href={`/store/apis/${this.api_uuid}/overview?environment=${Utils.getCurrentEnvironment().label}`}
                       target="_blank" title="Store" className={classes.storeButton}>
                        <sapn className={classes.storeButtonText}>View in Store</sapn> <OpenInNew />
                    </a>
            </div>
        )
    }
}

// Using `withRouter` helper from React-Router-Dom to get the current user location to be used with logout action,
// To identify which tab user is currently viewing we need to know their location information
// DOC: https://github.com/ReactTraining/react-router/blob/master/packages/react-router/docs/api/withRouter.md
export default withStyles(styles)(withRouter(NavBar));