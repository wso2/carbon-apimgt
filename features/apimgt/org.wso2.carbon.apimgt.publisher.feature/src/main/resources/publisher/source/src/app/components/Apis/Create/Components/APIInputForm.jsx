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

import React, { Component } from 'react';
import PropTypes from 'prop-types';
import TextField from '@material-ui/core/TextField';
import { withStyles } from '@material-ui/core/styles';
import FormControl from '@material-ui/core/FormControl';
import { ScopeValidation, resourceMethod, resourcePath } from 'AppData/ScopeValidation';
import { FormattedMessage, injectIntl } from 'react-intl';
import API from 'AppData/api';
import Policies from 'AppComponents/Apis/Details/LifeCycle/Policies';

const styles = theme => ({
    FormControl: {
        padding: `0 0 0 ${theme.spacing.unit}px`,
        width: '100%',
        marginTop: 0,
    },
    FormControlOdd: {
        padding: `0 0 0 ${theme.spacing.unit}px`,
        backgroundColor: theme.palette.background.paper,
        width: '100%',
        marginTop: 0,
    },
});
/**
 * @export @inheritDoc
 * @class InputForm
 * @extends {Component}
 */
class APIInputForm extends Component {
    /**
     * Creates an instance of InputForm.
     * @param {any} props @inheritDoc
     * @memberof InputForm
     */
    constructor(props) {
        super(props);
        this.state = {
            policies: [],
        };
    }

    /**
     * @inheritDoc
     * @memberof InputForm
     */
    componentDidMount() {
        const promisedTier = API.policies('subscription');

        promisedTier.then((response) => {
            const policies = response.obj;
            this.setState({ policies: policies.list });
        });
    }

