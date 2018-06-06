package net.floodlightcontroller.ssra;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import net.floodlightcontroller.restserver.RestletRoutable;

public class StaticEntryWebRoutable implements RestletRoutable  {

	@Override
	public Restlet getRestlet(Context context) {
		// TODO Auto-generated method stub
		Router router = new Router(context);
        router.attach("/desc", SignalingServerRESTapi.class);
        return router;
	}

	@Override
	public String basePath() {
		// TODO Auto-generated method stub
		 return "/wm/session";
	}

}