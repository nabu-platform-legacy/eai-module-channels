package be.nabu.libs.eai.module.channels.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import be.nabu.eai.repository.EAIRepositoryUtils;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.util.SystemPrincipal;
import be.nabu.libs.channels.api.ChannelException;
import be.nabu.libs.channels.api.ChannelProvider;
import be.nabu.libs.datastore.api.WritableDatastore;
import be.nabu.libs.datatransactions.api.DataTransactionBatch;
import be.nabu.libs.datatransactions.api.DataTransactionHandle;
import be.nabu.libs.datatransactions.api.Direction;
import be.nabu.libs.eai.module.channels.api.ServiceChannelProvider;
import be.nabu.libs.services.ServiceRuntime;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.services.pojo.MethodServiceInterface;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.Element;

public class ChannelServiceProvider implements ChannelProvider<Object> {

	private Direction direction;
	private DefinedService service;

	public ChannelServiceProvider(Direction direction, DefinedService service) {
		this.direction = direction;
		this.service = service;
	}
	
	@Override
	public void transact(Object properties, WritableDatastore datastore, DataTransactionBatch<ChannelProvider<?>> transactionBatch, URI...requests) throws ChannelException {
		try {
			ComplexContent newInstance = service.getServiceInterface().getInputDefinition().newInstance();
			Element<?> propertyElement = getPropertyElement();
			if (propertyElement != null) {
				newInstance.set(propertyElement.getName(), properties);
			}
			DataTransactionHandle handle = transactionBatch.start(this, properties, null);
			ComplexContent output;
			ServiceRuntime serviceRuntime;
			try {
				switch(direction) {
					case IN:
						serviceRuntime = new ServiceRuntime(service, EAIResourceRepository.getInstance().newExecutionContext(SystemPrincipal.ROOT));
						output = serviceRuntime.run(newInstance);
						handle.commit(output == null ? null : (URI) output.get("result"));
					break;
					case OUT:
						newInstance.set("requests", Arrays.asList(requests));
						serviceRuntime = new ServiceRuntime(service, EAIResourceRepository.getInstance().newExecutionContext(SystemPrincipal.ROOT));
						serviceRuntime.run(newInstance);
						handle.commit(null);
					break;
					case BOTH:
						newInstance.set("requests", Arrays.asList(requests));
						serviceRuntime = new ServiceRuntime(service, EAIResourceRepository.getInstance().newExecutionContext(SystemPrincipal.ROOT));
						output = serviceRuntime.run(newInstance);
						handle.commit(output == null ? null : (URI) output.get("result"));
					break;
				}
			}
			catch (Exception e) {
				StringWriter writer = new StringWriter();
				PrintWriter printer = new PrintWriter(writer);
				e.printStackTrace(printer);
				printer.flush();
				handle.fail(writer.toString());
				throw e instanceof ChannelException ? (ChannelException) e : new ChannelException(e);
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

	public DefinedService getService() {
		return service;
	}
	
	public ComplexType getPropertyType() {
		Element<?> propertyElement = getPropertyElement();
		return propertyElement == null ? null : (ComplexType) propertyElement.getType();
	}
	public Element<?> getPropertyElement() {
		MethodServiceInterface transactIn = MethodServiceInterface.wrap(ServiceChannelProvider.class, "transactIn");
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
				return additional;
			}
		}
		return null;
	}

}
