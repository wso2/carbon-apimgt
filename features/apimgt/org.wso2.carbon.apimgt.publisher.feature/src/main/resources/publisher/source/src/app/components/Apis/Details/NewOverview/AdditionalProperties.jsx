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

const renderAdditionalProperties = function (additionalProperties) {
    const items = [];
    for (const key in additionalProperties) {
        if (Object.prototype.hasOwnProperty.call(additionalProperties, key)) {
            const additionalPropertiesTypography = (
                <Typography component='p' variant='body1'>
                    <strong>{key}</strong>
:
                    {additionalProperties[key]}
                </Typography>
            );
            items.push(additionalPropertiesTypography);
        }
    }
    return items;
};

/**
 *
 *
 * @param {*} props
 * @returns
 */
function AdditionalProperties(props) {
    const { parentClasses, api } = props;
    return (
        <Paper className={classNames({ [parentClasses.root]: true, [parentClasses.specialGap]: true })}>
            <div className={parentClasses.titleWrapper}>
                <Typography variant='h5' component='h3' className={parentClasses.title}>
                    <FormattedMessage
                        id='Apis.Details.NewOverview.AdditionalProperties.additional.properties'
                        defaultMessage='Additional Properties'
                    />
                </Typography>
                <Link to={'/apis/' + api.id + '/properties'}>
                    <Button variant='contained' color='default'>
                        <FormattedMessage
                            id='Apis.Details.NewOverview.AdditionalProperties.edit'
                            defaultMessage='Edit'
                        />
                    </Button>
                </Link>
            </div>

            {renderAdditionalProperties(api.additionalProperties)}
        </Paper>
    );
}

AdditionalProperties.propTypes = {
    parentClasses: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({}).isRequired,
};

export default withAPI(AdditionalProperties);
