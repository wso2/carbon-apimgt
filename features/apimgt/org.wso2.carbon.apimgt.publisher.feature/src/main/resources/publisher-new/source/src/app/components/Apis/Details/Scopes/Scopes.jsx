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
import { FormattedMessage } from 'react-intl';
import CardActions from '@material-ui/core/CardActions';
import CardContent from '@material-ui/core/CardContent';
import Divider from '@material-ui/core/Divider';
import Button from '@material-ui/core/Button';
import withStyles from '@material-ui/core/styles/withStyles';
import { Link } from 'react-router-dom';

const styles = theme => ({
    buttonProgress: {
        position: 'relative',
        margin: theme.spacing.unit,
    },
    headline: { paddingTop: theme.spacing.unit * 1.25, paddingLeft: theme.spacing.unit * 2.5 },
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
        this.state = {
            apiScopes: null,
            apiScope: {},
            roles: [],
        };
        this.deleteScope = this.deleteScope.bind(this);
        this.updateScope = this.updateScope.bind(this);
        this.handleInputs = this.handleInputs.bind(this);
        this.addScope = this.addScope.bind(this);
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
                Alert.error('Something went wrong while updating the ' + scope.name + ' Scope!');
                return;
            }
            Alert.success(scope.name + ' Scope added successfully!');
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
        const { api } = this.props;
        const { scopes } = api;
        const { classes } = this.props;
        const url = `/apis/${api.id}/scopes/create`;

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
                                    id='create.scopes'
                                    defaultMessage='Create Scopes'
                                />
                            </Typography>
                            <Divider />
                            <CardContent>
                                <Typography align='justify' component='p'>
                                    <FormattedMessage
                                        id='create.scope.description'
                                        defaultMessage={'Scopes enable fine-grained access control to API resources'
                                        + ' based on user roles.'}
                                    />
                                </Typography>
                            </CardContent>
                            <CardActions>
                                <Link to={url}>
                                    <Button variant='contained' color='primary' className={classes.button}>
                                        <FormattedMessage
                                            id='create.scopes'
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
            <div>
                {/* <Card
                    title='Add Scope'
                    style={{
                        width: '100%',
                        marginBottom: 20,
                    }}
                >
                    <Row type='flex' justify='start'>
                        <Col span={4}> Scope Name </Col>
                        <Col span={10}>
                            <Input id='name' onChange={this.handleInputs} value={apiScope.name || ''} />
                        </Col>
                    </Row>
                    <br />
                    <Row type='flex' justify='start'>
                        <Col span={4}> Description </Col>
                        <Col span={10}>
                            <Input id='description' onChange={this.handleInputs} value={apiScope.description || ''} />
                        </Col>
                    </Row>
                    <br />
                    <Row type='flex' justify='start'>
                        <Col span={4}> Roles </Col>
                        <Col span={10}>
                            <TagsInput
                                value={roles}
                                onChange={this.handleInputs}
                                onlyUnique
                                inputProps={{
                                    placeholder: 'add a role',
                                }}
                            />
                        </Col>
                    </Row>
                    <br />
                    <Row type='flex' justify='start'>
                        <Col span={5} />
                        <Col span={10}>
                            <button onClick={this.addScope}> Add Scope to API </button>
                        </Col>
                        <Col span={5} />
                    </Row>
                </Card>
                {Object.keys(apiScopes).map((key) => {
                    const scope = apiScopes[key];
                    return (
                        <Scope
                            name={scope.name}
                            description={scope.description}
                            api_uuid={this.api_uuid}
                            deleteScope={this.deleteScope}
                            key={key}
                            updateScope={this.updateScope}
                        />
                    );
                })} */}
            </div>
        );
    }
}

Scopes.propTypes = {
    match: PropTypes.shape({
        params: PropTypes.object,
    }),
    api: PropTypes.shape({}).isRequired,
    classes: PropTypes.shape({}).isRequired,
};

Scopes.defaultProps = {
    match: { params: {} },
};

export default withStyles(styles)(Scopes);
