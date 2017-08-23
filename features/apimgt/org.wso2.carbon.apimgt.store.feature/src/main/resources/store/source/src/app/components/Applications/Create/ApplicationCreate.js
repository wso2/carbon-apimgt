import React, {Component} from 'react'
import API from '../../../data/api.js'

class ApplicationCreate extends Component {

    constructor(props) {
        super(props);
        this.state = {
            throttlingTier: null,
            description: null,
            appName: null,
            callbackUrl: "http://my.server.com/callback"
        };
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    handleSubmit = (e) => {
        e.preventDefault();
        this.setState({
            appName: "wso2app",
            description: "test",
            throttlingTier: "10PerMin",
            callbackUrl: "http://my.server.com/callback"
        });
        let application_data = {
            name: this.state.appName,
            description: this.state.description,
            throttlingTier: this.state.throttlingTier,
            callbackUrl: this.state.callbackUrl
        };
        let new_application = new API();
        new_application.createApplication(application_data);
    };

    render(){
        return (
            <div>
                <form onSubmit={this.handleSubmit} >
                    Name: <input type="text" name="appName" />
                    <br/>
                    Per Token Quota:
                        <select name="throttlingTier">
                            <option value="Unlimited">Unlimited</option>
                            <option value="10PerMin">10PerMin</option>
                            <option value="50PerMin">50PerMin</option>
                        </select>
                    <br/>
                    Description: <input type="text" name="description" />
                    <br/>
                    <input type="submit" value="Submit"/>
                </form>
            </div>
        );
    }

}
export default ApplicationCreate;