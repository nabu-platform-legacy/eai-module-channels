package be.nabu.libs.eai.module.channels.api;

import java.net.URI;
import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebResult;

public interface ServiceChannelProvider {
	@WebResult(name = "result")
	public URI transactIn(@WebParam(name = "properties") Object properties);
	
	public void transactOut(@WebParam(name = "properties") Object properties, @WebParam(name = "requests") List<URI> requests);
	
	@WebResult(name = "result")
	public URI transactInOut(@WebParam(name = "properties") Object properties, @WebParam(name = "requests") List<URI> requests);
}
