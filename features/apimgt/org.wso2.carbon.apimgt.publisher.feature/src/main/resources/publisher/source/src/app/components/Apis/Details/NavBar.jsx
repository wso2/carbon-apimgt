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
import { Link, withRouter } from 'react-router-dom';
import Button from 'material-ui/Button';
import {
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
    ArrowDropDown,
    OpenInNew,
} from '@material-ui/icons/';
import Menu, { MenuItem } from 'material-ui/Menu';
import Grow from 'material-ui/transitions/Grow';
import classNames from 'classnames';
import Paper from 'material-ui/Paper';
import Typography from 'material-ui/Typography';
import { Manager, Target, Popper } from 'react-popper';
import ClickAwayListener from 'material-ui/utils/ClickAwayListener';
import { withStyles } from 'material-ui/styles';
import blue from 'material-ui/colors/blue';
import PropTypes from 'prop-types';

import Utils from '../../../data/Utils';
import Api from '../../../data/api';
import DeleteApi from './DeleteApi';

const styles = {
    root: {
        display: 'flex',
        alignItems: 'center',
        flexWrap: 'wrap',
        marginBottom: 20,
    },
    active: {
        backgroundColor: blue[500],
    },
    drawerPaper: {
        position: 'relative',
        height: '100%',
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
    },
};

/**
 * Handles the API details Navigation Bar in top of the Details page
 * @class NavBar
 * @extends {Component}
 */
class NavBar extends Component {
    /**
     * Creates an instance of NavBar.
     * @param {any} props @inheritDoc
     * @memberof NavBar
     */
    constructor(props) {
        super(props);
        this.state = {
            api: null,
            openMore: false,
            windowWidth: 0,
            anchorEl: null,
        };
        this.updateWindowSize = this.updateWindowSize.bind(this);
    }

