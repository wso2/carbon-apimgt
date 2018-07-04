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

import React from 'react'
import Typography from 'material-ui/Typography';
import Grid from 'material-ui/Grid';
import {withStyles} from 'material-ui/styles';
import { Manager, Target, Popper } from 'react-popper';
import ImageGenerator from './ImageGenerator';
import { Link } from 'react-router-dom'
import PropTypes from 'prop-types';
import StarRatingBar from './StarRating';
import Api from '../../../data/api';

const styles = theme => ({
    lifeCycleState: {
        width: "1.5em",
        height: "1.5em",
        borderRadius: "50%",
        marginRight: "0.5em"
    },
    lifeCycleDisplay: {
        width: 95,
        height: 30,
        marginTop: -15,
        marginLeft: 10,
        color: '#fff',
        textAlign: 'center',
        lineHeight: '30px',
        position: 'absolute',
    },
    lifeCycleState_Created: {backgroundColor: "#0000ff"},
    lifeCycleState_Prototyped: {backgroundColor: "#42dfff"},
    lifeCycleState_Published: {backgroundColor: "#41830A"},
    lifeCycleState_Maintenance: {backgroundColor: "#cecece"},
    lifeCycleState_Deprecated: {backgroundColor: "#D7C850"},
    lifeCycleState_Retired: {backgroundColor: "#000000"},
    thumbContent: {
        width: 230,
        backgroundColor: theme.palette.background.paper,
        padding: 10
    },
    thumbLeft: {
        alignSelf: 'flex-start',
        flex: 1
    },
    thumbRight: {
        alignSelf: 'flex-end',
    },
    thumbInfo: {
        display: 'flex',
    },
    thumbHeader: {
        width: 250,
        whiteSpace: 'nowrap',
        overflow : 'hidden',
        textOverflow : 'ellipsis',
        cursor: 'pointer',
        margin: 0,
    },
    contextBox: {
        width: 140,
        whiteSpace: 'nowrap',
        overflow : 'hidden',
        textOverflow : 'ellipsis',
        cursor: 'pointer',
        margin: 0,
        display: 'inline-block',
        lineHeight: '1em',
    },
    descriptionOverlay: {
        content:'',
        width:'100%',
        height:'100%' ,
        position:'absolute',
        left:0,
        top:0,
        background:'linear-gradient(transparent 25px, theme.palette.background.paper)',
    },
    descriptionWrapper: {
        color: theme.palette.text.secondary,
        position: 'relative',
        height: 50,
        overflow: 'hidden'
    },
    thumbDelete: {
        cursor: 'pointer',
        backgroundColor: '#ffffff9a',
        display: 'inline-block',
        position: 'absolute',
        top: 20,
        left: 224,
    },
    thumbWrapper: {
        position: 'relative',
        paddingTop: 20,
    },
    deleteIcon: {
        fill: 'red'
    },
    imageWrapper: {
        color: theme.palette.text.secondary,
        textDecoration: 'none',
    }
});

class ApiThumb extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            active: true,
            loading: false,
            open: false,
            overview_link: '',
            isRedirect: false,
            openMoreMenu: false,
            rating: 0
        };

    }

    componentDidMount() {
        let api = new Api();
        let promised_rating = api.getRatingFromUser(this.props.api.id, null);
        promised_rating.then(
            response => {
                this.setState({rating: response.obj.userRating});
            }
        );
    }

    render() {
        let details_link = "/apis/" + this.props.api.id;
        const {api, classes} = this.props;

        const {name, lifeCycleStatus, version, context, description} = this.props.api;
        const {rating} = this.state;

        return (
            <Grid item xs={12} sm={6} md={4} lg={3} xl={2} className={classes.thumbWrapper}>
              
                <Link to={details_link} className={classes.imageWrapper}>
                    <ImageGenerator apiName={name} />
                </Link>

                <div className={classes.thumbContent}>
                    <Link to={details_link} className={classes.imageWrapper}>
                        <Typography className={classes.thumbHeader} variant="display1" gutterBottom
                                    onClick={this.handleRedirectToAPIOverview} title={name}>
                            {name}
                        </Typography>
                    </Link>
                    <Typography variant="caption" gutterBottom align="left">
                        By: provider}
                    </Typography>
                    <div className={classes.thumbInfo}>
                        <div className={classes.thumbLeft}>
                            <Typography variant="subheading">
                                {version}
                            </Typography>
                            <Typography variant="caption" gutterBottom align="left">
                                Version
                            </Typography>
                        </div>
                        <div className={classes.thumbRight}>
                            <Typography variant="subheading" align="right" className={classes.contextBox}>{context}</Typography>
                            <Typography variant="caption" gutterBottom align="right">
                                Context
                            </Typography>
                        </div>
                    </div>
                    <div className={classes.thumbInfo}>
                        <Typography variant="subheading" gutterBottom align="left">
                            <StarRatingBar rating={ rating }/>
                        </Typography>
                    </div>
                    <div className={classes.descriptionWrapper}>
                        {description}
                        <div className={classes.descriptionOverlay} />
                    </div>
                </div>

            </Grid>
        );
    }

}

ApiThumb.propTypes = {
    classes: PropTypes.object.isRequired,
    theme: PropTypes.object.isRequired,
};

export default withStyles(styles, { withTheme: true })(ApiThumb);
