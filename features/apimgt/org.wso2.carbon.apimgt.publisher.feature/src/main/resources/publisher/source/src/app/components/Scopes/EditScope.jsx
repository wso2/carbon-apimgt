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
import Grid from '@material-ui/core/Grid';
import TextField from '@material-ui/core/TextField';
import React from 'react';
import Typography from '@material-ui/core/Typography';
import { Progress } from 'AppComponents/Shared';
import { FormattedMessage, injectIntl } from 'react-intl';
import Button from '@material-ui/core/Button';
import PropTypes from 'prop-types';
import { withRouter } from 'react-router';
import { Link } from 'react-router-dom';
import { withStyles } from '@material-ui/core/styles';
import Alert from 'AppComponents/Shared/Alert';
import Paper from '@material-ui/core/Paper';
import FormControl from '@material-ui/core/FormControl';
import ChipInput from 'material-ui-chip-input';
import APIValidation from 'AppData/APIValidation';
import Chip from '@material-ui/core/Chip';
import { red } from '@material-ui/core/colors/';
import Icon from '@material-ui/core/Icon';
import base64url from 'base64url';
import InputAdornment from '@material-ui/core/InputAdornment';
import { isRestricted } from 'AppData/AuthManager';
import Error from '@material-ui/core/SvgIcon/SvgIcon';
import API from 'AppData/api';

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
        marginRight: theme.spacing(2),
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
});

/**
 * Display a comment list
 * @class EditScope
 * @extends {React.Component}
 */
class EditScope extends React.Component {
    /**
     * Creates an instance of EditScope
     * @param {*} props properies passed by the parent element
     * @memberof EditScope
     */
    constructor(props) {
        super(props);
        const valid = [];
        valid.displayName = {
            invalid: false,
            error: '',
        };
        valid.description = {
            invalid: false,
            error: '',
        };
        this.state = {
            sharedScope: null,
            validRoles: [],
            valid,
            invalidRoles: [],
            roleValidity: true,
        };
        this.updateScope = this.updateScope.bind(this);
        this.handleInputs = this.handleInputs.bind(this);
        this.handleRoleDeletion = this.handleRoleDeletion.bind(this);
        this.handleRoleAddition = this.handleRoleAddition.bind(this);
        this.validateScopeDetails = this.validateScopeDetails.bind(this);
        this.validateScopeDisplayName = this.validateScopeDisplayName.bind(this);
        this.handleScopeDisplayNameInput = this.handleScopeDisplayNameInput.bind(this);
    }

    /**
     * @inheritdoc
     */
    componentDidMount() {
        this.getScope();
    }

    /**
     * Get scope
     * @memberof EditScope
     */
    getScope() {
        const { location } = this.props;
        const { scopeId } = location.state;
        const restAPI = new API();
        if (scopeId) {
            const scopePromise = restAPI.getSharedScopeDetails(scopeId);
            scopePromise
                .then((doc) => {
                    this.setState({
                        sharedScope: doc.body,
                        validRoles: doc.body.bindings,
                    });
                })
                .catch((error) => {
                    const { response } = error;
                    if (response.body) {
                        const { description } = response.body;
                        Alert.error(description);
                    }
                });
        }
    }


    /**
     * Hadnling role deletion.
     * @param {any} role The role that needs to be deleted.
     * @memberof EditScope
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
     * @memberof EditScope
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
         * Handle api scope addition event
         * @param {any} event Button Click event
         * @memberof EditScope
         */
    handleInputs(event) {
        if (Array.isArray(event)) {
            const { sharedScope } = this.state;
            sharedScope.bindings = event;
            this.setState({
                sharedScope,
            });
        } else {
            const input = event.target;
            const { sharedScope } = this.state;
            sharedScope[input.id] = input.value;
            this.setState({
                sharedScope,
            });
        }
    }

    /**
     * Handle scope display name input.
     * @param {any} target The id and value of the target.
     * @memberof EditScope
     */
    handleScopeDisplayNameInput({ target: { id, value } }) {
        this.validateScopeDisplayName(id, value);
    }

    /**
     * Update scope
     * @memberof EditScope
     */
    updateScope() {
        const { sharedScope, validRoles } = this.state;
        const {
            intl, history,
        } = this.props;

        if (this.validateScopeDisplayName('displayName', sharedScope.displayName)) {
            // return status of the validation
            return;
        }

        const restAPI = new API();
        const updatedScope = sharedScope;
        updatedScope.bindings = validRoles;
        const promisedScopeUpdate = restAPI.updateSharedScope(updatedScope.id, updatedScope);
        promisedScopeUpdate.then(() => {
            Alert.info(intl.formatMessage({
                id: 'Scopes.EditScope.scope.updated.successfully',
                defaultMessage: 'Scope updated successfully',
            }));
            const redirectURL = '/scopes/';
            history.push(redirectURL);
        });
        promisedScopeUpdate.catch((error) => {
            const { response } = error;
            if (response.body) {
                const { description } = response.body;
                Alert.error(description);
            }
        });
    }

