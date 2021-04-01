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
import { withStyles, makeStyles } from '@material-ui/core/styles';
import PropTypes from 'prop-types';
import { FormattedMessage } from 'react-intl';
import Box from '@material-ui/core/Box';
import { Link } from 'react-router-dom';
import LaunchIcon from '@material-ui/icons/Launch';
import Grid from '@material-ui/core/Grid';
import Button from '@material-ui/core/Button';

import Typography from '@material-ui/core/Typography';
import Api from 'AppData/api';
import { doRedirectToLogin } from 'AppComponents/Shared/RedirectToLogin';

const styles = {
    root: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: 10,
    },
    heading: {
        marginRight: 20,
    },
    contentWrapper: {
        maxHeight: '125px',
        overflowY: 'auto',
    },
};

function VerbElement(props) {
    const {
        verb,
    } = props;

    const useMenuStyles = makeStyles((theme) => {
        const backgroundColor = theme.custom.resourceChipColors[verb.toLowerCase()];
        return {
            customButton: {
                backgroundColor: '#ffffff',
                borderColor: backgroundColor,
                color: backgroundColor,
                width: theme.spacing(2),
            },
        };
    });
    const classes = useMenuStyles();
    return (
        <Button disableFocusRipple variant='outlined' className={classes.customButton} size='small'>
            {verb.toUpperCase()}
        </Button>
    );
}

/**
 * Topics component
 */
class Topics extends React.Component {
    /**
     *
     * @param {*} props
     */
    constructor(props) {
        super(props);
        this.state = {
            topics: [],
        };
        this.restApi = new Api();
    }

    /**
     *
     */
    componentDidMount() {
        this.restApi.getAsyncAPIDefinition(this.props.api.id)
            .then((response) => {
                const topics = [];
                Object.entries(response.body.channels).forEach(([name, topic]) => {
                    if (topic.subscribe) {
                        topics.push({ name, type: 'subscribe' });
                    }

                    if (topic.publish) {
                        topics.push({ name, type: 'publish' });
                    }
                });
                this.setState({ topics });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const { status } = error;
                if (status === 404) {
                    this.setState({ notFound: true });
                } else if (status === 401) {
                    doRedirectToLogin();
                }
            });
    }

    /**
     *
     */
    render() {
        if (this.state.notFound) {
            return (
                <div>
                    <FormattedMessage
                        id='Apis.Details.NewOverview.Resources.resource.not.found'
                        defaultMessage='resource not found...'
                    />
                </div>
            );
        }
        const { classes, parentClasses, api } = this.props;

        return (
            <>
                <div className={parentClasses.titleWrapper}>
                    <Typography variant='h5' component='h3' className={parentClasses.title}>
                        <FormattedMessage
                            id='Apis.Details.NewOverview.async.api.topics'
                            defaultMessage='Topics'
                        />
                    </Typography>
                </div>
                <Box p={1}>
                    <div>
                        {
                            this.state.topics.map((topic) => {
                                return (
                                    <div className={classes.root}>
                                        <Grid container spacing={1}>
                                            <Grid item xs={12}>
                                                <Grid container direction='row' spacing={1}>
                                                    <Grid item>
                                                        <VerbElement verb={topic.type.substr(0, 3)} />
                                                    </Grid>
                                                    <Grid item>
                                                        <Typography className={classes.heading} variant='body1'>
                                                            {topic.name}
                                                        </Typography>
                                                    </Grid>
                                                </Grid>
                                            </Grid>
                                        </Grid>
                                    </div>
                                );
                            })
                        }
                    </div>
                    <Link to={'/apis/' + api.id + '/topics'}>
                        <Typography
                            className={classes.subHeading}
                            color='primary'
                            display='inline'
                            variant='caption'
                        >
                            <FormattedMessage
                                id='Apis.Details.NewOverview.Operations.ShowMore'
                                defaultMessage='Show More'
                            />
                            <LaunchIcon style={{ marginLeft: '2px' }} fontSize='small' />
                        </Typography>
                    </Link>
                </Box>
            </>
        );
    }
}
Topics.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
    history: PropTypes.shape({
        push: PropTypes.shape({}),
    }).isRequired,
    location: PropTypes.shape({
        pathname: PropTypes.shape({}),
    }).isRequired,
    parentClasses: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({ id: PropTypes.string }).isRequired,
};

export default withStyles(styles)(Topics);
