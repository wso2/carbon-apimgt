import React from 'react';
import { Link } from 'react-router-dom';
import { withStyles } from '@material-ui/core/styles';
import classNames from 'classnames';
import CustomIcon from 'AppComponents/Shared/CustomIcon';
import { ScopeValidation, resourceMethods, resourcePaths } from 'AppComponents/Shared/ScopeValidation';
import TokenManager from 'AppComponents/Shared/AppsAndKeys/TokenManager';
/**
 * @inheritdoc
 * @param {*} theme theme object
 */
const styles = theme => ({
    button: {
        padding: theme.spacing.unit,
        color: theme.palette.getContrastText(theme.palette.background.default),
        display: 'flex',
        alignItems: 'center',
        fontSize: '11px',
        cursor: 'pointer',
        '& span': {
            paddingLeft: 6,
            display: 'inline-block',
        },
    },
    actionColumn: {
        display: 'flex',
        textAlign: 'right',
        direction: 'rtl',
    },
    td: {
        color: theme.palette.getContrastText(theme.palette.background.default),
        borderBottom: 'solid 1px ' + theme.palette.grey.A200,
        fontSize: '11px',
        paddingLeft: theme.spacing.unit,
        height: 35,
    },
});

const subscriptionTableRow = (props) => {
    const {
        classes, loadInfo, handleSubscriptionDelete,
        theme, selectedAppId, updateSubscriptionData, selectedKeyType, app, index,
    } = props;
    return (
        <React.Fragment>
            <tr style={{ backgroundColor: index % 2 ? '' : '#ffffff' }}>
                <td className={classes.td}>{app.label}</td>
                <td className={classes.td}>{app.policy}</td>
                <td className={classes.td}>{app.status}</td>
                <td className={classes.td}>
                    <div className={classes.actionColumn}>
                        <Link
                            className={classes.button}
                            to={'/applications/' + app.value}
                        >
                            <span>MANAGE APP</span>
                            <CustomIcon
                                width={16}
                                height={16}
                                strokeColor={theme.palette.primary.main}
                                icon='applications'
                            />
                        </Link>
                        <ScopeValidation
                            resourcePath={resourcePaths.SINGLE_SUBSCRIPTION}
                            resourceMethod={resourceMethods.DELETE}
                        >
                            <a
                                className={classes.button}
                                onClick={() => handleSubscriptionDelete(
                                    app.subscriptionId,
                                    updateSubscriptionData,
                                )}
                                onKeyDown={() => handleSubscriptionDelete(
                                    app.subscriptionId,
                                    updateSubscriptionData,
                                )}
                            >
                                <span>UNSUBSCRIBE</span>
                                <CustomIcon
                                    width={16}
                                    height={16}
                                    strokeColor={theme.palette.primary.main}
                                    icon='subscriptions'
                                />
                            </a>
                        </ScopeValidation>
                        <a
                            className={classNames(classes.button, {
                                [classes.activeLink]: selectedAppId
                                            && selectedKeyType === 'PRODUCTION'
                                            && app.value === selectedAppId,
                            })}
                            onClick={() => loadInfo('PRODUCTION', app.value)}
                            onKeyDown={() => loadInfo('PRODUCTION', app.value)}
                        >
                            <span>PROD KEYS</span>
                            <CustomIcon
                                width={16}
                                height={16}
                                strokeColor={theme.palette.primary.main}
                                icon='productionkeys'
                            />
                        </a>
                        <a
                            className={classNames(classes.button, {
                                [classes.activeLink]: selectedAppId
                                            && selectedKeyType === 'SANDBOX'
                                            && app.value === selectedAppId,
                            })}
                            onClick={() => loadInfo('SANDBOX', app.value)}
                            onKeyDown={() => loadInfo('SANDBOX', app.value)}
                        >
                            <span>SANDBOX KEYS</span>
                            <CustomIcon
                                width={16}
                                height={16}
                                strokeColor={theme.palette.primary.main}
                                icon='productionkeys'
                            />
                        </a>
                    </div>
                </td>
            </tr>
            {app.value === selectedAppId && (selectedKeyType === 'PRODUCTION' || selectedKeyType === 'SANDBOX') && (
                <tr>
                    <td colSpan='4'>
                        <div className={classes.selectedWrapper}>
                            <TokenManager
                                keyType={selectedKeyType}
                                selectedApp={{ appId: app.value, label: app.label }}
                                updateSubscriptionData={updateSubscriptionData}
                            />
                        </div>
                    </td>
                </tr>
            )}
        </React.Fragment>
    );
};

export default withStyles(styles, { withTheme: true })(subscriptionTableRow);
