import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.tools.ForwardingFileObject;

import org.jgrapht.EdgeFactory;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;



import com.google.gson.Gson;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.ssl.HttpsURLConnection;

import main.*;
import sun.net.www.protocol.https.HttpsURLConnectionImpl;

public class TEM {
	
	/*
	
	Traffic Engineering Manager

	Connects to SDN Controller.

	Gets topologoy, computes paths and sends them to SDN Controller.
	
	 */
	
	
	public static String getTopology() throws IOException {
		String url = "http://127.0.0.1:8080/wm/topology/links/json";
		
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		//add request header
		con.setRequestProperty("User-Agent", "TEM");

		int responseCode = con.getResponseCode();
		//System.out.println("\nSending 'GET' request to URL : " + url);
		//System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		//print result
		//System.out.println(response.toString());
		
		if(response.toString().length() < 5) {
			System.out.println("\n[FATAL ERROR]: MININET NOT STARTED !");
			System.exit(0);
		}
		
		
		//Remove bad regex characters and return
		return response.toString().
				replaceAll("src-port", "srcport").
				replaceAll("dst-port", "dstport").
				replaceAll("src-switch", "srcswitch").
				replaceAll("dst-switch", "dstswitch");
	}
	
	
	   

	public static String getBandwidths() throws IOException {
			String url = "http://127.0.0.1:8080/wm/statistics/bandwidth/all/all/json";
			
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// optional default is GET
			con.setRequestMethod("GET");

			//add request header
			con.setRequestProperty("User-Agent", "TEM");

			int responseCode = con.getResponseCode();
			//System.out.println("\nSending 'GET' request to URL : " + url);
			//System.out.println("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			//print result
			//System.out.println(response.toString());

			
			//Remove bad regex characters and return
			return response.toString().
					replaceAll("link-speed-bits-per-second", "linkspeedbitspersecond").
					replaceAll("bits-per-second-rx", "bitspersecondrx").
					replaceAll("bits-per-second-tx", "bitspersecondtx");
		}

	private static String topology;

    private static Link[] forwardLinks;
    private static Link[] backwardLinks;
    
    private static DefaultDirectedGraph<String, DefaultEdge> forwardGraph;// = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
    private static DefaultDirectedGraph<String, DefaultEdge> backwardGraph;// = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);

    
    private static DefaultDirectedGraph<String, DefaultEdge> liveGraph;// Holds live info about bandwidths
    
    
    public static Link[] formBackwardLinks(Link[] links) {
    	Link[] backwardLinks_tmp = new Link[forwardLinks.length];
    	
    	for(int i=0;i<forwardLinks.length;i++) {
    		//backwardLinks[i] = new Link(srcswitch, srcport, dstswitch, dstport, type, direction, latency)
    		backwardLinks_tmp[i] = new Link(forwardLinks[i].dstswitch,
    				forwardLinks[i].dstport, forwardLinks[i].srcswitch, 
    				forwardLinks[i].srcport, forwardLinks[i].type, 
    				forwardLinks[i].direction, 
    				forwardLinks[i].latency);
    	}
    	
    	return backwardLinks_tmp;
    }
    
    
    //Generic graph creator. Use forward/reverse link lists to create forward/reverse graphs.
    public static DefaultDirectedGraph<String, DefaultEdge> formGraphFromLinks(Link[] links) {
    	  DefaultDirectedGraph<String, DefaultEdge> graph = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
    	  for(Link l:links) {
    		  graph.addVertex(l.srcswitch);
    		  graph.addVertex(l.dstswitch);
    		  graph.addEdge(l.srcswitch, l.dstswitch);  	
    		  graph.addEdge(l.dstswitch, l.srcswitch);  	
    	  }
    	  return graph;
    }
    
    
    
    //Gets the bandwidth info from the controller, updates the live graph (removes the slow links)
    public static DefaultDirectedGraph<String, DefaultEdge> updateLiveGraph(DefaultDirectedGraph<String, DefaultEdge> g) throws IOException {
  	  DefaultDirectedGraph<String, DefaultEdge> graph = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
  	  
  	  String bandwidth = getBandwidths();
  	  Bandwidth[] bl = TEM.g.fromJson(bandwidth, Bandwidth[].class); //Parse every link in the topology
  	  for(Bandwidth b:bl) {
  		  System.out.println(b.bitspersecondtx);
  	  }
  	  
  	  
  	  return graph;
  }
  
	//Sample Signaling Server Datapath ID. This can be changed by sending a POST request to TEM
    private static String signalingServerID = "00:00:00:00:00:00:00:06";
    
    
    public static String getSSID() {
    	return signalingServerID;
    }
    
