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
import { Link } from 'react-router-dom';
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';
import { withStyles } from '@material-ui/core/styles';
import PropTypes from 'prop-types';
import ChipInput from 'material-ui-chip-input';
import OpenInNew from '@material-ui/icons/OpenInNew';
import Tooltip from '@material-ui/core/Tooltip';
import Select from '@material-ui/core/Select';
import Input from '@material-ui/core/Input';
import InputLabel from '@material-ui/core/InputLabel';
import MenuItem from '@material-ui/core/MenuItem';
import ListItemText from '@material-ui/core/ListItemText';
import Checkbox from '@material-ui/core/Checkbox';
import EditIcon from '@material-ui/icons/EditAttributes';
import { FormControl } from '@material-ui/core/';
import IconButton from '@material-ui/core/IconButton';

import { Progress } from '../../../Shared';
import Api from '../../../../data/api';
import Alert from '../../../Shared/Alert';

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
            notFound: false,
            editDescription: false,
            editableDescriptionText: null,
        };
        this.downloadWSDL = this.downloadWSDL.bind(this);
        this.handleTagChange = this.handleTagChange.bind(this);
        this.handleTransportChange = this.handleTransportChange.bind(this);
        this.editDescription = this.editDescription.bind(this);
        this.handleInput = this.handleInput.bind(this);
    }

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
                anchor.download =
                    this.state.api.provider + '-' + this.state.api.name + '-' + this.state.api.version + '.wsdl';
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
     * Edit description
     *
     * @param {SyntheticEvent} sEvent Synthetic Event
     */
    editDescription(sEvent) {
        const { id } = sEvent.currentTarget;
        if (id === 'edit-description-button') {
            this.setState({ editDescription: true });
        } else {
            this.setState({ editDescription: false });
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
    }

    /**
     * Update state with input
     *
     * @param {SyntheticEvent} sEvent Synthetic Event
     */
    handleInput(sEvent) {
        this.setState({ [sEvent.target.id]: sEvent.target.value });
    }

    /** @inheritDoc */
    render() {
        const { editDescription, editableDescriptionText } = this.state;
        const { api, classes } = this.props;
        if (!api) {
            return <Progress />;
        }

        return (
            <Grid container>
                <Grid item>Name</Grid>
                <Grid item lg={8} md={8} sm={8} xs={8}>
                    Value
                </Grid>
            </Grid>
        );
    }
}

Overview.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(Overview);
