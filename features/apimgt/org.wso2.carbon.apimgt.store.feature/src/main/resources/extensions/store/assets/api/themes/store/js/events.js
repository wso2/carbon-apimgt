var events;

(function () {

    function EventBus() {
        this.eventMap = {};
    }

    EventBus.prototype.register=function(eventName){
         if(!this.eventMap.hasOwnProperty(eventName)){
             this.eventMap[eventName]={};
         }
    };

    EventBus.prototype.subscribe = function (eventName,subscriber,cb) {

        //Check if the eventMap has an event
        if (this.eventMap.hasOwnProperty(eventName)) {

            //Check if the subscriber exists
            if (!this.eventMap[eventName].hasOwnProperty(subscriber)) {
                this.eventMap[eventName][subscriber] = cb;
            }
        }
    };

    EventBus.prototype.unsubscribe = function (eventName,subscriber) {
        if (eventName == '*') {
            removeAllSubscriptions(subscriber, this.eventMap);
        }
        else {
            removeSubscription(subscriber, eventName);
        }

    };

    EventBus.prototype.publish = function (eventName, data) {
        var cb;
        var dataCopy;
        //Check if the event is tracked
        if (this.eventMap.hasOwnProperty(eventName)) {

            //Go through each subscriber
            for (var subscriber in this.eventMap[eventName]) {

                //Invoke all subscribers
                cb = this.eventMap[eventName][subscriber];

                //Perform a shallow copy so that each event handler recieves its own copy of the data
                //there is no sharing of data between handlers
                dataCopy=deepCopy(data);

                cb.invoke(dataCopy);
            }

        }
    };

    /*
     The function removes all subscriptions to events
     */
    EventBus.prototype.clearEvents = function () {
        for(var eventName in this.eventMap){

            for(var subscriber in this.eventMap[eventName]){
                delete this.eventMap[eventName][subscriber];
            }
        }
    };

    /*
    The function removes the subscription of a single subscriber
     */
    function removeSubscription(subscriber, eventName, eventMap) {
        if (eventMap.hasOwnProperty(eventMap)) {

            //Check if the subscriber exists
            if (eventMap[eventName].hasOwnProperty(eventName)) {
                delete eventMap[eventName][subscriber];
            }
        }
    }

    function removeAllSubscriptions(subscriber, eventMap) {

        for (var eventName in eventMap) {
            removeSubscription(subscriber, eventName, eventMap);
        }
    }

    var deepCopy=function(data){

        var copy={};

        for(var prop in data){
            copy[prop]=data[prop];
        }

        return copy;
    };

    events = new EventBus();

    console.info('Finished loading events');
})();