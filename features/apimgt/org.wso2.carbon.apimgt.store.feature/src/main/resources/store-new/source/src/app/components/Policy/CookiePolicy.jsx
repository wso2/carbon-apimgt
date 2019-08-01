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
import AppBar from '@material-ui/core/AppBar';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import { MenuList, MenuItem } from '@material-ui/core/Menu';
import { ListItemIcon } from '@material-ui/core/List';
import Label from '@material-ui/icons/Label';
import ToolBar from '@material-ui/core/Toolbar';
import Table, {
    TableBody, TableHead, TableRow, TableCell,
} from '@material-ui/core/Table';
import { FormattedMessage, injectIntl, } from 'react-intl';
import Footer from '../Base/Footer/Footer';

const styles = {
    header: {
        paddingTop: 70,
    },
};

/**
 *
 *
 * @class CookiePolicy
 * @extends {React.Component}
 */
class CookiePolicy extends React.Component {
    constructor(props) {
        this.super(props);
        const { intl } = props;
        this.ookieDetails = [
            {
                name: 'JSESSIONID',
                purpose: intl.formatMessage({
                    defaultMessage:
                        'Keeps track of the user session data when you are logged in for providing a better user experience',
                    id: 'Policy.CookiePolicy.keep.track.experience',
                }),
                retention: 'Session',
            },
            {
                name: 'goto_url',
                purpose: intl.formatMessage({
                    defaultMessage: 'Keeps track of the page that you should be directed to after login',
                    id: 'Policy.CookiePolicy.keep.track.login',
                }),
                retention: 'Session',
            },
            {
                name: 'workflowCookie',
                purpose: intl.formatMessage({
                    defaultMessage:
                        'Used for authentication purposes when invoking an admin service in the Business Process Server',
                    id: 'Policy.CookiePolicy.used.for.server',
                }),
                retention: 'Session',
            },
            {
                name: 'csrftoken',
                purpose: intl.formatMessage({
                    defaultMessage:
                        'Used for mitigating Cross Site Request Forgery Attacks to provide you with a secure service',
                    id: 'Policy.CookiePolicy.used.for.service',
                }),
                retention: 'Request',
            },
            {
                name: 'i18next',
                purpose: intl.formatMessage({
                    defaultMessage: 'Used to track the language API-M is served to you',
                    id: 'Policy.CookiePolicy.used.to.you',
                }),
                retention: 'Session',
            },
        ];
    }

