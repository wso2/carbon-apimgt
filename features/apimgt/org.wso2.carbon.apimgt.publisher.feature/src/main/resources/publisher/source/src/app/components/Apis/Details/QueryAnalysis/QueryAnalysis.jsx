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

import React from 'react';
import PropTypes from 'prop-types';
import { makeStyles } from '@material-ui/core/styles';
import AppBar from '@material-ui/core/AppBar';
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';
import Typography from '@material-ui/core/Typography';
import Box from '@material-ui/core/Box';
import QueryDepthIcon from '@material-ui/icons/AssignmentReturned';
import QueryComplexityIcon from '@material-ui/icons/AllInclusive';
import { FormattedMessage } from 'react-intl';
import DepthAnalysis from './DepthAnalysis';
import ComplexityAnalysis from './ComplexityAnalysis';

/**
 * Tab Panel configurations
 * @param {*} props props
 * @returns {*} content which are displayed under the tab
 */
function TabPanel(props) {
    const {
        children, value, index, ...other
    } = props;

    return (
        <Typography
            component='div'
            role='tabpanel'
            hidden={value !== index}
            id={`scrollable-force-tabpanel-${index}`}
            aria-labelledby={`scrollable-force-tab-${index}`}
            {...other}
        >
            {value === index && <Box p={3}>{children}</Box>}
        </Typography>
    );
}

TabPanel.propTypes = {
    children: PropTypes.node.isRequired,
    index: PropTypes.string.isRequired,
    value: PropTypes.string.isRequired,
};

/**
 * Setting cofigurations of inidividual tabs
 * @param {*} index index to access
 * @returns {*} properties of tabs
 */
function a11yProps(index) {
    return {
        id: `scrollable-force-tab-${index}`,
        'aria-controls': `scrollable-force-tabpanel-${index}`,
    };
}

const useStyles = makeStyles((theme) => ({
    root: {
        flexGrow: 1,
        width: '100%',
        backgroundColor: theme.palette.background.paper,
    },
    tabLabel: {
        fontSize: '0.8571428571428571rem',
    },
    titleWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: theme.spacing(2),
    },
}));

/**
 * Generate the Query Analysis UI in API details page
 * @returns {*} Basic UI components of Query Analysis page
 */
function QueryAnalysis() {
    const classes = useStyles();
    const [value, setValue] = React.useState(0);

    const handleChange = (event, newValue) => {
        setValue(newValue);
    };

    return (
        <>
            <Box className={classes.titleWrapper}>
                <Typography variant='h4'>
                    <FormattedMessage
                        id='Apis.Details.QueryAnalysis.QueryAnalysis.query.analysis'
                        defaultMessage='Query Analysis'
                    />
                </Typography>
            </Box>
            <div className={classes.root}>
                <AppBar position='static' color='default'>
                    <Tabs
                        value={value}
                        onChange={handleChange}
                        variant='scrollable'
                        scrollButtons='on'
                        indicatorColor='primary'
                        textColor='primary'
                        aria-label='scrollable force tabs example'
                    >
                        <Tab
                            label={<span className={classes.tabLabel}>Query Depth</span>}
                            icon={<QueryDepthIcon />}
                            {...a11yProps(0)}
                        />
                        <Tab
                            label={<span className={classes.tabLabel}>Query Complexity</span>}
                            icon={<QueryComplexityIcon />}
                            {...a11yProps(1)}
                        />
                    </Tabs>

                </AppBar>
                <TabPanel value={value} index={0}>
                    <DepthAnalysis />
                </TabPanel>
                <TabPanel value={value} index={1}>
                    <ComplexityAnalysis />
                </TabPanel>
            </div>
        </>
    );
}

export default QueryAnalysis;
