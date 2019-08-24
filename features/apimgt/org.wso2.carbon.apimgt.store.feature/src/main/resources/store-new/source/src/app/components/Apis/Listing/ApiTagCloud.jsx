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
import { TagCloud } from 'react-tagcloud';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import { Link } from 'react-router-dom';
import Paper from '@material-ui/core/Paper';
import { FormattedMessage } from 'react-intl';
import API from 'AppData/api';
import { ApiContext } from 'AppComponents/Apis/Details/ApiContext';
import ApiTableView from './ApiTableView';

const styles = theme => ({
    root: {
        marginTop: theme.spacing.unit * 2,
        padding: theme.spacing.unit * 3,
    },
    tagCloudWrapper: {
        display: 'flex',
        flexDirection: 'row',
        flexWrap: 'wrap',
        paddingLeft: theme.spacing.unit * 3,
    },
    listContentWrapper: {
        padding: `0 ${theme.spacing.unit * 3}px`,
    },
    mainTitle: {
        paddingTop: 10,
        paddingBottom: theme.spacing.unit * 3,
    },
    tags: {
        paddingLeft: theme.spacing.unit * 3,
        width: theme.spacing.unit * 30,
    },
    selectedTagSpacing: {
        paddingLeft: theme.spacing.unit * 3,
    },
    clickablePointer: {
        cursor: 'pointer',
    },
});

/**
 * Component used to handle API Tag Cloud
 * @class ApiTagCloud
 * @extends {React.Component}
 * @param {any} value @inheritDoc
 */
class ApiTagCloud extends React.Component {
    /**
     * @param {*} props properties
     */
    constructor(props) {
        super(props);
        this.state = {
            allTags: null,
            selectedTag: null,
        };
        this.handleOnClick = this.handleOnClick.bind(this);
    }

    /**
     * @memberof ApiTagCloud
     */
    componentDidMount() {
        const api = new API();
        const promisedTags = api.getAllTags();
        promisedTags
            .then((response) => {
                if (response.body.count !== 0) {
                    this.setState({ allTags: response.body.list });
                }
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
            });
    }

    /**
     *
     * @param {String} tag selected tag
     * @memberof ApiTagCloud
     */
    handleOnClick = (tag) => {
        this.setState({ selectedTag: tag.value });
    };

    /**
     * @returns {React.Component}
     * @memberof ApiTagCloud
     */
    render() {
        const { classes, listType, apiType } = this.props;
        const { allTags, selectedTag } = this.state;
        const options = {
            luminosity: 'dark',
            hue: 'blue',
        };
        return (
            <div className={classes.tagCloudWrapper}>
                {allTags && (
                    <Paper className={classes.root}>
                        <div className={classes.tags}>
                            <div>
                                <Typography variant='display1' className={classes.mainTitle}>
                                    <FormattedMessage defaultMessage='Tags' id='Apis.Listing.ApiTagCloud.tags.heading' />
                                </Typography>
                            </div>
                            <div>
                                <TagCloud
                                    minSize={12}
                                    maxSize={35}
                                    colorOptions={options}
                                    tags={allTags}
                                    shuffle={false}
                                    className={classes.clickablePointer}
                                    onClick={tag => this.handleOnClick(tag)}
                                />
                            </div>
                        </div>
                    </Paper>
                )}
                { selectedTag && (
                    <div className={classes.selectedTagSpacing}>
                        <Typography variant='display1' className={classes.mainTitle}>
                            {' ('}
                            {selectedTag}
                            {') '}
                        </Typography>
                    </div>
                )}
                <div>
                    <div className={classes.listContentWrapper}>
                        {listType === 'grid'
                            && (
                                <ApiContext.Provider value={{ apiType }}>
                                    <ApiTableView selectedTag={selectedTag} gridView />
                                </ApiContext.Provider>
                            )}
                        {listType === 'list'
                            && (
                                <ApiContext.Provider value={{ apiType }}>
                                    <ApiTableView selectedTag={selectedTag} gridView={false} />
                                </ApiContext.Provider>
                            )}
                    </div>
                </div>
            </div>
        );
    }
}

ApiTagCloud.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    tag: PropTypes.shape({}).isRequired,
    listType: PropTypes.string.isRequired,
    apiType: PropTypes.string.isRequired,
};

export default withStyles(styles, { withTheme: true })(ApiTagCloud);
