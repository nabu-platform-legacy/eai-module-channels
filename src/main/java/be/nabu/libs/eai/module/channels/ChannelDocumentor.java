package be.nabu.libs.eai.module.channels;

import java.util.Map;

import be.nabu.eai.repository.DocumentationManager.DocumentedImpl;
import be.nabu.eai.repository.api.Documented;
import be.nabu.eai.repository.api.Documentor;
import be.nabu.eai.repository.api.Repository;

public class ChannelDocumentor implements Documentor<ChannelArtifact> {

	@Override
	public Class<ChannelArtifact> getDocumentedClass() {
		return ChannelArtifact.class;
	}

	@Override
	public Documented getDocumentation(Repository repository, ChannelArtifact channel) {
		StringBuilder builder = new StringBuilder();
		builder.append("<section class='channel'><ul>");
		builder.append("<li><span class='key'>Context</span><span class='value'>" + channel.getConfig().getContext() + "</span></li>");
		builder.append("<li><span class='key'>Provider</span><span class='value'>" + channel.getConfig().getProviderId() + "</span></li>");
		if (channel.getConfig().getTransactionality() != null) {
			builder.append("<li><span class='key'>Transactionality</span><span class='value'>" + channel.getConfig().getTransactionality() + "</span></li>");
		}
		if (channel.getConfig().getRetryAmount() > 0) {
			builder.append("<li><span class='key'>Retry Amount</span><span class='value'>" + channel.getConfig().getRetryAmount() + "</span></li>");
			if (channel.getConfig().getRetryInterval() > 0) {
				builder.append("<li><span class='key'>Retry Interval</span><span class='value'>" + channel.getConfig().getRetryInterval() + "</span></li>");
			}
		}
		documentProperties(channel, builder);
		builder.append("</ul></section>");
		DocumentedImpl documentation = new DocumentedImpl("text/html");
		documentation.setDescription(builder.toString());
		documentation.setTitle(channel.getId());
		return documentation;
	}

	public static void documentProperties(ChannelArtifact channel, StringBuilder builder) {
		Map<String, String> properties = channel.getConfig().getProperties();
		StringBuilder propertyBuilder = new StringBuilder();
		if (properties != null) {
			for (String key : properties.keySet()) {
				if (properties.get(key) != null) {
					propertyBuilder.append("<li><span class='key'>" + key + "</span><span class='value'>" + properties.get(key) + "</span></li>");
				}
			}
		}
		if (!propertyBuilder.toString().isEmpty()) {
			builder.append("<li><span class='key'>Properties</span></li><ul>");
			builder.append(propertyBuilder);
			builder.append("</ul>");
		}
	}
}
