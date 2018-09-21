import React from 'react';
import { Redirect, Route, Switch } from 'react-router-dom';
import PropTypes from 'prop-types';
import Utils from 'AppData/Utils';

import { PageNotFound } from '../../../Base/Errors/';
import EnvironmentOverview from '../EnvironmentOverview/EnvironmentOverview';
// import PermissionFormWrapper from '../Permissions/Permission';

/**
 *
 * Get the <Route/> element for given array of `apiDetailPages`, the structure of
 * the element in the `apiDetailPages` is defined in propTypes.
 *
 * Routing component for API Details page, Handle all the routes to each page in API details
 * (i:e Overview, Lifecycle, Security) `apiDetailPages` is an array of page info objects (Beans)
 * @param {Object} { api, apiDetailPages }
 * @returns {React.Component} Entire routing switch condition for API details page.
 */
const APIDetailsRoutes = ({ api, apiDetailPages }) => {
    const environmentOverview = Utils.isMultiEnvironmentOverviewEnabled() || null;
    const redirectUrl = `/apis/${api.id}/overview`;
    return (
        <Switch>
            <Redirect exact from='/apis/:api_uuid' to={redirectUrl} />
            {environmentOverview && (
                <Route
                    path='/apis/:api_uuid/environment view'
                    render={routeProps => (
                        <EnvironmentOverview resourceNotFountMessage='' {...routeProps} />
                    )}
                />
            )}
            {apiDetailPages.map(({ pathName, PageComponent }) => (
                <Route
                    key={pathName}
                    path={`/apis/:apiUUID/${pathName}`}
                    render={routeProps => <PageComponent api={api} {...routeProps} />}
                />
            ))}
            <Route component={PageNotFound} />
        </Switch>
    );
};

APIDetailsRoutes.propTypes = {
    api: PropTypes.shape({}).isRequired,
    apiDetailPages: PropTypes.arrayOf(PropTypes.shape({
        pathName: PropTypes.string,
        PageComponent: PropTypes.func,
        name: PropTypes.string,
        NavIcon: PropTypes.element,
    })).isRequired,
};

export default APIDetailsRoutes;
