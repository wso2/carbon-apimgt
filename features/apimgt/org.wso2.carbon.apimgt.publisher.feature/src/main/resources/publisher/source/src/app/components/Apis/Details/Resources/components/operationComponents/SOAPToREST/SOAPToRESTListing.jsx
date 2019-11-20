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

import React, {
    useState, Suspense, lazy, useEffect,
} from 'react';
import PropTypes from 'prop-types';
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';
import Divider from '@material-ui/core/Divider';
import CircularProgress from '@material-ui/core/CircularProgress';
import useMediaQuery from '@material-ui/core/useMediaQuery';
import Button from '@material-ui/core/Button';
import EditIcon from '@material-ui/icons/Edit';
import Paper from '@material-ui/core/Paper';
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';
import Box from '@material-ui/core/Box';

import PolicyEditor from './PolicyEditor';

const MonacoEditor = lazy(() => import('react-monaco-editor' /* webpackChunkName: "SOAPToRESTMonaco" */));

/**
 *
 * Renders the operation parameters section
 * @export
 * @param {*} props
 * @returns
 */
export default function SOAPToRESTListing(props) {
    const { resourcePolicy, resourcePoliciesDispatcher } = props;
    const [resourcePolicyIn, setResourcePolicyIn] = useState(resourcePolicy.in);
    const [resourcePolicyOut, setResourcePolicyOut] = useState(resourcePolicy.out);
    useEffect(() => {
        setResourcePolicyIn(resourcePolicy.in);
        setResourcePolicyOut(resourcePolicy.out);
    }, [resourcePolicy]);
    // Get use preference from OS https://material-ui.com/customization/palette/#user-preference
    const prefersDarkMode = useMediaQuery('(prefers-color-scheme: dark)');
    const [openEditor, setOpenEditor] = useState(false);
    const [selectedTab, setTabIndex] = useState('in');
    const editorOptions = {
        selectOnLineNumbers: true,
        readOnly: true,
        smoothScrolling: true,
        wordWrap: 'on',
    };
    const selectedPolicy = selectedTab === 'in' ? resourcePolicyIn : resourcePolicyOut;
    const editorProps = {
        language: 'xml',
        height: 'calc(50vh)',
        theme: prefersDarkMode ? 'vs-dark' : 'vs',
        value: selectedPolicy.content,
        options: editorOptions,
    };

    /**
     *
     *
     * @param {*} content
     */
    function setPolicyContent(content) {
        if (selectedTab === 'in') {
            setResourcePolicyIn({ ...resourcePolicyIn, content });
        } else {
            setResourcePolicyOut({ ...resourcePolicyOut, content });
        }
    }
    return (
        <>
            <Grid item xs={12} md={12}>
                <Typography variant='subtitle1'>
                    Mediation
                    <Divider variant='middle' />
                </Typography>
            </Grid>
            <Grid item xs={12}>
                <Tabs
                    value={selectedTab}
                    indicatorColor='primary'
                    textColor='primary'
                    onChange={(event, tab) => setTabIndex(tab)}
                    aria-label='Resource mediation in/out tabs'
                >
                    <Tab value='in' label='In' />
                    <Tab value='out' label='Out' />
                </Tabs>
                <Box p={1}>
                    <Button onClick={() => setOpenEditor(true)} variant='outlined' size='small' color='primary'>
                        Edit
                        {' '}
                        <EditIcon />
                    </Button>
                    {!openEditor && (
                        <Paper elevation={4}>
                            <Suspense fallback={<CircularProgress disableShrink />}>
                                <MonacoEditor {...editorProps} />
                            </Suspense>
                        </Paper>
                    )}
                </Box>
            </Grid>
            <PolicyEditor
                resourcePoliciesDispatcher={resourcePoliciesDispatcher}
                setPolicyContent={setPolicyContent}
                selectedPolicy={selectedPolicy}
                originalResourcePolicy={resourcePolicy[selectedTab]}
                direction={selectedTab}
                prefersDarkMode={prefersDarkMode}
                onClose={() => setOpenEditor(false)}
                open={openEditor}
            />
        </>
    );
}

SOAPToRESTListing.defaultProps = {
    disableUpdate: false,
};
SOAPToRESTListing.propTypes = {
    resourcePoliciesDispatcher: PropTypes.func.isRequired,
    resourcePolicy: PropTypes.shape({}).isRequired,
    operationsDispatcher: PropTypes.func.isRequired,
    target: PropTypes.string.isRequired,
    verb: PropTypes.string.isRequired,
    disableUpdate: PropTypes.bool,
};
