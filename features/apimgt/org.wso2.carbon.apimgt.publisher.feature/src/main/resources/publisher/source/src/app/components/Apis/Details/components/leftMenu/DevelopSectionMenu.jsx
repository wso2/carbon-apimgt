/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React, { useState } from 'react';
import { makeStyles, withStyles } from '@material-ui/core/styles';
import Accordion from '@material-ui/core/Accordion';
import MuiAccordionSummary from '@material-ui/core/AccordionSummary';
import MuiAccordionDetails from '@material-ui/core/AccordionDetails';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import LeftMenuItem from 'AppComponents/Shared/LeftMenuItem';
import Typography from '@material-ui/core/Typography';
import EndpointIcon from '@material-ui/icons/GamesOutlined';
import ScopesIcon from '@material-ui/icons/VpnKey';
import DocumentsIcon from '@material-ui/icons/LibraryBooks';
import BusinessIcon from '@material-ui/icons/Business';
import ConfigurationIcon from '@material-ui/icons/Build';
import PropertiesIcon from '@material-ui/icons/List';
import SubscriptionsIcon from '@material-ui/icons/RssFeed';
import Tooltip from '@material-ui/core/Tooltip';
import CommentIcon from '@material-ui/icons/Comment';
import IconButton from '@material-ui/core/IconButton';
import InfoOutlinedIcon from '@material-ui/icons/InfoOutlined';
import RuntimeConfigurationIcon from '@material-ui/icons/Settings';
import MonetizationIcon from '@material-ui/icons/LocalAtm';
import { isRestricted } from 'AppData/AuthManager';
import { PROPERTIES as UserProperties } from 'AppData/User';
import { useUser } from 'AppComponents/Shared/AppContext';
import { useIntl } from 'react-intl';


const AccordianSummary = withStyles({
    root: {
        backgroundColor: '#1a1f2f',
        paddingLeft: '8px',
        borderBottom: '1px solid rgba(0, 0, 0, .125)',
        minHeight: 40,
        '&$expanded': {
            minHeight: 40,
        },
    },
    content: {
        '&$expanded': {
            margin: 0,
        },
    },
    expanded: {
        backgroundColor: '#1a1f2f',
    },
})(MuiAccordionSummary);

const AccordionDetails = withStyles((theme) => ({
    root: {
        backgroundColor: '#1a1f2f',
        paddingLeft: theme.spacing(0),
        paddingRight: theme.spacing(2),
        paddingTop: '0',
        paddingBottom: '0',
    },
}))(MuiAccordionDetails);


const useStyles = makeStyles((theme) => ({
    footeremaillink: {
        marginLeft: theme.custom.leftMenuWidth, /* 4px */
    },
    root: {
        backgroundColor: theme.palette.background.leftMenu,
        paddingLeft: theme.spacing(2),
        paddingRight: theme.spacing(2),
        paddingTop: '0',
        paddingBottom: '0',
    },
    expanded: {
        '&$expanded': {
            margin: 0,
            backgroundColor: theme.palette.background.leftMenu,
            minHeight: 40,
            paddingBottom: 0,
            paddingLeft: 0,
            paddingRight: 0,
            paddingTop: 0,
        },
    },
    leftLInkText: {
        color: theme.palette.getContrastText(theme.palette.background.leftMenu),
        textTransform: theme.custom.leftMenuTextStyle,
        width: '100%',
        textAlign: 'left',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        fontSize: theme.typography.body1.fontSize,
        fontWeight: 250,
        whiteSpace: 'nowrap',
    },
    expandIconColor: {
        color: '#ffffff',
    },
}));

/**
 *
 * @param {*} props
 * @returns
 */
