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
import { Link } from 'react-router-dom';
import { withStyles } from '@material-ui/core/styles';
import Cancel from '@material-ui/icons/Cancel';
import { StarRate } from '@material-ui/icons';
import Alert from 'AppComponents/Shared/Alert';
import Api from 'AppData/api';
import AuthManager from 'AppData/AuthManager';
import StarRatingSummary from 'AppComponents/Apis/Details/StarRatingSummary';
import Rating from "@material-ui/lab/Rating";

/**
 *
 *
 * @param {*} theme
 */
const styles = theme => ({
    starRate: {
        fontSize: 30,
        color: theme.custom.infoBar.starColor,
    },
    noStarRate: {
        fontSize: 30,
        color: theme.palette.grey.A200,
    },
    removeRating: {
        fontSize: 20,
        color: 'black',
    },
    userRating: {
        display: 'flex',
        justifyContent: 'flex-start',
        alignItems: 'center',
    },
});

/**
 *
 *
 * @class StarRatingBar
 * @extends {React.Component}
 */
class StarRatingBar extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            avgRating: 0,
            userRating: 0,
            count: 0,
            total: 0,
        };
        this.getApiRating = this.getApiRating.bind(this);
        this.removeUserRating = this.removeUserRating.bind(this);
        this.doRate = this.doRate.bind(this);
    }

    /**
     *
     *
     * @memberof StarRatingBar
     */
    componentDidMount() {
        this.getApiRating();
    }

    /**
     *
     *
     * @memberof StarRatingBar
     */
    getApiRating() {
        const { apiId } = this.props;
        const user = AuthManager.getUser();
        const api = new Api();
        // get api rating
        if (user != null) {
            const promisedRating = api.getRatingFromUser(apiId, null);
            promisedRating.then((response) => {
                this.setState({
                    avgRating: response.body.avgRating,
                    userRating: response.body.userRating,
                    count: response.body.count,
                    total: response.body.pagination.total,
                });
            });
        }
    }

    /**
     *
     *
     * @param {*} rateIndex
     * @memberof StarRatingBar
     */
    doRate(rateIndex) {
        const { apiId } = this.props;
        const api = new Api();
        const ratingInfo = { rating: rateIndex };
        const promise = api.addRating(apiId, ratingInfo);
        promise
            .then(() => {
                this.getApiRating();
            })
            .catch((error) => {
                Alert.error('Error occured while adding ratings');
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
            });
    }

    /**
     *
     *
     * @memberof StarRatingBar
     */
    removeUserRating() {
        const { apiId } = this.props;
        const api = new Api();
        // remove user rating
        api.removeRatingOfUser(apiId, null)
            .then(() => this.getApiRating())
            .catch((error) => {
                Alert.error('Error occured while removing ratings');
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
            });
    }

    /**
     *
     *
     * @returns
     * @memberof StarRatingBar
     */
    render() {
        const {
            avgRating,
            userRating,
            count,
            total,
        } = this.state;
        const {
            classes,
            isEditable,
            showSummary,
            apiRating,
        } = this.props;
        return (
            <React.Fragment>
                {showSummary ? (
                    <StarRatingSummary avgRating={avgRating} reviewCount={total} returnCount={count} />
                ) : (
                    <React.Fragment>
                        {isEditable ? (
                            <React.Fragment>
                                <div className={classes.userRating}>
                                    {[1, 2, 3, 4, 5].map(i => (
                                        <Link>
                                            <StarRate
                                                key={i}
                                                className={userRating >= i
                                                    ? classes.starRate
                                                    : classes.noStarRate
                                                }
                                                onClick={() => this.doRate(i)}
                                            />
                                        </Link>
                                    ))}
                                    <Link>
                                        <Cancel
                                            className={classes.removeRating}
                                            onClick={() => this.removeUserRating()}
                                        />
                                    </Link>
                                </div>
                            </React.Fragment>
                        ) : (
                            <React.Fragment>
                                <Rating name='half-rating' value={apiRating} precision={0.1} readOnly={true} />
                            </React.Fragment>
                        )}
                    </React.Fragment>
                )}
            </React.Fragment>
        );
    }
}

StarRatingBar.defaultProps = {
    apiRating: '0',
};

StarRatingBar.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
    apiId: PropTypes.string.isRequired,
    isEditable: PropTypes.bool.isRequired,
    showSummary: PropTypes.bool.isRequired,
    apiRating: PropTypes.string,
};

export default withStyles(styles, { withTheme: true })(StarRatingBar);