    /**
     * @inheritDoc
     * @memberof NavBar
     */
    componentDidMount() {
        const api = new Api();
        const { apiUUID } = this.props;
        const promisedApi = api.get(apiUUID);
        promisedApi
            .then((response) => {
                this.setState({ api: response.obj });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.error(error);
                }
            });
        this.updateWindowSize();
        window.addEventListener('resize', this.updateWindowSize);
    }

    /**
     * @inheritDoc
     * @memberof NavBar
     */
    componentWillUnmount() {
        clearTimeout(this.timeout);
        window.removeEventListener('resize', this.updateWindowSize);
    }

    handleClickMore = (event) => {
        this.setState({ openMore: !this.state.openMore, anchorEl: event.currentTarget });
    };

    handleCloseMore = () => {
        this.setState({ anchorEl: null, openMore: false });
    };

    /**
     * Update inner width when resizing window
     * @memberof NavBar
     */
    updateWindowSize() {
        this.setState({ windowWidth: window.innerWidth });
    }

    /**
     * @inheritDoc
     * @returns {React.Component} Render NavBar component
     * @memberof NavBar
     */
    render() {
        /* TODO: This could have been done easily with match object containing apiUUID value , But
         Due to a bug (https://github.com/ReactTraining/react-router/issues/4649) in the latest version(4.1.1),
         it's not working as expected, Hence doing this hack, revert to following with react-router upgrade
         const apiUUID = this.props.match.params.apiUUID; ~tmkb */
        const pathSegments = this.props.location.pathname.split('/');
        // This assume that last segment and segment before it contains detail page action and API UUID
        const [activeTab, apiUUID] = pathSegments.reverse();
        const { classes } = this.props;
        const {
            openMore, api, windowWidth, anchorEl,
        } = this.state;

        const tabs = [
            { name: 'lifecycle', icon: <DonutLarge className={classes.icon} />, important: true },
            { name: 'endpoints', icon: <Games className={classes.icon} />, important: true },
            { name: 'resources', icon: <FilterNone className={classes.icon} />, important: true },
            { name: 'scopes', icon: <FilterNone className={classes.icon} /> },
            { name: 'documents', icon: <InsertDriveFile className={classes.icon} /> },
            { name: 'permission', icon: <LockOutline className={classes.icon} /> },
            { name: 'mediation', icon: <Tune className={classes.icon} /> },
            { name: 'scripting', icon: <Code className={classes.icon} /> },
            { name: 'subscriptions', icon: <Subscriptions className={classes.icon} /> },
            { name: 'security', icon: <VerifiedUser className={classes.icon} /> },
        ];
        if (Utils.isMultiEnvironmentOverviewEnabled()) {
            tabs.splice(1, 0, { name: 'environment view', icon: <Description /> });
        }

        function highlightActiveTab(tab) {
            return tab === activeTab ? 'primary' : 'default';
        }

        function showMoreActiveTab() {
            let activeMoreElement = '';
            for (let i = 0; i < tabs.length; i++) {
                if (tabs[i].name === activeTab && !tabs[i].important) {
                    activeMoreElement = ' ( ' + activeTab + ' ) ';
                }
            }
            return activeMoreElement;
        }
        return (
            <div className={classes.root}>
                <Link to={'/apis/' + apiUUID + '/overview'}>
                    <Typography variant='display1' gutterBottom className={classes.heading}>
                        {api && <span>{api.name}</span>}
                    </Typography>
                </Link>
                {tabs.map(tab =>
                    tab.important && (
                        <Link key={tab.name} name={tab.name} to={'/apis/' + apiUUID + '/' + tab.name}>
                            <Button
                                key={tab.name}
                                variant='raised'
                                size='small'
                                color={highlightActiveTab(tab.name)}
                                className={classes.button}
                            >
                                {tab.icon} {windowWidth > 1300 && <span>{tab.name}</span>}
                            </Button>
                        </Link>
                    ))}
                {api && <DeleteApi api={api} />}
                <Manager className={classes.manager}>
                    <Target>
                        <Button
                            aria-owns={openMore ? 'menu-list' : null}
                            aria-haspopup='true'
                            onClick={this.handleClickMore}
                            variant='raised'
                            size='small'
                            className={classes.moreButton}
                            color={showMoreActiveTab() === '' ? 'default' : 'primary'}
                        >
                            <ArrowDropDown />
                            More
                            <div className={classes.activeMoreTab}>{showMoreActiveTab()}</div>
                        </Button>
                    </Target>
                    <Popper
                        placement='bottom-start'
                        eventsEnabled={openMore}
                        className={classNames({ [classes.popperClose]: !openMore })}
                    >
                        <ClickAwayListener onClickAway={this.handleCloseMore}>
                            <Grow in={openMore} id='menu-list' style={{ transformOrigin: '0 0 0' }}>
                                <Paper>
                                    <Menu anchorEl={anchorEl} open={openMore} onClose={this.handleCloseMore}>
                                        {tabs.map(tab =>
                                            !tab.important && (
                                                <Link
                                                    key={tab.name}
                                                    name={tab.name}
                                                    to={'/apis/' + apiUUID + '/' + tab.name}
                                                >
                                                    <MenuItem
                                                        key={tab.name}
                                                        size='small'
                                                        color={highlightActiveTab(tab.name)}
                                                        className={classes.button}
                                                    >
                                                        {tab.icon} {tab.name}
                                                    </MenuItem>
                                                </Link>
                                            ))}
                                    </Menu>
                                </Paper>
                            </Grow>
                        </ClickAwayListener>
                    </Popper>
                </Manager>
                <div className={classes.contentShifter} />
                <a
                    href={`/store/apis/${apiUUID}/overview?environment=${Utils.getCurrentEnvironment().label}`}
                    target='_blank'
                    title='Store'
                    className={classes.storeButton}
                >
                    <span className={classes.storeButtonText}>View in Store</span> <OpenInNew />
                </a>
            </div>
        );
    }
}

NavBar.propTypes = {
    location: PropTypes.shape({
        pathname: PropTypes.string,
    }).isRequired,
    apiUUID: PropTypes.string.isRequired,
    classes: PropTypes.shape({}).isRequired,
};

// Using `withRouter` helper from React-Router-Dom to get the current user location to be used with logout action,
// To identify which tab user is currently viewing we need to know their location information
// DOC: https://github.com/ReactTraining/react-router/blob/master/packages/react-router/docs/api/withRouter.md
export default withStyles(styles)(withRouter(NavBar));
