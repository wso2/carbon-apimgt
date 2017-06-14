import React from 'react'
import {Link} from 'react-router-dom'

class Leftnav extends React.Component{
  constructor(props){
    super(props);
  }
  render(){
    return <div className="sidebar-wrapper sidebar-nav" data-side="left" data-width="260" data-container=".page-content-wrapper" data-container-divide="true" data-fixed-offset-top="0" data-spy="affix" data-offset-top="80" id="sidebar-theme">
        <div className="nano ">
            <div className="nano-content ">
                <ul className="nav nav-pills nav-stacked pages">
                    <li><Link to="/">Home</Link></li>
                    <li><Link to="/apis">Apis</Link></li>
                    <li><Link to="/statistics">Statistics</Link></li>
                    <li>
                        <a data-toggle="collapse" data-target="#forms" className="collapsed">Forms <span className="arrow"></span></a>
                        <ul className="sub-menu collapse" id="forms">
                            <li><Link to="/topics">Topics</Link></li>
                            <li><Link to="/topics">Topics</Link></li>
                            <li><Link to="/topics">Topics</Link></li>
                        </ul>
                    </li>
                </ul>
            </div>
        </div>
      </div>
  }
}
export default Leftnav;
