package be.nabu.libs.eai.module.channels.util;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import be.nabu.eai.repository.EAIRepositoryUtils;
import be.nabu.libs.channels.api.ChannelException;
import be.nabu.libs.channels.api.ChannelProvider;
import be.nabu.libs.datastore.api.WritableDatastore;
import be.nabu.libs.datatransactions.api.DataTransactionBatch;
import be.nabu.libs.datatransactions.api.DataTransactionHandle;
import be.nabu.libs.datatransactions.api.Direction;
import be.nabu.libs.eai.module.channels.api.ServiceChannelProvider;
import be.nabu.libs.services.api.Service;
import be.nabu.libs.services.pojo.MethodServiceInterface;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.Element;

public class ChannelServiceProvider implements ChannelProvider<Object> {

	private ServiceChannelProvider provider;
	private Direction direction;
	private Service service;

	public ChannelServiceProvider(ServiceChannelProvider provider, Direction direction, Service service) {
		this.provider = provider;
		this.direction = direction;
		this.service = service;
	}
	
	@Override
	public void transact(Object properties, WritableDatastore datastore, DataTransactionBatch<ChannelProvider<?>> transactionBatch, URI...requests) throws ChannelException {
		try {
			DataTransactionHandle handle = transactionBatch.start(this, properties, null);
			try {
				switch(direction) {
					case IN:
						handle.commit(provider.transactIn(properties));
					break;
					case OUT:
						provider.transactOut(properties, Arrays.asList(requests));
						handle.commit(null);
					break;
					case BOTH:
						handle.commit(provider.transactInOut(properties, Arrays.asList(requests)));
					break;
				}
			}
			catch (Exception e) {
				handle.fail("Service execution failed: " + e.getMessage());
			}
		}
		catch (IOException e) {
			throw new ChannelException(e);
		}
	}

	@Override
	public Direction getDirection() {
		return direction;
	}

	@Override
	public Class<Object> getPropertyClass() {
		return Object.class;
	}

	public Service getService() {
		return service;
	}
	
	public ComplexType getPropertyType() {
		MethodServiceInterface transactIn = MethodServiceInterface.wrap(ServiceChannelProvider.class, "transact");
		MethodServiceInterface transactOut = MethodServiceInterface.wrap(ServiceChannelProvider.class, "transactOut");
		MethodServiceInterface transactInOut = MethodServiceInterface.wrap(ServiceChannelProvider.class, "transactInOut");
		List<Element<?>> inputExtensions;
		switch(direction) {
			case IN:
				inputExtensions = EAIRepositoryUtils.getInputExtensions(service, transactIn);
			break;
			case OUT:
				inputExtensions = EAIRepositoryUtils.getInputExtensions(service, transactOut);
			break;
			case BOTH:
				inputExtensions = EAIRepositoryUtils.getInputExtensions(service, transactInOut);
			break;
			default: throw new RuntimeException("Unsupported direction");
		}
		for (Element<?> additional : inputExtensions) {
			if (additional.getType() instanceof ComplexType) {
				return (ComplexType) additional.getType();
			}
		}
		return null;
	}

}
