import React from 'react';
import Grid from '@material-ui/core/Grid';
import Alert from 'AppComponents/Shared/MuiAlert';
import { FormattedMessage } from 'react-intl';
import CircularProgress from '@material-ui/core/CircularProgress';

/**
 *
 *
 * @export
 * @param {*} props
 * @return {*}
 */
export default function TaskState(props) {
    const {
        completed, errors, inProgress, children, completedMessage, inProgressMessage,
    } = props;
    let severity;
    let message = children;
    if (completed) {
        severity = 'success';
        if (completedMessage) {
            message = completedMessage;
        }
    } else if (inProgress) {
        severity = 'info';
        if (inProgressMessage) {
            message = inProgressMessage;
        }
    } else {
        severity = 'waiting';
    }
    if (errors) {
        severity = 'error';
        if (errors.response) {
            const { body } = errors.response;
            message = (
                <>
                    <b>
                        [
                        {body.code}
                        ]
                    </b>
                    {' '}
                    :
                    {body.description}
                </>
            );
        } else {
            message = (
                <>
                    <FormattedMessage
                        id='Apis.Listing.TaskState.generic.error.prefix'
                        defaultMessage='Error while'
                    />
                    {' '}
                    {inProgressMessage}
                </>
            );
        }
    }

    return (
        <>
            <Grid item md={2} lg={3} xs={3} />
            <Grid item md={9} lg={8} xs={9}>
                <Alert
                    icon={inProgress ? <CircularProgress size={20} thickness={2} /> : null}
                    variant={errors ? 'standard' : 'plain'}
                    severity={severity}
                >
                    {message}
                </Alert>
            </Grid>
            <Grid item md={1} lg={1} xs={0} />
        </>
    );
}
