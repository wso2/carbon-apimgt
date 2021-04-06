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
import Grid from '@material-ui/core/Grid';
import { FormattedMessage } from 'react-intl';
import PropTypes from 'prop-types';
import moment from 'moment';
import ReactMarkdown from 'react-markdown';
import Typography from '@material-ui/core/Typography';
import Box from '@material-ui/core/Box';
import Tooltip from '@material-ui/core/Tooltip';
import API from 'AppData/api';
import { capitalizeFirstLetter } from 'AppData/stringFormatter';
import { useAPI } from 'AppComponents/Apis/Details/components/ApiContext';
import BusinessInformation from './BusinessInformation';

/**
 *
 *
 * @param {*} props
 * @returns
 */
function MetaData(props) {
    const { parentClasses } = props;
    const [api] = useAPI();

    return (
        <>
            <Typography variant='h5' component='h3' className={parentClasses.title}>
                <FormattedMessage
                    id='Apis.Details.NewOverview.MetaData.metadata'
                    defaultMessage='Metadata'
                />
            </Typography>
            <Box p={1}>
                <Grid container spacing={1}>
                    <Grid item xs={12} md={6} lg={4}>
                        <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                            <FormattedMessage
                                id='Apis.Details.NewOverview.MetaData.description'
                                defaultMessage='Description'
                            />
                        </Typography>
                    </Grid>
                    <Grid item xs={12} md={6} lg={8}>
                        <Typography component='p' variant='body1'>
                            {api.description
                            && (
                                <ReactMarkdown source={capitalizeFirstLetter(api.description)} />
                            )}
                        </Typography>
                        <Typography component='p' variant='body1' className={parentClasses.notConfigured}>
                            {!api.description
                                && (
                                    <>
                                        <Typography
                                            component='p'
                                            variant='body1'
                                            className={parentClasses.notConfigured}
                                        >
                                            <FormattedMessage
                                                id='Apis.Details.NewOverview.MetaData.description.not.set'
                                                defaultMessage='-'
                                            />
                                        </Typography>
                                    </>
                                )}
                        </Typography>
                    </Grid>
                    <Grid item xs={12} md={6} lg={4}>
                        {/* Provider */}
                        <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                            <FormattedMessage
                                id='Apis.Details.NewOverview.MetaData.provider'
                                defaultMessage='Provider'
                            />
                        </Typography>
                    </Grid>
                    <Grid item xs={12} md={6} lg={8}>
                        <Typography component='p' variant='body1'>
                            {api.provider && <>{api.provider}</>}
                        </Typography>
                    </Grid>
                    <Grid item xs={12} md={6} lg={4}>
                        <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                            <FormattedMessage
                                id='Apis.Details.NewOverview.MetaData.context:'
                                defaultMessage='Context:'
                            />
                        </Typography>
                    </Grid>
                    <Grid item xs={12} md={6} lg={8}>
                        <Typography component='p' variant='body1'>
                            {api.context && <>{api.context}</>}
                        </Typography>
                    </Grid>
                    {/* Version */}
                    {api.apiType === API.CONSTS.API && (
                        <>
                            <Grid item xs={12} md={6} lg={4}>
                                <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                                    <FormattedMessage
                                        id='Apis.Details.NewOverview.MetaData.version'
                                        defaultMessage='Version'
                                    />
                                </Typography>
                            </Grid>
                            <Grid item xs={12} md={6} lg={8}>
                                <Typography component='p' variant='body1'>
                                    {api.version && <>{api.version}</>}
                                </Typography>
                            </Grid>
                        </>
                    )}
                    {/* Type */}
                    {api.apiType === API.CONSTS.APIProduct ? null : (
                        <>
                            <Grid item xs={12} md={6} lg={4}>
                                <>
                                    <Typography
                                        component='p'
                                        variant='subtitle2'
                                        className={parentClasses.subtitle}
                                    >
                                        <FormattedMessage
                                            id='Apis.Details.NewOverview.MetaData.type:'
                                            defaultMessage='Type:'
                                        />
                                    </Typography>
                                </>
                            </Grid>
                            <Grid item xs={12} md={6} lg={8}>
                                <Typography component='p' variant='body1'>
                                    {api.type && <>{api.type === 'WS' ? 'WebSocket' : api.type}</>}
                                    {!api.type
                                        && (
                                            <>
                                                <Typography
                                                    component='p'
                                                    variant='body1'
                                                    className={parentClasses.notConfigured}
                                                >
                                                    <FormattedMessage
                                                        id='Apis.Details.NewOverview.MetaData.type.not.set'
                                                        defaultMessage='-'
                                                    />
                                                </Typography>
                                            </>
                                        )}
                                </Typography>
                            </Grid>
                        </>
                    )}
                    <Grid item xs={12} md={6} lg={4}>
                        {/* Created Time */}
                        <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                            <FormattedMessage
                                id='Apis.Details.NewOverview.MetaData.created.time'
                                defaultMessage='Created Time'
                            />
                        </Typography>
                    </Grid>
                    <Grid item xs={12} md={6} lg={8}>
                        <Typography component='p' variant='body1'>
                            {api.createdTime
                            && (
                                <>
                                    <Tooltip
                                        title={moment(api.createdTime).calendar()}
                                        aria-label='add'
                                        interactive
                                        placement='top-start'
                                        tabIndex='-1'
                                    >
                                        <Typography variant='body1' display='block'>
                                            {capitalizeFirstLetter(moment(api.createdTime).fromNow())}
                                        </Typography>
                                    </Tooltip>
                                </>
                            )}
                            {!api.createdTime
                                && (
                                    <>
                                        <Typography
                                            component='p'
                                            variant='body1'
                                            className={parentClasses.notConfigured}
                                        >
                                            <FormattedMessage
                                                id='Apis.Details.NewOverview.MetaData.createdTime.not.set'
                                                defaultMessage='-'
                                            />
                                        </Typography>
                                    </>
                                )}
                        </Typography>
                    </Grid>
                    <Grid item xs={12} md={6} lg={4}>
                        {/* Last Updated Time */}
                        <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                            <FormattedMessage
                                id='Apis.Details.NewOverview.MetaData.last.updated.time'
                                defaultMessage='Last Updated Time'
                            />
                        </Typography>
                    </Grid>
                    <Grid item xs={12} md={6} lg={8}>
                        <Typography component='p' variant='body1'>
                            {api.lastUpdatedTime
                            && (
                                <>
                                    <Tooltip
                                        title={moment(api.lastUpdatedTime).calendar()}
                                        aria-label='add'
                                        interactive
                                        placement='top-start'
                                        tabIndex='-1'
                                    >
                                        <Typography variant='body1' display='block'>
                                            {capitalizeFirstLetter(moment(api.lastUpdatedTime).fromNow())}
                                        </Typography>
                                    </Tooltip>
                                </>
                            )}
                            {!api.lastUpdatedTime
                                && (
                                    <>
                                        <Typography
                                            component='p'
                                            variant='body1'
                                            className={parentClasses.notConfigured}
                                        >
                                            <FormattedMessage
                                                id='Apis.Details.NewOverview.MetaData.lastUpdatedTime.not.set'
                                                defaultMessage='-'
                                            />
                                        </Typography>
                                    </>
                                )}
                        </Typography>
                    </Grid>
                    <BusinessInformation parentClasses={parentClasses} />
                </Grid>
            </Box>
        </>
    );
}

MetaData.propTypes = {
    parentClasses: PropTypes.shape({}).isRequired,
};

export default MetaData;
