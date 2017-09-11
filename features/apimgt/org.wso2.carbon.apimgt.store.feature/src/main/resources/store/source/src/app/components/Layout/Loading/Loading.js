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
import {Spin, Row, Col} from 'antd'

const LoadingAnimation = (props) => {
    return (
        <Row type="flex" justify="center" align="middle">
            <Col span={24} style={{textAlign: "center"}}>
                <Spin spinning={true} size="large"/>
            </Col>
        </Row>
    );
};

LoadingAnimation.propTypes = {
    message: PropTypes.string
};
LoadingAnimation.defaultProps = {
    message: "Loading . . ."
};
export default LoadingAnimation