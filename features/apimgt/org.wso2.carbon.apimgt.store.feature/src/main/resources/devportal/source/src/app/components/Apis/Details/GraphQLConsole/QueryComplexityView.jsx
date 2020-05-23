import React, { useState, useEffect, useContext } from 'react';
import { makeStyles } from '@material-ui/core/styles';
import { FormattedMessage } from 'react-intl';
import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import Typography from '@material-ui/core/Typography';
import CloseIcon from '@material-ui/icons/Close';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import Divider from '@material-ui/core/Divider';
import Button from '@material-ui/core/Button';
import { ApiContext } from '../ApiContext';
import Api from '../../../../data/api';
import Progress from '../../../Shared/Progress';

const useStyles = makeStyles((theme) => ({
    root: {
        width: '100%',
    },
    title: {
        fontSize: theme.typography.pxToRem(15),
        fontWeight: 'bold',
        flexBasis: '50%',
        flexShrink: 0,
        marginTop: theme.spacing(2),
        marginBottom: theme.spacing(2),
        marginLeft: theme.spacing(4),
        marginRight: theme.spacing(1),
    },
    heading: {
        fontSize: theme.typography.pxToRem(15),
        fontWeight: theme.typography.fontWeightRegular,
        flexBasis: '50%',
        flexShrink: 0,
        marginTop: theme.spacing(1),
        marginBottom: theme.spacing(1),
    },
    column: {
        flexBasis: '33.33%',
        marginLeft: theme.spacing(1),
    },
}));

export default function NestedList(props) {
    const classes = useStyles();
    const { api } = useContext(ApiContext);
    const { open, setOpen } = props;
    const [typelist, setTypeList] = useState([]);
    const [state, setState] = useState([]);

    useEffect(() => {
        const apiId = api.id;
        const apiClient = new Api();
        const promisedComplexity = apiClient.getGraphqlPoliciesComplexity(apiId);
        promisedComplexity
            .then((res) => {
                setState(res.list);
                const type = [...new Set(res.list.map((respond) => respond.type))];
                setTypeList(type);
            });
    }, []);

    const handleClose = () => {
        setOpen(!open);
    };

    if (state.length === 0) {
        return <Progress />;
    }

    return (
        <>
            <div>
                <div className={classes.title} style={{ display: 'flex', position: 'relative' }}>
                    <div>
                        <FormattedMessage
                            id='Apis.Details.GraphQLConsole.QueryComplexityView.title'
                            defaultMessage='Custom Complexity Values'
                        />
                    </div>
                    <Button size='small' onClick={handleClose}>
                        <CloseIcon />
                    </Button>
                </div>
                <Divider />
                <div
                    className={classes.root}
                    style={{ maxHeight: '740px', overflow: 'scroll' }}
                >
                    <div>
                        {typelist.map((res) => (
                            <ExpansionPanel>
                                <ExpansionPanelSummary
                                    expandIcon={<ExpandMoreIcon />}
                                    aria-controls='panel1a-content'
                                    id='panel1a-header'
                                >
                                    <Typography className={classes.heading}>
                                        {res}
                                    </Typography>
                                </ExpansionPanelSummary>
                                <Divider />
                                {state.map((respond) => ((respond.type === res) && (
                                    <ExpansionPanelDetails>
                                        <div className={classes.column}>
                                            {respond.field}
                                            {':'}
                                        </div>
                                        <div className={classes.column}>
                                            {respond.complexityValue}
                                        </div>
                                    </ExpansionPanelDetails>
                                )))}
                            </ExpansionPanel>
                        ))}
                    </div>
                </div>
            </div>
        </>
    );
}
