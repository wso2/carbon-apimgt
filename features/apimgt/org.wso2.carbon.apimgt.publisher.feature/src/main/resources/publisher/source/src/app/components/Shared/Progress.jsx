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
import defaultTheme from 'AppData/defaultTheme';

const Progress = (props) => {
    const { message, per } = props;
    const backgroundColor = per <= 10 ? 'white' : defaultTheme.custom.wrapperBackground;
    if (per === -1) { // Means default value means no progress percentage
        return (
            <div className='apim-dual-ring'>
                <span style={{ width: '200px', display: 'block', marginLeft: '-33px' }}>{message}</span>
            </div>
        );
    } else {
        return (
            <div id='apim-loader' className='progress-bar-striped'>
                <span style={{
                    backgroundColor, // to override this u need to build the app
                    width: '300px',
                    display: 'flex',
                    justifyContent: 'center',
                    fontFamily: '"Open Sans", "Helvetica", "Arial", sans-serif',
                    'padding-bottom': '14px',
                }}
                >
                    {message}
                </span>
                <div style={{ width: `${per}%` }}> &nbsp; </div>
            </div>
        );
    }
};

Progress.defaultProps = {
    message: '',
    per: -1,
};

Progress.propTypes = {
    message: PropTypes.string,
    per: PropTypes.number,
};
export default Progress;
