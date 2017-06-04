import React, {Component} from 'react'

class Overview extends Component {
    constructor(props) {
        super(props);
        this.state = {
            api: props.api
        }
    }

    componentWillReceiveProps(nextProps) {
        this.setState(
            {
                api: nextProps.api
            }
        );

    }

    render() {
        return (
            <div>
                <div role="tabpanel" className="tab-pane fade active in" id="overview-tab">
                    <div className="card">
                        <div id="overview-content">
                            <div className="row">
                                <div className="col-sm-2 col-md-2">
                                    <div className="thumbnail">
                                        <div className="square-element setbgcolor"
                                             style={{background: 'rgb(92, 107, 192)'}}>
                                            <div className="api-name-icon text-uppercase"
                                                 style={{fontSize: '900%', color: 'white', textAlign: 'center'}}>A
                                            </div>
                                            <div style={{display: 'none'}}>
                                                <a className="api-name" title="exsample_4">{this.state.api.name}</a>
                                            </div>
                                        </div>
                                        <div className="caption">
                                            <h1 className="text-primary">{this.state.api.name}
                                                <small>{this.state.api.version}</small>
                                            </h1>
                                            <ul className="nav nav-list">
                                                <li>
                                                    <a href="#" title="User">
                                                        <i className="fw fw-user" title="User"/>
                                                        <span className="userCount"><b>N/A</b> Users</span>
                                                    </a>
                                                </li>
                                                <li>
                                                    <a href="#" title="Published">
                            <span id="status"><i className="fw fw-lifecycle" title={this.state.api.lifeCycleStatus}/>
                                &nbsp;{this.state.api.lifeCycleStatus}</span>
                                                    </a>
                                                </li>
                                                <li>
                                                    <a href="#" title="Docs">
                                                        <i className="fw fw-document" title="Docs"/> Docs
                                                    </a>
                                                </li>
                                                <li>
                                                    <a target="_blank"
                                                       href="/store/apis/a554f29f-3d76-4a75-94c8-cd0b9b8b79b5"
                                                       id="goToStore" title="View in Store">
                                                        <i className="fw fw-store"/>&nbsp;View in Store
                                                    </a>
                                                </li>
                                            </ul>
                                        </div>
                                    </div>
                                </div>
                                <div className="col-md-8">
                                    <table className="table table-bordered">
                                        <tbody>
                                        <tr>
                                            <td>Visibility</td>
                                            <td id="inUrl">
                                                {this.state.api.visibility}
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>Context</td>
                                            <td id="inUrl">{this.state.api.name}</td>
                                        </tr>
                                        <tr>
                                            <td>Date Last Updated</td>
                                            <td id="inUpdated" className="dateFull">{this.state.api.createdTime}</td>
                                        </tr>
                                        <tr>
                                            <td>Tier Availability</td>
                                            <td id="tierAvb">Unlimited</td>
                                        </tr>
                                        <tr>
                                            <td>Default API Version</td>
                                            <td id="defaultAPIVersion">
                                                false
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>Published Environments</td>
                                            <td>
                                                <b>N/A</b>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>Labels</td>
                                            <td id="labelValues"><b>N/A</b></td>
                                        </tr>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

export default Overview