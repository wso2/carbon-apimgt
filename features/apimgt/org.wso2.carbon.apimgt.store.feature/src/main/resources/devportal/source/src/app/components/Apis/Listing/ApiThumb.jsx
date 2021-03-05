import React from 'react';
import { useTheme } from '@material-ui/core/styles';
import ApiThumbClassic from 'AppComponents/Apis/Listing/APICards/ApiThumbClassic';
import APIThumbPlain from 'AppComponents/Apis/Listing/APICards/APIThumbPlain';

export default function ApiThumb(props) {
    const theme = useTheme();
    const { custom } = theme;
    if (!custom.thumbnailTemplates || !custom.thumbnailTemplates.active) {
        return (
            <ApiThumbClassic {...props} />
        );
    }
    return (
        <APIThumbPlain {...props} />
    )
}
