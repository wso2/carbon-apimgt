/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import Switch from '@material-ui/core/Switch';
import React, { useState, useEffect } from 'react';
import API from 'AppData/api';
import base64url from 'base64url';
import { isRestricted } from 'AppData/AuthManager';
import APIProduct from 'AppData/APIProduct';

/**
 * Renders an Deployrevision list
 * @class Environments
 * @extends {React.Component}
 */
export default function DisplayDevportal(props) {
    const {
        api,
        name,
        EnvDeployments,
    } = props;
    const restApi = new API();
    const restAPIProduct = new APIProduct();
    const [check, setCheck] = useState(EnvDeployments.disPlayDevportal);

    useEffect(() => {
        setCheck(typeof EnvDeployments.disPlayDevportal === 'undefined' ? false : EnvDeployments.disPlayDevportal);
    },
    [EnvDeployments.disPlayDevportal]);

    const handleDisplayOnDevPortal = (event) => {
        if (typeof EnvDeployments.revision === 'undefined') {
            setCheck(event.target.checked);
        } else {
            const body = {
                revisionUuid: EnvDeployments.revision.id,
                displayOnDevportal: event.target.checked,
            };
            setCheck(event.target.checked);
            if (api.apiType === API.CONSTS.APIProduct) {
                restAPIProduct.displayInDevportalProduct(api.id, base64url.encode(event.target.name), body);
            } else {
                restApi.displayInDevportalAPI(api.id, base64url.encode(event.target.name), body);
            }
        }
    };

    return (
        <Switch
            checked={check}
            onChange={handleDisplayOnDevPortal}
            disabled={api.isRevision || isRestricted(['apim:api_create', 'apim:api_publish'], api)}
            name={name}
        />
    );
}
