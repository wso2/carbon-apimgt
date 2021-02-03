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
import Hidden from '@material-ui/core/Hidden';
import { Link as MUILink } from '@material-ui/core';
import green from '@material-ui/core/colors/green';
import { makeStyles } from '@material-ui/styles';
import { Link } from 'react-router-dom';
import Typography from '@material-ui/core/Typography';
import Box from '@material-ui/core/Box';
import Grid from '@material-ui/core/Grid';
import CircularProgress from '@material-ui/core/CircularProgress';
import Configurations from 'Config';
import MenuButton from 'AppComponents/Shared/MenuButton';
import { FormattedMessage } from 'react-intl';
import AuthManager from 'AppData/AuthManager';

const useStyles = makeStyles(() => ({
    buttonProgress: {
        color: green[500],
        position: 'relative',
    },
    links: {
        cursor: 'pointer',
    },
    root: {
        marginTop: 0,
        '& a': {
            color: '#34679D',
        },
        '& a:visited': {
            color: '#34679D',
        },
    },
}));

const APICreateMenu = (props) => {
    const classes = useStyles();
    const { handleDeploySample, deploying } = props;
    const createTypes = (

        <Grid container spacing={5} className={classes.root}>
            <Hidden mdUp><Box mt={3}>xxxxs</Box></Hidden>

            {(deploying !== null && handleDeploySample !== null) && (
                <Grid item xl={3}>
                    <Box textAlign='center' mb={2}>
                        <Typography variant='h6'>
                            <FormattedMessage
                                id='Apis.Listing.SampleAPI.SampleAPI.create.new'
                                defaultMessage='Create an API'
                            />
                        </Typography>
                    </Box>
                    <Box textAlign='center'>
                        <Typography variant='body2'>
                            <FormattedMessage
                                id='Apis.Listing.SampleAPI.SampleAPI.create.new.description'
                                defaultMessage={`API creation is the process of linking an existing 
                        backend API backend API implementation to the API Publisher, 
                        so that you can manage and monitor the API’s lifecycle, documentation, 
                        security, community, and subscriptions Alternatively, you can provide 
                        the API implementation in-line in the API Publisher itself.`}
                            />
                        </Typography>
                    </Box>
                </Grid>
            )}
            <Grid item xs={12} md={12}>
                <Box pl={1} pr={2} pb={2}>
                    <Grid container>
                        <Grid item xs={12} sm={6} md={3} lg={3}>
                            <Box textAlign='center' mt={2}>
                                <Typography variant='subtitle' component='div'>
                                    <FormattedMessage
                                        id='Apis.Listing.SampleAPI.SampleAPI.rest.api'
                                        defaultMessage='REST API'
                                    />
                                </Typography>
                                <img
                                    src={Configurations.app.context
                                + '/site/public/images/landing-icons/restapi.svg'}
                                    alt='Rest API'
                                />
                                <Box mt={2}>
                                    <Typography variant='body1'>
                                        <Link
                                            id='itest-id-createdefault'
                                            to='/apis/create/rest'
                                            className={classes.links}
                                        >
                                            <FormattedMessage
                                                id='Apis.Listing.SampleAPI.SampleAPI.rest.api.scratch.title'
                                                defaultMessage='Start From Scratch'
                                            />
                                        </Link>
                                    </Typography>
                                    <Typography variant='body2'>
                                        <FormattedMessage
                                            id='Apis.Listing.SampleAPI.SampleAPI.rest.api.scratch.content'
                                            defaultMessage='Design and prototype a new REST API'
                                        />
                                    </Typography>
                                </Box>
                                <Box mt={2}>
                                    <Typography variant='body1'>

                                        <Link
                                            id='itest-id-createdefault'
                                            to='/apis/create/openapi'
                                            className={classes.links}
                                        >
                                            <FormattedMessage
                                                id='Apis.Listing.SampleAPI.SampleAPI.rest.api.import.open.title'
                                                defaultMessage='Import Open API'
                                            />
                                        </Link>
                                    </Typography>
                                    <Typography variant='body2'>
                                        <FormattedMessage
                                            id='Apis.Listing.SampleAPI.SampleAPI.rest.api.import.open.content'
                                            defaultMessage='Upload definition or provide the url'
                                        />
                                    </Typography>
                                </Box>
                                {(deploying !== null && handleDeploySample !== null) && (
                                    <Box mt={2}>
                                        {!deploying ? (
                                            <Typography variant='body1'>

                                                <MUILink
                                                    id='itest-id-createdefault'
                                                    onClick={this.handleDeploySample}
                                                    className={classes.links}
                                                >
                                                    <FormattedMessage
                                                        id={'Apis.Listing.SampleAPI.SampleAPI.'
                                                + 'rest.d.sample.title'}
                                                        defaultMessage='Deploy Sample API'
                                                    />
                                                </MUILink>
                                            </Typography>
                                        )
                                            : (
                                                <CircularProgress
                                                    size={24}
                                                    className={classes.buttonProgress}
                                                />
                                            )}
                                        <Typography variant='body2'>
                                            <FormattedMessage
                                                id='Apis.Listing.SampleAPI.SampleAPI.rest.d.sample.content'
                                                defaultMessage={`This is a sample API for Pizza Shack 
                                    online pizza delivery store`}
                                            />
                                        </Typography>
                                    </Box>
                                )}


                            </Box>

                        </Grid>
                        <Grid item xs={12} sm={6} md={3} lg={3}>
                            <Box textAlign='center' mt={2}>
                                <Typography variant='subtitle' component='div'>
                                    <FormattedMessage
                                        id='Apis.Listing.SampleAPI.SampleAPI.soap.api'
                                        defaultMessage='SOAP API'
                                    />
                                </Typography>
                                <img
                                    src={Configurations.app.context
                                + '/site/public/images/landing-icons/soapapi.svg'}
                                    alt='SOAP API'
                                />
                                <Box mt={2}>
                                    <Typography variant='body1'>

                                        <Link
                                            id='itest-id-createdefault'
                                            to='/apis/create/wsdl'
                                            className={classes.links}
                                        >
                                            <FormattedMessage
                                                id='Apis.Listing.SampleAPI.SampleAPI.soap.import.wsdl.title'
                                                defaultMessage='Import WSDL'
                                            />
                                        </Link>
                                    </Typography>
                                    <Typography variant='body2'>
                                        <FormattedMessage
                                            id='Apis.Listing.SampleAPI.SampleAPI.soap.import.wsdl.content'
                                            defaultMessage='Use an existing WSDL'
                                        />
                                    </Typography>
                                </Box>
                            </Box>
                        </Grid>
                        <Grid item xs={12} sm={6} md={3} lg={3}>
                            <Hidden mdUp><Box height={30} /></Hidden>
                            <Box textAlign='center' mt={2}>
                                <Typography variant='subtitle' component='div'>
                                    <FormattedMessage
                                        id='Apis.Listing.SampleAPI.SampleAPI.graphql.api'
                                        defaultMessage='GraphQL'
                                    />
                                </Typography>
                                <img
                                    src={Configurations.app.context
                                + '/site/public/images/landing-icons/graphqlapi.svg'}
                                    alt='GraphQL'
                                />
                                <Box mt={2}>
                                    <Typography variant='body1'>

                                        <Link
                                            id='itest-id-createdefault'
                                            to='/apis/create/graphQL'
                                            className={classes.links}
                                        >
                                            <FormattedMessage
                                                id='Apis.Listing.SampleAPI.SampleAPI.graphql.import.sdl.title'
                                                defaultMessage='Import GraphQL SDL'
                                            />
                                        </Link>
                                    </Typography>
                                    <Typography variant='body2'>
                                        <FormattedMessage
                                            id='Apis.Listing.SampleAPI.SampleAPI.graphql.import.sdl.content'
                                            defaultMessage='Use an existing definition'
                                        />
                                    </Typography>
                                </Box>
                            </Box>
                        </Grid>
                        <Grid item xs={12} sm={6} md={3} lg={3}>
                            <Hidden mdUp><Box height={30} /></Hidden>
                            <Box textAlign='center' mt={2}>
                                <Typography variant='subtitle' component='div'>
                                    <FormattedMessage
                                        id='Apis.Listing.SampleAPI.SampleAPI.websocket.api'
                                        defaultMessage='WebSocket API'
                                    />
                                </Typography>
                                <img
                                    src={Configurations.app.context
                                + '/site/public/images/landing-icons/websocketapi.svg'}
                                    alt='WebSocket API'
                                />
                                <Box mt={2}>
                                    <Typography variant='body1'>
                                        <Link
                                            id='itest-id-createdefault'
                                            to='/apis/create/ws'
                                            className={classes.links}
                                        >
                                            <FormattedMessage
                                                id='Apis.Listing.SampleAPI.SampleAPI.websocket.design.new.title'
                                                defaultMessage='Design New WebSocket API'
                                            />
                                        </Link>
                                    </Typography>
                                    <Typography variant='body2'>
                                        <FormattedMessage
                                            id='Apis.Listing.SampleAPI.SampleAPI.websocket.design.new.content'
                                            defaultMessage='Design and prototype a new WebSocket API'
                                        />
                                    </Typography>
                                </Box>
                            </Box>
                        </Grid>
                    </Grid>
                </Box>
            </Grid>
        </Grid>

    );
    return !AuthManager.isNotCreator() && <MenuButton {...props} menuList={createTypes} />;
};
APICreateMenu.defaultProps = {
    handleDeploySample: null,
    deploying: null,
};
APICreateMenu.propTypes = {
    children: PropTypes.oneOfType([PropTypes.element, PropTypes.array]).isRequired,
    handleDeploySample: PropTypes.func,
    deploying: PropTypes.bool,
};
export default APICreateMenu;
