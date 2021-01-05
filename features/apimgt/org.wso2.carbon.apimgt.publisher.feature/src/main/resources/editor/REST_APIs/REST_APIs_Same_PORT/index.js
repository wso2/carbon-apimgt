const Joi = require('joi');
var fs = require('fs');
var cors = require("cors");
const express = require('express');
const app = express();
app.use(express.json());


app.use(cors());
const path = __dirname + '/dist/';
app.use(express.static(path));
const path1 = __dirname + '/public_dir/';
app.use(express.static(path1))

app.get('/', function(req,res){
    res.sendFile(path + "index.html")
});

var corsOptions = {
    origin: 'http://localhost:8080',
    credentials: true,
    optionsSuccessStatus: 200 // some legacy browsers (IE11, various SmartTVs) choke on 204
   }
    
app.use(cors(corsOptions));

app.get('/api/am/publisher/v1/settings',(req, res) => {
    
    try {
        let fileContents = fs.readFileSync('settings', 'utf8');
        res.send(fileContents);
        //resolve(fileContents);
      } catch (e) {
        console.log(e);
      }
   
});

const port = process.env.PORT || 8080;
app.listen(port, () => console.log(`Listening on port ${port}...`));