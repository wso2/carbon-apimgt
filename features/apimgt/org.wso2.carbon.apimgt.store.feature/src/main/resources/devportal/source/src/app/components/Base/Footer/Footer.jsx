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
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage } from 'react-intl';

/**
 *
 *
 * @param {*} theme
 */
const styles = theme => ({
    footer: {
        position: 'fixed',
        bottom: 0,
        width: '100%',
        backgroundColor: theme.palette.background.paper,
        padding: 8,
        textAlign: 'center',
        zIndex: 10,
    },
});
/**
 *
 *
 * @param {*} props
 * @returns
 */
const Footer = (props) => {
    const { classes } = props;
    return (
        <footer className={classes.footer}>
            <Typography noWrap>
                <FormattedMessage
                    id='Base.Footer.Footer.copyright.text'
                    defaultMessage='WSO2 API-M v3.1.0 | © 2019 WSO2 Inc'
                />
            </Typography>
        </footer>
    );
};
Footer.propTypes = {
    classes: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(Footer);
