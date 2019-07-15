import React from 'react';
import { withStyles } from '@material-ui/core/styles';

const styles = theme => ({
    root: {
        padding: theme.spacing.unit * 3,
    },
});

const waitingForApproval = (props) => {
    const {
        classes, keyState, states, intl,
    } = props;
    let message = intl.formatMessage({
        defaultMessage: 'A request to register this application has been sent.',
        id: 'Shared.AppsAndKeys.WaitingForApproval.msg.ok',
    });
    if (keyState === states.REJECTED) {
        message = intl.formatMessage({
            defaultMessage: 'This application has been rejected from generating keys',
            id: 'Shared.AppsAndKeys.WaitingForApproval.msg.reject',
        });
    }
    return <div className={classes.root}>{message}</div>;
};

export default withStyles(styles)(waitingForApproval);