    /**
     * Validate scope details.
     * @param {any} target The id and value of the target.
     * @memberof CreateScope
     */
    validateScopeDetails({ target: { id, value } }) {
        const { sharedScope } = this.state;
        const updatedScope = sharedScope;
        if (id === 'displayName') {
            updatedScope.displayName = value;
        } else if (id === 'description') {
            updatedScope.description = value;
        }
        this.setState({
            sharedScope: updatedScope,
        });
    }

    /**
     * Scope display name validation.
     * @param {any} id The id of the scope name.
     * @param {any} value The value of the scope name.
     * @returns {boolean} whether the scope name is validated.
     * @memberof EditScope
     */
    validateScopeDisplayName(id, value) {
        const { valid, sharedScope } = this.state;

        sharedScope[id] = value;
        valid[id].invalid = !(value && value.length > 0);
        if (valid[id].invalid) {
            valid[id].error = 'Scope display name cannot be empty';
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
     *
     *
     * @returns {any} returns the UI render.
     * @memberof EditScope
     */
    render() {
        const { classes } = this.props;
        const {
            sharedScope, roleValidity, validRoles, invalidRoles, valid,
        } = this.state;
        const url = '/scopes';
        if (!sharedScope) {
            return <Progress />;
        }
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
                                            id='Scopes.EditScope.heading.scope.heading'
                                            defaultMessage='Scopes'
                                        />
                                    </Typography>
                                </Link>
                                <Icon>keyboard_arrow_right</Icon>
                                <Typography variant='h4'>
                                    <FormattedMessage
                                        id='Scopes.EditScope.update.scope'
                                        defaultMessage='Update Scope'
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
                                        fullWidth
                                        margin='normal'
                                        variant='outlined'
                                        InputLabelProps={{
                                            shrink: true,
                                        }}
                                        value={sharedScope.name}
                                        onChange={this.handleScopeNameInput}
                                        disabled
                                    />
                                </FormControl>
                                <FormControl margin='normal'>
                                    <TextField
                                        id='displayName'
                                        label='Display Name'
                                        fullWidth
                                        margin='normal'
                                        variant='outlined'
                                        placeholder='Scope Display Name'
                                        error={valid.displayName.invalid}
                                        helperText={valid.displayName.invalid ? (
                                            valid.displayName.error
                                        ) : (
                                            <FormattedMessage
                                                id='Scopes.EditScope.short.description.display.name'
                                                defaultMessage='Enter Scope Display Name ( E.g.,: creator )'
                                            />
                                        )}
                                        InputLabelProps={{
                                            shrink: true,
                                        }}
                                        value={sharedScope.displayName || ''}
                                        onChange={this.handleScopeDisplayNameInput}
                                    />
                                </FormControl>
                                <FormControl margin='normal'>
                                    <TextField
                                        id='description'
                                        label='Description'
                                        variant='outlined'
                                        placeholder='Short description about the scope'
                                        helperText={(
                                            <FormattedMessage
                                                id='Scopes.EditScope.short.description.about.the.scope'
                                                defaultMessage='Short description about the scope'
                                            />
                                        )}
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
                                                    id='Scopes.EditScope.Roles.Invalid'
                                                    defaultMessage='Role is invalid'
                                                />
                                            ) : (
                                                <FormattedMessage
                                                    id='Scopes.EditScope.roles.help'
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
                                        onClick={this.updateScope}
                                        disabled={invalidRoles.length !== 0
                                            || isRestricted(['apim:shared_scope_manage'])}
                                        className={classes.saveButton}
                                    >
                                        <FormattedMessage
                                            id='Scopes.EditScope.update'
                                            defaultMessage='Update'
                                        />
                                    </Button>
                                    <Link to={url}>
                                        <Button variant='contained'>
                                            <FormattedMessage
                                                id='Scopes.EditScope.cancel'
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

EditScope.propTypes = {
    match: PropTypes.shape({
        params: PropTypes.shape({}),
    }),
    classes: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({ formatMessage: PropTypes.func }).isRequired,
    location: PropTypes.shape({
        state: PropTypes.shape({
            scopeName: PropTypes.string,
            scopeId: PropTypes.string,
        }),
    }).isRequired,
    history: PropTypes.shape({ push: PropTypes.func }).isRequired,
};

EditScope.defaultProps = {
    match: { params: {} },
};

export default injectIntl(withRouter(withStyles(styles)(EditScope)));
