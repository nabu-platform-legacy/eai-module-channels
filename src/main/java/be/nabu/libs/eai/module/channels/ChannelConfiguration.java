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

package be.nabu.libs.eai.module.channels;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import be.nabu.eai.api.EnvironmentSpecific;
import be.nabu.eai.api.ValueEnumerator;
import be.nabu.eai.repository.util.KeyValueMapAdapter;
import be.nabu.libs.datatransactions.api.Transactionality;
import be.nabu.libs.eai.module.channels.util.ProviderEnumerator;

@XmlRootElement(name = "channel")
@XmlType(propOrder = { "context", "providerId", "transactionality", "batch", "properties", "priority", "finishAmount", "retryAmount", "retryInterval", "continueOnFailure" })
public class ChannelConfiguration {
	// the name of the provider
	private String providerId, context;
	private Transactionality transactionality;
	private int priority, finishAmount, retryAmount;
	private long retryInterval;
	private boolean continueOnFailure;
	private boolean batch;
	
	private Map<String, String> properties;

	@EnvironmentSpecific
	@XmlJavaTypeAdapter(value = KeyValueMapAdapter.class)
	public Map<String, String> getProperties() {
		if (properties == null) {
			properties = new LinkedHashMap<String, String>();
		}
		return properties;
	}
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
	
	@EnvironmentSpecific
	@ValueEnumerator(enumerator = ProviderEnumerator.class)
	public String getProviderId() {
		return providerId;
	}
	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}
	public String getContext() {
		return context;
	}
	public void setContext(String context) {
		this.context = context;
	}
	
	@EnvironmentSpecific
	public Transactionality getTransactionality() {
		return transactionality;
	}
	public void setTransactionality(Transactionality transactionality) {
		this.transactionality = transactionality;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	public int getFinishAmount() {
		return finishAmount;
	}
	public void setFinishAmount(int finishAmount) {
		this.finishAmount = finishAmount;
	}
	public int getRetryAmount() {
		return retryAmount;
	}
	public void setRetryAmount(int retryAmount) {
		this.retryAmount = retryAmount;
	}
	public long getRetryInterval() {
		return retryInterval;
	}
	public void setRetryInterval(long retryInterval) {
		this.retryInterval = retryInterval;
	}
	public boolean isContinueOnFailure() {
		return continueOnFailure;
	}
	public void setContinueOnFailure(boolean continueOnFailure) {
		this.continueOnFailure = continueOnFailure;
	}
	public boolean isBatch() {
		return batch;
	}
	public void setBatch(boolean batch) {
		this.batch = batch;
	}
}
