import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;

import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.protocol.OFPortDescProp;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionEnqueue;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.OFPort;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.security.ntlm.Client;

import net.floodlightcontroller.clientAttachment.ClientAttachment;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.internal.OFSwitchManager;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.util.SingletonTask;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.restserver.RestletRoutable;
import net.floodlightcontroller.routing.IRoutingService;
import net.floodlightcontroller.threadpool.IThreadPoolService;
import net.floodlightcontroller.util.OFMessageDamper;

public class SDNslicing extends ServerResource implements IFloodlightModule, IRestApiService {

	protected IFloodlightProviderService floodlightProvider;
	protected IThreadPoolService threadPoolService;
	protected IOFSwitchService switchService;
	protected IRoutingService routingEngine;
	protected static Logger logger;
	protected OFMessageDamper messageDamper;
	protected boolean done = true;
	protected String[] edgeSwitchesID = new String[1000];
	protected String[] hostIPV4 = new String[1000];;
	protected String[] switchIDTEM =new String[1000];;
	protected Integer[] oFPortTEM = new Integer[1000];;
	protected HashMap<String, Integer> queueMap= new HashMap<String, Integer>(); 

	protected IRestApiService restApiService;

    private static String HOST = "00:00:00:00:00:00:00:01" ; //MAC ID of the switch connected to the host
    private static String SFU = "00:00:00:00:00:00:00:05";  //MAC ID of the switch connected to the SFU
    private static String GRE = "00:00:00:00:00:00:00:05";  //MAC ID of the GRE switch 

