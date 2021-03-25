/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import React from 'react';
import { Link } from 'react-router-dom';
import { withStyles } from '@material-ui/core/styles';
import classNames from 'classnames';
import CustomIcon from 'AppComponents/Shared/CustomIcon';
import { ScopeValidation, resourceMethods, resourcePaths } from 'AppComponents/Shared/ScopeValidation';
import TokenManager from 'AppComponents/Shared/AppsAndKeys/TokenManager';
import PropTypes from 'prop-types';
import { FormattedMessage } from 'react-intl';

/**
 * @inheritdoc
 * @param {*} theme theme object
 */
const styles = (theme) => ({
    button: {
        padding: theme.spacing(1),
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
        paddingLeft: theme.spacing(1),
        height: 35,
    },
    selectedWrapper: {
        borderLeft: 'solid 2px ' + theme.palette.primary.main,
    },
});

const subscriptionTableRow = (props) => {
    const {
        classes, loadInfo, handleSubscriptionDelete,
        theme, selectedAppId, updateSubscriptionData, selectedKeyType, app, applicationOwner, hashEnabled,
    } = props;
    return (
        <>
            <tr>
                <td className={classes.td}>{app.label}</td>
                <td className={classes.td}>{app.policy}</td>
                <td className={classes.td}>{app.status}</td>
                <td className={classes.td}>
                    <div className={classes.actionColumn}>
                        <Link
                            className={classes.button}
                            to={'/applications/' + app.value}
                        >
                            <span>
                                <FormattedMessage
                                    id='Apis.Details.Credentials.SubscriptionTableRow.manage.app'
                                    defaultMessage='MANAGE APP'
                                />
                            </span>
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
                            <Link
                                className={classes.button}
                                onClick={() => handleSubscriptionDelete(
                                    app.subscriptionId,
                                    updateSubscriptionData,
                                )}
                            >
                                <span>
                                    <FormattedMessage
                                        id='Apis.Details.Credentials.SubscriptionTableRow.unsubscribe'
                                        defaultMessage='UNSUBSCRIBE'
                                    />
                                </span>
                                <CustomIcon
                                    width={16}
                                    height={16}
                                    strokeColor={theme.palette.primary.main}
                                    icon='subscriptions'
                                />
                            </Link>
                        </ScopeValidation>
                        <Link
                            className={classNames(classes.button, {
                                [classes.activeLink]: selectedAppId
                                            && selectedKeyType === 'PRODUCTION'
                                            && app.value === selectedAppId,
                            })}
                            onClick={() => loadInfo('PRODUCTION', app.value)}
                        >
                            <span>
                                <FormattedMessage
                                    id='Apis.Details.Credentials.SubscriptionTableRow.prod.keys'
                                    defaultMessage='PROD KEYS'
                                />
                            </span>
                            <CustomIcon
                                width={16}
                                height={16}
                                strokeColor={theme.palette.primary.main}
                                icon='productionkeys'
                            />
                        </Link>
                        <Link
                            className={classNames(classes.button, {
                                [classes.activeLink]: selectedAppId
                                            && selectedKeyType === 'SANDBOX'
                                            && app.value === selectedAppId,
                            })}
                            onClick={() => loadInfo('SANDBOX', app.value)}
                        >
                            <span>
                                <FormattedMessage
                                    id='Apis.Details.Credentials.SubscriptionTableRow.sandbox.keys'
                                    defaultMessage='SANDBOX KEYS'
                                />
                            </span>
                            <CustomIcon
                                width={16}
                                height={16}
                                strokeColor={theme.palette.primary.main}
                                icon='productionkeys'
                            />
                        </Link>
                    </div>
                </td>
            </tr>
            {app.value === selectedAppId && (selectedKeyType === 'PRODUCTION' || selectedKeyType === 'SANDBOX') && (
                <tr>
                    <td colSpan='4'>
                        <div className={classes.selectedWrapper}>
                            <TokenManager
                                keyType={selectedKeyType}
                                selectedApp={{
                                    appId: app.value,
                                    label: app.label,
                                    owner: applicationOwner,
                                    hashEnabled,
                                }}
                                updateSubscriptionData={updateSubscriptionData}
                            />
                        </div>
                    </td>
                </tr>
            )}
        </>
    );
};
subscriptionTableRow.propTypes = {
    classes: PropTypes.shape({
        td: PropTypes.shape({}),
        actionColumn: PropTypes.shape({}),
        button: PropTypes.shape({}),
        activeLink: PropTypes.shape({}),
        selectedWrapper: PropTypes.shape({}),
    }).isRequired,
    theme: PropTypes.shape({

    }).isRequired,
    handleSubscriptionDelete: PropTypes.func.isRequired,
    loadInfo: PropTypes.func.isRequired,
    selectedAppId: PropTypes.string.isRequired,
    updateSubscriptionData: PropTypes.func.isRequired,
    selectedKeyType: PropTypes.string.isRequired,
    applicationOwner: PropTypes.string.isRequired,
    app: PropTypes.shape({
        label: PropTypes.string,
        policy: PropTypes.string,
        status: PropTypes.string,
        value: PropTypes.string,
        subscriptionId: PropTypes.string,
    }).isRequired,
};
export default withStyles(styles, { withTheme: true })(subscriptionTableRow);
