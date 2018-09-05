import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import {KeyboardArrowLeft, StarRate, FileCopy} from '@material-ui/icons'
import Typography from '@material-ui/core/Typography'
import TextField from '@material-ui/core/TextField';
import HighlightOff from '@material-ui/icons/HighlightOff';


import {Link} from 'react-router-dom'
import Loading from '../../Base/Loading/Loading'
import ResourceNotFound from "../../Base/Errors/ResourceNotFound";
import Api from '../../../data/api'

import ImageGenerator from "../Listing/ImageGenerator"
import CopyToClipboard from 'react-copy-to-clipboard';
import Tooltip from '@material-ui/core/Tooltip';


const styles = theme => ({
    root: {
        height: 70,
        background: theme.palette.background.paper,
        borderBottom: 'solid 1px ' + theme.palette.grey['A200'],
        display: 'flex',
        alignItems: 'center',
    },
    backIcon: {
        color: theme.palette.primary.main,
        fontSize: 56,
        cursor: 'pointer',
    },
    backText: {
        color: theme.palette.primary.main,
        cursor: 'pointer',
        fontFamily: theme.typography.fontFamily,
    },
    apiIcon: {
        height: 45,
        marginTop: 10,
        marginRight: 10,
    },
    starRate: {
        fontSize: 70,
        color: theme.palette.custom.starColor,
    },
    starRateMy: {
        fontSize: 70,
        color: theme.palette.primary.main,
    },
    rateLink: {
        cursor: 'pointer',
        lineHeight: '70px',
    },

    topBar: {
        display: 'flex',
        paddingBottom: theme.spacing.unit * 2,
    },
    infoContent: {
        background: theme.palette.background.paper,
        borderBottom: 'solid 1px ' + theme.palette.grey['A200'],
        padding: theme.spacing.unit * 3,

    },
    infoItem: {
        marginRight: theme.spacing.unit * 4,
    },
    bootstrapRoot: {
        padding: 0,
        'label + &': {
          marginTop: theme.spacing.unit * 3,
        },
    },
    bootstrapInput: {
        borderRadius: 4,
        backgroundColor: theme.palette.common.white,
        border: '1px solid #ced4da',
        padding: '5px 12px',
        width: 350,
        transition: theme.transitions.create(['border-color', 'box-shadow']),
        fontFamily: [
          '-apple-system',
          'BlinkMacSystemFont',
          '"Segoe UI"',
          'Roboto',
          '"Helvetica Neue"',
          'Arial',
          'sans-serif',
          '"Apple Color Emoji"',
          '"Segoe UI Emoji"',
          '"Segoe UI Symbol"',
        ].join(','),
        '&:focus': {
          borderColor: '#80bdff',
          boxShadow: '0 0 0 0.2rem rgba(0,123,255,.25)',
        },
    },
    epWrapper: {
        display: 'flex',
    },
    prodLabel: {
        lineHeight: '30px',
        marginRight: 10,
        width: 100,
    },
    contentWrapper: {
        width: theme.palette.custom.contentAreaWidth - theme.palette.custom.leftMenuWidth,
    },
    ratingBoxWrapper: {
        position: 'relative',
        display: 'flex',
        alignItems:"center",
    },
    ratingBox: {
        backgroundColor:theme.palette.background.leftMenu,
        border:"1px solid rgb(71, 211, 244)",
        borderRadius:"5px",
        display:"flex",
        position: 'absolute',
        left: '-310px',
        top: 14,
        height:"40px",
        color :theme.palette.getContrastText(theme.palette.background.leftMenu),
        alignItems:"center",
        left:"0",
        paddingLeft:"5px",
        paddingRight:"5px",
    },
    userRating: {
        display: 'flex',
        alignItems: 'flex-end',
    },
    verticalDividerStar: {
        borderLeft: 'solid 1px ' + theme.palette.grey['A200'],
        height: 40, 
        marginRight:theme.spacing.unit,
        marginLeft:theme.spacing.unit,
    },
    backLink: {
        alignItems: 'center',
        textDecoration: 'none',
        display: 'flex',
    },
    ratingSummery: {
        alignItems: 'center',
        flexDirection: 'column',
        display: 'flex',
    },
    infoBarMain: {
        width: '100%',
    }
});

