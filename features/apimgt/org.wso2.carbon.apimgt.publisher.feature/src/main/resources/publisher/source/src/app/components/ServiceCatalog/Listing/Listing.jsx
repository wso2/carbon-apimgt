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
import { Link } from 'react-router-dom';
import { makeStyles } from '@material-ui/core/styles';
import Box from '@material-ui/core/Box';
import Typography from '@material-ui/core/Typography';
import MUIDataTable from 'mui-datatables';
import moment from 'moment';
import { FormattedMessage, useIntl } from 'react-intl';
import { Progress } from 'AppComponents/Shared';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import Alert from 'AppComponents/Shared/Alert';
import ServiceCatalog from 'AppData/ServiceCatalog';
import Onboarding from 'AppComponents/ServiceCatalog/Listing/Onboarding';
import Delete from 'AppComponents/ServiceCatalog/Listing/Delete';
import Usages from 'AppComponents/ServiceCatalog/Listing/Usages';
import CreateApi from 'AppComponents/ServiceCatalog/CreateApi';
import Grid from '@material-ui/core/Grid';
import Help from '@material-ui/icons/Help';
import Tooltip from '@material-ui/core/Tooltip';

const useStyles = makeStyles((theme) => ({
    contentInside: {
        padding: theme.spacing(3),
        paddingTop: theme.spacing(2),
        paddingLeft: theme.spacing(3),
        paddingRight: theme.spacing(3),
        '& > div[class^="MuiPaper-root-"]': {
            boxShadow: 'none',
            backgroundColor: 'transparent',
        },
    },
    serviceNameLink: {
        display: 'flex',
        alignItems: 'center',
        '& span': {
            marginLeft: theme.spacing(),
        },
        '& span.material-icons': {
            marginLeft: 0,
            color: '#444',
            marginRight: theme.spacing(),
            fontSize: 18,
        },
    },
    buttonStyle: {
        marginTop: theme.spacing(1),
        marginBottom: theme.spacing(1),
        marginRight: theme.spacing(2),
    },
    content: {
        display: 'flex',
        flex: 1,
        flexDirection: 'column',
        paddingBottom: theme.spacing(3),
    },
    helpDiv: {
        marginTop: theme.spacing(0.5),
    },
    helpIcon: {
        fontSize: 20,
    },
    horizontalDivider: {
        marginTop: theme.spacing(3),
        borderTop: '0px',
        width: '100%',
    },
    tableStyle: {
        marginTop: theme.spacing(4),
        marginLeft: 'auto',
        marginRight: 'auto',
        '& > td[class^=MUIDataTableBodyCell-cellHide-]': {
            display: 'none',
        },
        '& .MUIDataTableBodyCell-cellHide-793': {
            display: 'none',
        },
        '& td': {
            wordBreak: 'break-word',
        },
        '& th': {
            minWidth: '150px',
        },
    },
}));

/**
 * Listing for service catalog entries
 *
 * @function Listing
 * @returns {any} Listing Page for Services
 */