    public static void setSSID(String ssID) {
    	signalingServerID = ssID;
    }
    
    
    public static String findPortOfSwitch(String macID) {
    	
    	String portNo = "";
    	
    	for(Link l1:forwardLinks) {
    		if(l1.srcswitch.equals(macID)) {
    			portNo = l1.srcport;
    			return portNo;
    		}
    	}
    	
    	for(Link l2:backwardLinks) {
    		if(l2.srcswitch.equals(macID)) {
    			portNo = l2.srcport;
    			return portNo;
    		}
    	}
    
    	return portNo;
    }
    
    
    
    private static String HOST; //MAC ID of the switch connected to the host
    private static String SFU;  //MAC ID of the switch connected to the SFU
    private static String GRE;  //MAC ID of the GRE switch 
    
    public static volatile Boolean didStart=false;
    
    public static void setIDs(String id1, String id2, String id3) {
    	HOST = id1;
    	SFU = id2;
    	GRE = id3;
    }
    
    
    @SuppressWarnings("unchecked")
	public static void sendPathsToController() throws IOException {
    
    	if(didStart) { //Only find path if initialized
		    	
		    	//Clone the topology 
		    	//make a deep clone of Al
				DefaultDirectedGraph<String, DefaultEdge> graph = (DefaultDirectedGraph<String, DefaultEdge>)deepClone(forwardGraph);
				
		    	//Create new graph without slow links
		    	
		    	//Check both forward and reverse links to find 
		    	String bandwidth = getBandwidths();
		    	Bandwidth[] bl = TEM.g.fromJson(bandwidth, Bandwidth[].class); //Parse every link in the topology
		    	System.out.println(bl.length);
		    	  
		    	  for(Bandwidth b:bl) { //For each bandwidth info, check each Link[] lists
		    		  
		    		  //Only check if bandwidth is lower than threshold
		    		  // Threshold = 500bits/sec
		    		  
						if(Integer.parseInt(b.bitspersecondtx) > 500) { //500KBits
		    			
		
		        		  for(Link f:forwardLinks) {
		        			  if(f.getSrcswitch().equals(b.dpid)) {
		        				  graph.removeEdge(f.getSrcswitch(), f.getDstswitch());
		        			  }
		        		  }
		        		  
		        		  for(Link f:backwardLinks) {
		        			  if(f.getSrcswitch().equals(b.dpid)) {
		        				  graph.removeEdge(f.getSrcswitch(), f.getDstswitch());
		        			  }
		        		  }
		    		  }
		    	  }
		    	  
		    	  //Now we have the new graph with slow links dropped.
		    	  
		    	  //Form 4 paths and send them to controller
		    	  
		    	  // [PATH] HOST - SFU
		    	  // [PATH] SFU  - HOST
		    	  // [PATH] SFU  - GRE
		    	  // [PATH] GRE  - SFU
		    	  List<DefaultEdge> l = DijkstraShortestPath.findPathBetween(graph, HOST, SFU);
		    		
		    	  List<DefaultEdge> l2 = DijkstraShortestPath.findPathBetween(graph, SFU, HOST);
		    		
		    	  List<DefaultEdge> l3 = DijkstraShortestPath.findPathBetween(graph, SFU, GRE);
		    		
		    	  List<DefaultEdge> l4 = DijkstraShortestPath.findPathBetween(graph, GRE, SFU);
		    		
		    	  if(l.size()==0 || l2.size()==0 || l3.size()==0 || l4.size()==0) {
		    		  return;
		    	  }
		    	  
				String path1 = "";
		    	for(DefaultEdge x:l) {
		    		System.out.println(x.toString());
		    		String sw = x.toString().substring(1,24);
		    		path1 += sw + "Z" + findPortOfSwitch(sw) +"M";
		    	}
		    	path1 = path1.substring(0,path1.length()-1);
		    	  
		    	String path2 = "";
		    	for(DefaultEdge x:l2) {
		    		System.out.println(x.toString());
		    		String sw = x.toString().substring(1,24);
		    		path2 += sw + "Z" + findPortOfSwitch(sw) +"M";
		    	}
		    	path2 = path2.substring(0,path2.length()-1);
		    	  
		    	
		    	String path3 = "";
		    	for(DefaultEdge x:l3) {
		    		System.out.println(x.toString());
		    		String sw = x.toString().substring(1,24);
		    		path3 += sw + "Z" + findPortOfSwitch(sw) +"M";
		    	}
		    	path3 = path3.substring(0,path3.length()-1);
		    	  
		    	
		    	String path4 = "";
		    	for(DefaultEdge x:l4) {
		    		System.out.println(x.toString());
		    		String sw = x.toString().substring(1,24);
		    		path4 += sw + "Z" + findPortOfSwitch(sw) +"M";
		    	}
		    	path4 = path4.substring(0,path4.length()-1);
		    	  
		    	  
		    	if(l.size()==0 || l2.size()==0  || l3.size()==0 || l4.size()==0) {
		    		System.out.println("[TEM] ERROR CODE 3412");
		    	}
		    	 
		    	
		    	String response = path1 + "A" + path2 + "A" + path3 + "A" + path4;
		    	System.out.println("Sending this path to controller:\n");
		    	System.out.println(response);
		    	
				
				//Previous implementation sent the paths to SDN controller periodically.

				//Current version creates paths periodically and only sends them when SDN controller sends a request.
			
		    	//sendPostRequest("http://127.0.0.1:8080/wm/session/desc", response);
    	
    	}
    
    }
    
    
    @SuppressWarnings("unchecked")
	public static String createPath(String macID) throws IOException {
    	//Clone the topology 
    	//make a deep clone of Al
		DefaultDirectedGraph<String, DefaultEdge> graph = (DefaultDirectedGraph<String, DefaultEdge>)deepClone(forwardGraph);
		
    	//Create new graph without slow links
    	
    	//Check both forward and reverse links to find 
    	String bandwidth = getBandwidths();
    	Bandwidth[] bl = TEM.g.fromJson(bandwidth, Bandwidth[].class); //Parse every link in the topology
    	System.out.println(bl.length);
    	  
    	  for(Bandwidth b:bl) { //For each bandwidth info, check each Link[] lists
    		  
    		  //Only check if bandwidth is lower than threshold
    		  // Threshold = 60bits/sec
    		  //System.out.println(b.bitspersecondtx);
    		  if(Integer.parseInt(b.bitspersecondtx) > 500) { //500KBits
    			
					
				//Remove links that are below the threshold from the graph
        		  for(Link f:forwardLinks) {
        			  if(f.getSrcswitch().equals(b.dpid)) {
        				  graph.removeEdge(f.getSrcswitch(), f.getDstswitch());
        			  }
        		  }
        		  
        		  for(Link f:backwardLinks) {
        			  if(f.getSrcswitch().equals(b.dpid)) {
        				  graph.removeEdge(f.getSrcswitch(), f.getDstswitch());
        			  }
        		  }
    		  }
    	  }
    	  
    	  
    	//Check the lenght of the new graph. If there are no paths, there is probably conflicting datapath IDs.
    	 
    	List<DefaultEdge> l = DijkstraShortestPath.findPathBetween(graph, macID, signalingServerID);
  		if(l.size()==0) {
  			System.out.println("TEM FOUND A PATH WITH 0 LENGTH.\nSOMETHING WRONG?\nmacID="+macID+"\nSSID="+signalingServerID+"\n");
  		}
  		
  		
  		String path = "";
    	for(DefaultEdge x:l) {
    		System.out.println(x.toString());
    		path += x.toString() + "Z";
    	}
    	path = path.substring(0,path.length()-1);
    	//Here, find the relevant switch/port tuples and send them to controller

    	String[] routes = path.split("Z");
    	if(routes.length>1) { //If not single hop , iterate through paths and append with switch/port IDs.
    		
    		String responseToController = "";
    		
    		for(String hop : routes) {
    			
    			String port = findPortOfSwitch(hop.substring(1, 24));
    			if(port.length()==0) {
    				System.out.println(" SOMETHING WRONG [ERROR CODE] : 9932");	
    			}
    			responseToController += hop.substring(1, 24) + "/" + port +"A";    			
    		}
    		
    		System.out.println("TEM FOUND PATH :\n");
    		System.out.println(responseToController.substring(0, responseToController.length()-1));

    		sendPostRequest("", responseToController.substring(0, responseToController.length()-1));
    		
    		
    	}else if(routes.length == 1) { //If single hop, form the response and send.
    		String firstHop = routes[0].substring(1, 24);
    		
    		String port = findPortOfSwitch(firstHop);
    		

    		String responseToController = "";
    		
    		responseToController += firstHop + port;
    		
    		System.out.println(responseToController);
    		sendPostRequest("", responseToController);
    		
    	}else {
    		System.out.println(" SOMETHING WRONG [ERROR CODE] : 9933");
    	}
    	
    	
    	return "";
    }
    
    
    /**
     * This method makes a "deep clone" of any Java object it is given.
     * Used to deep copy old topology while updating the links
	 * Using a default copy may lead to wrong paths during the computation.
     */
     public static Object deepClone(Object object) {
       try {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(object);
         ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
         ObjectInputStream ois = new ObjectInputStream(bais);
         return ois.readObject();
       }
       catch (Exception e) {
         e.printStackTrace();
         return null;
       }
     }
     
     
	//Returns the known links in the topology
     public static synchronized String getLinks() {
    	 String res = "";
    	 for(Link x:forwardLinks) {
    		 res += x.srcswitch + "-" + x.dstswitch + "\n";
    	 }
    	 return res;
     }
    
    
    private static Gson g = new Gson();
    
