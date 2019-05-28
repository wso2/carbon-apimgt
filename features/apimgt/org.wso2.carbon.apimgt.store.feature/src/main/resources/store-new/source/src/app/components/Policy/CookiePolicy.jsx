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
import Footer from '../Base/Footer/Footer';

const styles = {
    header: {
        paddingTop: 70,
    },
};

const cookieDetails = [{ name: 'JSESSIONID', purpose: 'Keeps track of the user session data when you are logged in for providing a better user experience', retention: 'Session' }, { name: 'goto_url', purpose: 'Keeps track of the page that you should be directed to after login', retention: 'Session' }, { name: 'workflowCookie', purpose: 'Used for authentication purposes when invoking an admin service in the Business Process Server', retention: 'Session' }, { name: 'csrftoken', purpose: 'Used for mitigating Cross Site Request Forgery Attacks to provide you with a secure service', retention: 'Request' }, { name: 'i18next', purpose: 'Used to track the language API-M is served to you', retention: 'Session' }];
/**
 *
 *
 * @class CookiePolicy
 * @extends {React.Component}
 */
class CookiePolicy extends React.Component {
    render() {
        const { classes } = this.props;
        return (
            <div>
                <div className={classes.header}>
                    <AppBar position='absolute'>
                        <ToolBar>
                            <Typography variant='title' noWrap>
                                <img src='/store-new/site/public/images/logo.png' alt='wso2-logo' />
                                {' '}
                                <span style={{ color: 'white' }}>API STORE</span>
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
                                    Cookie Policy
                                </a>
                            </MenuItem>
                            <MenuItem>
                                <ListItemIcon>
                                    <Label />
                                </ListItemIcon>
                                <a style={{ textDecoration: 'none' }} href='#cookie'>
                                    What is a cookie?
                                </a>
                            </MenuItem>
                            <MenuItem>
                                <ListItemIcon>
                                    <Label />
                                </ListItemIcon>
                                <a style={{ textDecoration: 'none' }} href='#howCookieIsProcessed'>
                                    How does API-M process cookies?
                                </a>
                            </MenuItem>
                            <MenuItem>
                                <ListItemIcon>
                                    <Label />
                                </ListItemIcon>
                                <a style={{ textDecoration: 'none' }} href='#useOfCookies'>
                                    What does API-M use cookies for?
                                </a>
                            </MenuItem>
                            <MenuItem>
                                <ListItemIcon>
                                    <Label />
                                </ListItemIcon>
                                <a style={{ textDecoration: 'none' }} href='#typeOfCookies'>
                                    What type of cookies does API-M use?
                                </a>
                            </MenuItem>
                            <MenuItem>
                                <ListItemIcon>
                                    <Label />
                                </ListItemIcon>
                                <a style={{ textDecoration: 'none' }} href='#controllingCookies'>
                                    How do I control my cookies?
                                </a>
                            </MenuItem>
                            <MenuItem>
                                <ListItemIcon>
                                    <Label />
                                </ListItemIcon>
                                <a style={{ textDecoration: 'none' }} href='#usedCookies'>
                                    What are the cookies used?
                                </a>
                            </MenuItem>
                            <MenuItem>
                                <ListItemIcon>
                                    <Label />
                                </ListItemIcon>
                                <a style={{ textDecoration: 'none' }} href='#disclaimer'>
                                    Disclaimer
                                </a>
                            </MenuItem>
                        </MenuList>
                    </Grid>
                    <Grid item xs={8}>
                        <h1>
                            <strong>WSO2 API Manager - Cookie Policy</strong>
                        </h1>
                        <p>
                            <a target='_blank' href='https://wso2.com/api-management/'>
                                About WSO2 API Manager
                            </a>
                        </p>
                        <p>WSO2 API Manager (referred hereafter as “API-M ”) is an open source enterprise-class solution that supports API publishing, lifecycle management, application development, access control, rate limiting, and analytics in one cleanly integrated system.</p>
                        <h2 id='cookiePolicy'>
                            <strong>Cookie Policy</strong>
                        </h2>
                        <p>API-M uses cookies to provide you with the best user experience, and to securely identify you. You might not be able to access some of the services if you disable cookies.</p>
                        <h2 id='cookie'>What is a cookie?</h2>
                        <p>A browser cookie is a small piece of data that is stored on your device to help websites and mobile apps remember things about you. Other technologies, including Web storage and identifiers associated with your device, may be used for similar purposes. In this policy, we use the term “cookies” to discuss all of these technologies.</p>
                        <h2 id='howCookieIsProcessed'>How does API-M process cookies?</h2>
                        <p>API-M uses cookies to store and retrieve information on your browser. This information is used to provide a better user experience. Some cookies have the primary purpose of allowing logging in to the system, maintaining sessions, and keeping track of activities you do within the login session.</p>
                        <p>Some cookies used in API-M are used to identify you personally. However, the cookie lifetime will end when you log-out ending your session or when your session expires.</p>
                        <p>Some cookies are simply used to give you a more personalised web experience, and these cannot be used to identify you or your activities personally.</p>
                        <p>This Cookie Policy is part of the API-M Privacy Policy.</p>
                        <h2 id='useOfCookies'>What does API-M use cookies for?</h2>
                        <p>Cookies are used for two purposes in API-M</p>
                        <ol>
                            <li>Security</li>
                            <li>Providing a better user experience</li>
                        </ol>
                        <h3>API-M uses cookies for the following purposes</h3>
                        <h4>Preferences</h4>
                        <p>API-M uses cookies to remember your settings and preferences and to auto-fill the fields to make your interactions with the site easier.</p>
                        <ul>
                            <li>These cannot be used to identify you personally</li>
                        </ul>
                        <h4>Security</h4>
                        <p>API-M uses selected cookies to identify and prevent security risks.</p>
                        <p>For example, API-M may use cookies to store your session information to prevent others from changing your password without your username and password.</p>
                        <p>API-M uses session cookie to maintain your active session.</p>
                        <p>API-M may use a temporary cookie when performing multi-factor authentication and federated authentication.</p>
                        <p>API-M may use permanent cookies to detect the devices you have logged in previously. This is to to calculate the risk level associated with your current login attempt. Using these cookies protects you and your account from possible attacks.</p>
                        <h4>Performance</h4>
                        <p>API-M may use cookies to allow “Remember Me” functionalities.</p>
                        <h3>Analytics</h3>
                        <p>API-M as a product does not use cookies for analytical purposes.</p>
                        <h3>Third party cookies</h3>
                        <p>Using API-M may cause some third-party cookie being set to your browser. API-M has no control over the operation of these cookies. The third-party cookies which maybe set include,</p>
                        <ul>
                            <li>Any of the social login sites, when API-M is configured to use “Social” or “Federated” login, and you opt to do login with your “Social Account”</li>
                            <li>Any third party federated login</li>
                        </ul>
                        <p>We strongly advise you to refer the respective cookie policies of such sites carefully as API-M has no knowledge or use on these cookies.</p>
                        <h2 id='typeOfCookies'>What type of cookies does API-M use?</h2>
                        <p>API-M uses persistent cookies and session cookies. A persistent cookie helps API-M to recognize you as an existing user, so you can easily return to WSO2 or interact with API-M without signing in again. After you sign in, a persistent cookie stays in your browser and will be read by API-M when you return.</p>
                        <p>A session cookie is erased when the user closes the Web browser. It is stored in temporarily and is not retained after the browser is closed. Session cookies do not collect information from the user’s computer.</p>
                        <h2 id='controllingCookies'>How do I control my cookies?</h2>
                        <p>Most browsers allow you to control cookies through settings. However, if you limit the ability of websites to set cookies, you may worsen your overall user experience, since it will no longer be personalized to you. It may also stop you from saving customized settings like login information. Disabling cookies might make you unable to use Authentication and Authorization functionalities offered by API-M.</p>
                        <p>If you have any questions or concerns regarding the use of cookies, please contact the Data Protection Officer of the organization running this API-M instance.</p>
                        <h2 id='usedCookies'>What are the cookies used?</h2>
                        <Table>
                            <TableHead>
                                <TableRow>
                                    <TableCell>Cookie Name</TableCell>
                                    <TableCell>Purpose</TableCell>
                                    <TableCell>Retention</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {cookieDetails.map((detail) => {
                                    return (
                                        <TableRow>
                                            <TableCell>{detail.name}</TableCell>
                                            <TableCell>
                                                {detail.purpose}
                                                {' '}
                                            </TableCell>
                                            <TableCell>{detail.retention}</TableCell>
                                        </TableRow>
                                    );
                                })}
                            </TableBody>
                        </Table>
                        <h2 id='disclaimer'>Disclaimer</h2>
                        <p>This cookie policy is only for illustrative purposes of the API-M product. The content in this policy is technically correct at the time of product shipment. The organization which runs this API-M instance has the full authority and responsibility of the effective Cookie Policy.</p>
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

export default withStyles(styles)(CookiePolicy);
