import React from 'react';
import PropTypes from 'prop-types';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import LaunchIcon from '@material-ui/icons/Launch';
import EditIcon from '@material-ui/icons/Edit';
import KeyboardArrowLeft from '@material-ui/icons/KeyboardArrowLeft';

import Grid from '@material-ui/core/Grid';
import { withStyles } from '@material-ui/core/styles';
import Chip from '@material-ui/core/Chip';
import { Link } from 'react-router-dom';
import VerticalDivider from 'AppComponents/Shared/VerticalDivider';
// import { FormattedMessage } from 'react-intl';

import ImageGenerator from '../../Listing/components/ImageGenerator';
import DeleteApiButton from './DeleteApiButton';



const styles = theme => ({
    root: {
        height: 70,
        background: theme.palette.background.paper,
        borderBottom: "solid 1px " + theme.palette.grey["A200"],
        display: "flex",
        alignItems: "center"
    },
    backLink: {
        alignItems: "center",
        textDecoration: "none",
        display: "flex"
    },
    backIcon: {
      color: theme.palette.primary.main,
      fontSize: 56,
      cursor: "pointer"
    },
    backText: {
      color: theme.palette.primary.main,
      cursor: "pointer",
      fontFamily: theme.typography.fontFamily
    },
    viewInStoreLauncher: {
        display: "flex",
        flexDirection: 'column',
        color: theme.palette.getContrastText(theme.palette.background.paper),
        textAlign: 'center',
    },
    linkText: {
        fontSize: theme.typography.fontSize,
    },
  });
  

const DetailsTopMenu = ({ classes, api, theme }) => {
    const storeURL = `${window.location.origin}/store/${api.id}/overview`; // todo: need to support rev proxy ~tmkb
    return (
        <div className={classes.root}>
          <Link to="/apis" className={classes.backLink}>
            <KeyboardArrowLeft className={classes.backIcon} />
            <div className={classes.backText}>
              BACK TO <br />
              LISTING
            </div>
          </Link>
          <VerticalDivider height={70} />
          <ImageGenerator api={api} width="70" height="50" />
          <div style={{ marginLeft: theme.spacing.unit }}>
            <Typography variant="display1">{api.name} : {api.version}</Typography>
            <Typography variant="caption" gutterBottom align="left">
                Created by: {api.provider}
            </Typography>
          </div>
          <VerticalDivider height={70} />
          <div className={classes.infoItem}>
            <Typography variant="subheading" gutterBottom>
            {api.lifeCycleStatus} 
            </Typography>
            <Typography variant="caption" gutterBottom align="left">
            State
            </Typography>
         </div>
          
        <VerticalDivider height={70} />
        
        <a
            target='_blank'
            href={storeURL}
            className={classes.viewInStoreLauncher}
        >
            <div><LaunchIcon /></div>
            <div className={classes.linkText}>View In store</div>
        </a>
        <VerticalDivider height={70} />
        <DeleteApiButton buttonClass={classes.viewInStoreLauncher} api={api} />
        </div>
    );
};

DetailsTopMenu.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({}).isRequired,
    theme: PropTypes.object.isRequired,
};

export default withStyles(styles, {withTheme: true})(DetailsTopMenu);
