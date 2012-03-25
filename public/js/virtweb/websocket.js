window.virtweb = window.virtweb || {};

window.virtweb.WebSocket = virtweb.extend({
    initialize: function() {
        if (!this.onmessage) {
            this.onmessage = _.bind(this.logmessage, this);
        }
        this.connect();
    },
    logmessage: function(msg) {
        console.log(msg);
    },
    connect: function() {
        if (this.closed) {
            return;
        }
        if (this.socket) {
            try { this.socket.close(); } catch (e) {}
        }
        if (this.pathname) {
            console.log("connect to " + this.server + this.pathname);
            this.socket = new WebSocket(this.server + this.pathname);
        } else {
            this.socket = new WebSocket(this.server);
        }
        this.socket.onclose = _.bind(this.onclose, this);
        this.socket.onmessage = _.bind(this.onmessage, this);
    },
    onclose: function() {
        if (this.closed) {
            window.setTimeout(_.bind(this.connect,this), this.reconnectDelay);
        }
    },
    close: function() {
        this.closed = true;
        if (this.socket) {
            this.socket.close();
        }
    },
    defaults: {
        reconnectDelay: 1000,
        server: 'ws://' +
                window.location.hostname +
                ((!window.location.port) ? '' : (':' + window.location.port)),
        closed: false
    }
});
