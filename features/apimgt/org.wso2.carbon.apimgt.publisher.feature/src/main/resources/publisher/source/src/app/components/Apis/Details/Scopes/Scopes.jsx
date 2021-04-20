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

import 'react-tagsinput/react-tagsinput.css';
import PropTypes from 'prop-types';
import React from 'react';
import Api from 'AppData/api';
import { Progress } from 'AppComponents/Shared';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage, injectIntl } from 'react-intl';
import Button from '@material-ui/core/Button';
import withStyles from '@material-ui/core/styles/withStyles';
import { Link } from 'react-router-dom';
import { List, ListItem, ListItemText } from '@material-ui/core';
import AddCircle from '@material-ui/icons/AddCircle';
import MUIDataTable from 'mui-datatables';
import Icon from '@material-ui/core/Icon';
import InlineMessage from 'AppComponents/Shared/InlineMessage';
import Grid from '@material-ui/core/Grid';
import { isRestricted } from 'AppData/AuthManager';
import { withAPI } from 'AppComponents/Apis/Details/components/ApiContext';
import HelpOutlineIcon from '@material-ui/icons/HelpOutline';
import IconButton from '@material-ui/core/IconButton';
import Tooltip from '@material-ui/core/Tooltip';

import Delete from './Delete';

const styles = (theme) => ({
    root: {
        paddingTop: 0,
        paddingLeft: 0,
    },
    buttonProgress: {
        position: 'relative',
        margin: theme.spacing(1),
    },
    headline: { paddingTop: theme.spacing(1.25), paddingLeft: theme.spacing(2.5) },
    heading: {
        flexGrow: 1,
        marginTop: 10,
        '& table td:nth-child(2)': {
            'word-break': 'break-word',
        },
        '& table td button span, & table th': {
            'white-space': 'nowrap',
        },
    },
    titleWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: theme.spacing(2),
    },
    mainTitle: {
        paddingLeft: 0,
    },
    buttonIcon: {
        marginRight: theme.spacing(1),
    },
    content: {
        margin: `${theme.spacing(2)}px 0 ${theme.spacing(2)}px 0`,
    },
    head: {
        fontWeight: 200,
    },
});
/**
 * Generate the scopes UI in API details page.
 * @class Scopes
 * @extends {React.Component}
 */
class Scopes extends React.Component {
    /**
     * Creates an instance of Scopes.
     * @param {any} props Generic props
     * @memberof Scopes
     */
    constructor(props) {
        super(props);
        this.api = new Api();
        this.api_uuid = props.match.params.api_uuid;
        this.api_data = props.api;
    }

