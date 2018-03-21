import React from 'react';
import PropTypes from 'prop-types';
import { FormControl, FormControlLabel, FormLabel } from 'material-ui/Form';
import Radio, { RadioGroup } from 'material-ui/Radio';

const BindingInfo = (props) => {
    const { apiFields, wsdlBean, classes } = props;
    if (wsdlBean.info && (wsdlBean.info.bindingInfo.hasHttpBinding || wsdlBean.info.bindingInfo.hasSoapBinding)) {
        return (
            <FormControl component='fieldset'>
                <FormLabel component='legend'>Implementation Type</FormLabel>
                <RadioGroup
                    aria-label='Implementation-Type'
                    name='implementationType'
                    value={apiFields.implementationType}
                    onChange={this.updateApiInputs}
                >
                    <FormControlLabel
                        disabled={!wsdlBean.info.bindingInfo.hasSoapBinding}
                        value='soap'
                        control={<Radio />}
                        label='Pass-through SOAP API'
                        className={classes.radioGroup}
                    />
                    <FormControlLabel
                        disabled={!wsdlBean.info.bindingInfo.hasHttpBinding}
                        value='httpBinding'
                        control={<Radio />}
                        label='With HTTP binding operations'
                        className={classes.radioGroup}
                    />
                </RadioGroup>
            </FormControl>
        );
    }
    return null;
};

BindingInfo.propTypes = {
    apiFields: PropTypes.shape({
        implementationType: PropTypes.string,
    }).isRequired,
    wsdlBean: PropTypes.shape({
        info: PropTypes.object,
    }).isRequired,
    classes: PropTypes.shape({}).isRequired,
};

export default BindingInfo;
