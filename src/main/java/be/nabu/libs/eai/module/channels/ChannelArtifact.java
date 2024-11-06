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

package be.nabu.libs.eai.module.channels;

import java.io.IOException;

import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.artifacts.jaxb.JAXBArtifact;
import be.nabu.libs.eai.module.channels.util.ChannelManagerImpl;
import be.nabu.libs.resources.api.ResourceContainer;

/**
 * There are some problems with the result handler:
 * - we could dynamically give the result handler when calling "transact" on one or more channels, but this result handler is not logged anywhere which makes automatic recovery impossible
 * - we could add the result handler to the channel, but this limits the amount of channels to be run at the same time (in the same batch) to 1, otherwise you might have multiple result handlers
 * - we could keep an external configuration of "context > result handler" but here is the chance of odd behavior (more specific handler is overridden by more generic handler if generic context is used)
 * - In theory the result handler could perform complex evaluation of properties and channel metadata to choose a provider, we could have a flow service for this but again it would have to be registered or be one central one
 * 
 * At a conceptual level the channel is responsible for getting data into the system and is in no way linked to what you do to that data afterwards, this is why there is a disconnect in the library
 * However in reality, it is easier to configure the handler in the channel itself, it would also be odd to call the same channel with two different handlers assuming the channel is generally scheduled
 * This would create odd race conditions.
 * 
 * On the other hand it could be interesting (mostly for manually run channels) to be able to switch out the handler.
 */
public class ChannelArtifact extends JAXBArtifact<ChannelConfiguration> {

	public ChannelArtifact(String id, ResourceContainer<?> directory, Repository repository) {
		super(id, directory, repository, "channel.xml", ChannelConfiguration.class);
	}

	@Override
	public void save(ResourceContainer<?> directory) throws IOException {
		super.save(directory);
		ChannelManagerImpl.getInstance().reload();
	}
	
}
