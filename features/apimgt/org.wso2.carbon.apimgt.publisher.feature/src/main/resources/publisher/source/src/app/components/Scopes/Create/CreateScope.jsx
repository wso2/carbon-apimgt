/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import TextField from '@material-ui/core/TextField';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage, injectIntl } from 'react-intl';
import Button from '@material-ui/core/Button';
import PropTypes from 'prop-types';
import { Link } from 'react-router-dom';
import { withStyles } from '@material-ui/core/styles';
import FormControl from '@material-ui/core/FormControl';
import Grid from '@material-ui/core/Grid';
import ChipInput from 'material-ui-chip-input';
import APIValidation from 'AppData/APIValidation';
import base64url from 'base64url';
import Error from '@material-ui/icons/Error';
import InputAdornment from '@material-ui/core/InputAdornment';
import Chip from '@material-ui/core/Chip';
import Icon from '@material-ui/core/Icon';
import Paper from '@material-ui/core/Paper';
import { red } from '@material-ui/core/colors/';
import CircularProgress from '@material-ui/core/CircularProgress';
import Alert from 'AppComponents/Shared/Alert';
import API from 'AppData/api';
import { isRestricted } from 'AppData/AuthManager';

const styles = (theme) => ({
    root: {
        flexGrow: 1,
        marginTop: 10,
        display: 'flex',
        flexDirection: 'column',
        padding: 20,
    },
    titleWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: theme.spacing(3),
    },
    titleLink: {
        color: theme.palette.primary.main,
        marginRight: theme.spacing(1),
    },
    contentWrapper: {
        maxWidth: theme.custom.contentAreaWidth,
    },
    mainTitle: {
        paddingLeft: 0,
    },
    FormControl: {
        padding: `0 0 0 ${theme.spacing(1)}px`,
        width: '100%',
        marginTop: 0,
    },
    FormControlOdd: {
        padding: `0 0 0 ${theme.spacing(1)}px`,
        backgroundColor: theme.palette.background.paper,
        width: '100%',
        marginTop: 0,
    },
    FormControlLabel: {
        marginBottom: theme.spacing(1),
        marginTop: theme.spacing(1),
        fontSize: theme.typography.caption.fontSize,
    },
    buttonSection: {
        paddingTop: theme.spacing(3),
    },
    saveButton: {
        marginRight: theme.spacing(1),
    },
    helpText: {
        color: theme.palette.text.hint,
        marginTop: theme.spacing(1),
    },
    extraPadding: {
        paddingLeft: theme.spacing(2),
    },
    addNewOther: {
        paddingTop: 40,
    },
    titleGrid: {
        ' & .MuiGrid-item': {
            padding: 0,
            margin: 0,
        },
    },
    descriptionForm: {
        marginTop: theme.spacing(1),
    },
    progress: {
        marginLeft: theme.spacing(1),
    },
});

/**
 * Create new scopes for an API
 * @class CreateScope
 * @extends {Component}
 */
class CreateScope extends React.Component {
    /**
     * Creates an instance of CreateScope
     * @param {*} props properies passed by the parent element
     * @memberof CreateScope
     */
    constructor(props) {
        super(props);
        this.api = new API();
        const valid = [];
        valid.name = {
            invalid: false,
            error: '',
        };
        valid.description = {
            invalid: false,
            error: '',
        };
        valid.displayName = {
            invalid: false,
            error: '',
        };
        this.state = {
            apiScopes: null,
            sharedScope: {},
            validRoles: [],
            valid,
            roleValidity: true,
            invalidRoles: [],
            scopeAddDisabled: false,
        };
        this.addScope = this.addScope.bind(this);
        this.validateScopeName = this.validateScopeName.bind(this);
        this.handleScopeNameInput = this.handleScopeNameInput.bind(this);
        this.validateScopeDetails = this.validateScopeDetails.bind(this);
        this.validateScopeDisplayName = this.validateScopeDisplayName.bind(this);
        this.handleScopeDisplayNameInput = this.handleScopeDisplayNameInput.bind(this);
        this.handleRoleAddition = this.handleRoleAddition.bind(this);
        this.handleRoleDeletion = this.handleRoleDeletion.bind(this);
    }

    /**
     * Hadnling role deletion.
     * @param {any} role The role that needs to be deleted.
     * @memberof CreateScope
     */
    handleRoleDeletion = (role) => {
        const { validRoles, invalidRoles } = this.state;
        if (invalidRoles.includes(role)) {
            const invalidRolesArray = invalidRoles.filter((existingRole) => existingRole !== role);
            this.setState({ invalidRoles: invalidRolesArray });
            if (invalidRolesArray.length === 0) {
                this.setState({ roleValidity: true });
            }
        } else {
            this.setState({ validRoles: validRoles.filter((existingRole) => existingRole !== role) });
        }
    };

