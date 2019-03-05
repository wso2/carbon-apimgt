import React from "react";
import { Link } from "react-router-dom";
import PropTypes from "prop-types";
import Typography from "@material-ui/core/Typography";
import Grid from "@material-ui/core/Grid";
import { withStyles } from "@material-ui/core/styles";
import classnames from "classnames";
import { ApiContext } from "./ApiContext";
import CustomIcon from "../../Shared/CustomIcon";

const styles = theme => ({
  root: {
    padding: theme.spacing.unit * 3,
    width: theme.custom.contentAreaWidth
  },
  boxTop1: {
    borderTop: theme.custom.overviewBox1.borderStyle,
    borderLeft: theme.custom.overviewBox1.borderStyle,
    borderRight: theme.custom.overviewBox1.borderStyle,
    padding: theme.spacing.unit * 2,
    backgroundColor: theme.custom.overviewBox1.backColorTop,
    color: theme.palette.getContrastText(theme.custom.overviewBox1.backColorTop)
  },
  boxBottom1: {
    border: theme.custom.overviewBox1.borderStyle,
    backgroundColor: theme.custom.overviewBox1.backColorBottom,
    color: theme.palette.getContrastText(
      theme.custom.overviewBox1.backColorBottom
    ),
    padding: theme.spacing.unit * 2,
    paddingTop: theme.spacing.unit,
    paddingBottom: theme.spacing.unit
  },
  boxBottom1Link: {
    color: theme.palette.getContrastText(
      theme.custom.overviewBox1.backColorBottom
    ),
    textDecoration: "none"
  },
  boxTop2: {
    borderTop: theme.custom.overviewBox2.borderStyle,
    borderLeft: theme.custom.overviewBox2.borderStyle,
    borderRight: theme.custom.overviewBox2.borderStyle,
    padding: theme.spacing.unit * 2,
    backgroundColor: theme.custom.overviewBox2.backColorTop,
    color: theme.palette.getContrastText(theme.custom.overviewBox2.backColorTop)
  },
  boxBottom2: {
    border: theme.custom.overviewBox2.borderStyle,
    backgroundColor: theme.custom.overviewBox2.backColorBottom,
    color: theme.palette.getContrastText(
      theme.custom.overviewBox2.backColorBottom
    ),
    padding: theme.spacing.unit * 2,
    paddingTop: theme.spacing.unit,
    paddingBottom: theme.spacing.unit
  },
  boxBottom2Link: {
    color: theme.palette.getContrastText(
      theme.custom.overviewBox2.backColorBottom
    ),
    textDecoration: "none"
  },
  iconClass: {
    marginRight: 10
  },
  boxWrapper: {
    marginLeft: 10
  },
  veryBigText: {
    fontSize: 60
  },
  LinkToSomething1: {
    color: theme.palette.getContrastText(
      theme.custom.overviewBox1.backColorTop
    ),
    fontSize: 11
  },
  boxTopSize: {
    minHeight: 115
  }
});

class Overview extends React.Component {
  state = {
    value: 0
  };

  handleExpandClick = () => {
    this.setState(state => ({ expanded: !state.expanded }));
  };

