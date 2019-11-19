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

class ApiProgress extends React.Component {
    constructor(props) {
        super(props);
        this.handleResourceIconDisplay = this.handleResourceIconDisplay.bind(this);
        this.handleTiersIconDisplay = this.handleTiersIconDisplay.bind(this);
    }

    handleResourceIconDisplay = () => {
        return this.props.resources === 'true' ? 'fw fw-resource' : 'fw fw-resource icon-white';
    };

    handleTiersIconDisplay = () => {
        return this.props.tiers === 'true' ? 'fw fw-throttling-policy' : 'fw fw-throttling-policy icon-white';
    };

    render() {
        return (
            <div>
                <a href=''>
                    <i className={this.handleResourceIconDisplay()} />
                </a>
                <a href=''>
                    <i className={this.handleTiersIconDisplay()} />
                </a>
            </div>
        );
    }
}

ApiProgress.propTypes = {
    resources: PropTypes.bool.isRequired,
    tiers: PropTypes.bool.isRequired,
};

export default ApiProgress;
