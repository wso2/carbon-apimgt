import React from 'react'
class Navbar extends React.Component{
  constructor(props){
    super(props);
  }

  render(){
    return <div className="navbar-wrapper">
        <nav className="navbar navbar-default">
            <div className="container-fluid">
                <div className="navbar-header">
                    <button type="button" className="navbar-toggle" aria-controls="navbar">
                        <span className="sr-only">Toggle navigation</span>
                        <span className="icon-bar"></span>
                        <span className="icon-bar"></span>
                        <span className="icon-bar"></span>
                    </button>

                    <a className="navbar-menu-toggle" aria-expanded="false">
                        <span className="icon fw-stack">
                            <i className="fw fw-menu fw-stack-1x"></i>
                        </span>
                    </a>
                    <div className="navbar-brand">
                        <a href="#">Gadgets</a>
                    </div>
                </div>
                <div id="navbar" className="collapse navbar-collapse">
                    <ul className="nav navbar-nav">
                        <li>
                            <a href="#">
                                <i className="fw fw-left fw-helper fw-helper-circle-outline"></i>
                                Go Back
                            </a>
                        </li>
                        <li className="active">
                            <a href="#">
                                <span className="icon fw-stack">
                                    <i className="fw fw-add fw-helper fw-helper-circle-outline"></i>
                                </span>
                                Add
                            </a>
                        </li>
                    </ul>
                    <ul className="nav navbar-nav navbar-right">
                        <li className="dropdown">
                            <a data-toggle="dropdown">
                                <i className="fw fw-notification"></i>
                                <span className="badge badge-bubble">4</span>
                            </a>
                            <ul className="dropdown-menu" role="menu">
                                <li className="dropdown-header"><h5>Notifications</h5></li>
                                <li className="divider"></li>
                                <li><a href="#"><i className="fw fw-success"></i> Lorem ipsum dolor sit amet</a></li>
                                <li><a href="#"><i className="fw fw-error"></i> Consectetur adipiscing elit</a></li>
                                <li><a href="#"><i className="fw fw-warning"></i> Vivamus sagittis elit et orci molestie</a></li>
                                <li><a href="#"><i className="fw fw-error"></i> className aptent taciti sociosqu</a></li>
                                <li className="divider"></li>
                                <li><a href="#">See All</a></li>
                            </ul>
                        </li>
                    </ul>
                </div>
            </div>
        </nav>
    </div>
  }
}

export default Navbar;
