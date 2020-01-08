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
import TextField from '@material-ui/core/TextField';
import { isRestricted } from 'AppData/AuthManager';
import { useAPI } from 'AppComponents/Apis/Details/components/ApiContext';

/**
 *
 *
 * @export
 * @param {*} props
 * @returns
 */
export default function Description(props) {
    const { api, configDispatcher } = props;
    const [apiFromContext] = useAPI();

    return (
        <TextField
            id='outlined-multiline-static'
            label='Description'
            multiline
            rows='4'
            value={api.description}
            margin='0'
            fullWidth
            variant='outlined'
            onChange={(e) => configDispatcher({ action: 'description', value: e.target.value })}
            disabled={isRestricted(['apim:api_create'], apiFromContext)}
        />
    );
}

Description.propTypes = {
    api: PropTypes.shape({}).isRequired,
    configDispatcher: PropTypes.func.isRequired,
};
