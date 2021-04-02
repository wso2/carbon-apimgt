import React from 'react';
import { makeStyles } from '@material-ui/core/styles';
import Modal from '@material-ui/core/Modal';
import CodeIcon from '@material-ui/icons/Code';
import { FormattedMessage, injectIntl } from 'react-intl';
import CopyToClipboard from 'react-copy-to-clipboard';
import Tooltip from '@material-ui/core/Tooltip';
import Icon from '@material-ui/core/Icon';

/**
 * Position the modal
 * @returns {JSON} css atrributes JSON.
 */
function getModalStyle() {
    const top = 50;
    const left = 50;

    return {
        top: `${top}%`,
        left: `${left}%`,
        transform: `translate(-${top}%, -${left}%)`,
    };
}

const useStyles = makeStyles((theme) => ({
    paper: {
        position: 'absolute',
        width: 400,
        backgroundColor: theme.palette.background.paper,
        border: '2px solid #000',
        boxShadow: theme.shadows[5],
        padding: theme.spacing(2, 4, 3),
    },
    codeIcon: {
        cursor: 'pointer',
        color: theme.palette.getContrastText(theme.custom.infoBar.background),
    },
    code: {
        background: '#efefef',
        color: 'cc0000',
        border: 'solid 1px #ccc',
        padding: theme.spacing(1),
    },
    iconStyle: {
        position: 'absolute',
        top: 60,
        right: 30,
    },
}));

/**
 * Adds two numbers together.
 * @param {JSON} props props passed from parent
 * @returns {JSX} code in a modal
 */
function EmbedCode(props) {
    const { intl } = props;
    const classes = useStyles();
    // getModalStyle is not a pure function, we roll the style only on the first render
    const [modalStyle] = React.useState(getModalStyle);
    const [open, setOpen] = React.useState(false);
    const [codeCopied, setCodeCopied] = React.useState(false);
    const url = new URL(window.location);
    url.searchParams.set('widget', true);

    const onCopy = () => {
        setCodeCopied(true);
        setTimeout(() => setCodeCopied(false), 2000);
    };

    const handleOpen = () => {
        setOpen(true);
    };

    const handleClose = () => {
        setOpen(false);
    };
    const embedCode = '<iframe width="450" height="120" src="'
        + url + '" frameBorder="0" allowFullScreen title="Embed API" />';

    return (
        <div>
            <CodeIcon className={classes.codeIcon} onClick={handleOpen} />
            <Modal
                open={open}
                onClose={handleClose}
                aria-labelledby='simple-modal-title'
                aria-describedby='simple-modal-description'
            >
                <div style={modalStyle} className={classes.paper}>
                    <h2 id='simple-modal-title'>
                        <FormattedMessage
                            id='Apis.Details.Social.EmbedCode'
                            defaultMessage='Embed'
                        />
                    </h2>
                    <div className={classes.code}>
                        <code>{embedCode}</code>
                    </div>
                    <Tooltip
                        title={
                            codeCopied
                                ? intl.formatMessage({
                                    defaultMessage: 'Copied',
                                    id: 'Apis.Details.Environments.copied',
                                })
                                : intl.formatMessage({
                                    defaultMessage: 'Copy to clipboard',
                                    id: 'Apis.Details.Environments.copy.to.clipboard',
                                })
                        }
                        placement='right'
                        className={classes.iconStyle}
                    >
                        <CopyToClipboard
                            text={embedCode}
                            onCopy={onCopy}
                        >
                            <Icon color='secondary'>file_copy</Icon>
                        </CopyToClipboard>
                    </Tooltip>
                </div>
            </Modal>
        </div>
    );
}

export default injectIntl(EmbedCode);
