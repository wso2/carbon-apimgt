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
import CardActions from '@material-ui/core/CardActions';
import CardContent from '@material-ui/core/CardContent';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import ContentBase from 'AppComponents/AdminPages/Addons/ContentBase';

const useStyles = makeStyles((theme) => ({
    rootGrid: {
        flexGrow: 1,
    },
    paper: {
        padding: theme.spacing(2),
        textAlign: 'center',
        color: theme.palette.text.secondary,
    },
    root: {
        minWidth: 275,
    },
    bullet: {
        display: 'inline-block',
        margin: '0 2px',
        transform: 'scale(0.8)',
    },
    title: {
        fontSize: 14,
    },
    pos: {
        marginBottom: 12,
    },
}));

/**
 * Render progress inside a container centering in the container.
 * @returns {JSX} Loading animation.
 */
export default function Dashboard() {
    const classes = useStyles();
    const bull = <span className={classes.bullet}>•</span>;

    return (
        <ContentBase title='Dashboard' pageStyle='paperLess'>

            <div className={classes.rootGrid}>
                <Grid container spacing={3}>
                    <Grid item xs={12}>
                        <Card className={classes.root}>
                            <CardContent>
                                <Typography className={classes.title} color='textSecondary' gutterBottom>
                                    Word of the Day
                                </Typography>
                                <Typography variant='h5' component='h2'>
                                    be
                                    {bull}
                                    nev
                                    {bull}
                                    o
                                    {bull}
                                    lent
                                </Typography>
                                <Typography className={classes.pos} color='textSecondary'>
                                    adjective
                                </Typography>
                                <Typography variant='body2' component='p'>
                                    well meaning and kindly.
                                    <br />
                                    well meaning and kindly.
                                </Typography>
                            </CardContent>
                            <CardActions>
                                <Button size='small'>Learn More</Button>
                            </CardActions>
                        </Card>
                    </Grid>
                    <Grid item xs={6}>
                        <Card className={classes.root}>
                            <CardContent>
                                <Typography className={classes.title} color='textSecondary' gutterBottom>
                                    Word of the Day
                                </Typography>
                                <Typography variant='h5' component='h2'>
                                    be
                                    {bull}
                                    nev
                                    {bull}
                                    o
                                    {bull}
                                    lent
                                </Typography>
                                <Typography className={classes.pos} color='textSecondary'>
                                    adjective
                                </Typography>
                                <Typography variant='body2' component='p'>
                                    well meaning and kindly.
                                    <br />
                                    well meaning and kindly.
                                </Typography>
                            </CardContent>
                            <CardActions>
                                <Button size='small'>Learn More</Button>
                            </CardActions>
                        </Card>
                    </Grid>
                    <Grid item xs={6}>
                        <Card className={classes.root}>
                            <CardContent>
                                <Typography className={classes.title} color='textSecondary' gutterBottom>
                                    Word of the Day
                                </Typography>
                                <Typography variant='h5' component='h2'>
                                    be
                                    {bull}
                                    nev
                                    {bull}
                                    o
                                    {bull}
                                    lent
                                </Typography>
                                <Typography className={classes.pos} color='textSecondary'>
                                    adjective
                                </Typography>
                                <Typography variant='body2' component='p'>
                                    well meaning and kindly.
                                    <br />
                                    well meaning and kindly.
                                </Typography>
                            </CardContent>
                            <CardActions>
                                <Button size='small'>Learn More</Button>
                            </CardActions>
                        </Card>
                    </Grid>

                </Grid>
            </div>
        </ContentBase>
    );
}
