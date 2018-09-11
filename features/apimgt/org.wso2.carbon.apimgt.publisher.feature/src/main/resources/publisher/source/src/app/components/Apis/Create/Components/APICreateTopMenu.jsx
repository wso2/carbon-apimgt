import React from 'react';
import PropTypes from 'prop-types';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import BackIcon from '@material-ui/icons/KeyboardBackspace';

import Grid from '@material-ui/core/Grid';
import { withStyles } from '@material-ui/core/styles';
import { Link } from 'react-router-dom';
// import { FormattedMessage } from 'react-intl';

const styles = theme => ({
    root: {
        flexGrow: 1,
    },
    apiMetaInfo: {
        borderRight: '2px solid',
        padding: theme.spacing.unit,
    },
    viewInStoreLauncher: {
        padding: '0px',
    },
});

const APICreateTopMenu = ({ classes }) => {
    return (
        <Grid container direction='row' justify='flex-start' alignItems='center' className={classes.root}>
            <Grid item>
                <Link to='/'>
                    <Button aria-label='Back'>
                        <BackIcon />
                    </Button>
                </Link>
            </Grid>
            <Grid item lg={8}>
                <Typography variant='display1'>Create New API</Typography>
            </Grid>
        </Grid>
    );
};

APICreateTopMenu.propTypes = {
    classes: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(APICreateTopMenu);
