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
/* TODO: Move this file to components/Shared/ location ~tmkb */
import React from 'react';
import PropTypes from 'prop-types';

const ApiPermissionValidation = (props) => {
    const { checkingPermissionType, userPermissions } = props;
    if (userPermissions && userPermissions.includes(checkingPermissionType)) {
        return <>{props.children}</>;
    }
    return null;
};

ApiPermissionValidation.propTypes = {
    checkingPermissionType: PropTypes.string,
    userPermissions: PropTypes.arrayOf(PropTypes.string).isRequired,
    children: PropTypes.node.isRequired,
};

ApiPermissionValidation.permissionType = {
    READ: 'READ',
    UPDATE: 'UPDATE',
    DELETE: 'DELETE',
    MANAGE_SUBSCRIPTION: 'MANAGE_SUBSCRIPTION',
};

ApiPermissionValidation.defaultProps = {
    checkingPermissionType: ApiPermissionValidation.permissionType.UPDATE,
};

export default ApiPermissionValidation;
