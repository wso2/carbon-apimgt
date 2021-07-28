import React, { useContext } from 'react';
import PropTypes from 'prop-types';
import Typography from '@material-ui/core/Typography';
import Box from '@material-ui/core/Box';
import Utils from 'AppData/Utils';
import Alert from 'AppComponents/Shared/Alert';
import { FormattedMessage, injectIntl } from 'react-intl';
import LaunchIcon from '@material-ui/icons/Launch';
import CloudDownloadRounded from '@material-ui/icons/CloudDownloadRounded';
import { withStyles } from '@material-ui/core/styles';
import { Link } from 'react-router-dom';
import ApiContext from 'AppComponents/Apis/Details/components/ApiContext';
import { useAppContext } from 'AppComponents/Shared/AppContext';
import ThumbnailView from 'AppComponents/Apis/Listing/components/ImageGenerator/ThumbnailView';
import VerticalDivider from 'AppComponents/Shared/VerticalDivider';
import GoTo from 'AppComponents/Apis/Details/GoTo/GoTo';
import API from 'AppData/api';
import DeleteApiButton from './DeleteApiButton';
import CreateNewVersionButton from './CreateNewVersionButton';


const styles = (theme) => ({
    root: {
        height: 70,
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
});


const APIDetailsTopMenu = (props) => {
    const {
        classes, theme, api, isAPIProduct, imageUpdate, intl,
    } = props;
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
        const restApi = new API();
        return restApi.exportApi(api.id).then((zipFile) => {
            return Utils.forceDownload(zipFile);
        }).catch((error) => {
            if (error.response) {
                Alert.error(error.response.body.description);
            } else {
                Alert.error(intl.formatMessage({
                    id: 'Apis.Details.components.APIDetailsTopMenu.error',
                    defaultMessage: 'Something went wrong while downloading the API.',
                }));
            }
            console.error(error);
        });
    }

    const isDownlodable = ['API'].includes(api.apiType);
    const { settings, user } = useAppContext();
    const { tenantList } = useContext(ApiContext);
    const userNameSplit = user.name.split('@');
    const tenantDomain = userNameSplit[userNameSplit.length - 1];
    let devportalUrl = `${settings.storeUrl}/apis/${api.id}/overview`;
    if (tenantList && tenantList.length > 0) {
        devportalUrl = `${settings.storeUrl}/apis/${api.id}/overview?tenant=${tenantDomain}`;
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
                    <Typography variant='h4' className={classes.apiName}>
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
            <VerticalDivider height={70} />
            <GoTo api={api} isAPIProduct={isAPIProduct} />
            {(isVisibleInStore || isAPIProduct) && api.enableStore && (
                <>
                    <VerticalDivider height={70} />
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
                </>
            )}
            {/* Page error banner */}
            {/* end of Page error banner */}
            {isAPIProduct ? null : <CreateNewVersionButton buttonClass={classes.viewInStoreLauncher} api={api} />}
            {(isDownlodable) && <VerticalDivider height={70} />}
            <div className={classes.downloadApi}>
                {(isDownlodable) && (
                    <a
                        onClick={exportAPI}
                        onKeyDown='null'
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
            <DeleteApiButton buttonClass={classes.viewInStoreLauncher} api={api} isAPIProduct={isAPIProduct} />
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
