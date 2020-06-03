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
import Grid from '@material-ui/core/Grid';
import Paper from '@material-ui/core/Paper';
import PropTypes from 'prop-types';
import Progress from './Progress';

import './redirecting.css';

const Redirecting = (props) => {
    const { message } = props;

    return (
        <div className='redirect-flex-container'>
            <Grid container justify='center' alignItems='center' spacing={0} className='redirect-grid-container'>
                <Grid item lg={6} md={8} xs={10}>
                    <Grid container alignItems='center'>
                        <Grid item sm={2} xs={12}>
                            <Progress className='redirect-loadingbar' />
                        </Grid>
                        <Grid item sm={10} xs={12}>
                            <div className='redirect-main-content'>
                                <Paper elevation={5} square className='redirect-paper'>
                                    {message}
                                </Paper>
                            </div>
                        </Grid>
                    </Grid>
                </Grid>
            </Grid>
        </div>
    );
};

Redirecting.propTypes = {
    message: PropTypes.string.isRequired,
};

export default Redirecting;
