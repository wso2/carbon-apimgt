import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Credentials from './Credentials/Credentials';
import Comments from './Comments/Comments';

const styles = theme => ({
    root: {
        flexGrow: 1,
        backgroundColor: theme.palette.background.paper,
    },
});

class Overview extends React.Component {
  state = {
      value: 0,
  };

  handleExpandClick = () => {
      this.setState(state => ({ expanded: !state.expanded }));
  };


  render() {
      const { classes, api } = this.props;
      const { value } = this.state;
      const apiId = this.props.match.params.api_uuid;
      return (
          <React.Fragment>
              <Credentials />
              <Comments api={api} apiId={apiId} />
          </React.Fragment>
      );
  }
}

Overview.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(Overview);
