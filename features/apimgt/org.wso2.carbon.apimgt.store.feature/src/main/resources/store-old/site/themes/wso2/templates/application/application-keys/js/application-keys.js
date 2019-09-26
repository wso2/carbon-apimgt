$(document).ready(function() {
   var clipboard = new ClipboardJS('.copy-button',{
       text: function(trigger) {
           $(trigger).parent().parent().notify("Copied",{ position:"top right" ,className: 'success'});
           return trigger.getAttribute('data-clipboard-text');
       }}
   );


    clipboard.on('success', function(e) {
        e.clearSelection();
    });

    clipboard.on('error', function(e) {
        console.error('Action:', e.action);
        console.error('Trigger:', e.trigger);
    });


});