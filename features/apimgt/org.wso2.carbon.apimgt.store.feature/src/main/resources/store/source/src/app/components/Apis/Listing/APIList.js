import React from "react";
import qs from "qs";
import Api from "../../../data/api.js";
import ResourceNotFound from "../../Base/Errors/ResourceNotFound";
import PropTypes from "prop-types";
import { withStyles } from "@material-ui/core/styles";
import Select from "@material-ui/core/Select";
import Button from "@material-ui/core/Button";
import MUIDataTable from "mui-datatables";
import Alert from '../../Shared/Alert';

const api = new Api();
let applicationId;
let updateSubscriptions;
const styles = theme => ({
  root: {
    display: "flex"
  },
  buttonGap: {
    marginRight: 10
  }
});
class SubscribeItemObj extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      policies: null,
      policy: null
    };
  }
  componentDidMount() {
    let apiId = this.props.apiId;
    let promised_api = api.getAPIById(apiId);
    promised_api
      .then(response => {
          let policy = null;
          if(response.obj.policies.length > 0 ){
            policy = response.obj.policies[0];
          }
        this.setState({ policies: response.obj.policies, policy });
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
  handleChange = event => {
    this.setState({ policy: event.target.value });
  };
  subscribe = event => {
    const { apiId } = this.props;
    const { policy } = this.state;
      if(!policy){
        Alert.error('Select a policy to subscribe');
        return;
      }
    let promised_subscribe = api.subscribe(apiId, applicationId, policy);
    promised_subscribe.then((response) => {
        if (response.status !== 201) {
            Alert.error('subscription error');
        } else {
            Alert.error('Subuscription successfull');
            updateSubscriptions(applicationId);
        }
        
    }).catch( (error) => {
        Alert.error('subscription error');
    })
  }
  render() {
    const { classes } = this.props;
    const { policies } = this.state;
    return (
      policies && (
        <div className={classes.root}>
          <Button
            variant="contained"
            size="small"
            color="primary"
            className={classes.buttonGap}
            onClick={this.subscribe}
          >
            Subscribe
          </Button>
          <Select value={this.state.policy} onChange={this.handleChange}>
            {policies.map(policy => (
              <option value={policy} key={policy}>
                {policy}
              </option>
            ))}
          </Select>
        </div>
      )
    );
  }
}
SubscribeItemObj.propTypes = {
  classes: PropTypes.object.isRequired
};

let SubscribeItem = withStyles(styles)(SubscribeItemObj);

const columns = [
  {
    name: "id",
    options: {
      display: "excluded"
    }
  },
  {
    name: "Policy",
    options: {
      customBodyRender: (value, tableMeta, updateValue) => {
        if (tableMeta["rowData"]) {
          let apiId = tableMeta["rowData"][0];
          return <SubscribeItem apiId={apiId}  />;
        }
      }
    }
  },
  "name"
];

const options = {
  filterType: "checkbox"
};





class APIList extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      apis: null,
      value: 1,
      order: "asc",
      orderBy: "name",
    };
    this.state.listType = this.props.theme.custom.defaultApiView;
  }

  setListType = value => {
    this.setState({ listType: value });
  };
  componentWillReceiveProps(nextProps) {
    this.setState({ data: nextProps.data });  
  }
  componentDidMount() {
    applicationId = this.props.applicationId;
    updateSubscriptions = this.props.updateSubscriptions;
    let promised_apis = api.getAllAPIs();
    promised_apis
      .then(response => {
          let { subscriptions } = this.props;
          let {count, list} = response.obj;
          let filteredApis = [];
          for (var i=0; i < count; i++){
              let apiIsSubscribed = false;
              for ( var j=0; j< subscriptions.length; j++ ){
                if(list[i].id === subscriptions[j].apiIdentifier){
                    apiIsSubscribed = true;
                }
              }
              if(!apiIsSubscribed){
                  filteredApis.push(list[i]);
              }
          }
        this.setState({ apis: filteredApis });
      })
      .catch(error => {
        let status = error.status;
        if (status === 404) {
          this.setState({ notFound: true });
        } else if (status === 401) {
          this.setState({ isAuthorize: false });
          let params = qs.stringify({
            reference: this.props.location.pathname
          });
          this.props.history.push({ pathname: "/login", search: params });
        }
      });
  }

  render() {
    if (this.state.notFound) {
      return <ResourceNotFound />;
    }

    const { apis } = this.state;
    const { theme } = this.props;

    return (
      apis && (
        <MUIDataTable
          title={"APIs"}
          data={apis}
          columns={columns}
          options= {{selectableRows:false}}
        />
      )
    );
  }
}

APIList.propTypes = {
  classes: PropTypes.object.isRequired,
  theme: PropTypes.object.isRequired
};
export default withStyles(styles, { withTheme: true })(APIList);
