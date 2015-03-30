var renderAssets, mouseStop, renderAssetsScroll;

(function () {
    renderAssets = function (data) {
        var el = $('.store-left');
        caramel.css($('head'), data.body['sort-assets'].resources.css, 'sort-assets');
        caramel.code($('head'), data.body['assets'].resources.code);
        caramel.partials(data._.partials, function () {
            var assets = Handlebars.partials['assets'](data.body.assets.context),
                sort = Handlebars.partials['sort-assets'](data.body['sort-assets'].context);
            theme.loaded(el, sort);
            el.append(assets);
            caramel.js($('body'), data.body['assets'].resources.js, 'assets', function () {
                mouseStop();
            });
            caramel.js($('body'), data.body['sort-assets'].resources.js, 'sort-assets', function () {
                updateSortUI();
            });
            $(document).scrollTop(0);

            infiniteScroll = data.body.assets.context.assets.length >= 12;
        });
    };

    renderAssetsScroll = function(data){
    	var temp = '{{#slice assets size="4"}}<div class="row-fluid">';
        	temp += '{{#each .}}';
			temp += '<div class="span3 asset" data-id="{{id}}" data-path="{{path}}" data-type="{{type}}">';
			temp += '	{{#attributes}}';
			temp += '	<a href="{{url "/asts"}}/{{../type}}/details/{{../id}}">';
			temp += '	<div class="asset-icon">';	
			temp += '		{{#if ../indashboard}}';	
			temp += '				<i class="icon-bookmark store-bookmark-icon"></i>';	
			temp += '		{{/if}}';		
			temp += '	<img src="{{#if images_thumbnail}}{{url ""}}/storage/{{../../../../rxt.shortName}}/{{../../id}}/{{../../thumbnail}}{{/if}}">';
			temp += '	</div> </a>';
			temp += '	<div class="asset-details">';
			temp += '		<div class="asset-name">';
			temp += '			<a href="{{url "/asts"}}/{{../type}}/details/{{../id}}"> <h4>{{overview_name}}</h4> </a>';
			temp += '		</div>';
			temp += '		<div class="asset-rating">';
			temp += '			<div class="asset-rating-{{../avgRating}}star">';
			temp += '			</div>';
			temp += '		</div>';
			temp += '		<div class="asset-author-category">';
			temp += '			<ul>';
			temp += '				<li>';
			temp += '					<h4>{{t "Version"}}</h4>';
			temp += '					<a class="asset-version" href="#">{{overview_version}}</a>';
			temp += '				</li>';
			temp += '				<li>';
			temp += '					<h4>{{t "Category"}}</h4>';
			temp += '					<a class="asset-category" href="#">{{cap ../type}}</a>';
			temp += '				</li>';
			temp += '				<li>';
			temp += '					<h4>{{t "Author"}}</h4>';
			temp += '					<a class="asset-author" href="#">{{overview_provider}}</a>';					
			temp += '				</li>';
			temp += '			</ul>';
			temp += '			{{#if ../indashboard}}';
			temp += '			<a href="#" class="btn disabled btn-added">{{t "Bookmarked"}}</a>';
			temp += '			{{else}}';
			temp += '				{{# if ../../../../sso}}';			
			temp += '				<a href="{{url "/login"}}" class="btn btn-primary asset-add-btn">{{t "Bookmark"}}</a>';
			temp += '				{{else}}';							
			temp += '					<a href="#" class="btn btn-primary asset-add-btn">{{t "Bookmark"}}</a>';
			temp += '				{{/if}}';
			temp += '				{{# if ../../../../user.username}}';		
			temp += '				<a href="#" class="btn btn-primary asset-add-btn">{{t "Bookmark"}}</a>';
			temp += '				{{/if}}';
			temp += '			{{/if}}';
			temp += '		</div>';
			temp += '	</div>';
			temp += '	{{/attributes}}';
			temp += '</div>';
			temp += '{{/each}}';
			temp += '</div>{{/slice}}';
			
      var assetsTemp = Handlebars.compile(temp);
 	  var render = assetsTemp(data.body.assets.context);
      $('#assets-container').append(render);
      
       caramel.js($('body'), data.body['assets'].resources.js, 'assets', function () {
                mouseStop();
            });
    	
    };

    mouseStop = function () {
    	var windowWidth = $(window).width();
    	var offsetTop = windowWidth < 980 ? 167 : 200;
        var id;
        $('.asset').mousestop(function () {
            var that = $(this);
            id = setTimeout(function () {
		that.find('.store-bookmark-icon').animate({
		    top : -200
		}, 200);
                that.find('.asset-details').animate({
                    top: 0
                }, 200);
            }, 300);
        }).mouseleave(function () {
                clearTimeout(id);
		$(this).find('.store-bookmark-icon').animate({top: -4}, 200);
                $(this).find('.asset-details').animate({top: offsetTop}, 200);
            });
    };
}());
