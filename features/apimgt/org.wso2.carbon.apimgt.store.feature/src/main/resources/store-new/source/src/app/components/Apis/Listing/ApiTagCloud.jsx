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
import { withStyles } from '@material-ui/core/styles';
import ApiTagThumb from './ApiTagThumb';

const styles = theme => ({
    root: {
        marginTop: theme.spacing.unit * 2,
        padding: theme.spacing.unit * 3,
        paddingLeft: theme.spacing.unit * 3,
        width: theme.spacing.unit * 30,
    },
    tagCloudWrapper: {
        display: 'flex',
        flexDirection: 'row',
        paddingLeft: theme.spacing.unit * 3,
    },
    listContentWrapper: {
        padding: `0 ${theme.spacing.unit * 3}px`,
    },
    mainTitle: {
        paddingTop: 10,
        paddingBottom: theme.spacing.unit * 3,
    },
    selectedTagSpacing: {
        paddingLeft: theme.spacing.unit * 3,
    },
    clickablePointer: {
        cursor: 'pointer',
    },

    tagedApisWrapper: {
        display: 'flex',
        flexDirection: 'row',
        paddingLeft: theme.spacing.unit * 3,
        flexWrap: 'wrap',
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
            selectedTag: null,
        };
        this.handleOnClick = this.handleOnClick.bind(this);
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
        const {
            classes, data, listType,
        } = this.props;
        const tagWiseURL = '/apis?limit=10&offset=0&query=tag';
        return (
            <div className={classes.tagedApisWrapper}>
                {/* Todo: api tag wise grouping */}
                {/* {theme.custom.tagWiseMode === true && data && (
                    <div className={classes.listContentWrapper}>
                        <div>
                            <div>
                                <Typography variant='h4' className={classes.mainTitle}>
                                    <FormattedMessage
                                        defaultMessage='Tags'
                                        id='Apis.Listing.ApiTagCloud.tags.heading'
                                    />
                                </Typography>
                            </div>
                            <div>
                                <TagCloud
                                    minSize={18}
                                    maxSize={40}
                                    colorOptions={Coloroptions}
                                    tags={data}
                                    shuffle={false}
                                    className={classes.clickablePointer}
                                    onClick={tag => this.handleOnClick(tag)}
                                />
                            </div>
                        </div>
                    </div>
                )}
                { theme.custom.tagWiseMode === true && selectedTag && (
                    <div className={classes.selectedTagSpacing}>
                        <Typography variant='h4' className={classes.mainTitle}>
                            {' ('}
                            {selectedTag}
                            {') '}
                        </Typography>
                    </div>
                )}
                <div /> */}
                {Object.keys(data).map((key) => {
                    return <ApiTagThumb tag={data[key]} listType={listType} path={tagWiseURL} />;
                })}
            </div>
        );
    }
}

ApiTagCloud.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    tag: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
    listType: PropTypes.string.isRequired,
    data: PropTypes.shape({}).isRequired,
};

export default withStyles(styles, { withTheme: true })(ApiTagCloud);
