import React from 'react';
import PropTypes from 'prop-types';
import Api from 'AppData/api';
import PageContainer from 'AppComponents/Base/container/';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import PageNavigation from '../../APIsNavigation';
import { withStyles } from '@material-ui/core/styles';
import TextField from '@material-ui/core/TextField';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import { FormattedMessage } from 'react-intl';
import { Progress } from 'AppComponents/Shared/';


/**
 * API Details Document page component
 * @class Details
 * @extends {Component}
 */

const styles = theme => ({
  root: {
    width: '100%',
    maxWidth: 360,
    backgroundColor: theme.palette.background.paper,
  },
  textField: {
    width: '50%',
  },
  caption: {
    marginTop: theme.spacing.unit * 2,
  },
});

/**
 *
 *
 * @class Details
 * @extends {Component}
 */
class Details extends React.Component {
  /**
   *Creates an instance of Details.
   * @param {*} props properies passed by the parent element
   * @memberof Details
   */
  constructor(props) {
    super(props);
    this.state = {
      doc: null,
      isEditable: false,
    };
  }

  /**
   *
   *
   * @memberof Details
   */
  componentDidMount() {
    const { apiUUID, documentId } = this.props.match.params;
    const api = new Api();
    const promisedDocument = api.getDocument(apiUUID, documentId);
    promisedDocument.then((response) => {
      if (response.obj) {
        this.setState({ doc: response.obj });
      }
    }).catch((error) => {
      const { status } = error;
      if (status === 404) {
        this.setState({ notFound: true });
      } else if (status === 401) {
        const params = qs.stringify({ reference: this.props.location.pathname });
        this.props.history.push({ pathname: '/login', search: params });
      }
    });
  }
  handleTextChange = prop => event => {
    this.state.doc[prop] = event.target.value;
    this.setState({ doc: this.state.doc });
  };

  /**
   *
   *  Render method of the component
   * @returns {React.Component} endpoint detail html component
   * @memberof Details
   */
  render() {
    const { classes } = this.props;
    const { notFound, doc, isEditable } = this.state;
    if (notFound) {
      return (
        <PageContainer pageNav={<PageNavigation />}>
          <ResourceNotFound />
        </PageContainer>
      );
    }

    if (!doc) {
      return <Progress />;
    }

    return (
      <Grid container spacing={0} direction='column' justify='flex-start' alignItems='stretch'>
        <Grid item xs={12}>
          <Grid container>
            <Grid item>
              <Typography variant='display1' align='left' className={classes.mainTitle}>
                {doc.name}
              </Typography>
            </Grid>
          </Grid>
        </Grid>
        <Grid item xs={12}>
          <Grid container direction='column'>
            <Grid item >
              <TextField
                id='doc-type'
                label={<FormattedMessage id='type' defaultMessage='Type' />}
                value={doc.type}
                placeholder='No Value!'
                margin='normal'
                InputProps={{
                  readOnly: !isEditable,
                }}
                className={classes.textField}
                onChange={this.handleTextChange('type')}
              />
            </Grid>
            <Grid item >
              <TextField
                id='doc-summary'
                label={<FormattedMessage id='summary' defaultMessage='Summary' />}
                value={doc.summary}
                placeholder='No Value!'
                margin='normal'
                className={classes.textField}
                multiline
                rowsMax="4"
                InputProps={{
                  readOnly: !isEditable,
                }}
                variant="outlined"
                onChange={this.handleTextChange('summary')}
              />
            </Grid>
            <Grid item>
              {doc.sourceType === 'URL' &&
                <React.Fragment>
                  {isEditable ?
                    <TextField
                      id='doc-source'
                      label={<FormattedMessage id='document' defaultMessage='Document' />}
                      value={doc.sourceUrl}
                      placeholder='No Value!'
                      margin='normal'
                      className={classes.textField}
                      variant="outlined"
                      onChange={this.handleTextChange('sourceUrl')}
                    />
                    :
                    <React.Fragment>
                      <Typography variant="caption" className={classes.caption}>
                        <FormattedMessage
                          id='document'
                          defaultMessage='Document'
                        /></Typography>
                      <a href={doc.sourceUrl} target="_blank">{doc.sourceUrl}</a>
                    </React.Fragment>
                  }
                </React.Fragment>
              }
              {(doc.sourceType === 'FILE') &&
                <React.Fragment>
                  {isEditable ?
                    <TextField
                      id='doc-source'
                      label={<FormattedMessage id='document' defaultMessage='Document' />}
                      value={doc.sourceUrl}
                      placeholder='No Value!'
                      margin='normal'
                      className={classes.textField}
                      variant="outlined"
                      onChange={this.handleTextChange('sourceUrl')}
                    />
                    :
                    <React.Fragment>
                      <Typography variant="caption" className={classes.caption}>
                        <FormattedMessage
                          id='document'
                          defaultMessage='Document'
                        /></Typography>
                      <a href={doc.sourceUrl} target="_blank">{doc.sourceUrl}</a>
                    </React.Fragment>
                  }
                </React.Fragment>
              }
              {(doc.sourceType === 'INLINE') &&
                <React.Fragment>
                  {isEditable ?
                    <TextField
                      id='doc-source'
                      label={<FormattedMessage id='document' defaultMessage='Document' />}
                      value={doc.sourceUrl}
                      placeholder='No Value!'
                      margin='normal'
                      className={classes.textField}
                      variant="outlined"
                      onChange={this.handleTextChange('sourceUrl')}
                    />
                    :
                    <React.Fragment>
                      <Typography variant="caption" className={classes.caption}>
                        <FormattedMessage
                          id='document'
                          defaultMessage='Document'
                        /></Typography>
                      <a href={doc.sourceUrl} target="_blank">{doc.sourceUrl}</a>
                    </React.Fragment>
                  }
                </React.Fragment>
              }
            </Grid>
          </Grid>
        </Grid>
      </Grid>
    );

  }
}

Details.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(Details);
