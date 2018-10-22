import React from 'react';
import PropTypes from 'prop-types';
import Typography from '@material-ui/core/Typography';
import KeyboardArrowLeft from '@material-ui/icons/KeyboardArrowLeft';
import { withStyles } from '@material-ui/core/styles';
import { Link } from 'react-router-dom';
import { FormattedMessage } from 'react-intl';
import VerticalDivider from 'AppComponents/Shared/VerticalDivider';
import CustomIcon from 'AppComponents/Shared/CustomIcon';

// import { FormattedMessage } from 'react-intl';

const styles = theme => ({
    rightIcon: {
        marginLeft: theme.spacing.unit,
    },
    button: {
        margin: theme.spacing.unit,
        marginBottom: 0,
    },
    buttonRight: {
        alignSelf: 'flex-end',
        display: 'flex',
    },
    ListingWrapper: {
        paddingTop: 10,
        paddingLeft: 35,
    },
    root: {
        height: 70,
        background: theme.palette.background.paper,
        borderBottom: 'solid 1px ' + theme.palette.grey.A200,
        display: 'flex',
        alignItems: 'center',
    },
    mainTitle: {
        paddingTop: 10,
    },
    mainTitleWrapper: {},
    APICreateMenu: {
        flexGrow: 1,
        display: 'flex',
        alignItems: 'center',
    },
    content: {
        flexGrow: 1,
    },
    backText: {
        color: theme.palette.primary.main,
        cursor: 'pointer',
        fontFamily: theme.typography.fontFamily,
    },
    backLink: {
        alignItems: 'center',
        textDecoration: 'none',
        display: 'flex',
        paddingLeft: theme.spacing.unit * 3,
    },
    backIcon: {
        color: theme.palette.primary.main,
        fontSize: 56,
        cursor: 'pointer',
    },
    mainIconWrapper: {
        paddingTop: 13,
        paddingLeft: 0,
        paddingRight: 20,
    },
});

const APIDetailsTopMenu = ({ classes, theme }) => {
    const strokeColorMain = theme.palette.getContrastText(theme.palette.background.paper);
    return (
        <main className={classes.content}>
            <div className={classes.root}>
                <Link to='/apis' className={classes.backLink}>
                    <KeyboardArrowLeft className={classes.backIcon} />
                    <div className={classes.backText}>
                        BACK TO <br />
                        LISTING
                    </div>
                </Link>
                <VerticalDivider height={70} />
                <div className={classes.mainIconWrapper}>
                    <CustomIcon strokeColor={strokeColorMain} width={42} height={42} icon='api' />
                </div>
                <div className={classes.mainTitleWrapper}>
                    <Typography variant='display1'>
                        <FormattedMessage id='apis.create.new.api' defaultMessage='APIs - Create New API' />
                    </Typography>
                    <Typography variant='caption' gutterBottom align='left'>
                        <FormattedMessage id='fill.the.mandatory.fields' defaultMessage='Fill the mandatory fields (Name, Version, Context) and create the API. Configure advanced configurations later.' />
                    </Typography>
                </div>
            </div>
        </main>
    );
};

APIDetailsTopMenu.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
};

export default withStyles(styles, { withTheme: true })(APIDetailsTopMenu);
