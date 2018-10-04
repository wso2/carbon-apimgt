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

import React, {Component} from 'react'
import PropTypes from 'prop-types';
import ProductionKey from './ProductionKey'
import SandboxKey from './SandboxKey'
import Subscriptions from './Subscriptions'
import {Route, Switch, Redirect, Link} from 'react-router-dom'
import API from '../../../data/api'
import {PageNotFound} from '../../Base/Errors/index'
import Loading from '../../Base/Loading/Loading'
import ResourceNotFound from "../../Base/Errors/ResourceNotFound";
import { withStyles } from '@material-ui/core/styles';
import CustomIcon from '../../Shared/CustomIcon';
import InfoBar from './InfoBar';



const styles = theme => ({
    linkColor: {
        color: theme.palette.getContrastText(theme.palette.background.leftMenu),
    },
    LeftMenu: {
        backgroundColor: theme.palette.background.leftMenu,
        width: 90,
        textAlign: 'center',
        fontFamily: theme.typography.fontFamily,
        position: 'absolute',
        bottom: 0,
        left: 0,
        top: 0,
    },
    leftLInk: {
        paddingTop: 10,
        paddingBottom: 10,  
        paddingLeft: 5,
        paddingRight: 5,
        fontSize: 11,
        cursor: 'pointer'
    },
    leftLInkMain: {
        borderRight: 'solid 1px ' + theme.palette.background.leftMenu,
        paddingBottom: 5,
        paddingTop: 5,
        height: 60,
        fontSize: 12,
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
        marginLeft: 90,
        paddingBottom: 20,
      }
});
class Details extends Component {

    constructor(props) {
        super(props);
        this.state = {
            application: null,
            active: 'overview',
        };
    }

    componentDidMount() {
        const client = new API();
        let promised_application = client.getApplication(this.props.match.params.application_uuid);
        promised_application.then(
            response => {
                this.setState({application: response.obj});
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

    handleChange = (event, value) => {
        this.setState({value});
        this.props.history.push({pathname: "/applications/" + this.props.match.params.application_uuid + "/" + value});
    };
    handleMenuSelect = (menuLink) => {
        this.props.history.push({pathname: "/applications/" + this.props.match.params.application_uuid + "/" + menuLink});
        //(menuLink === "overview") ? this.infoBar.toggleOverview(true) : this.infoBar.toggleOverview(false) ;
        this.setState({active: menuLink});
    }
    render() {
        let redirect_url = "/applications/" + this.props.match.params.application_uuid + "/productionkeys";

        const  { classes, theme } = this.props;
        const strokeColor = theme.palette.getContrastText(theme.palette.background.leftMenu);

        if (this.state.notFound) {
            return <ResourceNotFound />
        } else if (!this.state.application) {
            return <Loading/>
        }

        return (
            <React.Fragment>
                <div className={classes.LeftMenu}>
                    <Link to={"/apis"}>
                        <div className={classes.leftLInkMain}>
                            <CustomIcon width={52} height={52} icon="applications" />
                        </div>
                    </Link>
                    <div className={classes.leftLInk} 
                        onClick={( () => this.handleMenuSelect('productionkeys') ) }
                        style={{backgroundColor: this.state.active === "productionkeys" ? theme.palette.background.appBar : ''}}
                        >
                        <CustomIcon strokeColor={strokeColor}  width={32} height={32} icon="keys" />
                        <div className={classes.linkColor}>Production Keys</div>
                    </div>
                    <div className={classes.leftLInk} 
                        onClick={( () => this.handleMenuSelect('sandBoxkeys') ) }
                        style={{backgroundColor: this.state.active === "sandBoxkeys" ? theme.palette.background.appBar : ''}}
                        >
                        <CustomIcon strokeColor={strokeColor}  width={32} height={32} icon="keys" />
                        <div className={classes.linkColor}>Sandbox Keys</div>
                    </div>
                    <div className={classes.leftLInk} 
                        onClick={( () => this.handleMenuSelect('subscriptions') ) }
                        style={{backgroundColor: this.state.active === "subscriptions" ? theme.palette.background.appBar : ''}}
                        >
                        <CustomIcon strokeColor={strokeColor}  width={32} height={32} icon="keys" />
                        <div className={classes.linkColor}>Subscriptions</div>
                    </div>
                </div>
            <div className={classes.content}>
              <InfoBar applicationId={this.props.match.params.application_uuid} innerRef={node => this.infoBar = node}/>
                <Switch>
                    <Redirect exact from="/applications/:applicationId" to={redirect_url}/>
                    <Route path="/applications/:applicationId/productionkeys" component={ProductionKey}/>
                    <Route path="/applications/:applicationId/sandBoxkeys" component={SandboxKey}/>
                    <Route path="/applications/:applicationId/subscriptions" component={Subscriptions}/>
                    <Route component={PageNotFound}/>
                </Switch>
              </div>
            </React.Fragment>
            
        );
    }

}

Details.propTypes = {
    classes: PropTypes.object.isRequired,
    theme: PropTypes.object.isRequired,
  };
  
  export default withStyles(styles, { withTheme: true })(Details);
