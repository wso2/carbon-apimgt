/*
 Description: Renders the assets.jag view
 Filename:assets.js
 Created Date: 29/7/2013
 */

var render = function (theme, data, meta, require) {
    var listPartial = 'list-assets';
//Determine what view to show
    switch (data.op) {
        case 'list':
            listPartial = 'list-assets';
            data = require('/helpers/view-asset.js').format(data);
            break;
        case 'statistics':
            listPartial = 'statistics';
            data = require('/helpers/view-asset.js').format(data);
            break;
        default:
            break;
    }
    //var addAssetUrl = "/publisher/asset/" + data.meta.shortName +"";
    theme('single-col-fluid', {
        title: data.title,
        header: [
            {
                partial: 'header',
                context: data
            }
        ],
        ribbon: [
            {
                partial: 'ribbon',
                context:data
            }
        ],
        leftnav: [
            {
                partial: 'left-nav',
                context: require('/helpers/left-nav.js').generateLeftNavJson(data, listPartial)
            }
        ],
        listassets: [
            {
                partial: listPartial,
                context: data
            }
        ]
    });
};
