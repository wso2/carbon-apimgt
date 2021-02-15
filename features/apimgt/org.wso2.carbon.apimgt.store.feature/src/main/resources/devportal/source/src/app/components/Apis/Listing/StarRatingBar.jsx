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
import Box from '@material-ui/core/Box';
import { withStyles } from '@material-ui/core/styles';
import Cancel from '@material-ui/icons/Cancel';
import StarRate from '@material-ui/icons/StarRate';
import StarIcon from '@material-ui/icons/Star';
import StarBorderIcon from '@material-ui/icons/StarBorder';
import Alert from 'AppComponents/Shared/Alert';
import Api from 'AppData/api';
import AuthManager from 'AppData/AuthManager';
import StarRatingSummary from 'AppComponents/Apis/Details/StarRatingSummary';
import Rating from '@material-ui/lab/Rating';
import { FormattedMessage } from 'react-intl';

/**
 *
 *
 * @param {*} theme
 */
const styles = theme => ({
    starRate: {
        fontSize: 25,
        color: theme.custom.infoBar.starColor,
    },
    noStarRate: {
        fontSize: 30,
        color: theme.palette.grey.A200,
    },
    iconFilled: {
        color: theme.custom.infoBar.starColor,
    },
    iconEmpty: {
        color: theme.custom.infoBar.starColorEmpty || '#cfcfcf',
    },
    removeRating: {
        fontSize: 20,
        color: theme.palette.getContrastText(theme.custom.infoBar.background),
    },
    userRating: {
        display: 'flex',
        justifyContent: 'flex-start',
        alignItems: 'center',
        cursor: 'pointer',
        padding: '5px',
        background: '#efefef',
        borderRadius: '3px',
        position: 'absolute',
        left: '62px',
        top: '-5px',
    },
    rateThis: {
        lineHeight: '15px',
        width: 40,
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
            showEditing: false,
        };
        this.getApiRating = this.getApiRating.bind(this);
        this.removeUserRating = this.removeUserRating.bind(this);
        this.doRate = this.doRate.bind(this);
        this.toggleEditRating = this.toggleEditRating.bind(this);
    }
    componentDidUpdate(prevProps) {
        // Typical usage (don't forget to compare props):
        if (this.props.ratingUpdate !== prevProps.ratingUpdate) {
            this.getApiRating();
        }
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
        const { apiId, setRatingUpdate } = this.props;
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
                if (setRatingUpdate) setRatingUpdate({
                    avgRating: response.body.avgRating,
                    count: response.body.count,
                    total: response.body.pagination.total
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
                Alert.error('Error occurred while adding ratings');
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
            })
            .finally(() => {
                this.toggleEditRating();
            });
    }

    /**
     *
     *
     * @memberof StarRatingBar
     */
    removeUserRating() {
        const { apiId, setRatingUpdate } = this.props;
        const api = new Api();
        // remove user rating
        api.removeRatingOfUser(apiId, null)
            .then(() => {
                this.getApiRating();
                setRatingUpdate();
            })
            .catch((error) => {
                Alert.error('Error occurred while removing ratings');
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
            })
            .finally(() => {
                this.toggleEditRating();
            });
    }

    toggleEditRating() {
        this.setState({ showEditing: !this.state.showEditing });
    }
    /**
     *
     *
     * @returns
     * @memberof StarRatingBar
     */
    render() {
        const {
            avgRating, userRating, count, total, showEditing
        } = this.state;
        const {
            classes, isEditable, showSummary, apiRating,
        } = this.props;
        const apiRatingNumber = parseFloat(apiRating);
        return (
            <>
                {showSummary ? (
                    <StarRatingSummary avgRating={avgRating} reviewCount={total} returnCount={count} />
                ) : (
                        <>
                            {isEditable ? (
                                <Box position='relative'>
                                    <Box onClick={this.toggleEditRating} display='flex' style={{ cursor: 'pointer' }}>
                                        {(userRating === 0) 
                                        ? (<StarBorderIcon style={{ fontSize: 30 }} />) 
                                        : (<StarIcon style={{ fontSize: 30, color: '#75d5fa'}} />)}
                                        <Typography variant='body2' className={classes.rateThis}>
                                            {(userRating === 0) ? (
                                                <FormattedMessage defaultMessage='Rate This' id='Apis.Listing.StarRatingBar.rate.this' />
                                            ) : (
                                                    <><Box fontSize={22} ml={1}>{userRating}</Box>You</>
                                                )}
                                        </Typography>
                                    </Box>
                                    {showEditing && (<>
                                        <div className={classes.userRating}>
                                            {[1, 2, 3, 4, 5].map(i => (
                                                <StarRate
                                                    key={i}
                                                    className={userRating >= i ? classes.starRate : classes.noStarRate}
                                                    onClick={() => this.doRate(i)}
                                                />
                                            ))}
                                            <Cancel
                                                className={classes.removeRating}
                                                onClick={() => this.removeUserRating()}
                                            />
                                        </div>
                                    </>)}
                                </Box>
                            ) : (
                                    <>
                                        <Rating
                                            name='half-rating'
                                            value={apiRatingNumber}
                                            precision={0.1}
                                            readOnly
                                            classes={{ iconEmpty: classes.iconEmpty, iconFilled: classes.iconFilled }}
                                        />
                                        <Typography variant='caption' gutterBottom align='left' component='div'>
                                            {`${avgRating}/5.0 (${total}`}
                                            {total === 1 ? (
                                                <FormattedMessage defaultMessage='user' id='Apis.Listing.StarRatingBar.user' />
                                            ) : (
                                                    <FormattedMessage defaultMessage='users' id='Apis.Listing.StarRatingBar.users' />
                                                )}
                                            {')'}
                                        </Typography>
                                    </>
                                )}
                        </>
                    )}
            </>
        );
    }
}

StarRatingBar.defaultProps = {
    apiRating: 0,
    ratingUpdate: 0,
    setRatingUpdate: () => { },
};

StarRatingBar.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    theme: PropTypes.shape({}).isRequired,
    apiId: PropTypes.string.isRequired,
    isEditable: PropTypes.bool.isRequired,
    showSummary: PropTypes.bool.isRequired,
    apiRating: PropTypes.oneOfType([
        PropTypes.string,
        PropTypes.number
    ]),
    ratingUpdate: PropTypes.number,
    setRatingUpdate: PropTypes.func,
};

export default withStyles(styles, { withTheme: true })(StarRatingBar);
