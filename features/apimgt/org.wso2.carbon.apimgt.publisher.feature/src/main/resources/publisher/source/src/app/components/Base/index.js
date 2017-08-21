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

import Header from './Header/Header'

import Drawer from 'material-ui/Drawer';
import NavBar from  '../Apis/Details/NavBar'
import Grid from 'material-ui/Grid';

const defaultOffset = "250px";


class Layout extends React.Component {
    constructor(props){
        super(props);
        this.state = {
            drawerOpen: true,
            showLeftMenu: false,
            layoutLeftOffset: defaultOffset
        };
        this.updateWindowDimensions = this.updateWindowDimensions.bind(this);
    }
    toggleDrawer = () => {
        if( !this.state.showLeftMenu ) return;
        this.setState({ drawerOpen: !this.state.drawerOpen });
        this.setState({ layoutLeftOffset : (() => {return (this.state.drawerOpen ? "0px" : defaultOffset)})() });
    };

    componentDidMount() {
        if(!this.props.showLeftMenu) { this.setState({layoutLeftOffset:"0px"})}
        this.updateWindowDimensions();
        window.addEventListener('resize', this.updateWindowDimensions);
    }

    componentWillUnmount() {
        window.removeEventListener('resize', this.updateWindowDimensions);
    }
    componentWillReceiveProps(nextProps){
        if(nextProps.showLeftMenu){
            this.setState({showLeftMenu:nextProps.showLeftMenu});
            if(nextProps.showLeftMenu) { this.setState({layoutLeftOffset:defaultOffset})}
        }

    }
    updateWindowDimensions() {
        if(!this.state.showLeftMenu) {
            return;
        }
        window.innerWidth < 650  ?
            this.setState({drawerOpen:false,layoutLeftOffset:"0px"}) :
            this.setState({drawerOpen:true,layoutLeftOffset:defaultOffset});
    }


    render(props){
        //let params = qs.stringify({referrer: props.location.pathname});
        return (
            <div  style={{marginLeft:this.state.layoutLeftOffset}}  >
                <Header toggleDrawer={this.toggleDrawer} showLeftMenu={this.state.showLeftMenu} />
                <Drawer
                    open={this.state.drawerOpen && this.state.showLeftMenu}
                    onRequestClose={this.handleLeftClose}
                    docked={true}
                >
                    <NavBar />
                </Drawer>
                <Grid container spacing={0} >
                    <Grid item xs={12} >
                        {this.props.children}
                    </Grid>
                </Grid>


            </div>
        );
    }

}

export default Layout;