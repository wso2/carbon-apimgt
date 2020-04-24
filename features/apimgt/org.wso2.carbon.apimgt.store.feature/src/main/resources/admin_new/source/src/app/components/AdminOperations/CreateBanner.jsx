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
import InlineMessage from 'AppComponents/Shared/InlineMessage';
import { Link } from 'react-router-dom';
import Typography from '@material-ui/core/Typography';
import 'react-tagsinput/react-tagsinput.css';
import Button from '@material-ui/core/Button';
import withStyles from '@material-ui/core/styles/withStyles';

const styles = (theme) => ({
    root: {
        paddingTop: 0,
        paddingLeft: 0,
    },
    buttonProgress: {
        position: 'relative',
        margin: theme.spacing(1),
    },
    headline: {
        paddingTop: theme.spacing(1.25),
        paddingLeft: theme.spacing(2.5),
    },
    heading: {
        flexGrow: 1,
        marginTop: 10,
    },
    titleWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: theme.spacing(2),
    },
    mainTitle: {
        paddingLeft: 0,
    },
    button: {
        textDecoration: 'none',
        color: theme.palette.getContrastText(theme.palette.primary.main),
        marginLeft: theme.spacing(1),
    },
    buttonIcon: {
        marginRight: theme.spacing(1),
    },
    content: {
        margin: `${theme.spacing(2)}px 0 ${theme.spacing(2)}px 0`,
    },
    head: {
        fontWeight: 200,
    },
    contentWrapper: {
        maxWidth: theme.custom.contentAreaWidth,
        paddingLeft: theme.spacing(3),
        paddingTop: theme.spacing(3),
    },
});

const CreateBanner = (props) => {
    const { classes, title, description, buttonText, to } = props;
    return (
        <div className={classes.contentWrapper}>
            <InlineMessage type='info' height={140}>
                <div className={classes.contentWrapper}>
                    <Typography
                        variant='h5'
                        component='h3'
                        className={classes.head}
                    >
                        {title}
                    </Typography>
                    <Typography component='p' className={classes.content}>
                        {description}
                    </Typography>
                    <div className={classes.actions}>
                        <Link to={to}>
                            <Button
                                variant='contained'
                                color='primary'
                                className={classes.button}
                            >
                                {buttonText}
                            </Button>
                        </Link>
                    </div>
                </div>
            </InlineMessage>
        </div>
    );
};

export default withStyles(styles)(CreateBanner);
