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
import Joyride from 'react-joyride';

/**
 * The base component of APIWalkthrough
 * @param {any} props The props passed to the layout
 * @returns {any} HTML representation.
 */
class APIWalkthrough extends React.Component {
    /**
     * @inheritDoc
     */
    constructor(props) {
        super(props);
        this.state = {
            // TODO: Add steps here
            steps: [
                {
                    target: '.first-step',
                    content: 'Welcome to API Manager 3.0.0 !',
                    disableBeacon: true,
                },
                {
                    target: '.second-step',
                    content: 'Let\'s create an API.',
                    disableBeacon: true,
                    isFixed: true,
                },
            ],
        };
    }

    /**
     * Return APIWalkthrough
     * @returns {React.Component} return react component
     * @memberof APIWalkthrough
     */
    render() {
        const { steps } = this.state;
        return (
            <Joyride
                steps={steps}
                continuous
                showProgress
                showSkipButton
                styles={{
                    options: {
                        // TODO: Add styles here
                    },
                }}
            />
        );
    }
}

export default APIWalkthrough;
