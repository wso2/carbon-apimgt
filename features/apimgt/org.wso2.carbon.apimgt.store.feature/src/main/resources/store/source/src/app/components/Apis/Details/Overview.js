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

import React, {Component} from 'react'
import {Link} from 'react-router-dom'
import Loading from '../../Base/Loading/Loading'
import ResourceNotFound from "../../Base/Errors/ResourceNotFound";
import Api from '../../../data/api'

import Grid from 'material-ui/Grid';
import Typography from 'material-ui/Typography';

import ImageGenerator from "../Listing/ImageGenerator"
import {withStyles} from 'material-ui/styles';
import PropTypes from 'prop-types';
import Chip from 'material-ui/Chip';
import Subscribe from './Subscribe'


const styles = theme => ({
    imageSideContent: {
        display: 'inline-block',
        paddingLeft: 20,
    },
    imageWrapper: {
        display: 'flex',
        flexAlign: 'top',
    },
    headline: {
        marginTop: 20
    },
    titleCase: {
        textTransform: 'capitalize',
    },
    chip: {
        marginLeft: 0,
        cursor: 'pointer',
    },
    openNewIcon: {
        display: 'inline-block',
        marginLeft: 20,
    },
    endpointsWrapper: {
        display: 'flex',
        justifyContent: 'flex-start',
    }
});

class Overview extends Component {
    constructor(props) {
        super(props);
        this.state = {
            api: null,
            applications: null,
            policies: null,
            dropDownApplications: null,
            dropDownPolicies: null,
            notFound: false,
            tabValue: "Social Sites",
            comment: '',
            commentList: null
        };
        this.api_uuid = this.props.match.params.api_uuid;
        this.handleTabChange = this.handleTabChange.bind(this);
    }

    componentDidMount() {
        const api = new Api();
        let promised_api = api.getAPIById(this.api_uuid);
        promised_api.then(
            response => {
                this.setState({api: response.obj});
                this.props.setDetailsAPI(response.obj);
            }
        ).catch(
            error => {
                if (process.env.NODE_ENV !== "production") {
                    console.log(error);
                }
                let status = error.status;
                if (status === 404) {
                    this.setState({notFound: true});
                }
            }
        );

        let promised_applications = api.getAllApplications();
        promised_applications.then(
            response => {
                this.setState({applications: response.obj.list});
            }
        ).catch(
            error => {
                if (process.env.NODE_ENV !== "production") {
                    console.log(error);
                }
                let status = error.status;
                if (status === 404) {
                    this.setState({notFound: true});
                }
            }
        );

        let promised_subscriptions = api.getSubscriptions(this.api_uuid, null);
        promised_subscriptions.then(
            response => {

            }
        ).catch(
            error => {
                if (process.env.NODE_ENV !== "production") {
                    console.log(error);
                }
                let status = error.status;
                if (status === 404) {
                    this.setState({notFound: true});
                }
            }
        );
    }

    handleClick() {
        this.setState({redirect: true});
    }

    handleTabChange = (event, tabValue) => {
        this.setState({tabValue: tabValue});
    };

