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
import Typography from '@material-ui/core/Typography';
import { FormattedMessage } from 'react-intl';
import Box from '@material-ui/core/Box';

/**
 * Generate the Complexity Analysis UI under Query Complexity tab within Query Analysis
 * @returns {*} Complexity Analysis page UI
 */
function ComplexityAnalysis() {
    return (
        <>
            <Box>
                <Typography variant='h6'>
                    <FormattedMessage
                        id='Apis.Details.QueryAnalysis.ComplexityAnalysis.query.complexity'
                        defaultMessage='Query Complexity'
                    />
                </Typography>
            </Box>
        </>
    );
}

export default ComplexityAnalysis;
