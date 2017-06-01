import React, {Component} from 'react'
import Footer from './Footer/Footer'
import Header from './Header/Header'

import LeftNav from './Navigation/LeftNav'

class Base extends Component {

    render() {
        return (
            <div>
                <Header/>
                <div className="page-content-wrapper sidebar-target">
                    <LeftNav/>
                    {/* page content */}
                    <div className="container-fluid">
                        <div className="body-wrapper">
                            {this.props.children}
                        </div>
                    </div>
                </div>
                <Footer/>
            </div>
        );
    }
}

export default Base;