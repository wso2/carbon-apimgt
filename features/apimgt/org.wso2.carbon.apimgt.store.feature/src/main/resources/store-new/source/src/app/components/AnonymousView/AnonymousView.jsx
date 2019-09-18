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
import { injectIntl, } from 'react-intl';
import Utils from '../../data/Utils';
import AuthManager from '../../data/AuthManager';
import ConfigManager from '../../data/ConfigManager';
/**
 *
 *
 * @class AnonymousView
 * @extends {React.Component}
 */
class AnonymousView extends React.Component {
    constructor(props) {
        super(props);
        this.authManager = new AuthManager();
        this.state = {
            environments: [],
            environmentId: 0,
        };
    }

    /**
     *
     *
     * @memberof AnonymousView
     */
    componentDidMount() {
        const { intl } = this.props;
        ConfigManager.getConfigs()
            .environments.then((response) => {
                const environments = response.data.environments;
                let environmentId = Utils.getEnvironmentID(environments);
                if (environmentId === -1) {
                    environmentId = 0;
                }
                this.setState({ environments, environmentId });
                const environment = environments[environmentId];
                Utils.setEnvironment(environment);
            })
            .catch(() => {
                console.error(intl.formatMessage({
                    defaultMessage: 'Error while receiving environment configurations', id:'AnonymousView.AnonymousView.error'}));
            });
    }

    /**
     *
     *
     * @returns
     * @memberof AnonymousView
     */
    render() {
        return <div />;
    }
}

export default injectIntl(AnonymousView);
