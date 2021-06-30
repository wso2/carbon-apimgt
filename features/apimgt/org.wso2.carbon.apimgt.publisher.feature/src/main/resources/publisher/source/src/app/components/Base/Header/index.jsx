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
import HeaderOriginal from './HeaderOriginal';

/**
 * Acts as an extension point.
 * You can return the Header original component with additional
 * props to override the defualt look and feel.
 * <HeaderOriginal avatar={<NewSettings />}
        user={user}
        menuItems={[<SettingsButton />, <SettingsButton />]}
    />
 * @param {object} props props passed down from the parent.
 * @returns {element} Original header component.
 */
export default function Header(props) {
    const { avatar, user } = props;
    return <HeaderOriginal avatar={avatar} user={user} />;
}

Header.propTypes = {
    avatar: PropTypes.element.isRequired,
    user: PropTypes.shape({ name: PropTypes.string.isRequired }).isRequired,
};
