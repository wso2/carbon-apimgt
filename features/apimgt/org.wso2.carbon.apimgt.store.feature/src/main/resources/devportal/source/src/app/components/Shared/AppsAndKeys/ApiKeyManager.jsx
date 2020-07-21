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
import { FormattedMessage, injectIntl } from 'react-intl';
import API from 'AppData/api';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import FormGroup from '@material-ui/core/FormGroup';
import Grid from '@material-ui/core/Grid';
import ViewToken from './ViewToken';
import ApiKey from '../ApiKey';
import ApiKeyRestriction from '../ApiKeyRestriction';
import Card from "@material-ui/core/Card";
import CardContent from "@material-ui/core/CardContent";
import CircularProgress from '@material-ui/core/CircularProgress';

const styles = (theme) => ({
  root: {
    padding: theme.spacing(3),
    '& span, & h5, & label, & input': {
      color: theme.palette.getContrastText(theme.palette.background.paper),
    },
  },
  dialog: {
    '& span, & h2, & label': {
      color: theme.palette.getContrastText(theme.palette.background.paper),
    },
  },
  button: {
    '& span': {
      color: theme.palette.getContrastText(theme.palette.primary.main),
    }
  },
  tokenSection: {
    marginTop: theme.spacing(2),
    marginBottom: theme.spacing(2),
  },
  margin: {
    marginRight: theme.spacing(2),
  },
  keyConfigWrapper: {
    flexDirection: 'column',
    marginBottom: 0,
  },
  generateWrapper: {
    padding: '10px',
    'margin-inline-end': 'auto',
  },
  paper: {
    display: 'flex',
    marginTop: theme.spacing(2),
    marginBottom: theme.spacing(2),
    marginLeft: theme.spacing(10),
  },
  dialogTitle: {
    padding: '24px 24px 0px',
  },
  dialogContent: {
    padding: '0 24px 0px',
  },
  formGroup: {
    padding: '20px',
  },
  gridWrapper: {
    'align-self': 'center',
  },
  keyTitle: {
    textTransform: 'capitalize',
  },
  cardBody: {
    padding: theme.spacing(1),
    lineHeight: 2,
  },
  generateKey: {
    '& span': {
      color: theme.palette.getContrastText(theme.palette.primary.main),
    }
  },
});

class ApiKeyManager extends React.Component {
  constructor(props) {
    super(props);
    const { classes, selectedApp, keyType } = this.props;
    this.state = {
      apikey: null,
      open: false,
      showToken: false,
      accessTokenRequest: {
        timeout: -1,
      },
      ipList: [],
      newIP: null,
      restrictSchema: 'none',
      refererList: [],
      newReferer: null,
      isGenerating: false,
    };
  }

  updateIpList = (ipList) => {
    this.setState(() => ({ ipList }));
  };

  updateNewIp = (newIP) => {
    this.setState(() => ({ newIP }));
  };

  updateRefererList = (refererList) => {
    this.setState(() => ({ refererList }));
  };

  updateNewReferer = (newReferer) => {
    this.setState(() => ({ newReferer }));
  };

  updateRestrictSchema = (restrictSchema) => {
    this.setState(() => ({ restrictSchema }));
  }

  handleClose = () => {
    this.setState(() => ({ open: false, accessTokenRequest: { timeout: -1 } }));
  }

  handleClickOpen = () => {
    this.setState(() => ({ open: true, showToken: false }));
  }

  updateAccessTokenRequest = (accessTokenRequest) => {
    this.setState(() => ({ accessTokenRequest }));
  }

  generateKeys = () => {
    const { selectedApp, keyType } = this.props;
    this.setState({isGenerating: true});
    const client = new API();
    const restrictions = {
      permittedIP: this.state.ipList.join(","),
      permittedReferer: this.state.refererList.join(","),
    };
    const promisedKey = client.generateApiKey(selectedApp.appId, keyType,
      this.state.accessTokenRequest.timeout, restrictions);

    promisedKey
      .then((response) => {
        console.log('Non empty response received');
        const apikey = { accessToken: response.body.apikey, validityTime: response.body.validityTime, isOauth: false };
        this.setState(() => ({
          apikey, open: true, showToken: true,
          ipList: [], refererList: []
        }));
        this.setState({isGenerating: false});
      })
      .catch((error) => {
        if (process.env.NODE_ENV !== 'production') {
          console.log(error);
        }
        const { status } = error;
        if (status === 404) {
          this.setState({
            notFound: true, ipList: [],
            refererList: []
          });
        }
        this.setState({isGenerating: false});
      });
  }

