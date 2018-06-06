// JS SIGNALING SERVER //
// KAYA - HIZIROGLU //
//  //

//Import some  libraries
var req = require('request'); // To send post requests
var XMLHttpRequest = require("xmlhttprequest").XMLHttpRequest; //To send post requests via rest api
var method = "POST";
var async = true;
var ipaddr = require("ipaddr.js"); //Used to get the IP address of connected hosts

var http = require("http"), url = require("url"), path = require("path"), //To host the demo web site
	fs = require("fs") ,
	port = process.argv[2] || 8888;

  var connectedPeers = [
    "TEST-HOST"
];

http.createServer(function(request, response) {

    
      var peerIP = getIPofPeer(request); //Gets the IPv4 of the connected peer as string


      // Notify the controller about the new peer
      notifyController(peerIP);


      var uri = url.parse(request.url).pathname,
          filename = path.join(process.cwd(), uri);


      fs.exists(filename, function(exists) {
        if (!exists) {
          response.writeHead(404, {"Content-Type": "text/plain"});
          response.write("404 Not Found\n");
          response.end();
          return;
        }
 	// This signaling server does not use an index.html, instead it directly server vp9 video call html page. 
	//This can be changed easily by modifying the filename attribute.
      if (fs.statSync(filename).isDirectory()) 
          //filename += '/index.html';
          filename += '/vp9svctest.html';
        fs.readFile(filename, "binary", function(err, file) {
          if (err) {
            response.writeHead(500, {"Content-Type": "text/plain"});
            response.write(err + "\n");
            response.end();
            return;
          }

		  //Prepare response headers and send file file

          //console.log("fs. STAT SYNC");
          response.writeHead(200);
          //console.log("WRITE HEAD 200");
          response.write(file, "binary");
          //console.log("file,BINARY");
          response.end();
          //console.log("END");
        });
      });
    })
    //.listen(parseInt(port, 10));
    .listen(parseInt(port));
    
console.log(
    "Static file server running at\n  => http://localhost:" + port +
    "/\nCTRL + C to shutdown\nFloodlight is working on 127.0.0.1:8080 ?");




// This function stores connected IP peers so that we only send unique IP addresses to SDN controller.
var notifyController = function(peerIP){


  //DO NOT SEND PEER IP IF IT'S ALREADY SENT !!!
  if(connectedPeers.includes(peerIP)){
    // Do nothing !
  }else{

    var requestX = new XMLHttpRequest();
    var Surl = "http://127.0.0.1:8080/wm/session/desc"; //Our custom route in the SDN controller
														//This route is automatically created if you use the Floodlight Module in the repo
  
    var post = String(peerIP);
  
  
    requestX.onload = function () {
       var status = requestX.status; // HTTP response status, e.g., 200 for "200 OK"
       var data = requestX.responseText; // Returned data, e.g., an HTML document.
    }
    requestX.open(method, Surl, async);
    requestX.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    requestX.setRequestHeader("Content-Type", "text/plain;charset=UTF-8");
    requestX.send(post);


    //Store the peer IP
    connectedPeers.push(peerIP);
  }

}


//Gets the external IP address of the connected host

var getIPofPeer = function(request){
  // Send the user data to Floodlight controller
      // Configure the request
      var ipString = request.connection.remoteAddress;
      if (ipaddr.IPv4.isValid(ipString)) {
        // ipString is IPv4
      } else if (ipaddr.IPv6.isValid(ipString)) {
        var ip = ipaddr.IPv6.parse(ipString);
        if (ip.isIPv4MappedAddress()) {
          ipString = ip.toIPv4Address().toString()
        } else {
          // ipString is IPv6
        }
      } else {
        // ipString is invalid
      }     

      if(connectedPeers.includes(ipString)){
        // Do nothing !
      }else{
        console.log("[CONNECTION]: IP : "+ ipString + "\n");
        //connectedPeers.push(ipString); -->> Let the controller do this instead.
      }

      return ipString;
}



var info={ // Some user info to send to Janus

      timeOpened:new Date(),
      timezone:(new Date()).getTimezoneOffset()/60,
  
      pageon(){return window.location.pathname},
      referrer(){return document.referrer},
      previousSites(){return history.length},
  
      browserName(){return navigator.appName},
      browserEngine(){return navigator.product},
      browserVersion1a(){return navigator.appVersion},
      browserVersion1b(){return navigator.userAgent},
      browserLanguage(){return navigator.language},
      browserOnline(){return navigator.onLine},
      browserPlatform(){return navigator.platform},
      javaEnabled(){return navigator.javaEnabled()},
      dataCookiesEnabled(){return navigator.cookieEnabled},
      dataCookies1(){return document.cookie},
      dataCookies2(){return decodeURIComponent(document.cookie.split(";"))},
      dataStorage(){return localStorage},
  
      sizeScreenW(){return screen.width},
      sizeScreenH(){return screen.height},
      sizeDocW(){return document.width},
      sizeDocH(){return document.height},
      sizeInW(){return innerWidth},
      sizeInH(){return innerHeight},
      sizeAvailW(){return screen.availWidth},
      sizeAvailH(){return screen.availHeight},
      scrColorDepth(){return screen.colorDepth},
      scrPixelDepth(){return screen.pixelDepth},
  
  
      latitude(){return position.coords.latitude},
      longitude(){return position.coords.longitude},
      accuracy(){return position.coords.accuracy},
      altitude(){return position.coords.altitude},
      altitudeAccuracy(){return position.coords.altitudeAccuracy},
      heading(){return position.coords.heading},
      speed(){return position.coords.speed},
      timestamp(){return position.timestamp},
  
  
      };
