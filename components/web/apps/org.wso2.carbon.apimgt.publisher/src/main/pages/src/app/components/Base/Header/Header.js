import React from 'react'
import {Link} from "react-router-dom";

const Header = (props) => {
    return (
        <header className="header header-default">
            <div className="container-fluid">
                <div id="nav-icon1" className="menu-trigger navbar-left " data-toggle="sidebar"
                     data-target="#left-sidebar" data-container=".page-content-wrapper" data-container-divide="true"
                     aria-expanded="false" rel="sub-nav">
                    <span />
                    <span />
                    <span />
                </div>
                <div className="pull-left brand">
                    <Link to="/">
                        <span>APIM Publisher</span>
                    </Link>
                </div>
                <ul className="nav navbar-right">
                    <li className="visible-inline-block">
                        <a className="dropdown" data-toggle="dropdown" aria-expanded="false">
                <span className="icon fw-stack">
                  <i className="fw fw-circle fw-stack-2x"/>
                  <i className="fw fw-user fw-stack-1x fw-inverse"/>
                </span>
                            <span className="hidden-xs add-margin-left-1x">John Doe <span className="caret"/></span>
                        </a>
                        <ul className="dropdown-menu dropdown-menu-right slideInDown" role="menu">
                            <li><a href="#">Profile Settings</a></li>
                            <li><a href="#">Logout</a></li>
                        </ul>
                    </li>
                </ul>
            </div>
        </header >
    );
};

export default Header