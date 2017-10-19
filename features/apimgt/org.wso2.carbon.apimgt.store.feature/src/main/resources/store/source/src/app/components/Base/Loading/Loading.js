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

import React from 'react'
import PropTypes from 'prop-types'
import Grid from 'material-ui/Grid'
import { LinearProgress } from 'material-ui/Progress';

const LoadingAnimation = (props) => {
    return (
        <Grid container spacing={24}>
            <Grid item xs={12}>
                <LinearProgress color="accent"/>
            </Grid>
        </Grid>
    );
};

LoadingAnimation.propTypes = {
    message: PropTypes.string
};
LoadingAnimation.defaultProps = {
    message: "Loading . . ."
};
export default LoadingAnimation