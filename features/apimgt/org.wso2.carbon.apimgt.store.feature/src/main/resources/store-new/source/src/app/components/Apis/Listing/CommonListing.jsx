/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import Typography from '@material-ui/core/Typography';
import { FormattedMessage } from 'react-intl';
import IconButton from '@material-ui/core/IconButton';
import GridIcon from '@material-ui/icons/GridOn';
import Grid from '@material-ui/core/Grid';
import List from '@material-ui/icons/List';
import { withStyles } from '@material-ui/core/styles';
import CustomIcon from '../../Shared/CustomIcon';
import Loading from '../../Base/Loading/Loading';
import ApiThumb from './ApiThumb';
import ApiTableView from './ApiTableView';

const styles = theme => ({
    rightIcon: {
        marginLeft: theme.spacing.unit,
    },
    button: {
        margin: theme.spacing.unit,
        marginBottom: 0,
    },
    buttonRight: {
        alignSelf: 'flex-end',
        display: 'flex',
    },
    ListingWrapper: {
        paddingTop: 10,
        paddingLeft: 35,
        width: theme.custom.contentAreaWidth,
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
    mainTitle: {
        paddingTop: 10,
    },
    mainTitleWrapper: {
        flexGrow: 1,
    },
    content: {
        flexGrow: 1,
    },
});

/**
 * Shared listing page
 *
 * @class CommonListing
 * @extends {Component}
 */
class CommonListing extends React.Component {
    /**
     * Constructor
     *
     * @param {*} props Properties
     */
    constructor(props) {
        super(props);
        this.state = {
            listType: props.theme.custom.defaultApiView,
        };
    }

    /**
     *
     * Switch the view between grid and list view
     * @param {String} value view type
     * @memberof CommonListing
     */
    setListType = (value) => {
        this.setState({ listType: value });
    };

    /**
     *
     * @inheritdoc
     * @returns {React.Component} @inheritdoc
     * @memberof CommonListing
     */
    render() {
        const { apis, isApiProduct, theme, classes } = this.props;
        const { listType } = this.state;
        const strokeColorMain = theme.palette.getContrastText(theme.palette.background.paper);

        return (
            <main className={classes.content}>
                <div className={classes.root}>
                    <div className={classes.mainIconWrapper}>
                        <CustomIcon strokeColor={strokeColorMain} width={42} height={42} icon='api' />
                    </div>
                    <div className={classes.mainTitleWrapper}>
                        <Typography variant='display1' className={classes.mainTitle}>
                            <FormattedMessage defaultMessage='APIs' id='Apis.Listing.Listing.apis.main' />
                        </Typography>
                        {apis && (
                            <Typography variant='caption' gutterBottom align='left'>
                                <FormattedMessage defaultMessage='Displaying' id='Apis.Listing.Listing.displaying' />
                                {apis.count}
                                <FormattedMessage defaultMessage='APIs' id='Apis.Listing.Listing.apis.count' />
                            </Typography>
                        )}
                    </div>
                    <div className={classes.buttonRight}>
                        <IconButton className={classes.button} onClick={() => this.setListType('list')}>
                            <List color={listType === 'list' ? 'primary' : 'default'} />
                        </IconButton>
                        <IconButton className={classes.button} onClick={() => this.setListType('grid')}>
                            <GridIcon color={listType === 'grid' ? 'primary' : 'default'} />
                        </IconButton>
                    </div>
                </div>

                {listType === 'grid' && <ApiTableView gridView isApiProduct={isApiProduct} />}
                {listType === 'list' && <ApiTableView gridView={false} isApiProduct={isApiProduct} />}
            </main>
        );
    }
}

CommonListing.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
};

export default withStyles(styles, { withTheme: true })(CommonListing);
