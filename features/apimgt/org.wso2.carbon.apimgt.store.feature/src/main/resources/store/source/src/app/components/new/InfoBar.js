import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import {KeyboardArrowLeft, StarRate } from '@material-ui/icons'
import Typography from '@material-ui/core/Typography'

const styles = theme => ({
    root: {
        height: 70,
        background: theme.palette.background.paper,
        borderBottom: 'solid 1px ' + theme.palette.grey['A200'],
        display: 'flex',
    },
    backIcon: {
        color: theme.palette.primary.main,
        fontSize: 56,
        cursor: 'pointer',
    },
    backText: {
        color: theme.palette.primary.main,
        paddingTop: 10,
        cursor: 'pointer',
    },
    apiIcon: {
        height: 45,
        marginTop: 10,
        marginRight: 10,
    },
    starRate: {
        fontSize: 70,
        color: theme.palette.custom.starColor,
    },
    ratingSummery: {
        marginTop: 15,
    },
    rateLink: {
        cursor: 'pointer',
        lineHeight: '70px',
    }
});

class InfoBar extends React.Component {
  state = {
  };

 
  render() {
    const { classes, theme } = this.props;

    return (

        <div className={classes.root}>
            <KeyboardArrowLeft className={classes.backIcon}  />
            <div className={classes.backText}>
                BACK TO <br />
                LISTING
            </div>
            <div className="vertical-divider-70"></div>
            <img src="./img/tmp-icon.png" className={classes.apiIcon} />
            <div>
                <Typography variant="display1" >
                    SwaggerPetstore
                </Typography>
                <Typography variant="caption" gutterBottom align="left">
                    Kasun | 21-May 2018
                </Typography>
            </div>
            <div className="vertical-divider-70"></div>
            <StarRate className={classes.starRate}  />
            <div className={classes.ratingSummery}>
                <Typography>7.1/10</Typography>
                <Typography variant="caption" gutterBottom align="left">
                    121 users
                </Typography>
            </div>
            <div className="vertical-divider-midway"></div>
            <StarRate className={classes.starRate} style={{color:theme.palette.grey['A200']}} />
            <Typography className={classes.rateLink}>Rate this API</Typography>
        </div>
        
    );
  }
}

InfoBar.propTypes = {
  classes: PropTypes.object.isRequired,
  theme: PropTypes.object.isRequired,
};

export default withStyles(styles, { withTheme: true })(InfoBar);
