package be.nabu.libs.eai.module.channels.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.util.SystemPrincipal;
import be.nabu.libs.channels.ChannelUtils;
import be.nabu.libs.channels.api.Channel;
import be.nabu.libs.channels.api.ChannelManager;
import be.nabu.libs.channels.api.ChannelProvider;
import be.nabu.libs.channels.api.ChannelResultHandler;
import be.nabu.libs.channels.resources.DirectoryInProvider;
import be.nabu.libs.channels.resources.FileInProvider;
import be.nabu.libs.channels.resources.FileOutProvider;
import be.nabu.libs.datatransactions.api.Direction;
import be.nabu.libs.datatransactions.api.ProviderResolver;
import be.nabu.libs.eai.module.channels.ChannelArtifact;
import be.nabu.libs.eai.module.channels.api.ServiceChannelProvider;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.services.pojo.MethodServiceInterface;
import be.nabu.libs.services.pojo.POJOUtils;

public class ChannelManagerImpl implements ChannelManager {

	private static ChannelManagerImpl instance = new ChannelManagerImpl();
	
	public static ChannelManagerImpl getInstance() {
		return instance;
	}
	
	private Map<String, ChannelProvider<?>> providers;

	private List<Channel<?>> channels;
	
	public Map<String, ChannelProvider<?>> getProviders() {
		if (providers == null || EAIResourceRepository.isDevelopment()) {
			synchronized(this) {
				if (providers == null || EAIResourceRepository.isDevelopment()) {
					Map<String, ChannelProvider<?>> providers = new HashMap<String, ChannelProvider<?>>();
					providers.put("file+dir.in", new DirectoryInProvider());
					providers.put("file.in", new FileInProvider());
					providers.put("file.out", new FileOutProvider());
					MethodServiceInterface transactIn = MethodServiceInterface.wrap(ServiceChannelProvider.class, "transactIn");
					MethodServiceInterface transactOut = MethodServiceInterface.wrap(ServiceChannelProvider.class, "transactOut");
					MethodServiceInterface transactInOut = MethodServiceInterface.wrap(ServiceChannelProvider.class, "transactInOut");
					for (DefinedService service : EAIResourceRepository.getInstance().getArtifacts(DefinedService.class)) {
						if (POJOUtils.isImplementation(service, transactIn)) {
							providers.put(service.getId(), new ChannelServiceProvider(POJOUtils.newProxy(ServiceChannelProvider.class, EAIResourceRepository.getInstance(), SystemPrincipal.ROOT, service), Direction.IN, service));
						}
						else if (POJOUtils.isImplementation(service, transactOut)) {
							providers.put(service.getId(), new ChannelServiceProvider(POJOUtils.newProxy(ServiceChannelProvider.class, EAIResourceRepository.getInstance(), SystemPrincipal.ROOT, service), Direction.OUT, service));
						}
						else if (POJOUtils.isImplementation(service, transactInOut)) {
							providers.put(service.getId(), new ChannelServiceProvider(POJOUtils.newProxy(ServiceChannelProvider.class, EAIResourceRepository.getInstance(), SystemPrincipal.ROOT, service), Direction.BOTH, service));
						}
					}
					this.providers = providers;
				}
			}
		}
		return providers;
	}

	@Override
	public List<? extends Channel<?>> getChannels() {
		if (channels == null || EAIResourceRepository.isDevelopment()) {
			synchronized(this) {
				if (channels == null || EAIResourceRepository.isDevelopment()) {
					List<Channel<?>> channels = new ArrayList<Channel<?>>();
					for (ChannelArtifact artifact : EAIResourceRepository.getInstance().getArtifacts(ChannelArtifact.class)) {
						channels.add(new ChannelArtifactWrapper(artifact, this));
					}
					this.channels = channels;
				}
			}
		}
		return channels;
	}

	public void reload() {
		channels = null;
		providers = null;
	}


	@Override
	public ProviderResolver<ChannelProvider<?>> getProviderResolver() {
		return new ProviderResolver<ChannelProvider<?>>() {
			@Override
			public String getId(ChannelProvider<?> provider) {
				for (String id : getProviders().keySet()) {
					if (getProviders().get(id).equals(provider)) {
						return id;
					}
				}
				throw new IllegalArgumentException("Can not find the provider " + provider);
			}

			@Override
			public ChannelProvider<?> getProvider(String id) {
				return getProviders().get(id);
			}
		};
	}

	@Override
	public ProviderResolver<ChannelResultHandler> getResultHandlerResolver() {
		return ChannelUtils.newChannelResultHandlerResolver();
	}
}
