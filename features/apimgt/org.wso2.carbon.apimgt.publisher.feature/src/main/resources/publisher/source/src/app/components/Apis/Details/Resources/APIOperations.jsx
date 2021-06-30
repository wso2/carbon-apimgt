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
import Typography from '@material-ui/core/Typography';
import Box from '@material-ui/core/Box';
import { FormattedMessage } from 'react-intl';

import Resources from './Resources';

/**
 *
 *
 * @export
 * @param {*} props
 * @returns
 */
export default function APIOperations() {
    return (
        <>
            <Box mb={4}>
                <Typography id='itest-api-details-resources-head' variant='h4' gutterBottom>
                    <FormattedMessage
                        id='Apis.Details.Resources.APIOperations.title'
                        defaultMessage='Resources'
                    />
                </Typography>
            </Box>
            <Resources />
        </>
    );
}