	public static void main(String[] args) throws IOException {

		//Enable statistics collection on controller
		sendPostRequest("http://127.0.0.1:8080/wm/statistics/config/enable/json","");
		
	
		int port = 4000;
		
		InetSocketAddress addr = new InetSocketAddress(port);
		HttpServer server = HttpServer.create(addr, 0);

		server.createContext("/", new MasterHandler());
		server.setExecutor(Executors.newCachedThreadPool());
		server.start();
		System.out.println("TEM listening on port : "+ port + "\n" );
		
		
		
		//First, get the topology from the controller and store it
		topology = getTopology();
		
		
	    //Parse the topology and form Link array
	    forwardLinks = g.fromJson(topology, Link[].class); //Parse every link in the topology
		backwardLinks = formBackwardLinks(forwardLinks); //Parse link in the reverse order - Reason: only directed edges are allowed !
		
		
		
		//Create graphs
		forwardGraph = formGraphFromLinks(forwardLinks);
		backwardGraph = formGraphFromLinks(backwardLinks);
		

		
		//createPath("asdasd");
		Timer t = new Timer();
		t.schedule(new TimerTask() {
		    @Override
		    public void run() {
		       try {
				sendPathsToController();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    }
		}, 0, 2000);
	}
	
	
	
	//Forms POST request headers and sends Post request to defined URL
	public static String sendPostRequest(String requestUrl, String payload) {
		StringBuffer jsonString = new StringBuffer();
        
		try {
	        URL url = new URL(requestUrl);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

	        connection.setDoInput(true);
	        connection.setDoOutput(true);
	        connection.setRequestMethod("POST");
	        connection.setRequestProperty("Accept", "application/json");
	        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
	        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
	        writer.write(payload);
	        writer.close();
	        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        String line;
	        while ((line = br.readLine()) != null) {
	                jsonString.append(line);
	        }
	        br.close();
	        connection.disconnect();
	    } catch (Exception e) {
	            throw new RuntimeException(e.getMessage());
	    }
	    return jsonString.toString();
	}
	
	
}


class MasterHandler implements HttpHandler {

