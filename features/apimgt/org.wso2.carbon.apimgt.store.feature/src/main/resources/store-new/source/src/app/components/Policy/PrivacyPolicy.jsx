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
import { FormattedMessage } from 'react-intl';
import Footer from '../Base/Footer/Footer';

const styles = {
    header: {
        paddingTop: 70,
    },
};
/**
 *
 *
 * @class PrivacyPolicy
 * @extends {React.Component}
 */
class PrivacyPolicy extends React.Component {
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
                                    <FormattedMessage defaultMessage='API STORE' id='Policy.PrivacyPolicy.api.store' />
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
                                <a style={{ textDecoration: 'none' }} href='#privacyPolicy'>
                                    <FormattedMessage defaultMessage='Privacy Policy' id='Policy.PrivacyPolicy.privacy.policy' />
                                </a>
                            </MenuItem>
                            <MenuItem>
                                <ListItemIcon>
                                    <Label />
                                </ListItemIcon>
                                <a style={{ textDecoration: 'none' }} href='#personalInfo'>
                                    <FormattedMessage
                                        defaultMessage='What are the personal information?'
                                        id='Policy.PrivacyPolicy.what.are.the'
                                    />
                                </a>
                            </MenuItem>
                            <MenuItem>
                                <ListItemIcon>
                                    <Label />
                                </ListItemIcon>
                                <a style={{ textDecoration: 'none' }} href='#infoCollection'>
                                    <FormattedMessage
                                        defaultMessage='Collection of your information'
                                        id='Policy.PrivacyPolicy.collection.of'
                                    />
                                </a>
                            </MenuItem>
                            <MenuItem>
                                <ListItemIcon>
                                    <Label />
                                </ListItemIcon>
                                <a style={{ textDecoration: 'none' }} href='#useOfInfo'>
                                    <FormattedMessage
                                        defaultMessage='Use of your personal information'
                                        id='Policy.PrivacyPolicy.use.of.your'
                                    />
                                </a>
                            </MenuItem>
                            <MenuItem>
                                <ListItemIcon>
                                    <Label />
                                </ListItemIcon>
                                <a style={{ textDecoration: 'none' }} href='#disclosureOfInfo'>
                                    <FormattedMessage
                                        defaultMessage='Disclosure of your information'
                                        id='Policy.PrivacyPolicy.disclosure.of.your'
                                    />
                                </a>
                            </MenuItem>
                            <MenuItem>
                                <ListItemIcon>
                                    <Label />
                                </ListItemIcon>
                                <a style={{ textDecoration: 'none' }} href='#storageOfInfo'>
                                    <FormattedMessage
                                        defaultMessage='How API-M keeps your information'
                                        id='Policy.PrivacyPolicy.how.apim'
                                    />
                                </a>
                            </MenuItem>
                            <MenuItem>
                                <ListItemIcon>
                                    <Label />
                                </ListItemIcon>
                                <a style={{ textDecoration: 'none' }} href='#about'>
                                    <FormattedMessage defaultMessage='About' id='Policy.PrivacyPolicy.about.link' />
                                </a>
                            </MenuItem>
                            <MenuItem>
                                <ListItemIcon>
                                    <Label />
                                </ListItemIcon>
                                <a style={{ textDecoration: 'none' }} href='#disclaimer'>
                                    <FormattedMessage defaultMessage='Disclaimer' id='Policy.PrivacyPolicy.disclaimer.link' />
                                </a>
                            </MenuItem>
                        </MenuList>
                    </Grid>
                    <Grid item xs={8}>
                        <h1>
                            <strong>
                                <FormattedMessage
                                    defaultMessage='WSO2 API Manager - Privacy Policy'
                                    id='Policy.PrivacyPolicy.description1'
                                />
                            </strong>
                        </h1>
                        <p>
                            <a target='_blank' href='https://wso2.com/api-management/'>
                                <FormattedMessage
                                    defaultMessage='About WSO2 API Manager'
                                    id='Policy.PrivacyPolicy.description2'
                                />
                            </a>
                        </p>
                        <p>
                            <FormattedMessage
                                defaultMessage='WSO2 API Manager (referred hereafter as “API-M ”) is an open source enterprise-class solution that supports API publishing, lifecycle management, application development, access control, rate limiting, and analytics in one cleanly integrated system.'
                                id='Policy.PrivacyPolicy.description2'
                            />
                        </p>
                        <h2 id='privacyPolicy'>
                            <strong>
                                <FormattedMessage
                                    defaultMessage='Privacy Policy'
                                    id='Policy.PrivacyPolicy.description2'
                                />
                            </strong>
                        </h2>
                        <p>
                            <FormattedMessage
                                defaultMessage='This section explains how API-M captures your personal information, purpose of capturing, and the retention of your personal information. Please note that this policy is for reference only, and is applicable for the software as a product. WSO2 Inc., or its developers have no access to the information held within API-M. Please refer “Disclaimer” for more information.'
                                id='Policy.PrivacyPolicy.description2'
                            />
                        </p>
                        <h2 id='personalInfo'>
                            <FormattedMessage
                                defaultMessage='What are the personal information?'
                                id='Policy.PrivacyPolicy.description2'
                            />
                        </h2>
                        <ul>
                            <li>
                                <FormattedMessage
                                    defaultMessage='Your user name (except in the case where your user name is created by your employer under contract)'
                                    id='Policy.PrivacyPolicy.description2'
                                />
                            </li>
                            <li>
                                <FormattedMessage
                                    defaultMessage='The IP address you use to login'
                                    id='Policy.PrivacyPolicy.description2'
                                />
                            </li>
                            <li>
                                <FormattedMessage
                                    defaultMessage='Your device ID, if you choose to login with a device (Phone, Tablet)'
                                    id='Policy.PrivacyPolicy.description2'
                                />
                            </li>
                        </ul>
                        <p>
                            <FormattedMessage
                                defaultMessage='API-M considers anything related to you as your personal information. This includes, but is not limited to, However API-M does not consider the following as your personal information, and uses this only for analytical purposes, since this information cannot be used to track you.'
                                id='Policy.PrivacyPolicy.description2'
                            />
                        </p>
                        <ul>
                            <li>
                                <FormattedMessage
                                    defaultMessage='City/Country from which your TCP/IP connection originates'
                                    id='Policy.PrivacyPolicy.description2'
                                />
                            </li>
                            <li>
                                <FormattedMessage
                                    defaultMessage='Time of the day you login.(Year, Month, Week, Hour or Minute)'
                                    id='Policy.PrivacyPolicy.description2'
                                />
                            </li>
                            <li>
                                <FormattedMessage
                                    defaultMessage='Type of the device you use to login (Phone, Tablet, etc.)'
                                    id='Policy.PrivacyPolicy.description2'
                                />
                            </li>
                            <li>
                                <FormattedMessage
                                    defaultMessage='Operating system and Generic browser information'
                                    id='Policy.PrivacyPolicy.description2'
                                />
                            </li>
                        </ul>
                        <h2 id='infoCollection'>
                            <FormattedMessage
                                defaultMessage='Collection of your information'
                                id='Policy.PrivacyPolicy.description2'
                            />
                        </h2>
                        <p>
                            <FormattedMessage
                                defaultMessage='API-M collects your information to only serve your access requirements. For example,'
                                id='Policy.PrivacyPolicy.description2'
                            />
                        </p>
                        <ul>
                            <li>
                                <FormattedMessage
                                    defaultMessage='API-M uses your IP address to detect any suspicious login attempt to your account.'
                                    id='Policy.PrivacyPolicy.description2'
                                />
                            </li>
                            <li>
                                <FormattedMessage
                                    defaultMessage='API-M uses your First Name, Last Name, etc to provide rich and personalized information.'
                                    id='Policy.PrivacyPolicy.description2'
                                />
                            </li>
                        </ul>
                        <h3>
                            <FormattedMessage
                                defaultMessage='Tracking Technologies'
                                id='Policy.PrivacyPolicy.description2'
                            />
                        </h3>
                        <p>
                            <FormattedMessage
                                defaultMessage='API-M collects your information through the following,'
                                id='Policy.PrivacyPolicy.description2'
                            />
                        </p>
                        <ul>
                            <li>
                                <FormattedMessage
                                    defaultMessage='The user sign up page where you enter your personal data'
                                    id='Policy.PrivacyPolicy.description2'
                                />
                            </li>
                            <li>
                                <FormattedMessage
                                    defaultMessage='Tracking your IP address with HTTP request, HTTP headers, and TCP/IP'
                                    id='Policy.PrivacyPolicy.description2'
                                />
                            </li>
                            <li>
                                <FormattedMessage
                                    defaultMessage='Tracking your geographic information with the IP address'
                                    id='Policy.PrivacyPolicy.description2'
                                />
                            </li>
                            <li>
                                <FormattedMessage
                                    defaultMessage='Your login history with browser cookies. Please refer our cookie policy for more information'
                                    id='Policy.PrivacyPolicy.description2'
                                />
                            </li>
                        </ul>
                        <h2 id='useOfInfo'>
                            <FormattedMessage
                                defaultMessage='Use of your personal information'
                                id='Policy.PrivacyPolicy.description2'
                            />
                        </h2>
                        <p>
                            <FormattedMessage
                                defaultMessage='API-M will use your personal information only for the purposes for which it was collected (or for a use identified as consistent with that purpose).'
                                id='Policy.PrivacyPolicy.description2'
                            />
                        </p>
                        <p>
                            <FormattedMessage
                                defaultMessage='API-M uses your personal information only for the following purposes.'
                                id='Policy.PrivacyPolicy.description2'
                            />
                        </p>
                        <ul>
                            <li>
                                <FormattedMessage
                                    defaultMessage='To provide you with a personalized user experience. API-M uses attributes such as your name for this purpose'
                                    id='Policy.PrivacyPolicy.description2'
                                />
                            </li>
                            <li>
                                <FormattedMessage
                                    defaultMessage='To protect your account from unauthorized access or a potential hacking attempt. API-M use HTTP or TCP/IP Headers for this purpose which includes'
                                    id='Policy.PrivacyPolicy.description2'
                                />
                            </li>
                            <ul>
                                <li>
                                    <FormattedMessage
                                        defaultMessage='IP address,'
                                        id='Policy.PrivacyPolicy.description2'
                                    />
                                </li>
                                <li>
                                    <FormattedMessage
                                        defaultMessage='Browser fingerprinting,'
                                        id='Policy.PrivacyPolicy.description2'
                                    />
                                </li>
                                <li>
                                    <FormattedMessage defaultMessage='Cookies' id='Policy.PrivacyPolicy.description2' />
                                </li>
                            </ul>
                            <li>
                                <FormattedMessage
                                    defaultMessage='To derive statistical data for analytical purposes on system performance improvements. API-M will not keep any personal information after statistical calculations. Thus a statistical report has no means to identify an individual person'
                                    id='Policy.PrivacyPolicy.description2'
                                />
                            </li>
                            <li>
                                <FormattedMessage
                                    defaultMessage='API-M may use'
                                    id='Policy.PrivacyPolicy.description2'
                                />
                            </li>
                            <ul>
                                <li>
                                    <FormattedMessage
                                        defaultMessage='The IP Address to derive geographic information'
                                        id='Policy.PrivacyPolicy.description2'
                                    />
                                </li>
                                <li>
                                    <FormattedMessage
                                        defaultMessage='Browser fingerprinting to determine the browser technology and version'
                                        id='Policy.PrivacyPolicy.description2'
                                    />
                                </li>
                            </ul>
                        </ul>
                        <h2 id='disclosureOfInfo'>
                            <FormattedMessage
                                defaultMessage='Disclosure of your personal information'
                                id='Policy.PrivacyPolicy.description2'
                            />
                        </h2>
                        <p>
                            <FormattedMessage
                                defaultMessage='API-M will disclose personal information only for the purposes for which it was collected (or for a use identified as consistent with that purpose), unless you have consented otherwise or where it is required by law.'
                                id='Policy.PrivacyPolicy.description2'
                            />
                        </p>
                        <h3>
                            <FormattedMessage defaultMessage='Legal process' id='Policy.PrivacyPolicy.description2' />
                        </h3>
                        <p>
                            <FormattedMessage
                                defaultMessage='API-M may disclose your personal information with or without your consent where it is required by law following the due and lawful process.'
                                id='Policy.PrivacyPolicy.description2'
                            />
                        </p>
                        <h2 id='storageOfInfo'>
                            <FormattedMessage
                                defaultMessage='How API-M keeps your personal information'
                                id='Policy.PrivacyPolicy.description2'
                            />
                        </h2>
                        <h3>
                            <FormattedMessage
                                defaultMessage='Where your personal information is stored'
                                id='Policy.PrivacyPolicy.description2'
                            />
                        </h3>
                        <p>
                            <FormattedMessage
                                defaultMessage='API-M stores your personal information in secured databases. API-M exercises proper industry accepted security measures to protect the database where your personal information is held'
                                id='Policy.PrivacyPolicy.description2'
                            />
                        </p>
                        <p>
                            <FormattedMessage
                                defaultMessage='API-M may use encryption to keep your personal data with added level of security.'
                                id='Policy.PrivacyPolicy.description2'
                            />
                        </p>
                        <h3>
                            <FormattedMessage
                                defaultMessage='How long does API-M keep your personal information?'
                                id='Policy.PrivacyPolicy.description2'
                            />
                        </h3>
                        <p>
                            <FormattedMessage
                                defaultMessage='API-M keep your personal data as long as you are an active user of our system. You can update your personal data at any time with the given self-care user portals.'
                                id='Policy.PrivacyPolicy.description2'
                            />
                        </p>
                        <h3>
                            <FormattedMessage
                                defaultMessage='How can you request a removal of your personal information?'
                                id='Policy.PrivacyPolicy.description2'
                            />
                        </h3>
                        <p>
                            <FormattedMessage
                                defaultMessage='You can request the administrator to delete your account The administrator will be the administrator of the tenant you are registered or the super-administrator if you do not use the tenant feature.'
                                id='Policy.PrivacyPolicy.description2'
                            />
                        </p>
                        <p>
                            <FormattedMessage
                                defaultMessage='You can additionally request to anonymize all traces of your activities that may have been retained by API-M in Logs, Databases or Analytical storage.'
                                id='Policy.PrivacyPolicy.description2'
                            />
                        </p>
                        <h2 id='about'>
                            <FormattedMessage defaultMessage='About' id='Policy.PrivacyPolicy.description2' />
                        </h2>
                        <h3>
                            <FormattedMessage
                                defaultMessage='Changes to this policy'
                                id='Policy.PrivacyPolicy.description2'
                            />
                        </h3>
                        <p>
                            <FormattedMessage
                                defaultMessage='The organization running API-M may revise the Privacy Policy from time to time. You can find the most recent governing policy with the respective link provided by the organization running API-M. The organization will notify any changes to the privacy policy over our official public channels.'
                                id='Policy.PrivacyPolicy.description2'
                            />
                        </p>
                        <h3>
                            <FormattedMessage defaultMessage='Your choices' id='Policy.PrivacyPolicy.description2' />
                        </h3>
                        <p>
                            <FormattedMessage
                                defaultMessage='If you already have an account with API Manager; you have the right to deactivate your account if you find that this privacy policy is unacceptable for you.'
                                id='Policy.PrivacyPolicy.description2'
                            />
                        </p>
                        <p>
                            <FormattedMessage
                                defaultMessage='If you do not have an account, you can choose not to subscribe if you do not agree with our privacy policy.'
                                id='Policy.PrivacyPolicy.description2'
                            />
                        </p>
                        <h3>
                            <FormattedMessage defaultMessage='Contact Us' id='Policy.PrivacyPolicy.description2' />
                        </h3>
                        <p>
                            <FormattedMessage
                                defaultMessage='Please contact us if you have any question or concerns of this privacy policy.'
                                id='Policy.PrivacyPolicy.description2'
                            />
                        </p>
                        <p>
                            <a target='_blank' href='https://wso2.com/contact/'>
                                https://wso2.com/contact/
                            </a>
                        </p>
                        <h2 id='disclaimer'>
                            <FormattedMessage defaultMessage='Disclaimer' id='Policy.PrivacyPolicy.description2' />
                        </h2>
                        <ol>
                            <li>
                                <FormattedMessage
                                    defaultMessage='This privacy policy statement serves as a template for the organization running WSO2 API-M. The organizational policies will govern the real privacy policy applicable for its business purposes.'
                                    id='Policy.PrivacyPolicy.description2'
                                />
                            </li>
                            <li>
                                <FormattedMessage
                                    defaultMessage='WSO2 or its employees, partners, affiliates do not have access to any data, including privacy-related data held at the organization running API-M.'
                                    id='Policy.PrivacyPolicy.description2'
                                />
                            </li>
                            <li>
                                <FormattedMessage
                                    defaultMessage='This policy should be modified according to the organizational requirements.'
                                    id='Policy.PrivacyPolicy.description2'
                                />
                            </li>
                        </ol>
                    </Grid>
                </Grid>
                <Footer />
            </div>
        );
    }
}

PrivacyPolicy.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(PrivacyPolicy);
