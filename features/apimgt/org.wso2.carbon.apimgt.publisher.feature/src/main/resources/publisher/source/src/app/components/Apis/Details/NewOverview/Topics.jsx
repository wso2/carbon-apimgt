
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
import { withStyles, withTheme } from '@material-ui/core/styles';
import PropTypes from 'prop-types';
import Chip from '@material-ui/core/Chip';
import { FormattedMessage } from 'react-intl';
import Box from '@material-ui/core/Box';
import { Link } from 'react-router-dom';
import LaunchIcon from '@material-ui/icons/Launch';
import Grid from '@material-ui/core/Grid';

import Typography from '@material-ui/core/Typography';
import Api from 'AppData/api';
import { doRedirectToLogin } from 'AppComponents/Shared/RedirectToLogin';

function RenderMethodBase(props) {
    const { theme, method } = props;
    let chipColor = theme.custom.resourceChipColors ? theme.custom.resourceChipColors[method] : null;
    let chipTextColor = '#000000';
    if (!chipColor) {
        console.log('Check the theme settings. The resourceChipColors is not populated properly');
        chipColor = '#cccccc';
    } else {
        chipTextColor = theme.palette.getContrastText(theme.custom.resourceChipColors[method]);
    }
    return (
        <Chip
            label={method.toUpperCase()}
            style={{
                backgroundColor: chipColor, color: chipTextColor, height: 20, marginRight: 5,
            }}
        />
    );
}

RenderMethodBase.propTypes = {
    method: PropTypes.string.isRequired,
    theme: PropTypes.shape({}).isRequired,
    classes: PropTypes.shape({}).isRequired,
};

const RenderMethod = withTheme(RenderMethodBase);

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
            paths: null,
            topics: []
        };
        this.restApi = new Api();
    }

    /**
     *
     */
    componentDidMount() {
        const { api } = this.props;
        const { id, operations } = api;

        let topics = operations.map((op) => {
            return {
                name: op.target,
                type: op.verb.toUpperCase()
            };
        });
        this.setState({ topics });

        // const promisedAPI = this.restApi.getSwagger(id);
        // promisedAPI
        //     .then((response) => {
        //         if (response.obj.paths !== undefined) {
        //             this.setState({ paths: response.obj.paths });
        //         }
        //     })
        //     .catch((error) => {
        //         if (process.env.NODE_ENV !== 'production') console.log(error);
        //         const { status } = error;
        //         if (status === 404) {
        //             this.setState({ notFound: true });
        //         } else if (status === 401) {
        //             doRedirectToLogin();
        //         }
        //     });
    }

    /**
     *
     */
    render() {
        // const { paths } = this.state;
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
        // if (!paths) {
        //     return (
        //         <div>
        //             <FormattedMessage
        //                 id='Apis.Details.NewOverview.Resources.loading'
        //                 defaultMessage='loading...1'
        //             />
        //         </div>
        //     );
        // }
        const { classes, parentClasses, api } = this.props;
        return (
            <>
                <div className={parentClasses.titleWrapper}>
                    <Typography variant='h5' component='h3' className={parentClasses.title}>
                        <FormattedMessage
                            id='Apis.Details.NewOverview.Resources.resources'
                            defaultMessage='Topics'
                        />
                    </Typography>
                </div>
                <Box p={1}>
                    <div>
                        {
                            // /className={classes.contentWrapper}
                            this.state.topics.map((topic) => {
                                return (
                                    <div className={classes.root}>
                                        <Grid container spacing={1}>
                                            <Grid item xs={12}>
                                                <Grid container direction='row' spacing={1}>
                                                    <Grid item>
                                                        <Typography className={classes.heading} variant='body1'>
                                                            {topic.name}
                                                        </Typography>
                                                    </Grid>
                                                    <Grid item>
                                                        <RenderMethod method={topic.type} />
                                                    </Grid>
                                                </Grid>
                                            </Grid>
                                            { api.type !== 'WEBSUB' && (
                                                <Grid container xs={12} style={{ paddingLeft: 30 }} spacing={1}>
                                                    <Grid item xs={2}>
                                                        <Typography
                                                            component='p'
                                                            variant='subtitle2'
                                                            className={classes.subtitle}
                                                        >
                                                            Production Endpoint
                                                        </Typography>
                                                    </Grid>
                                                    <Grid item xs={10}>
                                                        {
                                                            topic.endpoint.production.url ? (
                                                                <Typography
                                                                    component='p'
                                                                    variant='body1'
                                                                    className={classes.url}
                                                                >
                                                                    {topic.endpoint.production.url}
                                                                </Typography>
                                                            ) : (
                                                                <Typography
                                                                    component='p'
                                                                    variant='body1'
                                                                    className={classes.notConfigured}
                                                                >
                                                                    -
                                                                </Typography>
                                                            )
                                                        }
                                                    </Grid>
                                                    <Grid item xs={2}>
                                                        <Typography
                                                            component='p'
                                                            variant='subtitle2'
                                                            className={classes.subtitle}
                                                        >
                                                            Sandbox Endpoint
                                                        </Typography>
                                                    </Grid>
                                                    <Grid item xs={10}>
                                                        {
                                                            topic.endpoint.sandbox.url ? (
                                                                <Typography
                                                                    component='p'
                                                                    variant='body1'
                                                                    className={classes.url}
                                                                >
                                                                    {topic.endpoint.sandbox.url}
                                                                </Typography>
                                                            ) : (
                                                                <Typography
                                                                    component='p'
                                                                    variant='body1'
                                                                    className={classes.notConfigured}
                                                                >
                                                                    -
                                                                </Typography>
                                                            )
                                                        }
                                                    </Grid>
                                                </Grid>
                                            )}
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