    /**
     * Hadnling role addition.
     * @param {any} role The role that needs to be added.
     * @memberof CreateScope
     */
    handleRoleAddition(role) {
        const { validRoles, invalidRoles } = this.state;
        const promise = APIValidation.role.validate(base64url.encode(role));
        promise
            .then(() => {
                this.setState({
                    roleValidity: true,
                    validRoles: [...validRoles, role],
                });
            })
            .catch((error) => {
                if (error.status === 404) {
                    this.setState({
                        roleValidity: false,
                        invalidRoles: [...invalidRoles, role],
                    });
                } else {
                    Alert.error('Error when validating role: ' + role);
                    console.error('Error when validating role ' + error);
                }
            });
    }

    /**
     * Handle scope name input.
     * @param {any} target The id and value of the target.
     * @memberof CreateScope
     */
    handleScopeNameInput({ target: { id, value } }) {
        this.validateScopeName(id, value);
    }

    /**
     * Handle scope display name input.
     * @param {any} target The id and value of the target.
     * @memberof CreateScope
     */
    handleScopeDisplayNameInput({ target: { id, value } }) {
        this.validateScopeDisplayName(id, value);
    }

    /**
     * Add new scope
     * @memberof CreateScope
     */
    addScope() {
        const {
            intl, history,
        } = this.props;
        const {
            sharedScope, validRoles,
        } = this.state;
        if (this.validateScopeName('name', sharedScope.name)
            || this.validateScopeDisplayName('displayName', sharedScope.displayName)) {
            // return status of the validation
            return;
        }
        sharedScope.bindings = validRoles;

        const promisedScopeAdd = this.api.addSharedScope(sharedScope);
        this.setState({ scopeAddDisabled: true });
        promisedScopeAdd
            .then(() => {
                Alert.info(intl.formatMessage({
                    id: 'Scopes.Create.CreateScope.scope.added.successfully',
                    defaultMessage: 'Scope added successfully',
                }));
                const { apiScopes } = this.state;
                const redirectURL = '/scopes/';
                history.push(redirectURL);
                this.setState({
                    apiScopes,
                    sharedScope: {},
                    validRoles: [],
                });
            })
            .catch((error) => {
                const { response } = error;
                if (response.body) {
                    const { description } = response.body;
                    Alert.error(description);
                }
            })
            .finally(() => {
                this.setState({ scopeAddDisabled: false });
            });
    }

