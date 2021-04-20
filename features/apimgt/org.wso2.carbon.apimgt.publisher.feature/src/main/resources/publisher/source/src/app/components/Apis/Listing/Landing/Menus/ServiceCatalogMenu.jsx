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

// **** DEPRECATED ****
import React from 'react';
import { FormattedMessage } from 'react-intl';
import LandingMenuItem from 'AppComponents/Apis/Listing/Landing/components/LandingMenuItem';
import LandingMenu from 'AppComponents/Apis/Listing/Landing/components/LandingMenu';
import APICreateMenuSection from 'AppComponents/Apis/Listing/components/APICreateMenuSection';

/**
 * NOTE: This is no longer in use and deprecated !!!!
 * @param {Object} props Component interface
 * @returns
 */
const ServiceCatalogMenu = (props) => {
    const { icon, isCreateMenu } = props;
    const Component = isCreateMenu ? APICreateMenuSection : LandingMenu;
    const dense = isCreateMenu;
    return (
        <Component
            id='itest-rest-api-create-menu'
            openList={dense}
            title={(
                <FormattedMessage
                    id='Apis.Listing.SampleAPI.SampleAPI.service.catalog.api'
                    defaultMessage='Service Catalog'
                />
            )}
            icon={icon}
        >
            <LandingMenuItem
                dense={dense}
                id='itest-id-create-from-service-catalog'
                linkTo='/service-catalog'
            >
                <FormattedMessage
                    id='Apis.Listing.import.from.service.catalog.title'
                    defaultMessage='Import From Service Catalog'
                />
            </LandingMenuItem>
        </Component>
    );
};

export default ServiceCatalogMenu;
