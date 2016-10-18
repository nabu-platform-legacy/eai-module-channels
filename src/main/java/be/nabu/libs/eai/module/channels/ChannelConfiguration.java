package be.nabu.libs.eai.module.channels;

import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import be.nabu.eai.repository.util.KeyValueMapAdapter;
import be.nabu.libs.datatransactions.api.Direction;
import be.nabu.libs.datatransactions.api.Transactionality;

@XmlRootElement(name = "channel")
public class ChannelConfiguration {
	// the name of the provider
	private String providerId, context;
	private Direction direction;
	private Transactionality transactionality;
	private int priority, finishAmount, retryAmount;
	private long retryInterval;
	private boolean continueOnFailure;
	
	private Map<String, String> properties;

	@XmlJavaTypeAdapter(value = KeyValueMapAdapter.class)
	public Map<String, String> getProperties() {
		return properties;
	}
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
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
	public Direction getDirection() {
		return direction;
	}
	public void setDirection(Direction direction) {
		this.direction = direction;
	}
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
	
}