    /**
     * Scope display name validation.
     * @param {any} id The id of the scope name.
     * @param {any} value The value of the scope name.
     * @returns {boolean} whether the scope name is validated.
     * @memberof CreateScope
     */
    validateScopeDisplayName(id, value) {
        const { valid, sharedScope } = this.state;
        const { intl } = this.props;
        sharedScope[id] = value;
        valid[id].invalid = !(value && value.length > 0);
        if (valid[id].invalid) {
            valid[id].error = 'Scope display name cannot be empty';
        }
        valid[id].invalid = !(value && value.length <= 255);
        if (valid[id].invalid) {
            valid[id].error = intl.formatMessage({
                id: 'Scopes.Create.Scope.displayName.length.exceeded',
                defaultMessage: 'Exceeds maximum length limit of 255 characters',
            });
        }
        if (!valid[id].invalid && /[!@#$%^&*(),?"{}[\]|<>\t\n]|(^apim:)/i.test(value)) {
            valid[id].invalid = true;
            valid[id].error = 'Field contains special characters';
        }
        if (!valid[id].invalid) {
            valid[id].error = '';
        }
        this.setState({
            valid,
            sharedScope,
        });
        return valid[id].invalid;
    }

    /**
     * Scope name validation.
     * @param {any} id The id of the scope name.
     * @param {any} value The value of the scope name.
     * @returns {boolean} whether the scope name is validated.
     * @memberof CreateScope
     */
    validateScopeName(id, value) {
        const { valid, sharedScope } = this.state;
        const { intl } = this.props;
        sharedScope[id] = value;
        valid[id].invalid = !(value && value.length > 0);
        if (valid[id].invalid) {
            valid[id].error = 'Scope name cannot be empty';
        }
        valid[id].invalid = !(value && value.length <= 60);
        if (valid[id].invalid) {
            valid[id].error = intl.formatMessage({
                id: 'Scopes.Create.Scope.name.length.exceeded',
                defaultMessage: 'Exceeds maximum length limit of 60 characters',
            });
        }

        if (/\s/.test(value)) {
            valid[id].invalid = true;
            valid[id].error = 'Scope name cannot have spaces';
        }
        if (!valid[id].invalid && /[!@#$%^&*(),?"{}[\]|<>\t\n]|(^apim:)/i.test(value)) {
            valid[id].invalid = true;
            valid[id].error = 'Field contains special characters';
        }
        if (!valid[id].invalid) {
            const promise = APIValidation.scope.validate(base64url.encode(value));
            promise
                .then(() => {
                    valid[id].invalid = true;
                    valid[id].error = 'Scope name already exists';
                    this.setState({
                        valid,
                    });
                })
                .catch((error) => {
                    if (error.status === 404) {
                        valid[id].invalid = false;
                        valid[id].error = '';
                        this.setState({
                            valid,
                        });
                    } else {
                        Alert.error('Error when validating scope: ' + value);
                        console.error('Error when validating scope ' + error);
                    }
                });
        }
        if (!valid[id].invalid) {
            valid[id].error = '';
        }
        this.setState({
            valid,
            sharedScope,
        });
        return valid[id].invalid;
    }

    /**
 * Validate scope details.
 * @param {any} target The id and value of the target.
 * @memberof CreateScope
 */
    validateScopeDetails({ target: { id, value } }) {
        const { valid, sharedScope } = this.state;
        const { intl } = this.props;
        sharedScope[id] = value;
        if (value && value.length !== '' && value.length >= 512) {
            valid[id].invalid = true;
            valid[id].error = intl.formatMessage({
                id: 'Scopes.Create.Scope.description.length.exceeded',
                defaultMessage: 'Exceeds maximum length limit of 512 characters',
            });
        } else {
            valid[id].invalid = false;
            valid[id].error = '';
        }
        this.setState({
            valid,
            sharedScope,
        });
    }

    /**
     *
     *
     * @returns {any} returns the UI render.
     * @memberof CreateScope
     */
    render() {
        const { classes } = this.props;
        const url = '/scopes';
        const {
            roleValidity, validRoles, invalidRoles, scopeAddDisabled, valid, sharedScope,
        } = this.state;

        return (
            <Grid container spacing={3}>
                <Grid item sm={12} md={12} />
                {/*
            Following two grids control the placement of whole create page
            For centering the content better use `container` props, but instead used an empty grid item for flexibility
             */}
                <Grid item sm={0} md={0} lg={2} />
                <Grid item sm={12} md={12} lg={8}>
                    <Grid container spacing={5} className={classes.titleGrid}>
                        <Grid item md={12}>
                            <div className={classes.titleWrapper}>
                                <Link to={url} className={classes.titleLink}>
                                    <Typography variant='h4'>
                                        <FormattedMessage
                                            id='Scopes.Create.CreateScope.heading.scope.heading'
                                            defaultMessage='Scopes'
                                        />
                                    </Typography>
                                </Link>
                                <Icon>keyboard_arrow_right</Icon>
                                <Typography variant='h4'>
                                    <FormattedMessage
                                        id='Scopes.Create.CreateScope.create.new.scope'
                                        defaultMessage='Create New Scope'
                                    />
                                </Typography>
                            </div>
                        </Grid>
                        <Grid item md={12}>
                            <Paper elevation={0} className={classes.root}>
                                <FormControl margin='normal'>
                                    <TextField
                                        id='name'
                                        label='Name'
                                        placeholder='Scope Name'
                                        error={valid.name.invalid}
                                        helperText={
                                            valid.name.invalid ? (
                                                valid.name.error
                                            ) : (
                                                <FormattedMessage
                                                    id='Scopes.Create.CreateScope.short.description.name'
                                                    defaultMessage='Enter Scope Name ( E.g.,: creator )'
                                                />
                                            )
                                        }
                                        fullWidth
                                        margin='normal'
                                        variant='outlined'
                                        InputLabelProps={{
                                            shrink: true,
                                        }}
                                        value={sharedScope.name || ''}
                                        onChange={this.handleScopeNameInput}
                                    />
                                </FormControl>
                                <FormControl margin='normal'>
                                    <TextField
                                        id='displayName'
                                        label='Display Name'
                                        placeholder='Scope Display Name'
                                        error={valid.displayName.invalid}
                                        helperText={valid.displayName.invalid ? (
                                            valid.displayName.error
                                        ) : (
                                            <FormattedMessage
                                                id='Scopes.Create.CreateScope.short.description.display.name'
                                                defaultMessage='Enter Scope Display Name ( E.g.,: creator )'
                                            />
                                        )}
                                        fullWidth
                                        margin='normal'
                                        variant='outlined'
                                        InputLabelProps={{
                                            shrink: true,
                                        }}
                                        value={sharedScope.displayName || ''}
                                        onChange={this.handleScopeDisplayNameInput}
                                    />
                                </FormControl>
                                <FormControl margin='normal' classes={{ root: classes.descriptionForm }}>
                                    <TextField
                                        id='description'
                                        label='Description'
                                        variant='outlined'
                                        placeholder='Short description about the scope'
                                        error={valid.description.invalid}
                                        helperText={
                                            valid.description.invalid ? (
                                                valid.description.error
                                            ) : (
                                                <FormattedMessage
                                                    id='Apis.Details.Scopes.CreateScope.description.about.the.scope'
                                                    defaultMessage='Short description about the scope'
                                                />
                                            )
                                        }
                                        margin='normal'
                                        InputLabelProps={{
                                            shrink: true,
                                        }}
                                        onChange={this.validateScopeDetails}
                                        value={sharedScope.description || ''}
                                        multiline
                                    />
                                </FormControl>
                                <FormControl margin='normal'>
                                    <ChipInput
                                        label='Roles'
                                        InputLabelProps={{
                                            shrink: true,
                                        }}
                                        variant='outlined'
                                        value={validRoles.concat(invalidRoles)}
                                        alwaysShowPlaceholder={false}
                                        placeholder='Enter roles and press Enter'
                                        blurBehavior='clear'
                                        InputProps={{
                                            endAdornment: !roleValidity && (
                                                <InputAdornment position='end'>
                                                    <Error color='error' />
                                                </InputAdornment>
                                            ),
                                        }}
                                        onAdd={this.handleRoleAddition}
                                        error={!roleValidity}
                                        helperText={
                                            !roleValidity ? (
                                                <FormattedMessage
                                                    id='Scopes.Create.ScopeCreate.Roles.Invalid'
                                                    defaultMessage='Role is invalid'
                                                />
                                            ) : (
                                                <FormattedMessage
                                                    id='Scopes.Create.CreateScope.roles.help'
                                                    defaultMessage='Enter a valid role and press `Enter`.'
                                                />
                                            )
                                        }
                                        chipRenderer={({ value }, key) => (
                                            <Chip
                                                key={key}
                                                label={value}
                                                onDelete={() => {
                                                    this.handleRoleDeletion(value);
                                                }}
                                                style={{
                                                    backgroundColor: invalidRoles.includes(value) ? red[300] : null,
                                                    margin: '8px 8px 8px 0',
                                                    float: 'left',
                                                }}
                                            />
                                        )}
                                    />
                                </FormControl>
                                <div className={classes.addNewOther}>
                                    <Button
                                        variant='contained'
                                        color='primary'
                                        onClick={this.addScope}
                                        disabled={
                                            isRestricted(['apim:shared_scope_manage'])
                                            || valid.name.invalid
                                            || invalidRoles.length !== 0
                                            || scopeAddDisabled
                                            || valid.description.invalid
                                        }
                                        className={classes.saveButton}
                                    >
                                        {scopeAddDisabled ? (
                                            <>
                                                <FormattedMessage
                                                    id='Scopes.Create.CreateScope.saving'
                                                    defaultMessage='Saving'
                                                />
                                                <CircularProgress size={16} classes={{ root: classes.progress }} />
                                            </>
                                        ) : (
                                            <FormattedMessage
                                                id='Scopes.Create.CreateScope.save'
                                                defaultMessage='Save'
                                            />
                                        )}
                                    </Button>
                                    <Link to={url}>
                                        <Button>
                                            <FormattedMessage
                                                id='Scopes.Create.CreateScope.cancel'
                                                defaultMessage='Cancel'
                                            />
                                        </Button>
                                    </Link>
                                </div>
                            </Paper>
                        </Grid>
                    </Grid>
                </Grid>
            </Grid>
        );
    }
}

CreateScope.propTypes = {
    match: PropTypes.shape({
        params: PropTypes.shape({}),
    }),
    api: PropTypes.shape({
        id: PropTypes.string,
    }).isRequired,
    history: PropTypes.shape({ push: PropTypes.func }).isRequired,
    classes: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({ formatMessage: PropTypes.func }).isRequired,
};

CreateScope.defaultProps = {
    match: { params: {} },
};

export default injectIntl(withStyles(styles)(CreateScope));
