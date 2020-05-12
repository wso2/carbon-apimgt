/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import Toolbar from '@material-ui/core/Toolbar';
import Typography from '@material-ui/core/Typography';
import Paper from '@material-ui/core/Paper';
import Grid from '@material-ui/core/Grid';
import { withStyles } from '@material-ui/core/styles';

const styles = (theme) => ({
    root: {
        minHeight: 43,
    },
    paper: {
        maxWidth: 936,
        margin: 'auto',
        overflow: 'hidden',
    },
    smallBox: {
        maxWidth: 450,
        margin: 'auto',
        overflow: 'hidden',
    },
    searchBar: {
        borderBottom: '1px solid rgba(0, 0, 0, 0.12)',
    },
    searchInput: {
        fontSize: theme.typography.fontSize,
    },
    block: {
        display: 'block',
    },
    addUser: {
        marginRight: theme.spacing(1),
    },
    contentWrapper: {
        margin: '40px 16px',
    },
    secondaryBar: {
        zIndex: 0,
    },
    menuButton: {
        marginLeft: -theme.spacing(1),
    },
    iconButtonAvatar: {
        padding: 4,
    },
    link: {
        textDecoration: 'none',
        color: 'rgba(255, 255, 255, 0.7)',
        '&:hover': {
            color: theme.palette.common.white,
        },
    },
    button: {
        borderColor: 'rgba(255, 255, 255, 0.7)',
    },
    main: {
        flex: 1,
        padding: theme.spacing(6, 4),
        background: '#eaeff1',
    },
    gridRoot: {
        paddingLeft: 0,
    },
});

/**
 * Render base for content.
 * @param {JSON} props .
 * @returns {JSX} Header AppBar components.
 */
function ContentBase(props) {
    const {
        classes, title, children, help, pageStyle,
    } = props;

    return (
        <>
            <Toolbar className={classes.root}>
                <Grid container alignItems='center' spacing={1} classes={{ root: classes.gridRoot }}>
                    <Grid item xs>
                        <Typography color='inherit' variant='h5' component='h1'>
                            {title}
                        </Typography>
                    </Grid>
                    {help && (
                        <Grid item>
                            {help}
                        </Grid>
                    )}
                </Grid>
            </Toolbar>
            <main className={classes.main}>
                {pageStyle && (pageStyle === 'half') && (
                    <Paper className={classes.paper}>
                        {children}
                    </Paper>
                )}
                {pageStyle && (pageStyle === 'paperLess') && (
                    <>
                        {children}
                    </>
                )}
                {pageStyle && (pageStyle === 'small') && (
                    <div className={classes.smallBox}>
                        {children}
                    </div>
                )}
                {pageStyle && (pageStyle === 'full') && (
                    <Paper>
                        {children}
                    </Paper>
                )}
            </main>
        </>
    );
}
ContentBase.defaultProps = {
    pageStyle: null,
};
ContentBase.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    help: PropTypes.element.isRequired,
    title: PropTypes.string.isRequired,
    children: PropTypes.element.isRequired,
    pageStyle: PropTypes.string,
};

export default withStyles(styles)(ContentBase);
