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
import Grid from 'material-ui/Grid';
import Typography from 'material-ui/Typography';
import { withStyles } from 'material-ui/styles';
import PropTypes from 'prop-types';
import ChipInput from 'material-ui-chip-input';
import OpenInNew from '@material-ui/icons/OpenInNew';
import Tooltip from 'material-ui/Tooltip';
import Select from 'material-ui/Select';
import Input, { InputLabel } from 'material-ui/Input';
import { MenuItem } from 'material-ui/Menu';
import { ListItemText } from 'material-ui/List';
import Checkbox from 'material-ui/Checkbox';
import EditIcon from '@material-ui/icons/ModeEdit';
import { FormControl } from 'material-ui/Form';
import IconButton from 'material-ui/IconButton';

import { Progress } from '../../Shared';
import ResourceNotFound from '../../Base/Errors/ResourceNotFound';
import Api from '../../../data/api';
import ImageGenerator from '../Listing/ImageGenerator';
import Alert from '../../Shared/Alert';

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
        this.apiUUID = this.props.match.params.apiUUID;
        this.downloadWSDL = this.downloadWSDL.bind(this);
        this.handleTagChange = this.handleTagChange.bind(this);
        this.handleTransportChange = this.handleTransportChange.bind(this);
        this.editDescription = this.editDescription.bind(this);
        this.handleInput = this.handleInput.bind(this);
    }

    /**
     * @inheritDoc
     * @memberof Overview
     */
    componentDidMount() {
        const api = new Api();
        const promisedApi = api.get(this.apiUUID);
        promisedApi
            .then((response) => {
                let apiDescription;
                if (response.body.description && response.body.description.length) {
                    apiDescription = response.body.description;
                } else {
                    apiDescription = '< NOT SET FOR THIS API >';
                }
                this.setState({ api: response.body, editableDescriptionText: apiDescription });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const { status } = error;
                if (status === 404) {
                    this.setState({ notFound: true });
                }
            });
    }

    downloadWSDL() {
        const api = new Api();
        const promisedWSDL = api.getWSDL(this.apiUUID);
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
        const { apiUUID } = this;
        const currentAPI = this.state.api;
        const promisedApi = api.get(apiUUID);
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
        const { apiUUID } = this;
        const currentAPI = this.state.api;
        const promisedApi = api.get(apiUUID);
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
            const { apiUUID } = this;
            const { editableDescriptionText } = this.state;
            const promisedApi = api.get(apiUUID);
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
        const { api, editDescription, editableDescriptionText } = this.state;
        if (this.state.notFound) {
            return <ResourceNotFound message={this.props.resourceNotFountMessage} />;
        }
        if (!api) {
            return <Progress />;
        }
        const { classes } = this.props;

        return (
            <Grid container>
                <Grid item xs={12} sm={6} md={6} lg={4} className={classes.imageWrapper}>
                    <ImageGenerator apiName={api.name} />
                    <div className={classes.imageSideContent}>
                        <Typography variant='headline'>
                            {api.version} {api.isDefaultVersion && <span>( Default )</span>}
                            {/* TODO We need to show the default verison and a link to it here if this
                            is not the default version */}
                        </Typography>
                        <Typography variant='caption' gutterBottom align='left'>
                            Version
                        </Typography>
                        {/* Context */}
                        <Typography variant='headline' className={classes.headline}>
                            {api.context}
                        </Typography>
                        <Typography variant='caption' gutterBottom align='left'>
                            Context
                        </Typography>
                        {/* Visibility */}
                        <Typography variant='headline' className={classes.headline}>
                            {api.lifeCycleStatus}
                        </Typography>
                        <Typography variant='caption' gutterBottom align='left'>
                            Lifecycle Status
                        </Typography>
                    </div>
                </Grid>
                <Grid item xs={12} sm={6} md={6} lg={8} className={classes.headline}>
                    {/* Description */}
                    <div>
                        {editDescription ? (
                            <FormControl fullWidth>
                                <Input
                                    id='editableDescriptionText'
                                    multiline
                                    autoFocus
                                    rowsMax='5'
                                    value={editableDescriptionText}
                                    onChange={this.handleInput}
                                    onBlur={this.editDescription}
                                />
                            </FormControl>
                        ) : (
                            <Typography variant='subheading' gutterBottom align='left'>
                                {editableDescriptionText}
                                <IconButton
                                    id='edit-description-button'
                                    onClick={this.editDescription}
                                    aria-label='Edit Description'
                                >
                                    <EditIcon />
                                </IconButton>
                            </Typography>
                        )}
                        <Typography variant='caption' gutterBottom align='left'>
                            Description
                        </Typography>
                    </div>
                </Grid>
                <Grid item xs={12}>
                    <Typography variant='subheading' align='left'>
                        Created by {api.provider} : {api.createdTime}
                    </Typography>
                    <Typography variant='caption' gutterBottom align='left'>
                        Last update : {api.lastUpdatedTime}
                    </Typography>
                    {/* Endpoints */}
                    {api.endpoint &&
                        api.endpoint.map(ep => (
                            <div key={ep.inline.id}>
                                <div className={classes.endpointsWrapper + ' ' + classes.headline}>
                                    <Link to={'/apis/' + api.id + '/endpoints'} title='Edit endpoint'>
                                        <Typography variant='subheading' align='left'>
                                            {JSON.parse(ep.inline.endpointConfig).serviceUrl}
                                        </Typography>
                                    </Link>
                                    <a
                                        href={JSON.parse(ep.inline.endpointConfig).serviceUrl}
                                        target='_blank'
                                        className={classes.openNewIcon}
                                    >
                                        <OpenInNew />
                                    </a>
                                </div>
                                <Typography variant='caption' gutterBottom align='left'>
                                    <span className={classes.titleCase}>{ep.type}</span> Endpoint
                                </Typography>
                            </div>
                        ))}
                    {api.wsdlUri && (
                        <div>
                            <div className={classes.endpointsWrapper + ' ' + classes.headline}>
                                <a onClick={this.downloadWSDL} onKeyDown={this.downloadWSDL}>
                                    <Typography variant='subheading' align='left'>
                                        {api.wsdlUri}
                                    </Typography>
                                </a>
                            </div>
                            <Typography variant='caption' gutterBottom align='left'>
                                WSDL
                            </Typography>
                        </div>
                    )}
                    <Grid container>
                        <Grid item xs={12} sm={6} md={4} lg={3}>
                            {api.policies && api.policies.length ? (
                                <Link to={'/apis/' + api.id + '/policies'}>
                                    <Typography variant='subheading' align='left' className={classes.headline}>
                                        {api.policies.map(policy => policy + ', ')}
                                    </Typography>
                                </Link>
                            ) : (
                                <Link to={'/apis/' + api.id + '/policies'}>
                                    <Typography variant='subheading' align='left' className={classes.headline}>
                                        &lt; NOT SET FOR THIS API &gt;
                                    </Typography>
                                </Link>
                            )}
                            <Typography variant='caption' gutterBottom align='left'>
                                Throttling Policies
                            </Typography>
                        </Grid>
                        <Grid item xs={12} sm={6} md={4} lg={3}>
                            {api.securityScheme && api.securityScheme.length ? (
                                <Link to={'/apis/' + api.id + '/securityScheme'}>
                                    <Typography variant='subheading' align='left' className={classes.headline}>
                                        {api.securityScheme.map(policy => policy + ', ')}
                                    </Typography>
                                </Link>
                            ) : (
                                <Link to={'/apis/' + api.id + '/securityScheme'}>
                                    <Typography variant='subheading' align='left' className={classes.headline}>
                                        &lt; NOT SET FOR THIS API &gt;
                                    </Typography>
                                </Link>
                            )}
                            <Typography variant='caption' gutterBottom align='left'>
                                Security Scheme
                            </Typography>
                        </Grid>
                        <Grid item xs={12} sm={6} md={4} lg={3}>
                            {api.tags && api.tags.length ? (
                                <div className={classes.headline}>
                                    <Tooltip
                                        id='tooltip-controlled'
                                        title='Type and hit <Enter> to add Tags'
                                        enterDelay={300}
                                        leaveDelay={300}
                                        placement='top'
                                    >
                                        <ChipInput defaultValue={api.tags} onChange={this.handleTagChange} />
                                    </Tooltip>
                                </div>
                            ) : (
                                <div className={classes.headline}>
                                    <Tooltip
                                        id='tooltip-controlled'
                                        title='Type and hit <Enter> to add Tags'
                                        onClose={this.handleTooltipClose}
                                        enterDelay={300}
                                        leaveDelay={300}
                                        placement='top'
                                    >
                                        <ChipInput
                                            placeholder='< CLICK TO ADD TAGS >'
                                            onChange={this.handleTagChange}
                                        />
                                    </Tooltip>
                                </div>
                            )}
                            <Typography variant='caption' gutterBottom align='left'>
                                Tags
                            </Typography>
                        </Grid>
                        <Grid item xs={12} sm={6} md={4} lg={3}>
                            {api.threatProtectionPolicies && api.threatProtectionPolicies.length ? (
                                <Link to={'/apis/' + api.id + '/security'}>
                                    <Typography variant='subheading' align='left' className={classes.headline}>
                                        {api.threatProtectionPolicies.map(policy => policy + ', ')}
                                    </Typography>
                                </Link>
                            ) : (
                                <Link to={'/apis/' + api.id + '/security'}>
                                    <Typography variant='subheading' align='left' className={classes.headline}>
                                        &lt; NOT SET FOR THIS API &gt;
                                    </Typography>
                                </Link>
                            )}
                            <Typography variant='caption' gutterBottom align='left'>
                                Threat Protection Policies
                            </Typography>
                        </Grid>
                        <Grid item xs={12} sm={6} md={4} lg={3}>
                            <InputLabel htmlFor='transport-checkbox' />
                            {/* TODO:Set placeholder 'Select Transports' */}
                            <Select
                                multiple
                                autoWidth
                                value={api.transport}
                                onChange={this.handleTransportChange}
                                input={<Input id='transport-checkbox' />}
                                renderValue={selected => selected.join(',  ')}
                            >
                                <MenuItem key='HTTP' value='HTTP'>
                                    <Checkbox
                                        checked={api.transport && api.transport.includes('HTTP')}
                                        color='primary'
                                    />
                                    <ListItemText primary='HTTP' />
                                </MenuItem>
                                <MenuItem key='HTTPS' value='HTTPS'>
                                    <Checkbox
                                        checked={api.transport && api.transport.includes('HTTPS')}
                                        color='primary'
                                    />
                                    <ListItemText primary='HTTPS' />
                                </MenuItem>
                            </Select>
                            <Typography variant='caption' gutterBottom align='left'>
                                Transports
                            </Typography>
                        </Grid>
                        <Grid item xs={12} sm={6} md={4} lg={3}>
                            {api.userPermissionsForApi && api.userPermissionsForApi.length ? (
                                <Link to={'/apis/' + api.id + '/userPermissions'}>
                                    <Typography variant='subheading' align='left' className={classes.headline}>
                                        {api.userPermissionsForApi.map(permission => permission + ', ')}
                                    </Typography>
                                </Link>
                            ) : (
                                <Link to={'/apis/' + api.id + '/userPermissions'}>
                                    <Typography variant='subheading' align='left' className={classes.headline}>
                                        &lt; NOT SET FOR THIS API &gt;
                                    </Typography>
                                </Link>
                            )}
                            <Typography variant='caption' gutterBottom align='left'>
                                User Permissions
                            </Typography>
                        </Grid>
                        {api.visibility === 'PUBLIC' ? (
                            <Grid item xs={12} sm={6} md={4} lg={3}>
                                <Link to={'/apis/' + api.id + '/visibility'}>
                                    <Typography variant='subheading' align='left' className={classes.headline}>
                                        PUBLIC
                                    </Typography>
                                </Link>
                                <Typography variant='caption' gutterBottom align='left'>
                                    Visibility ( You can restrict visibility by role or tenant )
                                </Typography>
                            </Grid>
                        ) : (
                            <Grid item xs={12} sm={6} md={4} lg={3}>
                                {api.visibleRoles && api.visibleRoles.length ? (
                                    <Link to={'/apis/' + api.id + '/visibility'}>
                                        <Typography variant='subheading' align='left' className={classes.headline}>
                                            {api.visibleRoles.map(role => role + ', ')}
                                        </Typography>
                                    </Link>
                                ) : (
                                    <Link to={'/apis/' + api.id + '/visibility'}>
                                        <Typography variant='subheading' align='left' className={classes.headline}>
                                            &lt; NOT SET FOR THIS API &gt;
                                        </Typography>
                                    </Link>
                                )}
                                <Typography variant='caption' gutterBottom align='left'>
                                    Visible Roles
                                </Typography>
                            </Grid>
                        )}

                        {api.visibility !== 'PUBLIC' && (
                            <Grid item xs={12} sm={6} md={4} lg={3}>
                                {api.visibleTenants && api.visibleTenants.length ? (
                                    <Link to={'/apis/' + api.id + '/visibility'}>
                                        <Typography variant='subheading' align='left' className={classes.headline}>
                                            {api.visibleTenants.map(tenant => tenant + ', ')}
                                        </Typography>
                                    </Link>
                                ) : (
                                    <Link to={'/apis/' + api.id + '/visibility'}>
                                        <Typography variant='subheading' align='left' className={classes.headline}>
                                            &lt; NOT SET FOR THIS API &gt;
                                        </Typography>
                                    </Link>
                                )}
                                <Typography variant='caption' gutterBottom align='left'>
                                    Visible Tenant
                                </Typography>
                            </Grid>
                        )}
                    </Grid>
                </Grid>
            </Grid>
        );
    }
}

Overview.defaultProps = {
    resourceNotFountMessage: 'Resource not found!',
};

Overview.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    match: PropTypes.shape({
        params: PropTypes.shape({
            apiUUID: PropTypes.string,
        }),
    }).isRequired,
    resourceNotFountMessage: PropTypes.string,
};

export default withStyles(styles)(Overview);
