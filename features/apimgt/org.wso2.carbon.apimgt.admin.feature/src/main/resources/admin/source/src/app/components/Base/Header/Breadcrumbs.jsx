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
import { useIntl } from 'react-intl';
import Typography from '@material-ui/core/Typography';
import RouteMenuMapping from 'AppComponents/Base/RouteMenuMapping';
import { withRouter, Link as RouterLink } from 'react-router-dom';
import { Breadcrumbs as MUIBreadcrumbs } from '@material-ui/core';
import Link from '@material-ui/core/Link';

/**
 * Look for the relevant page ID's naviagation details
 * @param {JSON} routeMenuMapping Route Menu Map object
 * @param {string} currentPath page path to find IDs
 * @param {list} pageDetails Internal parameter to yield pageIDs and respective intermediate paths
 * @returns {list} page IDs with intermediate paths for a given path
 */
function getPageDetails(routeMenuMapping, currentPath, pageDetails = []) {
    const routeEntries = routeMenuMapping.entries();
    for (const routeEntry of routeEntries) {
        for (const routeDetail of routeEntry) {
            if (typeof routeDetail.children !== 'undefined') {
                const result = getPageDetails(routeDetail.children, currentPath, pageDetails);
                if (result !== '') {
                    result.unshift({ id: routeDetail.id, path: routeDetail.path });
                    return result;
                }
            } else if (typeof routeDetail.addEditPageDetails !== 'undefined') {
                const result = getPageDetails(routeDetail.addEditPageDetails, currentPath, pageDetails);
                if (result !== '') {
                    result.unshift({ id: routeDetail.id, path: routeDetail.path });
                    return result;
                }
            }

            if (routeDetail.path && currentPath.match(routeDetail.path)) {
                return [{ id: routeDetail.id, path: currentPath }];
            }
        }
    }
    return '';
}

/**
 * Render breadcrumb component
 * @param {JSON} props .
 * @returns {JSX} breadcrumbs.
 */
function Breadcrumbs(props) {
    const intl = useIntl();
    const routeMenuMapping = RouteMenuMapping(intl);
    const { history: { location: { pathname: currentPath } } } = props;
    const pageDetails = getPageDetails(routeMenuMapping, currentPath);
    if (pageDetails) {
        const breadcrumbElements = [];
        pageDetails.forEach((page) => {
            if (page.path) {
                breadcrumbElements.push(
                    <Link component={RouterLink} color='inherit' to={page.path}>
                        {page.id}
                    </Link>,
                );
            } else {
                breadcrumbElements.push(
                    <Typography color='textPrimary'>{page.id}</Typography>,
                );
            }
        });
        return (
            <MUIBreadcrumbs aria-label='breadcrumb'>
                {breadcrumbElements}
            </MUIBreadcrumbs>
        );
    } else {
        return <div />;
    }
}

export default withRouter(Breadcrumbs);
