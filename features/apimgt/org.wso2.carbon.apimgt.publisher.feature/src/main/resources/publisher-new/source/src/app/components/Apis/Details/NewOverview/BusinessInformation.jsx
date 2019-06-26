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
import PropTypes from 'prop-types';
import classNames from 'classnames';
import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import ApiContext from '../components/ApiContext';

function BusinessInformation(props) {
    const { parentClasses } = props;
    return (
        <ApiContext.Consumer>
            {({ api }) => (
                <Paper className={classNames({ [parentClasses.root]: true, [parentClasses.specialGap]: true })}>
                    <div className={parentClasses.titleWrapper}>
                        <Typography variant='h5' component='h3' className={parentClasses.title}>
                            Business Information
                        </Typography>
                        <Link to={'/apis/' + api.id + '/business info'}>
                            <Button variant='contained' color='default'>
                                Edit
                            </Button>
                        </Link>
                    </div>

                    {/* Business Owner */}
                    <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                        Business Owner
                    </Typography>
                    <Typography component='p' variant='body1'>
                        {api.businessInformation.businessOwner && (
                            <React.Fragment>{api.businessInformation.businessOwner}</React.Fragment>
                        )}
                        {!api.businessInformation.businessOwner && (
                            <React.Fragment>&lt;Not Configured&gt;</React.Fragment>
                        )}
                    </Typography>
                    {/* Business Email */}
                    <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                        Business Owner Email
                    </Typography>
                    <Typography component='p' variant='body1'>
                        {api.businessInformation.businessOwnerEmail && (
                            <React.Fragment>{api.businessInformation.businessOwnerEmail}</React.Fragment>
                        )}
                        {!api.businessInformation.businessOwnerEmail && (
                            <React.Fragment>&lt;Not Configured&gt;</React.Fragment>
                        )}
                    </Typography>
                    {/* Technical Owner */}
                    <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                        Technical Owner
                    </Typography>
                    <Typography component='p' variant='body1'>
                        {api.businessInformation.technicalOwner && (
                            <React.Fragment>{api.businessInformation.technicalOwner}</React.Fragment>
                        )}
                        {!api.businessInformation.technicalOwner && (
                            <React.Fragment>&lt;Not Configured&gt;</React.Fragment>
                        )}
                    </Typography>
                    {/* Technical Owner */}
                    <Typography component='p' variant='subtitle2' className={parentClasses.subtitle}>
                        Technical Owner Email
                    </Typography>
                    <Typography component='p' variant='body1'>
                        {api.businessInformation.technicalOwnerEmail && (
                            <React.Fragment>{api.businessInformation.technicalOwnerEmail}</React.Fragment>
                        )}
                        {!api.businessInformation.technicalOwnerEmail && (
                            <React.Fragment>&lt;Not Configured&gt;</React.Fragment>
                        )}
                    </Typography>
                </Paper>
            )}
        </ApiContext.Consumer>
    );
}

BusinessInformation.propTypes = {
    parentClasses: PropTypes.object.isRequired,
};

export default BusinessInformation;
