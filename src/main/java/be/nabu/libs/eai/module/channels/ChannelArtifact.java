package be.nabu.libs.eai.module.channels;

import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.artifacts.jaxb.JAXBArtifact;
import be.nabu.libs.resources.api.ResourceContainer;

public class ChannelArtifact extends JAXBArtifact<ChannelConfiguration> {

	public ChannelArtifact(String id, ResourceContainer<?> directory, Repository repository) {
		super(id, directory, repository, "channel.xml", ChannelConfiguration.class);
	}
	
}
