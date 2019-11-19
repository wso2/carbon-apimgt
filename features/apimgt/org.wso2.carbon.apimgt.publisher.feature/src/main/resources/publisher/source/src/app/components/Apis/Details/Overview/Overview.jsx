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

import React, { Component } from 'react';
import Grid from '@material-ui/core/Grid';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Checkbox from '@material-ui/core/Checkbox';
import { withStyles } from '@material-ui/core/styles';
import PropTypes from 'prop-types';
import TextField from '@material-ui/core/TextField';
import ChipInput from 'material-ui-chip-input';
import Typography from '@material-ui/core/Typography';
import Divider from '@material-ui/core/Divider';
import moment from 'moment';
import Button from '@material-ui/core/Button';
import AddIcon from '@material-ui/icons/Add';

import { Progress } from 'AppComponents/Shared';
import Api from 'AppData/api';
import Alert from 'AppComponents/Shared/Alert';

import APIPropertyField from './APIPropertyField';
import BusinessPlans from './BusinessPlans';
import AdditionalProperty from './AdditionalProperty';

const styles = () => ({
    imageSideContent: {
        display: 'inline-block',
        paddingLeft: 20,
    },
    imageWrapper: {
        display: 'flex',
        flexAlign: 'top',
    },
    headline: {
        marginTop: 20,
    },
    titleCase: {
        textTransform: 'capitalize',
    },
    chip: {
        marginLeft: 0,
        cursor: 'pointer',
    },
    openNewIcon: {
        display: 'inline-block',
        marginLeft: 20,
    },
    endpointsWrapper: {
        display: 'flex',
        justifyContent: 'flex-start',
    },
    button: {
        width: 50,
        marginTop: 20,
    },
});
/**
 * Handle API overview/details of an individual APIs in Publisher app.
 * @class Overview
 * @extends {Component}
 */
