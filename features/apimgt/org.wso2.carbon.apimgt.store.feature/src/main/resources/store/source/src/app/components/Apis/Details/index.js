import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import CustomIcon from '../../Shared/CustomIcon';
import LeftMenuItem from '../../Shared/LeftMenuItem';
import {Route, Switch, Redirect, Link} from 'react-router-dom';
import Overview from './Overview';
import ApiConsole from './ApiConsole/ApiConsole';
import Documentation from './Documents/Documentation';
import Forum from './Forum';
import Sdk from './Sdk';
import {PageNotFound} from '../../Base/Errors/index';
import InfoBar from './InfoBar';
import RightPanel from './RightPanel';
import { ApiContext } from './ApiContext';
import Credentials from './Credentials/Credentials';
import Api from '../../../data/api';
import Comments from './Comments/Comments';


const styles = theme => ({
    LeftMenu: {
        backgroundColor: theme.palette.background.leftMenu,
        width: theme.custom.leftMenuWidth,
        textAlign: 'center',
        fontFamily: theme.typography.fontFamily,
        position: 'absolute',
        bottom: 0,
        left: 0,
        top: 0,
    },
    leftLInkMain: {
        borderRight: 'solid 1px ' + theme.palette.background.leftMenu,
        paddingBottom: theme.spacing.unit,  
        paddingTop: theme.spacing.unit,  
        cursor: 'pointer',
        backgroundColor: theme.palette.background.leftMenuActive,
        color: theme.palette.getContrastText(theme.palette.background.leftMenuActive),
        textDecoration: 'none',
    },
    detailsContent: {
        display: 'flex',
        flex: 1,
    },
      content: {
        display: 'flex',
        flex: 1,
        flexDirection: 'column',
        marginLeft: theme.custom.leftMenuWidth,
        paddingBottom:  theme.spacing.unit*3,  
      }
});
class Details extends React.Component {
    constructor(props){
        super(props);
        
        this.state = {
            active: 'overview',
            overviewHiden: false,
            handleMenuSelect: this.handleMenuSelect,
            api: null,
            applications: null,
            subscribedApplications: [],
            applicationsAvailable: [],
            item: 1,
        };
        this.setDetailsAPI = this.setDetailsAPI.bind(this);
        this.api_uuid = this.props.match.params.api_uuid;
    }
    handleMenuSelect = (menuLink) => {
        this.props.history.push({pathname: "/apis/" + this.props.match.params.api_uuid + "/" + menuLink});
        (menuLink === "overview") ? this.infoBar.toggleOverview(true) : this.infoBar.toggleOverview(false) ;
        this.setState({active: menuLink});
    }
    setDetailsAPI(api){
        this.setState({api: api});
    }

    updateSubscriptionData() {
        const api = new Api();
        let promised_api = api.getAPIById(this.api_uuid);
        let existing_subscriptions = api.getSubscriptions(this.api_uuid, null);
        let promised_applications = api.getAllApplications();
        
        Promise.all([ promised_api, existing_subscriptions, promised_applications] ).then(
            response => {
                let [api, subscriptions, applications] = response.map(data => data.obj);

                //Getting the policies from api details
                this.setState({api: api});
                if(api && api.policies){
                    let apiTiers = api.policies;
                    let tiers = [];
                    for (let i = 0; i < apiTiers.length; i++) {
                        let tierName = apiTiers[i];
                        tiers.push({value: tierName, label: tierName});
                    }
                    this.setState({tiers: tiers});
                    if (tiers.length > 0) {
                        this.setState({policyName: tiers[0].value});
                    }
                }
                
                let subscribedApplications = [];
                //get the application IDs of existing subscriptions
                subscriptions.list.map(element => subscribedApplications.push({value: element.applicationId, 
                    policy: element.policy}));
                this.setState({subscribedApplications: subscribedApplications});


                //Removing subscribed applications from all the applications and get the available applications to subscribe
                let applicationsAvailable = [];
                for (let i = 0; i < applications.list.length; i++) {
                    let applicationId = applications.list[i].applicationId;
                    let applicationName = applications.list[i].name;
                    //include the application only if it does not has an existing subscriptions
                    let applicationSubscribed = false;
                    for ( var j =0; j < subscribedApplications.length; j++ ){
                        if( subscribedApplications[j].value === applicationId ){
                            applicationSubscribed = true;
                            subscribedApplications[j].label = applicationName;
                        }
                    }
                    if (!applicationSubscribed) {
                        applicationsAvailable.push({value: applicationId, label: applicationName});
                    }
                }
                this.setState({applicationsAvailable});
                if (applicationsAvailable && applicationsAvailable.length > 0) {
                    this.setState({applicationId: applicationsAvailable[0].value});
                }

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
        )
    }
    componentDidMount() {
     
        var body = document.body,
        html = document.documentElement;
  
        let currentLink = this.props.location.pathname.match(/[^\/]+(?=\/$|$)/g);
        if( currentLink && currentLink.length > 0){
            this.setState({ active: currentLink[0] });
        }
        console.info(this.state.active);
        this.updateSubscriptionData();
    }
    

  render() {
    const { classes, theme } = this.props;
    let redirect_url = "/apis/" + this.props.match.params.api_uuid + "/overview";
    const leftMenuIconMainSize = theme.custom.leftMenuIconMainSize;

    return (
        <ApiContext.Provider value={this.state}>
             <div className={classes.LeftMenu}>
                <Link to={"/apis"}>
                    <div className={classes.leftLInkMain}>
                        <CustomIcon width={leftMenuIconMainSize} height={leftMenuIconMainSize} icon="api" />
                    </div>
                </Link>
                <LeftMenuItem text="overview" handleMenuSelect={this.handleMenuSelect} active={this.state.active} />
                <LeftMenuItem text="credentials" handleMenuSelect={this.handleMenuSelect} active={this.state.active} />
                <LeftMenuItem text="comments" handleMenuSelect={this.handleMenuSelect} active={this.state.active} />
                <LeftMenuItem text="test" handleMenuSelect={this.handleMenuSelect} active={this.state.active} />
                <LeftMenuItem text="docs" handleMenuSelect={this.handleMenuSelect} active={this.state.active} />
                <LeftMenuItem text="sdk" handleMenuSelect={this.handleMenuSelect} active={this.state.active} />
               
            </div>
              <div className={classes.content}>
              <InfoBar api_uuid={this.props.match.params.api_uuid} innerRef={node => this.infoBar = node}/>
                <Switch>
                    <Redirect exact from="/apis/:api_uuid" to={redirect_url}/>
                    <Route path="/apis/:api_uuid/overview" component={Overview}/>
                    <Route path="/apis/:api_uuid/credentials" component={Credentials} />
                    <Route path="/apis/:api_uuid/comments" component={Comments} />
                    <Route path="/apis/:api_uuid/test" component={ApiConsole}/>
                    <Route path="/apis/:api_uuid/docs" component={Documentation}/>
                    <Route path="/apis/:api_uuid/forum" component={Forum}/>
                    <Route path="/apis/:api_uuid/sdk" component={Sdk}/>
                    <Route component={PageNotFound}/>
                </Switch>
              </div>
            {theme.custom.showApiHelp && <RightPanel /> }
        </ApiContext.Provider>
    );
  }
}

Details.propTypes = {
  classes: PropTypes.object.isRequired,
  theme: PropTypes.object.isRequired,
};

export default withStyles(styles, { withTheme: true })(Details);

