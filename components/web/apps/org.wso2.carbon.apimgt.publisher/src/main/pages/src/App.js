import React, {Component} from 'react'
import ReactDom from 'react-dom'
import {Base, Listing, Apis, Breadcrumb, Footer, Navbar, Header, Leftnav} from "./app/components/index.js"
import {BrowserRouter as Router, Route, Link, Switch} from 'react-router-dom'
import Utils from '../src/app/data/utils.js'
import './App.css'

const Home = () => (
    <div>
        <h2>Home</h2>
    </div>
);

class Publisher extends Component {
    constructor() {
        super();
        this.getName = this.getName.bind(this);
        this.state = {
            enteredText: ""
        }
    }

    componentDidMount() {
        Utils.autoLogin(); // TODO: Remove once login page is implemented
    }

    getName(event) {
        this.setState(
            {enteredText: this.nameInput.value}
        );
    }

    render() {
        return (
            <Router>
                <div>
                    <Header />
                    <Breadcrumb />
                    <div className="page-content-wrapper">
                        <Leftnav />
                        <Navbar />
                        <div className="container-fluid content-section">
                            <div className="body-wrapper">
                                <Route exact path="/" component={Apis}/>
                                <Route path="/apis" component={Apis}/>

                            </div>
                        </div>
                    </div>
                    <Footer />
                </div>
            </Router>
        );
    }
}

export default Publisher;
