import React from 'react';
import { useIntl } from 'react-intl';
import Typography from '@material-ui/core/Typography';
import RouteMenuMapping from 'AppComponents/Base/RouteMenuMapping';
import { withRouter } from 'react-router-dom';
import { Breadcrumbs as MUIBreadcrumbs } from '@material-ui/core';

/**
 * Look for the relevant page IDs for given path
 * @param {JSON} routeMenuMapping Route Menu Map object
 * @param {string} currentPath page path to find IDs
 * @param {list} pageIDs Internal parameter to yield pageIDs
 * @returns {list} page IDs for given path
 */
function getPageIDs(routeMenuMapping, currentPath, pageIDs = []) {
    const routeEntries = routeMenuMapping.entries();
    for (const routeEntry of routeEntries) {
        for (const routeDetail of routeEntry) {
            if (typeof routeDetail.children !== 'undefined') {
                const result = getPageIDs(routeDetail.children, currentPath, pageIDs);
                if (result !== '') {
                    result.unshift(routeDetail.id);
                    return result;
                }
            }
            if (routeDetail.path && currentPath.match(routeDetail.path)) {
                return [routeDetail.id];
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
    const pageIDs = getPageIDs(routeMenuMapping, currentPath);
    if (pageIDs) {
        return (
            <MUIBreadcrumbs aria-label='breadcrumb'>
                {pageIDs.map((page) => {
                    return <Typography color='textPrimary'>{page}</Typography>;
                })}
            </MUIBreadcrumbs>
        );
    } else {
        return <div />;
    }
}

export default withRouter(Breadcrumbs);
