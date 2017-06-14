import React, {Component} from 'react'
import API from '../../data/api.js'
import ApiTable from './ApisTable.jsx'

class Listing extends Component {
    constructor(props) {
        super(props);
        this.state = {
            apis: null
        }
    }

    componentDidMount() {
        let api = new API();
        let promised_apis = api.getAll();
        promised_apis.then((response) => {
            this.setState({apis: response.obj})
        });
    }

    render() {
        return (
            <div>
                <h2>API Listing DataTable</h2>
                {this.state.apis ? <ApiTable apis={this.state.apis}/> : "Loading APIs..."}
            </div>
        );
    }
}

export default Listing;