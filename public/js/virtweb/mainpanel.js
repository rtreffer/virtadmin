window.virtweb.MainPanel = virtweb.extend({
    initialize: function() {
        this.vncconnecthandler        = _.bind(this.vncconnect, this);
        this.delayedvncconnecthandler = _.bind(this.delayedvncconnect, this);
        this.starthandler             = _.bind(this.start,      this);
        this.shutdownhandler          = _.bind(this.shutdown,   this);
        this.reboothandler            = _.bind(this.reboot,     this);
        this.forceoffhandler          = _.bind(this.forceoff,   this);
        $('button#start'   ).click(this.starthandler);
        $('button#reboot'  ).click(this.reboothandler);
        $('button#shutdown').click(this.shutdownhandler);
        $('button#forceoff').click(this.forceoffhandler);
    },
    load: function(server, domain) {
        if (server) {
            this.server = server;
        } else {
            server = this.server;
        }
        if (domain) {
            this.domain = domain;
        } else {
            domain = this.domain;
        }
        this.domainUrl = "domain/" + server.ip + "/" + domain.name;
        console.log('Load domain ' + JSON.stringify(domain) + " on " +
                JSON.stringify(server));
        this.rfb = new RFB({
            'target':       document.getElementById('noVNC_canvas'),
            'encrypt':      WebUtil.getQueryVar('encrypt',
                     (window.location.protocol === "https:")),
            'true_color':   WebUtil.getQueryVar('true_color', true),
            'local_cursor': WebUtil.getQueryVar('cursor', true),
            'shared':       WebUtil.getQueryVar('shared', true),
            'view_only':    WebUtil.getQueryVar('view_only', false),
            'updateState':  function (e) {},
            'onPasswordRequired':  function () {}
        });
        this.vncconnect();
    },
    delayedvncconnect: function() {
        console.log("delayedvncconnect");
        window.setTimeout(this.vncconnecthandler, 1000);
    },
    vncconnect: function() {
        console.log("vncconnect");
        this.rfb.connect(
                window.location.hostname,
                (!window.location.port) ? '' : window.location.port,
                '',
                'novnc/' + this.server.ip + "/" + this.domain.name
            );
    },
    start: function() {
        console.log("start");
        var call = $.ajax({url: this.domainUrl + "/create"});
        call.done(this.delayedvncconnecthandler)
    },
    shutdown: function() {
        $.ajax({url: this.domainUrl + "/shutdown"});
    },
    reboot: function() {
        $.ajax({url: this.domainUrl + "/reboot"});
    },
    forceoff: function() {
        $.ajax({url: this.domainUrl + "/destroy"});
    }
});