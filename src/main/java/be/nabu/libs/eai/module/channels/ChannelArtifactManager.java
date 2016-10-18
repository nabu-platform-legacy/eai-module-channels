package be.nabu.libs.eai.module.channels;

import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.managers.base.JAXBArtifactManager;
import be.nabu.libs.resources.api.ResourceContainer;

public class ChannelArtifactManager extends JAXBArtifactManager<ChannelConfiguration, ChannelArtifact> {

	public ChannelArtifactManager() {
		super(ChannelArtifact.class);
	}

	@Override
	protected ChannelArtifact newInstance(String id, ResourceContainer<?> container, Repository repository) {
		return new ChannelArtifact(id, container, repository);
	}

}
