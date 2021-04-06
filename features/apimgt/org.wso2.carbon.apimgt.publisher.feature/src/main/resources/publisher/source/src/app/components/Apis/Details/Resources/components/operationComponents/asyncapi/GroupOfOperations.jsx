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

import React from 'react';
import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import PropTypes from 'prop-types';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import Typography from '@material-ui/core/Typography';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';

/**
 * Return a group container , User should provide the operations list as the child component
 * @export
 * @param {*} props
 * @returns {React.Component} @inheritdoc
 */
export default function GroupOfOperations(props) {
    const { children, tag } = props;
    return (
        <ExpansionPanel defaultExpanded>
            <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />} aria-controls='panel1a-content' id='panel1a-header'>
                <Typography variant='h4'>
                    {tag}
                </Typography>
            </ExpansionPanelSummary>
            <ExpansionPanelDetails>{children}</ExpansionPanelDetails>
        </ExpansionPanel>
    );
}

GroupOfOperations.propTypes = {
    children: PropTypes.element.isRequired,
    tag: PropTypes.string.isRequired,
};
