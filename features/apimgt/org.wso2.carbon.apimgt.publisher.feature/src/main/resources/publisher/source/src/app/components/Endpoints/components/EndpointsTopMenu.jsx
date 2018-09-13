import React from 'react';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import Grid from '@material-ui/core/Grid';
import { withStyles } from '@material-ui/core/styles';
import { FormattedMessage } from 'react-intl';
import { Link } from 'react-router-dom';

const styles = theme => ({
    button: {
        margin: theme.spacing.unit,
    },
});

const EndpointsTopMenu = () => {
    return (
        <Grid container direction='row' justify='space-between' alignItems='center'>
            <Grid item>
                <Grid container spacing={16}>
                    <Grid item>
                        <Typography variant='display1' gutterBottom>
                            <FormattedMessage id='global.endpoints' />
                        </Typography>
                    </Grid>

                    <Grid item>
                        <Link to='/endpoints/create'>
                            <Button size='medium' color='secondary' variant='contained'>
                                <FormattedMessage id='create.an.endpoint' />
                            </Button>
                        </Link>
                    </Grid>
                </Grid>
            </Grid>
        </Grid>
    );
};

EndpointsTopMenu.propTypes = {};

export default withStyles(styles)(EndpointsTopMenu);
