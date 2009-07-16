package org.sakaiproject.gradebook.gwt.client.model;


public class StatisticsModel extends EntityModel {

	private static final long serialVersionUID = 1L;

	public enum Key {
		ID("Id"), NAME("Name"), MEAN("Mean"), MEDIAN("Median"), MODE("Mode"), STANDARD_DEVIATION("Standard Deviation");
		
		private String propertyName;
		
		private Key(String propertyName) {
			this.propertyName = propertyName;
		}
		
		public String getPropertyName() {
			return propertyName;
		}
		
	}
	
	
	public StatisticsModel() {
		
	}
	
	public String getId() {
		return get(Key.ID.name());
	}
	
	public void setId(String id) {
		set(Key.ID.name(), id);
	}
	
	public String getName() {
		return get(Key.NAME.name());
	}
	
	public void setName(String name) {
		set(Key.NAME.name(), name);
	}
	
	public String getMean() {
		return get(Key.MEAN.name());
	}
	
	public void setMean(String mean) {
		set(Key.MEAN.name(), mean);
	}
	
	public String getMedian() {
		return get(Key.MEDIAN.name());
	}
	
	public void setMedian(String median) {
		set(Key.MEDIAN.name(), median);
	}
	
	public String getMode() {
		return get(Key.MODE.name());
	}
	
	public void setMode(String mode) {
		set(Key.MODE.name(), mode);
	}
	
	public String getStandardDeviation() {
		return get(Key.STANDARD_DEVIATION.name());
	}
	
	public void setStandardDeviation(String sd) {
		set(Key.STANDARD_DEVIATION.name(), sd);
	}

	@Override
	public String getDisplayName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getIdentifier() {
		return get(Key.ID.name());
	}
	
}
