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
import { capitalizeFirstLetter, upperCaseString, lowerCaseString } from 'AppData/StringFormatter';
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
        <React.Fragment>
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
                                placement='top'
                                tabIndex='-1'
                                classes={{
                                    tooltip: parentClasses.htmlTooltip,
                                }}
                                title={
                                    <React.Fragment>
                                        <FormattedMessage
                                            id='Apis.Details.NewOverview.MetaData.transport.tooltip'
                                            defaultMessage={
                                                'HTTP is less secure than HTTPS and ' +
                                                'makes your API vulnerable to security threats.'
                                            }
                                        />
                                    </React.Fragment>
                                }
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
                                <React.Fragment>
                                    {api.transport.map((item, index) => (
                                        <span>
                                            {upperCaseString(item)}
                                            {api.transport.length !== index + 1 && ', '}
                                        </span>
                                    ))}
                                </React.Fragment>
                            )}
                            {!api.transport && (
                                <React.Fragment>
                                    <Typography component='p' variant='body1' className={parentClasses.notConfigured}>
                                        <FormattedMessage
                                            id='Apis.Details.NewOverview.MetaData.transports.not.set'
                                            defaultMessage='-'
                                        />
                                    </Typography>
                                </React.Fragment>
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
                                placement='top'
                                tabIndex='-1'
                                classes={{
                                    tooltip: parentClasses.htmlTooltip,
                                }}
                                title={
                                    <React.Fragment>
                                        <FormattedMessage
                                            id='Apis.Details.NewOverview.MetaData.securityScheme.tooltip'
                                            defaultMessage='OAuth2 is used as the default security schema.'
                                        />
                                    </React.Fragment>
                                }
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
                                <React.Fragment>
                                    {api.securityScheme
                                        .filter(item => !item.includes('mandatory'))
                                        .map(filteredItem => HUMAN_READABLE_SCHEMES[filteredItem])
                                        .join(' , ')}
                                </React.Fragment>
                            )}
                            {!api.securityScheme && (
                                <React.Fragment>
                                    <Typography component='p' variant='body1' className={parentClasses.notConfigured}>
                                        <FormattedMessage
                                            id='Apis.Details.NewOverview.MetaData.securityScheme.not.set'
                                            defaultMessage='-'
                                        />
                                    </Typography>
                                </React.Fragment>
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
                                placement='top'
                                tabIndex='-1'
                                classes={{
                                    tooltip: parentClasses.htmlTooltip,
                                }}
                                title={
                                    <React.Fragment>
                                        <FormattedMessage
                                            id='Apis.Details.NewOverview.MetaData.access.control.all.tooltip'
                                            defaultMessage={
                                                'All : The API is viewable, ' +
                                                'modifiable by all the publishers and creators.'
                                            }
                                        />
                                        <br />
                                        <FormattedMessage
                                            id='Apis.Details.NewOverview.MetaData.access.control.tooltip'
                                            defaultMessage={
                                                'Restricted by roles : The API can be viewable and' +
                                                ' modifiable by only specific publishers and creators ' +
                                                'with the roles that you specify'
                                            }
                                        />
                                    </React.Fragment>
                                }
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
                                <React.Fragment>
                                    {capitalizeFirstLetter(lowerCaseString(api.accessControl))}
                                </React.Fragment>
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
                            {api.workflowStatus && <React.Fragment>{api.workflowStatus}</React.Fragment>}
                            {!api.workflowStatus && (
                                <React.Fragment>
                                    <Typography component='p' variant='body1' className={parentClasses.notConfigured}>
                                        <FormattedMessage
                                            id='Apis.Details.NewOverview.MetaData.workflowStatus.not.set'
                                            defaultMessage='-'
                                        />
                                    </Typography>
                                </React.Fragment>
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
                                tabIndex='-1'
                                classes={{
                                    tooltip: parentClasses.htmlTooltip,
                                }}
                                title={
                                    <React.Fragment>
                                        <FormattedMessage
                                            id='Apis.Details.NewOverview.MetaData.visibility.store.all.tooltip'
                                            defaultMessage={
                                                'Public: The API is accessible to everyone and can be advertised ' +
                                                'in multiple developer portals - a central developer portal ' +
                                                'and/or non-WSO2 developer portals.'
                                            }
                                        />
                                        <br />
                                        <FormattedMessage
                                            id='Apis.Details.NewOverview.MetaData.visibility.store.res.tooltip'
                                            defaultMessage={
                                                'Restricted by roles: The API is visible only to ' +
                                                'specific user roles in the tenant developer portal that you specify.'
                                            }
                                        />
                                    </React.Fragment>
                                }
                            >
                                <Button className={parentClasses.helpButton}>
                                    <HelpOutline className={parentClasses.helpIcon} />
                                </Button>
                            </Tooltip>
                        </Typography>
                    </Grid>
                    <Grid item xs={12} md={6} lg={8}>
                        <Typography component='p' variant='body1'>
                            {(api.visibility && api.visibility === 'PRIVATE') ?
                                <React.Fragment>
                                    {'Visible to my domain'}
                                </React.Fragment>
                                :
                                <React.Fragment>
                                    {capitalizeFirstLetter(lowerCaseString(api.visibility))}
                                </React.Fragment>
                            }
                            {api.visibility === 'RESTRICTED' && ' ( Visible to '}
                            {api.visibility === 'RESTRICTED' && api.visibleRoles.join()}
                            {api.visibility === 'RESTRICTED' && ' ) '}
                        </Typography>
                    </Grid>
                    <Policies parentClasses={parentClasses} />
                    {api.apiType === API.CONSTS.APIProduct ? null : (
                        <React.Fragment>
                            <Grid item xs={12} md={6} lg={4}>
                                <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                                    <FormattedMessage
                                        id='Apis.Details.NewOverview.MetaData.tags'
                                        defaultMessage='Tags'
                                    />
                                </Typography>
                            </Grid>
                            <Grid item xs={12} md={6} lg={8}>
                                {api.tags &&
                                    api.tags.map(tag => (
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
                                    <React.Fragment>
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
                                    </React.Fragment>
                                )}
                            </Grid>
                        </React.Fragment>
                    )}
                </Grid>
            </Box>
        </React.Fragment>
    );
}

Configuration.propTypes = {
    parentClasses: PropTypes.shape({}).isRequired,
};

export default Configuration;
