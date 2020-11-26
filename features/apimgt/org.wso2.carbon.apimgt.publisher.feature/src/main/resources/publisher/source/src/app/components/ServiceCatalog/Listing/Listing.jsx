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
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Button from '@material-ui/core/Button';
import Box from '@material-ui/core/Box';
import Typography from '@material-ui/core/Typography';
import MUIDataTable from 'mui-datatables';
import { FormattedMessage, injectIntl } from 'react-intl';
import { Progress } from 'AppComponents/Shared';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import Alert from 'AppComponents/Shared/Alert';
import ServiceCatalog from 'AppData/ServiceCatalog';
import Onboarding from 'AppComponents/ServiceCatalog/Listing/Onboarding';
import Delete from 'AppComponents/ServiceCatalog/Listing/Delete';
import Edit from 'AppComponents/ServiceCatalog/Listing/Edit';
import Grid from '@material-ui/core/Grid';
import Help from '@material-ui/icons/Help';
import Tooltip from '@material-ui/core/Tooltip';

const styles = (theme) => ({
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
    textStyle: {
        fontSize: 11,
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
    serviceNameStyle: {
        color: theme.palette.primary.main,
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
});

/**
 * Listing for service catalog entries
 *
 * @class Listing
 * @extends {React.Component}
 */
class Listing extends React.Component {
    /**
     * @inheritdoc
     * @param {*} props properties
     * @memberof Listing
     */
    constructor(props) {
        super(props);
        this.state = {
            serviceList: null,
            notFound: true,
            loading: true,
        };
    }

    componentDidMount() {
        this.getData();
    }

    // Get Services
    getData = () => {
        const { intl } = this.props;
        const promisedServices = ServiceCatalog.searchServices();
        promisedServices.then((data) => {
            const { body } = data;
            const { list } = body;
            this.setState({ serviceList: list, notFound: false });
        }).catch(() => {
            Alert.error(intl.formatMessage({
                defaultMessage: 'Error While Loading Services',
                id: 'ServiceCatalog.Listing.Listing.error.loading',
            }));
        }).finally(() => {
            this.setState({ loading: false });
        });
    };

    onDelete = (serviceId) => {
        const { intl } = this.props;
        const deleteServicePromise = ServiceCatalog.deleteService(serviceId);
        deleteServicePromise.then(() => {
            Alert.info(intl.formatMessage({
                id: 'ServiceCatalog.Listing.Listing.service.deleted.successfully',
                defaultMessage: 'Service deleted successfully!',
            }));
            // Reload the services list
            this.getData();
        }).catch(() => {
            Alert.error(intl.formatMessage({
                defaultMessage: 'Error while deleting service',
                id: 'ServiceCatalog.Listing.Listing.error.delete',
            }));
        });
    };

    /**
     * Function for updating a given service entry
     * @param {string} serviceId ID of the service
     * @param {object} body service payload
     */
    onEdit = (serviceId, body) => {
        const { intl } = this.props;
        const updateServicePromise = ServiceCatalog.updateService(serviceId, body);
        updateServicePromise.then(() => {
            Alert.info(intl.formatMessage({
                id: 'ServiceCatalog.Listing.Listing.service.updated.successfully',
                defaultMessage: 'Service updated successfully!',
            }));
            // Reload the services list
            this.getData();
        }).catch(() => {
            Alert.error(intl.formatMessage({
                defaultMessage: 'Error while updating service',
                id: 'ServiceCatalog.Listing.Listing.error.update',
            }));
        });
    }

    /**
     *
     *
     * @returns
     * @memberof Listing
     */
    render() {
        const {
            intl, classes,
        } = this.props;
        const { loading } = this.state;
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
                    customBodyRender: (value, tableMeta, updateValue, tableViewObj = this) => {
                        if (tableMeta.rowData) {
                            const dataRow = tableViewObj.state.serviceList[tableMeta.rowIndex];
                            const serviceDisplayName = tableMeta.rowData[1];
                            if (dataRow) {
                                return (
                                    <div className={classes.serviceNameStyle}>
                                        <span>{serviceDisplayName}</span>
                                    </div>
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
                    sort: false,
                },
            },
            {
                name: 'definitionType',
                label: intl.formatMessage({
                    id: 'ServiceCatalog.Listing.Listing.service.type',
                    defaultMessage: 'Service Type',
                }),
                options: {
                    sort: false,
                },
            },
            {
                name: 'definitionType',
                label: intl.formatMessage({
                    id: 'ServiceCatalog.Listing.Listing.schema.type',
                    defaultMessage: 'Schema Type',
                }),
                options: {
                    sort: false,
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
                name: 'usage',
                label: intl.formatMessage({
                    id: 'ServiceCatalog.Listing.Listing.usage',
                    defaultMessage: 'No. Of APIs',
                }),
                options: {
                    sort: false,
                },
            },
            {
                options: {
                    customBodyRender: (value, tableMeta, updateValue, tableViewObj = this) => {
                        if (tableMeta.rowData) {
                            const dataRow = tableViewObj.state.serviceList[tableMeta.rowIndex];
                            return (
                                <Box display='flex' flexDirection='row'>
                                    <Link>
                                        <Button color='primary' variant='outlined' className={classes.buttonStyle}>
                                            <Typography className={classes.textStyle}>
                                                <FormattedMessage
                                                    id='ServiceCatalog.Listing.Listing.create.api'
                                                    defaultMessage='Create API'
                                                />
                                            </Typography>
                                        </Button>
                                    </Link>
                                    <Edit dataRow={dataRow} onEdit={this.onEdit} />
                                    <Delete
                                        serviceDisplayName={dataRow.displayName}
                                        serviceId={dataRow.id}
                                        onDelete={this.onDelete}
                                    />
                                </Box>
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
        const {
            serviceList, notFound,
        } = this.state;
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
}

export default injectIntl(withStyles(styles, { withTheme: true })(Listing));

Listing.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({ formatMessage: PropTypes.func.isRequired }).isRequired,
    theme: PropTypes.shape({
        custom: PropTypes.string,
    }).isRequired,
};