  render() {
    const { classes, api } = this.props;
    const { value } = this.state;
    const apiId = this.props.match.params.api_uuid;
    return (
      <ApiContext.Consumer>
        {({ api, applicationsAvailable, subscribedApplications }) => (
          <Grid container className={classes.root} spacing={16}>
            <Grid item xs={subscribedApplications.length === 0 ? 6 : 12}>
              <Typography variant="h4" gutterBottom>
                <CustomIcon
                  strokeColor="#919191"
                  className={classes.iconClass}
                  width={30}
                  height={30}
                  icon="credentials"
                />
                Credentials
              </Typography>
              <Typography variant="body1" gutterBottom>
                API Credentials are grouped in to applications. An application
                is primarily used to decouple the consumer from the APIs. It
                allows you to Generate and use a single key for multiple APIs
                and subscribe multiple times to a single API with different SLA
                levels.
              </Typography>
              <Grid container spacing={16}>
                {subscribedApplications.length > 0 && (
                  <Grid item xs={6}>
                    <div
                      className={classnames(
                        classes.boxTop1,
                        classes.boxTopSize
                      )}
                    >
                      <Typography variant="h6" gutterBottom>
                        Subscribed Applications
                      </Typography>
                      <Typography
                        component="h2"
                        variant="h1"
                        className={classes.veryBigText}
                      >
                        {subscribedApplications.length}
                      </Typography>
                    </div>
                    <div className={classes.boxBottom1}>
                      <Link to="/" className={classes.boxBottom1Link}>
                        View Subscriptions >
                      </Link>
                    </div>
                  </Grid>
                )}
                <Grid item xs={subscribedApplications.length > 0 ? 6 : 12}>
                  <div
                    className={classnames(classes.boxTop1, classes.boxTopSize)}
                  >
                    <Typography variant="h6" gutterBottom>
                      Subscribe to an Application
                    </Typography>
                    {applicationsAvailable.length > 0 && (
                      <React.Fragment>
                        <Link to="/" className={classes.LinkToSomething1}>
                          With an Existing Application
                        </Link>
                        <Typography variant="caption" gutterBottom>
                          {applicationsAvailable.length} Available
                        </Typography>
                      </React.Fragment>
                    )}
                    <Link to="/" className={classes.LinkToSomething1}>
                      With a New Application
                    </Link>
                  </div>
                  <div className={classes.boxBottom1}>
                    <Link to="/" className={classes.boxBottom1Link}>
                      Subscribe >
                    </Link>
                  </div>
                </Grid>
              </Grid>
            </Grid>
            <Grid item xs={subscribedApplications.length === 0 ? 6 : 12}>
              <Typography variant="h4" gutterBottom style={{ marginTop: 10 }}>
                <CustomIcon
                  strokeColor="#919191"
                  className={classes.iconClass}
                  width={30}
                  height={30}
                  icon="test"
                />
                Test
              </Typography>
              <div
                className={classes.boxTop2}
                style={{
                  marginTop: subscribedApplications.length === 0 ? 55 : 0,
                  minHeight: subscribedApplications.length === 0 ? 115 : 0
                }}
              >
                <Typography variant="body1" gutterBottom>
                  You can use the embedded swagger console to test this API.
                </Typography>
              </div>
              <div className={classes.boxBottom2}>
                <Link to="/" className={classes.boxBottom2Link}>
                  Go to Swagger Console >
                </Link>
              </div>
            </Grid>
            {/* Docs */}

            <Grid item xs={subscribedApplications.length === 0 ? 6 : 12}>
              <Typography variant="h4" gutterBottom>
                <CustomIcon
                  strokeColor="#919191"
                  className={classes.iconClass}
                  width={30}
                  height={30}
                  icon="docs"
                />
                Docs
              </Typography>
              <Grid container spacing={16}>
                {subscribedApplications.length > 0 && (
                  <Grid item xs={6}>
                    <div
                      className={classnames(
                        classes.boxTop1,
                        classes.boxTopSize
                      )}
                    >
                      <Typography variant="h6" gutterBottom>
                        Latest Update
                      </Typography>
                      <Link to="/" className={classes.LinkToSomething1}>
                        aboutmyApi.pdf
                      </Link>
                      <Typography variant="caption" gutterBottom>
                        Last updated 23min ago.
                      </Typography>
                    </div>
                    <div className={classes.boxBottom1}>
                      <Link to="/" className={classes.boxBottom1Link}>
                        View All DOCS >
                      </Link>
                    </div>
                  </Grid>
                )}
                <Grid item xs={subscribedApplications.length > 0 ? 6 : 12}>
                  <div
                    className={classnames(classes.boxTop1, classes.boxTopSize)}
                  >
                    <Typography variant="h6" gutterBottom>
                      Docs for this API
                    </Typography>
                    <Typography
                        component="h2"
                        variant="h1"
                        className={classes.veryBigText}
                        >
                        2
                    </Typography>
                  </div>
                  <div className={classes.boxBottom1}>
                    <Link to="/" className={classes.boxBottom1Link}>
                      View All Docs >
                    </Link>
                  </div>
                </Grid>
              </Grid>
            </Grid>

            {/* Comments */}

            <Grid item xs={6}>
              <Typography variant="h4" gutterBottom style={{ marginTop: 10 }}>
                <CustomIcon
                  strokeColor="#919191"
                  className={classes.iconClass}
                  width={30}
                  height={30}
                  icon="comments"
                />
                Comments
              </Typography>

              <div className={classnames(classes.boxTop2, classes.boxTopSize)}>
                <Typography variant="h6" gutterBottom>
                  Comments For This API
                </Typography>
                <Typography
                  component="h2"
                  variant="h1"
                  className={classes.veryBigText}
                >
                  21
                </Typography>
              </div>
              <div className={classes.boxBottom2}>
                <Link to="/" className={classes.boxBottom2Link}>
                  Go To Comments >
                </Link>
              </div>
            </Grid>
            {/* Forum */}
            <Grid item xs={6}>
              <Typography variant="h4" gutterBottom style={{ marginTop: 10 }}>
                <CustomIcon
                  strokeColor="#919191"
                  className={classes.iconClass}
                  width={30}
                  height={30}
                  icon="comments"
                />
                Forum
              </Typography>

              <div className={classnames(classes.boxTop2, classes.boxTopSize)}>
                <Typography variant="h6" gutterBottom>
                  Forum Topics For This API
                </Typography>
                <Typography
                  component="h2"
                  variant="h1"
                  className={classes.veryBigText}
                >
                  2
                </Typography>
              </div>
              <div className={classes.boxBottom2}>
                <Link to="/" className={classes.boxBottom1Link}>
                  View Forum >
                </Link>
              </div>
            </Grid>
            <Grid item xs={12}>
              <Typography variant="h4" gutterBottom style={{ marginTop: 10 }}>
                <CustomIcon
                  strokeColor="#919191"
                  className={classes.iconClass}
                  width={30}
                  height={30}
                  icon="sdk"
                />
                SDK Generation
              </Typography>
              <div
                className={classes.boxTop1}
                style={{
                  marginTop: subscribedApplications.length === 0 ? 55 : 0,
                  minHeight: subscribedApplications.length === 0 ? 115 : 0
                }}
              >
                <Typography variant="body1" gutterBottom>
                    If you wants to create a software application to consume the subscribed APIs, you can generate client side SDK for a supported language/framework and use it as a start point to write the software application.                
                </Typography>
                <Link to="/" className={classes.LinkToSomething1}>
                    PYTHON, ANDROID, JAVA
                </Link>
                <Typography variant="caption" gutterBottom>
                    Supported lanuage/frameworks
                </Typography>

              </div>
              <div className={classes.boxBottom1}>
                <Link to="/" className={classes.boxBottom1Link}>
                    Download SDKs >
                </Link>
              </div>
            </Grid>
          </Grid>
        )}
      </ApiContext.Consumer>
    );
  }
}

Overview.propTypes = {
  classes: PropTypes.object.isRequired
};

export default withStyles(styles)(Overview);