    private static String host1IP = "10.0.0.55";
    private static String host2IP = "10.0.0.22";
    private static String sfuIP = "10.0.0.56";
    
	
	public boolean isNewID(String id, int port) {
		if(queueMap.size() == 0) {
			return true;
		}
		else {
			Iterator it =queueMap.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry)it.next();
				if(pair.getKey().equals(id) && port == (Integer) pair.getValue()) {
					return false;
				}
				System.out.println(pair.getKey() + " = " + pair.getValue());

			}
		}

		return true;

	}
    
    //Opens queues for the switches in the received path from TEM
    private void queueOpener(DatapathId datapathID, String ipv4Source, String ipv4Dest, int portNumber) throws IOException {
    	String s = datapathID.toString();
    	System.out.println(s);,
    	//System.out.println(switchService.toString());
    	//IOFSwitch mySwitch = switchService.getSwitch(datapathID);
    	
    	OFSwitchManager asd = new OFSwitchManager();
    	
    	IOFSwitch mySwitch = asd.getActiveSwitch(datapathID);
    	
		OFFactory myFactory = mySwitch.getOFFactory();
		System.out.println(mySwitch.getId().toString());
		Match m = myFactory.buildMatch()
				.setExact(MatchField.IPV4_SRC, IPv4Address.of(ipv4Source))
				.setExact(MatchField.ETH_TYPE, EthType.IPv4)
				.setExact(MatchField.IP_PROTO, IpProtocol.UDP)
				.setExact(MatchField.IPV4_DST, IPv4Address.of(ipv4Dest))
				.build();
		// Opening OF Queue and set the queue for the matched flow 
		String port = null;
		Iterator<OFPortDesc> it=mySwitch.getEnabledPorts().iterator();
		while(it.hasNext()) {
			if(it.next().getPortNo()==OFPort.of(portNumber)){
				port = it.next().getName();
			}
		}
		if(isNewID(datapathID.toString(), portNumber)) {
			String curl = "curl -d {\"switchid\":\""
					+datapathID
					+ "\",\"port\":\""
					+ port
					+ "\",\"rate\":20} http://127.0.0.1:8080/wm/queuepusher/qpc/json";	
			Runtime.getRuntime().exec(curl); 
		}
		ArrayList<OFAction> actionList = new ArrayList<OFAction>();
		OFActionEnqueue enqueue = myFactory.actions().buildEnqueue()
				.setPort(OFPort.of(portNumber)) // Must specify port number //
				.setQueueId(1)
				.build();

		
			OFActionOutput output = myFactory.actions().buildOutput().setPort(OFPort.of(portNumber)).build();

			if(isNewID(datapathID.toString(),portNumber)) {
				actionList.add(enqueue);
			}
			actionList.add(output);
			//Set the flow modification as a rule for the OpenVSwitches
			queueMap.put(datapathID.toString(), portNumber);
			OFFlowMod flowAdd = myFactory.buildFlowModify()
					.setIdleTimeout(120)
					.setHardTimeout(120)
					.setActions(actionList)
					.setMatch(m)
					.build();
			mySwitch.write(flowAdd);
			
	}
		
    
    
    private static String currentPath;

    public void doEverything() {
    	
        //Parses the path coming from TEM
		
		String[] paths = currentPath.split("A");
		
		System.out.println(currentPath);
		for(int i = 0; i<paths.length;i++) {
			
           // [PATH] HOST - SFU
            // [PATH] SFU  - HOST
            // [PATH] SFU  - GRE
            // [PATH] GRE  - SFU
			switch(i) {
			
				case 0: //HOST - SFU
					
					String[] s1 = paths[i].split("M");
					
					
					for(String datapathANDport : s1) {
						String[] s2 = datapathANDport.split("Z");
						System.out.println(datapathANDport);
						try {
							queueOpener(DatapathId.of(s2[0]), host1IP, sfuIP, Integer.parseInt(s2[1]));
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					
				case 1: // SFU  - HOST
					
					String[] s3 = paths[i].split("M");
					
					for(String datapathANDport : s3) {
						String[] s2 = datapathANDport.split("Z");

						System.out.println(datapathANDport);
						try {
							queueOpener(DatapathId.of(s2[0]), sfuIP, host1IP, Integer.parseInt(s2[1]));
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					
				case 2: //SFU  - GRE
					
					String[] s5 = paths[i].split("M");
					
					for(String datapathANDport : s5) {
						String[] s2 = datapathANDport.split("Z");

						System.out.println(datapathANDport);
						try {
							queueOpener(DatapathId.of(s2[0]), sfuIP, host2IP, Integer.parseInt(s2[1]));
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					
				case 3: //GRE  - SFU
					
					String[] s6 = paths[i].split("M");
					
					for(String datapathANDport : s6) {
						String[] s2 = datapathANDport.split("Z");

						System.out.println(datapathANDport);
						try {
							queueOpener(DatapathId.of(s2[0]), host2IP, sfuIP, Integer.parseInt(s2[1]));
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
			}
			
			
		}
    	
    	
    	
    }

	
	//Used to send POST request to TEM
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
	
	
	
	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		Collection<Class<? extends IFloodlightService>> l =
				new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IFloodlightService.class);
		// l.add(IOFSwitchService.class);
		return l;
	}


	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		// TODO Auto-generated method stub
		Collection<Class<? extends IFloodlightService>> l =
				new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IFloodlightProviderService.class);
		l.add(IThreadPoolService.class);
		l.add(IOFSwitchService.class);
		l.add(IRoutingService.class);
		l.add(IRestApiService.class);
		return l;
	}

	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
		// TODO Auto-generated method stub
		floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
		restApiService = context.getServiceImpl(IRestApiService.class);
		threadPoolService = context.getServiceImpl(IThreadPoolService.class);
		logger = LoggerFactory.getLogger(SDNslicing.class);
		switchService = context.getServiceImpl(IOFSwitchService.class);
		routingEngine = context.getServiceImpl(IRoutingService.class);
		messageDamper = new OFMessageDamper(10000,
				EnumSet.of(OFType.FLOW_MOD),
				250);
		done = false;

		
		}

	@Override
	public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
		// TODO Auto-generated method stub
		ScheduledExecutorService ses = threadPoolService.getScheduledExecutor();
		restApiService.addRestletRoutable(new StaticEntryWebRoutable());
	}


	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addRestletRoutable(RestletRoutable routable) {
		// TODO Auto-generated method stub

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}


	

	@Post
	public String store(String fmJson) throws IOException{
        
        //This function is called when TEM sends the path.
        //Sets the currentPath to incoming request's body and starts opening queues
        
		currentPath = fmJson;
		
		doEverything();
		
		return fmJson;

	}



}

