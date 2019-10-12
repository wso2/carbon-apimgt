import React from 'react';
import PropTypes from 'prop-types';
import Typography from '@material-ui/core/Typography';
import Tooltip from '@material-ui/core/Tooltip';
import { FormattedMessage } from 'react-intl';
import LaunchIcon from '@material-ui/icons/Launch';
import KeyboardArrowLeft from '@material-ui/icons/KeyboardArrowLeft';
import { withStyles } from '@material-ui/core/styles';
import { Link } from 'react-router-dom';
import moment from 'moment';

import { useAppContext } from 'AppComponents/Shared/AppContext';
import ThumbnailView from 'AppComponents/Apis/Listing/components/ImageGenerator/ThumbnailView';
import VerticalDivider from 'AppComponents/Shared/VerticalDivider';
import GoTo from 'AppComponents/Apis/Details/GoTo/GoTo';
import DeleteApiButton from './DeleteApiButton';
import CreateNewVersionButton from './CreateNewVersionButton';

const styles = theme => ({
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
        alignSelf: 'flex-start',
    },
    lastUpdatedTypography: {
        width: '25%',
        alignSelf: 'flex-end',
        'margin-top': '30px',
    },
});

const APIDetailsTopMenu = (props) => {
    const {
        classes, theme, api, isAPIProduct,
    } = props;
    const isVisibleInStore = ['PROTOTYPED', 'PUBLISHED'].includes(api.lifeCycleStatus);
    const { settings } = useAppContext();
    // todo: need to support rev proxy ~tmkb
    return (
        <div className={classes.root}>
            <Link to={isAPIProduct ? '/api-products' : '/apis'} className={classes.backLink}>
                <KeyboardArrowLeft className={classes.backIcon} />
                <div className={classes.backText}>
                    <FormattedMessage
                        id='Apis.Details.components.APIDetailsTopMenu.back.to.listing'
                        defaultMessage='BACK TO {break} APIs'
                        values={{ break: <br /> }}
                    />
                </div>
            </Link>
            <VerticalDivider height={70} />
            <ThumbnailView api={api} width={70} height={50} />
            <div style={{ marginLeft: theme.spacing.unit }}>
                <Typography variant='h4'>
                    {api.name} {isAPIProduct ? '' : ':' + api.version}
                </Typography>
                <Typography variant='caption' gutterBottom align='left'>
                    Created by: {api.provider}
                </Typography>
            </div>
            <VerticalDivider height={70} />
            <div className={classes.infoItem}>
                <Typography variant='subtitle1'>{isAPIProduct ? api.state : api.lifeCycleStatus}</Typography>
                <Typography variant='caption' align='left'>
                    State
                </Typography>
            </div>
            <VerticalDivider height={70} />
            <div className={classes.dateWrapper}>
                <Tooltip
                    title={moment(api.lastUpdatedTime).calendar()}
                    aria-label='add'
                    className={classes.lastUpdatedTooltip}
                >
                    <Typography variant='caption' display='block' className={classes.lastUpdatedTypography}>
                        <FormattedMessage
                            id='Apis.Details.components.APIDetailsTopMenu.last.updated.time'
                            defaultMessage='Last updated:'
                        />{' '}
                        {moment(api.lastUpdatedTime).fromNow()}
                    </Typography>
                </Tooltip>
            </div>
            <VerticalDivider height={70} />
            <GoTo api={api} isAPIProduct={isAPIProduct} />
            {(isVisibleInStore || isAPIProduct) && <VerticalDivider height={70} />}
            {(isVisibleInStore || isAPIProduct) && (
                <a
                    target='_blank'
                    rel='noopener noreferrer'
                    href={`${settings.storeUrl}/apis/${api.id}/overview`}
                    className={classes.viewInStoreLauncher}
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
};

export default withStyles(styles, { withTheme: true })(APIDetailsTopMenu);
