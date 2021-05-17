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
import PropTypes from 'prop-types';
import { Link } from 'react-router-dom';
import Button from '@material-ui/core/Button';
import Box from '@material-ui/core/Box';
import Typography from '@material-ui/core/Typography';
import IconButton from '@material-ui/core/IconButton';
import List from '@material-ui/icons/List';
import GridOn from '@material-ui/icons/GridOn';
import { withStyles } from '@material-ui/core/styles';
import { FormattedMessage } from 'react-intl';
import ButtonGroup from '@material-ui/core/ButtonGroup';
import VerticalDivider from 'AppComponents/Shared/VerticalDivider';
import APICreateMenu from './APICreateMenu';

const styles = (theme) => ({
    button: {
        margin: theme.spacing(1),
        marginBottom: 0,
    },
    root: {
        height: 70,
        background: theme.palette.background.paper,
        borderBottom: 'solid 1px ' + theme.palette.grey.A200,
        display: 'flex',
    },
    mainTitleWrapper: {
        paddingLeft: 35,
        display: 'flex',
        justifyContent: 'center',
        flexDirection: 'column',
    },
    APICreateMenu: {
        flexGrow: 1,
        display: 'flex',
        alignItems: 'center',
    },
});

/**
 *
 * @param props
 * @returns {*}
 */
function getTitleForArtifactType(props, count) {
    const {
        isAPIProduct, query,
    } = props;
    const isSingular = count === 1;
    if (query) {
        return isSingular ? (
            <FormattedMessage
                id='Apis.Listing.components.TopMenu.search.results.singular'
                defaultMessage='Search result'
            />
        ) : (
            <FormattedMessage id='Apis.Listing.components.TopMenu.search.results' defaultMessage='Search results' />
        );
    } else if (isAPIProduct) {
        return isSingular ? (
            <FormattedMessage
                id='Apis.Listing.components.TopMenu.apiproduct.singular'
                defaultMessage='API product'
            />
        ) : (
            <FormattedMessage
                id='Apis.Listing.components.TopMenu.apiproducts.results'
                defaultMessage='API products'
            />
        );
    } else {
        return isSingular ? (
            <FormattedMessage id='Apis.Listing.components.TopMenu.api.singular' defaultMessage='API' />
        ) : (
            <FormattedMessage id='Apis.Listing.components.TopMenu.apis' defaultMessage='APIs' />
        );
    }
}

/**
 *
 * Renders the top menu
 * @param {*} props
 * @returns JSX
 */
function TopMenu(props) {
    const {
        classes, data, setListType, count, isAPIProduct, listType, showToggle, query,
    } = props;
    if (count > 0) {
        return (
            <div className={classes.root}>
                <div className={classes.mainTitleWrapper}>
                    {data && (
                        <>
                            <Typography variant='h5' className={classes.mainTitle} component='div'>
                                {isAPIProduct && (
                                    <FormattedMessage
                                        id='Apis.Listing.components.TopMenu.apiproducts'
                                        defaultMessage='API Products'
                                    />
                                )}
                                { query && (
                                    <FormattedMessage
                                        id='Apis.Listing.components.TopMenu.unified.search'
                                        defaultMessage='Unified search'
                                    />
                                )}
                                { !query && !isAPIProduct && (
                                    <FormattedMessage id='Apis.Listing.components.TopMenu.apis' defaultMessage='APIs' />
                                )}
                            </Typography>
                            <Box
                                fontFamily='fontFamily'
                                fontSize='body1.fontSize'
                                display='flex'
                            >
                                <FormattedMessage
                                    id='Apis.Listing.components.TopMenu.displaying'
                                    defaultMessage='Total:'
                                />
                                <Box
                                    id='itest-apis-listing-total'
                                    fontWeight='fontWeightBold'
                                    px={0.5}
                                    mb={0.5}
                                >
                                    {count}
                                </Box>
                                {getTitleForArtifactType(props, count)}
                            </Box>
                        </>
                    )}
                </div>
                <VerticalDivider height={70} />
                <div className={classes.APICreateMenu}>
                    {isAPIProduct && (
                        <Link to='/api-products/create'>
                            <Button variant='contained' color='primary'>
                                <FormattedMessage
                                    id='Apis.Listing.components.TopMenu.create.an.api.product'
                                    defaultMessage='Create an API Product'
                                />
                            </Button>
                        </Link>
                    )}
                    {!query && !isAPIProduct && (
                        <APICreateMenu>
                            <FormattedMessage
                                id='Apis.Listing.components.TopMenu.create.api'
                                defaultMessage='Create API'
                            />
                        </APICreateMenu>
                    )}
                </div>
                {showToggle && (
                    <Box height={32} m='auto' mr={8}>
                        <ButtonGroup color='primary' aria-label='outlined primary button group'>
                            <IconButton onClick={() => setListType('grid')} aria-label='list'>
                                <GridOn color={listType === 'grid' ? 'primary' : 'disabled'} />
                            </IconButton>
                            <IconButton onClick={() => setListType('list')} aria-label='grid'>
                                <List color={listType === 'list' ? 'primary' : 'disabled'} />
                            </IconButton>
                        </ButtonGroup>
                    </Box>
                )}
            </div>
        );
    } else {
        return null;
    }
}

TopMenu.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    setListType: PropTypes.func.isRequired,
    listType: PropTypes.string.isRequired,
    data: PropTypes.arrayOf(PropTypes.object).isRequired,
    count: PropTypes.number.isRequired,
    theme: PropTypes.shape({
        custom: PropTypes.shape({}),
    }).isRequired,
    isAPIProduct: PropTypes.bool.isRequired,
    showToggle: PropTypes.bool.isRequired,
};

export default withStyles(styles)(TopMenu);
