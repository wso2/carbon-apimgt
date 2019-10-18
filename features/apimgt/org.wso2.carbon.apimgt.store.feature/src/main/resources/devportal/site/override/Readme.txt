# Override public folder
If you want to override a certain store webapp static files, this is the correct place to do it.
You do not have to copy the entire directory, only copy the desired file/files.

#### Example

Following will override the main.css file.
```sh
override
    ├── Readme.txt
    └── css
        └── main.css

```

### Configurations
Add following configurations to jaggery.conf file in order to apply the changes.

```json
      "filters":[
        {
            "name":"URLMappingFilter",
            "class":"org.wso2.carbon.apimgt.impl.filter.URLMappingFilter"
        }
      ]


     "filterMappings":[
        {
            "name":"URLMappingFilter",
            "url":"/site/public/*"
        }
     ]
```

### Runtime
You do not re-build or restart the server. This changes will apply runtime after configs are done.