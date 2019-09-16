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
import classNames from 'classnames';
import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
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
        <Paper className={classNames({ [parentClasses.root]: true, [parentClasses.specialGap]: true })}>
            <div className={parentClasses.titleWrapper}>
                <Typography variant='h5' component='h3' className={parentClasses.title}>
                    <FormattedMessage
                        id='Apis.Details.NewOverview.BusinessInformation.business.information'
                        defaultMessage='Business Information'
                    />
                </Typography>
                <Link to={'/apis/' + api.id + '/business info'}>
                    <Button variant='contained' color='default'>
                        <FormattedMessage
                            id='Apis.Details.NewOverview.BusinessInformation.edit'
                            defaultMessage='Edit'
                        />
                    </Button>
                </Link>
            </div>

            {/* Business Owner */}
            <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                <FormattedMessage
                    id='Apis.Details.NewOverview.BusinessInformation.business.owner'
                    defaultMessage='Business Owner'
                />
            </Typography>
            <Typography component='p' variant='body1'>
                {api.businessInformation.businessOwner && (
                    <React.Fragment>{api.businessInformation.businessOwner}</React.Fragment>
                )}
                {!api.businessInformation.businessOwner && (
                    <React.Fragment>&lt;
                        <FormattedMessage
                            id='Apis.Details.NewOverview.BusinessInformation.business.owner.not.configured'
                            defaultMessage='Not Configured'
                        />&gt;
                    </React.Fragment>
                )}
            </Typography>
            {/* Business Email */}
            <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                <FormattedMessage
                    id='Apis.Details.NewOverview.BusinessInformation.business.owner.email'
                    defaultMessage='Business Owner Email'
                />
            </Typography>
            <Typography component='p' variant='body1'>
                {api.businessInformation.businessOwnerEmail && (
                    <React.Fragment>{api.businessInformation.businessOwnerEmail}</React.Fragment>
                )}
                {!api.businessInformation.businessOwnerEmail && (
                    <React.Fragment>
                                &lt;
                        <FormattedMessage
                            id='Apis.Details.NewOverview.BusinessInformation.business.email.not.configured'
                            defaultMessage='Not Configured'
                        />
                                &gt;
                    </React.Fragment>
                )}
            </Typography>
            {/* Technical Owner */}
            <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                <FormattedMessage
                    id='Apis.Details.NewOverview.BusinessInformation.technical.owner'
                    defaultMessage='Technical Owner'
                />
            </Typography>
            <Typography component='p' variant='body1'>
                {api.businessInformation.technicalOwner && (
                    <React.Fragment>{api.businessInformation.technicalOwner}</React.Fragment>
                )}
                {!api.businessInformation.technicalOwner && (
                    <React.Fragment>&lt;
                        <FormattedMessage
                            id='Apis.Details.NewOverview.BusinessInformation.technical.owner.not.configured'
                            defaultMessage='Not Configured'
                        />&gt;
                    </React.Fragment>
                )}
            </Typography>
            {/* Technical Owner */}
            <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                <FormattedMessage
                    id='Apis.Details.NewOverview.BusinessInformation.technical.owner.email'
                    defaultMessage='Technical Owner Email'
                />
            </Typography>
            <Typography component='p' variant='body1'>
                {api.businessInformation.technicalOwnerEmail && (
                    <React.Fragment>{api.businessInformation.technicalOwnerEmail}</React.Fragment>
                )}
                {!api.businessInformation.technicalOwnerEmail && (
                    <React.Fragment>
                                &lt;
                        <FormattedMessage
                            id='Apis.Details.NewOverview.BusinessInformation.technical.email.not.configured'
                            defaultMessage='Not Configured'
                        />
                                &gt;
                    </React.Fragment>
                )}
            </Typography>
        </Paper>
    );
}

BusinessInformation.propTypes = {
    parentClasses: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({}).isRequired,
};

export default withAPI(BusinessInformation);
