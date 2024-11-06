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
		ChannelProvider<?> provider = ChannelManagerImpl.getInstance().getProviderResolver().getProvider(artifact.getConfig().getProviderId());
		return provider == null ? null : provider.getDirection();
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
		ChannelProvider<?> channelProvider = manager.getProviderResolver().getProvider(providerId);
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
