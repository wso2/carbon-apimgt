/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import Typography from '@material-ui/core/Typography';
import Icon from '@material-ui/core/Icon';
import IconButton from '@material-ui/core/IconButton';
import Tooltip from '@material-ui/core/Tooltip';
import { FormattedMessage } from 'react-intl';
import CircularProgress from '@material-ui/core/CircularProgress';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import { ScopeValidation, resourceMethods, resourcePaths } from 'AppComponents/Shared/ScopeValidation';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import AuthManager from 'AppData/AuthManager';

/**
 * @inheritdoc
 * @param {*} theme theme object
 */
const styles = (theme) => ({
    fullHeight: {
        height: '100%',
    },
    tableRow: {
        height: theme.spacing(5),
        '& td': {
            padding: theme.spacing(0.5),
        },
    },
    appOwner: {
        pointerEvents: 'none',
    },
    appName: {
        '& a': {
            color: '#1b9ec7 !important',
        },
    },
});
const StyledTableCell = withStyles((theme) => ({
    head: {
        backgroundColor: theme.palette.common.black,
        color: theme.palette.common.white,
    },
    body: {
        fontSize: 14,
    },
    root: {
        padding: `0 0 0  ${theme.spacing(2)}px`,
    },
}))(TableCell);

const StyledTableRow = withStyles((theme) => ({
    root: {
        '&:nth-of-type(odd)': {
            backgroundColor: theme.palette.background.default,
        },
    },
}))(TableRow);
/**
 *
 *
 * @class AppsTableContent
 * @extends {Component}
 */
class AppsTableContent extends Component {
    /**
     * @inheritdoc
     */
    constructor(props) {
        super(props);
        this.state = {
            notFound: false,
        };
        this.APPLICATION_STATES = {
            CREATED: 'CREATED',
            APPROVED: 'APPROVED',
            REJECTED: 'REJECTED',
        };
    }

    /**
     * @inheritdoc
     * @memberof AppsTableContent
     */
    render() {
        const {
            apps, toggleDeleteConfirmation, classes,
        } = this.props;
        const { notFound } = this.state;
        let appsTableData = [];

        if (apps) {
            appsTableData = [...apps.values()].map((app) => {
                const appInner = app;
                appInner.deleting = false;
                return appInner;
            });
        }
        if (notFound) {
            return <ResourceNotFound />;
        }
        return (
            <TableBody className={classes.fullHeight}>
                {appsTableData
                    .map((app) => {
                        const isAppOwner = app.owner === AuthManager.getUser().name;
                        return (
                            <StyledTableRow className={classes.tableRow} key={app.applicationId}>
                                <StyledTableCell align='left' className={classes.appName}>
                                    {app.status === this.APPLICATION_STATES.APPROVED ? (
                                        <Link to={'/applications/' + app.applicationId}>{app.name}</Link>
                                    ) : (
                                        app.name
                                    )}
                                </StyledTableCell>
                                <StyledTableCell align='left'>{app.owner}</StyledTableCell>
                                <StyledTableCell align='left'>{app.throttlingPolicy}</StyledTableCell>
                                <StyledTableCell align='left'>
                                    {app.status === this.APPLICATION_STATES.APPROVED && (
                                        <Typography variant='subtitle1' component='label' gutterBottom>
                                            <FormattedMessage
                                                id='Applications.Listing.AppsTableContent.active'
                                                defaultMessage='ACTIVE'
                                            />
                                        </Typography>
                                    )}
                                    {app.status === this.APPLICATION_STATES.CREATED && (
                                        <>
                                            <Typography variant='subtitle1' component='label' gutterBottom>
                                                <FormattedMessage
                                                    id='Applications.Listing.AppsTableContent.inactive'
                                                    defaultMessage='INACTIVE'
                                                />

                                            </Typography>
                                            <Typography variant='caption'>
                                                <FormattedMessage
                                                    id='Applications.Listing.AppsTableContent.wait.approval'
                                                    defaultMessage='waiting for approval'
                                                />
                                            </Typography>
                                        </>
                                    )}
                                    {app.status === this.APPLICATION_STATES.REJECTED && (
                                        <Typography variant='subtitle1' component='label' gutterBottom>
                                            <FormattedMessage
                                                id='Applications.Listing.AppsTableContent.rejected'
                                                defaultMessage='REJECTED'
                                            />
                                        </Typography>
                                    )}
                                </StyledTableCell>
                                <StyledTableCell align='left'>{app.subscriptionCount}</StyledTableCell>
                                <StyledTableCell align='left'>
                                    <ScopeValidation
                                        resourcePath={resourcePaths.SINGLE_APPLICATION}
                                        resourceMethod={resourceMethods.PUT}
                                    >
                                        {app.status === this.APPLICATION_STATES.APPROVED && (
                                            <Tooltip title={isAppOwner
                                                ? (
                                                    <FormattedMessage
                                                        id='Applications.Listing.AppsTableContent.edit.tooltip'
                                                        defaultMessage='Edit'
                                                    />
                                                ) : (
                                                    <FormattedMessage
                                                        id='Applications.Listing.AppsTableContent.edit.tooltip.disabled.button'
                                                        defaultMessage='Not allowed to modify shared applications'
                                                    />
                                                )}
                                            >
                                                <span>
                                                    <Link
                                                        to={`/applications/${app.applicationId}/edit/`}
                                                        className={!isAppOwner && classes.appOwner}
                                                    >
                                                        <IconButton disabled={!isAppOwner} aria-label={'Edit' + app.name}>
                                                            <Icon>
                                                                edit
                                                            </Icon>
                                                        </IconButton>
                                                    </Link>
                                                </span>
                                            </Tooltip>
                                        )}
                                    </ScopeValidation>
                                    <ScopeValidation
                                        resourcePath={resourcePaths.SINGLE_APPLICATION}
                                        resourceMethod={resourceMethods.DELETE}
                                    >
                                        <Tooltip title={isAppOwner ? (
                                            <FormattedMessage
                                                id='Applications.Listing.AppsTableContent.delete.tooltip'
                                                defaultMessage='Delete'
                                            />
                                        ) : (
                                            <FormattedMessage
                                                id='Applications.Listing.AppsTableContent.delete.tooltip.disabled.button'
                                                defaultMessage='Not allowed to delete shared applications'
                                            />
                                        )}
                                        >
                                            <span>
                                                <IconButton
                                                    className='itest-application-delete-button'
                                                    disabled={app.deleting || !isAppOwner}
                                                    data-appid={app.applicationId}
                                                    onClick={toggleDeleteConfirmation}
                                                    color='default'
                                                    aria-label={'Delete' + app.name}
                                                >
                                                    <Icon>delete</Icon>
                                                </IconButton>
                                            </span>
                                        </Tooltip>
                                    </ScopeValidation>
                                    {app.deleting && <CircularProgress size={24} />}
                                </StyledTableCell>
                            </StyledTableRow>
                        );
                    })}
            </TableBody>
        );
    }
}
AppsTableContent.propTypes = {
    toggleDeleteConfirmation: PropTypes.func.isRequired,
    apps: PropTypes.instanceOf(Map).isRequired,
};
export default withStyles(styles)(AppsTableContent);
