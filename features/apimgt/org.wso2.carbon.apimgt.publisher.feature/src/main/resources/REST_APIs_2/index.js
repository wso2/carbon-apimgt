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


const names = [
    {id:10,name:"Nuwanga",age:"12"},
    {id:20,name:"Mihiruwan",age:"32"},
    {id:30,name:"Heath",age:"23"}
];

app.get('/api/names',(req, res) => {
    //if (!course) return res.status(404).send('The course with the given ID was not found');
    res.send(names);
});

app.get('/api/names/:id',(req, res) => {
    const name = names.find(c => c.id === parseInt(req.params.id));
    if (!name) return res.status(404).send('The course with the given ID was not found');
    res.send(name);
});

app.put('/api/names/:id',(req, res) => {
    const name = names.find(c => c.id === parseInt(req.params.id));
    if (!name) {
        res.status(404).send('The name with the given ID was not found');
        return;
    };
    const index = names.indexOf(name);
    names.splice(index, 1);

    const newName = {
        id: parseInt(req.params.id),
        name: req.body.name,
        age: req.body.age
    };
    names.push(newName);
    res.send(newName);
});

app.post('/api/names',(req, res) => {
    const name = {
        id: names.length + 1,
        name: req.body.name,
        age: req.body.age
    };
    names.push(name);
    res.send(name); 
});

app.delete('/api/names/:id',(req, res) => {
    const name = names.find(c => c.id === parseInt(req.params.id));
    if (!name) return res.status(404).send('The course with the given ID was not found');
    
    const index = names.indexOf(name);
    names.splice(index, 1);

    res.send(name);
});

const port = process.env.PORT || 8080;
app.listen(port, () => console.log(`Listening on port ${port}...`));