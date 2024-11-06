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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.util.SystemPrincipal;
import be.nabu.libs.channels.ChannelUtils;
import be.nabu.libs.channels.api.Channel;
import be.nabu.libs.channels.api.ChannelException;
import be.nabu.libs.channels.api.ChannelManager;
import be.nabu.libs.channels.api.ChannelProvider;
import be.nabu.libs.channels.api.ChannelResultHandler;
import be.nabu.libs.channels.api.SingleChannelResultHandler;
import be.nabu.libs.channels.resources.DirectoryInProvider;
import be.nabu.libs.channels.resources.FileInProvider;
import be.nabu.libs.channels.resources.FileOutProvider;
import be.nabu.libs.channels.util.SimpleChannelResultHandler;
import be.nabu.libs.datatransactions.api.DataTransaction;
import be.nabu.libs.datatransactions.api.DataTransactionHandle;
import be.nabu.libs.datatransactions.api.Direction;
import be.nabu.libs.datatransactions.api.ProviderResolver;
import be.nabu.libs.eai.module.channels.ChannelArtifact;
import be.nabu.libs.eai.module.channels.api.ServiceChannelProvider;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.services.api.Service;
import be.nabu.libs.services.pojo.MethodServiceInterface;
import be.nabu.libs.services.pojo.POJOUtils;
import be.nabu.libs.services.pojo.POJOUtils.ServiceInvocationHandler;

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
							providers.put(service.getId(), new ChannelServiceProvider(Direction.IN, service));
						}
						else if (POJOUtils.isImplementation(service, transactOut)) {
							providers.put(service.getId(), new ChannelServiceProvider(Direction.OUT, service));
						}
						else if (POJOUtils.isImplementation(service, transactInOut)) {
							providers.put(service.getId(), new ChannelServiceProvider(Direction.BOTH, service));
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
				if (provider instanceof ChannelServiceProvider) {
					return ((ChannelServiceProvider) provider).getService().getId();
				}
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
		ProviderResolver<ChannelResultHandler> defaultResolver = ChannelUtils.newChannelResultHandlerResolver();
		return new ProviderResolver<ChannelResultHandler>() {
			@Override
			public String getId(ChannelResultHandler provider) {
				// if there is no provider, return null
				if (provider == null) {
					return null;
				}
				Object providerToCheck = provider;
				if (provider instanceof SimpleChannelResultHandler) {
					providerToCheck = ((SimpleChannelResultHandler) provider).getHandler();
				}
				if (Proxy.isProxyClass(providerToCheck.getClass())) {
					InvocationHandler invocationHandler = Proxy.getInvocationHandler(providerToCheck);
					if (invocationHandler instanceof ServiceInvocationHandler) {
						Service[] services = ((ServiceInvocationHandler<?>) invocationHandler).getServices();
						return ((DefinedService) services[0]).getId();
					}
					else {
						throw new IllegalArgumentException("The proxy can not be reslved");
					}
				}
				else {
					return defaultResolver.getId(provider);
				}
			}
			@Override
			public ChannelResultHandler getProvider(String id) {
				// if there is no id, return a default provider that does nothing
				if (id == null) {
					return new SimpleChannelResultHandler(new SingleChannelResultHandler() {
						@Override
						public void handle(DataTransaction<?> transaction) throws ChannelException {
							// do nothing
						}
					});
				}
				DefinedService handler = (DefinedService) EAIResourceRepository.getInstance().resolve(id);
				if (handler == null) {
					return defaultResolver.getProvider(id);
				}
				ChannelResultHandler channelResultHandler;
				if (POJOUtils.isImplementation(handler, MethodServiceInterface.wrap(ChannelResultHandler.class, "handle"))) {
					channelResultHandler = POJOUtils.newProxy(ChannelResultHandler.class, EAIResourceRepository.getInstance(), SystemPrincipal.ROOT, handler);
				}
				else if (POJOUtils.isImplementation(handler, MethodServiceInterface.wrap(SingleChannelResultHandler.class, "handle"))) {
					channelResultHandler = ChannelUtils.newChannelResultHandler(POJOUtils.newProxy(SingleChannelResultHandler.class, EAIResourceRepository.getInstance(), SystemPrincipal.ROOT, handler));
				}
				else {
					throw new IllegalArgumentException("The handler service does not implement batch or single handling interfaces");
				}
				return channelResultHandler;
			}
		};
	}
}
