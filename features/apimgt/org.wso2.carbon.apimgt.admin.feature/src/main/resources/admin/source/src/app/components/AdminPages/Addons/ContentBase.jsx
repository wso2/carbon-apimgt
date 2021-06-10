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
import Box from '@material-ui/core/Box';
import Grid from '@material-ui/core/Grid';
import { makeStyles } from '@material-ui/core/styles';
import clsx from 'clsx';

const useStyles = makeStyles(() => {
    const minHeight = 43;
    return ({
        pageTitle: {
            minHeight,
            backgroundColor: '#f6f6f6',
        },
        root: {
            flexGrow: 1,
            minHeight: `calc(100vh - (${minHeight + 57}px))`,
            backgroundColor: '#eaeff1',
        },
    });
});

/**
 * Render base for content.
 * @param {JSON} props -  Component properties
 * @param {string} props.title -  Page title
 * @param {string} props.pageDescription -  Page description
 * @param {object} props.children -  Page content
 * @param {object} props.help -  Page help component
 * @param {string} props.backgroundColor -  Page background color in #xxxxxx format
 * @param {object} props.paperProps -  Page background color in #xxxxxx format
 * @param {object} props.pageStyle - @deprecated Page style one of 'half' 'full' or 'small'
 * @returns {JSX} Header AppBar components.
 */
function ContentBase(props) {
    const classes = useStyles();
    const {
        title, pageDescription, children, help, width, pageStyle, PaperProps, classes: classesProp, paperLess, totalItems,
    } = props;
    let size = 8;// default half/medium
    if ([width, pageStyle].includes('small')) {
        size = 5;
    } else if ([width, pageStyle].includes('full')) {
        size = 11;
    }
    return (
        <div className={clsx(classesProp.root, classes.root)}>
            <Grid
                container
                direction='row'
                justify='center'
                alignItems='flex-start'
            >
                <Grid item xs={12}>
                    <Toolbar className={clsx(classesProp.pageTitle, classes.pageTitle)}>
                        <Grid container alignItems='center' spacing={1}>
                            <Grid item xs>
                                <Typography color='inherit' variant='h5' component='h1'>
                                    {title}
                                </Typography>
                                {totalItems && (<Typography color='inherit' variant='caption' component='div'>
                                    Application Count - {totalItems}
                                </Typography>)}
                                <Box pb={1}>
                                    {
                                        pageDescription !== null && (
                                            <Typography variant='body2' color='textSecondary' component='p'>
                                                {pageDescription}
                                            </Typography>
                                        )
                                    }
                                </Box>
                            </Grid>
                            <Grid item>
                                {help}
                            </Grid>
                        </Grid>
                    </Toolbar>
                </Grid>
                <Grid item xs={11} sm={size}>
                    <Box pt={6} position='relative'>
                        {pageStyle === 'paperLess' || paperLess ? children : (
                            <Paper {...PaperProps}>
                                {children}
                            </Paper>
                        )}
                    </Box>
                </Grid>

            </Grid>
        </div>
    );
}
ContentBase.defaultProps = {
    width: 'medium',
    PaperProps: {},
    classes: {},
    pageStyle: 'half',
    paperLess: false,
    pageDescription: null,
};
ContentBase.propTypes = {
    help: PropTypes.element.isRequired,
    title: PropTypes.string.isRequired,
    pageDescription: PropTypes.string,
    children: PropTypes.element.isRequired,
    width: PropTypes.oneOf(['medium', 'full', 'small']),
    pageStyle: PropTypes.oneOf(['half', 'full', 'small']), // @deprecated
    PaperProps: PropTypes.shape({ elevation: PropTypes.number }),
    /**
   * Override or extend the styles applied to the component.
   * See [CSS API](#css) below for more details.
   */
    classes: PropTypes.shape({ root: PropTypes.string }),
    paperLess: PropTypes.bool,
};

export default ContentBase;
