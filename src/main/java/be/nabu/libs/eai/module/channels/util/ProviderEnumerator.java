package be.nabu.libs.eai.module.channels.util;

import java.util.ArrayList;
import java.util.List;

import be.nabu.eai.api.Enumerator;

public class ProviderEnumerator implements Enumerator {

	@Override
	public List<?> enumerate() {
		List<String> names = new ArrayList<String>();
		names.addAll(ChannelManagerImpl.getInstance().getProviders().keySet());
		return names;
	}

}
