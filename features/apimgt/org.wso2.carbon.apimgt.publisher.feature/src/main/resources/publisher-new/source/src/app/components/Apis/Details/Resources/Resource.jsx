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
import ListItem from '@material-ui/core/ListItem';
import ListItemText from '@material-ui/core/ListItemText';
import ListItemSecondaryAction from '@material-ui/core/ListItemSecondaryAction';
import { withStyles } from '@material-ui/core/styles';
import Paper from '@material-ui/core/Paper';
import PropTypes from 'prop-types';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Delete from '@material-ui/icons/Delete';
import Grid from '@material-ui/core/Grid';
import Button from '@material-ui/core/Button';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Checkbox from '@material-ui/core/Checkbox';
import { Typography } from '@material-ui/core';
import TextField from '@material-ui/core/TextField';
import classNames from 'classnames';
import DeleteIcon from '@material-ui/icons/Delete';
import SelectContentType from './SelectContentType';
import InlineEditableField from './InlineEditableField';
import { FormattedMessage, injectIntl } from 'react-intl';

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
    selectWidth: {
        margin: theme.spacing.unit * 1,
        minWidth: 120,
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

class Resource extends React.Component {
    /**
     *
     * @param {any} props @inheritdoc
     */
    constructor(props) {
        super(props);
        let tempScopes = [];
        if (this.props.methodData.security && this.props.methodData.security.length !== 0) {
            this.props.methodData.security.map((object, i) => {
                if (object.OAuth2Security) {
                    tempScopes = object.OAuth2Security;
                }
            });
        }
        this.state = {
            visible: false,
            method: this.props.methodData,
            scopes: null,
            deleteChecked: false,
            newPropName: '',
        };
        this.propsSubmitHandler = this.propsSubmitHandler.bind(this);
        this.saveFieldCallback = this.saveFieldCallback.bind(this);
        this.toggleMethodData = this.toggleMethodData.bind(this);
        this.deleteResource = this.deleteResource.bind(this);
        this.handleScopeChange = this.handleScopeChange.bind(this);
        this.changeContentTypes = this.changeContentTypes.bind(this);
        this.handlePolicyChange = this.handlePolicyChange.bind(this);
        this.handleAuthTypeChange = this.handleAuthTypeChange.bind(this);
    }
    componentDidMount() {
        this.props.onRef(this);
    }
    componentWillUnmount() {
        this.props.onRef(undefined);
    }
    handleScopeChange(e) {
        this.handleMethodChangeInSwaggerRoot('x-scope', e.target.value);
    }
    handlePolicyChange(e)  {
        this.handleMethodChangeInSwaggerRoot('x-throttling-tier', e.target.value);
    }
    handleAuthTypeChange(e) {
        this.handleMethodChangeInSwaggerRoot('x-auth-type', e.target.value);
    }
    handleMethodChangeInSwaggerRoot(key, value) {
        const tempMethod = this.state.method;
        tempMethod[key] = value;
        this.setState({ method: tempMethod });
        this.props.updatePath(this.props.path, this.props.method, this.state.method);
    }
    propsSubmitHandler(e) {
        e.preventDefault();
        const defaultParams = {
            name: this.state.newPropName,
            description: '',
            required: false,
            in: 'query',
            type: 'string',
        };
        if(!this.state.method.parameters) {
            this.state.method.parameters = [];
        }
        this.state.method.parameters.push(defaultParams);
        this.props.updatePath(this.props.path, this.props.method, this.state.method);
    }
    onChangePropName = (e) => {
        this.setState({ newPropName: e.target.value });
    };
    deleteParam(i) {
        if (i > -1) {
            this.setState((state, props) => {
                const method = JSON.parse(JSON.stringify(state.method));
                method.parameters.splice(i, 1);
                props.updatePath(props.path, props.method, method);
                return { method };
            });
        }
    }
    saveFieldCallback(fieldName, fieldValue, fieldIndex) {
        if (fieldName.indexOf('.') !== -1) {
            const multiLevelFieldName = fieldName.split('param.')[1];
            const tmpPath = this.state.method;
            tmpPath.parameters[fieldIndex][multiLevelFieldName] = fieldValue;
            this.setState({ method: tmpPath });
        } else {
            const tmpPath = this.state.method;
            tmpPath[fieldName] = fieldValue;
            this.setState({ method: tmpPath });
        }
        this.props.updatePath(this.props.path, this.props.method, this.state.method);
    }
    toggleMethodData() {
        this.setState({ visible: !this.state.visible });
    }
    deleteResource() {
        /* We set null and call the update method of the Resources class */
        this.props.updatePath(this.props.path, this.props.method, null);
    }
    toggleDeleteCheck(checkState) {
        this.setState({ deleteChecked: checkState });
        this.forceUpdate();
    }
    handleDeleteCheck = (path, method) => (event) => {
        this.setState({ deleteChecked: event.target.checked });
        this.props.addRemoveToDeleteList(path, method);
    };
    changeContentTypes(contentTypes, fieldName) {
        if (contentTypes && contentTypes.length > 0) {
            this.setState((state, props) => {
                state.method[fieldName] = contentTypes;
                props.updatePath(props.path, props.method, state.method);
                return { method: state.method };
            });
        }
    }
    /**
     * @inheritdoc
     */
    render() {
        const {
            classes, method, path, scopes, theme, intl, policyLevel
        } = this.props;
        const resource = this.state.method;
        let chipColor = theme.custom.resourceChipColors ? theme.custom.resourceChipColors[method] : null;
        let chipTextColor = '#000000';
        if (!chipColor) {
            console.log('Check the theme settings. The resourceChipColors is not populated properlly');
            chipColor = '#cccccc';
        } else {
            chipTextColor = theme.palette.getContrastText(theme.custom.resourceChipColors[method]);
        }
        return (
            <div className={classes.resourceRoot}>
                <div className={classes.listItem}>
                    <FormControlLabel control={<Checkbox checked={this.state.deleteChecked} onChange={this.handleDeleteCheck(path, method)} value='' />} label='' className={classes.formControl} />
                    <a onClick={this.toggleMethodData} className={classes.link}>
                        <Chip label={method} style={{ backgroundColor: chipColor, color: chipTextColor }} className={classes.chipActive} />
                    </a>
                    <a onClick={this.toggleMethodData}>
                        <Typography variant='h6' className={classes.pathDisplay}>
                            {path}
                        </Typography>
                    </a>
                    <InlineEditableField
                        saveFieldCallback={this.saveFieldCallback}
                        initText={intl.formatMessage({
                            id: 'Apis.Details.Resources.Resource.click.here.to.add.summery',
                            defaultMessage: 'Click here to add summery',
                        })}
                        fieldValue={resource.summery}
                        type='textarea'
                        fieldName='summery'
                    />
                    <a onClick={this.deleteResource} className={classes.deleteButton}>
                        <Delete className={classes.rightIcon} />
                    </a>
                </div>
                {this.state.visible && (
                    <div>
                        <Grid container spacing={24}>
                            <Grid item xs={12} className={classes.descriptionWrapperUp}>
                                <Typography variant='caption' className={classes.descriptionWrapper}>
                                    <InlineEditableField
                                        saveFieldCallback={this.saveFieldCallback}
                                        initText={intl.formatMessage({
                                            id: 'Apis.Details.Resources.Resource.click.here.to.add.description',
                                            defaultMessage: 'Click here to add description',
                                        })}
                                        fieldValue={resource.description}
                                        type='textarea'
                                        fieldName='description'
                                    />
                                </Typography>
                            </Grid>
                            <Grid item xs={12}>
                                <Table>
                                    <TableRow className={classes.row}>
                                        <TableCell>
                                            <Typography variant='subtitle2'>
                                                <FormattedMessage
                                                    id='Apis.Details.Resources.Resource.produces'
                                                    defaultMessage='Produces'
                                                />
                                            </Typography>
                                        </TableCell>
                                        <TableCell>
                                            <Typography variant='subtitle2'>
                                                <FormattedMessage
                                                    id='Apis.Details.Resources.Resource.consumes'
                                                    defaultMessage='Consumes'
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
                                            <SelectContentType
                                                value={resource.produces}
                                                onChange={this.changeContentTypes}
                                                fieldName='produces'
                                            />
                                        </TableCell>
                                        <TableCell>
                                            <SelectContentType
                                                value={resource.consumes}
                                                onChange={this.changeContentTypes}
                                                fieldName='consumes'
                                            />
                                        </TableCell>
                                        <TableCell>
                                                <Select className={classes.selectWidth}
                                                    value={resource['x-scope']}
                                                    onChange={this.handleScopeChange}
                                                    inputProps={{
                                                        name: 'scopes',
                                                        id: 'age-simple',
                                                    }}
                                                >
                                                    {scopes.map((tempScope) => (
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

                            <Grid item xs={12}>
                                <Table>
                                    <TableRow className={classes.row}>
                                        <TableCell>
                                            <Typography variant='subtitle2'>
                                                <FormattedMessage
                                                    id='Apis.Details.Resources.Resource.authtype'
                                                    defaultMessage='Auth Type'
                                                />
                                            </Typography>
                                        </TableCell>
                                        {policyLevel === 'perResource' &&
                                        <TableCell>
                                            <Typography variant='subtitle2'>
                                                <FormattedMessage
                                                    id='Apis.Details.Resources.Resource.throttling.policy'
                                                    defaultMessage='Throttling Policy'
                                                />
                                            </Typography>
                                        </TableCell>
                                        }
                                    </TableRow>
                                    <TableRow className={classes.row}>
                                        <TableCell>
                                            <Select className={classes.selectWidth}
                                                value={resource['x-auth-type']}
                                                onChange={this.handleAuthTypeChange}
                                                fieldName='Auth Type'
                                            >
                                                <MenuItem
                                                    key='None'
                                                    value='None'
                                                >
                                                    <FormattedMessage
                                                        id='Apis.Details.Resources.Resource.authType.none'
                                                        defaultMessage='None'
                                                    />
                                                </MenuItem>
                                                <MenuItem
                                                    key='Application'
                                                    value='Application'
                                                >
                                                    <FormattedMessage
                                                        id='Apis.Details.Resources.Resource.authType.application'
                                                        defaultMessage='Application'
                                                    />
                                                </MenuItem>
                                                <MenuItem
                                                    key='Application User'
                                                    value='Application User'
                                                >
                                                    <FormattedMessage
                                                        id='Apis.Details.Resources.Resource.authType.application_user'
                                                        defaultMessage='Application User'
                                                    />
                                                </MenuItem>
                                                <MenuItem
                                                    key='Application & Application User'
                                                    value='Application & Application User'
                                                >
                                                    <FormattedMessage
                                                        id='Apis.Details.Resources.Resource.authType.application_and_user'
                                                        defaultMessage='Application & Application User'
                                                    />
                                                </MenuItem>
                                            </Select>
                                        </TableCell>
                                        {policyLevel==='perResource' &&
                                        <TableCell>
                                            <Select className={classes.selectWidth}
                                                value={resource['x-throttling-tier']}
                                                onChange={this.handlePolicyChange}
                                                fieldName='Throttling Policy'
                                            >
                                                {this.props.apiPolicies.map((policy) => (
                                                    <MenuItem
                                                        key={policy.name}
                                                        value={policy.name}
                                                    >
                                                        {policy.displayName}
                                                    </MenuItem>
                                                ))}
                                            </Select>
                                        </TableCell>
                                        }
                                    </TableRow>
                                </Table>
                            </Grid>

                            <Grid item xs={12} className={classes.addParamRow}>
                                <Typography variant='subtitle2'>
                                    <FormattedMessage
                                        id='Apis.Details.Resources.Resource.parameters'
                                        defaultMessage='Parameters'
                                    />
                                </Typography>
                                <form onSubmit={this.propsSubmitHandler} className={classes.propsForm}>
                                    <TextField
                                        id='outlined-dense'
                                        label='Parameter Name'
                                        className={classNames(classes.textField, classes.dense)}
                                        margin='dense'
                                        variant='outlined'
                                        value={this.state.newPropName}
                                        onChange={this.onChangePropName}
                                    />
                                    <Button
                                        variant='contained'
                                        className={classes.button}
                                        onClick={this.propsSubmitHandler}
                                    >
                                        <FormattedMessage
                                            id='Apis.Details.Resources.Resource.add'
                                            defaultMessage='Add'
                                        />
                                    </Button>
                                </form>
                                {/* <WrappedPropertyAddForm propsSubmitHandler={this.propsSubmitHandler} /> */}
                            </Grid>
                            <Grid item xs={12}>
                                {resource.parameters && resource.parameters.length > 0 && (
                                    <Table>
                                        <TableHead>
                                            <TableRow>
                                                <TableCell>
                                                    <FormattedMessage
                                                        id='Apis.Details.Resources.Resource.parameter.name'
                                                        defaultMessage='Parameter Name'
                                                    />
                                                </TableCell>
                                                <TableCell>
                                                    <FormattedMessage
                                                        id='Apis.Details.Resources.Resource.description'
                                                        defaultMessage='Description'
                                                    />
                                                </TableCell>
                                                <TableCell>
                                                    <FormattedMessage
                                                        id='Apis.Details.Resources.Resource.parameter.type'
                                                        defaultMessage='Parameter Type'
                                                    />
                                                </TableCell>
                                                <TableCell>
                                                    <FormattedMessage
                                                        id='Apis.Details.Resources.Resource.data.type'
                                                        defaultMessage='Data Type'
                                                    />
                                                </TableCell>
                                                <TableCell>
                                                    <FormattedMessage
                                                        id='Apis.Details.Resources.Resource.required'
                                                        defaultMessage='Required'
                                                    />
                                                </TableCell>
                                                <TableCell>
                                                    <FormattedMessage
                                                        id='Apis.Details.Resources.Resource.delete'
                                                        defaultMessage='Delete'
                                                    />
                                                </TableCell>
                                            </TableRow>
                                        </TableHead>
                                        <TableBody>
                                            {resource.parameters.map(function (param, i) {
                                                return (
                                                    <TableRow key={i}>
                                                        <TableCell>
                                                            <InlineEditableField type='input' saveFieldCallback={this.saveFieldCallback} fieldIndex={i} fieldValue={param.name} fieldName='param.name' />
                                                        </TableCell>
                                                        <TableCell>
                                                            <InlineEditableField type='input' saveFieldCallback={this.saveFieldCallback} fieldIndex={i} fieldValue={param.description} fieldName='param.description' />
                                                        </TableCell>
                                                        <TableCell>
                                                            <InlineEditableField type='select' saveFieldCallback={this.saveFieldCallback} fieldIndex={i} fieldValue={param.in} defaultValues={['body', 'query', 'header', 'formData']} fieldName='param.in' />
                                                        </TableCell>
                                                        <TableCell>
                                                            <InlineEditableField type='input' saveFieldCallback={this.saveFieldCallback} fieldIndex={i} fieldValue={param.type} fieldName='param.type' />
                                                        </TableCell>
                                                        <TableCell>
                                                            <InlineEditableField type='select' saveFieldCallback={this.saveFieldCallback} fieldIndex={i} fieldValue={param.required} defaultValues={['true', 'false']} fieldName='param.required' />
                                                        </TableCell>
                                                        <TableCell>
                                                            <a onClick={() => this.deleteParam(i)} className={classes.deleteLink}>
                                                                <DeleteIcon />
                                                            </a>
                                                        </TableCell>
                                                    </TableRow>
                                                );
                                            }, this)}
                                        </TableBody>
                                    </Table>
                                )}
                            </Grid>
                        </Grid>
                    </div>
                )}
            </div>
        );
    }
}
Resource.propTypes = {
    classes: PropTypes.object.isRequired,
    theme: PropTypes.object.isRequired,
};

export default injectIntl(withStyles(styles, { withTheme: true })(Resource));
