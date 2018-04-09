import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from 'material-ui/styles';
import List, { ListItem, ListItemText } from 'material-ui/List';
import Avatar from 'material-ui/Avatar';
import Typography from 'material-ui/Typography';
import { Link, FileDownload } from 'material-ui-icons';
import WorkIcon from 'material-ui-icons/Work';
import BeachAccessIcon from 'material-ui-icons/BeachAccess';
import ExpansionPanel, {
    ExpansionPanelSummary,
    ExpansionPanelDetails,
  } from 'material-ui/ExpansionPanel';
  import ExpandMoreIcon from 'material-ui-icons/ExpandMore';
import API from '../../../../data/api.js'
import Loading from '../../../Base/Loading/Loading'
import DocumentView from './DocumentView'


const styles = theme => ({
  root: {
    width: '100%',
    paddingTop: 10,
  },
  summary:{
      textDecoration: 'none',
      display: 'flex',
      paddingLeft: 0,
      cursor: 'pointer',
  },
  heading: {
    fontSize: theme.typography.pxToRem(15),
    fontWeight: theme.typography.fontWeightRegular,
  },
  listItem: {
      paddingLeft: 0,
  }
});

class Documentation extends React.Component {
    constructor(props){
        super(props);
        this.client = new API();
        this.state = {
	        api: null,
            documentsList: null,
        };
        this.api_id = this.props.match.params.api_uuid;
        this.initialDocSourceType = null;
        this.viewDocContentHandler=this.viewDocContentHandler.bind(this);
        
        
    }
    componentDidMount() {
        let promised_api = this.client.getDocumentsByAPIId(this.api_id);
        promised_api.then(
            response => {
                let types = [];
                if(response.obj.list.length > 0){
                    //Rearanging the response to group them by the sourceType property.
                    let allDocs = response.obj.list;
                    for( var i=0; i < allDocs.length; i++ ){
                        let selectedType = allDocs[i].type;
                        let hasType = false;
                        for( var j=0; j < types.length; j++ ){
                            if(selectedType === types[j].docType){
                                types[j].docs.push(allDocs[i]);
                                hasType = true;
                            } 
                        }
                        if(!hasType){
                            //Adding a new type entry
                            types.push({
                                docType: selectedType,
                                docs: [allDocs[i]],
                            })
                        }

                    }
                }

                this.setState({documentsList: types});
            }
        ).catch(
            error => {
                if (process.env.NODE_ENV !== "production") {
                    console.log(error);
                }
                let status = error.status;
                if (status === 404) {
                    this.setState({notFound: true});
                }
            }
        );
    }
    truncateSummary(summary) {
        let newSummery = summary;
        let maxCount = 200;
        if( summary.length > maxCount && summary.length > maxCount + 5 ) {
            newSummery = summary.substring(1,200) + " ... " ;
        }
        return newSummery;
    }
     /*
     On click listener for 'View' link on each document related row in the documents table.
     1- If the document type is 'URL' open it in new tab
     2- If the document type is 'INLINE' open the content with an inline editor
     3- If the document type is 'FILE' download the file
     */

    viewDocContentHandler(doc) {
        let promised_get_content = this.client.getFileForDocument(this.api_id, doc.documentId);
        promised_get_content.then((done) => {
            this.downloadFile(done);
        }).catch((error_response) => {
            throw error_response;
            let error_data = JSON.parse(error_response.data);
            let messageTxt = "Error[" + error_data.code + "]: " + error_data.description + " | " + error_data.message + ".";
            console.error(messageTxt);
        });
    }

    downloadFile(response) {
        let fileName = "";
        const contentDisposition = response.headers["content-disposition"];

        if (contentDisposition && contentDisposition.indexOf('attachment') !== -1) {
            const fileNameReg = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
            const matches = fileNameReg.exec(contentDisposition);
            if (matches != null && matches[1]) fileName = matches[1].replace(/['"]/g, '');
        }
        const contentType = response.headers["content-type"];
        const blob = new Blob([response.data], {
            type: contentType
        });
        if (typeof window.navigator.msSaveBlob !== 'undefined') {
            window.navigator.msSaveBlob(blob, fileName);
        } else {
            const URL = window.URL || window.webkitURL;
            const downloadUrl = URL.createObjectURL(blob);

            if (fileName) {
                const aTag = document.createElement("a");
                if (typeof aTag.download === 'undefined') {
                    window.location = downloadUrl;
                } else {
                    aTag.href = downloadUrl;
                    aTag.download = fileName;
                    document.body.appendChild(aTag);
                    aTag.click();
                }
            } else {
                window.location = downloadUrl;
            }

            setTimeout(function () {
                URL.revokeObjectURL(downloadUrl);
            }, 100);
        }
    }
    render() {
        const { classes } = this.props;
        if (!this.state.documentsList) {
            return <Loading/>
        }
        return (
            <div className={classes.root}>
                {(this.state.documentsList && this.state.documentsList.length > 0) && 
                    <div>
                       
                        {this.state.documentsList.map(item => 
                            <div key={item.docType}>
                                <ExpansionPanel defaultExpanded={true}>
                                    <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
                                    <Typography className={classes.heading}>{item.docType}</Typography>
                                    </ExpansionPanelSummary>
                                    <ExpansionPanelDetails>
                                        <List>
                                            {item.docs.map( doc =>
                                                    <ListItem key={doc.documentId} className={classes.listItem}>
                                                        {console.info(doc)}
                                                        {doc.sourceType === "INLINE" && 
                                                            <DocumentView doc={doc} truncateSummary={this.truncateSummary(doc.summary)} />
                                                        }
                                                        {doc.sourceType === "FILE" && 
                                                        
                                                            <a onClick={() => this.viewDocContentHandler(doc)} className={classes.summary}>
                                                                <Avatar>
                                                                    <FileDownload />
                                                                </Avatar>
                                                                <ListItemText primary={doc.name} secondary={this.truncateSummary(doc.summary)}/>
                                                            </a>
                                                        }
                                                        {doc.sourceType === "URL" && 
                                                            <a href={doc.sourceUrl} target="_blank"  className={classes.summary}>
                                                                <Avatar>
                                                                    <Link /> 
                                                                </Avatar>
                                                                <ListItemText primary={doc.name} secondary={this.truncateSummary(doc.summary)}/>
                                                            </a>
                                                        }
                                                    </ListItem>
                                                
                                            )}
                                        </List>
                                    </ExpansionPanelDetails>
                                </ExpansionPanel>
                            </div>
                        )}
                       
                    </div>
                }
                
            </div>
        )
    }
  
}

Documentation.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(Documentation);