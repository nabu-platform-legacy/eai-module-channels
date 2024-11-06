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

package nabu.frameworks.channels;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.validation.constraints.NotNull;

import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.Notification;
import be.nabu.libs.channels.api.ChannelException;
import be.nabu.libs.channels.api.ChannelOrchestrator;
import be.nabu.libs.channels.api.ChannelRecoverySelector;
import be.nabu.libs.channels.util.ChannelOrchestratorImpl;
import be.nabu.libs.datastore.api.ContextualWritableDatastore;
import be.nabu.libs.datatransactions.api.DataTransaction;
import be.nabu.libs.datatransactions.api.Direction;
import be.nabu.libs.datatransactions.api.Transactionality;
import be.nabu.libs.eai.module.channels.util.ChannelManagerImpl;
import be.nabu.libs.eai.module.data.transactions.DataTransactionArtifact;
import be.nabu.libs.services.ServiceRuntime;
import be.nabu.libs.services.ServiceUtils;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.services.api.ExecutionContext;
import be.nabu.libs.services.pojo.MethodServiceInterface;
import be.nabu.libs.services.pojo.POJOUtils;
import be.nabu.libs.validator.api.ValidationMessage.Severity;

@WebService
public class Services {
	
	private ExecutionContext context;

	public void run(
				@WebParam(name = "context") String context, 
				@NotNull @WebParam(name = "channelProviderId") String providerId, 
				@NotNull @WebParam(name = "dataTransactionProviderId") String dataTransactionProviderId, 
				@WebParam(name = "resultHandlerId") String resultHandlerId, 
				@WebParam(name = "channelProperties") Object properties, 
				@WebParam(name = "transactionality") Transactionality transactionality,
				@WebParam(name = "finishAmount") Integer finishAmount,
				@WebParam(name = "retryAmount") Integer retryAmount,
				@WebParam(name = "retryInterval") Long retryInterval,
				@WebParam(name = "requests") List<URI> requests) throws ChannelException {
		if (context == null) {
			context = ServiceUtils.getServiceContext(ServiceRuntime.getRuntime());
		}
		ContextualWritableDatastore<String> datastore = nabu.frameworks.datastore.Services.getAsDatastore(this.context);
		DataTransactionArtifact transactionProvider = this.context.getServiceContext().getResolver(DataTransactionArtifact.class).resolve(dataTransactionProviderId);
		if (transactionProvider == null) {
			throw new IllegalArgumentException("Can not find transactionProvider: " + dataTransactionProviderId);
		}
		ChannelManagerImpl instance = ChannelManagerImpl.getInstance();
		ChannelOrchestratorImpl orchestrator = new ChannelOrchestratorImpl(datastore, transactionProvider.getProvider(), EAIResourceRepository.getInstance().getName());
		ChannelException exception = orchestrator.transact(
			instance, 
			providerId, 
			properties, 
			context, 
			resultHandlerId == null ? null : instance.getResultHandlerResolver().getProvider(resultHandlerId), 
			transactionality, 
			finishAmount, 
			retryAmount == null ? 0 : retryAmount, 
			retryInterval == null ? 10000 : retryInterval, 
			requests == null ? new URI[0] : requests.toArray(new URI[0])
		);
		if (exception != null) {
			throw exception;
		}
	}
	
