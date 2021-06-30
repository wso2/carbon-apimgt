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
import Grid from '@material-ui/core/Grid';
import TextField from '@material-ui/core/TextField';
import React from 'react';
import Typography from '@material-ui/core/Typography';
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
import Error from '@material-ui/core/SvgIcon/SvgIcon';
import Api from 'AppData/api';

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

// eslint-disable-next-line valid-jsdoc
/**
 * Create new scopes for an API
 * @class CreateScope
 * @extends {Component}
 */
class EditScope extends React.Component {
    /**
     * constructor
     * @param {JSON} props parent props.
     */
    constructor(props) {
        super(props);
        // this.api = new Api();
        this.api_uuid = props.match.params.api_uuid;
        const { api, location } = this.props;
        const thisScope = api.scopes.find((apiScope) => {
            return apiScope.scope.name === location.state.scopeName;
        });
        this.state = {
            apiScope: { ...thisScope },
            validRoles: thisScope.scope.bindings,
            invalidRoles: [],
            roleValidity: true,
        };
        this.updateScope = this.updateScope.bind(this);
        this.handleInputs = this.handleInputs.bind(this);
        this.handleRoleDeletion = this.handleRoleDeletion.bind(this);
        this.handleRoleAddition = this.handleRoleAddition.bind(this);
        this.validateScopeDescription = this.validateScopeDescription.bind(this);
        this.validateScopeDisplayName = this.validateScopeDisplayName.bind(this);
    }

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
     * Handle api scope addition event
     * @param {any} event Button Click event
     * @memberof Scopes
     */
    handleInputs(event) {
        if (Array.isArray(event)) {
            const { apiScope } = this.state;
            apiScope.scope.bindings = event;
            this.setState({
                apiScope,
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
     * Handle Role Addition.
     * @param {string} role The first number.
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
     * Add new scope
     * @memberof Scopes
     */
    updateScope() {
        const { apiScope, validRoles } = this.state;
        const {
            intl, api, history, updateAPI,
        } = this.props;
        const originalScope = apiScope.scope;
        apiScope.scope = {
            id: originalScope.id,
            name: originalScope.name,
            displayName: originalScope.displayName,
            description: originalScope.description,
            bindings: validRoles,
        };
        const urlPrefix = api.apiType === Api.CONSTS.APIProduct ? 'api-products' : 'apis';
        const scopes = api.scopes.map((scopeObj) => {
            if (scopeObj.scope.name === apiScope.scope.name) {
                return apiScope;
            } else {
                return scopeObj;
            }
        });
        const updateProperties = { scopes };
        const promisedApiUpdate = updateAPI(updateProperties);
        promisedApiUpdate.then(() => {
            Alert.info(intl.formatMessage({
                id: 'Apis.Details.Scopes.CreateScope.scope.updated.successfully',
                defaultMessage: 'Scope updated successfully',
            }));
            const redirectURL = '/' + urlPrefix + '/' + api.id + '/scopes/';
            history.push(redirectURL);
        }).catch((error) => {
            const { response } = error;
            if (response.body) {
                const { description } = response.body;
                Alert.error(description);
            }
        });
    }

    /**
     * validate Scope Description.
     * @param {JSON} event click event object.
     */
    validateScopeDescription({ target: { value } }) {
        const { apiScope } = this.state;
        const originalScope = apiScope.scope;
        apiScope.scope = {
            id: originalScope.id,
            name: originalScope.name,
            displayName: originalScope.displayName,
            description: value,
            bindings: originalScope.bindings,
        };
        this.setState({
            apiScope,
        });
    }

    /**
     * validate Scope Display Name.
     * @param {JSON} event click event object.
     */
    validateScopeDisplayName({ target: { value } }) {
        const { apiScope } = this.state;
        const originalScope = apiScope.scope;
        apiScope.scope = {
            id: originalScope.id,
            name: originalScope.name,
            displayName: value,
            description: originalScope.description,
            bindings: originalScope.bindings,
        };
        this.setState({
            apiScope,
        });
    }

    /**
     * Render.
     * @returns {JSX} rendered component.
     */
    render() {
        const { classes, api, isAPIProduct } = this.props;
        const {
            apiScope, roleValidity, validRoles, invalidRoles,
        } = this.state;
        const urlPrefix = isAPIProduct ? 'api-products' : 'apis';
        const url = `/${urlPrefix}/${api.id}/scopes`;
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
                                            id='Apis.Details.Scopes.Scopes.heading.edit.scope.heading'
                                            defaultMessage='Scopes'
                                        />
                                    </Typography>
                                </Link>
                                <Icon>keyboard_arrow_right</Icon>
                                <Typography variant='h4'>
                                    <FormattedMessage
                                        id='Apis.Details.Scopes.EditScope.update.scope'
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
                                        value={apiScope.scope.name}
                                        onChange={this.handleScopeNameInput}
                                        disabled
                                    />
                                </FormControl>
                                <FormControl margin='normal'>
                                    <TextField
                                        id='displayName'
                                        label='Display Name'
                                        placeholder='Scope Display Name'
                                        helperText={(
                                            <FormattedMessage
                                                id='Apis.Details.Scopes.CreateScope.short.description.name'
                                                defaultMessage='Enter Scope Name ( E.g.,: creator )'
                                            />
                                        )}
                                        fullWidth
                                        margin='normal'
                                        variant='outlined'
                                        InputLabelProps={{
                                            shrink: true,
                                        }}
                                        value={apiScope.scope.displayName || ''}
                                        onChange={this.validateScopeDisplayName}
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
                                                id='Apis.Details.Scopes.EditScope.short.description.about.the.scope'
                                                defaultMessage='Short description about the scope'
                                            />
                                        )}
                                        margin='normal'
                                        InputLabelProps={{
                                            shrink: true,
                                        }}
                                        onChange={this.validateScopeDescription}
                                        value={apiScope.scope.description || ''}
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
                                                    id='Apis.Details.EditScopes.Roles.Invalid'
                                                    defaultMessage='Role is invalid'
                                                />
                                            ) : (
                                                <FormattedMessage
                                                    id='Apis.Details.Scopes.EditScopes.roles.help'
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
                                        disabled={invalidRoles.length !== 0 || api.isRevision}
                                        className={classes.saveButton}
                                    >
                                        <FormattedMessage
                                            id='Apis.Details.Scopes.EditScope.update'
                                            defaultMessage='Update'
                                        />
                                    </Button>
                                    <Link to={url}>
                                        <Button variant='contained'>
                                            <FormattedMessage
                                                id='Apis.Details.Scopes.EditScope.cancel'
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
    api: PropTypes.shape({
        id: PropTypes.string,
    }).isRequired,
    classes: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({ formatMessage: PropTypes.func }).isRequired,
    location: PropTypes.shape({
        state: PropTypes.shape({
            scopeName: PropTypes.string,
        }),
    }).isRequired,
    history: PropTypes.shape({ push: PropTypes.func }).isRequired,
    isAPIProduct: PropTypes.bool.isRequired,
    updateAPI: PropTypes.func.isRequired,
};

EditScope.defaultProps = {
    match: { params: {} },
};

export default injectIntl(withRouter(withStyles(styles)(EditScope)));
