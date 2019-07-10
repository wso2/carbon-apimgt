/**
 * Copyright (c)  WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { TextField, Button, Grid, withStyles } from '@material-ui/core';
import { FormattedMessage, injectIntl } from 'react-intl';

const styles = theme => ({
    fileinput: {
        display: 'none',
    },
});
/**
 * The base component for advanced endpoint configurations.
 * @param {any} props The input props.
 * @returns {any} The HTML representation of the Certificates.
 */
function Certificates(props) {
    const { classes } = props;
    const [certificate, setCertificate] = useState({ name: '', content: '' });
    const [alias, setAlias] = useState('');


    const handleAliasChange = (event) => {
        setAlias(event.target.value);
    };

    const handleCertUpload = (event) => {
        const certificateFile = event.target.files[0];
        let encodedContent = '';
        if (certificateFile) {
            const reader = new FileReader();
            reader.onload = (e) => {
                encodedContent = btoa(e.target.result);
                setCertificate({ name: certificateFile.name, content: encodedContent });
            };
            reader.readAsBinaryString(certificateFile);
        }
    };
    return (
        <Grid>
            <div>
                <TextField
                    id='certificateAlias'
                    label={<FormattedMessage id='Alias' defaultMessage='Alias' />}
                    value={alias}
                    placeholder='My Alias'
                    onChange={handleAliasChange}
                    margin='normal'
                    fullWidth
                />
            </div>
            <div>
                <TextField
                    id='certificateName'
                    label=''
                    value={certificate.name}
                    placeholder='My Alias'
                    onChange={handleAliasChange}
                    margin='normal'
                    fullWidth
                    InputProps={
                        { readOnly: true }
                    }
                />
                <input
                    accept='
                        application/pkcs8,
                        application/pkcs10,
                        application/pkix-crl,
                        application/pkcs7-mime,
                        application/x-x509-ca-cert,
                        application/x-x509-user-cert,
                        application/x-pkcs7-crl,
                        application/x-pkcs12,
                        application/x-pkcs7-certificates,
                        application/x-pkcs7-certreqresp,
                        .p8, .p10, .csr, .cer, .crl, .p7c, .crt, .der, .p12, .pfx, .p7b, .spc, .p7r'
                    className={classes.fileinput}
                    id='outlined-button-file'
                    type='file'
                    onChange={handleCertUpload}
                />
                <label id='input-label' htmlFor='outlined-button-file'>
                    <Button variant='outlined' component='span' className={classes.button}>
                        Upload
                    </Button>
                </label>
            </div>
        </Grid>
    );
}

Certificates.propTypes = {
    classes: PropTypes.shape({
        fileinput: PropTypes.shape({}),
        button: PropTypes.shape({}),
    }).isRequired,

};
export default injectIntl(withStyles(styles)(Certificates));
