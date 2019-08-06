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
import Typography from '@material-ui/core/Typography';
import IconButton from '@material-ui/core/IconButton';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import List from '@material-ui/icons/List';
import { FormattedMessage } from 'react-intl';
import GridIcon from '@material-ui/icons/GridOn';
import ResourceNotFound from '../../Base/Errors/ResourceNotFound';
import CustomIcon from '../../Shared/CustomIcon';
import ApiTableView from './ApiTableView';

/**
 *
 *
 * @param {*} theme
 */
const styles = theme => ({
    button: {
        margin: theme.spacing.unit,
        marginBottom: 0,
    },
    buttonRight: {
        alignSelf: 'flex-end',
        display: 'flex',
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
 *
 *
 * @class Listing
 * @extends {React.Component}
 */
class Listing extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            apis: null,
            value: 1,
            order: 'asc',
            orderBy: 'name',
        };
        this.state.listType = this.props.theme.custom.defaultApiView;
    }

    /**
     *
     *
     * @memberof Listing
     */
    setListType = (value) => {
        this.setState({ listType: value });
    };

    /**
     *
     *
     * @returns
     * @memberof Listing
     */
    render() {
        if (this.state.notFound) {
            return <ResourceNotFound />;
        }

        const { theme, classes } = this.props;
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
                        {this.state.apis && (
                            <Typography variant='caption' gutterBottom align='left'>
                                <FormattedMessage defaultMessage='Displaying' id='Apis.Listing.Listing.displaying' />
                                {this.state.apis.count}
                                <FormattedMessage defaultMessage='APIs' id='Apis.Listing.Listing.apis.count' />
                            </Typography>
                        )}
                    </div>
                    <div className={classes.buttonRight}>
                        <IconButton className={classes.button} onClick={() => this.setListType('list')}>
                            <List color={this.state.listType === 'list' ? 'primary' : 'default'} />
                        </IconButton>
                        <IconButton className={classes.button} onClick={() => this.setListType('grid')}>
                            <GridIcon color={this.state.listType === 'grid' ? 'primary' : 'default'} />
                        </IconButton>
                    </div>
                </div>
                {this.state.listType === 'grid' && <ApiTableView gridView />}
                {this.state.listType === 'list' && <ApiTableView gridView={false} />}
            </main>
        );
    }
}

Listing.propTypes = {
    classes: PropTypes.object.isRequired,
    theme: PropTypes.object.isRequired,
};
export default withStyles(styles, { withTheme: true })(Listing);
