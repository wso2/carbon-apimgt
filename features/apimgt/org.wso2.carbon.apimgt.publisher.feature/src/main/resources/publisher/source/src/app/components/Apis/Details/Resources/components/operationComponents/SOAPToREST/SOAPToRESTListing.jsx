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

import React, { Fragment, useState } from 'react';
import PropTypes from 'prop-types';
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';
import Divider from '@material-ui/core/Divider';
import CircularProgress from '@material-ui/core/CircularProgress';
import MonacoEditor from 'react-monaco-editor';
import useMediaQuery from '@material-ui/core/useMediaQuery';
import Button from '@material-ui/core/Button';
import { makeStyles } from '@material-ui/core/styles';
import EditIcon from '@material-ui/icons/Edit';
import AspectRatioIcon from '@material-ui/icons/AspectRatio';
import IconButton from '@material-ui/core/IconButton';
import Tooltip from '@material-ui/core/Tooltip';
import { capitalizeFirstLetter } from 'AppData/stringFormatter';
import isEmpty from 'lodash/isEmpty';

import PolicyEditor from './PolicyEditor';

const useStyles = makeStyles({
    root: {
        width: '100%',
        overflowX: 'auto',
    },
    table: {
        minWidth: 650,
    },
});

/**
 *
 * Renders the operation parameters section
 * @export
 * @param {*} props
 * @returns
 */
export default function SOAPToRESTListing(props) {
    const {
        operation, operationsDispatcher, target, verb, disableUpdate, api,
    } = props;
    // Get use preference from OS https://material-ui.com/customization/palette/#user-preference
    const prefersDarkMode = useMediaQuery('(prefers-color-scheme: dark)');
    const [openEditor, setOpenEditor] = useState(false);
    const { resourcePolicies } = api;
    const editorOptions = {
        selectOnLineNumbers: true,
        readOnly: true,
        smoothScrolling: true,
        wordWrap: 'on',
    };
    if (isEmpty(resourcePolicies)) {
        return (
            <Grid item>
                <CircularProgress disableShrink />
            </Grid>
        );
    }
    const editorProps = {
        language: 'xml',
        height: 'calc(100vh)',
        theme: prefersDarkMode ? 'vs-dark' : 'vs',
        value: resourcePolicies.in[target.slice(1)][verb].content,
        options: editorOptions,
    };
    return (
        <Fragment>
            <Grid item xs={12} md={12}>
                <Typography variant='subtitle1'>
                    Mediation
                    <Divider variant='middle' />
                </Typography>
            </Grid>
            <Grid item md={1} />
            <Grid item md={11}>
                <Button onClick={() => setOpenEditor(true)} variant='outlined' size='small' color='primary'>
                    Edit <EditIcon />
                </Button>
            </Grid>
            <Grid item xs={12}>
                <MonacoEditor {...editorProps} />
            </Grid>
            <PolicyEditor monacoProps={editorProps} onClose={() => setOpenEditor(false)} open={openEditor} />
        </Fragment>
    );
}

SOAPToRESTListing.defaultProps = {
    disableUpdate: false,
};
SOAPToRESTListing.propTypes = {
    operation: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({}).isRequired,
    operationsDispatcher: PropTypes.func.isRequired,
    target: PropTypes.string.isRequired,
    verb: PropTypes.string.isRequired,
    disableUpdate: PropTypes.bool,
};
