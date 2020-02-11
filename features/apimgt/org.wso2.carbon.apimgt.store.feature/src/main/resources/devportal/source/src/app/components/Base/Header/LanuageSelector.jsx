/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React, { useEffect } from 'react';
import { makeStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import MenuItem from '@material-ui/core/MenuItem';
import FormControl from '@material-ui/core/FormControl';
import Select from '@material-ui/core/Select';
import Box from '@material-ui/core/Box';
import { useTheme } from '@material-ui/core/styles';
import { app } from 'Settings';

const useStyles = makeStyles(theme => ({
    formControl: {
        margin: theme.spacing(1),
        minWidth: theme.custom.languageSwitch.minWidth,
        '& > div:before': {
            borderBottom: 'none',
        }
    },
    selectEmpty: {
        marginTop: theme.spacing(2),
    },
    listTextSmall: {
        color: theme.palette.getContrastText(theme.custom.appBar.background),
    },
    langText: {
        textIndent: theme.spacing(1),
    }
}));

export default function LanuageSelector() {
    const classes = useStyles();
    const theme = useTheme();
    const [language, setLanuage] = React.useState(null);
    const { custom: { languageSwitch: { languages, showFlag, showText } } } = theme;
    useEffect(() => {
        let selectedLanguage = localStorage.getItem('language');
        if(!selectedLanguage && languages && languages.length > 0){
            selectedLanguage = languages[0].key;
        }
        setLanuage(selectedLanguage);
    }, [])

    const [labelWidth, setLabelWidth] = React.useState(0);

    const handleChange = event => {
        const selectedLanguage = event.target.value;
        setLanuage(selectedLanguage);
        localStorage.setItem('language', selectedLanguage);
        window.location.reload();
    };

    return (
        <FormControl className={classes.formControl}>
            {language && <Select
                labelId="demo-simple-select-label"
                id="demo-simple-select"
                value={language}
                onChange={handleChange}
                className={classes.listTextSmall}
            >
                {languages.map((lang) => <MenuItem value={lang.key}>
                    <Box display='flex'>
                        {showFlag && <img src={`${app.context}${lang.image}`} alt={lang.key} width={`${lang.imageWidth}px`} />}
                        {showText && <Typography variant="body1" className={classes.langText}>{lang.text}</Typography>}
                    </Box>
                </MenuItem>)}

            </Select>}
        </FormControl>
    );
}
