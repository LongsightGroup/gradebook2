package org.sakaiproject.gradebook.gwt.client.gxt.event;

import org.sakaiproject.gradebook.gwt.client.model.ItemModel;

import com.extjs.gxt.ui.client.store.Store;

public class ItemCreate {

	public ItemModel item;
	public Store store;
	public boolean close;
	
	public ItemCreate(Store store, ItemModel item, boolean close) {
		this.store = store;
		this.item = item;
		this.close = close;
	}
	
}
