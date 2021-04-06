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
import React, { useContext } from 'react';
import Grid from '@material-ui/core/Grid';
import { FormattedMessage } from 'react-intl';
import PropTypes from 'prop-types';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import Chip from '@material-ui/core/Chip';
import Tooltip from '@material-ui/core/Tooltip';
import HelpOutline from '@material-ui/icons/HelpOutline';
import Box from '@material-ui/core/Box';
import API from 'AppData/api';
import { capitalizeFirstLetter, upperCaseString, lowerCaseString } from 'AppData/stringFormatter';
import APIContext from '../components/ApiContext';
import Policies from './Policies';

const HUMAN_READABLE_SCHEMES = {
    oauth2: 'OAuth2',
    basic_auth: 'Basic Auth',
    mutualssl: 'Mutual TLS',
    api_key: 'API Key',
};
/**
 *
 *
 * @param {*} props
 * @returns
 */
function Configuration(props) {
    const { parentClasses } = props;
    const { api } = useContext(APIContext);

    return (
        <>
            <div>
                <Typography variant='h5' component='h3' className={parentClasses.title}>
                    <FormattedMessage id='Apis.Details.NewOverview.MetaData.config' defaultMessage='Configuration' />
                </Typography>
            </div>
            <Box p={1}>
                <Grid container spacing={2}>
                    <Grid item xs={12} md={6} lg={4}>
                        {/* Transports */}
                        <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                            <FormattedMessage
                                id='Apis.Details.NewOverview.MetaData.transports'
                                defaultMessage='Transports'
                            />
                            <Tooltip
                                interactive
                                placement='top'
                                tabIndex='-1'
                                classes={{
                                    tooltip: parentClasses.htmlTooltip,
                                }}
                                title={(
                                    <>
                                        <FormattedMessage
                                            id='Apis.Details.NewOverview.MetaData.transport.tooltip'
                                            defaultMessage={
                                                'HTTP is less secure than HTTPS and '
                                                + 'makes your API vulnerable to security threats.'
                                            }
                                        />
                                    </>
                                )}
                            >
                                <Button className={parentClasses.helpButton}>
                                    <HelpOutline className={parentClasses.helpIcon} />
                                </Button>
                            </Tooltip>
                        </Typography>
                    </Grid>
                    <Grid item xs={12} md={6} lg={8}>
                        <Typography component='p' variant='body1'>
                            {api.transport && api.transport.length !== 0 && (
                                <>
                                    {api.transport.map((item, index) => (
                                        <span>
                                            {upperCaseString(item)}
                                            {api.transport.length !== index + 1 && ', '}
                                        </span>
                                    ))}
                                </>
                            )}
                            {!api.transport && (
                                <>
                                    <Typography component='p' variant='body1' className={parentClasses.notConfigured}>
                                        <FormattedMessage
                                            id='Apis.Details.NewOverview.MetaData.transports.not.set'
                                            defaultMessage='-'
                                        />
                                    </Typography>
                                </>
                            )}
                        </Typography>
                    </Grid>
                    <Grid item xs={12} md={6} lg={4}>
                        {/* API Security */}
                        <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                            <FormattedMessage
                                id='Apis.Details.NewOverview.MetaData.securityScheme'
                                defaultMessage='API Security'
                            />
                            <Tooltip
                                interactive
                                placement='top'
                                tabIndex='-1'
                                classes={{
                                    tooltip: parentClasses.htmlTooltip,
                                }}
                                title={(
                                    <>
                                        <FormattedMessage
                                            id='Apis.Details.NewOverview.MetaData.securityScheme.tooltip'
                                            defaultMessage='OAuth2 is used as the default security schema.'
                                        />
                                    </>
                                )}
                            >
                                <Button className={parentClasses.helpButton}>
                                    <HelpOutline className={parentClasses.helpIcon} />
                                </Button>
                            </Tooltip>
                        </Typography>
                    </Grid>
                    <Grid item xs={12} md={6} lg={8}>
                        <Typography component='p' variant='body1'>
                            {api.securityScheme && api.securityScheme.length !== 0 && (
                                <>
                                    {api.securityScheme
                                        .filter((item) => !item.includes('mandatory'))
                                        .map((filteredItem) => HUMAN_READABLE_SCHEMES[filteredItem])
                                        .join(' , ')}
                                </>
                            )}
                            {!api.securityScheme && (
                                <>
                                    <Typography component='p' variant='body1' className={parentClasses.notConfigured}>
                                        <FormattedMessage
                                            id='Apis.Details.NewOverview.MetaData.securityScheme.not.set'
                                            defaultMessage='-'
                                        />
                                    </Typography>
                                </>
                            )}
                        </Typography>
                    </Grid>
                    <Grid item xs={12} md={6} lg={4}>
                        {/* Access Control */}
                        <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                            <FormattedMessage
                                id='Apis.Details.NewOverview.MetaData.access.control'
                                defaultMessage='Access Control'
                            />
                            <Tooltip
                                interactive
                                placement='top'
                                tabIndex='-1'
                                classes={{
                                    tooltip: parentClasses.htmlTooltip,
                                }}
                                title={(
                                    <>
                                        <FormattedMessage
                                            id='Apis.Details.NewOverview.MetaData.access.control.all.tooltip'
                                            defaultMessage={
                                                'All : The API is viewable, '
                                                + 'modifiable by all the publishers and creators.'
                                            }
                                        />
                                        <br />
                                        <FormattedMessage
                                            id='Apis.Details.NewOverview.MetaData.access.control.tooltip'
                                            defaultMessage={
                                                'Restricted by roles : The API can be viewable and'
                                                + ' modifiable by only specific publishers and creators '
                                                + 'with the roles that you specify'
                                            }
                                        />
                                    </>
                                )}
                            >
                                <Button className={parentClasses.helpButton}>
                                    <HelpOutline className={parentClasses.helpIcon} />
                                </Button>
                            </Tooltip>
                        </Typography>
                    </Grid>
                    <Grid item xs={12} md={6} lg={8}>
                        <Typography component='p' variant='body1'>
                            {api.accessControl && (
                                <>
                                    {capitalizeFirstLetter(lowerCaseString(api.accessControl))}
                                </>
                            )}
                            {api.accessControl === 'RESTRICTED' && ' ( Visible to '}
                            {api.accessControl === 'RESTRICTED' && api.accessControlRoles.join()}
                            {api.accessControl === 'RESTRICTED' && ' ) '}
                        </Typography>
                    </Grid>
                    <Grid item xs={12} md={6} lg={4}>
                        {/* workflowStatus */}
                        <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                            <FormattedMessage
                                id='Apis.Details.NewOverview.MetaData.workflow.status'
                                defaultMessage='Workflow Status'
                            />
                        </Typography>
                    </Grid>
                    <Grid item xs={12} md={6} lg={8}>
                        <Typography component='p' variant='body1'>
                            {api.workflowStatus && <>{api.workflowStatus}</>}
                            {!api.workflowStatus && (
                                <>
                                    <Typography component='p' variant='body1' className={parentClasses.notConfigured}>
                                        <FormattedMessage
                                            id='Apis.Details.NewOverview.MetaData.workflowStatus.not.set'
                                            defaultMessage='-'
                                        />
                                    </Typography>
                                </>
                            )}
                        </Typography>
                    </Grid>
                    <Grid item xs={12} md={6} lg={4}>
                        {/* Visibility */}
                        <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                            <FormattedMessage
                                id='Apis.Details.NewOverview.MetaData.visibility.store'
                                defaultMessage='Visibility on Developer Portal'
                            />
                            <Tooltip
                                placement='top'
                                interactive
                                tabIndex='-1'
                                classes={{
                                    tooltip: parentClasses.htmlTooltip,
                                }}
                                title={(
                                    <>
                                        <FormattedMessage
                                            id='Apis.Details.NewOverview.MetaData.visibility.store.all.tooltip'
                                            defaultMessage={
                                                'Public: The API is accessible to everyone and can be advertised '
                                                + 'in multiple developer portals - a central developer portal '
                                                + 'and/or non-WSO2 developer portals.'
                                            }
                                        />
                                        <br />
                                        <FormattedMessage
                                            id='Apis.Details.NewOverview.MetaData.visibility.store.res.tooltip'
                                            defaultMessage={
                                                'Restricted by roles: The API is visible only to '
                                                + 'specific user roles in the tenant Developer Portal that you specify.'
                                            }
                                        />
                                    </>
                                )}
                            >
                                <Button className={parentClasses.helpButton}>
                                    <HelpOutline className={parentClasses.helpIcon} />
                                </Button>
                            </Tooltip>
                        </Typography>
                    </Grid>
                    <Grid item xs={12} md={6} lg={8}>
                        <Typography component='p' variant='body1'>
                            {(api.visibility && api.visibility === 'PRIVATE')
                                ? (
                                    <>
                                        Visible to my domain
                                    </>
                                )
                                : (
                                    <>
                                        {capitalizeFirstLetter(lowerCaseString(api.visibility))}
                                    </>
                                )}
                            {api.visibility === 'RESTRICTED' && ' ( Visible to '}
                            {api.visibility === 'RESTRICTED' && api.visibleRoles.join()}
                            {api.visibility === 'RESTRICTED' && ' ) '}
                        </Typography>
                    </Grid>
                    <Policies parentClasses={parentClasses} />
                    {api.apiType === API.CONSTS.APIProduct ? null : (
                        <>
                            <Grid item xs={12} md={6} lg={4}>
                                <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                                    <FormattedMessage
                                        id='Apis.Details.NewOverview.MetaData.tags'
                                        defaultMessage='Tags'
                                    />
                                </Typography>
                            </Grid>
                            <Grid item xs={12} md={6} lg={8}>
                                {api.tags
                                    && api.tags.map((tag) => (
                                        <Chip
                                            key={tag}
                                            label={tag}
                                            style={{
                                                'font-size': 13,
                                                height: 20,
                                                marginRight: 5,
                                            }}
                                        />
                                    ))}
                                {api.tags.length === 0 && (
                                    <>
                                        <Typography
                                            component='p'
                                            variant='body1'
                                            className={parentClasses.notConfigured}
                                        >
                                            <FormattedMessage
                                                id='Apis.Details.NewOverview.MetaData.tags.not.set'
                                                defaultMessage='-'
                                            />
                                        </Typography>
                                    </>
                                )}
                            </Grid>
                        </>
                    )}
                </Grid>
            </Box>
        </>
    );
}

Configuration.propTypes = {
    parentClasses: PropTypes.shape({}).isRequired,
};

export default Configuration;
