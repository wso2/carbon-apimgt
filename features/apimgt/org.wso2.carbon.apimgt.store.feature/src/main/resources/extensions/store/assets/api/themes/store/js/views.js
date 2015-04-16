var Views;

$(function(){

    var viewTemplate={
        id:'',
        partial:'',
        container:'',
        subscriptions:'',
        beforeRender:function(data){},
        afterRender:function(){},
        subscriptions:[],
        resolveRender:function(){
            return true;
        },
        render:function(data){
            console.log('default render function');
        },
        disabled:false
    }

    function ViewManagement(){
        this.viewMap={}; //We track all of the views
    }

    ViewManagement.prototype.extend=function(){

        var templateName=(arguments.length>1)?arguments[0]:null;
        var view=(arguments.length>1)?arguments[1]:arguments[0];
        var viewProxy;
        var template;

        //Override the template if one is provided
        if(templateName){

           //Check if the template can be obtained from an existing view
           template=getView(templateName,this.viewMap);

            //Use the default view template if the template cannot be found in
            //an existing view or if the template
           if((templateName=='view')||(!template)){
                template=viewTemplate;
           }

           view=override(template,view);
        }

        viewProxy=new ViewProxy(view);

        registerForEventing(viewProxy);

        //Add the view to the map
        registerView(viewProxy,this.viewMap);
    };


    ViewManagement.prototype.translate=function(word){
        return word;
    };

    /*
    The method is used to clear all registered views
     */
    ViewManagement.prototype.clear=function(){
        clearViews(this.viewMap);
    };

    ViewManagement.prototype.mirror=function(fromObj,toObj){
        for(var prop in fromObj){
            toObj[prop]=fromObj[prop];
        }

        return toObj;
    };

    function ViewProxy(view){
        this.view=view;
        var that=this;
        this.view.render=function(partial,data,container,cb){
              console.info('Rendering view id: '+that.view.id);
              renderView(partial,data,container,cb);
        }
    }

    ViewProxy.prototype.invoke=function(data){
        console.info('invoking view id: '+this.view.id);
        this.view.beforeRender(data);


        if((!this.view.disabled)&&(this.view.resolveRender(data))){
            this.view.render(this.view.partial,data,this.view.container,this.view.afterRender);
        }
    };

    ViewProxy.prototype.getSubscriptions=function(){
        return this.view.subscriptions;
    };

    ViewProxy.prototype.getId=function(){
       return this.view.id;
    };



    /*
     The function renders a partial with the provided data into the given container
     */
    function renderView(partial,data,container,cb){
        var obj={};
        obj[partial]='/extensions/assets/api/themes/store/partials/'+partial+'.hbs';
        caramel.partials(obj,function(){
            var template=Handlebars.partials[partial](data);

            $(container).html(template);

            if(cb){
                cb();
            }
        });
    }

    function override(parent,child){
         //Plugs in the values which are missing in the child
         for(var prop in parent){

             if(!child.hasOwnProperty(prop)){
                 child[prop]=parent[prop];
             }
         }

         return child;
    }

    function registerForEventing(viewProxy){
        var subscription;
        var subscriptions=viewProxy.getSubscriptions();
        //Go through each subscription event
        for(var subscriptionIndex in subscriptions){
            subscription=subscriptions[subscriptionIndex];

            events.subscribe(subscription,viewProxy.getId(),viewProxy);
        }
    }

    var registerView=function(viewProxy,viewMap){
        console.info('Registering view: '+viewProxy.view.id);
        viewMap[viewProxy.view.id]=viewProxy;
    };

    /*
    The method removes all views registered in the map
     */
    var clearViews=function(viewMap){
        for(var view in viewMap){
            delete viewMap[view];
        }
    };

    /*
    The method is used to obtain a view from the viewMap by
    giving the view id
     */
    var getView=function(viewId,viewMap){

        if(viewMap.hasOwnProperty(viewId)){
            return viewMap[viewId].view;
        }

        return null;
    };

    var instance=new ViewManagement();

    Views=instance;

});