import React from 'react';
import PropTypes from 'prop-types';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import LaunchIcon from '@material-ui/icons/Launch';
import EditIcon from '@material-ui/icons/Edit';
import BackIcon from '@material-ui/icons/KeyboardBackspace';
import DeleteIcon from '@material-ui/icons/Delete';
import Grid from '@material-ui/core/Grid';
import { withStyles } from '@material-ui/core/styles';
import Chip from '@material-ui/core/Chip';
import { Link } from 'react-router-dom';
import { FormattedMessage } from 'react-intl';

import ImageGenerator from '../../Listing/components/ImageGenerator';

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

const DetailsTopMenu = ({ classes, api }) => {
    const storeURL = `${window.location.origin}/store/${api.id}/overview`; // todo: need to support rev proxy ~tmkb
    return (
        <Grid container alignItems='center' className={classes.root}>
            <Grid item >
                <Link to='/'>
                    <Button aria-label='Back'>
                        <BackIcon />
                    </Button>
                </Link>
            </Grid>
            <Grid item>
                <ImageGenerator width={80} height={80} apiName={api.name} />
            </Grid>
            <Grid
                item
                xs={7}
                md={7}
                lg={7}
                className={classes.apiMetaInfo}
                sm
                container
                direction='row'
                justify='space-between'
            >
                <Grid item xs container direction='column' justify='space-between' alignItems='flex-start'>
                    <Grid item>
                        <Typography variant='title'>
                            {api.name} : {api.version}
                        </Typography>
                    </Grid>
                    <Grid item>
                        <Typography variant='subheading'>Created by: {api.provider}</Typography>
                    </Grid>
                </Grid>

                <Grid item xs container direction='column' justify='space-between' alignItems='flex-end'>
                    <Grid item>
                        <Button
                            component='a'
                            target='_blank'
                            href={storeURL}
                            size='small'
                            className={classes.viewInStoreLauncher}
                        >
                            <LaunchIcon />
                            View In store
                        </Button>
                    </Grid>
                    <Grid item>
                        State: <Chip label={api.lifeCycleStatus} color='primary' variant='outlined' />
                    </Grid>
                </Grid>
            </Grid>
            <Grid
                item
                style={{ padding: '8px' }}
                sm
                container
                direction='row'
                alignItems='center'
                justify='space-between'
            >
                <Grid item>
                    <Button size='small' className={classes.viewInStoreLauncher}>
                        <EditIcon />
                        Edit
                    </Button>
                    <Button size='small' className={classes.viewInStoreLauncher}>
                        <DeleteIcon color='secondary' />
                        Delete
                    </Button>
                </Grid>
            </Grid>
        </Grid>
    );
};

DetailsTopMenu.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(DetailsTopMenu);