class StarRatingBar extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            rating: null,
            dummyRateValue: 1,
            showRateNow: false,
        };

        this.handleMouseOver = this.handleMouseOver.bind(this);
        this.handleRatingUpdate = this.handleRatingUpdate.bind(this);
        this.handleMouseOut = this.handleMouseOut.bind(this);
    }
    updateRating() {
        var api = new Api();

        //get user rating
        let promised_rating = api.getRatingFromUser(this.props.apiIdProp, null);
        promised_rating.then(
            response => {
                this.setState({rating: response.obj, dummyRateValue: response.obj.userRating});
            }
        );
    }
    componentDidMount() {
        this.updateRating();    
    }

    handleMouseOver(index) {
        this.setState({rating: index});
    }

    handleMouseOut() {
        this.setState({rating: this.state.previousRating});
    }

    handleRatingUpdate() {
       
    }

    handleClickAway = () => {
        this.setState({
          showRateNow: false,
        });
      };

      showRateBox = () => {
        this.setState({
            showRateNow: true,
          });
      }
      highlightUs(index){
          this.setState({dummyRateValue: index});
      }
      unhighlightUs(){
        this.setState({dummyRateValue: 1});
      }
      doRate(rateIndex){
        this.setState({rateIndex, showRateNow:false});

        var api = new Api();
        let ratingInfo = {"rating": rateIndex/2};
        let promise = api.addRating(this.props.apiIdProp, ratingInfo);
        promise.then(
            response => {
                this.updateRating();   
                //message.success("Rating updated successfully");
            }).catch(
            error => {
                //message.error("Error occurred while adding ratings!");
            }
        );
      }

    render() {
        const  { classes, theme }  = this.props;
        if(!this.state.rating){
            return <span />
        }
        return (
            <React.Fragment>
                {this.state.rating.count > 0 ? 
                <React.Fragment>
                    <StarRate className={classes.starRate}  />
                    <div className={classes.ratingSummery}>
                        <div className={classes.userRating}>
                            <Typography variant="display1">{this.state.rating.avgRating*2}</Typography>
                            <Typography variant="caption">/10</Typography>
                        </div>
                        <Typography variant="caption" gutterBottom align="left">
                            {this.state.rating.count} {this.state.rating.count == 1 ? 'user' : 'users' }
                        </Typography>
                    </div>
                </React.Fragment>
                : <StarRate  onClick={this.showRateBox} className={classes.starRate} style={{color:theme.palette.grey['A200']}} /> }
                <div className="vertical-divider-midway"></div>
                <div className={classes.ratingBoxWrapper}>
                    {this.state.showRateNow && 
                    <div className={classes.ratingBox}>
                        <HighlightOff />
                        <div className="vertical-divider"></div>
                        {[1,2,3,4,5,6,7,8,9,10].map((i) =>
                            <StarRate color={ i <= this.state.rating.userRating*2 || i <= this.state.dummyRateValue  ? "primary" : "" } 
                            onMouseOver={() => this.highlightUs(i)}
                            onMouseLeave={() => this.unhighlightUs()}
                                onClick={() => this.doRate(i)} />)
                        }
                        
                    </div> }
                    {this.state.rating.userRating ?  
                        <React.Fragment>
                            <StarRate className={classes.starRateMy} onClick={this.showRateBox}  />
                            <div className={classes.ratingSummery} onClick={this.showRateBox} >
                                <Typography variant="display1">{this.state.rating.userRating*2}</Typography> 
                                <Typography variant="caption" gutterBottom align="left">
                                    YOU
                                </Typography>
                            </div>
                        </React.Fragment>
                    : <React.Fragment>
                        <StarRate  onClick={this.showRateBox} className={classes.starRate} style={{color:theme.palette.grey['A200']}} />
                        <Typography onClick={this.showRateBox}  className={classes.rateLink}>Rate this API</Typography>
                    </React.Fragment>
                
                }
                    
                </div>
            </React.Fragment>
        );
    }
}

StarRatingBar.propTypes = {
    classes: PropTypes.object.isRequired,
    theme: PropTypes.object.isRequired,
  };
  
StarRatingBar = withStyles(styles, { withTheme: true })(StarRatingBar);

