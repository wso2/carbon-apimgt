/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import Typography from '@material-ui/core/Typography';
import Paper from '@material-ui/core/Paper';
import PropTypes from 'prop-types';
import { FormattedMessage } from 'react-intl';
import Grid from '@material-ui/core/Grid';
import { Link } from 'react-router-dom';
import Button from '@material-ui/core/Button';
import Box from '@material-ui/core/Box';
import Container from '@material-ui/core/Container';
import Image404 from './Custom404Image';

const ResourceNotFound = (props) => {
    const { response } = props;
    const { message = {} } = props;

    return (
        <>
            <Container maxWidth='md'>
                <Box padding={4}>
                    <Paper elevation={0}>
                        <Box padding={4}>
                            <Grid container alignItems='center' justify='center' style={{ height: '100%' }}>
                                <Grid item xs={12} md={6}>
                                    <Typography variant='h5' gutterBottom>
                                        {message.title}
                                    </Typography>
                                    <Typography variant='subtitle1' gutterBottom>
                                        {message.body}
                                        <span style={{ color: 'green' }}>
                                            {' '}
                                            {response ? response.statusText : ''}
                                            {' '}
                                        </span>
                                    </Typography>
                                    {message.more}
                                    <Box py={5}>
                                        <Box pb={2}>
                                            <Typography variant='subtitle1'>
                                                <FormattedMessage
                                                    id='Base.Errors.ResourceNotFound.more.links'
                                                    defaultMessage='You may check the links below'
                                                />
                                            </Typography>
                                        </Box>
                                        <Link to='/apis/' style={{ marginRight: 8 }}>
                                            <Button variant='contained' color='primary'>
                                                <FormattedMessage
                                                    id='Base.Errors.ResourceNotFound.api.list'
                                                    defaultMessage='API List'
                                                />
                                            </Button>
                                        </Link>
                                        <Link to='/api-products/'>
                                            <Button variant='contained' color='primary'>
                                                <FormattedMessage
                                                    id='Base.Errors.ResourceNotFound.api.product.list'
                                                    defaultMessage='API Product List'
                                                />
                                            </Button>
                                        </Link>
                                    </Box>
                                </Grid>
                                <Grid item xs={12} md={6}>
                                    {/* Image */}
                                    <Image404 style={{ fontSize: 400, fill: '#ccc' }} />
                                </Grid>
                            </Grid>
                        </Box>
                    </Paper>
                </Box>
            </Container>
        </>
    );
};

ResourceNotFound.defaultProps = {
    message: {
        title: <FormattedMessage
            id='Base.Errors.ResourceNotfound.default_tittle'
            defaultMessage='Page Not Found'
        />,
        body: <FormattedMessage
            id='Base.Errors.ResourceNotfound.default_body'
            defaultMessage='The page you are looking for is not available'
        />,
    },
};

ResourceNotFound.propTypes = {
    response: PropTypes.shape({
        statusText: PropTypes.string,
    }).isRequired,
    message: PropTypes.shape({
        title: PropTypes.oneOfType([
            PropTypes.string,
            PropTypes.instanceOf(FormattedMessage),
        ]),
        body: PropTypes.oneOfType([
            PropTypes.string,
            PropTypes.instanceOf(FormattedMessage),
        ]),
    }),
};

export default ResourceNotFound;
