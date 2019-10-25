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
import Button from '@material-ui/core/Button';
import Dialog from '@material-ui/core/Dialog';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import IconButton from '@material-ui/core/IconButton';
import Typography from '@material-ui/core/Typography';
import CloseIcon from '@material-ui/icons/Close';
import Slide from '@material-ui/core/Slide';
import MonacoEditor from 'react-monaco-editor';

const useStyles = makeStyles(theme => ({
    appBar: {
        position: 'relative',
    },
    title: {
        marginLeft: theme.spacing(2),
        flex: 1,
    },
}));

const Transition = React.forwardRef((props, ref) => {
    return <Slide in ref={ref} {...props} />;
});

/**
 *
 *
 * @export
 * @returns
 */
export default function PolicyEditor(props) {
    const classes = useStyles();
    const { open, onClose, monacoProps } = props;

    return (
        <Dialog fullScreen open={open} onClose={onClose} TransitionComponent={Transition}>
            <AppBar className={classes.appBar}>
                <Toolbar>
                    <IconButton edge='start' color='inherit' onClick={onClose} aria-label='close'>
                        <CloseIcon />
                    </IconButton>
                    <Typography variant='h6' className={classes.title}>
                        Discard
                    </Typography>
                    <Button color='inherit' onClick={() => {}}>
                        save
                    </Button>
                </Toolbar>
            </AppBar>
            <MonacoEditor {...monacoProps} />
        </Dialog>
    );
}
