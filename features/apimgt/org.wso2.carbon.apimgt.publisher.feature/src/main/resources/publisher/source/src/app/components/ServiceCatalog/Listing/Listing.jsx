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

import React, { useEffect, useState } from 'react';
import { useIntl } from 'react-intl';
import { Progress } from 'AppComponents/Shared';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import Alert from 'AppComponents/Shared/Alert';
import ServiceCatalog from 'AppData/ServiceCatalog';
import Onboarding from 'AppComponents/ServiceCatalog/Listing/Onboarding';
import ServicesTableView from 'AppComponents/ServiceCatalog/Listing/components/ServicesTableView';
import ServiceCatalogTopMenu from 'AppComponents/ServiceCatalog/Listing/components/ServiceCatalogTopMenu';

import ServicesCardView from 'AppComponents/ServiceCatalog/Listing/components/ServicesCardView';
import Grid from '@material-ui/core/Grid';
import Box from '@material-ui/core/Box';


/**
 * Listing for service catalog entries
 *
 * @function Listing
 * @returns {any} Listing Page for Services
 */
function Listing() {
    const [servicesData, setServicesData] = useState(null);
    const [notFound, setNotFound] = useState(false);
    const [loading, setLoading] = useState(true);
    const [isGridView, setIsGridView] = useState(true);
    const intl = useIntl();

    // Get Services
    const getData = (limit = 10, offset = 0) => {
        const promisedServices = ServiceCatalog.searchServices(limit, offset);
        promisedServices.then((data) => {
            const { body } = data;
            setServicesData(body);
        }).catch((error) => {
            console.error(error);
            if (error.response) {
                Alert.error(error.response.body.description);
            } else {
                Alert.error(intl.formatMessage({
                    defaultMessage: 'Error while loading services',
                    id: 'ServiceCatalog.Listing.Listing.error.loading',
                }));
            }
            const { status } = error;
            if (status === 404) {
                setNotFound(true);
            }
        }).finally(() => {
            setLoading(false);
        });
    };

    useEffect(getData, []);

    const onDelete = (serviceId) => {
        const deleteServicePromise = ServiceCatalog.deleteService(serviceId);
        deleteServicePromise.then(() => {
            Alert.info(intl.formatMessage({
                id: 'ServiceCatalog.Listing.Listing.service.deleted.successfully',
                defaultMessage: 'Service deleted successfully!',
            }));
            // Reload the services list
            getData();
        }).catch((errorResponse) => {
            console.error(errorResponse);
            if (errorResponse.response.body.description !== null) {
                Alert.error(errorResponse.response.body.description);
            } else {
                Alert.error(intl.formatMessage({
                    defaultMessage: 'Error while deleting service',
                    id: 'ServiceCatalog.Listing.Listing.error.delete',
                }));
            }
        });
    };

    if (loading || !servicesData) {
        return <Progress per={90} message='Loading Services ...' />;
    }
    if (notFound) {
        return <ResourceNotFound />;
    }
    const haveServices = servicesData.list.length !== 0;
    return (
        <Box flexGrow={1}>
            <Grid
                container
                direction='column'
                justify='flex-start'
                alignItems='stretch'
            >
                <Grid xs={12}>
                    <ServiceCatalogTopMenu
                        showServiceToggle={haveServices}
                        isGridView={isGridView}
                        totalServices={servicesData.pagination.total}
                        setIsGridView={setIsGridView}
                    />
                </Grid>
                <Box px={4} pt={4}>
                    <Grid xs={12}>
                        {!haveServices && <Onboarding />}
                        {haveServices && (isGridView
                            ? (
                                <ServicesCardView
                                    serviceList={servicesData.list}
                                    pagination={servicesData.pagination}
                                    onDelete={onDelete}
                                    getData={getData}
                                />
                            )
                            : <ServicesTableView serviceList={servicesData.list} onDelete={onDelete} />)}
                    </Grid>
                </Box>
            </Grid>
        </Box>
    );
}

export default Listing;