    render() {
        const { classes } = this.props;
        return (
            <div>
                <div className={classes.header}>
                    <AppBar position='absolute'>
                        <ToolBar>
                            <Typography variant='title' noWrap>
                                <img src='/store-new/site/public/images/logo.png' alt='wso2-logo' />
                                <span style={{ color: 'white' }}>
                                    <FormattedMessage defaultMessage='API STORE' id='Policy.CookiePolicy.api.store' />
                                </span>
                            </Typography>
                        </ToolBar>
                    </AppBar>
                </div>
                <Grid container spacing={24}>
                    <Grid item xs={3}>
                        <MenuList>
                            <MenuItem>
                                <ListItemIcon>
                                    <Label />
                                </ListItemIcon>
                                <a style={{ textDecoration: 'none' }} href='#cookiePolicy'>
                                    <FormattedMessage
                                        defaultMessage='Cookie Policy'
                                        id='Policy.CookiePolicy.cookie.policy'
                                    />
                                </a>
                            </MenuItem>
                            <MenuItem>
                                <ListItemIcon>
                                    <Label />
                                </ListItemIcon>
                                <a style={{ textDecoration: 'none' }} href='#cookie'>
                                    <FormattedMessage
                                        defaultMessage='What is a cookie?'
                                        id='Policy.CookiePolicy.what.is'
                                    />
                                </a>
                            </MenuItem>
                            <MenuItem>
                                <ListItemIcon>
                                    <Label />
                                </ListItemIcon>
                                <a style={{ textDecoration: 'none' }} href='#howCookieIsProcessed'>
                                    <FormattedMessage
                                        defaultMessage='How does API-M process cookies?'
                                        id='Policy.CookiePolicy.how'
                                    />
                                </a>
                            </MenuItem>
                            <MenuItem>
                                <ListItemIcon>
                                    <Label />
                                </ListItemIcon>
                                <a style={{ textDecoration: 'none' }} href='#useOfCookies'>
                                    <FormattedMessage
                                        defaultMessage='What does API-M use cookies for?'
                                        id='Policy.CookiePolicy.what.does'
                                    />
                                </a>
                            </MenuItem>
                            <MenuItem>
                                <ListItemIcon>
                                    <Label />
                                </ListItemIcon>
                                <a style={{ textDecoration: 'none' }} href='#typeOfCookies'>
                                    <FormattedMessage
                                        defaultMessage='What type of cookies does API-M use?'
                                        id='Policy.CookiePolicy.what.type'
                                    />
                                </a>
                            </MenuItem>
                            <MenuItem>
                                <ListItemIcon>
                                    <Label />
                                </ListItemIcon>
                                <a style={{ textDecoration: 'none' }} href='#controllingCookies'>
                                    <FormattedMessage
                                        defaultMessage='How do I control my cookies?'
                                        id='Policy.CookiePolicy.how.do.i'
                                    />
                                </a>
                            </MenuItem>
                            <MenuItem>
                                <ListItemIcon>
                                    <Label />
                                </ListItemIcon>
                                <a style={{ textDecoration: 'none' }} href='#usedCookies'>
                                    <FormattedMessage
                                        defaultMessage='What are the cookies used?'
                                        id='Policy.CookiePolicy.what.are.the'
                                    />
                                </a>
                            </MenuItem>
                            <MenuItem>
                                <ListItemIcon>
                                    <Label />
                                </ListItemIcon>
                                <a style={{ textDecoration: 'none' }} href='#disclaimer'>
                                    <FormattedMessage defaultMessage='Disclaimer' id='Policy.CookiePolicy.disclaimer' />
                                </a>
                            </MenuItem>
                        </MenuList>
                    </Grid>
                    <Grid item xs={8}>
                        <h1>
                            <strong>
                                <FormattedMessage
                                    defaultMessage='WSO2 API Manager - Cookie Policy'
                                    id='Policy.CookiePolicy.description1'
                                />
                            </strong>
                        </h1>
                        <p>
                            <a target='_blank' href='https://wso2.com/api-management/'>
                                <FormattedMessage
                                    defaultMessage='About WSO2 API Manager'
                                    id='Policy.CookiePolicy.description2'
                                />
                            </a>
                        </p>
                        <p>
                            <FormattedMessage
                                defaultMessage='WSO2 API Manager (referred hereafter as “API-M ”) is an open source enterprise-class
                                solution that supports API publishing, lifecycle management, application development, access
                                control, rate limiting, and analytics in one cleanly integrated system.'
                                id='Policy.CookiePolicy.description3'
                            />
                        </p>
                        <h2 id='cookiePolicy'>
                            <strong>
                                <FormattedMessage
                                    defaultMessage='Cookie Policy'
                                    id='Policy.CookiePolicy.description4'
                                />
                            </strong>
                        </h2>
                        <p>
                            <FormattedMessage
                                defaultMessage='API-M uses cookies to provide you with the best user experience, and to securely identify
                            you. You might not be able to access some of the services if you disable cookies.'
                                id='Policy.CookiePolicy.description5'
                            />
                        </p>
                        <h2 id='cookie'>
                            <FormattedMessage
                                defaultMessage='What is a cookie?'
                                id='Policy.CookiePolicy.what.is.a.cookie'
                            />
                        </h2>
                        <p>
                            <FormattedMessage
                                defaultMessage='A browser cookie is a small piece of data that is stored on your device to help websites and
                                mobile apps remember things about you. Other technologies, including Web storage and
                                identifiers associated with your device, may be used for similar purposes. In this policy,
                                we use the term “cookies” to discuss all of these technologies.'
                                id='Policy.CookiePolicy.description6'
                            />
                        </p>
                        <h2 id='howCookieIsProcessed'>
                            <FormattedMessage
                                defaultMessage='How does API-M process cookies?'
                                id='Policy.CookiePolicy.description7'
                            />
                        </h2>
                        <p>
                            <FormattedMessage
                                defaultMessage='API-M uses cookies to store and retrieve information on your browser. This information is
                            used to provide a better user experience. Some cookies have the primary purpose of allowing
                            logging in to the system, maintaining sessions, and keeping track of activities you do
                            within the login session.'
                                id='Policy.CookiePolicy.description8'
                            />
                        </p>
                        <p>
                            <FormattedMessage
                                defaultMessage='Some cookies used in API-M are used to identify you personally. However, the cookie lifetime
                            will end when you log-out ending your session or when your session expires.'
                                id='Policy.CookiePolicy.description9'
                            />
                        </p>
                        <p>
                            <FormattedMessage
                                defaultMessage='Some cookies are simply used to give you a more personalised web experience, and these
                            cannot be used to identify you or your activities personally.'
                                id='Policy.CookiePolicy.description10'
                            />
                        </p>
                        <p>
                            <FormattedMessage
                                defaultMessage='This Cookie Policy is part of the API-M Privacy Policy.'
                                id='Policy.CookiePolicy.description11'
                            />
                        </p>
                        <h2 id='useOfCookies'>
                            <FormattedMessage
                                defaultMessage='What does API-M use cookies for?'
                                id='Policy.CookiePolicy.description12'
                            />
                        </h2>
                        <p>
                            <FormattedMessage
                                defaultMessage='Cookies are used for two purposes in API-M<'
                                id='Policy.CookiePolicy.description13'
                            />
                            /p>
                            <ol>
                                <li>
                                    <FormattedMessage
                                        defaultMessage='Security'
                                        id='Policy.CookiePolicy.description14'
                                    />
                                </li>
                                <li>
                                    <FormattedMessage
                                        defaultMessage='Providing a better user experience'
                                        id='Policy.CookiePolicy.description15'
                                    />
                                </li>
                            </ol>
                            <h3>
                                <FormattedMessage
                                    defaultMessage='API-M uses cookies for the following purposes'
                                    id='Policy.CookiePolicy.description16'
                                />
                            </h3>
                            <h4>
                                <FormattedMessage defaultMessage='Preferences' id='Policy.CookiePolicy.description17' />
                            </h4>
                            <p>
                                <FormattedMessage
                                    defaultMessage='API-M uses cookies to remember your settings and preferences and to auto-fill the fields to
                            make your interactions with the site easier.'
                                    id='Policy.CookiePolicy.description18'
                                />
                            </p>
                            <ul>
                                <li>
                                    <FormattedMessage
                                        defaultMessage='These cannot be used to identify you personally'
                                        id='Policy.CookiePolicy.description19'
                                    />
                                </li>
                            </ul>
                            <h4>
                                <FormattedMessage defaultMessage='Security' id='Policy.CookiePolicy.description20' />
                            </h4>
                            <p>
                                <FormattedMessage
                                    defaultMessage='API-M uses selected cookies to identify and prevent security risks.'
                                    id='Policy.CookiePolicy.description21'
                                />
                            </p>
                            <p>
                                <FormattedMessage
                                    defaultMessage='For example, API-M may use cookies to store your session information to prevent others from
                            changing your password without your username and password.'
                                    id='Policy.CookiePolicy.description22'
                                />
                            </p>
                            <p>
                                <FormattedMessage
                                    defaultMessage='API-M uses session cookie to maintain your active session.'
                                    id='Policy.CookiePolicy.description23'
                                />
                            </p>
                            <p>
                                <FormattedMessage
                                    defaultMessage='API-M may use a temporary cookie when performing multi-factor authentication and federated
                            authentication.'
                                    id='Policy.CookiePolicy.description24'
                                />
                            </p>
                            <p>
                                <FormattedMessage
                                    defaultMessage='API-M may use permanent cookies to detect the devices you have logged in previously. This is
                            to to calculate the risk level associated with your current login attempt. Using these
                            cookies protects you and your account from possible attacks.'
                                    id='Policy.CookiePolicy.description25'
                                />
                            </p>
                            <h4>
                                <FormattedMessage defaultMessage='Performance' id='Policy.CookiePolicy.description26' />
                            </h4>
                            <p>
                                <FormattedMessage
                                    defaultMessage='API-M may use cookies to allow “Remember Me” functionalities.'
                                    id='Policy.CookiePolicy.description27'
                                />
                            </p>
                            <h3>
                                <FormattedMessage defaultMessage='Analytics' id='Policy.CookiePolicy.description28' />
                            </h3>
                            <p>
                                <FormattedMessage
                                    defaultMessage='API-M as a product does not use cookies for analytical purposes.'
                                    id='Policy.CookiePolicy.description29'
                                />
                            </p>
                            <h3>
                                <FormattedMessage
                                    defaultMessage='Third party cookies'
                                    id='Policy.CookiePolicy.description30'
                                />
                            </h3>
                            <p>
                                <FormattedMessage
                                    defaultMessage='Using API-M may cause some third-party cookie being set to your browser. API-M has no
                            control over the operation of these cookies. The third-party cookies which maybe set
                            include,'
                                    id='Policy.CookiePolicy.description31'
                                />
                            </p>
                            <ul>
                                <li>
                                    <FormattedMessage
                                        defaultMessage=' Any of the social login sites, when API-M is configured to use "Social" or "Federated"
                                login, and you opt to do login with your "Social Account"'
                                        id='Policy.CookiePolicy.description32'
                                    />
                                </li>
                                <li>
                                    <FormattedMessage
                                        defaultMessage='Any third party federated login'
                                        id='Policy.CookiePolicy.description33'
                                    />
                                </li>
                            </ul>
                            <p>
                                <FormattedMessage
                                    defaultMessage='We strongly advise you to refer the respective cookie policies of such sites carefully as
                            API-M has no knowledge or use on these cookies.'
                                    id='Policy.CookiePolicy.description34'
                                />
                            </p>
                            <h2 id='typeOfCookies'>
                                <FormattedMessage
                                    defaultMessage='What type of cookies does API-M use?'
                                    id='Policy.CookiePolicy.description35'
                                />
                            </h2>
                            <p>
                                <FormattedMessage
                                    defaultMessage='API-M uses persistent cookies and session cookies. A persistent cookie helps API-M to
                            recognize you as an existing user, so you can easily return to WSO2 or interact with API-M
                            without signing in again. After you sign in, a persistent cookie stays in your browser and
                            will be read by API-M when you return.'
                                    id='Policy.CookiePolicy.description36'
                                />
                            </p>
                            <p>
                                <FormattedMessage
                                    defaultMessage='A session cookie is erased when the user closes the Web browser. It is stored in temporarily
                            and is not retained after the browser is closed. Session cookies do not collect information
                            from the user’s computer.'
                                    id='Policy.CookiePolicy.description37'
                                />
                            </p>
                            <h2 id='controllingCookies'>
                                <FormattedMessage
                                    defaultMessage='How do I control my cookies?'
                                    id='Policy.CookiePolicy.description38'
                                />
                            </h2>
                            <p>
                                <FormattedMessage
                                    defaultMessage='Most browsers allow you to control cookies through settings. However, if you limit the
                            ability of websites to set cookies, you may worsen your overall user experience, since it
                            will no longer be personalized to you. It may also stop you from saving customized settings
                            like login information. Disabling cookies might make you unable to use Authentication and
                            Authorization functionalities offered by API-M.'
                                    id='Policy.CookiePolicy.description39'
                                />
                            </p>
                            <p>
                                <FormattedMessage
                                    defaultMessage='If you have any questions or concerns regarding the use of cookies, please contact the Data
                            Protection Officer of the organization running this API-M instance.'
                                    id='Policy.CookiePolicy.description40'
                                />
                            </p>
                            <h2 id='usedCookies'>
                                <FormattedMessage
                                    defaultMessage='What are the cookies used?'
                                    id='Policy.CookiePolicy.what.are'
                                />
                            </h2>
                            <Table>
                                <TableHead>
                                    <TableRow>
                                        <TableCell>
                                            <FormattedMessage
                                                defaultMessage='Cookie Name'
                                                id='Policy.CookiePolicy.cookie.name'
                                            />
                                        </TableCell>
                                        <TableCell>
                                            <FormattedMessage
                                                defaultMessage='Purpose'
                                                id='Policy.CookiePolicy.purpose'
                                            />
                                        </TableCell>
                                        <TableCell>
                                            <FormattedMessage
                                                defaultMessage='Retention'
                                                id='Policy.CookiePolicy.retention'
                                            />
                                        </TableCell>
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    {this.cookieDetails.map((detail) => {
                                        return (
                                            <TableRow>
                                                <TableCell>{detail.name}</TableCell>
                                                <TableCell>{detail.purpose}</TableCell>
                                                <TableCell>{detail.retention}</TableCell>
                                            </TableRow>
                                        );
                                    })}
                                </TableBody>
                            </Table>
                            <h2 id='disclaimer'>
                                <FormattedMessage defaultMessage='Disclaimer' id='Policy.CookiePolicy.disclaimer' />
                            </h2>
                            <p>
                                <FormattedMessage
                                    defaultMessage='This cookie policy is only for illustrative purposes of the API-M product. The content in
                            this policy is technically correct at the time of product shipment. The organization which
                            runs this API-M instance has the full authority and responsibility of the effective Cookie
                            Policy.'
                                    id='Policy.CookiePolicy.disclaimer.description'
                                />
                            </p>
                        </p>
                    </Grid>
                </Grid>
                <Footer />
            </div>
        );
    }
}

CookiePolicy.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default injectIntl(withStyles(styles)(CookiePolicy));
