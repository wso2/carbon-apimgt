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
import APIPropertyField from 'AppComponents/Apis/Details/Overview/APIPropertyField';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage, injectIntl } from 'react-intl';
import TagsInput from 'react-tagsinput';
import Api from 'AppData/api';
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
class EditScope extends React.Component {
    constructor(props) {
        super(props);
        // this.api = new Api();
        this.api_uuid = props.match.params.api_uuid;
        const { api, location } = this.props;
        this.state = {
            apiScope: api.scopes.find((scope) => {
                return scope.name === location.state.scopeName;
            }),
        };
        this.updateScope = this.updateScope.bind(this);
        this.handleInputs = this.handleInputs.bind(this);
    }

    /**
     * Add new scope
     * @memberof Scopes
     */
    updateScope() {
        const restApi = new Api();
        const updatedScope = this.state.apiScope;
        const { intl, api, history } = this.props;
        // temp fix to deep copy
        // eslint-disable-next-line no-underscore-dangle
        const apiData = JSON.parse(JSON.stringify(api._data));
        apiData.scopes = api.scopes.map((scope) => {
            if (scope.name === updatedScope.name) {
                return updatedScope;
            } else {
                return scope;
            }
        });
        const promisedApiUpdate = restApi.update(apiData);
        promisedApiUpdate.then((response) => {
            if (response.status !== 200) {
                Alert.info(intl.formatMessage({
                    id: 'Apis.Details.Scopes.CreateScope.something.went.wrong.while.updating.the.scope',
                    defaultMessage: 'Something went wrong while updating the scope',
                }));
                return;
            }
            Alert.info(intl.formatMessage({
                id: 'Apis.Details.Scopes.CreateScope.scope.updated.successfully',
                defaultMessage: 'Scope updated successfully',
            }));
            const redirectURL = '/apis/' + api.id + '/scopes/';
            history.push(redirectURL);
        });
        promisedApiUpdate.catch((error) => {
            const { response } = error;
            if (response.body) {
                const { description } = response.body;
                Alert.error(description);
            }
        });
    }

    /**
     * Handle api scope addition event
     * @param {any} event Button Click event
     * @memberof Scopes
     */
    handleInputs(event) {
        if (Array.isArray(event)) {
            const { apiScope } = this.state;
            apiScope.bindings.values = event;
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

    render() {
        const { classes, api } = this.props;
        const { apiScope } = this.state;
        const url = `/apis/${api.id}/scopes`;
        return (
            <Grid container>
                <Typography
                    className={classes.headline}
                    gutterBottom
                    variant='h5'
                    component='h2'
                >
                    <FormattedMessage
                        id='Apis.Details.Scopes.EditScope.update.scope'
                        defaultMessage='Update Scope'
                    />
                </Typography>
                <Grid item lg={5} className={classes.topics}>
                    <APIPropertyField name='Name'>
                        <TextField
                            disabled
                            fullWidth
                            id='name'
                            type='text'
                            name='name'
                            margin='normal'
                            value={apiScope.name}
                        />
                    </APIPropertyField>
                    <APIPropertyField name='Description'>
                        <TextField
                            style={{
                                width: '100%',
                            }}
                            id='description'
                            name='description'
                            helperText={<FormattedMessage
                                id='Apis.Details.Scopes.CreateScope.short.description.about.the.scope'
                                defaultMessage='Short description about the scope'
                            />}
                            margin='normal'
                            type='text'
                            onChange={this.handleInputs}
                            value={this.state.apiScope.description}
                        />
                    </APIPropertyField>
                    <APIPropertyField name='Roles'>
                        <TagsInput
                            value={this.state.apiScope.bindings.values}
                            onChange={this.handleInputs}
                            onlyUnique
                        />
                    </APIPropertyField>
                    <Button
                        variant='contained'
                        color='primary'
                        onClick={this.updateScope}
                        className={classes.buttonSave}
                    >
                        <FormattedMessage
                            id='Apis.Details.Scopes.CreateScope.save'
                            defaultMessage='Save'
                        />
                    </Button>
                    <Link to={url}>
                        <Button
                            variant='contained'
                            color='primary'
                            className={classes.buttonCancel}
                        >
                            <FormattedMessage
                                id='Apis.Details.Scopes.CreateScope.cancel'
                                defaultMessage='Cancel'
                            />
                        </Button>
                    </Link>
                </Grid>
            </Grid>
        );
    }
}

EditScope.propTypes = {
    match: PropTypes.shape({
        params: PropTypes.object,
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
};

EditScope.defaultProps = {
    match: { params: {} },
};

export default injectIntl(withRouter(withStyles(styles)(EditScope)));
