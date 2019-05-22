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
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';

import KeyConfiguration from './KeyConfiguration';
import ViewKeys from './ViewKeys';

const styles = theme => ({
    root: {
        padding: theme.spacing.unit * 3,
    },
    button: {
        marginLeft: theme.spacing.unit * 2,
    },
    tokenSection: {
        marginTop: theme.spacing.unit * 2,
        marginBottom: theme.spacing.unit * 2,
    },
    margin: {
        marginRight: theme.spacing.unit * 2,
    },
    keyTitle: {
        textTransform: 'uppercase',
    },
    keyConfigWrapper: {
        flexDirection: 'column',
        marginBottom: 0,
    },
    generateWrapper: {
        padding: 10,
        borderLeft: 'solid 1px #fff',
        borderRight: 'solid 1px #fff',
        borderBottom: 'solid 1px #fff',
    },
});

class TokenManager extends React.Component {
  generateKeys = () => {
      const that = this;
      const promiseGenerate = this.keys.keygenWrapper();
      promiseGenerate
          .then((response) => {
              console.log('Keys generated successfully with ID : ' + response);
              if (that.props.updateSubscriptionData) { that.props.updateSubscriptionData(); }
              that.viewKeys.updateUI();
          })
          .catch((error) => {
              if (process.env.NODE_ENV !== 'production') {
                  console.error(error);
              }
              const { status } = error;
              if (status === 404) {
                  this.setState({ notFound: true });
              }
          });
  };

  handleChange = (event, value) => {
      this.setState({ value });
  };

  render() {
      const { classes, selectedApp, keyType } = this.props;
      return (
          <div className={classes.root}>
              <Typography variant='headline' className={classes.keyTitle}>
                  {keyType}
                  {' '}
          Key and Secret
              </Typography>
              <ViewKeys
                  selectedApp={selectedApp}
                  keyType={keyType}
                  innerRef={node => (this.viewKeys = node)}
              />

              <ExpansionPanel>
                  <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
                      <Typography className={classes.heading} variant='subtitle1'>
              Key Configuration
                      </Typography>
                  </ExpansionPanelSummary>
                  <ExpansionPanelDetails className={classes.keyConfigWrapper}>
                      <KeyConfiguration
                          innerRef={node => (this.keys = node)}
                          selectedApp={selectedApp}
                          keyType={keyType}
                      />
                  </ExpansionPanelDetails>
              </ExpansionPanel>
              <div className={classes.generateWrapper}>
                  <Button
                      variant='contained'
                      color='primary'
                      className={classes.button}
                      onClick={this.generateKeys}
                  >
            Generate Keys
                  </Button>
              </div>
          </div>
      );
  }
}

TokenManager.propTypes = {
    classes: PropTypes.object,
};

export default withStyles(styles)(TokenManager);
