/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import MenuButton from 'AppComponents/Shared/MenuButton';
import Landing from 'AppComponents/Apis/Listing/Landing';
import AuthManager from 'AppData/AuthManager';


const APICreateMenu = (props) => {
    const { handleDeploySample, deploying } = props;

    if (deploying !== null && handleDeploySample !== null) {
        return <Landing />;
    } else {
        return !AuthManager.isNotCreator() && <MenuButton {...props} menuList={<Landing />} />;
    }
};
APICreateMenu.defaultProps = {
    handleDeploySample: null,
    deploying: null,
};
APICreateMenu.propTypes = {
    children: PropTypes.oneOfType([PropTypes.element, PropTypes.arrayOf(PropTypes.shape({}))]).isRequired,
    handleDeploySample: PropTypes.func,
    deploying: PropTypes.bool,
};
export default APICreateMenu;
