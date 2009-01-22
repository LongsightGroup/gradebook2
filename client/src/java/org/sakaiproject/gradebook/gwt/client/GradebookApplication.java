/**********************************************************************************
*
* $Id:$
*
***********************************************************************************
*
* Copyright (c) 2008, 2009 The Regents of the University of California
*
* Licensed under the
* Educational Community License, Version 2.0 (the "License"); you may
* not use this file except in compliance with the License. You may
* obtain a copy of the License at
* 
* http://www.osedu.org/licenses/ECL-2.0
* 
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an "AS IS"
* BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
* or implied. See the License for the specific language governing
* permissions and limitations under the License.
*
**********************************************************************************/
package org.sakaiproject.gradebook.gwt.client;

import java.util.List;

import org.sakaiproject.gradebook.gwt.client.action.RemoteCommand;
import org.sakaiproject.gradebook.gwt.client.action.UserActionHistory;
import org.sakaiproject.gradebook.gwt.client.action.UserEntityAction;
import org.sakaiproject.gradebook.gwt.client.action.UserEntityGetAction;
import org.sakaiproject.gradebook.gwt.client.action.Action.EntityType;
import org.sakaiproject.gradebook.gwt.client.gxt.GradebookContainer;
import org.sakaiproject.gradebook.gwt.client.gxt.GradebookTabItem;
import org.sakaiproject.gradebook.gwt.client.gxt.TabContainer;
import org.sakaiproject.gradebook.gwt.client.model.ApplicationModel;
import org.sakaiproject.gradebook.gwt.client.model.GradebookModel;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.util.Theme;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.gears.client.database.Database;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class GradebookApplication implements EntryPoint {
	
	private GradebookToolFacadeAsync dataService;
	private GradebookContainer gradebookContainer;
	private Viewport viewport;
	private int screenHeight = 600;
	private Database db;
	
    public GradebookApplication() {
    	GXT.setDefaultTheme(Theme.GRAY, true);
    }
	
	public void onModuleLoad() {
		viewport = new Viewport();
		viewport.setLayout(new FitLayout());
		viewport.setHeight(screenHeight);
		RootPanel.get().add(viewport);
		
		GradebookConstants i18n = (GradebookConstants) GWT.create(GradebookConstants.class);
		
		if (dataService == null) {
			dataService = GWT.create(GradebookToolFacade.class);
			EndpointUtil.setEndpoint((ServiceDefTarget) dataService);
		}
		
		if (dataService == null) {
			MessageBox box = new MessageBox();
			box.setButtons(MessageBox.OK);
			box.setIcon(MessageBox.INFO);
			box.setTitle("Information");
			box.setMessage("No service detected");
			box.show();
			return;
		}
		
		Registry.register("history", new UserActionHistory());
		Registry.register("service", dataService);
		Registry.register("i18n", i18n);

		UserEntityGetAction<ApplicationModel> action = 
			new UserEntityGetAction<ApplicationModel>(EntityType.APPLICATION);
		
		RemoteCommand<ApplicationModel> remoteCommand =
			new RemoteCommand<ApplicationModel>() {
		
			public void onCommandSuccess(UserEntityAction<ApplicationModel> action, ApplicationModel model) {
				
				String placementId = model.getPlacementId();
				if (placementId != null) {
					String modifiedId = placementId.replace('-', 'x');
					resizeMainFrame("Main" + modifiedId, screenHeight + 20);
				}
				
				loadGradebooks(screenHeight, model.getGradebookModels());
			}
			
		};
		
		remoteCommand.execute(action);
	}
	
	protected void loadGradebooks(final int screenHeight, List<GradebookModel> models) {
		final GradebookToolFacadeAsync service = (GradebookToolFacadeAsync) Registry.get("service");

		if (service == null) {
			MessageBox box = new MessageBox();
			box.setButtons(MessageBox.OK);
			box.setIcon(MessageBox.INFO);
			box.setTitle("Information");
			box.setMessage("No service detected");
			box.show();
			return;
		}

		int index = 0;
		boolean isTabbed = models.size() > 1;
		TabContainer container = null;
				
		if (isTabbed) {
			container = new TabContainer();
			container.setHeight(screenHeight);
		}
				
		for (GradebookModel model : models) {
			Registry.register(model.getGradebookUid(), model);
								
			// Okay, now build the UI component
			if (isTabbed) {
				GradebookTabItem item = new GradebookTabItem(model.getGradebookUid());
				item.setText(model.getName());
				item.setClosable(false);
				item.addStyleName("pad-text");
				gradebookContainer = new GradebookContainer(model.getGradebookUid());
				item.add(gradebookContainer);
				container.getTabPanel().add(item);
			} else {
				gradebookContainer = new GradebookContainer(model.getGradebookUid());
				viewport.add(gradebookContainer);
			}
					
			index++;
		}
		if (isTabbed) {
			viewport.add(container);
		}
		viewport.layout();
	}
	

	// FIXME: This needs to be cleaned up
	public native void resizeMainFrame(String placementId, int setHeight) /*-{
		
		//	$wnd.alert("Is " + placementId + " equal to Mainff1e8b82x01e4x4d00x9a17x3982e11d5bd1 ? ");
	
		
			var frame = $wnd.parent.document.getElementById(placementId);

			if (frame)
		   	{
		       // reset the scroll
		 //      $wnd.parent.window.scrollTo(0,0);
	
		       var objToResize = (frame.style) ? frame.style : frame;
		       var height;                
		       var offsetH = $wnd.parent.document.body.offsetHeight;
		       
		       //$wnd.alert($doc.body.offsetHeight + " and " + offsetH);
		       
		       var innerDocScrollH = null;

		       if (typeof(frame.contentDocument) != 'undefined' || typeof(frame.contentWindow) != 'undefined')
		       {
		           // very special way to get the height from IE on Windows!
		           // note that the above special way of testing for undefined variables is necessary for older browsers
		           // (IE 5.5 Mac) to not choke on the undefined variables.
		           var innerDoc = (frame.contentDocument) ? frame.contentDocument : frame.contentWindow.document;
		           innerDocScrollH = (innerDoc != null) ? innerDoc.body.scrollHeight : null;
		       }
		       
		       if ($wnd.parent.document.all && innerDocScrollH != null)
		       {
		           // IE on Windows only
		           height = innerDocScrollH;
		       }
		       else
		       {
		           // every other browser!
		           height = offsetH;
		       }
	
		       // here we fudge to get a little bigger
		       var newHeight = setHeight;

	
		       // but not too big!
		       if (newHeight > 32760) newHeight = 32760;
	
		       // capture my current scroll position
	//	       var scroll = findScroll();
	
		       // resize parent frame (this resets the scroll as well)
		       objToResize.height=newHeight + "px";
	
		       // reset the scroll, unless it was y=0)
	//	       if (scroll[1] > 0)
	//	       {
	//	           var position = findPosition(frame);
	//	           $wnd.parent.window.scrollTo(position[0]+scroll[0], position[1]+scroll[1]);
	//	       }
		       
		       //objToResize.height=offsetH + "px";
		       
		    } 
		    
	 }-*/;


	public native void findScroll() /*-{
	 	var x = 0;
	 	var y = 0;
	 	if (self.pageYOffset)
	 	{
	 		x = self.pageXOffset;
	 		y = self.pageYOffset;
	 	}
	 	else if ($doc.documentElement && $doc.documentElement.scrollTop)
	 	{
	 		x = $doc.documentElement.scrollLeft;
	 		y = $doc.documentElement.scrollTop;
	 	}
	 	else if ($doc.body)
	 	{
	 		x = $doc.body.scrollLeft;
	 		y = $doc.body.scrollTop;
	 	}
	 	
	 	return [x,y];
	 }-*/;

	public GradebookContainer getGradebookContainer() {
		return gradebookContainer;
	}


}
