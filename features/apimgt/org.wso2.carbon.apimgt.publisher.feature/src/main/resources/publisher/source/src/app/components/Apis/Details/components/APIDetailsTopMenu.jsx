/*
 * Copyright (c), WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React, { useState, useContext, useEffect } from 'react';
import PropTypes from 'prop-types';
import Typography from '@material-ui/core/Typography';
import Box from '@material-ui/core/Box';
import Utils from 'AppData/Utils';
import Alert from 'AppComponents/Shared/Alert';
import { FormattedMessage, injectIntl } from 'react-intl';
import LaunchIcon from '@material-ui/icons/Launch';
import CloudDownloadRounded from '@material-ui/icons/CloudDownloadRounded';
import { isRestricted } from 'AppData/AuthManager';
import { withStyles } from '@material-ui/core/styles';
import { Link, useHistory } from 'react-router-dom';
import ApiContext from 'AppComponents/Apis/Details/components/ApiContext';
import { useAppContext } from 'AppComponents/Shared/AppContext';
import { useRevisionContext } from 'AppComponents/Shared/RevisionContext';
import ThumbnailView from 'AppComponents/Apis/Listing/components/ImageGenerator/ThumbnailView';
import VerticalDivider from 'AppComponents/Shared/VerticalDivider';
import MenuItem from '@material-ui/core/MenuItem';
import TextField from '@material-ui/core/TextField';
import Grid from '@material-ui/core/Grid';
import GoTo from 'AppComponents/Apis/Details/GoTo/GoTo';
import Tooltip from '@material-ui/core/Tooltip';
import API from 'AppData/api';
import MUIAlert from 'AppComponents/Shared/MuiAlert';
import DeleteApiButton from './DeleteApiButton';
import CreateNewVersionButton from './CreateNewVersionButton';

const styles = (theme) => ({
    root: {
        height: theme.custom.apis.topMenu.height,
        background: theme.palette.background.paper,
        borderBottom: 'solid 1px ' + theme.palette.grey.A200,
        display: 'flex',
        alignItems: 'center',
    },
    backLink: {
        alignItems: 'center',
        textDecoration: 'none',
        display: 'flex',
        color: theme.palette.getContrastText(theme.palette.background.paper),
    },
    backIcon: {
        color: theme.palette.primary.main,
        fontSize: 56,
        cursor: 'pointer',
    },
    backText: {
        color: theme.palette.primary.main,
        cursor: 'pointer',
        fontFamily: theme.typography.fontFamily,
    },
    viewInStoreLauncher: {
        display: 'flex',
        flexDirection: 'column',
        color: theme.palette.getContrastText(theme.palette.background.paper),
        textAlign: 'center',
        justifyContent: 'center',
        height: 70,
    },
    linkText: {
        fontSize: theme.typography.fontSize,
    },
    dateWrapper: {
        flex: 1,
        alignSelf: 'center',
    },
    lastUpdatedTypography: {
        display: 'inline-block',
        minWidth: 30,
    },
    apiName: {
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        whiteSpace: 'nowrap',
    },
    downloadApi: {
        display: 'flex',
        flexDirection: 'column',
        textAlign: 'center',
        justifyContent: 'center',
        cursor: 'pointer',
        height: 70,
        color: theme.palette.getContrastText(theme.palette.background.paper),
    },
    downloadApiFlex: {
        display: 'flex',
        flexDirection: 'column',
    },
    revisionWrapper: {
        paddingRight: theme.spacing(2),
    },
    topRevisionStyle: {
        marginLeft: theme.spacing(1),
        maxWidth: 500,
    },
    readOnlyStyle: {
        color: 'red',
    },
    active: {
        background: theme.custom.revision.activeRevision.background,
        width: 8,
        height: 8,
        borderRadius: '50%',
        alignItems: 'center',
    },
});

const APIDetailsTopMenu = (props) => {
    const {
        classes, theme, api, isAPIProduct, imageUpdate, intl, openPageSearch, setOpenPageSearch,
    } = props;
    const history = useHistory();
    const prevLocation = history.location.pathname;
    const lastIndex = prevLocation.split('/')[3];
    const [revisionId, setRevisionId] = useState(api.id);
    const isVisibleInStore = ['PROTOTYPED', 'PUBLISHED'].includes(api.lifeCycleStatus);
    /**
         * The component for advanced endpoint configurations.
         * @param {string} name The name of the
         * @param {string} version Version of the API
         * @param {string} provider Provider of the API
         * @param {string} format Weather to recive files in YALM of JSON format
         * @returns {zip} Zpi file containing the API directory.
     */
    function exportAPI() {
        return api.export().then((zipFile) => {
            return Utils.forceDownload(zipFile);
        }).catch((error) => {
            console.error(error);
            if (error.response) {
                Alert.error(error.response.body.description);
            } else {
                Alert.error(intl.formatMessage({
                    id: 'Apis.Details.components.APIDetailsTopMenu.error',
                    defaultMessage: 'Something went wrong while downloading the API.',
                }));
            }
        });
    }

    const handleChange = (event) => {
        setRevisionId(event.target.value);
    };

    /**
     * Update the state when new props are available
     */
    useEffect(() => {
        setRevisionId(api.id);
    }, [api.id]);

    const isDownloadable = [API.CONSTS.API, API.CONSTS.APIProduct].includes(api.apiType);
    const { settings, user } = useAppContext();
    const { allRevisions, allEnvRevision } = useRevisionContext();
    const { tenantList } = useContext(ApiContext);
    const userNameSplit = user.name.split('@');
    const tenantDomain = userNameSplit[userNameSplit.length - 1];
    let devportalUrl = `${settings.devportalUrl}/apis/${api.id}/overview`;
    if (tenantList && tenantList.length > 0) {
        devportalUrl = `${settings.devportalUrl}/apis/${api.id}/overview?tenant=${tenantDomain}`;
    }

    function getDeployments(revisionKey) {
        const array = [];
        allEnvRevision.filter(
            (env) => env.id === revisionKey,
        )[0].deploymentInfo.map((environment) => array.push(environment.name));
        return array.join(', ');
    }

    // todo: need to support rev proxy ~tmkb
    return (
        <div className={classes.root}>
            <Link
                to={isAPIProduct
                    ? `/api-products/${api.id}/overview`
                    : `/apis/${api.id}/overview`}
                className={classes.backLink}
            >
                <Box width={70} height={50} marginLeft={1}>
                    <ThumbnailView api={api} width={70} height={50} imageUpdate={imageUpdate} />
                </Box>
                <div style={{ marginLeft: theme.spacing(1), maxWidth: 500 }}>
                    <Typography id='itest-api-name-version' variant='h4' className={classes.apiName}>
                        {api.name}
                        {' '}
                        {isAPIProduct ? '' : ':' + api.version}
                    </Typography>
                    <Typography variant='caption' gutterBottom align='left'>
                        <FormattedMessage
                            id='Apis.Details.components.APIDetailsTopMenu.created.by'
                            defaultMessage='Created by:'
                        />
                        {' '}
                        {api.provider}
                    </Typography>
                </div>
            </Link>
            <VerticalDivider height={70} />
            <div className={classes.infoItem}>
                <Typography variant='subtitle1'>{isAPIProduct ? api.state : api.lifeCycleStatus}</Typography>
                <Typography variant='caption' align='left'>
                    <FormattedMessage
                        id='Apis.Details.components.APIDetailsTopMenu.state'
                        defaultMessage='State'
                    />
                </Typography>
            </div>

            <div className={classes.dateWrapper} />
            {api.isRevision && (
                <MUIAlert
                    variant='outlined'
                    severity='warning'
                    icon={false}
                >
                    <FormattedMessage
                        id='Apis.Details.components.APIDetailsTopMenu.read.only.label'
                        defaultMessage='Read only'
                    />
                </MUIAlert>
            )}
            <div className={classes.topRevisionStyle}>
                <TextField
                    id='revision-selector'
                    value={revisionId}
                    select
                    SelectProps={{
                        MenuProps: {
                            anchorOrigin: {
                                vertical: 'bottom',
                                horizontal: 'left',
                            },
                            getContentAnchorEl: null,
                        },
                    }}
                    name='selectRevision'
                    onChange={handleChange}
                    margin='dense'
                    variant='outlined'
                >
                    {!isAPIProduct ? (
                        <MenuItem
                            value={api.isRevision ? api.revisionedApiId : api.id}
                            component={Link}
                            to={'/apis/' + (api.isRevision ? api.revisionedApiId : api.id) + '/' + lastIndex}
                        >
                            <FormattedMessage
                                id='Apis.Details.components.APIDetailsTopMenu.current.api'
                                defaultMessage='Current API'
                            />
                        </MenuItem>
                    ) : (
                        <MenuItem
                            value={api.isRevision ? api.revisionedApiProductId : api.id}
                            component={Link}
                            to={'/api-products/' + (api.isRevision
                                ? api.revisionedApiProductId : api.id) + '/' + lastIndex}
                        >
                            <FormattedMessage
                                id='Apis.Details.components.APIDetailsTopMenu.current.api'
                                defaultMessage='Current API'
                            />
                        </MenuItem>
                    )}
                    {allRevisions && !isAPIProduct && allRevisions.map((item) => (
                        <MenuItem value={item.id} component={Link} to={'/apis/' + item.id + '/' + lastIndex}>
                            <Grid
                                container
                                direction='row'
                                alignItems='center'
                            >
                                <Grid item>
                                    {item.displayName}
                                </Grid>
                                {allEnvRevision && allEnvRevision.find((env) => env.id === item.id) && (
                                    <Grid item>
                                        <Box ml={2}>
                                            <Tooltip
                                                title={getDeployments(item.id)}
                                                placement='bottom'
                                            >
                                                <Grid className={classes.active} />
                                            </Tooltip>
                                        </Box>
                                    </Grid>
                                )}
                            </Grid>
                        </MenuItem>
                    ))}
                    {allRevisions && isAPIProduct && allRevisions.map((item) => (
                        <MenuItem value={item.id} component={Link} to={'/api-products/' + item.id + '/' + lastIndex}>
                            <Grid
                                container
                                direction='row'
                                alignItems='center'
                            >
                                <Grid item>
                                    {item.displayName}
                                </Grid>
                                {allEnvRevision && allEnvRevision.find((env) => env.id === item.id) && (
                                    <Grid item>
                                        <Box ml={2}>
                                            <Tooltip
                                                title={getDeployments(item.id)}
                                                placement='bottom'
                                            >
                                                <Grid className={classes.active} />
                                            </Tooltip>
                                        </Box>
                                    </Grid>
                                )}
                            </Grid>
                        </MenuItem>
                    ))}
                </TextField>
            </div>

            <VerticalDivider height={70} />
            <GoTo
                setOpenPageSearch={setOpenPageSearch}
                openPageSearch={openPageSearch}
                api={api}
                isAPIProduct={isAPIProduct}
            />
            {(isVisibleInStore || isAPIProduct) && <VerticalDivider height={70} />}
            {(isVisibleInStore || isAPIProduct) && (
                <a
                    target='_blank'
                    rel='noopener noreferrer'
                    href={devportalUrl}
                    className={classes.viewInStoreLauncher}
                    style={{ minWidth: 90 }}
                >
                    <div>
                        <LaunchIcon />
                    </div>
                    <Typography variant='caption'>
                        <FormattedMessage
                            id='Apis.Details.components.APIDetailsTopMenu.view.in.portal'
                            defaultMessage='View in Dev Portal'
                        />
                    </Typography>
                </a>
            )}
            {/* Page error banner */}
            {/* end of Page error banner */}
            {isAPIProduct || api.isRevision
                ? null : <CreateNewVersionButton buttonClass={classes.viewInStoreLauncher} api={api} />}
            {(isDownloadable) && <VerticalDivider height={70} />}
            <div className={classes.downloadApi}>
                {(isDownloadable) && (
                    <a
                        onClick={exportAPI}
                        onKeyDown={null}
                        className={classes.downloadApiFlex}
                    >
                        <div>
                            <CloudDownloadRounded />
                        </div>
                        <Typography variant='caption' align='left'>
                            <FormattedMessage
                                id='Apis.Details.APIDetailsTopMenu.download.api'
                                defaultMessage='Download API'
                            />
                        </Typography>
                    </a>
                )}
            </div>
            {api.isRevision || isRestricted(['apim:api_create'], api)
                ? (<div className={classes.revisionWrapper} />)
                : (<DeleteApiButton buttonClass={classes.viewInStoreLauncher} api={api} isAPIProduct={isAPIProduct} />)}
        </div>
    );
};

APIDetailsTopMenu.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({}).isRequired,
    isAPIProduct: PropTypes.bool.isRequired,
    imageUpdate: PropTypes.number.isRequired,
};

// export default withStyles(styles, { withTheme: true })(APIDetailsTopMenu);

export default injectIntl(withStyles(styles, { withTheme: true })(APIDetailsTopMenu));
