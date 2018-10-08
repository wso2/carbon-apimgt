import React from 'react';
import PropTypes from 'prop-types';
import Typography from '@material-ui/core/Typography';
import IconButton from '@material-ui/core/IconButton';
import Grid from '@material-ui/core/Grid';
import List from '@material-ui/icons/List';
import { withStyles } from '@material-ui/core/styles';
import { FormattedMessage } from 'react-intl';

import DocCreateMenu from './DocCreateMenu';

const styles = theme => ({
    button: {
        margin: theme.spacing.unit,
    },
});

const DocMenu = ({ classes }) => {
    return (
        <Grid container direction='row' justify='space-between' alignItems='center'>
            <Grid item>
                <Grid container spacing={16}>
                    <Grid item>
                        <Typography variant='display1' align='left' className={classes.mainTitle}>
                            <FormattedMessage
                                id='documents'
                                defaultMessage='Documents'
                            />
                        </Typography>
                    </Grid>
                    <Grid item>
                        <DocCreateMenu buttonProps={{ size: 'medium', color: 'secondary', variant: 'contained' }}>
                            <FormattedMessage id='create.btn' defaultMessage='Create' />
                        </DocCreateMenu>
                    </Grid>
                </Grid>
            </Grid>
        </Grid>
    );
};

DocMenu.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    toggleView: PropTypes.func.isRequired,
};

export default withStyles(styles)(DocMenu);
