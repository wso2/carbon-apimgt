/* eslint-disable react/jsx-props-no-spreading */
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
import { makeStyles } from '@material-ui/core/styles';
import Card from '@material-ui/core/Card';
import CardContent from '@material-ui/core/CardContent';
import ContentBase from 'AppComponents/AdminPages/Addons/ContentBase';
import Typography from '@material-ui/core/Typography';
import Box from '@material-ui/core/Box';
import WarningIcon from '@material-ui/icons/Warning';

const useStyles = makeStyles((theme) => ({
    root: {},
    warningIcon: {
        color: theme.palette.warning.dark,
        fontSize: 44,
    },
}));
/**
 * Adds two numbers together.
 * @param {JSON} props The first number.
 * @returns {JSX} Render the inline warning message
 */
export default function SimplePaper(props) {
    const classes = useStyles();
    const { content, title, pageProps } = props;

    return (
        <ContentBase
            {...pageProps}
            pageStyle='small'
        >
            <Card className={classes.root}>
                <CardContent>
                    <Box display='flex' flexDirection='row'>
                        <WarningIcon className={classes.warningIcon} />
                        <Typography variant='h5' component='h2'>
                            {title}
                        </Typography>
                    </Box>

                    <Typography variant='body2' color='textSecondary' component='p'>
                        {content}
                    </Typography>
                </CardContent>
            </Card>
        </ContentBase>
    );
}
