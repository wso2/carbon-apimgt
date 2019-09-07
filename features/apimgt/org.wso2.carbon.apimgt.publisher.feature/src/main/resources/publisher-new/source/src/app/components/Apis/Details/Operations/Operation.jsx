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
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import { injectIntl } from 'react-intl';
import { Typography } from '@material-ui/core';
import Switch from '@material-ui/core/Switch';

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
    dropDown: {
        width: theme.spacing.unit * 11.25,
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

/**
 *
 */
class Operation extends React.Component {
    /**
     *
     * @param {any} props @inheritdoc
     */
    constructor(props) {
        super(props);
        this.state = {
            isSecurity: false,
        };
        this.handleScopeChange = this.handleScopeChange.bind(this);
        this.handlePolicyChange = this.handlePolicyChange.bind(this);
        this.handleChange = this.handleChange.bind(this);
    }

    /**
     *
     * @param {*} e event triggered for handle  Scope Change
     */
    handleScopeChange(e) {
        const { operation } = this.props;
        const newoperation = {
            ...operation,
            scopes: [...operation.scopes],
        };
        newoperation.scopes = [e.target.value];
        this.props.handleUpdateList(newoperation);
    }
    /**
     *
     * @param {*} e event triggered for handle  policy Change
     */
    handlePolicyChange(e) {
        const { operation } = this.props;
        const newoperation = { ...operation };
        newoperation.throttlingPolicy = e.target.value;
        this.props.handleUpdateList(newoperation);
    }

    /**
     * @param {*} event event triggered for handle  policy Change
     */
    handleChange(event) {
        const { operation } = this.props;
        const newoperation = { ...operation };
        const { checked } = event.target;
        if (checked) {
            newoperation.authType = 'Any';
        } else {
            newoperation.authType = 'None';
        }
        this.setState({
            isSecurity: checked,
        });
        this.props.handleUpdateList(newoperation);
    }

    /**
     * @inheritdoc
     */
    render() {
        const {
            operation, theme, classes, apiPolicies, scopes,
        } = this.props;
        const { isSecurity } = this.state;
        let chipColor = theme.custom.operationChipColor ?
            theme.custom.operationChipColor[operation.verb.toLowerCase()]
            : null;
        let chipTextColor = '#000000';
        if (!chipColor) {
            console.log('Check the theme settings. The resourceChipColors is not populated properlly');
            chipColor = '#cccccc';
        } else {
            chipTextColor =
            theme.palette.getContrastText(theme.custom.operationChipColor[operation.verb.toLowerCase()]);
        }
        return (
            <TableRow style={{ borderStyle: 'hidden' }}>
                <TableCell>
                    <Typography variant='body1'>
                        {operation.target}
                    </Typography>
                </TableCell>
                <TableCell>
                    <Chip
                        label={operation.verb.toLowerCase()}
                        style={{
                            backgroundColor: chipColor, color: chipTextColor, height: 20, width: 40,
                        }}
                        className={classes.chipActive}
                    />
                </TableCell>
                <TableCell>
                    <Select
                        className={classes.dropDown}
                        value={operation.throttlingPolicy}
                        onChange={this.handlePolicyChange}
                        fieldName='Throttling Policy'
                    >
                        {apiPolicies.map(policy => (
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
                        className={classes.dropDown}
                        value={operation.scopes}
                        onChange={this.handleScopeChange}
                        inputProps={{
                            name: 'scopes',
                            id: 'age-simple',
                        }}
                    >
                        {scopes.map(tempScope => (
                            <MenuItem
                                key={tempScope.name}
                                value={tempScope.name}
                            >
                                {tempScope.name}
                            </MenuItem>
                        ))}
                    </Select>
                </TableCell>
                <TableCell>
                    <Switch
                        checked={(() => {
                            if (operation.authType === 'None') {
                                return false;
                            }
                            return true;
                        })()}
                        onChange={this.handleChange}
                        value={isSecurity}
                        color='primary'
                    />
                </TableCell>
            </TableRow>
        );
    }
}

Operation.propTypes = {
    classes: PropTypes.shape({
    }).isRequired,
    operation: PropTypes.shape({
        target: PropTypes.string,
        verb: PropTypes.string,
        throttlingPolicy: PropTypes.string,
        auth: PropTypes.string,
    }).isRequired,
    apiPolicies: PropTypes.shape({
    }).isRequired,
    scopes: PropTypes.shape({
    }).isRequired,
    handleUpdateList: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({
        formatMessage: PropTypes.func,
    }).isRequired,
};

export default injectIntl(withStyles(styles, { withTheme: true })(Operation));
