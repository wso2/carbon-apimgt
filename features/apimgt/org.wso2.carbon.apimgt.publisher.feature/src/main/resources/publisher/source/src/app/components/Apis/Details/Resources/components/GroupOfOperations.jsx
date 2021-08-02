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
import { makeStyles } from '@material-ui/core/styles';
import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import PropTypes from 'prop-types';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import Typography from '@material-ui/core/Typography';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';

const useStyles = makeStyles((theme) => ({
    tagClass: {
        maxWidth: 1000,
        overflow: 'hidden',
        whiteSpace: 'nowrap',
        textOverflow: 'ellipsis',
        [theme.breakpoints.down('md')]: {
            maxWidth: 800,
        },
    },
}
));

/**
 *
 * Return a group container , User should provide the operations list as the child component
 * @export
 * @param {*} props
 * @returns {React.Component} @inheritdoc
 */
export default function GroupOfOperations(props) {
    const classes = useStyles();
    const { openAPI, children, tag } = props;
    const currentTagInfo = openAPI.tags && openAPI.tags.find((tagInfo) => tagInfo.name === tag);
    return (
        <ExpansionPanel defaultExpanded>
            <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />} aria-controls='panel1a-content' id='panel1a-header'>
                <Typography
                    variant='h4'
                    className={classes.tagClass}
                    title={tag}
                >
                    {tag}
                </Typography>
                {currentTagInfo && (
                    <Typography style={{ margin: '0px 30px' }} variant='caption'>
                        {currentTagInfo.description}
                    </Typography>
                )}
            </ExpansionPanelSummary>
            <ExpansionPanelDetails>{children}</ExpansionPanelDetails>
        </ExpansionPanel>
    );
}

GroupOfOperations.propTypes = {
    children: PropTypes.element.isRequired,
    openAPI: PropTypes.shape({ tags: PropTypes.arrayOf(PropTypes.string) }).isRequired,
    tag: PropTypes.string.isRequired,
};
