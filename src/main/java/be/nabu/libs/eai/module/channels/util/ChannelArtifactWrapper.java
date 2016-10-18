package be.nabu.libs.eai.module.channels.util;

import be.nabu.libs.channels.api.Channel;
import be.nabu.libs.channels.api.ChannelManager;
import be.nabu.libs.channels.api.ChannelProvider;
import be.nabu.libs.datatransactions.api.Direction;
import be.nabu.libs.datatransactions.api.Transactionality;
import be.nabu.libs.eai.module.channels.ChannelArtifact;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.java.BeanInstance;
import be.nabu.libs.types.java.BeanResolver;

public class ChannelArtifactWrapper implements Channel<Object> {

	private ChannelArtifact artifact;
	private ChannelManager manager;

	public ChannelArtifactWrapper(ChannelArtifact artifact, ChannelManager manager) {
		this.artifact = artifact;
		this.manager = manager;
	}
	
	@Override
	public String getContext() {
		return artifact.getConfig().getContext() == null ? artifact.getId() : artifact.getConfig().getContext();
	}

	@Override
	public Direction getDirection() {
		return artifact.getConfig().getDirection() == null ? Direction.IN : artifact.getConfig().getDirection();
	}

	@Override
	public String getProviderId() {
		return artifact.getConfig().getProviderId();
	}

	@Override
	public Object getProperties() {
		String providerId = artifact.getConfig().getProviderId();
		ComplexType type = null;
		boolean asBean = false;
		ChannelProvider<?> channelProvider = manager.getProviders().get(providerId);
		if (channelProvider instanceof ChannelServiceProvider) {
			type = ((ChannelServiceProvider) channelProvider).getPropertyType();
		}
		else {
			type = (ComplexType) BeanResolver.getInstance().resolve(channelProvider.getPropertyClass());
			asBean = true;
		}
		if (type == null) {
			return null;
		}
		ComplexContent content = type.newInstance();
		if (artifact.getConfig().getProperties() != null) {
			for (String key : artifact.getConfig().getProperties().keySet()) {
				content.set(key, artifact.getConfig().getProperties().get(key));
			}
		}
		return asBean ? ((BeanInstance<?>) content).getUnwrapped() : content;
	}

	@Override
	public Transactionality getTransactionality() {
		return artifact.getConfig().getTransactionality() == null ? Transactionality.THREE_PHASE : artifact.getConfig().getTransactionality();
	}

	@Override
	public int getPriority() {
		return artifact.getConfig().getPriority();
	}

	@Override
	public int getFinishAmount() {
		return artifact.getConfig().getFinishAmount();
	}

	@Override
	public boolean isContinueOnFailure() {
		return artifact.getConfig().isContinueOnFailure();
	}

	@Override
	public int getRetryAmount() {
		return artifact.getConfig().getRetryAmount();
	}

	@Override
	public long getRetryInterval() {
		return artifact.getConfig().getRetryInterval();
	}

}
