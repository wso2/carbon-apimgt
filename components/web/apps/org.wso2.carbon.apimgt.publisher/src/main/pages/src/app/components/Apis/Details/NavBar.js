import React, {Component} from 'react'

export default class NavBar extends Component {

    render() {
        return (
            <div>
                <div className="tabs-holder" style={{background: 'floralwhite'}}>
                    <div className="button-bar">
                        <ul className="nav nav-pills tab-effect">
                            <li id="tab-1" role="presentation" className="active"><a href="#overview-tab"
                                                                                     aria-controls="overview-tab"
                                                                                     role="tab" data-toggle="tab"
                                                                                     aria-expanded="true">
                                <i className="fw fw-view"/>
                                &nbsp;Overview</a>
                            </li>
                            <li id="tab-2" role="presentation"><a href="#lc-tab" aria-controls="lc-tab" role="tab"
                                                                  data-toggle="tab"><i
                                className="fw fw-lifecycle"/>&nbsp;
                                Life-Cycle</a>
                            </li>
                            <li id="tab-3" role="presentation"><a href="#endpoints-tab" role="tab"
                                                                  aria-controls="endpoints-tab" data-toggle="tab"><i
                                className="fw fw-endpoint"/>&nbsp; Endpoints</a>
                            </li>
                            <li id="tab-4" role="presentation"><a href="#resources-tab" role="tab"
                                                                  aria-controls="resources-tab" data-toggle="tab"><i
                                className="fw fw-resource"/>&nbsp;
                                Resources</a></li>
                            <li id="tab-5" role="presentation"><a href="#documents-tab" role="tab"
                                                                  aria-controls="documents-tab" data-toggle="tab"><i
                                className="fw fw-document"/>&nbsp; Documents</a>
                            </li>
                            <li id="tab-6" role="presentation"><a href="#actrl-tab" role="tab" aria-controls="actrl-tab"
                                                                  data-toggle="tab"><i
                                className="fw fw-contract"/>&nbsp; Access Control</a>
                            </li>
                            <li id="tab-7" role="presentation"><a href="#mediation-tab" role="tab"
                                                                  aria-controls="mediation-tab" data-toggle="tab"><i
                                className="fw fw-sequence"/>&nbsp;
                                Mediation</a>
                            </li>
                            <li id="tab-8" role="presentation"><a href="#scripting-tab" role="tab"
                                                                  aria-controls="scripting-tab" data-toggle="tab"><i
                                className="fw fw-prototype"/>&nbsp; Scripting</a>
                            </li>
                            <li id="tab-9" role="presentation"><a href="#subscriptions-tab" role="tab"
                                                                  aria-controls="subscriptions-tab" data-toggle="tab"><i
                                className="fw fw-subscribe"/>&nbsp; Subscriptions</a>
                            </li>
                        </ul>
                    </div>
                </div>

            </div>
        );
    }
}