class Overview extends Component {
    /**
     * Extract WSDL file name from the `content-disposition` in the response of GET WSDL file request giving the API ID
     * @static
     * @param {String} contentDispositionHeader Value of the content-disposition header
     * @returns {String} filename
     * @memberof Overview
     */
    static getWSDLFileName(contentDispositionHeader) {
        let filename = 'default.wsdl';
        if (contentDispositionHeader && contentDispositionHeader.indexOf('attachment') !== -1) {
            const filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
            const matches = filenameRegex.exec(contentDispositionHeader);
            if (matches !== null && matches[1]) {
                filename = matches[1].replace(/['"]/g, '');
            }
        }
        return filename;
    }

    /**
     * Creates an instance of Overview.
     * @param {any} props @inheritDoc
     * @memberof Overview
     */
    constructor(props) {
        super(props);
        this.state = {
            api: null,
            editableDescriptionText: null,
            additionalProperties: props.api.additionalProperties,
        };
        this.downloadWSDL = this.downloadWSDL.bind(this);
        this.handleTagChange = this.handleTagChange.bind(this);
        this.handleTransportChange = this.handleTransportChange.bind(this);
        this.editDescription = this.editDescription.bind(this);
        this.handleInput = this.handleInput.bind(this);
        this.handleAddAdditionalProperties = this.handleAddAdditionalProperties.bind(this);
        this.handleDeleteAdditionalProperties = this.handleDeleteAdditionalProperties.bind(this);
    }


    /**
     *
     *
     * @memberof Overview
     */
    downloadWSDL() {
        const { id } = this.props.api;
        const api = new Api();
        const promisedWSDL = api.getWSDL(id);
        promisedWSDL.then((response) => {
            const windowUrl = window.URL || window.webkitURL;
            const binary = new Blob([response.data]);
            const url = windowUrl.createObjectURL(binary);
            const anchor = document.createElement('a');
            anchor.href = url;
            if (response.headers['content-disposition']) {
                anchor.download = Overview.getWSDLFileName(response.headers['content-disposition']);
            } else {
                // assumes a single WSDL in text format
                anchor.download = this.state.api.provider + '-'
                + this.state.api.name + '-'
                + this.state.api.version + '.wsdl';
            }
            anchor.click();
            windowUrl.revokeObjectURL(url);
        });
    }

    /**
     * Handle tag update
     *
     * @param {string} apiId API Id
     * @param {string[]} tags Tag List
     */
    handleTagChange(tags) {
        const api = new Api();
        const { id } = this.props.api;
        const currentAPI = this.state.api;
        const promisedApi = api.get(id);
        promisedApi
            .then((getResponse) => {
                const apiData = getResponse.body;
                apiData.tags = tags;
                const promisedUpdate = api.update(apiData);
                promisedUpdate
                    .then((updateResponse) => {
                        this.setState({ api: updateResponse.body });
                    })
                    .catch((errorResponse) => {
                        console.error(errorResponse);
                        Alert.error('Error occurred while updating tags');
                        this.setState({ api: getResponse.body });
                    });
            })
            .catch((errorResponse) => {
                console.error(errorResponse);
                Alert.error('Error occurred while retrieving API');
                this.setState({ api: currentAPI });
            });
    }

    /**
     * Handle transport update
     *
     * @param {Event} event Event
     */
    handleTransportChange(event) {
        const api = new Api();
        const { id } = this.props.api;
        const currentAPI = this.state.api;
        const promisedApi = api.get(id);
        promisedApi
            .then((getResponse) => {
                const apiData = getResponse.body;
                apiData.transport = event.target.value;
                this.setState({ api: apiData });
                const promisedUpdate = api.update(apiData);
                promisedUpdate
                    .then((updateResponse) => {
                        this.setState({ api: updateResponse.body });
                    })
                    .catch((errorResponse) => {
                        console.error(errorResponse);
                        Alert.error('Error occurred while updating transports');
                        this.setState({ api: getResponse.body });
                    });
            })
            .catch((errorResponse) => {
                console.error(errorResponse);
                Alert.error('Error occurred while retrieving API');
                this.setState({ api: currentAPI });
            });
    }

    /**
     * Handle adding Additional properties
     */
    handleAddAdditionalProperties() {
        this.setState((cState) => ({
            additionalProperties: [
                ...cState.additionalProperties,
                {
                    key: '',
                    value: '',
                },
            ],
        }));
    }

    /**
     * Handle delete Additional properties
     *
     * @param {AdditionalProperty} key AdditionalProperty
     */
    handleDeleteAdditionalProperties(key) {
        const { additionalProperties } = this.state;
        this.setState({
            additionalProperties: additionalProperties.filter((property) => property.key !== key),
        });
    }

    /**
     * Edit description
     *
     * @param {SyntheticEvent} sEvent Synthetic Event
     */
    editDescription() {
        const api = new Api();
        const apiId = this.props.api.id;
        const { editableDescriptionText } = this.state;
        const promisedApi = api.get(apiId);
        promisedApi
            .then((getResponse) => {
                const apiData = getResponse.body;
                apiData.description = editableDescriptionText;
                const promisedUpdate = api.update(apiData);
                promisedUpdate
                    .then((updateResponse) => {
                        this.setState({ api: updateResponse.body });
                    })
                    .catch((errorResponse) => {
                        console.error(errorResponse);
                        Alert.error('Error occurred while updating API description');
                    });
            })
            .catch((errorResponse) => {
                console.error(errorResponse);
                Alert.error('Error occurred while retrieving API');
            });
    }

    /**
     * Update state with input
     *
     * @param {SyntheticEvent} sEvent Synthetic Event
     */
    handleInput(sEvent) {
        this.setState({ [sEvent.target.id]: sEvent.target.value });
    }

    renderAdditionalProperties(additionalProperties) {
        const { isEditable } = this.props;
        const items = [];
        for (const key in additionalProperties) {
            if (Object.prototype.hasOwnProperty.call(additionalProperties, key)) {
                items.push(<AdditionalProperty
                    property={{ key, value: additionalProperties[key] }}
                    isEditable={isEditable}
                    onDelete={this.handleDeleteAdditionalProperties}
                />);
            }
        }
        return items;
    }

    /** @inheritDoc */
    render() {
        const { api, isEditable, classes } = this.props;
        const { additionalProperties } = this.state;
        if (!api) {
            return <Progress />;
        }

        return (
            <Grid container spacing={0} direction='column' justify='flex-start' alignItems='stretch'>
                <Grid item container direction='row' justify='flex-end'>
                    <span>
Last Updated :
                        {moment(api.lastUpdatedTime).fromNow()}
                    </span>
                </Grid>
                <APIPropertyField name='Description'>
                    <TextField
                        style={{
                            border: '#3f50b5 1px solid',
                            padding: '8px',
                            borderRadius: '15px',
                            width: '50%',
                        }}
                        id='api-description'
                        label={isEditable && 'Description'}
                        value={api.description}
                        placeholder='No Value!'
                        helperText='A short description about the API'
                        margin='normal'
                        multiline
                        rows={2}
                        rowsMax={8}
                        InputProps={{
                            readOnly: !isEditable,
                        }}
                    />
                </APIPropertyField>
                <APIPropertyField name='Endpoints'>
                    <Grid item lg={5}>
                        <TextField
                            fullWidth
                            id='api-endpoint-production'
                            label={isEditable && 'Endpoint'}
                            value={
                                api.getProductionEndpoint()
                            }
                            placeholder='No Value!'
                            helperText='Production'
                            margin='normal'
                            InputProps={{
                                readOnly: !isEditable,
                            }}
                        />
                    </Grid>
                    <Grid item lg={5}>
                        <TextField
                            fullWidth
                            id='api-endpoint-sandbox'
                            label={isEditable && 'Endpoint'}
                            value={api.getSandboxEndpoint()}
                            placeholder='No Value!'
                            helperText='Sandbox'
                            margin='normal'
                            InputProps={{
                                readOnly: !isEditable,
                            }}
                        />
                    </Grid>
                </APIPropertyField>
                <APIPropertyField name='Tags'>
                    <ChipInput disabled value={api.tags} />
                </APIPropertyField>
                <APIPropertyField name='Context'>
                    <TextField
                        id='api-context'
                        label={isEditable && 'Context'}
                        value={api.context}
                        placeholder='No Value!'
                        helperText='Context of the API'
                        margin='normal'
                        InputProps={{
                            readOnly: !isEditable,
                        }}
                    />
                </APIPropertyField>
                <APIPropertyField name='Default API Version'>
                    <FormControlLabel
                        control={<Checkbox checked={api.isDefaultVersion} value='isDefaultVersion' color='primary' />}
                        label='Default Version'
                    />
                </APIPropertyField>
                <BusinessPlans api={api} />
                <Grid item>
                    <Typography variant='h5'> Business Information</Typography>
                    <Divider />
                </Grid>
                <APIPropertyField name='Business Owner'>
                    <TextField
                        id='api-business-owner'
                        label={isEditable && 'Business Owner'}
                        defaultValue={api.businessInformation.businessOwner}
                        placeholder='No Value!'
                        helperText='Business Owner'
                        margin='normal'
                        InputProps={{
                            readOnly: !isEditable,
                        }}
                    />
                </APIPropertyField>
                <APIPropertyField name='Business Owner Email'>
                    <TextField
                        id='api-business-owner-email'
                        label={isEditable && 'Business Owner Email'}
                        defaultValue={api.businessInformation.businessOwnerEmail}
                        placeholder='No Value!'
                        helperText='Business Owner Email'
                        margin='normal'
                        InputProps={{
                            readOnly: !isEditable,
                        }}
                    />
                </APIPropertyField>
                <APIPropertyField name='Technical Owner'>
                    <TextField
                        id='api-technical-owner'
                        label={isEditable && 'Technical Owner'}
                        defaultValue={api.businessInformation.technicalOwner}
                        placeholder='No Value!'
                        helperText='Technical Owner'
                        margin='normal'
                        InputProps={{
                            readOnly: !isEditable,
                        }}
                    />
                </APIPropertyField>
                <APIPropertyField name='Technical Owner Email'>
                    <TextField
                        id='api-technical-owner-email'
                        label={isEditable && 'Technical Owner Email'}
                        defaultValue={api.businessInformation.technicalOwnerEmail}
                        placeholder='No Value!'
                        helperText='Technical Owner Email'
                        margin='normal'
                        InputProps={{
                            readOnly: !isEditable,
                        }}
                    />
                </APIPropertyField>
                {additionalProperties && (
                    <>
                        <Grid item>
                            <Typography variant='h5'> Additional Properties</Typography>
                            <Divider />
                        </Grid>
                        <Button
                            variant='outlined'
                            id='add'
                            aria-label='Add'
                            onClick={this.handleAddAdditionalProperties}
                            className={classes.button}
                        >
                            <AddIcon id='1' />
                        </Button>
                        {this.renderAdditionalProperties(additionalProperties)}
                    </>
                )}
            </Grid>
        );
    }
}

Overview.defaultProps = {
    isEditable: false,
};

Overview.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({
        id: PropTypes.string,
        additionalProperties: PropTypes.shape({}).isRequired,
    }).isRequired,
    isEditable: PropTypes.bool,
};

export default withStyles(styles)(Overview);
