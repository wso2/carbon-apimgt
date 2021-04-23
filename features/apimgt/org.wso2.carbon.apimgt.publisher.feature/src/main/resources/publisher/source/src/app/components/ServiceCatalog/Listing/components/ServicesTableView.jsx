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
import { Link } from 'react-router-dom';
import { makeStyles } from '@material-ui/core/styles';
import Box from '@material-ui/core/Box';
import Tooltip from '@material-ui/core/Tooltip';
import Configurations from 'Config';
import Delete from 'AppComponents/ServiceCatalog/Listing/Delete';
import Usages from 'AppComponents/ServiceCatalog/Listing/Usages';
import CreateApi from 'AppComponents/ServiceCatalog/CreateApi';
import { isRestricted } from 'AppData/AuthManager';
import MUIDataTable from 'mui-datatables';
import { FormattedMessage } from 'react-intl';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';

dayjs.extend(relativeTime);


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
function ServicesTableView(props) {
    const { serviceList, onDelete } = props;

    const classes = useStyles();

    const getDefinitionTypeDisplayName = (definitionType) => {
        return Configurations.serviceCatalogDefinitionTypes[definitionType] || definitionType;
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
            name: 'name',
            label: <FormattedMessage
                id='ServiceCatalog.ServicesTableView.ServicesTableView.name'
                defaultMessage='Service'
            />,
            options: {
                customBodyRender: (value, tableMeta) => {
                    if (tableMeta.rowData) {
                        const dataRow = serviceList[tableMeta.rowIndex];
                        const serviceId = dataRow.id;
                        if (dataRow) {
                            return (
                                <Link
                                    className={classes.serviceNameLink}
                                    to={'/service-catalog/' + serviceId + '/overview'}
                                >
                                    <span>{dataRow.name}</span>
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
            label: <FormattedMessage
                id='ServiceCatalog.ServicesTableView.ServicesTableView.service.url'
                defaultMessage='Service URL'
            />,
            options: {
                customBodyRender: (value, tableMeta) => {
                    if (tableMeta.rowData) {
                        const dataRow = serviceList[tableMeta.rowIndex];
                        const { serviceUrl } = dataRow;
                        if (dataRow) {
                            return (
                                <Tooltip
                                    placement='top-start'
                                    title={serviceUrl}
                                    aria-label='add'
                                >
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
            name: 'definitionType',
            label: <FormattedMessage
                id='ServiceCatalog.ServicesTableView.ServicesTableView.schema.type'
                defaultMessage='Schema Type'
            />,
            options: {
                customBodyRender: (value, tableMeta) => {
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
            label: <FormattedMessage
                id='ServiceCatalog.ServicesTableView.ServicesTableView.version'
                defaultMessage='Version'
            />,
            options: {
                sort: false,
            },
        },
        {
            name: 'createdTime',
            label: <FormattedMessage
                id='ServiceCatalog.ServicesTableView.ServicesTableView.created.time'
                defaultMessage='Created Time'
            />,
            options: {
                customBodyRender: (value, tableMeta) => {
                    if (tableMeta.rowData) {
                        const dataRow = serviceList[tableMeta.rowIndex];
                        const { createdTime } = dataRow;
                        if (dataRow) {
                            return (
                                <Tooltip
                                    placement='top-start'
                                    title={dayjs(createdTime).format('lll')}
                                    aria-label='add'
                                >
                                    <span>{dayjs(createdTime).fromNow()}</span>
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
            label: <FormattedMessage
                id='ServiceCatalog.ServicesTableView.ServicesTableView.usage'
                defaultMessage='Number of Usages'
            />,
            options: {
                customBodyRender: (value, tableMeta) => {
                    if (tableMeta.rowData) {
                        const dataRow = serviceList[tableMeta.rowIndex];
                        const { usage, id, name } = dataRow;
                        if (dataRow) {
                            return (
                                <Usages usageNumber={usage} serviceDisplayName={name} serviceId={id} />
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
                customBodyRender: (value, tableMeta) => {
                    if (tableMeta.rowData) {
                        const dataRow = serviceList[tableMeta.rowIndex];
                        const {
                            id, serviceKey, name, definitionType, version, serviceUrl, usage,
                        } = dataRow;
                        return (
                            <>
                                {!isRestricted(['apim:api_create']) && (
                                    <Box display='flex' flexDirection='row'>
                                        <CreateApi
                                            serviceId={id}
                                            serviceKey={serviceKey}
                                            definitionType={definitionType}
                                            serviceDisplayName={name}
                                            serviceVersion={version}
                                            serviceUrl={serviceUrl}
                                            usage={usage}
                                        />
                                        <Delete
                                            serviceDisplayName={name}
                                            serviceId={id}
                                            onDelete={onDelete}
                                        />
                                    </Box>
                                )}
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
        elevation: 1,
    };

    return (

        <div className={classes.tableStyle}>
            <MUIDataTable title='' data={serviceList} columns={columns} options={options} />
        </div>
    );
}

export default ServicesTableView;
