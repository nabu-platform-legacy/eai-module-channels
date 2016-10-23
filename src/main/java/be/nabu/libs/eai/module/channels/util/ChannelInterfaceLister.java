package be.nabu.libs.eai.module.channels.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import be.nabu.eai.developer.api.InterfaceLister;
import be.nabu.eai.developer.util.InterfaceDescriptionImpl;

public class ChannelInterfaceLister implements InterfaceLister {
	
	private static Collection<InterfaceDescription> descriptions = null;
	
	@Override
	public Collection<InterfaceDescription> getInterfaces() {
		if (descriptions == null) {
			synchronized(ChannelInterfaceLister.class) {
				if (descriptions == null) {
					List<InterfaceDescription> descriptions = new ArrayList<InterfaceDescription>();
					descriptions.add(new InterfaceDescriptionImpl("Channels", "Single Handler", "be.nabu.libs.channels.api.SingleChannelResultHandler.handle"));
					descriptions.add(new InterfaceDescriptionImpl("Channels", "Batch Handler", "be.nabu.libs.channels.api.ChannelResultHandler.handle"));
					descriptions.add(new InterfaceDescriptionImpl("Channels", "Recovery Selector", "be.nabu.libs.channels.api.ChannelRecoverySelector.recover"));
					descriptions.add(new InterfaceDescriptionImpl("Channels", "Channel In Provider", "be.nabu.libs.eai.module.channels.api.ServiceChannelProvider.transactIn"));
					descriptions.add(new InterfaceDescriptionImpl("Channels", "Channel Out Provider", "be.nabu.libs.eai.module.channels.api.ServiceChannelProvider.transactOut"));
					descriptions.add(new InterfaceDescriptionImpl("Channels", "Channel In/Out Provider", "be.nabu.libs.eai.module.channels.api.ServiceChannelProvider.transactInOut"));
					ChannelInterfaceLister.descriptions = descriptions;
				}
			}
		}
		return descriptions;
	}

}
