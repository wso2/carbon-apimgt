import React from 'react'
import {Route, Switch, BrowserRouter as Router} from 'react-router-dom'

import Listing from './Listing/Listing'
import Details from './Details/index'
import {PageNotFound} from '../Base/Errors/index'

const Apis = (props) => {
    return (
        <div>
            <Switch>
                <Route exact path={"/apis"} component={Listing}/>
                <Route path={"/apis/:api_uuid/"} component={Details}/>
                <Route component={PageNotFound}/>
            </Switch>
        </div>
    );
}

export default Apis;
