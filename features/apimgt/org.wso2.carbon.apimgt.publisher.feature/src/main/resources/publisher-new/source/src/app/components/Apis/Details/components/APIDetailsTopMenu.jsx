import React from 'react';
import PropTypes from 'prop-types';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage } from 'react-intl';
import LaunchIcon from '@material-ui/icons/Launch';
import KeyboardArrowLeft from '@material-ui/icons/KeyboardArrowLeft';
import { withStyles } from '@material-ui/core/styles';
import { Link } from 'react-router-dom';
import ThumbnailView from 'AppComponents/Apis/Listing/components/ImageGenerator/ThumbnailView';
import VerticalDivider from 'AppComponents/Shared/VerticalDivider';
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
});

const DetailsTopMenu = ({
    classes, theme, api, isAPIProduct,
}) => {
    // todo: need to support rev proxy ~tmkb
    return (
        <div className={classes.root}>
            <Link to={isAPIProduct ? '/api-products' : '/apis'} className={classes.backLink}>
                <KeyboardArrowLeft className={classes.backIcon} />
                <div className={classes.backText}>
                    <FormattedMessage
                        id='Apis.Details.components.APIDetailsTopMenu.back.to.listing'
                        defaultMessage='BACK TO {break} LISTING'
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
                <Typography variant='subtitle1' gutterBottom>
                    {isAPIProduct ? api.state : api.lifeCycleStatus}
                </Typography>
                <Typography variant='caption' gutterBottom align='left'>
                            State
                </Typography>
            </div>

            <VerticalDivider height={70} />

            <a
                target='_blank'
                rel='noopener noreferrer'
                href={`${window.location.origin}/store-new/apis/${api.id}/overview`}
                className={classes.viewInStoreLauncher}
            >
                <div>
                    <LaunchIcon />
                </div>
                <div className={classes.linkText}>View In store</div>
            </a>
            <VerticalDivider height={70} />
            {isAPIProduct ? null : (
                <CreateNewVersionButton buttonClass={classes.viewInStoreLauncher} api={api} />
            )}
            <DeleteApiButton buttonClass={classes.viewInStoreLauncher} api={api} isAPIProduct={isAPIProduct} />
        </div>
    );
};

DetailsTopMenu.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({}).isRequired,
    isAPIProduct: PropTypes.bool.isRequired,
};

export default withStyles(styles, { withTheme: true })(DetailsTopMenu);
