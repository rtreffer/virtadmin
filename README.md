VirtWeb
-------

Virtweb is a proof of concept libvirt web client.
It is based on the idea that virt-manager should be available as a web ui.

Requirements
------------

VirtWeb currently requires
- ssh key exchange without password
- libvirtd configured for unix group acces (including the virtweb user)
- mdns (libvirtd+avahi)
- a modern browser with websockets support (ff & chrome work)

Features
--------
- Autodiscover servers and domains
- Show a list of servers/domains
- Tunnel the VNC terminal through SSH and WebSockets
- Start/Stop domains

Bugs/Problems
-------------

NoVNC + keymaps is broken. This is a noVNC problem, read
https://github.com/kanaka/noVNC/issues/21 for details.

There is no web-way (yet) to accept a remote key (add it to known hosts).

libvirt can't handle ipv6 ip strings.
https://bugzilla.redhat.com/show_bug.cgi?id=675991

Future
------

This is a minimal proof of concept. It consists of a few hundred lines,
it is meant to be a proof of concept. You can tunnel libvirt into your
browser, in an interactive way. You can stream the VNC console.

But, and this is really special, you don't have to build up a huge "cloud"
tool to manage a set of libvirt enabled servers.

Stick to that idea and improve it, it's most likely just a few lines until
it can be used:
- "Add"/"Save" libvirt urls (non-mdns setup)
- A java vnc fallback or virtual keyboard/map
- Authentification
- Auto-refresh of serverlist
- Collaps servers
- Indicator which domains are running

License
-------

This is just a dump wrapper over a few libs. Take it as BSD, Apache or LGPL.

Just keep in mind that you must respect the licenses of
- novnc (lgpl v3, so keep it "linked")
- xitrum (MIT)
- jquery (MIT, BSD, GPL)
- underscore.js (BSD or MIT, not 100% sure)
- sshj2 (Apache)
- jzlib (BSD)
- jsch (BSD)
- bouncycastle (MIT/X11)
- Twitter bootstrap (Apache)
- Netty (Apache)

