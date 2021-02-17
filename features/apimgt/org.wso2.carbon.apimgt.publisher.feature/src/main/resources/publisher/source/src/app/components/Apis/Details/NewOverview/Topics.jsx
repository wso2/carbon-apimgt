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

function RenderMethodBase(props) {
    const { theme, methods } = props;
    return methods.map((method) => {
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
    });
}

RenderMethodBase.propTypes = {
    methods: PropTypes.arrayOf(PropTypes.string).isRequired,
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
            topics: [],
        };
        this.restApi = new Api();
    }

    /**
     *
     */
    componentDidMount() {
        const { api } = this.props;
        const { operations } = api;

        const topics = operations.map((op) => {
            return {
                name: op.target,
                // type: op.verb.toUpperCase(),
            };
        });
        this.setState({ topics });
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
                                const methods = ['subscribe'];
                                if (api.type === 'WS') {
                                    methods.push('publish');
                                }
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
                                                        <RenderMethod methods={methods} />
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