function Listing(props) {
    const [serviceList, setServiceList] = useState([]);
    const [notFound, setNotFound] = useState(true);
    const [loading, setLoading] = useState(true);
    const intl = useIntl();
    const classes = useStyles();
    const { history } = props;

    // Get Services
    const getData = () => {
        const promisedServices = ServiceCatalog.searchServices();
        promisedServices.then((data) => {
            const { body } = data;
            const { list } = body;
            setServiceList(list);
            setNotFound(false);
        }).catch((error) => {
            console.error(error);
            Alert.error(intl.formatMessage({
                defaultMessage: 'Error while loading services',
                id: 'ServiceCatalog.Listing.Listing.error.loading',
            }));
        }).finally(() => {
            setLoading(false);
        });
    };

    useEffect(() => {
        getData();
    }, []);

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

    const getDefinitionTypeDisplayName = (definitionType) => {
        switch (definitionType) {
            case 'OAS2':
                return Listing.CONST.OAS2;
            case 'OAS3':
                return Listing.CONST.OAS3;
            case 'WSDL1':
                return Listing.CONST.WSDL1;
            case 'WSDL2':
                return Listing.CONST.WSDL2;
            case 'GRAPHQL_SDL':
                return Listing.CONST.GRAPHQL_SDL;
            case 'ASYNC_API':
                return Listing.CONST.ASYNC_API;
            default:
                return definitionType;
        }
    };

    const columns = [
        {
            name: 'id',
            options: {
                display: 'excluded',
                filter: false,
            },
        },
        {
            name: 'displayName',
            label: intl.formatMessage({
                id: 'ServiceCatalog.Listing.Listing.name',
                defaultMessage: 'Service',
            }),
            options: {
                customBodyRender: (value, tableMeta = this) => {
                    if (tableMeta.rowData) {
                        const dataRow = serviceList[tableMeta.rowIndex];
                        const serviceId = dataRow.id;
                        if (dataRow) {
                            return (
                                <Link
                                    className={classes.serviceNameLink}
                                    to={'/service-catalog/' + serviceId + '/overview'}
                                >
                                    <span>{dataRow.displayName}</span>
                                </Link>
                            );
                        }
                    }
                    return <span />;
                },
                sort: false,
                filter: false,
            },
        },
        {
            name: 'serviceUrl',
            label: intl.formatMessage({
                id: 'ServiceCatalog.Listing.Listing.service.url',
                defaultMessage: 'Service URL',
            }),
            options: {
                customBodyRender: (value, tableMeta = this) => {
                    if (tableMeta.rowData) {
                        const dataRow = serviceList[tableMeta.rowIndex];
                        const { serviceUrl } = dataRow;
                        if (dataRow) {
                            return (
                                <span style={{
                                    whiteSpace: 'nowrap',
                                    textOverflow: 'ellipsis',
                                    width: '300px',
                                    display: 'block',
                                    overflow: 'hidden',
                                }}
                                >
                                    {serviceUrl}
                                </span>
                            );
                        }
                    }
                    return <span />;
                },
                sort: false,
                filter: false,
            },
        },
        {
            name: 'definitionType',
            label: intl.formatMessage({
                id: 'ServiceCatalog.Listing.Listing.schema.type',
                defaultMessage: 'Schema Type',
            }),
            options: {
                customBodyRender: (value, tableMeta = this) => {
                    if (tableMeta.rowData) {
                        const dataRow = serviceList[tableMeta.rowIndex];
                        const { definitionType } = dataRow;
                        if (dataRow) {
                            return (
                                <span>{getDefinitionTypeDisplayName(definitionType)}</span>
                            );
                        }
                    }
                    return <span />;
                },
                sort: false,
                filter: false,
            },
        },
        {
            name: 'version',
            label: intl.formatMessage({
                id: 'ServiceCatalog.Listing.Listing.version',
                defaultMessage: 'Version',
            }),
            options: {
                sort: false,
            },
        },
        {
            name: 'createdTime',
            label: intl.formatMessage({
                id: 'ServiceCatalog.Listing.Listing.created.time',
                defaultMessage: 'Created Time',
            }),
            options: {
                customBodyRender: (value, tableMeta = this) => {
                    if (tableMeta.rowData) {
                        const dataRow = serviceList[tableMeta.rowIndex];
                        const { createdTime } = dataRow;
                        if (dataRow) {
                            return (
                                <Tooltip
                                    placement='right'
                                    title={moment(createdTime).format('lll')}
                                    aria-label='add'
                                >
                                    <span>{moment(createdTime).fromNow()}</span>
                                </Tooltip>
                            );
                        }
                    }
                    return <span />;
                },
                sort: false,
                filter: false,
            },
        },
        {
            name: 'usage',
            label: intl.formatMessage({
                id: 'ServiceCatalog.Listing.Listing.usage',
                defaultMessage: 'Number of Usages',
            }),
            options: {
                customBodyRender: (value, tableMeta = this) => {
                    if (tableMeta.rowData) {
                        const dataRow = serviceList[tableMeta.rowIndex];
                        const { usage, id, displayName } = dataRow;
                        if (dataRow) {
                            return (
                                <Usages usageNumber={usage} serviceDisplayName={displayName} serviceId={id} />
                            );
                        }
                    }
                    return <span />;
                },
                sort: false,
                filter: false,
            },
        },
        {
            options: {
                customBodyRender: (value, tableMeta = this) => {
                    if (tableMeta.rowData) {
                        const dataRow = serviceList[tableMeta.rowIndex];
                        const { id, displayName, definitionType } = dataRow;
                        return (
                            <>
                                <Box display='flex' flexDirection='row'>
                                    <CreateApi
                                        history={history}
                                        serviceId={id}
                                        serviceDisplayName={displayName}
                                        definitionType={definitionType}
                                    />
                                    <Delete
                                        serviceDisplayName={displayName}
                                        serviceId={id}
                                        onDelete={onDelete}
                                    />
                                </Box>
                            </>
                        );
                    }
                    return false;
                },
                sort: false,
                name: 'actions',
                label: '',
            },
        },
    ];

    const options = {
        filterType: 'dropdown',
        selectableRows: 'none',
        title: false,
        filter: false,
        sort: false,
        print: false,
        download: false,
        viewColumns: false,
        customToolbar: false,
        rowsPerPageOptions: [5, 10, 25, 50, 100],
    };
    if (loading || !serviceList) {
        return <Progress per={90} message='Loading Services ...' />;
    }
    if (notFound) {
        return <ResourceNotFound />;
    }
    if (serviceList.length === 0) {
        return (
            <Onboarding />
        );
    }

    return (
        <>
            <div className={classes.content}>
                <div className={classes.contentInside}>
                    <Grid container direction='row' spacing={10}>
                        <Grid item md={11}>
                            <Typography className={classes.heading} variant='h4'>
                                <FormattedMessage
                                    id='ServiceCatalog.Listing.Listing.heading'
                                    defaultMessage='Service Catalog'
                                />
                            </Typography>
                        </Grid>
                        <Grid item md={1}>
                            <Tooltip
                                placement='right'
                                title={(
                                    <FormattedMessage
                                        id='ServiceCatalog.Listing.Listing.help.tooltip'
                                        defaultMessage='The Service Catalog enables API-first Integration'
                                    />
                                )}
                            >
                                <div className={classes.helpDiv}>
                                    <Help className={classes.helpIcon} />
                                </div>
                            </Tooltip>
                        </Grid>
                    </Grid>
                    <hr className={classes.horizontalDivider} />
                    <div className={classes.tableStyle}>
                        <MUIDataTable title='' data={serviceList} columns={columns} options={options} />
                    </div>
                </div>
            </div>
        </>
    );
}

Listing.CONST = {
    OAS2: 'Swagger',
    OAS3: 'Open API V3',
    WSDL1: 'WSDL 1',
    WSDL2: 'WSDL 2',
    GRAPHQL_SDL: 'GraphQL SDL',
    ASYNC_API: 'AsyncAPI',
    BASIC: 'Basic',
    DIGEST: 'Digest',
    OAUTH2: 'OAuth2',
    NONE: 'None',

};

export default Listing;