    /**
     * Render Scopes section
     * @returns {React.Component} React Component
     * @memberof Scopes
     */
    render() {
        const {
            intl, classes, api,
        } = this.props;
        const urlPrefix = (api.apiType === Api.CONSTS.APIProduct) ? 'api-products' : 'apis';
        const { scopes } = api;
        const url = `/${urlPrefix}/${api.id}/scopes/create`;
        const editUrl = `/${urlPrefix}/${api.id}/scopes/edit`;
        const columns = [
            intl.formatMessage({
                id: 'Apis.Details.Scopes.Scopes.table.header.name',
                defaultMessage: 'Name',
            }),
            intl.formatMessage({
                id: 'Apis.Details.Scopes.Scopes.table.header.description',
                defaultMessage: 'Description',
            }),
            {
                options: {
                    customBodyRender: (value, tableMeta) => {
                        if (tableMeta.rowData) {
                            const roles = value || [];
                            return roles.join(',');
                        }
                        return false;
                    },
                    filter: false,
                    sort: false,
                    label: (
                        <FormattedMessage
                            id='Apis.Details.Scopes.Scopes.table.header.roles'
                            defaultMessage='Roles'
                        />
                    ),
                },
            },
            {
                options: {
                    customBodyRender: (value, tableMeta) => {
                        if (value && tableMeta.rowData) {
                            return (
                                <List component='nav' className={classes.root}>
                                    {value.map((resource) => (
                                        <ListItem button>
                                            <ListItemText primary={resource} />
                                        </ListItem>
                                    ))}
                                </List>
                            );
                        }
                        return false;
                    },
                    filter: false,
                    sort: false,
                    label: (
                        <FormattedMessage
                            id='Apis.Details.Scopes.Scopes.table.header.usages'
                            defaultMessage='Used In'
                        />
                    ),
                },
            },
            {
                options: {
                    customBodyRender: (value, tableMeta) => {
                        if (tableMeta.rowData) {
                            const scopeName = tableMeta.rowData[0];
                            return (
                                <table className={classes.actionTable}>
                                    <tr>
                                        <td>
                                            <Link
                                                to={
                                                    !isRestricted(['apim:api_create'], api) && !api.isRevision && {
                                                        pathname: editUrl,
                                                        state: {
                                                            scopeName,
                                                        },
                                                    }
                                                }
                                            >
                                                <Button
                                                    disabled={isRestricted(
                                                        ['apim:api_create'],
                                                        api,
                                                    ) || api.isRevision}
                                                >
                                                    <Icon>edit</Icon>
                                                    <FormattedMessage
                                                        id='Apis.Details.scopes.Edit.text.editor.edit'
                                                        defaultMessage='Edit'
                                                    />
                                                </Button>
                                            </Link>
                                        </td>
                                        <td>
                                            <Delete scopeName={scopeName} api={api} isAPIProduct />
                                        </td>
                                    </tr>
                                </table>
                            );
                        }
                        return false;
                    },
                    filter: false,
                    sort: false,
                    label: (
                        <FormattedMessage
                            id='Apis.Details.Scopes.Scopes.table.header.actions'
                            defaultMessage='Actions'
                        />
                    ),
                },
            },
        ];
        const options = {
            filterType: 'multiselect',
            selectableRows: false,
            title: false,
            filter: false,
            sort: false,
            print: false,
            download: false,
            viewColumns: false,
            customToolbar: false,
        };

        const scopesList = api.scopes.filter((apiScope) => {
            return !apiScope.shared;
        }).map((apiScope) => {
            const aScope = [];
            aScope.push(apiScope.scope.name);
            aScope.push(apiScope.scope.description);
            aScope.push(apiScope.scope.bindings);
            const resources = api.operations && api.operations
                .filter((op) => {
                    return op.scopes.includes(apiScope.scope.name);
                })
                .map((op) => {
                    return op.target + ' ' + op.verb;
                });
            aScope.push(resources);
            return aScope;
        });

        if (!scopes) {
            return <Progress />;
        }

        if (scopes.length === 0) {
            return (
                <div className={classes.root}>
                    <div className={classes.titleWrapper}>
                        <Typography
                            id='itest-api-details-scopes-onboarding-head'
                            variant='h4'
                            align='left'
                            className={classes.mainTitle}
                        >
                            <FormattedMessage
                                id='Apis.Details.local.Scopes.heading.scope.heading'
                                defaultMessage='Local Scopes'
                            />
                        </Typography>
                        <Tooltip
                            title={(
                                <FormattedMessage
                                    id='Apis.Details.Scopes.Scopes.heading.scope.title.tooltip'
                                    defaultMessage='Manage scopes that are local to this API'
                                />
                            )}
                            placement='top-end'
                            aria-label='Local Scopes'
                        >
                            <IconButton size='small' aria-label='delete'>
                                <HelpOutlineIcon fontSize='small' />
                            </IconButton>
                        </Tooltip>
                    </div>
                    <InlineMessage type='info' height={140}>
                        <div className={classes.contentWrapper}>
                            <Typography variant='h5' component='h3' className={classes.head}>
                                <FormattedMessage
                                    id='Apis.Details.Scopes.Scopes.create.scopes.title'
                                    defaultMessage='Create API Local Scopes'
                                />
                            </Typography>
                            <Typography component='p' className={classes.content}>
                                <FormattedMessage
                                    id='Apis.Details.Scopes.Scopes.scopes.enable.fine.gained.access.control'
                                    defaultMessage={
                                        'Scopes enable fine-grained access control to API resources'
                                        + ' based on user roles.'
                                    }
                                />
                            </Typography>
                            <div className={classes.actions}>
                                <Link to={!isRestricted(['apim:api_create'], api) && !api.isRevision && url}>
                                    <Button
                                        variant='contained'
                                        color='primary'
                                        className={classes.button}
                                        disabled={isRestricted(['apim:api_create'], api) || api.isRevision}
                                    >
                                        <FormattedMessage
                                            id='Apis.Details.Scopes.Scopes.create.scopes.button'
                                            defaultMessage='Create Scopes'
                                        />
                                    </Button>
                                </Link>
                            </div>
                        </div>
                    </InlineMessage>
                </div>
            );
        }

        return (
            <div className={classes.heading}>
                <div className={classes.titleWrapper}>
                    <Typography variant='h4' align='left' className={classes.mainTitle}>
                        <FormattedMessage
                            id='Apis.Details.local.Scopes.heading.edit.heading'
                            defaultMessage='Local Scopes'
                        />
                    </Typography>
                    <Tooltip
                        title={(
                            <FormattedMessage
                                id='Apis.Details.Scopes.Scopes.heading.scope.title.tooltip2'
                                defaultMessage='Manage scopes that are local to this API'
                            />
                        )}
                        placement='top-end'
                        aria-label='Local Scopes'
                    >
                        <IconButton size='small' aria-label='delete'>
                            <HelpOutlineIcon fontSize='small' />
                        </IconButton>
                    </Tooltip>
                    <Link to={!isRestricted(['apim:api_create'], api) && !api.isRevision && url}>
                        <Button
                            variant='outlined'
                            color='primary'
                            size='small'
                            disabled={isRestricted(['apim:api_create'], api) || api.isRevision}
                        >
                            <AddCircle className={classes.buttonIcon} />
                            <FormattedMessage
                                id='Apis.Details.Scopes.Scopes.heading.scope.add_new'
                                defaultMessage='Add New Local Scope'
                            />
                        </Button>
                    </Link>
                    {isRestricted(['apim:api_create'], api) && (
                        <Grid item>
                            <Typography variant='body2' color='primary'>
                                <FormattedMessage
                                    id='Apis.Details.Scopes.Scopes.update.not.allowed'
                                    defaultMessage={
                                        '*You are not authorized to update scopes of'
                                        + ' the API due to insufficient permissions'
                                    }
                                />
                            </Typography>
                        </Grid>
                    )}
                </div>

                <MUIDataTable title={false} data={scopesList} columns={columns} options={options} />
            </div>
        );
    }
}

Scopes.propTypes = {
    match: PropTypes.shape({
        params: PropTypes.shape({}),
    }),
    api: PropTypes.instanceOf(Object).isRequired,
    classes: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({ formatMessage: PropTypes.func }).isRequired,
};

Scopes.defaultProps = {
    match: { params: {} },
};

export default injectIntl(withAPI(withStyles(styles)(Scopes)));
