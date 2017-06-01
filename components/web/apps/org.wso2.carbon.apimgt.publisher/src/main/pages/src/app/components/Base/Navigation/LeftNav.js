import React, {Component} from 'react'
import {Link} from 'react-router-dom'

const LeftNav = (props) => {
    return (
        <div>
            {/* .left-sidebar */}
            <div className="sidebar-wrapper sidebar-nav affix-top" data-side="left" data-width={260}
                 data-container=".page-content-wrapper" data-container-divide="true" data-fixed-offset-top={0}
                 data-spy="affix" data-offset-top={80} id="left-sidebar">
                <div className="nano ">
                    <div className="nano-content ">
                        <ul className="nav nav-pills nav-stacked pages">
                            <li><Link to="/apis"><i className="fw fw-api"/> APIs</Link></li>
                            <li><a className="icon" href="#"><i className="fw fw-statistics"/> Statistics</a></li>
                            <li><a className="icon" href="#"><i className="fw fw-subscribe"/> Subscriptions</a></li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default LeftNav