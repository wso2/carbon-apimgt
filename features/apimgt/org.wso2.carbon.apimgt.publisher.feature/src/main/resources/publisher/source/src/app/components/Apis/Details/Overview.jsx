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
import OpenInNew from 'material-ui-icons/OpenInNew';

import { Progress } from '../../Shared';
import ResourceNotFound from '../../Base/Errors/ResourceNotFound';
import Api from '../../../data/api';
import ImageGenerator from '../Listing/ImageGenerator';
import Alert from '../../Shared/Alert';

const styles = theme => ({
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
 * API Overview Coponent
 */
class Overview extends Component {
    constructor(props) {
        super(props);
        this.state = {
            api: null,
            notFound: false,
            openMenu: false,
        };
        this.api_uuid = this.props.match.params.api_uuid;
        this.downloadWSDL = this.downloadWSDL.bind(this);
    }

    componentDidMount() {
        const api = new Api();
        const promised_api = api.get(this.api_uuid);
        promised_api
            .then((response) => {
                this.setState({ api: response.obj });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const status = error.status;
                if (status === 404) {
                    this.setState({ notFound: true });
                }
            });
    }

    downloadWSDL() {
        const api = new Api();
        const promised_wsdl = api.getWSDL(this.api_uuid);
        promised_wsdl.then((response) => {
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

    static getWSDLFileName(content_disposition_header) {
        let filename = 'default.wsdl';
        if (content_disposition_header && content_disposition_header.indexOf('attachment') !== -1) {
            const filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
            const matches = filenameRegex.exec(content_disposition_header);
            if (matches !== null && matches[1]) {
                filename = matches[1].replace(/['"]/g, '');
            }
        }
        return filename;
    }

    /**
     * Handle tag update
     *
     * @param {string} apiId API Id
     * @param {string[]} tags Tag List
     */
    handleTagChange(apiId, tags) {
        const api = new Api();
        const promisedApi = api.get(apiId);
        promisedApi
            .then((response) => {
                const apiData = JSON.parse(response.data);
                apiData.tags = tags;
                const promisedUpdate = api.update(apiData);
                promisedUpdate.catch((errorResponse) => {
                    console.log(JSON.stringify(errorResponse));
                    Alert.error('Error occurred while updating tags');
                });
            })
            .catch((errorResponse) => {
                console.log(JSON.stringify(errorResponse));
                Alert.error('Error occurred while retrieving API');
            });
    }

    /** @inheritDoc */
    render() {
        const api = this.state.api;
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
                    <div>
                        {/* Description */}
                        <Typography variant='subheading' className={classes.headline}>
                            {api.description}
                        </Typography>
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
                    {api.endpoint.map(ep => (
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
                                <a onClick={this.downloadWSDL}>
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
                                    <ChipInput
                                        defaultValue={api.tags}
                                        onChange={(api.id, chips => this.handleTagChange(api.id, chips))}
                                    />
                                </div>
                            ) : (
                                <Typography variant='subheading' align='left' className={classes.headline}>
                                    &lt; NOT SET FOR THIS API &gt;
                                </Typography>
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
                            {api.transport && api.transport.length ? (
                                <Link to={'/apis/' + api.id + '/transport'}>
                                    <Typography variant='subheading' align='left' className={classes.headline}>
                                        {api.transport.map(trans => trans + ', ')}
                                    </Typography>
                                </Link>
                            ) : (
                                <Link to={'/apis/' + api.id + '/transport'}>
                                    <Typography variant='subheading' align='left' className={classes.headline}>
                                        &lt; NOT SET FOR THIS API &gt;
                                    </Typography>
                                </Link>
                            )}
                            <Typography variant='caption' gutterBottom align='left'>
                                Transport
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
Overview.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(Overview);
