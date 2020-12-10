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

import React, { useState, useEffect } from 'react';
import { FormattedMessage, useIntl } from 'react-intl';
import { makeStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import Button from '@material-ui/core/Button';
import Icon from '@material-ui/core/Icon';
import Configurations from 'Config';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import Chip from '@material-ui/core/Chip';
import Tooltip from '@material-ui/core/Tooltip';
import { Progress } from 'AppComponents/Shared';
import Alert from 'AppComponents/Shared/Alert';
import ServiceCatalog from 'AppData/ServiceCatalog';
import Container from '@material-ui/core/Container';
import Box from '@material-ui/core/Box';
import Paper from '@material-ui/core/Paper';
import moment from 'moment';
import PropTypes from 'prop-types';
import LocalOfferOutlinedIcon from '@material-ui/icons/LocalOfferOutlined';

const useStyles = makeStyles((theme) => ({
    preview: {
        height: theme.spacing(16),
        marginBottom: theme.spacing(3),
        marginLeft: theme.spacing(1),
    },
    contentWrapper: {
        marginTop: theme.spacing(3),
        alignItems: 'center',
    },
    iconAligner: {
        display: 'flex',
        justifyContent: 'flex-start',
        alignItems: 'center',
    },
    tableIcon: {
        width: theme.spacing(3),
    },
    bodyStyle: {
        marginLeft: theme.spacing(2),
        marginBottom: theme.spacing(3),
    },
    contentTopBarStyle: {
        display: 'flex',
    },
    table: {
        minWidth: '100%',
    },
    iconTextWrapper: {
        display: 'inline-block',
        paddingLeft: 20,
    },
    versionBarStyle: {
        marginTop: theme.spacing(1),
        display: 'flex',
    },
    topBarDetailsSectionStyle: {
        marginLeft: theme.spacing(5),
    },
    versionStyle: {
        marginLeft: theme.spacing(1),
    },
    topMarginSpacing: {
        marginTop: theme.spacing(2),
        display: 'flex',
    },
    chipStyle: {
        marginTop: theme.spacing(3),
    },
    moreButtonSansDescription: {
        marginTop: theme.spacing(10),
    },
    expandButton: {
        marginTop: -7,
    },
    headingSpacing: {
        marginTop: theme.spacing(3),
    },
    buttonWrapper: {
        paddingTop: 10,
    },
    buttonSection: {
        paddingTop: theme.spacing(1),
    },
}));

/**
 * Service Catalog Overview Page
 *
 * @param {any} props props
 * @returns {any} Overview page of a service
 */
function Overview(props) {
    const classes = useStyles();
    const intl = useIntl();
    const { match, history } = props;
    const serviceId = match.params.service_uuid;
    const [service, setService] = useState(null);

    // Get Service Details
    const getService = () => {
        const promisedService = ServiceCatalog.getServiceById(serviceId);
        promisedService.then((data) => {
            setService(data);
        }).catch(() => {
            Alert.error(intl.formatMessage({
                defaultMessage: 'Error While Loading Service',
                id: 'ServiceCatalog.Listing.Edit.error.loading.service',
            }));
        });
        return null;
    };

    useEffect(() => {
        getService();
    }, []);

    const listingRedirect = () => {
        history.push('/service-catalog');
    };

    if (!service) {
        return <Progress per={90} message='Loading Service ...' />;
    }

    let serviceTypeIcon = (
        <img
            className={classes.preview}
            src={Configurations.app.context + '/site/public/images/restAPIIcon.png'}
            alt='Type API'
        />
    );
    if (service.definitionType === 'OAS3' || service.definitionType === 'OAS2') {
        serviceTypeIcon = (
            <Tooltip
                position='right'
                title={(
                    <FormattedMessage
                        id='ServiceCatalog.Listing.Overview.service.type.rest.tooltip'
                        defaultMessage='REST Service'
                    />
                )}
            >
                <img
                    className={classes.preview}
                    src={Configurations.app.context + '/site/public/images/swaggerIcon.svg'}
                    alt='Type Rest API'
                />
            </Tooltip>
        );
    } else if (service.definitionType === 'GRAPHQL_SDL') {
        serviceTypeIcon = (
            <Tooltip
                position='right'
                title={(
                    <FormattedMessage
                        id='ServiceCatalog.Listing.Overview.service.type.graphql.tooltip'
                        defaultMessage='GraphQL Service'
                    />
                )}
            >
                <img
                    className={classes.preview}
                    src={Configurations.app.context + '/site/public/images/graphqlIcon.svg'}
                    alt='Type GraphQL API'
                />
            </Tooltip>
        );
    } else if (service.definitionType === 'ASYNC_API') {
        serviceTypeIcon = (
            <Tooltip
                position='right'
                title={(
                    <FormattedMessage
                        id='ServiceCatalog.Listing.Overview.service.type.async.tooltip'
                        defaultMessage='Async API Service'
                    />
                )}
            >
                <img
                    className={classes.preview}
                    src={Configurations.app.context + '/site/public/images/asyncAPIIcon.jpeg'}
                    alt='Type Async API'
                />
            </Tooltip>
        );
    } else if (service.definitionType === 'WSDL1' || service.definitionType === 'WSDL2') {
        serviceTypeIcon = (
            <Tooltip
                position='right'
                title={(
                    <FormattedMessage
                        id='ServiceCatalog.Listing.Overview.service.type.soap.tooltip'
                        defaultMessage='SOAP Service'
                    />
                )}
            >
                <img
                    className={classes.preview}
                    src={Configurations.app.context + '/site/public/images/restAPIIcon.png'}
                    alt='Type SOAP API'
                />
            </Tooltip>
        );
    }

    return (
        <>
            <Container maxWidth='md'>
                <Box mb={3} className={classes.headingSpacing}>
                    <Typography variant='h4'>
                        <FormattedMessage
                            id='ServiceCatalog.Listing.Overview.heading'
                            defaultMessage='Overview'
                        />
                    </Typography>
                    <Typography variant='caption'>
                        <FormattedMessage
                            id='ServiceCatalog.Listing.Overview.heading.caption'
                            defaultMessage='Overview of the service'
                        />
                    </Typography>
                </Box>
                <Paper elevation={1}>
                    <Box px={8} py={5}>
                        <div>
                            <Grid container spacing={1}>
                                <Grid item md={9}>
                                    <div className={classes.contentTopBarStyle}>
                                        {serviceTypeIcon}
                                        <div className={classes.topBarDetailsSectionStyle}>
                                            <div className={classes.versionBarStyle}>
                                                <Typography className={classes.heading} variant='h5'>
                                                    <FormattedMessage
                                                        id='ServiceCatalog.Listing.Overview.service.display.name'
                                                        defaultMessage='{serviceDisplayName}'
                                                        values={{ serviceDisplayName: service.displayName }}
                                                    />
                                                </Typography>
                                            </div>
                                            <div className={classes.versionBarStyle}>
                                                <LocalOfferOutlinedIcon />
                                                <Typography className={classes.versionStyle}>
                                                    <FormattedMessage
                                                        id='ServiceCatalog.Listing.Overview.service.version'
                                                        defaultMessage='{serviceVersion}'
                                                        values={{ serviceVersion: service.version }}
                                                    />
                                                </Typography>
                                            </div>
                                            <div className={classes.chipStyle}>
                                                <Chip
                                                    variant='outlined'
                                                    color='primary'
                                                    label={intl.formatMessage({
                                                        id: 'ServiceCatalog.Listing.Overview.usage.data',
                                                        defaultMessage: 'Used by {usage} API(s)',
                                                    }, { usage: service.usage })}
                                                />
                                            </div>
                                        </div>
                                    </div>
                                </Grid>
                                <Grid item md={3}>
                                    <Button color='primary' variant='contained' className={classes.topMarginSpacing}>
                                        <Typography>
                                            <FormattedMessage
                                                id='ServiceCatalog.Listing.Overview.create.api'
                                                defaultMessage='Create API'
                                            />
                                        </Typography>
                                    </Button>
                                </Grid>
                            </Grid>
                        </div>
                        <div className={classes.bodyStyle}>
                            <Grid container spacing={1}>
                                { service.description && service.description !== '' && (
                                    <>
                                        <Grid item md={12}>
                                            <Typography>
                                                <FormattedMessage
                                                    id='ServiceCatalog.Listing.Overview.service.description'
                                                    defaultMessage='{description}'
                                                    values={{ description: service.description }}
                                                />
                                            </Typography>
                                        </Grid>
                                    </>
                                )}
                            </Grid>
                            <div className={classes.contentWrapper}>
                                <Table className={classes.table}>
                                    <TableBody>
                                        <TableRow>
                                            <TableCell component='th' scope='row'>
                                                <div className={classes.iconAligner}>
                                                    <Icon className={classes.tableIcon}>link</Icon>
                                                    <span className={classes.iconTextWrapper}>
                                                        <FormattedMessage
                                                            id='ServiceCatalog.Listing.Overview.service.url'
                                                            defaultMessage='Service URL'
                                                        />
                                                    </span>
                                                </div>
                                            </TableCell>
                                            <TableCell>{service.serviceUrl}</TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell component='th' scope='row'>
                                                <div className={classes.iconAligner}>
                                                    <Icon className={classes.tableIcon}>code</Icon>
                                                    <span className={classes.iconTextWrapper}>
                                                        <FormattedMessage
                                                            id='ServiceCatalog.Listing.Overview.definition.type'
                                                            defaultMessage='Schema Type'
                                                        />
                                                    </span>
                                                </div>
                                            </TableCell>
                                            <TableCell>{service.definitionType}</TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell component='th' scope='row'>
                                                <div className={classes.iconAligner}>
                                                    <Icon className={classes.tableIcon}>security</Icon>
                                                    <span className={classes.iconTextWrapper}>
                                                        <FormattedMessage
                                                            id='ServiceCatalog.Listing.Overview.security.type'
                                                            defaultMessage='Security Type'
                                                        />
                                                    </span>
                                                </div>
                                            </TableCell>
                                            <TableCell>{service.securityType}</TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell component='th' scope='row'>
                                                <div className={classes.iconAligner}>
                                                    <Icon className={classes.tableIcon}>sync_alt</Icon>
                                                    <span className={classes.iconTextWrapper}>
                                                        <FormattedMessage
                                                            id='ServiceCatalog.Listing.Overview.mutual.ssl'
                                                            defaultMessage='Mutual SSL'
                                                        />
                                                    </span>
                                                </div>
                                            </TableCell>
                                            <TableCell>
                                                {service.mutualSSLEnabled ? (
                                                    <FormattedMessage
                                                        id='ServiceCatalog.Listing.Overview.mutual.ssl.enabled'
                                                        defaultMessage='Enabled'
                                                    />
                                                ) : (
                                                    <FormattedMessage
                                                        id='ServiceCatalog.Listing.Overview.mutual.ssl.disabled'
                                                        defaultMessage='Disabled'
                                                    />
                                                )}
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell component='th' scope='row'>
                                                <div className={classes.iconAligner}>
                                                    <Icon className={classes.tableIcon}>timeline</Icon>
                                                    <span className={classes.iconTextWrapper}>
                                                        <FormattedMessage
                                                            id='ServiceCatalog.Listing.Overview.created.time'
                                                            defaultMessage='Created Time'
                                                        />
                                                    </span>
                                                </div>
                                            </TableCell>
                                            <TableCell>{moment(service.createdTime).fromNow()}</TableCell>
                                        </TableRow>
                                    </TableBody>
                                </Table>
                            </div>
                        </div>
                        <div className={classes.buttonWrapper}>
                            <Grid
                                container
                                direction='row'
                                alignItems='flex-start'
                                spacing={1}
                                className={classes.buttonSection}
                            >
                                <Grid item>
                                    <Button onClick={listingRedirect} color='primary'>
                                        <FormattedMessage
                                            id='ServiceCatalog.Listing.Overview.back.btn'
                                            defaultMessage='Go Back'
                                        />
                                    </Button>
                                </Grid>
                            </Grid>
                        </div>
                    </Box>
                </Paper>
            </Container>
        </>
    );
}

Overview.propTypes = {
    match: PropTypes.shape({
        params: PropTypes.object,
    }).isRequired,
};

export default Overview;
