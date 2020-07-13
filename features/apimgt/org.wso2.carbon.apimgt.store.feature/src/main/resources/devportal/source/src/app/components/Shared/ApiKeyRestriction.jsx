/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import React, { useState } from "react";
import { injectIntl, FormattedMessage } from "react-intl";
import PropTypes from "prop-types";
import { withStyles } from "@material-ui/core/styles";
import TextField from "@material-ui/core/TextField";
import FormControlLabel from "@material-ui/core/FormControlLabel";
import IconButton from "@material-ui/core/IconButton";
import DeleteIcon from "@material-ui/icons/Delete";
import Tooltip from "@material-ui/core/Tooltip";
import Grid from "@material-ui/core/Grid";
import Fab from "@material-ui/core/Fab";
import AddIcon from "@material-ui/icons/Add";
import List from "@material-ui/core/List";
import ListItem from "@material-ui/core/ListItem";
import ListItemSecondaryAction from "@material-ui/core/ListItemSecondaryAction";
import ListItemText from "@material-ui/core/ListItemText";
import Box from "@material-ui/core/Box";
import Radio from "@material-ui/core/Radio";
import RadioGroup from "@material-ui/core/RadioGroup";
import FormControl from "@material-ui/core/FormControl";
import Typography from "@material-ui/core/Typography";
import Validation from 'AppData/Validation';

const styles = (theme) => ({
  FormControl: {
    "margin-bottom": theme.spacing(1),
    width: "100%",
    padding: theme.spacing(0, 1),
  },
  outterBox: {
    margin: theme.spacing(1),
    padding: theme.spacing(1),
    borderColor: theme.palette.text.secondary,
    marginLeft: 20,
    borderColor: '#cccccc',
  },
  Fab: {
    marginLeft: theme.spacing(2),
    marginRight: theme.spacing(2),
  },
});

/**
 * Used to display IP address and Http Referer restrictions in generate api key UI
 */
