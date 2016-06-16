/*
 * 	Character Count Plugin - jQuery plugin
 * 	Dynamic character count for text areas and input fields
 *	written by Alen Grakalic	
 *	http://cssglobe.com/post/7161/jquery-plugin-simplest-twitterlike-dynamic-character-count-for-textareas
 *
 *	Copyright (c) 2009 Alen Grakalic (http://cssglobe.com)
 *	Dual licensed under the MIT (MIT-LICENSE.txt)
 *	and GPL (GPL-LICENSE.txt) licenses.
 *
 *	Built for jQuery library
 *	http://jquery.com
 *
 */
 
(function($) {

	$.fn.charCount = function(options){
	  
		// default configuration properties
		var defaults = {	
			allowed: 140,		
			warning: 25,
			css: 'counter-container',
			counterElement: 'span',
			cssWarning: 'warning',
			cssExceeded: 'exceeded',
			counterText: '',
			placement:'after'
		}; 
			
		var options = $.extend(defaults, options); 
		
		function calculate(obj){
			var count = $(obj).val().length;
			var available = options.allowed - count;

            $(obj).parent().find(options.counterElement).html(options.counterText + available);
			if(available <= options.warning && available >= 0){
				$(obj).parent().find(options.counterElement).addClass(options.cssWarning);
			} else {
				$(obj).parent().find(options.counterElement).removeClass(options.cssWarning);
			}
			if(available < 0){
                $(obj).val($(obj).val().substring(0, options.allowed));
                $(obj).parent().find(options.counterElement).html(options.counterText + 0);
                $(obj).scrollTop(
                        $(obj)[0].scrollHeight - $(obj).height()
                        );
                $(obj).parent().find(options.counterElement).addClass(options.cssExceeded);

            } else {
				$(obj).parent().find(options.counterElement).removeClass(options.cssExceeded);
			}

		};
				
		this.each(function() {
			var counterStr = '<'+ options.counterElement +' class="' + options.css + '">'+ options.counterText +'</'+ options.counterElement +'>';
			if(options.placement == "before"){
				$(this).before(counterStr);
			}else{
				$(this).after(counterStr);
			}
			calculate(this);
			$(this).keyup(function(){calculate(this)});
			$(this).change(function(){calculate(this)});
		});
	  
	};

})(jQuery);
