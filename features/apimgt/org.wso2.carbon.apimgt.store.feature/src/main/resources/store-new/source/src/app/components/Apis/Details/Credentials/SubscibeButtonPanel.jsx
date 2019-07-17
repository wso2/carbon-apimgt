import React from 'react';
import { withStyles } from '@material-ui/core/styles';
import { Typography } from '@material-ui/core';
import Button from '@material-ui/core/Button';
import { ScopeValidation, resourceMethods, resourcePaths } from 'AppComponents/Shared/ScopeValidation';
import PropTypes from 'prop-types';

const styles = theme => ({
    subscribeButtons: {
        display: 'flex',
        paddingTop: theme.spacing.unit * 2,
    },
    buttonElm: {
        height: 28,
        marginLeft: 20,
    },
    buttonElmText: {
        marginLeft: 20,
        paddingTop: 5,
    },
});
const subscibeButtonPanel = (props) => {
    const {
        classes, avalibleAppsLength, subscribedAppsLength, handleClickToggle,
    } = props;
    return (
        <div className={classes.subscribeButtons}>
            <div>
                <Typography variant='headline'>Subscribed Applications</Typography>
                <Typography variant='caption'>
                        (
                    {' '}
                    {subscribedAppsLength}
                    {' '}
                    {subscribedAppsLength === 1 ? 'subscription' : 'subscriptions'}
                    {' '}
                        )
                </Typography>
            </div>
            <ScopeValidation
                resourcePath={resourcePaths.SUBSCRIPTIONS}
                resourceMethod={resourceMethods.POST}
            >
                {avalibleAppsLength > 0 && (
                    <div>
                        <Button
                            variant='outlined'
                            size='small'
                            color='primary'
                            className={classes.buttonElm}
                            onClick={() => handleClickToggle('openAvailable')}
                        >
                            Subscribe to Available App
                        </Button>
                        <Typography
                            variant='caption'
                            component='p'
                            className={classes.buttonElmText}
                        >
                            {avalibleAppsLength}
                            {' '}
                            Available
                        </Typography>
                    </div>
                )}
                <Button
                    variant='outlined'
                    size='small'
                    color='primary'
                    className={classes.buttonElm}
                    onClick={() => handleClickToggle('openNew')}
                >
                    Subscribe to New App
                </Button>
            </ScopeValidation>
        </div>
    );
};
subscibeButtonPanel.propTypes = {
    classes: PropTypes.shape({
        subscribeButtons: PropTypes.shape({}),
        buttonElm: PropTypes.shape({}),
        buttonElmText: PropTypes.shape({}),
    }).isRequired,
    handleClickToggle: PropTypes.func.isRequired,
    avalibleAppsLength: PropTypes.number.isRequired,
    subscribedAppsLength: PropTypes.number.isRequired,
};
export default withStyles(styles, { withTheme: true })(subscibeButtonPanel);
