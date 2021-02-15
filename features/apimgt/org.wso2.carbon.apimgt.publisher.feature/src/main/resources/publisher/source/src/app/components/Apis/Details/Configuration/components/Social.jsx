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
import PropTypes from 'prop-types';
import { FormattedMessage } from 'react-intl';
import { isRestricted } from 'AppData/AuthManager';
import { useAPI } from 'AppComponents/Apis/Details/components/ApiContext';
import TextField from '@material-ui/core/TextField';

/**
 *
 *
 * @export
 * @param {*} props
 * @returns
 */
export default function Social(props) {
    const { api, configDispatcher } = props;
    const [apiFromContext] = useAPI();
    return (
        <>
            <TextField
                label={(
                    <FormattedMessage
                        id='Apis.Details.Configuration.components.Social.giturl'
                        defaultMessage='GitHub URL'
                    />
                )}
                variant='outlined'
                value={api.additionalProperties.github_repo || ''}
                fullWidth
                margin='normal'
                onChange={(e) => configDispatcher({ action: 'github_repo', value: e.target.value })}
                disabled={isRestricted(['apim:api_create', 'apim:api_publish'], apiFromContext)}
                helperText={(
                    <FormattedMessage
                        id='Apis.Details.Configuration.components.Social.giturl.help'
                        defaultMessage={`You need to enable social feature to view this from devportal. 
                        Set custom.social.showSharing.active to true. Refer documentation for more information`}
                    />
                )}
                style={{ marginTop: 0 }}
            />
            <TextField
                label={(
                    <FormattedMessage
                        id='Apis.Details.Configuration.components.Social.slack'
                        defaultMessage='Slack URL'
                    />
                )}
                variant='outlined'
                value={api.additionalProperties.slack_url || ''}
                fullWidth
                margin='normal'
                onChange={(e) => configDispatcher({ action: 'slack_url', value: e.target.value })}
                disabled={isRestricted(['apim:api_create', 'apim:api_publish'], apiFromContext)}
                helperText={(
                    <FormattedMessage
                        id='Apis.Details.Configuration.components.Social.slack_url.help'
                        defaultMessage={`You need to enable social feature to view this from devportal. 
                        Set custom.social.showSharing.active to true. Refer documentation for more information`}
                    />
                )}
                style={{ marginTop: 0 }}
            />
        </>
    );
}

Social.propTypes = {
    api: PropTypes.shape({}).isRequired,
    configDispatcher: PropTypes.func.isRequired,
};
