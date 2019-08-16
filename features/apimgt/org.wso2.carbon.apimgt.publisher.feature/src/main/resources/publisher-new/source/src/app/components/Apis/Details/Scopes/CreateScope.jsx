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
import Grid from '@material-ui/core/Grid';
import TextField from '@material-ui/core/TextField';
import React from 'react';
import APIPropertyField from 'AppComponents/Apis/Details/Overview/APIPropertyField';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage, injectIntl } from 'react-intl';
import TagsInput from 'react-tagsinput';
import Api from 'AppData/api';
// import Scopes from 'AppData/Scopes';  // TODO move below method once Scopes resource response is fixed ~tmkb
import Button from '@material-ui/core/Button';
import PropTypes from 'prop-types';
import { withRouter } from 'react-router';
import { Link } from 'react-router-dom';
import { withStyles } from '@material-ui/core/styles';
import Alert from 'AppComponents/Shared/Alert';

const styles = theme => ({
    buttonSave: {
        marginTop: theme.spacing.unit * 10,
    },
    buttonCancel: {
        marginTop: theme.spacing.unit * 10,
        marginLeft: theme.spacing.unit * 5,
    },
    topics: {
        marginTop: theme.spacing.unit * 10,
    },
    headline: {
        paddingTop: theme.spacing.unit * 1.5,
        paddingLeft: theme.spacing.unit * 2.5,
    },
});

/**
 * Create new scopes for an API
 * @class CreateScope
 * @extends {Component}
 */
class CreateScope extends React.Component {
    constructor(props) {
        super(props);
        this.api = new Api();
        this.api_uuid = props.match.params.api_uuid;
        this.state = {
            apiScopes: null,
            apiScope: {},
            roles: [],
        };
        this.addScope = this.addScope.bind(this);
        this.handleInputs = this.handleInputs.bind(this);
    }

    /**
     * Add new scope
     * @memberof Scopes
     */
    addScope() {
        const api = new Api();
        const scope = this.state.apiScope;
        const { intl, api: apiProp } = this.props;
        scope.bindings = {
            type: 'role',
            values: this.state.roles,
        };
        const newApi = { ...apiProp.toJSON(), scopes: [...apiProp.toJSON().scopes] };
        // TODO: Need to fix the direct mutation here // Cannot add property 0, object is not extensible ~tmkb
        newApi.scopes.push(scope);
        // eslint-disable-next-line no-underscore-dangle
        const promisedApiUpdate = api.update(newApi);
        // TODO move below method once Scopes resource response is fixed ~tmkb
        // Scopes.add(apiProp.id, scope).then((response) => {
        promisedApiUpdate.then((response) => {
            if (!response.id) {
                Alert.info(intl.formatMessage({
                    id: 'Apis.Details.Scopes.CreateScope.something.went.wrong.while.updating.the.scope',
                    defaultMessage: 'Something went wrong while updating the scope',
                }));
                return;
            }
            Alert.info(intl.formatMessage({
                id: 'Apis.Details.Scopes.CreateScope.scope.added.successfully',
                defaultMessage: 'Scope added successfully',
            }));
            const { apiScopes } = this.state;
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
     *
     *
     * @returns
     * @memberof CreateScope
     */
    render() {
        const { classes } = this.props;
        const url = `/apis/${this.props.api.id}/scopes`;
        return (
            <Grid container>
                <Typography className={classes.headline} gutterBottom variant='h5' component='h2'>
                    <FormattedMessage
                        id='Apis.Details.Scopes.CreateScope.create.new.scope'
                        defaultMessage='Create New Scope'
                    />
                </Typography>
                <Grid item lg={5} className={classes.topics}>
                    <APIPropertyField name='Name'>
                        <TextField
                            fullWidth
                            id='name'
                            type='text'
                            name='name'
                            margin='normal'
                            value={this.state.apiScope.name || ''}
                            onChange={this.handleInputs}
                        />
                    </APIPropertyField>
                    <APIPropertyField name='Description'>
                        <TextField
                            style={{
                                width: '100%',
                            }}
                            id='description'
                            name='description'
                            helperText={
                                <FormattedMessage
                                    id='Apis.Details.Scopes.CreateScope.short.description.about.the.scope'
                                    defaultMessage='Short description about the scope'
                                />
                            }
                            margin='normal'
                            type='text'
                            onChange={this.handleInputs}
                            value={this.state.apiScope.description || ''}
                        />
                    </APIPropertyField>
                    <APIPropertyField name='Roles'>
                        <TagsInput value={this.state.roles} onChange={this.handleInputs} onlyUnique />
                    </APIPropertyField>
                    <Button variant='contained' color='primary' onClick={this.addScope} className={classes.buttonSave}>
                        <FormattedMessage id='Apis.Details.Scopes.CreateScope.save' defaultMessage='Save' />
                    </Button>
                    <Link to={url}>
                        <Button variant='contained' color='primary' className={classes.buttonCancel}>
                            <FormattedMessage id='Apis.Details.Scopes.CreateScope.cancel' defaultMessage='Cancel' />
                        </Button>
                    </Link>
                </Grid>
            </Grid>
        );
    }
}

CreateScope.propTypes = {
    match: PropTypes.shape({
        params: PropTypes.object,
    }),
    api: PropTypes.shape({
        id: PropTypes.string,
    }).isRequired,
    classes: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({ formatMessage: PropTypes.func }).isRequired,
};

CreateScope.defaultProps = {
    match: { params: {} },
};

export default injectIntl(withRouter(withStyles(styles)(CreateScope)));
