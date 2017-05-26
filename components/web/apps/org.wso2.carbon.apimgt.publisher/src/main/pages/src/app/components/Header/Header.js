import React from 'react'

class Header extends React.Component{
  constructor(props){
    super(props);
  }
  render(){
    return <header className="header header-default">
      <div className="container-fluid">
        <div className="pull-left brand">
          <a href="/theme-wso2">
             <img src="/theme-wso2/images/logo-inverse.svg" alt="wso2" title="wso2" className="logo" />
             <span>Theme </span>
          </a>
        </div>
      </div>
    </header>
  }
}
export default Header;
