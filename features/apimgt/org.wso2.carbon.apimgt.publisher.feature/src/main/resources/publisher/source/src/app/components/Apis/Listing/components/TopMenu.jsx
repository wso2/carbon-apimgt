import React from 'react';
import PropTypes from 'prop-types';
import Typography from '@material-ui/core/Typography';
import IconButton from '@material-ui/core/IconButton';
import Grid from '@material-ui/core/Grid';
import List from '@material-ui/icons/List';
import GridIcon from '@material-ui/icons/GridOn';
import { withStyles } from '@material-ui/core/styles';
import { FormattedMessage } from 'react-intl';

import APICreateMenu from '../components/APICreateMenu';

const styles = theme => ({
    button: {
        margin: theme.spacing.unit,
    },
});

const TopMenu = ({ classes, isCardView, toggleView }) => {
    return (
        <Grid container direction='row' justify='space-between' alignItems='center'>
            <Grid item>
                <Grid container spacing={16}>
                    <Grid item>
                        <Typography variant='display1' gutterBottom>
                            APIs
                        </Typography>
                    </Grid>

                    <Grid item>
                        <APICreateMenu buttonProps={{ size: 'medium', color: 'secondary', variant: 'contained' }}>
                            <FormattedMessage id='create.an.api' defaultMessage='Create API' />
                        </APICreateMenu>
                    </Grid>
                </Grid>
            </Grid>
            <Grid item>
                <IconButton className={classes.button} disabled={!isCardView} aria-label='List' onClick={toggleView}>
                    <List />
                </IconButton>
                <IconButton className={classes.button} disabled={isCardView} aria-label='Grid' onClick={toggleView}>
                    <GridIcon />
                </IconButton>
            </Grid>
        </Grid>
    );
};

TopMenu.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    toggleView: PropTypes.func.isRequired,
    isCardView: PropTypes.bool.isRequired,
};

export default withStyles(styles)(TopMenu);
