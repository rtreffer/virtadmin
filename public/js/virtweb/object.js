window.virtweb = window.virtweb || {};

window.virtweb.extend = function(base, proto) {
    if (typeof proto == 'undefined') {
        proto = base;
        base = false;
    }

    var x = function(attr) {
        if (typeof attr != 'undefined') {
            _.extend(this, attr);
        }
        var chain = [this];
        while (chain[chain.length -1 ].__proto__) {
            chain.push(chain[chain.length -1 ].__proto__);
        }
        for (var i = 0; i < chain.length; i++) {
            if (chain[i].hasOwnProperty('defaults')) {
                _.defaults(this, chain[i].defaults);
            }
        }
        chain = chain.reverse();
        for (var i = 0; i < chain.length; i++) {
            if (typeof chain[i].initialize == 'function' &&
                chain[i].hasOwnProperty('initialize')
            ) {
                chain[i].initialize.call(this);
            }
        }
        return this;
    };

    x.prototype = proto;

    if (typeof base != 'undefined' && base) {
        if (typeof base == 'function') {
            proto.__proto__ = base.prototype;
        }
        if (typeof base == 'object') {
            proto.__proto__ = base;
        }
    }

    return x;
};