const apiKeyRestrictions = (props) => {
  const [invalidIP, setInvalidIP] = useState(false);
  const [invalidReferer, setInvalidReferer] = useState(false);

  const {
    intl,
    classes,
    newIP,
    updateNewIp,
    ipList,
    updateIpList,
    restrictSchema,
    updateRestrictSchema,
    newReferer,
    updateNewReferer,
    refererList,
    updateRefererList,
  } = props;

  const onRefererTextUpdate = (e) => {
    updateNewReferer(e.target.value.trim());
    if (e.target.value.trim() === "") {
      setInvalidReferer(false);
    }
  };

  const addRefererItem = () => {
    if (newReferer !== null && newReferer !== "") {
      setInvalidReferer(false);
      refererList.push(newReferer.trim());
      updateRefererList(refererList);
      updateNewReferer("");
    } else {
      setInvalidReferer(true);
    }
  };

  const deleteRefererItem = (refererItem) => {
    refererList.splice(refererList.indexOf(refererItem), 1);
    updateRefererList(refererList);
  };

  const onIpTextUpdate = (e) => {
    updateNewIp(e.target.value.trim());
    if (e.target.value.trim() === "") {
      setInvalidIP(false);
    }
  };

  const addIpItem = () => {
    if (newIP !== null && newIP !== "") {
      if (Validation.ipAddress.validate(newIP).error) {
        setInvalidIP(true);
      } else {
        setInvalidIP(false);
        ipList.push(newIP);
        updateIpList(ipList);
        updateNewIp("");
      }
    }
  };

  const deleteIpItem = (ipItem) => {
    ipList.splice(ipList.indexOf(ipItem), 1);
    updateIpList(ipList);
  };

  const onRestrictSchemaChange = (e) => {
    updateRestrictSchema(e.target.value);
    updateIpList([]);
    updateRefererList([]);
    updateNewIp("");
    updateNewReferer("");
    setInvalidIP(false);
    setInvalidReferer(false);
  };

  return (
    <React.Fragment>
      <Box border={1} borderRadius={5} className={classes.outterBox}>
        <Typography variant="body1">
          <FormattedMessage
              defaultMessage='Key Restrictions'
              id='Shared.ApiKeyRestriction.key.restrictions'
          />
        </Typography>
        <FormControl component="fieldset">
          <RadioGroup
            aria-label="API Key Restrictions"
            value={restrictSchema}
            row
            onChange={onRestrictSchemaChange}
          >
            <FormControlLabel
              value="none"
              control={<Radio color="primary" />}
              label="None"
              labelPlacement="end"
            />
            <FormControlLabel
              value="ip"
              control={<Radio color="primary" />}
              label="IP Addresses"
              labelPlacement="end"
            />
            <FormControlLabel
              value="referer"
              control={<Radio color="primary" />}
              label="HTTP Referrers (Web Sites)"
              labelPlacement="end"
            />
          </RadioGroup>
        </FormControl>

        {restrictSchema === "ip" && (
          <Box component="div" id="ipPanel">
            <Grid
              container
              direction="row"
              spacing={0}
              justify="left"
              alignItems="left"
            >
              <Grid item md={10} xs={10}>
                <TextField
                  label="IP Address"
                  value={newIP}
                  onChange={onIpTextUpdate}
                  className={classes.inputText}
                  helperText={
                    invalidIP
                      ? intl.formatMessage({
                          defaultMessage: "Invalid IP Address",
                          id:
                            "Shared.AppsAndKeys.Tokens.apiKeyRestriction.ip.validity.error",
                        })
                      : ""
                  }
                  error={invalidIP}
                  margin="dense"
                  variant="outlined"
                  placeholder={intl.formatMessage({
                    defaultMessage: "Enter IP Address",
                    id: "Shared.AppsAndKeys.Tokens.apiKeyRestriction.enter.ip",
                  })}
                  fullWidth
                />
              </Grid>
              <Grid item md={2} xs={2}>
                <span>
                  <Fab
                    className={classes.Fab}
                    size="small"
                    color="primary"
                    aria-label="add"
                    onClick={addIpItem}
                  >
                    <AddIcon />
                  </Fab>
                </span>
              </Grid>
            </Grid>
            <Grid
              container
              direction="row"
              spacing={0}
              justify="left"
              alignItems="left"
              md={10}
              xs={10}
            >
              {ipList.length > 0 && (
                <List>
                  {ipList.map((ip, index) => (
                    <ListItem>
                      <ListItemText primary={ip} />
                      <ListItemSecondaryAction>
                        <Tooltip title="Delete task" placement="top">
                          <IconButton
                            edge="end"
                            aria-label="delete"
                            onClick={() => deleteIpItem(ip)}
                          >
                            <DeleteIcon />
                          </IconButton>
                        </Tooltip>
                      </ListItemSecondaryAction>
                    </ListItem>
                  ))}
                </List>
              )}
            </Grid>
          </Box>
        )}

        {restrictSchema === "referer" && (
          <Box component="div" id="refererPanel">
            <Grid
              container
              direction="row"
              spacing={0}
              justify="left"
              alignItems="left"
            >
              <Grid item md={10} xs={10}>
                <TextField
                  label="Referer"
                  value={newReferer}
                  onChange={onRefererTextUpdate}
                  className={classes.inputText}
                  helperText={
                    invalidReferer
                      ? intl.formatMessage({
                          defaultMessage: "Invalid Http Referer",
                          id: "Shared.AppsAndKeys.Tokens.apiKeyRestriction.referer.validity.error",
                        })
                      : ""
                  }
                  error={invalidReferer}
                  margin="dense"
                  variant="outlined"
                  placeholder={intl.formatMessage({
                    defaultMessage: "Enter Http Referer",
                    id: "Shared.AppsAndKeys.Tokens.apiKeyRestriction.enter.referer",
                  })}
                  fullWidth
                />
              </Grid>
              <Grid item md={2} xs={2}>
                <span>
                  <Fab
                  size="small"
                    className={classes.Fab}
                    color="primary"
                    aria-label="add"
                    onClick={addRefererItem}
                  >
                    <AddIcon />
                  </Fab>
                </span>
              </Grid>
            </Grid>
            <Grid
              container
              direction="row"
              spacing={0}
              justify="left"
              alignItems="left"
              md={10}
              xs={10}
            >
              {refererList.length > 0 && (
                <List>
                  {refererList.map((referer, index) => (
                    <ListItem>
                      <ListItemText primary={referer} />
                      <ListItemSecondaryAction>
                        <Tooltip title="Delete task" placement="top">
                          <IconButton
                            edge="end"
                            aria-label="delete"
                            onClick={() => deleteRefererItem(referer)}
                          >
                            <DeleteIcon />
                          </IconButton>
                        </Tooltip>
                      </ListItemSecondaryAction>
                    </ListItem>
                  ))}
                </List>
              )}
            </Grid>
          </Box>
        )}
      </Box>
    </React.Fragment>
  );
};
apiKeyRestrictions.contextTypes = {
  intl: PropTypes.shape({}).isRequired,
};
export default injectIntl(withStyles(styles)(apiKeyRestrictions));
