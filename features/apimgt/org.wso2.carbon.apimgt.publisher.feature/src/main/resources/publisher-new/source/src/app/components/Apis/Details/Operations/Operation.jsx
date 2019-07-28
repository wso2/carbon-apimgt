/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React from 'react';
import Select from '@material-ui/core/Select';
import MenuItem from '@material-ui/core/MenuItem';
import Chip from '@material-ui/core/Chip';
import { withStyles } from '@material-ui/core/styles';
import PropTypes from 'prop-types';
import Table from '@material-ui/core/Table';
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import Grid from '@material-ui/core/Grid';
import { FormattedMessage, injectIntl } from 'react-intl';
import { Typography } from '@material-ui/core';

const styles = theme => ({
    root: {
        flexGrow: 1,
        marginTop: 10,
    },
    container: {
        display: 'flex',
        flexWrap: 'wrap',
    },
    textField: {
        marginLeft: theme.spacing.unit,
        marginRight: theme.spacing.unit,
        width: 400,
    },
    mainTitle: {
        paddingLeft: 20,
    },
    scopes: {
        width: 400,
    },
    divider: {
        marginTop: 20,
        marginBottom: 20,
    },
    chip: {
        margin: theme.spacing.unit,
        color: theme.palette.text.secondary,
        minWidth: 100,
    },
    chipActive: {
        margin: theme.spacing.unit,
        color: theme.palette.text.secondary,
        background: theme.palette.background.active,
        minWidth: 100,
        borderRadius: theme.shape.borderRadius,
        cursor: 'pointer',
    },
    paper: {
        padding: 20,
    },
    link: {
        cursor: 'pointer',
    },
    listItem: {
        paddingLeft: 0,
        display: 'flex',
        alignItems: 'center',
    },
    formControl: {
        paddingRight: 0,
        marginRight: 0,
    },
    resourceRoot: {
        background: theme.palette.grey['100'],
        paddingLeft: theme.spacing.unit,
        paddingRight: theme.spacing.unit,
        borderRadius: theme.shape.borderRadius,
        marginBottom: theme.spacing.unit,
    },
    deleteButton: {
        marginLeft: 'auto',
    },
    pathDisplay: {
        marginRight: theme.spacing.unit * 2,
        marginLeft: theme.spacing.unit * 2,
    },
    descriptionWrapper: {
        paddingTop: theme.spacing.unit,
        paddingBottom: theme.spacing.unit,
    },
    scopeSelect: {
        width: '100%',
    },
    descriptionWrapperUp: {
        paddingBottom: '0 !important',
    },
    addParamRow: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
    },
    propsForm: {
        display: 'flex',
        alignItems: 'center',
    },
    deleteLink: {
        cursor: 'pointer',
    },
    row: {
        '& td': {
            borderBottom: 'none',
            verticalAlign: 'bottom',
            width: '33%',
            paddingLeft: 0,
        },
    },
});

class Operation extends React.Component {
    /**
     *
     * @param {any} props @inheritdoc
     */
    constructor(props) {
        super(props);
        this.state = {
            visible: false,
        };
        this.toggleMethodData = this.toggleMethodData.bind(this);
        this.handleScopeChange = this.handleScopeChange.bind(this);
        this.handlePolicyChange = this.handlePolicyChange.bind(this);
    }

    handleMethodChangeInSwaggerRoot(key, value) {
        const tempMethod = this.state.method;
        tempMethod[key] = value;
        this.setState({ method: tempMethod });
        this.props.updatePath(this.props.operation.target, this.props.operation.verb, this.state.method);
    }


    toggleMethodData() {
        this.setState({ visible: !this.state.visible });
    }
    handleScopeChange(e) {
        const operation = JSON.parse(JSON.stringify(this.props.operation));
        operation.scopes = [e.target.value];
        this.props.handleUpdateList(operation);
    }
    handlePolicyChange(e) {
        const operation = JSON.parse(JSON.stringify(this.props.operation));
        operation.throttlingPolicy = e.target.value;
        this.props.handleUpdateList(operation);
    }
    /**
     * @inheritdoc
     */
    render() {
        const {
            operation, theme, classes, intl, apiPolicies,
        } = this.props;
        let chipColor = theme.custom.resourceChipColors ? theme.custom.resourceChipColors[operation.verb] : null;
        const chipTextColor = '#000000';
        if (!chipColor && operation.verb === 'QUERY') {
            chipColor = '#EF8B27';
        } else if (!chipColor && operation.verb === 'MUTATION') {
            chipColor = '#EFEF27';
        } else if (!chipColor && operation.verb === 'SUBSCRIPTION') {
            chipColor = '#27EFA3';
        }
        return (
            <div className={classes.resourceRoot}>
                <div className={classes.listItem}>
                    <a onClick={this.toggleMethodData} className={classes.link}>
                        <Chip label={operation.verb} style={{ backgroundColor: chipColor, color: chipTextColor }} className={classes.chipActive} />
                    </a>
                    <a onClick={this.toggleMethodData}>
                        <Typography variant='h6' className={classes.pathDisplay}>
                            {operation.target}
                        </Typography>
                    </a>
                </div>
                {this.state.visible && (
                    <div>
                        <Grid container spacing={12}>
                            <Grid item xs={12}>
                                <Table>
                                    <TableRow className={classes.row}>
                                        <TableCell>
                                            <Typography variant='subtitle2'>
                                                <FormattedMessage
                                                    id='Apis.Details.Resources.Resource.throttling.policy'
                                                    defaultMessage='Throttling Policy'
                                                />
                                            </Typography>
                                        </TableCell>
                                        <TableCell>
                                            <Typography variant='subtitle2'>
                                                <FormattedMessage
                                                    id='Apis.Details.Resources.Resource.scopes'
                                                    defaultMessage='Scopes'
                                                />
                                            </Typography>
                                        </TableCell>
                                    </TableRow>
                                    <TableRow className={classes.row}>
                                        <TableCell>
                                            <Select
                                                value={operation.throttlingPolicy}
                                                onChange={this.handlePolicyChange}
                                                fieldName='Throttling Policy'
                                            >
                                                {this.props.apiPolicies.map(policy => (
                                                    <MenuItem
                                                        key={policy.name}
                                                        value={policy.name}
                                                    >
                                                        {policy.displayName}
                                                    </MenuItem>
                                                ))}
                                            </Select>
                                        </TableCell>
                                        <TableCell>
                                            <Select
                                                value={operation.scopes}
                                                onChange={this.handleScopeChange}
                                                inputProps={{
                                                    name: 'scopes',
                                                    id: 'age-simple',
                                                }}
                                            >
                                                {this.props.scopes.map(tempScope => (
                                                    <MenuItem
                                                        key={tempScope.name}
                                                        value={tempScope.name}
                                                    >
                                                        {tempScope.name}
                                                    </MenuItem>
                                                ))}
                                            </Select>
                                        </TableCell>
                                    </TableRow>
                                </Table>
                            </Grid>
                        </Grid>
                    </div>
                )}
            </div>
        );
    }
}

Operation.propTypes = {
    classes: PropTypes.object.isRequired,
    theme: PropTypes.object.isRequired,
};

export default injectIntl(withStyles(styles, { withTheme: true })(Operation));
