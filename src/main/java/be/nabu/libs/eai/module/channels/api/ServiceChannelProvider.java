package be.nabu.libs.eai.module.channels.api;

import java.net.URI;
import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebResult;

// need to fix this, the "properties" are undefined objects here but they need to be defined for the service input
public interface ServiceChannelProvider {
	@WebResult(name = "result")
	public URI transactIn();
	
	public void transactOut(@WebParam(name = "requests") List<URI> requests);
	
	@WebResult(name = "result")
	public URI transactInOut(@WebParam(name = "requests") List<URI> requests);
}
