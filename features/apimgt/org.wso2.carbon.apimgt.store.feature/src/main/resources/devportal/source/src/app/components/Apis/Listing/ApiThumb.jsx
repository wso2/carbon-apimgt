import React from 'react';
import { useTheme } from '@material-ui/core/styles';
import ApiThumbClassic from 'AppComponents/Apis/Listing/APICards/ApiThumbClassic';
import APIThumbPlain from 'AppComponents/Apis/Listing/APICards/APIThumbPlain';

/**
 * Render no api section
 * @param {JSON} props properties passed from parent
 * @returns {void}
 */
export default function ApiThumb(props) {
    const theme = useTheme();
    const { custom } = theme;
    if (!custom.thumbnailTemplates || !custom.thumbnailTemplates.active) {
        return (
            <ApiThumbClassic {...props} />
        );
    }
    const { thumbnailTemplates: { variant, active } } = custom;
    if (active && variant === 'plain') {
        return (
            <APIThumbPlain {...props} />
        );
    }
    if (active && variant === 'text') {
        return (
            <ApiThumbClassic {...props} />
        );
    }
}
