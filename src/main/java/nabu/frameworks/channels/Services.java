package nabu.frameworks.channels;

import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.validation.constraints.NotNull;

import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.libs.channels.api.ChannelException;
import be.nabu.libs.channels.api.ChannelOrchestrator;
import be.nabu.libs.channels.api.ChannelRecoverySelector;
import be.nabu.libs.channels.util.ChannelOrchestratorImpl;
import be.nabu.libs.datastore.api.ContextualWritableDatastore;
import be.nabu.libs.datatransactions.api.Direction;
import be.nabu.libs.eai.module.channels.util.ChannelManagerImpl;
import be.nabu.libs.eai.module.data.transactions.DataTransactionArtifact;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.services.api.ExecutionContext;
import be.nabu.libs.services.pojo.MethodServiceInterface;
import be.nabu.libs.services.pojo.POJOUtils;

@WebService
public class Services {
	
	private ExecutionContext context;
	
	public void transact(@NotNull @WebParam(name = "context") String channelContext, @NotNull @WebParam(name = "dataTransactionProviderId") String dataTransactionProviderId, @NotNull @WebParam(name = "resultHandlerId") String resultHandlerId, @WebParam(name = "direction") Direction direction, @WebParam(name = "requests") List<URI> requests) throws ChannelException {
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
		orchestrator.transact(instance, channelContext, direction, instance.getResultHandlerResolver().getProvider(resultHandlerId), requests.toArray(new URI[requests.size()]));
	}
	
	public void recover(@NotNull @WebParam(name = "dataTransactionProviderId") String dataTransactionProviderId, @NotNull @WebParam(name = "since") Date since, @NotNull @WebParam(name = "channelRecoverySelectorId") String channelRecoverySelectorId) throws IOException {
		ContextualWritableDatastore<String> datastore = nabu.frameworks.datastore.Services.getAsDatastore(this.context);
		DataTransactionArtifact transactionProvider = this.context.getServiceContext().getResolver(DataTransactionArtifact.class).resolve(dataTransactionProviderId);
		if (transactionProvider == null) {
			throw new IllegalArgumentException("Can not find transactionProvider: " + dataTransactionProviderId);
		}
		DefinedService channelRecoveryService = this.context.getServiceContext().getResolver(DefinedService.class).resolve(channelRecoverySelectorId);
		if (channelRecoveryService == null) {
			throw new IllegalArgumentException("Can not find channel recovery selector: " + channelRecoverySelectorId);
		}
		if (!POJOUtils.isImplementation(channelRecoveryService, MethodServiceInterface.wrap(ChannelRecoverySelector.class, "recover"))) {
			throw new IllegalArgumentException("Service is not a channel recovery selector: " + channelRecoverySelectorId);
		}
		ChannelOrchestrator orchestrator = new ChannelOrchestratorImpl(datastore, transactionProvider.getProvider(), EAIResourceRepository.getInstance().getName());
		ChannelManagerImpl instance = ChannelManagerImpl.getInstance();
		orchestrator.recover(instance.getProviderResolver(), instance.getResultHandlerResolver(), since, POJOUtils.newProxy(ChannelRecoverySelector.class, channelRecoveryService, this.context));
	}
}
