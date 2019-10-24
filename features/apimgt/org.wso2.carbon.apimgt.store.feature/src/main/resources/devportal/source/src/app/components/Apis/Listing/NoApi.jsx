import React from 'react';
import { makeStyles, useTheme } from '@material-ui/core/styles';
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage } from 'react-intl';
import { app } from 'Settings';

const useStyles = makeStyles(theme => ({
    root: {
        flexGrow: 1,
    },
    messageWrapper: {
        marginTop: theme.spacing(4),
        padding: theme.spacing(2),
        textAlign: 'center',
        color: theme.palette.text.secondary,
    },
}));

export default function NoApi() {
    const classes = useStyles();
    const theme = useTheme();

    return (
        <div className={classes.root}>
            <Grid container spacing={3}>
                <Grid item xs={12} className={classes.messageWrapper}>
                    <img src={app.context + theme.custom.noApiImage} className={classes.messageWrapper} />
                    <Typography variant='h5' gutterBottom>
                        <FormattedMessage id='Apis.Listing.NoApi.nodata.title' defaultMessage='No APIs Available' />
                    </Typography>
                    <Typography variant='subtitle1' gutterBottom>
                        <FormattedMessage
                            id='Apis.Listing.NoApi.nodata.content'
                            defaultMessage='There are no APIs to display right now.'
                        />
                    </Typography>
                </Grid>
            </Grid>
        </div>
    );
}
