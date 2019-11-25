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
import { Typography, withStyles, Container } from '@material-ui/core';
import { FormattedMessage } from 'react-intl';
import PropTypes from 'prop-types';
import Settings from './Settings';

const styles = theme => ({
    root: {
        padding: theme.spacing(3),
        width: '100%',
    },
    headingWrapper: {
        paddingTop: theme.spacing(2),
        paddingBottom: theme.spacing(2),
        paddingLeft: theme.spacing(),
    },
});

function SettingsBase(props) {
    const { classes } = props;
    return (
        <Container fixed>
            <div className={classes.headingWrapper}>
                <Typography variant='h5'>
                    <FormattedMessage
                        id='Apis.Settings.SettingsBase.header'
                        defaultMessage='Settings'
                    />
                </Typography>
                <Typography variant='caption'>
                    <FormattedMessage
                        id='Apis.Settings.SettingsBase.sub.header'
                        defaultMessage='View and Configure Developer Portal Settings'
                    />
                </Typography>
            </div>
            <Settings />
        </Container>
    );
}

SettingsBase.propTypes = {
    classes: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(SettingsBase);
