import React, { useContext } from 'react';
import PropTypes from 'prop-types';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage } from 'react-intl';
import LaunchIcon from '@material-ui/icons/Launch';
import KeyboardArrowLeft from '@material-ui/icons/KeyboardArrowLeft';
import { withStyles } from '@material-ui/core/styles';
import { Link } from 'react-router-dom';
import ApiContext from 'AppComponents/Apis/Details/components/ApiContext';
import { useAppContext } from 'AppComponents/Shared/AppContext';
import ThumbnailView from 'AppComponents/Apis/Listing/components/ImageGenerator/ThumbnailView';
import VerticalDivider from 'AppComponents/Shared/VerticalDivider';
import GoTo from 'AppComponents/Apis/Details/GoTo/GoTo';
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
});

const APIDetailsTopMenu = (props) => {
    const {
        classes, theme, api, isAPIProduct, imageUpdate,
    } = props;
    const isVisibleInStore = ['PROTOTYPED', 'PUBLISHED'].includes(api.lifeCycleStatus);
    const { settings, user } = useAppContext();
    let apiType = 'API';
    if (isAPIProduct) {
        apiType = 'PRODUCT';
    }
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
            <Link to={isAPIProduct ? '/api-products' : '/apis'} className={classes.backLink}>
                <KeyboardArrowLeft className={classes.backIcon} />
                <div className={classes.backText}>
                    <FormattedMessage
                        id='Apis.Details.components.APIDetailsTopMenu.back.to.listing'
                        defaultMessage='BACK TO {break} {apiType}s'
                        values={{ break: <br />, apiType }}
                    />
                </div>
            </Link>
            <VerticalDivider height={70} />
            <ThumbnailView api={api} width={70} height={50} imageUpdate={imageUpdate} />
            <div style={{ marginLeft: theme.spacing(1), maxWidth: 500 }}>
                <Typography variant='h4' className={classes.apiName}>
                    {api.name}
                    {' '}
                    {isAPIProduct ? '' : ':' + api.version}
                </Typography>
                <Typography variant='caption' gutterBottom align='left'>
                    Created by:
                    {' '}
                    {api.provider}
                </Typography>
            </div>
            <VerticalDivider height={70} />
            <div className={classes.infoItem}>
                <Typography variant='subtitle1'>{isAPIProduct ? api.state : api.lifeCycleStatus}</Typography>
                <Typography variant='caption' align='left'>
                    State
                </Typography>
            </div>
            <div className={classes.dateWrapper} />
            <VerticalDivider height={70} />
            <GoTo api={api} isAPIProduct={isAPIProduct} />
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
            {isAPIProduct ? null : <CreateNewVersionButton buttonClass={classes.viewInStoreLauncher} api={api} />}
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

export default withStyles(styles, { withTheme: true })(APIDetailsTopMenu);
