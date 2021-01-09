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

import React, {
    useState,
    useEffect,
    lazy,
    Suspense,
} from 'react';
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
import Tooltip from '@material-ui/core/Tooltip';
import { Progress } from 'AppComponents/Shared';
import Alert from 'AppComponents/Shared/Alert';
import ServiceCatalog from 'AppData/ServiceCatalog';
import Container from '@material-ui/core/Container';
import Utils from 'AppData/Utils';
import CloudDownloadRounded from '@material-ui/icons/CloudDownloadRounded';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import ExpandLessIcon from '@material-ui/icons/ExpandLess';
import CreateApi from 'AppComponents/ServiceCatalog/CreateApi';
import Usages from 'AppComponents/ServiceCatalog/Listing/Usages';
import Listing from 'AppComponents/ServiceCatalog/Listing/Listing';
import VerticalDivider from 'AppComponents/Shared/VerticalDivider';
import SwapHorizontalCircle from '@material-ui/icons/SwapHorizontalCircle';
import YAML from 'js-yaml';
import Box from '@material-ui/core/Box';
import Breadcrumbs from '@material-ui/core/Breadcrumbs';
import { Link } from 'react-router-dom';
import Paper from '@material-ui/core/Paper';
import moment from 'moment';
import PropTypes from 'prop-types';
import LocalOfferOutlinedIcon from '@material-ui/icons/LocalOfferOutlined';

