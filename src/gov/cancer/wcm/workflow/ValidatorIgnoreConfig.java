package gov.cancer.wcm.workflow;

import java.util.List;

public class ValidatorIgnoreConfig {
	private List<String> ignoreTriggers;
	private List<String> ignoreWorkflows;
	
	/**
	 * @return List of triggers to ignore.
	 */
	public List<String> getIgnoreTriggers() {
		return this.ignoreTriggers;
	}
	
	/**
	 * @return List of work flows to ignore.
	 */
	public List<String> getIgnoreWorkflows(){
		return this.ignoreWorkflows;
	}
	
	private ValidatorIgnoreConfig(List<String> ignoreTriggers, List<String> ignoreWorkflows) {
		this.ignoreTriggers = ignoreTriggers;
		this.ignoreWorkflows = ignoreWorkflows;
	}
}
