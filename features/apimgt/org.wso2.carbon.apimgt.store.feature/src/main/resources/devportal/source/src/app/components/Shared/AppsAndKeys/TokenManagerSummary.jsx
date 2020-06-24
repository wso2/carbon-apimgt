/* eslint-disable react/prop-types */
import React from 'react';
import { makeStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage, injectIntl } from 'react-intl';
import { app } from 'Settings';
import WaitingForApproval from './WaitingForApproval';
import ViewKeys from './ViewKeys';

const useStyles = makeStyles(theme => ({
    root: {
        padding: theme.spacing(3, 2),
    },
    noKeysRoot: {
        backgroundImage: `url(${app.context + theme.custom.overviewPage.keysBackground})`,
        height: '100%',
        backgroundPosition: 'center',
        backgroundRepeat: 'no-repeat',
        backgroundSize: 'cover',
        minHeight: 192,
        display: 'flex',
        alignItems: 'center',
    },
}));

/**
 * Render a compressed view of the key gneration view.
 * @param {JSON} props Input params.
 * @returns {JSX} Rendered output.
 */
export default function TokenManagerSummary(props) {
    const classes = useStyles();
    const { keys, keyStates, key, selectedApp, keyType, isKeyJWT, isUserOwner, selectedTab } = props;
    if (keys.size > 0 && key && key.keyState === 'APPROVED' && !key.consumerKey) {
        return (
            <div className={classes.emptyBox}>
                <Typography variant="h5" component="h3">
                    Error
                </Typography>
                <Typography variant='body2'>
                    <FormattedMessage
                        id='Shared.AppsAndKeys.TokenManagerSummary'
                        defaultMessage='Error! You have partially-created keys. Use `Clean Up` option.'
                    />
                </Typography>
            </div>
        );
    }
    if (key && (key.keyState === keyStates.CREATED || key.keyState === keyStates.REJECTED)) {
        return (
            <div className={classes.emptyBox}>
                <Typography variant='body2'>
                    <WaitingForApproval keyState={key.keyState} states={keyStates} />
                </Typography>
            </div>
        );
    }
    const keyGrantTypes = key ? key.supportedGrantTypes : [];

    return (
        <ViewKeys
            selectedApp={selectedApp}
            selectedTab={selectedTab}
            keyType={keyType}
            keys={keys}
            isKeyJWT={isKeyJWT}
            selectedGrantTypes={keyGrantTypes}
            isUserOwner={isUserOwner}
            summary
        />
    );
}