class InfoBar extends React.Component {
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
            commentList: null,
            prodUrlCopied: false,
            sandboxUrlCopied: false,
        };
        this.api_uuid = this.props.api_uuid;
    }
    componentDidMount() {
        const api = new Api();
        let promised_api = api.getAPIById(this.api_uuid);
        promised_api.then(
            response => {
                console.info("xoxo", response.obj);
                this.setState({api: response.obj});
                //this.props.setDetailsAPI(response.obj);
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
    
      onCopy = name => event => {
        this.setState({
          [name]: true,
        });
        let that = this;
        let elementName = name;
        var caller = function(){
            that.setState({
                [elementName]: false,
              });   
        }
        setTimeout(caller,4000);
      };
     
 
  render() {
    const { classes, theme } = this.props;
    const api = this.state.api;
    if (this.state.notFound) {
        return <ResourceNotFound message={this.props.resourceNotFountMessage}/>
    }
    if (!api) {
        return <Loading/>
    }
    return (
        <div className={classes.infoBarMain}>
            <div className={classes.root}>
                <Link to="/apis" className={classes.backLink}>
                    <KeyboardArrowLeft className={classes.backIcon}  />
                    <div className={classes.backText}>
                        BACK TO <br />
                        LISTING
                    </div>
                </Link>
                <div className="vertical-divider-70"></div>
                <ImageGenerator apiName={api.name} width="70" height="50"  />
                <div style={{marginLeft: theme.spacing.unit}}>
                    <Typography variant="display1" >
                        {api.name}
                    </Typography>
                    <Typography variant="caption" gutterBottom align="left">
                        {api.provider} | 21-May 2018
                    </Typography>
                </div>
                <div className="vertical-divider-70"></div>
                <StarRatingBar apiIdProp={api.id} />
                
            </div>
            <div className={classes.infoContent}>
            <div className={classes.contentWrapper}>
                <div className={classes.topBar}>
                    <div className={classes.infoItem}>
                        <Typography variant="subheading" gutterBottom>
                            {api.version}
                        </Typography>
                        <Typography variant="caption" gutterBottom align="left">
                            Version
                        </Typography>
                    </div>
                    <div className={classes.infoItem}>
                        <Typography variant="subheading" gutterBottom>
                            {api.context}
                        </Typography>
                        <Typography variant="caption" gutterBottom align="left">
                            Context
                        </Typography>
                    </div>
                    <div>
                        <div className={classes.epWrapper}>
                            <Typography className={classes.prodLabel}>
                                Production URL
                            </Typography>
                            <TextField
                                    defaultValue="http://192.168.1.2:8282/SwaggerPetstore/1.0.0"
                                    id="bootstrap-input"
                                    InputProps={{
                                    disableUnderline: true,
                                    classes: {
                                        root: classes.bootstrapRoot,
                                        input: classes.bootstrapInput,
                                    },
                                    }}
                                    InputLabelProps={{
                                    shrink: true,
                                    className: classes.bootstrapFormLabel,
                                    }}

                                />
                                <Tooltip title={this.state.prodUrlCopied ? "Copied" : "Copy to clipboard"} placement="right">
                                    <CopyToClipboard text="http://192.168.1.2:8282/SwaggerPetstore/1.0.0" onCopy={this.onCopy("prodUrlCopied")} >
                                        <FileCopy color="secondary" />
                                    </CopyToClipboard>
                                </Tooltip>
                        </div>
                        <div className={classes.epWrapper}>
                            <Typography className={classes.prodLabel}>
                                Sandbox URL
                            </Typography>
                            <TextField
                                    defaultValue="http://192.168.1.2:8282/SwaggerPetstore/1.0.0"
                                    id="bootstrap-input"
                                    InputProps={{
                                    disableUnderline: true,
                                    classes: {
                                        root: classes.bootstrapRoot,
                                        input: classes.bootstrapInput,
                                    },
                                    }}
                                    InputLabelProps={{
                                    shrink: true,
                                    className: classes.bootstrapFormLabel,
                                    }}
                                />
                                <Tooltip title={this.state.sandboxUrlCopied ? "Copied" : "Copy to clipboard"} placement="right">
                                    <CopyToClipboard text="http://192.168.1.2:8282/SwaggerPetstore/1.0.0" onCopy={this.onCopy("sandboxUrlCopied")}  >
                                        <FileCopy color="secondary" />
                                    </CopyToClipboard>
                                </Tooltip>

                        </div>
                    </div> 
                </div>
                <Typography>
                    Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut dolor dui, fermentum in ipsum a, pharetra feugiat orci. Suspendisse at sem nunc. Integer in eros eget orci sollicitudin ultricies. Mauris vehicula mollis vulputate. Morbi sed velit vulputate nisl ullamcorper blandit. Quisque diam orci, ultrices at risus vel, auctor vulputate odio. Etiam vel iaculis massa, vel sollicitudin velit. Aenean facilisis vitae elit vitae iaculis. Nam vel tincidunt arcu.
                </Typography> 
            </div>
        </div>
    </div> 
    );
  }
}

InfoBar.propTypes = {
  classes: PropTypes.object.isRequired,
  theme: PropTypes.object.isRequired,
};

export default withStyles(styles, { withTheme: true })(InfoBar);
