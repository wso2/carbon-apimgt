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
import { FormattedMessage } from 'react-intl';
import PropTypes from 'prop-types';
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';
import { withAPI } from 'AppComponents/Apis/Details/components/ApiContext';


/**
 *
 *
 * @param {*} props
 * @returns
 */
function BusinessInformation(props) {
    const { parentClasses, api } = props;
    return (
        <React.Fragment>
            <Grid item xs={12} md={6} lg={4}>
                {/* Business Owner */}
                <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                    <FormattedMessage
                        id='Apis.Details.NewOverview.BusinessInformation.business.owner'
                        defaultMessage='Business Owner'
                    />
                </Typography>
            </Grid>
            <Grid item xs={12} md={6} lg={8}>
                <Typography component='p' variant='body1'>
                    {api.businessInformation.businessOwner && (
                        <React.Fragment>{api.businessInformation.businessOwner}</React.Fragment>
                    )}
                </Typography>
                <Typography component='p' variant='body1' className={parentClasses.notConfigured}>
                    {!api.businessInformation.businessOwner && (
                        <React.Fragment>&lt;
                            <FormattedMessage
                                id='Apis.Details.NewOverview.BusinessInformation.business.owner.not.configured'
                                defaultMessage='Not Configured'
                            />&gt;
                        </React.Fragment>
                    )}
                </Typography>
            </Grid>
            <Grid item xs={12} md={6} lg={4}>
                {/* Technical Owner */}
                <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                    <FormattedMessage
                        id='Apis.Details.NewOverview.BusinessInformation.technical.owner'
                        defaultMessage='Technical Owner'
                    />
                </Typography>
            </Grid>
            <Grid item xs={12} md={6} lg={8}>
                <Typography component='p' variant='body1'>
                    {api.businessInformation.technicalOwner &&
                        <React.Fragment>{api.businessInformation.technicalOwner}</React.Fragment>}
                </Typography>
                <Typography component='p' variant='body1' className={parentClasses.notConfigured}>
                    {!api.businessInformation.technicalOwner && (
                        <React.Fragment>&lt;
                            <FormattedMessage
                                id='Apis.Details.NewOverview.BusinessInformation.technical.owner.not.configured'
                                defaultMessage='Not Configured'
                            />&gt;
                        </React.Fragment>
                    )}
                </Typography>
            </Grid>
        </React.Fragment>
    );
}

BusinessInformation.propTypes = {
    parentClasses: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({}).isRequired,
};

export default withAPI(BusinessInformation);
