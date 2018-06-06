package net.floodlightcontroller.ssra;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.restserver.RestletRoutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SignalingServerRESTapi extends ServerResource implements IFloodlightModule, IRestApiService {

	
	
	protected IFloodlightProviderService floodlightProvider;
	protected Set<Long> macAddresses;
	protected static Logger logger;
	protected IRestApiService restApiService;
	
	public static ArrayList<String> connectedPeers = new ArrayList<String>();

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		// TODO Auto-generated method stub
		Collection<Class<? extends IFloodlightService>> l =
		        new ArrayList<Class<? extends IFloodlightService>>();
		    l.add(IFloodlightProviderService.class);
		    l.add(IRestApiService.class);
		    return l;
	}

	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
		// TODO Auto-generated method stub
		   floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
		    macAddresses = new ConcurrentSkipListSet<Long>();
		    logger = LoggerFactory.getLogger(SignalingServerRESTapi.class);
		    System.out.println("SIGNALING SERVER STARTED");
		    
		    
		    restApiService = context.getServiceImpl(IRestApiService.class);
		    
		    
	}

	@Override
	public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
		// TODO Auto-generated method stub
		restApiService.addRestletRoutable(new StaticEntryWebRoutable());
	}



	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

	
	//ONLY IPV4 ADDRESSES SHOULD BE SENT TO THIS ROUTE !!!
	@Post
	public String store(String fmJson) throws IOException{
		

		System.out.println(" POST REQUEST ARRIVED ");
		/*
	    IStorageSourceService storageSource =
	        (IStorageSourceService)getContext().getAttributes().
	            get(IStorageSourceService.class.getCanonicalName());
	    Map<String, Object> rowValues;
	    try {
	        rowValues = StaticEntries.jsonToStorageEntry(fmJson);
	        
	    }catch(IOException e){
	    	throw e;
	    }
	    
	    System.out.println(rowValues);
		return fmJson;*/
		
		//Print out the incoming session description
		System.out.println(fmJson);
		
		//Add the new peer to connected peer data structure
		connectedPeers.add(fmJson);
		return "";
	    
	}
	
	
	
	@Override
	public void addRestletRoutable(RestletRoutable routable) {
		// TODO Auto-generated method stub
		
	}
	
	
}