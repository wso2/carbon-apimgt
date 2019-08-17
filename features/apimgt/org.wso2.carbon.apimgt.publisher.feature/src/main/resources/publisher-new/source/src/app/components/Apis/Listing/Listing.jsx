/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
// TODO: DO we need this component ? this is a pure proxy just passing the props through this to children ? ~tmkb
import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import TableView from './TableView/TableView';

const styles = {
    content: {
        flexGrow: 1,
    },
};
/**
 * Render the APIs Listing page, This is the Default Publisher Landing page as well
 *
 * @function Listing
 * @returns {React.Component} @inheritdoc
 */
function Listing(props) {
    const { classes, isAPIProduct, theme } = props;

    return (
        <main className={classes.content}>
            <TableView isAPIProduct={isAPIProduct} theme={theme} />
        </main>
    );
}

Listing.propTypes = {
    classes: PropTypes.shape({
        content: PropTypes.string,
        contentInside: PropTypes.string,
    }).isRequired,
    theme: PropTypes.shape({
        custom: PropTypes.string,
    }).isRequired,
    isAPIProduct: PropTypes.bool.isRequired,
};

export default withStyles(styles, { withTheme: true })(Listing);
