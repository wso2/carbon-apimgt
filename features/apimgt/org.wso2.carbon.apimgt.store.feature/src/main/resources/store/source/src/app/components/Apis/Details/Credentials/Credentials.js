import React from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';
import { withStyles } from '@material-ui/core/styles';
import Collapse from '@material-ui/core/Collapse';
import ArrowDropDownCircleOutlined from '@material-ui/icons/ArrowDropDownCircleOutlined';
import IconButton from '@material-ui/core/IconButton';
import { Typography } from '@material-ui/core';
import InlineMessage from '../../../Shared/InlineMessage';
import Wizard from './Wizard';
import { ApiContext } from '../ApiContext';
import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import Button from '@material-ui/core/Button';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import DeleteIcon from '@material-ui/icons/Delete';
import SubscribeToApi from "../../../Shared/AppsAndKeys/SubscribeToApi"


const styles = theme => ({
  root: {
    display: 'flex',
    alignItems: 'center',
    paddingTop: 8,
    paddingBottom: 8,
  },
  contentWrapper: {
      maxWidth: theme.custom.contentAreaWidth,
      paddingLeft: theme.spacing.unit*2,
      paddingTop: theme.spacing.unig,
  },
  titleSub: {
    cursor: 'pointer',
  },
  button: {
    margin: theme.spacing.unit,
    color: theme.palette.getContrastText(theme.palette.background.default),
    display:'flex',
    alignItems: 'center',
    fontSize: '11px',
  },
  tableMain: {
    width: '100%',
    borderCollapse: 'collapse',
  },
  actionColumn: {
    display: 'flex',
    textAlign: 'right',
    borderBottom: 'solid 1px ' + theme.palette.grey['A200'],
    direction: 'rtl',
  },
  td:{
    color: theme.palette.getContrastText(theme.palette.background.default),
    borderBottom: 'solid 1px ' + theme.palette.grey['A200'],
    fontSize: '11px',
    paddingLeft: 3,
  },
  th: {
    color: theme.palette.getContrastText(theme.palette.background.default),
    borderBottom: 'solid 2px ' + theme.palette.grey['A200'],
    borderTop: 'solid 2px ' + theme.palette.grey['A200'],
    textAlign: 'left',
    fontSize: '11px',
    paddingLeft: 3,
  },
  expansion: {
    background: 'transparent',
    boxShadow: 'none',
    
  },
  summary: {
    alignItems: 'center',
  }
});

class Credentials extends React.Component {
  state = {
    value: 0,
    expanded: true,
    wizardOn: false,
  };

  handleExpandClick = () => {
    this.setState(state => ({ expanded: !state.expanded }));
  };
  startStopWizard = () => {
    this.setState( state => ({wizardOn: !state.wizardOn}) )
  }
  handleSubscribe = () => {
    let promised_subscribe = this.subscribeToApi.createSubscription();  
    if(promised_subscribe) {
      promised_subscribe.then(response => {
        console.log("Subscription created successfully with ID : " + response.body.subscriptionId);
        
        alert('subscribed successfully');

        }).catch(error => {
            console.log("Error while creating the subscription.");
            console.error(error);
        }
      )
    }
  }
  render() {
    const { classes } = this.props;
    return (
      <ApiContext.Consumer>
      { ({api, applicationsAvailable, subscribedApplications}) => (
        <div className={classes.contentWrapper}>
          <div className={classes.root}>

            <ArrowDropDownCircleOutlined  
            onClick={this.handleExpandClick}
            aria-expanded={this.state.expanded}
            />

            <Typography onClick={this.handleExpandClick} variant="display1" className={classes.titleSub}>API Credentials</Typography>

          </div>
          <Collapse in={this.state.expanded} timeout="auto" unmountOnExit>
            {(applicationsAvailable.length === 0 && subscribedApplications.length === 0 ) ? 
                !this.state.wizardOn && <InlineMessage handleMenuSelect={this.startStopWizard} 
                  type="info">
                  <Typography variant="headline" component="h3">
                      Generate Credentials
                  </Typography>
                  <Typography component="p">
                      You need to generate credentials to access this API
                  </Typography>
                    <Button variant="contained" color="primary" className={classes.button} onClick={this.startStopWizard}>
                        GENERATE
                    </Button>
                </InlineMessage>
            : 
              <React.Fragment>
                <ExpansionPanel defaultExpanded={true} className={classes.expansion}>
                    <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
                    
                      <Typography variant="headline">{api.name} is subscribed to the following applications.</Typography>
                      <Typography variant="caption">( {subscribedApplications.length} {subscribedApplications.length === 1 ? 'subscription' : 'subscriptions' } )</Typography>
                    </ExpansionPanelSummary>
                    <ExpansionPanelDetails>
                      <table className={classes.tableMain}>
                      <tr>
                        <th className={classes.th}>Application Name</th>
                        <th className={classes.th}>Throttling Tier</th>
                        <th className={classes.th}></th>
                      </tr>
                      {subscribedApplications.map((app, index) => 
                        <tr style={{ backgroundColor: index%2 ? '' : '#ffffff77' }}>
                          <td className={classes.td}>{app.label}</td>
                          <td className={classes.td}>{app.policy}</td>
                          <td className={classes.actionColumn}>
                            <a className={classes.button}>
                              <DeleteIcon />
                              UNSUBSCRIBE
                            </a>
                            <a className={classes.button} >
                              <DeleteIcon />
                              PROD KEYS
                            </a>
                            <a className={classes.button} >
                              <DeleteIcon />
                              SANDBOX KEYS
                            </a>
                            <a className={classes.button} >
                              <DeleteIcon />
                              MANAGE APP
                            </a>
                          </td>
                        </tr>

                      )}
                      </table>
                    
                    </ExpansionPanelDetails>
                </ExpansionPanel>
                {applicationsAvailable.length > 0 && 
                <ExpansionPanel>
                    <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />} className={classes.summary}>
                      <Typography variant="headline">Subscribe {api.name} to {applicationsAvailable.length === 1 ? 'an available application' : 'available applications'}.</Typography>
                      <Typography variant="caption">( {applicationsAvailable.length} Applications )</Typography>
                    </ExpansionPanelSummary>
                    <ExpansionPanelDetails style={{flexDirection:'column'}}>
                      <SubscribeToApi innerRef={node => this.subscribeToApi = node} 
                                 api={api} applicationsAvailable={applicationsAvailable} />
                                 <div>
                                 <Button variant="contained" color="primary" className={classes.button} onClick={this.handleSubscribe}>
                                    Subscribe
                                </Button>
                                 </div>
                      
                    </ExpansionPanelDetails>
                </ExpansionPanel>
                }
                <ExpansionPanel className={classes.expansion}>>
                    <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />} className={classes.summary}>
                      <Typography variant="headline">Subscribe {api.name} to a new application.</Typography>
                      
                    </ExpansionPanelSummary>
                    <ExpansionPanelDetails>
                        <Wizard />
                    </ExpansionPanelDetails>
                </ExpansionPanel>
              </React.Fragment>
            }
            { this.state.wizardOn && <Wizard /> }
          </Collapse>
          
        </div>
        )}
        </ApiContext.Consumer>
          
    );
  }
}

Credentials.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(Credentials);
