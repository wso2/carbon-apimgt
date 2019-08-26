/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import React from 'react';
import PropTypes from 'prop-types';
import Radio from '@material-ui/core/Radio';
import Grid from '@material-ui/core/Grid';
import TextField from '@material-ui/core/TextField';
import RadioGroup from '@material-ui/core/RadioGroup';
import FormHelperText from '@material-ui/core/FormHelperText';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import FormControl from '@material-ui/core/FormControl';
import FormLabel from '@material-ui/core/FormLabel';
import DropZoneLocal from './components/DropZoneLocal';

/**
 * Source https://stackoverflow.com/questions/5717093/check-if-a-javascript-string-is-a-url
 * TODO: Needs to replace this kind of ad-hoc validation methods with proper library
 *
 * @param {*} str
 * @returns {Boolean} Whether the given string is a valid URL or not
 */
function isURL(str) {
    const pattern = new RegExp(
        '^(https?:\\/\\/)?' + // protocol
        '((([a-z\\d]([a-z\\d-]*[a-z\\d])*)\\.?)+[a-z]{2,}|' + // domain name
        '((\\d{1,3}\\.){3}\\d{1,3}))' + // OR ip (v4) address
        '(\\:\\d+)?(\\/[-a-z\\d%_.~+]*)*' + // port and path
        '(\\?[;&a-z\\d%_.~+=-]*)?' + // query string
            '(\\#[-a-z\\d_]*)?$',
        'i',
    ); // fragment locator
    return pattern.test(str);
}
/**
 * Sub component of API Create using WSDL UI, This is handling the taking input of WSDL file or URL from the user
 * In the create API using WSDL wizard first step out of 2 steps
 * @export
 * @param {*} props
 * @returns {React.Component} @inheritdoc
 */
export default function ProvideWSDL(props) {
    const { apiInputs, inputsDispatcher } = props;
    const isFileInput = apiInputs.inputType === 'file';
    // const [validity, setValidity] = useState({ url: true, file: true });
    /**
     *
     *
     * @param {*} files
     */
    function onDrop(files) {
        // Why `files[0]` below is , We only handle one wsdl file at a time, So if use provide multiple, We would only
        // accept the first file. This information is shown in the dropdown helper text
        inputsDispatcher({ action: 'inputValue', value: [files[0]] });
    }
    return (
        <React.Fragment>
            <Grid container spacing={5}>
                <Grid item md={12}>
                    <FormControl component='fieldset'>
                        <FormLabel component='legend'>Input type</FormLabel>
                        <RadioGroup
                            aria-label='Input type'
                            value={apiInputs.inputType}
                            onChange={event => inputsDispatcher({ action: 'inputType', value: event.target.value })}
                        >
                            <FormControlLabel value='url' control={<Radio />} label='WSDL URL' />
                            <FormControlLabel value='file' control={<Radio />} label='WSDL Archive/File' />
                        </RadioGroup>
                    </FormControl>
                </Grid>
                <Grid item md={7}>
                    {isFileInput ? (
                        // TODO: Pass message saying accepting only one file ~tmkb
                        <DropZoneLocal onDrop={onDrop} files={apiInputs.inputValue} />
                    ) : (
                        <TextField
                            id='outlined-full-width'
                            label='WSDL URL'
                            inputProps={{ onBlur: event => console.log(event.target.value) }}
                            placeholder='Enter WSDL URL'
                            error={isURL(apiInputs.inputValue)}
                            helperText='Give the URL of WSDL endpoint'
                            fullWidth
                            margin='normal'
                            variant='outlined'
                            onChange={({ target: { value } }) => inputsDispatcher({ action: 'inputValue', value })}
                            value={apiInputs.inputValue}
                            InputLabelProps={{
                                shrink: true,
                            }}
                        />
                    )}
                </Grid>
                <Grid item md={12}>
                    <FormControl component='fieldset'>
                        <FormLabel component='legend'>Implementation type</FormLabel>
                        <RadioGroup
                            aria-label='Implementation type'
                            value={isFileInput ? 'PASS' : apiInputs.type}
                            onChange={event => inputsDispatcher({ action: 'type', value: event.target.value })}
                        >
                            <FormControlLabel value='PASS' control={<Radio />} label='Pass Through' />
                            <FormControlLabel
                                disabled={isFileInput}
                                value='SOAPtoREST'
                                control={<Radio />}
                                label='Generate REST APIs'
                            />
                        </RadioGroup>
                        <FormHelperText>
                            <sup>*</sup>
                            <b>Generate REST APIs</b> option is only available for WSDL URL input type
                        </FormHelperText>
                    </FormControl>
                </Grid>
            </Grid>
        </React.Fragment>
    );
}

ProvideWSDL.propTypes = {
    apiInputs: PropTypes.shape({
        type: PropTypes.string,
        inputType: PropTypes.string,
    }).isRequired,
    inputsDispatcher: PropTypes.func.isRequired,
};