  render() {
    const { classes, keyType } = this.props;
    const {
      showToken, accessTokenRequest, open, apikey, newIP, ipList,
      newReferer, refererList, restrictSchema, isGenerating,
    } = this.state;
    return (
      <Grid container direction="row" spacing={0} justify="left" alignItems="left">
        <Grid item md={5} xs={12}>
          <ApiKeyRestriction
            updateNewIp={this.updateNewIp}
            newIP={newIP}
            updateIpList={this.updateIpList}
            ipList={ipList}
            restrictSchema={restrictSchema}
            updateRestrictSchema={this.updateRestrictSchema}
            refererList={refererList}
            newReferer={newReferer}
            updateNewReferer={this.updateNewReferer}
            updateRefererList={this.updateRefererList}
          />
          <FormGroup row className={classes.formGroup}>

            <Button
              variant="contained"
              color="primary"
              onClick={this.handleClickOpen}
              className={classes.generateKey}
            >
              {"Generate Key"}
            </Button>
            <Typography
              component="div"
              variant="body2"
              className={classes.formLabel}
            >
              <FormattedMessage
                id="Shared.AppsAndKeys.ApiKeyManager.generate.key.help"
                defaultMessage="Use the Generate Key button to generate a self-contained JWT token."
              />
            </Typography>
          </FormGroup>
          <Dialog
            open={open}
            onClose={this.handleClose}
            aria-labelledby="form-dialog-title"
            className={classes.dialog}
          >
            <DialogTitle id="responsive-dialog-title" className={classes.dialogTitle}>
              {"Generate API Key"}
            </DialogTitle>
            <DialogContent className={classes.dialogContent}>
              <DialogContentText>
                {!showToken && (
                  <ApiKey
                    updateAccessTokenRequest={this.updateAccessTokenRequest}
                    accessTokenRequest={accessTokenRequest}
                  />
                )}
                {showToken && <ViewToken token={apikey} />}
              </DialogContentText>
            </DialogContent>
            <DialogActions>
              {!showToken && (
                <Button
                  onClick={this.generateKeys}
                  disabled={!accessTokenRequest.timeout || isGenerating}
                  color="primary"
                  variant='contained'
                  className={classes.button}
                >
                  <FormattedMessage
                    id="Shared.AppsAndKeys.ViewKeys.consumer.generate.btn"
                    defaultMessage="Generate"
                  />
                   {isGenerating && <CircularProgress size={24} />}
                </Button>
              )}
              <Button onClick={this.handleClose} color="primary" autoFocus>
                <FormattedMessage
                  id="Shared.AppsAndKeys.ViewKeys.consumer.close.btn"
                  defaultMessage="Close"
                />
              </Button>
            </DialogActions>
          </Dialog>
        </Grid>
        {restrictSchema === "ip" && (
          <Grid item md={5} xs={12}>
            <Card>
              <CardContent>
                <Typography variant="h5" component="h2">
                  <FormattedMessage
                    id="Shared.AppsAndKeys.ViewKeys.apiKeyRestriction.ip.example.heading"
                    defaultMessage="Examples of IP Addresses allowed"
                  />
                </Typography>
                <Typography variant="body1" component="p" className={classes.cardBody}>
                  <FormattedMessage
                    id="Shared.AppsAndKeys.ViewKeys.apiKeyRestriction.ip.example.content"
                    defaultMessage={
                      "Specify one IPv4 or IPv6 or a subnet using CIDR notation{linebreak}Examples: {ip1}, {ip2}, {ip3} or {ip4}"
                    }
                    values={{
                      linebreak: <br />,
                      ip1: <b>192.168.1.2</b>,
                      ip2: <b>152.12.0.0/13</b>,
                      ip3: <b>2002:eb8::2</b>,
                      ip4: <b>1001:ab8::/44</b>,
                    }}
                  />
                </Typography>

              </CardContent>
            </Card>
          </Grid>
        )}
        {restrictSchema === "referer" && (
          <Grid item md={5} xs={12}>
            <Card>
              <CardContent>
                <Typography variant="h5" component="h2">
                  <FormattedMessage
                    id="Shared.AppsAndKeys.ViewKeys.apiKeyRestriction.referer.example.heading"
                    defaultMessage="Examples of URLs allowed to restrict websites"
                  />
                </Typography>
                <Typography variant="body1" component="p" className={classes.cardBody}>
                  <FormattedMessage
                    id="Shared.AppsAndKeys.ViewKeys.apiKeyRestriction.ip.example.content.message"
                    defaultMessage={
                      "A specific URL with an exact path: {url1}{linebreak}Any URL in a single subdomain, using a wildcard asterisk (*): {url2}{linebreak}Any subdomain or path URLs in a single domain, using wildcard asterisks (*): {url3}"
                    }
                    values={{
                      linebreak: <br />,
                      url1: <b>www.example.com/path</b>,
                      url2: <b>sub.example.com/*</b>,
                      url3: <b>*.example.com/*</b>,
                    }}
                  />
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        )}
      </Grid>
    );
  }
}

ApiKeyManager.propTypes = {
  classes: PropTypes.shape({}).isRequired,
  selectedApp: PropTypes.shape({
    tokenType: PropTypes.string.isRequired,
  }).isRequired,
  keyType: PropTypes.string.isRequired,
  intl: PropTypes.shape({ formatMessage: PropTypes.func }).isRequired,
};

export default injectIntl(withStyles(styles)(ApiKeyManager));
