import React, {Component} from 'react'
import ReactDom from 'react-dom'

class ApiTable extends Component {

    render() {
        const api_rows = [];
        for (let api of this.props.apis.list) {
            console.log(api);
            api_rows.push(
                <tr key={api.id}>
                    <td>{api.name}</td>
                    <td>/{api.context}</td>
                    <td>{api.version}</td>
                </tr>
            );
        }
        return (
            <div>
                <table style={{width: '100%'}}>
                    <tbody>
                    <tr>
                        <th>Name</th>
                        <th>Context</th>
                        <th>Version</th>
                    </tr>
                    {api_rows}
                    </tbody>
                </table>
            </div>
        );
    }
}

export default ApiTable;