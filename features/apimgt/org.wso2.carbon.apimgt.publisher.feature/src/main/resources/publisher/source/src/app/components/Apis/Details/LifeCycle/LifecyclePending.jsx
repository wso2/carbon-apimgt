import React from 'react';
import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage, useIntl } from 'react-intl';
import Box from '@material-ui/core/Box';
import Button from '@material-ui/core/Button';
import PropTypes from 'prop-types';
import { useAPI } from 'AppComponents/Apis/Details/components/ApiContext';
import Alert from 'AppComponents/Shared/Alert';

const lifecyclePending = (props) => {
    const { currentState } = props;
    const intl = useIntl();
    const [api, updateAPI] = useAPI();
    const deleteTask = () => {
        const { id } = api;
        api.cleanupPendingTask(id)
            .then(() => {
                Alert.info(intl.formatMessage({
                    id: 'Apis.Details.LifeCycle.LifeCycleUpdate.LifecyclePending.success',
                    defaultMessage: 'Lifecycle task deleted successfully',
                }));
                updateAPI();
            })
            .catch((errorResponse) => {
                console.error(errorResponse);
                Alert.error(intl.formatMessage({
                    id: 'Apis.Details.LifeCycle.LifeCycleUpdate.LifecyclePending.error',
                    defaultMessage: 'Error while deleting task',
                }));
            });
    };
    return (
        <Paper>
            <Box display='block' p={2} mt={2}>
                <Box display='block'>
                    <Typography variant='h6'>
                        <FormattedMessage
                            id='Apis.Details.LifeCycle.LifeCycleUpdate.LifecyclePending.pending'
                            defaultMessage='Pending lifecycle state change.'
                        />
                    </Typography>
                </Box>
                <Box display='block' mt={0.5}>
                    <Typography variant='subtitle2'>
                        <FormattedMessage
                            id='Apis.Details.LifeCycle.LifeCycleUpdate.LifecyclePending.adjective'
                            defaultMessage='Current state is'
                        />
                        {' '}
                        {currentState}
                    </Typography>
                </Box>
                <Box display='flex' mt={2}>
                    <Button
                        size='small'
                        variant='outlined'
                        color='primary'
                        onClick={deleteTask}
                    >
                        <FormattedMessage
                            id='Apis.Details.LifeCycle.LifeCycleUpdate.LifecyclePending.delete.task'
                            defaultMessage='Delete Task'
                        />
                    </Button>

                </Box>
            </Box>
        </Paper>
    );
};
lifecyclePending.propTypes = {
    currentState: PropTypes.string.isRequired,
};
export default lifecyclePending;
