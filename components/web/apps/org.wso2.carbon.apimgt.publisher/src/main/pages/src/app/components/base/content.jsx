import React, {Component} from 'react';

class BaseTheme extends Component {

    render() {
        return (
            <div>
                <div className="breadcrumb-wrapper">
                    <ol className="breadcrumb">
                        <li>
                            <a href="/" title="Home">
                                <i className="icon fw fw-home"/>
                                <span className="hidden-xs">Home</span></a>
                        </li>
                        <li className="active">
                            <a href="/apis" title="API List">APIs</a>
                        </li>
                    </ol>
                </div>

                <div className="page-content-wrapper">
                    <div className="navbar-wrapper">
                        <nav className="navbar navbar-default" data-spy="affix"
                             data-offset-top="80" data-offset-bottom="40">
                            <div className="container-fluid">
                                <div className="navbar-header">
                                    <button type="button" className="navbar-toggle collapsed"
                                            data-toggle="collapse" data-target="#navbar" aria-expanded="false"
                                            aria-controls="navbar">
                                        <span className="sr-only">Toggle navigation</span> <span className="icon-bar"/>
                                        <span className="icon-bar"/> <span className="icon-bar"/>
                                    </button>
                                    <a className="navbar-menu-toggle collapsed hidden"
                                       data-toggle="collapse" data-target="#navbar2" aria-expanded="false"
                                       aria-controls="navbar2" title="navigator bar"> <span className="icon fw-stack">
                                        <i className="fw fw-tiles fw-stack-1x toggle-icon-up"/>
                                    </span>
                                    </a>
                                    <a className="navbar-menu-toggle" data-toggle="sidebar" data-target="#left-sidebar"
                                       aria-expanded="false" rel="leftmenu-sidebar">
                                        <span className="icon fw-stack">
                                            <i className="fw fw-menu fw-stack-1x toggle-icon-left-arrow"/>
                                        </span>
                                    </a>
                                </div>
                            </div>
                        </nav>
                    </div>
                    <div className="container-fluid">
                        <div className="body-wrapper" id="bodyWrapper">
                            {this.props.children}
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

export default BaseTheme;