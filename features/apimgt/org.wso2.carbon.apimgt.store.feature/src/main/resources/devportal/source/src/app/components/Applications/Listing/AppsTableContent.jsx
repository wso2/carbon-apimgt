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

/**
 *
 * @param {*} order order
 * @param {*} orderBy orderby
 * @returns {Boolean}
 */
function getSorting(order, orderBy) {
    return order === 'desc' ? (a, b) => (b[orderBy] < a[orderBy] ? -1 : 1)
        : (a, b) => (a[orderBy] < b[orderBy] ? -1 : 1);
}
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
            apps, toggleDeleteConfirmation,
        } = this.props;
        const { notFound } = this.state;
        let appsTableData = [];

        if (apps) {
            appsTableData = [...apps.values()].map((app) => {
                app.deleting = false;
                return app;
            });
        }
        if (notFound) {
            return <ResourceNotFound />;
        }
        return (
            <TableBody>
                {appsTableData
                    .map((app) => {
                        return (
                            <TableRow key={app.applicationId}>
                                <TableCell align='left'>
                                    {app.status === this.APPLICATION_STATES.APPROVED ? (
                                        <Link to={'/applications/' + app.applicationId}>{app.name}</Link>
                                    ) : (
                                        app.name
                                    )
                                    }
                                </TableCell>
                                <TableCell align='left'>{app.owner}</TableCell>
                                <TableCell align='left'>{app.throttlingPolicy}</TableCell>
                                <TableCell align='left'>
                                    {app.status === this.APPLICATION_STATES.APPROVED && (
                                        <Typography variant='subtitle1' gutterBottom>
                                            <FormattedMessage
                                                id='Applications.Listing.AppsTableContent.active'
                                                defaultMessage='ACTIVE'
                                            />
                                        </Typography>
                                    )}
                                    {app.status === this.APPLICATION_STATES.CREATED && (
                                        <React.Fragment>
                                            <Typography variant='subtitle1' gutterBottom>
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
                                        </React.Fragment>
                                    )}
                                    {app.status === this.APPLICATION_STATES.REJECTED && (
                                        <Typography variant='subtitle1' gutterBottom>
                                            <FormattedMessage
                                                id='Applications.Listing.AppsTableContent.rejected'
                                                defaultMessage='REJECTED'
                                            />
                                        </Typography>
                                    )}
                                </TableCell>
                                <TableCell align='left'>{app.subscriptionCount}</TableCell>
                                <TableCell align='left'>
                                    <ScopeValidation
                                        resourcePath={resourcePaths.SINGLE_APPLICATION}
                                        resourceMethod={resourceMethods.PUT}
                                    >
                                        {app.status === this.APPLICATION_STATES.APPROVED && (
                                            <Tooltip title='Edit'>
                                                <Link to={`/applications/${app.applicationId}/edit/`}>
                                                    <IconButton>
                                                        <Icon aria-label={(
                                                            <FormattedMessage
                                                                id='Applications.Listing.AppsTableContent.edit.btn'
                                                                defaultMessage='Edit'
                                                            />
                                                        )}
                                                        >
                                                            edit
                                                        </Icon>
                                                    </IconButton>
                                                </Link>
                                            </Tooltip>
                                        )}
                                    </ScopeValidation>
                                    <ScopeValidation
                                        resourcePath={resourcePaths.SINGLE_APPLICATION}
                                        resourceMethod={resourceMethods.DELETE}
                                    >
                                        <Tooltip title={(
                                            <FormattedMessage
                                                id='Applications.Listing.AppsTableContent.delete.tooltip'
                                                defaultMessage='Delete'
                                            />
                                        )}
                                        >
                                            <IconButton
                                                disabled={app.deleting}
                                                data-appId={app.applicationId}
                                                onClick={toggleDeleteConfirmation}
                                                color='default'
                                                aria-label={(
                                                    <FormattedMessage
                                                        id='Applications.Listing.AppsTableContent.delete.label'
                                                        defaultMessage='Delete'
                                                    />
                                                )}
                                            >
                                                <Icon>delete</Icon>
                                            </IconButton>
                                        </Tooltip>
                                    </ScopeValidation>
                                    {app.deleting && <CircularProgress size={24} />}
                                </TableCell>
                            </TableRow>
                        );
                    })}
            </TableBody>
        );
    }
}
AppsTableContent.propTypes = {
    toggleDeleteConfirmation: PropTypes.func.isRequired,
    page: PropTypes.number.isRequired,
    rowsPerPage: PropTypes.number.isRequired,
    order: PropTypes.string.isRequired,
    orderBy: PropTypes.string.isRequired,
    apps: PropTypes.instanceOf(Map).isRequired,
};
export default AppsTableContent;
