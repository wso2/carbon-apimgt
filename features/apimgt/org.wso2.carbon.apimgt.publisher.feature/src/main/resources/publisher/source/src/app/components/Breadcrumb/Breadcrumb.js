import React from 'react'

class Breadcrumb extends React.Component{
  constructor(props){
    super(props);
  }
  render(){
    return <div className="breadcrumb-wrapper">
        <ol className="breadcrumb">
            <li><a href="/theme-wso2"><i className="icon fw fw-home"></i> <span className="hidden-xs">Home</span></a></li>
            <li className="active"><a href="/theme-wso2/demo/breadcrumb/">Breadcrumb</a></li>
        </ol>
    </div>
  }
}
export default Breadcrumb;