export default function DevelopSectionMenu(props) {
    const {
        pathPrefix, isAPIProduct, api, getLeftMenuItemForResourcesByType, getLeftMenuItemForDefinitionByType,
    } = props;
    const user = useUser();
    const [portalConfigsExpanded, setPortalConfigsExpanded] = useState(user
        .getProperty(UserProperties.PORTAL_CONFIG_OPEN));
    const [apiConfigsExpanded, setApiConfigsExpanded] = useState(user.getProperty(UserProperties.API_CONFIG_OPEN));
    const handleAccordionState = (section, isExpanded) => {
        if (section === 'portalConfigsExpanded') {
            setPortalConfigsExpanded(isExpanded);
            user.setProperty(UserProperties.PORTAL_CONFIG_OPEN, isExpanded);
        } else {
            setApiConfigsExpanded(isExpanded);
            user.setProperty(UserProperties.API_CONFIG_OPEN, isExpanded);
        }
    };
    const classes = useStyles();
    const intl = useIntl();

    return (
        <div className={classes.root}>
            <Accordion
                id='itest-api-details-portal-config-acc'
                defaultExpanded={portalConfigsExpanded}
                elevation={0}
                onChange={(e, isExpanded) => handleAccordionState('portalConfigsExpanded',
                    isExpanded)}
                classes={{ expanded: classes.expanded }}
            >
                <AccordianSummary
                    expandIcon={<ExpandMoreIcon className={classes.expandIconColor} />}
                >
                    <Typography className={classes.leftLInkText}>
                        Portal Configurations
                    </Typography>
                </AccordianSummary>
                <AccordionDetails>
                    <div>
                        <LeftMenuItem
                            className={classes.footeremaillink}
                            text={intl.formatMessage({
                                id: 'Apis.Details.index.design.configs',
                                defaultMessage: 'Basic info',
                            })}
                            route='configuration'
                            to={pathPrefix + 'configuration'}
                            Icon={<ConfigurationIcon />}
                        />
                        <LeftMenuItem
                            text={intl.formatMessage({
                                id: 'Apis.Details.index.business.info',
                                defaultMessage: 'business info',
                            })}
                            to={pathPrefix + 'business info'}
                            Icon={<BusinessIcon />}
                        />
                        {!isAPIProduct && api.advertiseInfo && !api.advertiseInfo.advertised && (
                            <LeftMenuItem
                                text={intl.formatMessage({
                                    id: 'Apis.Details.index.subscriptions',
                                    defaultMessage: 'subscriptions',
                                })}
                                to={pathPrefix + 'subscriptions'}
                                Icon={<SubscriptionsIcon />}
                            />
                        )}
                        {isAPIProduct && (
                            <LeftMenuItem
                                text={intl.formatMessage({
                                    id: 'Apis.Details.index.subscriptions',
                                    defaultMessage: 'subscriptions',
                                })}
                                to={pathPrefix + 'subscriptions'}
                                Icon={<SubscriptionsIcon />}
                            />
                        )}
                        <LeftMenuItem
                            text={intl.formatMessage({
                                id: 'Apis.Details.index.documents',
                                defaultMessage: 'documents',
                            })}
                            to={pathPrefix + 'documents'}
                            Icon={<DocumentsIcon />}
                        />
                        {!isAPIProduct && (
                            <LeftMenuItem
                                text={intl.formatMessage({
                                    id: 'Apis.Details.index.comments',
                                    defaultMessage: 'Comments',
                                })}
                                route='comments'
                                to={pathPrefix + 'comments'}
                                Icon={<CommentIcon />}
                            />
                        )}
                    </div>
                </AccordionDetails>
            </Accordion>
            <Accordion
                id='itest-api-details-api-config-acc'
                defaultExpanded={apiConfigsExpanded}
                elevation={0}
                onChange={(e, isExpanded) => handleAccordionState('apiConfigsExpanded',
                    isExpanded)}
                classes={{ expanded: classes.expanded }}
            >
                <AccordianSummary
                    expandIcon={<ExpandMoreIcon className={classes.expandIconColor} />}
                >
                    <Typography className={classes.leftLInkText}>
                        API Configurations
                    </Typography>
                    <Tooltip
                        title={'If you make any changes to the API configuration, you need to redeploy'
                            + ' the API to see updates in the API Gateway.'}
                        placement='bottom'
                    >
                        <IconButton color='primary' size='small' aria-label='delete'>
                            <InfoOutlinedIcon fontSize='small' />
                        </IconButton>
                    </Tooltip>
                </AccordianSummary>
                <AccordionDetails>
                    <div>
                        {!isAPIProduct && api.advertiseInfo && !api.advertiseInfo.advertised
                            && !api.isWebSocket() && (
                            <LeftMenuItem
                                text={intl.formatMessage({
                                    id: 'Apis.Details.index.runtime.configs',
                                    defaultMessage: 'Runtime',
                                })}
                                route='runtime-configuration'
                                to={pathPrefix + 'runtime-configuration'}
                                Icon={<RuntimeConfigurationIcon />}
                            />
                        )}
                        {isAPIProduct && (
                            <LeftMenuItem
                                text={intl.formatMessage({
                                    id: 'Apis.Details.index.runtime.configs',
                                    defaultMessage: 'Runtime',
                                })}
                                route='runtime-configuration'
                                to={pathPrefix + 'runtime-configuration'}
                                Icon={<RuntimeConfigurationIcon />}
                            />
                        )}
                        {api.advertiseInfo && !api.advertiseInfo.advertised && api.isWebSocket() && (
                            <LeftMenuItem
                                text={intl.formatMessage({
                                    id: 'Apis.Details.index.runtime.configs',
                                    defaultMessage: 'Runtime',
                                })}
                                route='runtime-configuration'
                                to={pathPrefix + 'runtime-configuration-websocket'}
                                Icon={<RuntimeConfigurationIcon />}
                            />
                        )}
                        {!isAPIProduct && api.advertiseInfo && !api.advertiseInfo.advertised
                            && getLeftMenuItemForResourcesByType(api.type)}
                        {isAPIProduct && getLeftMenuItemForResourcesByType(api.type)}
                        {getLeftMenuItemForDefinitionByType(api.type)}
                        {api.advertiseInfo && !api.advertiseInfo.advertised && !isAPIProduct
                            && api.type !== 'WEBSUB' && (
                            <LeftMenuItem
                                text={intl.formatMessage({
                                    id: 'Apis.Details.index.endpoints',
                                    defaultMessage: 'endpoints',
                                })}
                                to={pathPrefix + 'endpoints'}
                                Icon={<EndpointIcon />}
                            />
                        )}
                        {api.advertiseInfo && !api.advertiseInfo.advertised && !isAPIProduct && (
                            <LeftMenuItem
                                text={intl.formatMessage({
                                    id: 'Apis.Details.index.left.menu.scope',
                                    defaultMessage: 'Local Scopes',
                                })}
                                route='scopes'
                                to={pathPrefix + 'scopes'}
                                Icon={<ScopesIcon />}
                            />
                        )}

                        <LeftMenuItem
                            text={intl.formatMessage({
                                id: 'Apis.Details.index.properties',
                                defaultMessage: 'properties',
                            })}
                            to={pathPrefix + 'properties'}
                            Icon={<PropertiesIcon />}
                        />

                        {!api.isWebSocket() && !isRestricted(['apim:api_publish'], api) && (
                            <>
                                {!isAPIProduct && api.advertiseInfo
                                    && !api.advertiseInfo.advertised && (
                                    <LeftMenuItem
                                        text={intl.formatMessage({
                                            id: 'Apis.Details.index.monetization',
                                            defaultMessage: 'monetization',
                                        })}
                                        to={pathPrefix + 'monetization'}
                                        Icon={<MonetizationIcon />}
                                    />
                                )}
                            </>
                        )}
                        {isAPIProduct && !api.isWebSocket()
                            && !isRestricted(['apim:api_publish'], api) && (
                            <LeftMenuItem
                                text={intl.formatMessage({
                                    id: 'Apis.Details.index.monetization',
                                    defaultMessage: 'monetization',
                                })}
                                to={pathPrefix + 'monetization'}
                                Icon={<MonetizationIcon />}
                            />
                        )}
                    </div>
                </AccordionDetails>
            </Accordion>
        </div>
    );
}
