SVGElement.prototype.getTransformToElement = SVGElement.prototype.getTransformToElement || function(elem) {
return elem.getScreenCTM().inverse().multiply(this.getScreenCTM());
};
//https://github.com/cpettitt/dagre-d3/issues/202
