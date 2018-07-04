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

import React from 'react';
import { Link } from 'react-router-dom';
import API from '../../../data/api';

import { TableCell, TableRow } from 'material-ui/Table';
import { withStyles } from 'material-ui/styles';
import PropTypes from 'prop-types';
import Typography from 'material-ui/Typography';
import StarRatingBar from './StarRating';

const styles = theme => ({
  root: {
    borderBottom: `1px transparent`
  }
});

class APiTableRow extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      active: true,
      loading: false,
      collapseOpen: false,
      open: false,
      subscribedApplications: [],
      policies: [],
      description: null,
      rating: 0
    };
    this.handleApiDelete = this.handleApiDelete.bind(this);
  }
  /**
   * Handle click event of table row to change the state of its collapsible row.
   */
  handleClick = () => {
    this.setState(state => ({
      collapseOpen: !state.collapseOpen
    }));
  };

  handleRequestClose = () => {
    this.setState({ openUserMenu: false });
  };

  handleApiDelete(e) {
    this.setState({ loading: true });
    const api = new API();
    const api_uuid = this.props.api.id;
    const name = this.props.api.name;
    let promised_delete = api.deleteAPI(api_uuid);
    promised_delete.then(response => {
      if (response.status !== 200) {
        console.log(response);
        message.error(
          "Something went wrong while deleting the " + name + " API!"
        );
        this.setState({ open: false });
        return;
      }
      message.success(name + " API deleted successfully!");
      this.setState({ active: false, loading: false });
    });
  }

  componentDidMount() {
    const api = new API();
    // Get api details, subscribed applications and user rating
    let promised_api = api.getAPIById(this.props.api.id);
    let existing_subscriptions = api.getSubscriptions(this.props.api.id, null);
    let promised_applications = api.getAllApplications();
    let promised_rating = api.getRatingFromUser(this.props.api.id, null);

    Promise.all([
      promised_api,
      existing_subscriptions,
      promised_applications,
      promised_rating
    ])
      .then(response => {
        let [api, subscriptions, applications, rating] = response.map(
          data => data.obj
        );
        this.setState({
          policies: api.policies,
          description: api.description,
          rating: rating.userRating
        });
        let subscribedApplications = subscriptions.list.map(element => ({
          value: element.applicationId
        }));
        // Get the application names of subscribed aplications
        for (let i = 0; i < applications.list.length; i++) {
          let applicationId = applications.list[i].applicationId;
          let applicationName = applications.list[i].name;
          for (var j = 0; j < subscribedApplications.length; j++) {
            if (subscribedApplications[j].value === applicationId) {
              subscribedApplications[j].label = applicationName;
            }
          }
        }
        this.setState({ subscribedApplications });
      })
      .catch(error => {
        if (process.env.NODE_ENV !== "production") {
          console.log(error);
        }
        let status = error.status;
        if (status === 404) {
          this.setState({ notFound: true });
        }
      });
  }

  render() {
    const { name, version, context, id } = this.props.api;
    const details_link = "/apis/" + id;
    const {
      collapseOpen,
      subscribedApplications,
      rating,
      description,
      policies
    } = this.state;
    if (!this.state.active) {
      // Controls the delete state, We set the state to inactive on delete success call
      return null;
    }
    const { classes } = this.props;
    const apiRows = [
      <TableRow hover onClick={this.handleClick} key={"row-data-" + id}>
        <TableCell className={collapseOpen ? classes.root : null}>
          <Link to={details_link}>{name}</Link>
        </TableCell>
        <TableCell className={collapseOpen ? classes.root : null}>
          {version}
        </TableCell>
        <TableCell className={collapseOpen ? classes.root : null}>
          {context}
        </TableCell>
        <TableCell className={collapseOpen ? classes.root : null}>
          <StarRatingBar rating={rating} />
        </TableCell>
      </TableRow>
    ];

    // If the state of collapsible row is open, render the row with additional details of the api
    if (collapseOpen) {
      apiRows.push(
        <TableRow key={"row-data-child-" + id}>
          <TableCell colSpan="4">
            <Typography variant="body2">{description}</Typography>
            <Typography variant="body2">
              Policies:
              {policies.length === 0 ? (
                <span>&lt; NOT SET FOR THIS API &gt;</span>
              ) : null}
              {policies.map((policy, index) => (
                <span key={"span-policy-" + index}>
                  {" " + policy}
                  {policies.length !== 1 &&
                    index !== policies.length - 1 && <span>,</span>}
                </span>
              ))}
            </Typography>
            <Typography variant="body2">
              Subscribed Apps:
              {subscribedApplications.length === 0 ? (
                <span>&lt; NOT SET FOR THIS API &gt;</span>
              ) : null}
              {subscribedApplications.map((app, index) => (
                <Link
                  to={"/applications/" + app.value}
                  key={"app-link-" + app.value}
                >
                  {" " + app.label}
                  {subscribedApplications.length !== 1 &&
                    index !== subscribedApplications.length - 1 && (
                      <span>,</span>
                    )}
                </Link>
              ))}
            </Typography>
          </TableCell>
        </TableRow>
      );
    }
    return apiRows;
  }
}

APiTableRow.propTypes = {
  classes: PropTypes.object.isRequired
};
export default withStyles(styles)(APiTableRow);
