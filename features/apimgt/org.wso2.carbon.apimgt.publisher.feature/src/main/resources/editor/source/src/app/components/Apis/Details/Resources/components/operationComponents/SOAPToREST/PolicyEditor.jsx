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
import React, { Suspense, lazy, useState } from 'react';
import PropTypes from 'prop-types';
import { makeStyles } from '@material-ui/core/styles';
import Button from '@material-ui/core/Button';
import Dialog from '@material-ui/core/Dialog';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import Fade from '@material-ui/core/Fade';
import CircularProgress from '@material-ui/core/CircularProgress';
import { useAPI } from 'AppComponents/Apis/Details/components/ApiContext';
import Alert from 'AppComponents/Shared/Alert';
import Grid from '@material-ui/core/Grid';
import Banner from 'AppComponents/Shared/Banner';
import CloseConfirmation from './CloseConfirmation';

const MonacoEditor = lazy(() => import('react-monaco-editor' /* webpackChunkName: "PolicyEditorMonaco" */));

const useStyles = makeStyles((theme) => ({
    appBar: {
        // position: 'relative',
        top: 'auto',
        bottom: 0,
    },
    title: {
        marginLeft: theme.spacing(2),
        flex: 1,
    },
}));

const Transition = React.forwardRef((props, ref) => {
    return <Fade in ref={ref} {...props} />;
});

/**
 *
 *
 * @export
 * @returns
 */
export default function PolicyEditor(props) {
    const classes = useStyles();
    const [api] = useAPI();
    const {
        open,
        onClose,
        prefersDarkMode,
        originalResourcePolicy,
        selectedPolicy,
        setPolicyContent,
        resourcePoliciesDispatcher,
        direction,
    } = props;
    const [pageError, setPageError] = useState(null);
    const [openConfirmation, setOpenConfirmation] = useState(false);
    const [saving, setSaving] = useState(false);

    const editorOptions = {
        selectOnLineNumbers: true,
        readOnly: saving,
        smoothScrolling: true,
        wordWrap: 'on',
    };
    const editorProps = {
        language: 'xml',
        height: 'calc(100vh)',
        theme: prefersDarkMode ? 'vs-dark' : 'vs',
        value: selectedPolicy.content,
        options: editorOptions,
        onChange: setPolicyContent,
    };

    /**
     *
     *
     */
    function confirmAndClose() {
        // No need to confirm if user have not done any changes
        if (selectedPolicy.content !== originalResourcePolicy.content) {
            setOpenConfirmation(true);
        } else {
            onClose();
        }
    }
    /**
     *
     *
     */
    function save() {
        setSaving(true);
        api.updateResourcePolicy(selectedPolicy)
            .then((response) => {
                Alert.success('Resource policy updated successfully');
                resourcePoliciesDispatcher({ action: 'update', data: { value: response.body, direction } });
                onClose();
            })
            .catch((error) => {
                if (error.response) {
                    Alert.error(error.response.body.description);
                    setPageError(error.response.body);
                } else {
                    // TODO add i18n ~tmkb
                    const message = error.message || 'Something went wrong while updating resource policy!';
                    Alert.error(message);
                    setPageError(message);
                }
                console.error(error);
            })
            .finally(() => setSaving(false));
    }

    return (
        <Dialog fullScreen open={open} onClose={onClose} TransitionComponent={Transition}>
            <AppBar position='fixed' color='default' className={classes.appBar}>
                <Toolbar variant='dense'>
                    <Grid container direction='row' justify='flex-start' alignItems='flex-start'>
                        <Grid item>
                            <Button
                                disabled={saving}
                                variant='outlined'
                                color='primary'
                                className={classes.title}
                                onClick={save}
                            >
                                save & close
                                {saving && <CircularProgress size={18} />}
                            </Button>
                        </Grid>
                        <Grid item>
                            <Button color='inherit' className={classes.title} onClick={confirmAndClose}>
                                close
                            </Button>
                        </Grid>
                    </Grid>
                </Toolbar>
            </AppBar>
            <Grid container direction='row' justify='center' alignItems='center'>
                {pageError && (
                    <Grid item xs={12}>
                        <Banner
                            onClose={() => setPageError(null)}
                            disableActions
                            dense
                            paperProps={{ elevation: 1 }}
                            type='error'
                            message={pageError}
                        />
                    </Grid>
                )}
            </Grid>
            <Grid item xs={12}>
                <Suspense fallback={<CircularProgress disableShrink />}>
                    <MonacoEditor {...editorProps} />
                </Suspense>
            </Grid>
            <CloseConfirmation
                open={openConfirmation}
                onClose={() => {
                    setPolicyContent(originalResourcePolicy.content);
                    setOpenConfirmation(false);
                }}
                closeEditor={onClose}
            />
        </Dialog>
    );
}
PolicyEditor.defaultProps = {
    open: false,
    onClose: () => {},
    prefersDarkMode: false,

};
PolicyEditor.propTypes = {
    open: PropTypes.bool,
    onClose: PropTypes.func,
    prefersDarkMode: PropTypes.bool,
    originalResourcePolicy: PropTypes.shape({}).isRequired,
    selectedPolicy: PropTypes.shape({}).isRequired,
    setPolicyContent: PropTypes.func.isRequired,
    resourcePoliciesDispatcher: PropTypes.func.isRequired,
    direction: PropTypes.oneOf(['in', 'out']).isRequired,
};
