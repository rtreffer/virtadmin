window.virtweb = window.virtweb || {};
window.virtweb.app = window.virtweb.app || {};

window.virtweb.app.mainpanel = new virtweb.MainPanel();

window.virtweb.app.serverlist = new virtweb.ServerList({
    onserverclick: function(server, domain) {
        window.virtweb.app.mainpanel.load(server, domain);
    }
});
