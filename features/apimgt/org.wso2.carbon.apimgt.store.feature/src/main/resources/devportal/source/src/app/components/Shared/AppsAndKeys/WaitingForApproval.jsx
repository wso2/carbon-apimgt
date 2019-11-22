import React from 'react';
import { withStyles } from '@material-ui/core/styles';
import { injectIntl } from 'react-intl';

const styles = theme => ({
    root: {
        padding: theme.spacing(3),
    },
});

const waitingForApproval = (props) => {
    const {
        classes, keyState, states, intl,
    } = props;
    let message = intl.formatMessage({
        defaultMessage: 'A request to register this application has been sent and is pending approval.',
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

export default injectIntl(withStyles(styles)(waitingForApproval));
