window.virtweb.ServerList = virtweb.extend({
    initialize: function() {
        var onserverclick = _.bind(this.onserverclick, this);
        this.onserverclickhandler = function() {
            var server = $.parseJSON(this.getAttribute('data-server'));
            var domain = $.parseJSON(this.getAttribute('data-domain'));
            onserverclick(server, domain);
        };
        this.updateserverhandler = _.bind(this.updateserver, this);
        this.update();
    },
    pathname: "/servers",
    updateserver: function(servers) {
        console.log(servers);
        var root = $('#server');
        root.empty();
        for (var i = 0, l = servers.length; i < l; i++) {
            var server = servers[i];

            var serverDom = document.createElement('ul');
            serverDom.setAttribute('class', 'nav nav-list');
            var titleDom = document.createElement('li');
            titleDom.setAttribute('class', 'nav-header');
            titleDom.appendChild(document.createTextNode(server.name));
            serverDom.appendChild(titleDom);

            for (var j = 0, m = server.domains.length; j < m; j++) {
                var domain = server.domains[j];
                var domainDom = document.createElement('li');
                var domainLink = document.createElement('a');
                domainDom.appendChild(domainLink);
                domainLink.appendChild(document.createTextNode(domain.name));
                domainLink.setAttribute('href', '#');
                domainLink.setAttribute('data-domain', JSON.stringify(domain));
                domainLink.setAttribute('data-server', JSON.stringify(
                        {'ip':server.ip,'name':server.name}));
                serverDom.appendChild(domainDom);
            }

            root.append(serverDom);
            root.find('a').click(this.onserverclickhandler);
        }
    },
    onserverclick: function(server, domain) {
        console.log('Clicked domain ' + JSON.stringify(domain) + " on " +
                JSON.stringify(server));
    },
    update: function() {
        $.ajax({url: "servers"}).done(this.updateserverhandler);
    }
});
