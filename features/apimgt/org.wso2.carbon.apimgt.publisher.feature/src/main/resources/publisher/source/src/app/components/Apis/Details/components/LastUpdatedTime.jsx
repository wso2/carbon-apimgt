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
import React from 'react';
import PropTypes from 'prop-types';
import moment from 'moment';
import { Typography, Tooltip } from '@material-ui/core';
import { FormattedMessage } from 'react-intl';

/**
 * API/Product last updated time.
 * @param {Parameter} props last updated time.
 * @returns {Property} The sum of the two numbers.
 */
function LastUpdatedTime(props) {
    const { lastUpdatedTime } = props;
    return (
        <div style={{ float: 'right' }}>
            <Tooltip
                title={moment(lastUpdatedTime).calendar()}
                aria-label='add'
            >
                <Typography variant='caption' display='block'>
                    <FormattedMessage
                        id='Apis.Details.components.APIDetailsTopMenu.last.updated.time'
                        defaultMessage='Last updated:'
                    />
                    {' '}
                    {moment(lastUpdatedTime).fromNow()}
                </Typography>
            </Tooltip>
        </div>
    );
}

LastUpdatedTime.propTypes = {
    lastUpdatedTime: PropTypes.shape({ content: PropTypes.string }).isRequired,
};

export default (LastUpdatedTime);
