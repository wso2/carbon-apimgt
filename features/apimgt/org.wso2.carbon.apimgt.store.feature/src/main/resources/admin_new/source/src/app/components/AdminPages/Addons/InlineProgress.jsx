import React from 'react';
import { makeStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import CircularProgress from '@material-ui/core/CircularProgress';
import { FormattedMessage } from 'react-intl';

const useStyles = makeStyles((theme) => ({
    root: {
        display: 'flex',
        '& > * + *': {
            marginLeft: theme.spacing(2),
        },
        justifyContent: 'center',
        padding: 20,
    },
}));

export default function InlineProgress(props) {
    const classes = useStyles();
    const { message } = props;
    return (
        <div className={classes.root}>
            <CircularProgress />
            <Typography color="textSecondary" align="center">
                {message || <FormattedMessage
                    id='AdminPages.Addons.InlineProgress.message'
                    defaultMessage='Loading...'
                />}
            </Typography>
        </div>
    );
}
