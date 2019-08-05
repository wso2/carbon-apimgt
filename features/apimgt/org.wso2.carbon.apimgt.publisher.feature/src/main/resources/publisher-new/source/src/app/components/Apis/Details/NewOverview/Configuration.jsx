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
import React from 'react';
import { Link } from 'react-router-dom';
import { FormattedMessage } from 'react-intl';
import PropTypes from 'prop-types';
import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import Chip from '@material-ui/core/Chip';
import Tooltip from '@material-ui/core/Tooltip';
import HelpOutline from '@material-ui/icons/HelpOutline';
import ThumbnailView from 'AppComponents/Apis/Listing/components/ImageGenerator/ThumbnailView';
import API from 'AppData/api';
import ApiContext from '../components/ApiContext';

function Configuration(props) {
    const { parentClasses } = props;
    return (
        <ApiContext.Consumer>
            {({ api }) => (
                <Paper className={parentClasses.root} elevation={1}>
                    <div className={parentClasses.titleWrapper}>
                        <Typography variant='h5' component='h3' className={parentClasses.title}>
                            <FormattedMessage
                                id='Apis.Details.NewOverview.Configuration.configuration'
                                defaultMessage='Configuration'
                            />
                        </Typography>
                        <Link to={((api.apiType === API.CONSTS.APIProduct) ? '/api-products/' : '/apis/') +
                            api.id + '/configuration'}
                        >
                            <Button variant='contained' color='default'>
                                <FormattedMessage
                                    id='Apis.Details.NewOverview.Configuration.edit'
                                    defaultMessage='Edit'
                                />
                            </Button>
                        </Link>
                    </div>
                    <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                        <FormattedMessage
                            id='Apis.Details.NewOverview.Configuration.description'
                            defaultMessage='Description'
                        />
                    </Typography>
                    <Typography component='p' variant='body1'>
                        {api.description && <React.Fragment>{api.description}</React.Fragment>}
                        {!api.description && <React.Fragment>&lt;Description Not Configured&gt;</React.Fragment>}
                    </Typography>
                    <div className={parentClasses.imageContainer}>
                        <div className={parentClasses.imageWrapper}>
                            {/* Thumbnail */}
                            <ThumbnailView api={api} width={200} height={200} isEditable />
                            {/* Provider */}
                            <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                                <FormattedMessage
                                    id='Apis.Details.NewOverview.Configuration.provider'
                                    defaultMessage='Provider'
                                />
                            </Typography>
                            <Typography component='p' variant='body1'>
                                {api.provider && <React.Fragment>{api.provider}</React.Fragment>}
                            </Typography>
                            {/* Type */}
                            {(api.apiType === API.CONSTS.APIProduct) ?
                                null :
                                <div>
                                    <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                                        <FormattedMessage
                                            id='Apis.Details.NewOverview.Configuration.type'
                                            defaultMessage='Type'
                                        />
                                    </Typography>
                                    <Typography component='p' variant='body1'>
                                        {api.type && <React.Fragment>{api.type}</React.Fragment>}
                                        {!api.type && <React.Fragment>?</React.Fragment>}
                                    </Typography>
                                </div>
                            }
                            {/* workflowStatus */}
                            <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                                <FormattedMessage
                                    id='Apis.Details.NewOverview.Configuration.workflow.status'
                                    defaultMessage='Workflow Status'
                                />
                            </Typography>
                            <Typography component='p' variant='body1'>
                                {api.workflowStatus && <React.Fragment>{api.workflowStatus}</React.Fragment>}
                                {!api.workflowStatus && <React.Fragment>?</React.Fragment>}
                            </Typography>
                            {/* Created Time */}
                            <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                                <FormattedMessage
                                    id='Apis.Details.NewOverview.Configuration.created.time'
                                    defaultMessage='Created Time'
                                />
                            </Typography>
                            <Typography component='p' variant='body1'>
                                {api.createdTime && <React.Fragment>{api.createdTime}</React.Fragment>}
                                {!api.createdTime && <React.Fragment>?</React.Fragment>}
                            </Typography>
                            {/* Last Updated Time */}
                            <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                                <FormattedMessage
                                    id='Apis.Details.NewOverview.Configuration.last.updated.time'
                                    defaultMessage='Last Updated Time'
                                />
                            </Typography>
                            <Typography component='p' variant='body1'>
                                {api.lastUpdatedTime && <React.Fragment>{api.lastUpdatedTime}</React.Fragment>}
                                {!api.lastUpdatedTime && <React.Fragment>?</React.Fragment>}
                            </Typography>
                        </div>
                        <div>
                            <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                                <FormattedMessage
                                    id='Apis.Details.NewOverview.Configuration.context'
                                    defaultMessage='Context'
                                />
                            </Typography>
                            <Typography component='p' variant='body1'>
                                {api.context && <React.Fragment>{api.context}</React.Fragment>}
                            </Typography>
                            {/* Version */}
                            <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                                <FormattedMessage
                                    id='Apis.Details.NewOverview.Configuration.version'
                                    defaultMessage='Version'
                                />
                            </Typography>
                            <Typography component='p' variant='body1'>
                                {api.version && <React.Fragment>{api.version}</React.Fragment>}
                            </Typography>
                            {/* Default Version */}
                            <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                                <FormattedMessage
                                    id='Apis.Details.NewOverview.Configuration.default.version'
                                    defaultMessage='Default Version'
                                />
                                <Tooltip
                                    placement='top'
                                    classes={{
                                        tooltip: parentClasses.htmlTooltip,
                                    }}
                                    disableHoverListener
                                    title={
                                        <React.Fragment>
                                            <FormattedMessage
                                                id='Apis.Details.NewOverview.Configuration.tooltip'
                                                defaultMessage={'Marks one API version in a group as ' +
                                                    'the default so that it can be invoked without specifying ' +
                                                    'the version number in the URL. For example, if you mark ' +
                                                    'http://host:port/youtube/2.0 as the default API, ' +
                                                    'requests made to ' +
                                                    'http://host:port/youtube/ are automatically ' +
                                                    'routed to version 2.0.' +
                                                    'If you mark an unpublished API as the default, ' +
                                                    'the previous default published API will still be used' +
                                                    ' as the default until the new default API is published.'}
                                            />
                                        </React.Fragment>
                                    }
                                >
                                    <Button className={parentClasses.helpButton}>
                                        <HelpOutline className={parentClasses.helpIcon} />
                                    </Button>
                                </Tooltip>
                            </Typography>
                            <Typography component='p' variant='body1'>
                                {api.isDefaultVersion && <React.Fragment>Yes</React.Fragment>}
                                {!api.isDefaultVersion && <React.Fragment>No</React.Fragment>}
                            </Typography>
                            {/* Transports */}
                            <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                                <FormattedMessage
                                    id='Apis.Details.NewOverview.Configuration.transports'
                                    defaultMessage='Transports'
                                />
                                <Tooltip
                                    placement='top'
                                    classes={{
                                        tooltip: parentClasses.htmlTooltip,
                                    }}
                                    disableHoverListener
                                    title={
                                        <React.Fragment>
                                            <FormattedMessage
                                                id='Apis.Details.NewOverview.Configuration.transport.tooltip'
                                                defaultMessage={'HTTP is less secure than HTTPS and ' +
                                                    'makes your API vulnerable to security threats.'}
                                            />
                                        </React.Fragment>
                                    }
                                >
                                    <Button className={parentClasses.helpButton}>
                                        <HelpOutline className={parentClasses.helpIcon} />
                                    </Button>
                                </Tooltip>
                            </Typography>
                            <Typography component='p' variant='body1'>
                                {api.transport && api.transport.length !== 0 && (
                                    <React.Fragment>
                                        {api.transport.map((item, index) => (
                                            <span>
                                                {item}
                                                {api.transport.length !== index + 1 && ', '}
                                            </span>
                                        ))}
                                    </React.Fragment>
                                )}
                                {!api.transport && <React.Fragment>?</React.Fragment>}
                            </Typography>
                            {/* Response Caching */}
                            <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                                <FormattedMessage
                                    id='Apis.Details.NewOverview.Configuration.response.caching'
                                    defaultMessage='Response Caching'
                                />
                                <Tooltip
                                    placement='top'
                                    classes={{
                                        tooltip: parentClasses.htmlTooltip,
                                    }}
                                    disableHoverListener
                                    title={
                                        <React.Fragment>
                                            <FormattedMessage
                                                id='Apis.Details.NewOverview.Configuration.response.caching.tooltip'
                                                defaultMessage={'This option determines whether to cache the ' +
                                                    'response messages of the API. Caching improves performance ' +
                                                    'because the backend server does not have to process the same' +
                                                    ' data multiple times. To offset the risk of stale data in' +
                                                    'the cache, set an appropriate timeout period when prompted.'}
                                            />
                                        </React.Fragment>
                                    }
                                >
                                    <Button className={parentClasses.helpButton}>
                                        <HelpOutline className={parentClasses.helpIcon} />
                                    </Button>
                                </Tooltip>
                            </Typography>
                            <Typography component='p' variant='body1'>
                                {api.responseCaching && <React.Fragment>{api.responseCaching}</React.Fragment>}
                                {!api.responseCaching && <React.Fragment>?</React.Fragment>}
                            </Typography>
                            {/* Authorization Header */}
                            <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                                <FormattedMessage
                                    id='Apis.Details.NewOverview.Configuration.authorization.header'
                                    defaultMessage='Authorization Header'
                                />
                                <Tooltip
                                    placement='top'
                                    classes={{
                                        tooltip: parentClasses.htmlTooltip,
                                    }}
                                    disableHoverListener
                                    title={
                                        <React.Fragment>
                                            <FormattedMessage
                                                id='Apis.Details.NewOverview.Configuration.authorization.header.tooltip'
                                                defaultMessage={'A custom authorization header can be defined ' +
                                                    'as a replacement to the default Authorization header ' +
                                                    'used to send a request. If a value is specified here, ' +
                                                    'it will be used as the header field to send the access token' +
                                                    'in a request to consume the API'}
                                            />
                                        </React.Fragment>
                                    }
                                >
                                    <Button className={parentClasses.helpButton}>
                                        <HelpOutline className={parentClasses.helpIcon} />
                                    </Button>
                                </Tooltip>
                            </Typography>
                            <Typography component='p' variant='body1'>
                                {api.authorizationHeader && <React.Fragment>{api.authorizationHeader}</React.Fragment>}
                                {!api.authorizationHeader && <React.Fragment>?</React.Fragment>}
                            </Typography>
                            {/* Access Control */}
                            <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                                <FormattedMessage
                                    id='Apis.Details.NewOverview.Configuration.access.control'
                                    defaultMessage='Access Control'
                                />
                                <Tooltip
                                    placement='top'
                                    classes={{
                                        tooltip: parentClasses.htmlTooltip,
                                    }}
                                    disableHoverListener
                                    title={
                                        <React.Fragment>
                                            <FormattedMessage
                                                id='Apis.Details.NewOverview.Configuration.access.control.all.tooltip'
                                                defaultMessage={'All : The API is viewable, ' +
                                                    'modifiable by all the publishers and creators.'}
                                            />
                                            <br />
                                            <FormattedMessage
                                                id='Apis.Details.NewOverview.Configuration.access.control.tooltip'
                                                defaultMessage={'Restricted by roles : The API can be viewable and' +
                                                    'modifiable by only specific publishers and creators ' +
                                                    'with the roles that you specify'}
                                            />
                                        </React.Fragment>
                                    }
                                >
                                    <Button className={parentClasses.helpButton}>
                                        <HelpOutline className={parentClasses.helpIcon} />
                                    </Button>
                                </Tooltip>
                            </Typography>
                            <Typography component='p' variant='body1'>
                                {api.accessControl && <React.Fragment>{api.accessControl}</React.Fragment>}
                                {api.accessControl === 'RESTRICTED' && ' ( Visible to '}
                                {api.accessControl === 'RESTRICTED' && api.accessControlRoles.join()}
                                {api.accessControl === 'RESTRICTED' && ' ) '}
                            </Typography>
                            {/* Visibility */}
                            <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                                <FormattedMessage
                                    id='Apis.Details.NewOverview.Configuration.visibility.store'
                                    defaultMessage='Visibility on Store'
                                />
                                <Tooltip
                                    placement='top'
                                    classes={{
                                        tooltip: parentClasses.htmlTooltip,
                                    }}
                                    disableHoverListener
                                    title={
                                        <React.Fragment>
                                            <FormattedMessage
                                                id='Apis.Details.NewOverview.Configuration.visibility.store.all.tooltip'
                                                defaultMessage={'Public: The API is accessible to everyone and can be' +
                                                    'advertised in multiple stores - a central store ' +
                                                    'and/or non-WSO2 stores.'}
                                            />
                                            <br />
                                            <FormattedMessage
                                                id='Apis.Details.NewOverview.Configuration.visibility.store.res.tooltip'
                                                defaultMessage={'Restricted by roles: The API is visible only ' +
                                                    'to specific user roles in the tenant store that you specify.'}
                                            />
                                        </React.Fragment>
                                    }
                                >
                                    <Button className={parentClasses.helpButton}>
                                        <HelpOutline className={parentClasses.helpIcon} />
                                    </Button>
                                </Tooltip>
                            </Typography>
                            <Typography component='p' variant='body1'>
                                {api.visibility && <React.Fragment>{api.visibility}</React.Fragment>}
                                {api.visibility === 'RESTRICTED' && ' ( Visible to '}
                                {api.visibility === 'RESTRICTED' && api.visibleRoles.join()}
                                {api.visibility === 'RESTRICTED' && ' ) '}
                            </Typography>
                        </div>
                    </div>

                    {(api.apiType === API.CONSTS.APIProduct) ?
                        null :
                        <React.Fragment>
                            <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                                <FormattedMessage
                                    id='Apis.Details.NewOverview.Configuration.tags'
                                    defaultMessage='Tags'
                                />
                            </Typography>
                            <Typography variant='body1'>
                                ({api.tags && api.tags.map(tag =>
                                    (<Chip
                                        key={tag}
                                        label={tag}
                                        className={parentClasses.chip}
                                    />))
                                })
                            </Typography>
                        </React.Fragment>
                    }
                </Paper>
            )}
        </ApiContext.Consumer>
    );
}

Configuration.propTypes = {
    parentClasses: PropTypes.shape({}).isRequired,
};

export default Configuration;
