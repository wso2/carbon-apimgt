import React from 'react';
import { withStyles } from '@material-ui/core/styles';

const styles = theme => ({
    root: {
        padding: theme.spacing.unit * 3,
    },
});

const waitingForApproval = (props) => {
    const { classes, keyState, states } = props;
    let message = 'A request to register this application has been sent.';
    if (keyState === states.REJECTED) {
        message = 'This application has been rejected from generating keys';
    }
    return (
        <div className={classes.root}>{message}</div>
    );
};

export default withStyles(styles)(waitingForApproval);
