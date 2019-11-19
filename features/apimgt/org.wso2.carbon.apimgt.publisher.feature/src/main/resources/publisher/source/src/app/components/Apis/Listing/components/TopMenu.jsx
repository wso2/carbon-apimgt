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
import Typography from '@material-ui/core/Typography';
import IconButton from '@material-ui/core/IconButton';
import List from '@material-ui/icons/List';
import GridOn from '@material-ui/icons/GridOn';
import { withStyles } from '@material-ui/core/styles';
import { FormattedMessage } from 'react-intl';
import VerticalDivider from 'AppComponents/Shared/VerticalDivider';
import CustomIcon from 'AppComponents/Shared/CustomIcon';
import APICreateMenu from '../components/APICreateMenu';

const styles = theme => ({
    rightIcon: {
        marginLeft: theme.spacing(1),
    },
    button: {
        margin: theme.spacing(1),
        marginBottom: 0,
    },
    buttonRight: {
        display: 'flex',
    },
    ListingWrapper: {
        paddingTop: 10,
        paddingLeft: 35,
    },
    root: {
        height: 70,
        background: theme.palette.background.paper,
        borderBottom: 'solid 1px ' + theme.palette.grey.A200,
        display: 'flex',
    },
    mainIconWrapper: {
        paddingTop: 13,
        paddingLeft: 35,
        paddingRight: 20,
    },
    mainTitleWrapper: {
        display: 'flex',
        justifyContent: 'center',
        flexDirection: 'column',
    },
    APICreateMenu: {
        flexGrow: 1,
        display: 'flex',
        alignItems: 'center',
    },
    content: {
        flexGrow: 1,
    },
    createButton: {
        color: '#000000',
        background: '#15b8cf',
    },
});

/**
 *
 * @param props
 * @returns {*}
 */
function getTitleForArtifactType(props) {
    const {
        isAPIProduct, query,
    } = props;
    if (query) {
        return (
            <FormattedMessage id='Apis.Listing.components.TopMenu.search.results' defaultMessage='Search Result(s)' />
        );
    } else if (isAPIProduct) {
        return (
            <FormattedMessage
                id='Apis.Listing.components.TopMenu.apiproduct(s)'
                defaultMessage='API Product(s)'
            />
        );
    } else {
        return (
            <FormattedMessage id='Apis.Listing.components.TopMenu.api(s)' defaultMessage='API(s)' />
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
        classes, data, setListType, theme, count, isAPIProduct, listType,
    } = props;
    const strokeColorMain = theme.palette.getContrastText(theme.palette.background.paper);
    return (
        <div className={classes.root}>
            <div className={classes.mainIconWrapper}>
                <CustomIcon strokeColor={strokeColorMain} width={42} height={42} icon='api' />
            </div>
            <div className={classes.mainTitleWrapper}>
                {data && (
                    <React.Fragment>
                        <Typography variant='h5' className={classes.mainTitle} component='div'>
                            {isAPIProduct ? (
                                <FormattedMessage
                                    id='Apis.Listing.components.TopMenu.apiproducts'
                                    defaultMessage='API Products'
                                />
                            ) : (
                                <FormattedMessage id='Apis.Listing.components.TopMenu.apis' defaultMessage='APIs' />
                            )}
                        </Typography>
                        <Typography variant='caption' gutterBottom align='left' component='div'>
                            <FormattedMessage
                                id='Apis.Listing.components.TopMenu.displaying'
                                defaultMessage='Displaying'
                            />
                            {' '} {count} {' '}
                            {getTitleForArtifactType(props)}
                        </Typography>
                    </React.Fragment>
                )}
            </div>
            <VerticalDivider height={70} />
            <div className={classes.APICreateMenu}>
                {isAPIProduct ? (
                    <Link to='/api-products/create'>
                        <Button variant='contained' className={classes.createButton}>
                            <FormattedMessage
                                id='Apis.Listing.components.TopMenu.create.an.api.product'
                                defaultMessage='Create an API Product'
                            />
                        </Button>
                    </Link>
                ) : (
                    <APICreateMenu buttonProps={{ variant: 'contained', color: 'primary', className: classes.button }}>
                        <FormattedMessage id='Apis.Listing.components.TopMenu.create.api' defaultMessage='Create API' />
                    </APICreateMenu>
                )}
            </div>
            <div className={classes.buttonRight}>
                <IconButton
                    className={classes.button}
                    disabled={data.length === 0}
                    onClick={() => setListType('list')}
                >
                    <List color={listType === 'list' ? 'primary' : 'default'} />
                </IconButton>
                <IconButton
                    className={classes.button}
                    disabled={data.length === 0}
                    onClick={() => setListType('grid')}
                >
                    <GridOn color={listType === 'grid' ? 'primary' : 'default'} />
                </IconButton>
            </div>
        </div>
    );
}

TopMenu.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    setListType: PropTypes.func.isRequired,
    listType: PropTypes.string.isRequired,
    data: PropTypes.shape({}).isRequired,
    count: PropTypes.number.isRequired,
    theme: PropTypes.shape({
        custom: PropTypes.string,
    }).isRequired,
    isAPIProduct: PropTypes.bool.isRequired,
};

export default withStyles(styles, { withTheme: true })(TopMenu);