    /**
     * @inheritDoc
     * @returns {React.Component}
     * @memberof InputForm
     */
    render() {
        const { policies } = this.state;
        const {
            api, handleInputChange, classes, valid, isAPIProduct, intl,
        } = this.props;
        const policiesProps = { handleInputChange, api, policies };
        const endpoint = api.getProductionEndpoint();
        const nameHelperMsg = isAPIProduct ?
            (<FormattedMessage
                id='api.product.create.name.helper'
                defaultMessage='API Product Name is unique. Special characters and empty space are not allowed'
            />) :
            (<FormattedMessage
                id='api.create.name.helper'
                defaultMessage='API Name is unique. Special characters and empty space are not allowed'
            />);
        const contextHelperMsg = isAPIProduct ?
            (<FormattedMessage
                id='api.product.create.context.help'
                defaultMessage={'The API product context is used by the Gateway' +
                    ' to identify the API product. Therefore, the API productcontext must be unique. ' +
                    ' into the context. For example, {version}/phoneverify.'
                }
            />)
            :
            (<FormattedMessage
                id='api.create.context.help'
                defaultMessage={'The API context is used by the Gateway' +
                    ' to identify the API. Therefore, the API context must' +
                    ' be unique. You can define the APIs version as a parameter' +
                    ' of its context by adding the {version}' +
                    ' into the context. For example, {version}/phoneverify.'}
            />);

        return (
            <React.Fragment>
                <FormControl margin='normal' className={classes.FormControl}>
                    <TextField
                        error={valid.name.empty}
                        fullWidth
                        id='name'
                        label={<FormattedMessage id='name' defaultMessage='Name' />}
                        placeholder={isAPIProduct ? intl.formatMessage({
                            id: 'Apis.Create.Components.APIInputForm.apiproduct.name.placeholder',
                            defaultMessage: 'eTicketing',
                        }) : intl.formatMessage({
                            id: 'Apis.Create.Components.APIInputForm.api.name.placeholder',
                            defaultMessage: 'eTicketing',
                        })}

                        helperText={
                            valid.name.empty ? (
                                <FormattedMessage id='error.empty' defaultMessage='This field cannot be empty.' />
                            ) : (
                                nameHelperMsg
                            )
                        }
                        type='text'
                        name='name'
                        margin='normal'
                        value={api.name || ''}
                        onChange={handleInputChange}
                        InputLabelProps={{
                            shrink: true,
                        }}
                        autoFocus
                    />
                </FormControl>
                {isAPIProduct ? null : (
                    <FormControl margin='normal' className={classes.FormControlOdd}>
                        <TextField
                        // TODO: These lines were commented because they need to be there but currently
                        // REST API doesn't support API versioning.So when we implement the versioning
                        // support we could simply uncomment those 2 lines and allow the user to
                        // provide version numbers. ~tmkb
                        // InputLabelProps={inputLabelClass}
                        // value={this.state.apiFields.apiVersion}
                            error={valid.version.empty}
                            fullWidth
                            label={<FormattedMessage id='version' defaultMessage='Version' />}
                            id='version'
                            placeholder='E.g: 1.0.0'
                            helperText={
                                valid.version.empty ? (
                                    <FormattedMessage id='error.empty' defaultMessage='This field cannot be empty.' />
                                ) : (
                                    ''
                                )
                            }
                            type='text'
                            name='version'
                            margin='normal'
                            value={api.version || ''}
                            onChange={handleInputChange}
                            InputLabelProps={{
                                shrink: true,
                            }}
                        />
                    </FormControl>
                )}
                <FormControl margin='normal' className={isAPIProduct ? classes.FormControlOdd : classes.FormControl}>
                    <TextField
                        error={valid.context.empty}
                        fullWidth
                        id='context'
                        label={<FormattedMessage id='context' defaultMessage='Context' />}
                        placeholder={intl.formatMessage({
                            id: 'Apis.Create.Components.APIInputForm.context.placeholder',
                            defaultMessage: '/events/{id}/sales',
                        })}
                        helperText={
                            valid.context.empty ? (
                                <FormattedMessage
                                    id='error.empty'
                                    defaultMessage='This field cannot be empty.'
                                />
                            ) : (
                                contextHelperMsg
                            )
                        }
                        InputLabelProps={{
                            shrink: true,
                        }}
                        type='text'
                        name='context'
                        margin='normal'
                        value={api.context}
                        onChange={handleInputChange}
                    />
                </FormControl>
                {isAPIProduct ? null : (
                    <FormControl margin='normal' className={classes.FormControlOdd}>
                        <TextField
                            fullWidth
                            id='endpoint'
                            placeholder='E.g: http://appserver/resource'
                            helperText={
                                valid.context.empty ? (
                                    <FormattedMessage id='error.empty' defaultMessage='This field cannot be empty.' />
                                ) : (
                                    <FormattedMessage
                                        id='api.create.endpoint.help'
                                        defaultMessage={
                                            'This is the actual endpoint' +
                                            ' where the API implementation can be found'
                                        }
                                    />
                                )
                            }
                            InputLabelProps={{
                                shrink: true,
                            }}
                            label={<FormattedMessage id='endpoint' defaultMessage='Endpoint' />}
                            type='text'
                            name='endpoint'
                            margin='normal'
                            value={endpoint}
                            onChange={handleInputChange}
                        />
                    </FormControl>
                )}
                <FormControl margin='normal' className={classes.FormControl}>
                    <ScopeValidation
                        resourcePath={isAPIProduct ? resourcePath.API_PRODUCTS : resourcePath.API_CHANGE_LC}
                        resourceMethod={resourceMethod.POST}
                    >
                        {policies && <Policies {...policiesProps} isAPIProduct={isAPIProduct} />}
                    </ScopeValidation>
                </FormControl>
            </React.Fragment>
        );
    }
}

APIInputForm.propTypes = {
    intl: PropTypes.shape({
        formatMessage: PropTypes.func,
    }).isRequired,
    api: PropTypes.shape({}).isRequired,
    handleInputChange: PropTypes.func.isRequired,
    classes: PropTypes.shape({}).isRequired,
    valid: PropTypes.shape({}).isRequired,
    isAPIProduct: PropTypes.shape({}).isRequired,
};

export default injectIntl(withStyles(styles)(APIInputForm));
