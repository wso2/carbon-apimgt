import React from 'react';
import PropTypes from 'prop-types';
import FormControl from '@material-ui/core/FormControl';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import FormLabel from '@material-ui/core/FormLabel';
import Radio from '@material-ui/core/Radio';
import RadioGroup from '@material-ui/core/RadioGroup';
import { withStyles } from '@material-ui/core/styles';

const styles = (theme) => ({
    FormControl: {
        padding: 0,
        width: '100%',
        marginTop: theme.spacing(3),
    },
    radioWrapper: {
        display: 'flex',
        flexDirection: 'row',
    },
});

const BindingInfo = (props) => {
    const {
        api, wsdlBean, classes, updateApiInputs,
    } = props;
    if (wsdlBean.info && (wsdlBean.info.bindingInfo.hasHttpBinding || wsdlBean.info.bindingInfo.hasSoapBinding)) {
        return (
            <FormControl className={classes.FormControl}>
                <FormLabel component='legend'>Implementation Type</FormLabel>
                <RadioGroup
                    aria-label='Implementation-Type'
                    name='implementationType'
                    value={api.implementationType}
                    onChange={updateApiInputs}
                    className={classes.radioWrapper}
                >
                    <FormControlLabel
                        disabled={!wsdlBean.info.bindingInfo.hasSoapBinding}
                        value='soap'
                        control={<Radio color='primary' />}
                        label='Pass-through SOAP API'
                        className={classes.radioGroup}
                    />
                    <FormControlLabel
                        disabled={!wsdlBean.info.bindingInfo.hasHttpBinding}
                        value='httpBinding'
                        control={<Radio color='primary' />}
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
    updateApiInputs: PropTypes.func.isRequired,
    api: PropTypes.shape({
        implementationType: PropTypes.string,
    }).isRequired,
    wsdlBean: PropTypes.shape({
        info: PropTypes.object,
    }).isRequired,
    classes: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(BindingInfo);