    render() {
        const api = this.state.api;
        if (this.state.notFound) {
            return <ResourceNotFound message={this.props.resourceNotFountMessage}/>
        }
        if (!api) {
            return <Loading/>
        }
        const { classes } = this.props;
        return (
            <Grid container>
                <Grid item xs={12}>
                    <Typography variant="subheading" align="left">
                        Created by {api.provider} : {api.createdTime}
                    </Typography>
                    <Typography variant="caption" gutterBottom align="left">
                        Last update : {api.lastUpdatedTime}
                    </Typography>

                    <Typography variant="subheading" align="left">
                        <StarRatingBar apiIdProp={this.api_uuid}></StarRatingBar>
                    </Typography>


                    <Grid container>
                        <Grid item xs={12} sm={6} md={4} lg={3}>
                            {api.policies && api.policies.length ?
                                    <Typography variant="subheading" align="left" className={classes.headline}>
                                        {api.policies.map(policy => policy + ", ")}
                                    </Typography>
                                :
                                    <Typography variant="subheading" align="left" className={classes.headline}>
                                        &lt; NOT SET FOR THIS API &gt;
                                    </Typography>
                            }
                            <Typography variant="caption" gutterBottom align="left">
                                Throttling Policies
                            </Typography>
                        </Grid>

                        <Grid item xs={12} sm={6} md={4} lg={3}>
                            {api.tags && api.tags.length ?
                                <div className={classes.headline}>
                                    {api.tags.map(tag => <Link to={"/apis/" + api.id + "/tags" } key={tag}>
                                        <Chip label={tag} className={classes.chip} />
                                    </Link>)}
                                </div>
                                :
                                    <Typography variant="subheading" align="left" className={classes.headline}>
                                        &lt; NOT SET FOR THIS API &gt;
                                    </Typography>
                            }
                            <Typography variant="caption" gutterBottom align="left">
                                Tags
                            </Typography>

                        </Grid>
                        <Grid item xs={12} sm={6} md={4} lg={3}>

                            {api.transport && api.transport.length ?
                                <Typography variant="subheading" align="left" className={classes.headline}>
                                    {api.transport.map(trans => trans + ", ")}
                                </Typography>
                                :
                                <Typography variant="subheading" align="left" className={classes.headline}>
                                    &lt; NOT SET FOR THIS API &gt;
                                </Typography>
                            }
                            <Typography variant="caption" gutterBottom align="left">
                                Transport
                            </Typography>

                        </Grid>

                    </Grid>
                </Grid>

            </Grid>
        );
    }
}
class Star extends React.Component {
    constructor(props) {
        super(props);

        this.handleHoveringOver = this.handleHoveringOver.bind(this);
    }

    handleHoveringOver(event) {
        this.props.hoverOver(this.props.name);
    }

    render() {
        return this.props.isRated ?
            <span onMouseOver={this.handleHoveringOver} style={{color: 'gold'}}>
                ★
            </span> :
            <span onMouseOver={this.handleHoveringOver} style={{color: 'gold'}}>
                ☆
            </span>;
    }
}
class StarRatingBar extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            previousRating: 0,
            rating: 0
        };

        this.handleMouseOver = this.handleMouseOver.bind(this);
        this.handleRatingUpdate = this.handleRatingUpdate.bind(this);
        this.handleMouseOut = this.handleMouseOut.bind(this);
    }

    componentDidMount() {
        var api = new Api();
        let promised_api = api.getAPIById(this.props.apiIdProp);
        promised_api.then(
            response => {
            }
        );

        //get user rating
        let promised_rating = api.getRatingFromUser(this.props.apiIdProp, null);
        promised_rating.then(
            response => {
                this.setState({rating: response.obj.userRating});
                this.setState({previousRating: response.obj.userRating});
            }
        );
    }

    handleMouseOver(index) {
        this.setState({rating: index});
    }

    handleMouseOut() {
        this.setState({rating: this.state.previousRating});
    }

    handleRatingUpdate() {
        this.setState({previousRating: this.state.rating});
        this.setState({rating: this.state.rating});

        var api = new Api();
        let ratingInfo = {"rating": this.state.rating};
        let promise = api.addRating(this.props.apiIdProp, ratingInfo);
        promise.then(
            response => {
                message.success("Rating updated successfully");
            }).catch(
            error => {
                message.error("Error occurred while adding ratings!");
            }
        );
    }

    render() {
        return (<div onClick={this.handleRatingUpdate} onMouseOut={this.handleMouseOut}>
            <Star name={1} isRated={this.state.rating >= 1} hoverOver={this.handleMouseOver}> </Star>
            <Star name={2} isRated={this.state.rating >= 2} hoverOver={this.handleMouseOver}> </Star>
            <Star name={3} isRated={this.state.rating >= 3} hoverOver={this.handleMouseOver}> </Star>
            <Star name={4} isRated={this.state.rating >= 4} hoverOver={this.handleMouseOver}> </Star>
            <Star name={5} isRated={this.state.rating >= 5} hoverOver={this.handleMouseOver}> </Star>
        </div>);
    }
}
Overview.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(Overview);
export {Star};