	public void transact(@NotNull @WebParam(name = "context") String channelContext, @NotNull @WebParam(name = "dataTransactionProviderId") String dataTransactionProviderId, @NotNull @WebParam(name = "resultHandlerId") String resultHandlerId, @WebParam(name = "direction") Direction direction, @WebParam(name = "requests") List<URI> requests) throws ChannelException {
		try {
			if (direction == null) {
				direction = Direction.IN;
			}
			ContextualWritableDatastore<String> datastore = nabu.frameworks.datastore.Services.getAsDatastore(this.context);
			DataTransactionArtifact transactionProvider = this.context.getServiceContext().getResolver(DataTransactionArtifact.class).resolve(dataTransactionProviderId);
			if (transactionProvider == null) {
				throw new IllegalArgumentException("Can not find transactionProvider: " + dataTransactionProviderId);
			}
			ChannelManagerImpl instance = ChannelManagerImpl.getInstance();
			ChannelOrchestrator orchestrator = new ChannelOrchestratorImpl(datastore, transactionProvider.getProvider(), EAIResourceRepository.getInstance().getName());
			if (requests == null) {
				orchestrator.transact(instance, channelContext, direction, instance.getResultHandlerResolver().getProvider(resultHandlerId));
			}
			else {
				orchestrator.transact(instance, channelContext, direction, instance.getResultHandlerResolver().getProvider(resultHandlerId), requests.toArray(new URI[requests.size()]));
			}
		}
		catch (Exception e) {
			Notification notification = new Notification();
			notification.setCode("CHANNELS-0");
			notification.setContext(Arrays.asList(channelContext, dataTransactionProviderId));
			notification.setMessage("Could not transact channel(s)");
			notification.setDescription(Notification.format(e));
			notification.setSeverity(Severity.ERROR);
			notification.setType("nabu.frameworks.channels.transact");
			EAIResourceRepository.getInstance().getEventDispatcher().fire(notification, this);
			if (e instanceof ChannelException) {
				throw (ChannelException) e;
			}
			else if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			else {
				throw new RuntimeException(e);
			}
		}
	}
	
	public void recover(@NotNull @WebParam(name = "dataTransactionProviderId") String dataTransactionProviderId, @NotNull @WebParam(name = "since") Date since, @WebParam(name = "channelRecoverySelectorId") String channelRecoverySelectorId) throws IOException, ChannelException {
		try {
			ContextualWritableDatastore<String> datastore = nabu.frameworks.datastore.Services.getAsDatastore(this.context);
			DataTransactionArtifact transactionProvider = this.context.getServiceContext().getResolver(DataTransactionArtifact.class).resolve(dataTransactionProviderId);
			if (transactionProvider == null) {
				throw new IllegalArgumentException("Can not find transactionProvider: " + dataTransactionProviderId);
			}
			ChannelRecoverySelector channelRecoverySelector;
			if (channelRecoverySelectorId != null) {
				DefinedService channelRecoveryService = this.context.getServiceContext().getResolver(DefinedService.class).resolve(channelRecoverySelectorId);
				if (channelRecoveryService == null) {
					throw new IllegalArgumentException("Can not find channel recovery selector: " + channelRecoverySelectorId);
				}
				if (!POJOUtils.isImplementation(channelRecoveryService, MethodServiceInterface.wrap(ChannelRecoverySelector.class, "recover"))) {
					throw new IllegalArgumentException("Service is not a channel recovery selector: " + channelRecoverySelectorId);
				}
				channelRecoverySelector = POJOUtils.newProxy(ChannelRecoverySelector.class, channelRecoveryService, this.context);
			}
			else {
				channelRecoverySelector = new ChannelRecoverySelector() {
					@Override
					public boolean recover(DataTransaction<?> transaction) {
						return true;
					}
				};
			}
			ChannelOrchestrator orchestrator = new ChannelOrchestratorImpl(datastore, transactionProvider.getProvider(), EAIResourceRepository.getInstance().getName());
			ChannelManagerImpl instance = ChannelManagerImpl.getInstance();
			orchestrator.recover(instance.getProviderResolver(), instance.getResultHandlerResolver(), since, channelRecoverySelector);
		}
		catch (Exception e) {
			Notification notification = new Notification();
			notification.setCode("CHANNELS-1");
			notification.setContext(Arrays.asList(dataTransactionProviderId));
			notification.setType("nabu.frameworks.channels.recover");
			notification.setMessage("Could not recover channel transaction(s)");
			notification.setDescription(Notification.format(e));
			notification.setSeverity(Severity.ERROR);
			EAIResourceRepository.getInstance().getEventDispatcher().fire(notification, this);
			if (e instanceof IOException) {
				throw (IOException) e;
			}
			else if (e instanceof ChannelException) {
				throw (ChannelException) e;
			}
			else if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			else {
				throw new RuntimeException(e);
			}
		}
	}
}