	public void handle(HttpExchange exchange) throws IOException {
		String requestMethod = exchange.getRequestMethod();
		if (requestMethod.equalsIgnoreCase("GET")) {
			System.out.println("GOT GET REQUEST !!!!!"
					+ "\n");
			Headers responseHeaders = exchange.getResponseHeaders();
			responseHeaders.set("Content-Type", "text/plain");
			exchange.sendResponseHeaders(200, 0);

			OutputStream responseBody = exchange.getResponseBody();
			Headers requestHeaders = exchange.getRequestHeaders();
				
			//Reports the known links in the topology

			String easyUse = "[TEM REPORT] Links in the topology:\n";
			responseBody.write(easyUse.getBytes());
			String res = TEM.getLinks();
			responseBody.write(res.getBytes());
			responseBody.close();
		}


		else if(requestMethod.equalsIgnoreCase("POST")) {
			System.out.println("GOT POST REQUEST !!!!!"
					+ "\n");
			System.out.println(requestMethod + " /post");

			String body = new BufferedReader(
					new InputStreamReader(
							exchange.getRequestBody()
							)
					).lines().collect(Collectors.joining("\n"));
			String[] parts = body.split("=");
			String name = null;
			if (parts.length > 1) {
				name = parts[1];
			}
			exchange.getResponseHeaders().set("Content-Type", "text/plain");
			exchange.sendResponseHeaders(200, 0);
			OutputStream responseBody = exchange.getResponseBody();
			Headers requestHeaders = exchange.getRequestHeaders();

			System.out.println(body);

			//Checks the input form first. The correct format should have 3 datapath IDs with the letter 'A' between them.
			String[] macIDs = body.split("A");
			if(macIDs.length!=3) {
				String msg = "[TEM REPORT] INVALID FORMAT (Put 'A' between MAC IDs).\n";
				responseBody.write(msg.getBytes());
				responseBody.close();  
			}else { //Format is OK!
				TEM.setIDs(macIDs[0], macIDs[1], macIDs[2]);
				TEM.didStart = true;
				responseBody.close();  
			}
			
			
			
		}
	}
}
