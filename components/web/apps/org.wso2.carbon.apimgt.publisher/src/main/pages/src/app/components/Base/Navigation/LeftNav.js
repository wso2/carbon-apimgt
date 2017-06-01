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
                            <li><Link to="/apis"> APIs </Link></li>
                            <li><a className="icon" href="#"><i className="fw fw-statistics"/> Statistics</a></li>
                            <li><a className="icon" href="#"><i className="fw fw-subscribe"/> Subscriptions</a></li>
                            <li className="active"><a href="#">Test</a></li>
                            <li>
                                <a data-toggle="collapse" data-target="#forms" className="collapsed">Sub <span
                                    className="arrow"/></a>
                                <ul className="sub-menu collapse" id="forms">
                                    <li><a className="icon" href="#"><i className="fw fw-view"/> Overview</a></li>
                                    <li><a className="icon" href="#"><i className="fw fw-lifecycle"/> Lifecycle</a></li>
                                    <li><a className="icon" href="#"><i className="fw fw-endpoint"/> Endpoints</a></li>
                                    <li><a className="icon" href="#"><i className="fw fw-resource"/> Resources</a></li>
                                    <li><a className="icon" href="#"><i className="fw fw-document"/> Documents</a></li>
                                    <li><a className="icon" href="#"><i className="fw fw-checklist"/> Access Control</a>
                                    </li>
                                    <li><a className="icon" href="#"><i className="fw fw-message"/> Mediation</a></li>
                                    <li><a className="icon" href="#"><i className="fw fw-code"/> Scripting</a></li>
                                    <li><a className="icon" href="#"><i className="fw fw-subscribe"/> Subscriptions</a>
                                    </li>
                                    <li><a className="icon" href="#"><i className="fw fw-display"/> API Console</a></li>
                                </ul>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default LeftNav