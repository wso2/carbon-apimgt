import  React from 'react'
import Paper from 'material-ui/Paper';
import Grid from 'material-ui/Grid';
import Typography from 'material-ui/Typography';
import PropTypes from 'prop-types';

import SwaggerUi, {presets} from 'swagger-ui';
import 'swagger-ui/dist/swagger-ui.css';

class ApiConsole extends React.Component {

    componentDidMount() {
        SwaggerUI({
          dom_id: "#ui",
          url: "http://petstore.swagger.io/v2/swagger.json"
        })
      }

    render() {
        return (
            <div id="ui" />
        );
    }
}

ApiConsole.propTypes = {
	optionalArray: PropTypes.array
}

export default ApiConsole;