const MonacoEditor = lazy(() => import('react-monaco-editor' /* webpackChunkName: "APIDefMonacoEditor" */));

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
    apiUsageStyle: {
        marginTop: theme.spacing(3),
    },
    headingSpacing: {
        marginTop: theme.spacing(3),
    },
    buttonWrapper: {
        paddingTop: 10,
    },
    buttonSection: {
        paddingTop: theme.spacing(1),
        marginLeft: theme.spacing(2),
    },
    paperStyle: {
        marginBottom: theme.spacing(3),
    },
    downloadServiceFlex: {
        display: 'flex',
        flexDirection: 'row',
        justifyContent: 'space-between',
        marginLeft: -8,
    },
    downloadServiceGroup: {
        display: 'flex',
        flexDirection: 'row',
        marginLeft: -8,
    },
    iconSpacing: {
        marginRight: theme.spacing(1),
    },
    button: {
        marginLeft: theme.spacing(2),
    },
    buttonIcon: {
        marginRight: theme.spacing(1),
    },
    downloadButtonSpacing: {
        marginLeft: theme.spacing(1),
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
    const [notFound, setNotFound] = useState(true);
    const [serviceDefinition, setServiceDefinition] = useState({});
    const [openReadOnlyDefinition, setOpenReadOnlyDefinition] = useState(false);
    const [format, setFormat] = useState('yaml');
    const [convertTo, setConvertTo] = useState(null);

    // Get Service Details
    const getService = () => {
        const promisedService = ServiceCatalog.getServiceById(serviceId);
        promisedService.then((data) => {
            setService(data);
            setNotFound(false);
        }).catch(() => {
            Alert.error(intl.formatMessage({
                defaultMessage: 'Error while loading service',
                id: 'ServiceCatalog.Listing.Overview.error.loading.service',
            }));
        });
        return null;
    };

    /**
     * Export Service as a zipped archive
     * @param {string} serviceName The name of the service
     * @param {string} serviceVersion Version of the service
     * @returns {zip} Zip file containing the Service.
     */
    function exportService(serviceName, serviceVersion) {
        return ServiceCatalog.exportService(serviceName, serviceVersion).then((zipFile) => {
            return Utils.forceDownload(zipFile);
        }).catch((error) => {
            if (error.response) {
                Alert.error(error.response.body.description);
            } else {
                Alert.error(intl.formatMessage({
                    id: 'ServiceCatalog.Listing.Overview.download.service.zip.error',
                    defaultMessage: 'Something went wrong while downloading the Service.',
                }));
            }
        });
    }

    /**
     * Toggle the format of the service definition.
     * JSON -> YAML, YAML -> JSON
     */
    const onChangeFormatClick = () => {
        let formattedString = '';
        if (convertTo === 'json') {
            formattedString = JSON.stringify(YAML.load(serviceDefinition), null, 1);
        } else {
            formattedString = YAML.safeDump(YAML.safeLoad(serviceDefinition));
        }
        setServiceDefinition(formattedString);
        const tmpConvertTo = convertTo;
        setConvertTo(format);
        setFormat(tmpConvertTo);
    };

    const getConvertToFormat = (value) => {
        return value === 'json' ? 'yaml' : 'json';
    };

    const showServiceDefinition = () => {
        if (openReadOnlyDefinition) {
            setOpenReadOnlyDefinition(false);
            setConvertTo(null);
            setServiceDefinition({});
            setFormat('yaml');
        } else {
            const promisedServiceDefinition = ServiceCatalog.getServiceDefinition(serviceId);
            promisedServiceDefinition.then((data) => {
                if (service.definitionType !== 'GRAPHQL_SDL') {
                    setServiceDefinition(YAML.safeDump(YAML.safeLoad(data)));
                    setFormat('yaml');
                    setConvertTo(getConvertToFormat(format));
                } else {
                    setServiceDefinition(data.obj.schemaDefinition);
                    setFormat('txt');
                    setConvertTo(null);
                }
                setOpenReadOnlyDefinition(true);
            }).catch((error) => {
                if (error.response) {
                    Alert.error(error.response.body.description);
                } else {
                    Alert.error(intl.formatMessage({
                        id: 'ServiceCatalog.Listing.Overview.retrieve.service.def.error',
                        defaultMessage: 'Something went wrong while retrieving the Service Definition.',
                    }));
                }
            });
        }
        return null;
    };

    useEffect(() => {
        getService();
    }, []);

    const listingRedirect = () => {
        history.push('/service-catalog');
    };

    const renderContent = () => {
        if (service.mutualSSLEnabled) {
            return (
                <FormattedMessage
                    id='ServiceCatalog.Listing.Overview.mutual.ssl.enabled'
                    defaultMessage='Enabled'
                />
            );
        } else {
            return (
                <FormattedMessage
                    id='ServiceCatalog.Listing.Overview.mutual.ssl.disabled'
                    defaultMessage='Disabled'
                />
            );
        }
    };

    if (!service) {
        return <Progress per={90} message='Loading Service ...' />;
    }

    if (notFound) {
        return <ResourceNotFound />;
    }

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

    const getSecurityTypeDisplayName = (securityType) => {
        switch (securityType) {
            case 'BASIC':
                return Listing.CONST.BASIC;
            case 'DIGEST':
                return Listing.CONST.DIGEST;
            case 'OAUTH2':
                return Listing.CONST.OAUTH2;
            case 'NONE':
                return Listing.CONST.NONE;
            default:
                return securityType;
        }
    };

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

    const editorOptions = {
        selectOnLineNumbers: true,
        readOnly: true,
        smoothScrolling: true,
        wordWrap: 'on',
        scrollBeyondLastLine: false,
    };

    return (
        <>
            <Container maxWidth='md'>
                <Box mb={3} className={classes.headingSpacing}>
                    <Breadcrumbs aria-label='breadcrumb'>
                        <Link color='inherit' to='/service-catalog'>
                            <FormattedMessage
                                id='ServiceCatalog.Listing.Overview.parent.breadcrumb'
                                defaultMessage='Service Catalog'
                            />
                        </Link>
                        <Typography color='textPrimary'>
                            <FormattedMessage
                                id='ServiceCatalog.Listing.Overview.readonly.breadcrumb'
                                defaultMessage='Overview'
                            />
                        </Typography>
                    </Breadcrumbs>
                </Box>
                <Paper elevation={1} className={classes.paperStyle}>
                    <Box px={8} py={5}>
                        <div>
                            <Grid container spacing={1}>
                                <Grid item md={10}>
                                    <div className={classes.contentTopBarStyle}>
                                        {serviceTypeIcon}
                                        <div className={classes.topBarDetailsSectionStyle}>
                                            <div className={classes.versionBarStyle}>
                                                <Typography className={classes.heading} variant='h5'>
                                                    <FormattedMessage
                                                        id='ServiceCatalog.Listing.Overview.display.name'
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
                                            <Usages
                                                usageNumber={service.usage}
                                                serviceDisplayName={service.displayName}
                                                serviceId={service.id}
                                                isOverview
                                                classes={classes}
                                            />
                                        </div>
                                    </div>
                                </Grid>
                                <Grid item md={2}>
                                    <Box display='flex' flexDirection='column'>
                                        <CreateApi
                                            history={history}
                                            serviceId={service.id}
                                            serviceDisplayName={service.displayName}
                                            definitionType={service.definitionType}
                                            isOverview
                                        />
                                    </Box>
                                </Grid>
                            </Grid>
                        </div>
                        <div className={classes.bodyStyle}>
                            <Grid container spacing={1}>
                                { (service.description && service.description !== '') && (
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
                                            <TableCell style={{ maxWidth: '220px', wordWrap: 'break-word' }}>
                                                {service.serviceUrl}
                                            </TableCell>
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
                                            <TableCell>
                                                {getDefinitionTypeDisplayName(service.definitionType)}
                                            </TableCell>
                                        </TableRow>
                                        <TableRow>
                                            <TableCell component='th' scope='row'>
                                                <div className={classes.iconAligner}>
                                                    <Icon className={classes.tableIcon}>description</Icon>
                                                    <span className={classes.iconTextWrapper}>
                                                        <FormattedMessage
                                                            id='ServiceCatalog.Listing.Overview.definition.download'
                                                            defaultMessage='Service Definition'
                                                        />
                                                    </span>
                                                </div>
                                            </TableCell>
                                            <TableCell>
                                                <div className={classes.downloadServiceFlex}>
                                                    <div className={classes.downloadServiceGroup}>
                                                        <Button
                                                            onClick={
                                                                () => exportService(
                                                                    service.displayName,
                                                                    service.version,
                                                                )
                                                            }
                                                            color='primary'
                                                            className={classes.downloadButtonSpacing}
                                                        >
                                                            <CloudDownloadRounded className={classes.iconSpacing} />
                                                            <FormattedMessage
                                                                id='ServiceCatalog.Listing.Overview.download.service'
                                                                defaultMessage='Download'
                                                            />
                                                        </Button>
                                                        <VerticalDivider height={30} />
                                                        <Button
                                                            onClick={showServiceDefinition}
                                                            color='primary'
                                                            endIcon={
                                                                openReadOnlyDefinition
                                                                    ? (<ExpandLessIcon />) : (<ExpandMoreIcon />)
                                                            }
                                                        >
                                                            <FormattedMessage
                                                                id='ServiceCatalog.Listing.Overview.view.definition'
                                                                defaultMessage='View Definition'
                                                            />
                                                        </Button>
                                                    </div>
                                                    { service.definitionType !== 'GRAPHQL_SDL' && convertTo && (
                                                        <div>
                                                            <Button
                                                                size='small'
                                                                className={classes.button}
                                                                onClick={onChangeFormatClick}
                                                            >
                                                                <SwapHorizontalCircle className={classes.buttonIcon} />
                                                                <FormattedMessage
                                                                    id='ServiceCatalog.Listing.Overview.convert.to'
                                                                    defaultMessage='Convert to'
                                                                />
                                                                {' '}
                                                                {convertTo}
                                                            </Button>
                                                        </div>
                                                    )}
                                                </div>
                                                {openReadOnlyDefinition && (
                                                    <Suspense fallback={<Progress />}>
                                                        <MonacoEditor
                                                            language={format}
                                                            width={(service.definitionType !== 'GRAPHQL_SDL')
                                                                ? 'calc(100% + 55px)' : 'calc(100% + 120px)'}
                                                            height='calc(75vh - 200px)'
                                                            theme='vs-dark'
                                                            value={serviceDefinition}
                                                            options={editorOptions}
                                                        />
                                                    </Suspense>
                                                )}
                                            </TableCell>
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
                                            <TableCell>
                                                {getSecurityTypeDisplayName(service.securityType)}
                                            </TableCell>
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
                                                { renderContent() }
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
                                            <TableCell>
                                                <Tooltip
                                                    placement='right'
                                                    title={moment(service.createdTime).format('lll')}
                                                >
                                                    <span>{moment(service.createdTime).fromNow()}</span>
                                                </Tooltip>
                                            </TableCell>
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
