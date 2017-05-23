import React, {Component} from 'react';
import API from '../../data/api.js';

class Listing extends Component {
    constructor(props) {
        super(props);
    }

    componentDidMount() {
        let api = new API();
        const apis = api.getAll();
    }

    render() {
        return (
            <h2>API Listing DataTable</h2>
        );
    }
}

export default Listing;