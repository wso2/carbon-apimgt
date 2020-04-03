/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
import React from 'react';
import { Icon, IconButton, withStyles } from '@material-ui/core';
import PropTypes from 'prop-types';
import { Link } from 'react-router-dom';
import { FormattedMessage } from 'react-intl';

const styles = (theme) => ({
    settingsIconbtn: {
        color: theme.palette.getContrastText(theme.palette.background.appBar),
        fontSize: theme.typography.fontSize,
        textTransform: 'uppercase',
        fontWeight: 'bold',
    },
    settinsIcon: {
        marginRight: theme.spacing(1),
    },
});

const SettingsButton = (props) => {
    const { classes } = props;
    return (
        <>
            <Link to='/settings'>
                <IconButton
                    id='settings-btn'
                    aria-owns='profile-menu-appbar'
                    aria-haspopup='true'
                    color='inherit'
                    className={classes.settingsIconbtn}
                >
                    <Icon className={classes.settinsIcon}>
                        settings
                    </Icon>
                    <FormattedMessage
                        id='Apis.Base.Header.settings.SettingsButton.settings.caption'
                        defaultMessage='Settings'
                    />
                </IconButton>
            </Link>
        </>
    );
};

SettingsButton.propTypes = {
    classes: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(SettingsButton);
