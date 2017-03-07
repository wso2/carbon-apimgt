//----------------------- Google Analytics -----------------------
(function (i, s, o, g, r, a, m) {
    i['GoogleAnalyticsObject'] = r;
    i[r] = i[r] || function () {
            (i[r].q = i[r].q || []).push(arguments)
        }
        , i[r].l = 1 * new Date();
    a = s.createElement(o),
        m = s.getElementsByTagName(o)[0];
    a.async = 1;
    a.src = g;
    m.parentNode.insertBefore(a, m)
})(window, document, 'script', '//www.google-analytics.com/analytics.js', 'ga');
ga('create', 'UA-XXXXXXXX-X', 'auto');
ga('send', 'pageview');


//----------------------- Google Tag Manager -----------------------
(function (w, d, s, l, i) {
    w[l] = w[l] || [];
    w[l].push(
        {'gtm.start': new Date().getTime(), event: 'gtm.js'}
    );
    var f = d.getElementsByTagName(s)[0],
        j = d.createElement(s), dl = l != 'dataLayer' ? '&l=' + l : '';
    j.async = true;
    j.src =
        '//www.googletagmanager.com/gtm.js?id=' + i + dl;
    f.parentNode.insertBefore(j, f);
})(window, document, 'script', 'dataLayer', 'XXXXXXXX');


//----------------------- Zopim Chat -------------------------------
window.$zopim || (function (d, s) {
    var z = $zopim = function (c) {
        z._.push(c)
    }, $ = z.s =
        d.createElement(s), e = d.getElementsByTagName(s)[0];
    z.set = function (o) {
        z.set._.push(o)
    };
    z._ = [];
    z.set._ = [];
    $.async = !0;
    $.setAttribute("charset", "utf-8");
    $.src = "//v2.zopim.com/?3u5GXGidREH2DDzU8flaiJan1BdOwqNk";
    z.t = +new Date;
    $.type = "text/javascript";
    e.parentNode.insertBefore($, e)
})(document, "script");

$zopim(function () {
    var name = $("#zopim-name").attr('value');
    if (name.indexOf('null') < 0) {
        $zopim.livechat.setName(name);
        $zopim.livechat.setEmail($("#user-email").attr('value'));
    }
});