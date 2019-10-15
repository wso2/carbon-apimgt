import React from 'react';
import PropTypes from 'prop-types';
import deburr from 'lodash/deburr';
import Downshift from 'downshift';
import { makeStyles } from '@material-ui/core/styles';
import TextField from '@material-ui/core/TextField';
import Paper from '@material-ui/core/Paper';
import MenuItem from '@material-ui/core/MenuItem';
import API from '../../../../../data/api';

const arns = API.getAmznResourceNames();

/**
 * The renderInput function.
 * @param {any} inputProps The props that are being passed to the function.
 * @returns {any} HTML view of the inputs.
 */
function renderInput(inputProps) {
    const {
        InputProps, classes, ref, ...other
    } = inputProps;
    return (
        <TextField
            InputProps={{
                inputRef: ref,
                classes: {
                    root: classes.inputRoot,
                    input: classes.inputInput,
                },
                ...InputProps,
            }}
            {...other}
        />
    );
}

renderInput.propTypes = {
    /**
   * Override or extend the styles applied to the component.
   */
    // eslint-disable-next-line react/forbid-prop-types
    classes: PropTypes.object.isRequired,
    // eslint-disable-next-line react/forbid-prop-types, react/require-default-props
    InputProps: PropTypes.object,
};

/**
 * The renderSuggestion function.
 * @param {any} suggestionProps The props that are being passed to the function.
 * @returns {any} HTML view of the suggestions.
 */
function renderSuggestion(suggestionProps) {
    const {
        suggestion, index, itemProps, highlightedIndex, selectedItem,
    } = suggestionProps;
    const isHighlighted = highlightedIndex === index;
    const isSelected = (selectedItem || '').indexOf(suggestion.label) > -1;

    return (
        <MenuItem
            {...itemProps}
            key={suggestion.label}
            selected={isHighlighted}
            component='div'
            style={{
                fontWeight: isSelected ? 500 : 400,
            }}
        >
            {suggestion.label}
        </MenuItem>
    );
}

renderSuggestion.propTypes = {
    highlightedIndex: PropTypes.oneOfType([PropTypes.oneOf([null]), PropTypes.number]).isRequired,
    index: PropTypes.number.isRequired,
    // eslint-disable-next-line react/forbid-prop-types
    itemProps: PropTypes.object.isRequired,
    selectedItem: PropTypes.string.isRequired,
    suggestion: PropTypes.shape({
        label: PropTypes.string.isRequired,
    }).isRequired,
};

/**
 * The getSuggestions function.
 * @param {any} value The props that are being passed to the function.
 * @returns {any} suggestion values.
 */
function getSuggestions(value, { showEmpty = false } = {}) {
    const inputValue = deburr(value.trim()).toLowerCase();
    const inputLength = inputValue.length;
    let count = 0;

    return inputLength === 0 && !showEmpty
        ? []
        : arns.filter((suggestion) => {
            const keep =
          count < 5 && suggestion.label.slice(0, inputLength).toLowerCase() === inputValue;

            if (keep) {
                count += 1;
            }

            return keep;
        });
}

const useStyles = makeStyles(theme => ({
    root: {
        flexGrow: 1,
    },
    container: {
        flexGrow: 1,
        position: 'relative',
    },
    paper: {
        position: 'absolute',
        zIndex: 1,
        marginTop: theme.spacing(1),
        left: 0,
        right: 0,
    },
    chip: {
        margin: theme.spacing(0.5, 0.25),
    },
    inputRoot: {
        flexWrap: 'wrap',
    },
    inputInput: {
        width: 'auto',
        flexGrow: 1,
    },
}));

/**
 * The autocomplete component. This component lists the ARNs of a specific user role.
 * @returns {any} HTML view of the autocomplete component.
 * @param {any} props The input parameters.
 */
export default function IntegrationDownshift(props) {
    const classes = useStyles();
    const {
        arn,
        setArn,
        isEmptyArn,
        setIsEmptyArn,
    } = props;
    return (
        <div className={classes.root}>
            <Downshift
                id='downshift-options'
                onSelect={(changes) => {
                    if (changes !== null) {
                        setIsEmptyArn(false);
                        setArn(changes);
                    }
                }}
            >
                {({
                    clearSelection,
                    getInputProps,
                    getItemProps,
                    getLabelProps,
                    getMenuProps,
                    highlightedIndex,
                    inputValue,
                    isOpen,
                    openMenu,
                    selectedItem,
                }) => {
                    const {
                        onBlur, onChange, onFocus, ...inputProps
                    } = getInputProps({
                        onChange: (event) => {
                            setArn(event.target.value);
                            if (event.target.value === '') {
                                setIsEmptyArn(true);
                                clearSelection();
                            } else {
                                setIsEmptyArn(false);
                            }
                        },
                        onBlur: () => {
                            clearSelection();
                        },
                        value: arn,
                        required: true,
                        onFocus: openMenu,
                        placeholder: 'Select or type an ARN *',
                    });
                    return (
                        <div className={classes.container}>
                            {renderInput({
                                error: isEmptyArn,
                                variant: 'outlined',
                                required: true,
                                helperText: isEmptyArn ? 'ARN should not be empty' : '',
                                fullWidth: true,
                                classes,
                                InputLabelProps: getLabelProps({ shrink: true }),
                                InputProps: { onBlur, onChange, onFocus },
                                inputProps,
                            })}

                            <div {...getMenuProps()}>
                                {isOpen ? (
                                    <Paper className={classes.paper} square>
                                        {getSuggestions(inputValue, { showEmpty: true }).map((suggestion, index) =>
                                            renderSuggestion({
                                                suggestion,
                                                index,
                                                itemProps: getItemProps({ item: suggestion.label }),
                                                highlightedIndex,
                                                selectedItem,
                                            }))}
                                    </Paper>
                                ) : null}
                            </div>
                        </div>
                    );
                }}
            </Downshift>
        </div>
    );
}

IntegrationDownshift.propTypes = {
    arn: PropTypes.shape('').isRequired,
    setArn: PropTypes.func.isRequired,
    isEmptyArn: PropTypes.shape('').isRequired,
    setIsEmptyArn: PropTypes.func.isRequired,
};
