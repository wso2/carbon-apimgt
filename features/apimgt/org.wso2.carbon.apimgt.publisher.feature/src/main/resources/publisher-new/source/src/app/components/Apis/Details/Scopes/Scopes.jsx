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
import { Progress, Alert } from 'AppComponents/Shared';
import Grid from '@material-ui/core/Grid';
import Card from '@material-ui/core/Card';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage, injectIntl } from 'react-intl';
import CardActions from '@material-ui/core/CardActions';
import CardContent from '@material-ui/core/CardContent';
import Divider from '@material-ui/core/Divider';
import Button from '@material-ui/core/Button';
import withStyles from '@material-ui/core/styles/withStyles';
import { Link } from 'react-router-dom';
import {
    List,
    ListItem,
    ListItemText,
} from '@material-ui/core';
import AddCircle from '@material-ui/icons/AddCircle';
import MUIDataTable from 'mui-datatables';
import Edit from '../Documents/Edit';
import Delete from '../Documents/Delete';

const styles = theme => ({
    buttonProgress: {
        position: 'relative',
        margin: theme.spacing.unit,
    },
    headline: { paddingTop: theme.spacing.unit * 1.25, paddingLeft: theme.spacing.unit * 2.5 },
    root: {
        width: '100%',
        maxWidth: 800,
        backgroundColor: theme.palette.background.paper,
    },
    heading: {
        flexGrow: 1,
        marginTop: 10,
    },
    titleWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
    },
    mainTitle: {
        paddingLeft: 0,
    },
    button: {
        marginLeft: theme.spacing.unit * 2,
        textTransform: theme.custom.leftMenuTextStyle,
        color: theme.palette.getContrastText(theme.palette.primary.main),
    },
    buttonIcon: {
        marginRight: 10,
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
        this.state = {
            apiScopes: null,
            apiScope: {},
            roles: [],
            scopesList: [],
        };
        this.deleteScope = this.deleteScope.bind(this);
        this.updateScope = this.updateScope.bind(this);
        this.handleInputs = this.handleInputs.bind(this);
        this.addScope = this.addScope.bind(this);
        props.api.scopes.map((scope) => {
            const aScope = [];
            const resources = [];
            aScope.push(scope.name);
            aScope.push(scope.bindings.values);
            props.api.operations.map((op) => {
                if (op.scopes.includes(scope.name)) {
                    resources.push(op.uritemplate + ' ' + op.httpVerb);
                }
                return false;
            });
            aScope.push(resources);
            this.state.scopesList.push(aScope);
            return false;
        });
    }

    /**
     * Delete scope
     * @param {any} scopeName Name of the scope need to be deleted
     * @memberof Scopes
     */
    deleteScope(scopeName) {
        const { apiScopes } = this.state;
        for (const apiScope in apiScopes) {
            if (Object.prototype.hasOwnProperty.call(apiScopes, apiScope) && apiScopes[apiScope].name === scopeName) {
                apiScopes.splice(apiScope, 1);
                break;
            }
        }
        this.setState({
            apiScopes,
        });
    }

    /**
     * Update scope
     * @param {any} scopeName Scope name to be updated
     * @param {any} scopeObj New Scope object
     * @memberof Scopes
     */
    updateScope(scopeName, scopeObj) {
        const { apiScopes } = this.state;
        for (const apiScope in apiScopes) {
            if (Object.prototype.hasOwnProperty.call(apiScopes, apiScope) && apiScopes[apiScope].name === scopeName) {
                apiScopes[apiScope].description = scopeObj.description;
                break;
            }
        }
        this.setState({
            apiScopes,
        });
    }

    /**
     * Add new scope
     * @memberof Scopes
     */
    addScope() {
        const { intl } = this.props;
        const api = new Api();
        const scope = this.state.apiScope;
        scope.bindings = {
            type: 'role',
            values: this.state.roles,
        };
        const promisedScopeAdd = api.addScope(this.props.match.params.api_uuid, scope);
        promisedScopeAdd.then((response) => {
            if (response.status !== 201) {
                console.log(response);
                Alert.error(intl.formatMessage({
                    id: 'Apis.Details.Scopes.Scope.something.went.wrong.while.updating.the.scope',
                    defaultMessage: 'Something went wrong while updating the {scopeName} Scope!',
                }, { scopeName: scope.name }));
                return;
            }
            Alert.success(intl.formatMessage({
                id: 'Apis.Details.Scopes.Scope.scope.added.successfully',
                defaultMessage: '{scopeName} Scope added successfully!',
            }, { scopeName: scope.name }));
            const { apiScopes } = this.state;
            apiScopes[apiScopes.length] = this.state.apiScope;
            this.setState({
                apiScopes,
                apiScope: {},
                roles: [],
            });
        });
    }
    /**
     * Handle api scope addition event
     * @param {any} event Button Click event
     * @memberof Scopes
     */
    handleInputs(event) {
        if (Array.isArray(event)) {
            this.setState({
                roles: event,
            });
        } else {
            const input = event.target;
            const { apiScope } = this.state;
            apiScope[input.id] = input.value;
            this.setState({
                apiScope,
            });
        }
    }

    /**
     * Render Scopes section
     * @returns {React.Component} React Component
     * @memberof Scopes
     */
    render() {
        const { intl } = this.props;
        const { api } = this.props;
        const { scopes } = api;
        const { classes } = this.props;
        const url = `/apis/${api.id}/scopes/create`;
        const columns = [
            intl.formatMessage({
                id: 'Apis.Details.Scopes.Scopes.table.header.name',
                defaultMessage: 'Name',
            }),
            {
                options: {
                    customBodyRender: (value, tableMeta) => {
                        if (tableMeta.rowData && tableMeta.rowData[1]) {
                            const roles = value || [];
                            return (
                                roles.join(',')
                            );
                        }
                        return false;
                    },
                    filter: false,
                    label: <FormattedMessage
                        id='Apis.Details.Scopes.Scopes.table.header.roles'
                        defaultMessage='Applying Roles'
                    />,
                },
            },
            {
                options: {
                    customBodyRender: (value, tableMeta) => {
                        if (tableMeta.rowData) {
                            return (
                                <List component='nav' className={classes.root}>
                                    {value.map(resource => (
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
                    label: <FormattedMessage
                        id='Apis.Details.Scopes.Scopes.table.header.usages'
                        defaultMessage='Used In'
                    />,
                },
            },
            {
                options: {
                    customBodyRender: (value, tableMeta) => {
                        if (tableMeta.rowData) {
                            const scopeName = tableMeta.rowData[1];
                            return (
                                <table className={classes.actionTable}>
                                    <tr>
                                        <td>
                                            <Edit scopeName={scopeName} apiId={this.apiId} />
                                        </td>
                                        <td>
                                            <Delete scopeName={scopeName} apiId={this.apiId} />
                                        </td>
                                    </tr>
                                </table>
                            );
                        }
                        return false;
                    },
                    filter: false,
                    label: <FormattedMessage
                        id='Apis.Details.Scopes.Scopes.table.header.actions'
                        defaultMessage='Actions'
                    />,
                },
            }];
        const options = {
            filterType: 'multiselect',
            selectableRows: false,
        };

        if (!scopes) {
            return <Progress />;
        }

        if (scopes.length === 0) {
            return (
                <Grid container justify='center'>
                    <Grid item sm={5}>
                        <Card className={classes.card}>
                            <Typography className={classes.headline} gutterBottom variant='headline' component='h2'>
                                <FormattedMessage
                                    id='Apis.Details.Scopes.Scopes.create.scopes.title'
                                    defaultMessage='Create Scopes'
                                />
                            </Typography>
                            <Divider />
                            <CardContent>
                                <Typography align='justify' component='p'>
                                    <FormattedMessage
                                        id='Apis.Details.Scopes.Scopes.scopes.enable.fine.gained.access.control'
                                        defaultMessage={'Scopes enable fine-grained access control to API resources'
                                            + ' based on user roles.'}
                                    />
                                </Typography>
                            </CardContent>
                            <CardActions>
                                <Link to={url}>
                                    <Button variant='contained' color='primary' className={classes.button}>
                                        <FormattedMessage
                                            id='Apis.Details.Scopes.Scopes.create.scopes.button'
                                            defaultMessage='Create Scopes'
                                        />
                                    </Button>
                                </Link>
                            </CardActions>
                        </Card>
                    </Grid>
                </Grid>
            );
        }

        return (
            <div className={classes.heading}>
                <div className={classes.titleWrapper}>
                    <Typography variant='h4' align='left' className={classes.mainTitle}>
                        <FormattedMessage
                            id='Apis.Details.Scopes.Scopes.heading.scope.heading'
                            defaultMessage='Scopes'
                        />
                    </Typography>
                    <Link to={url}>
                        <Button size='small' className={classes.button}>
                            <AddCircle className={classes.buttonIcon} />
                            <FormattedMessage
                                id='Apis.Details.Scopes.Scopes.heading.scope.add_new'
                                defaultMessage='Add New Scope'
                            />
                        </Button>
                    </Link>
                </div>

                <MUIDataTable
                    title={intl.formatMessage({
                        id: 'Apis.Details.Scopes.Scopes.table.scope.name',
                        defaultMessage: 'Scopes',
                    })}
                    data={this.state.scopesList}
                    columns={columns}
                    options={options}
                />


            </div>
        );
    }
}

Scopes.propTypes = {
    match: PropTypes.shape({
        params: PropTypes.object,
    }),
    api: PropTypes.instanceOf(Object).isRequired,
    classes: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({ formatMessage: PropTypes.func }).isRequired,
};

Scopes.defaultProps = {
    match: { params: {} },
};

export default injectIntl(withStyles(styles)(Scopes));
