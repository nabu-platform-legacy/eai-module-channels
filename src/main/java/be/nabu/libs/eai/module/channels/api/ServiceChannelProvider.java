/*
* Copyright (C) 2016 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

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
