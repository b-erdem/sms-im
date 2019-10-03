# SmsIM
SmsIM provides necessary tools for SMS messaging from your browser. Mobile app, Web UI and server. It uses WebSocket connection between mobile app and server. Nothing stored on the server. There is no database on backend. When a user scanned QRCode and entered to the Web UI, some events sent to mobile app to get SMS messages.

SmsIM uses your real SMS for sending SMS messages.

# Quickstart
If you want to quickly test it, just visit this page: https://websms.erdem.dev/
* Download the APK
* Give requested permissions.(Camera(for QRCode scanning), Contacts(for showing your contacts on Web UI), Read/Send SMS(for showing your SMS messages on Web UI)).
* Scan QRCode on the Web UI using mobile app.
* Now you connected. You can receive/send SMS messages from the Web UI.

# How It Works
When you visit https://websms.erdem.dev/: 
### Server:
* creates an unique and random topic.
* Encode topic in QRCode.
* Send QRCode and topic name to web app.

### Web UI:
* Displays QRCode image.
* Connects WebSocket using topic name.

### Mobile App
* When user scans the QRCode, he/she gets topic name encoded in QRCode image.
* Connects to the channel using topic name.
* Ceremony is done. Now user can send/receive SMS messages.

# Contributions
There are a lot of features can be implemented. It would be great to choose and implement one of them if you liked. Here is a few that comes to mind:
* Search functionality.
* Choose a contact and send SMS.
* Add presence information so user can see which devices are connected to the channel.
* Better feedback on mobile app.
* Better authentication/authorization flow.
* Persistent websocket connection. When user closed the tab, it must initiated new WebSocket connection for now.
* ~~WebSocket connection drops when user's phone sleeps.~~
* e2e encryption.

# Acknowledgments
Thanks to [@muhammederdem](https://github.com/muhammederdem) for contributions to Web UI.

# Licensing
SmsIM is released under the terms of the GNU Lesser General Public License v3.0 license.
