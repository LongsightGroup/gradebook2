package org.sakaiproject.gradebook.gwt.client.gxt.view;

import org.sakaiproject.gradebook.gwt.client.gxt.Notifier;
import org.sakaiproject.gradebook.gwt.client.gxt.event.GradebookEvents;
import org.sakaiproject.gradebook.gwt.client.gxt.event.NotificationEvent;
import org.sakaiproject.gradebook.gwt.client.gxt.view.panel.NotificationPanel;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;

public class NotificationView extends View {

	private static Notifier notifier;
	
	private static final String decisionFlag = "DecisionType";
	private enum Decision { OK, CANCEL };
	
	/*private NotificationPanel notificationPanel;
	private Html text;
	private Button okButton;
	private Button cancelButton;
	private ConfirmationEvent confirmationEvent;
	private SelectionListener<ButtonEvent> selectionListener ;*/
	
	public NotificationView(Controller controller) {
		super(controller);
		this.notifier = new Notifier();
		
		//initListeners();
		/*this.notificationPanel = new NotificationPanel();
		this.text = this.notificationPanel.addText("");
		this.okButton = new Button("Ok");
		this.okButton.setData(decisionFlag, Decision.OK);
		this.okButton.setItemId(AppConstants.ID_CONFIRM_OK_BUTTON);
		this.cancelButton = new Button("Cancel");
		this.cancelButton.setData(decisionFlag, Decision.CANCEL);
		this.cancelButton.setItemId(AppConstants.ID_CONFIRM_CANCEL_BUTTON);
		this.okButton.addSelectionListener(selectionListener);
		this.cancelButton.addSelectionListener(selectionListener);
		this.notificationPanel.addButton(okButton);
		this.notificationPanel.addButton(cancelButton);
		
		okButton.setVisible(false);
		cancelButton.setVisible(false);*/
	}

	@Override
	protected void handleEvent(AppEvent<?> event) {
		NotificationEvent notification = (NotificationEvent)event.data;
		
		switch (GradebookEvents.getEvent(event.type).getEventKey()) {
		case EXCEPTION:
			notifier.notifyError(notification.getError());
			break;
		default:
			notifier.notifyUserError("Request Failed", notification.getText());
			
		
		/*case CLOSE_NOTIFICATION:
			text.setHtml("");
			okButton.setVisible(false);
			cancelButton.setVisible(false);
			break;
		case CONFIRMATION:
			confirmationEvent = (ConfirmationEvent)event.data;
			text.setHtml(confirmationEvent.text);
			okButton.setVisible(true);
			cancelButton.setVisible(true);
			notificationPanel.layout();
			break;
		case NOTIFICATION:
			NotificationEvent notificationEvent = (NotificationEvent)event.data;
			text.setHtml(notificationEvent.getText());
			confirmationEvent = null;
			okButton.setVisible(false);
			cancelButton.setVisible(false);
			break;*/
		}
	}

	/*
	private void initListeners() {
		selectionListener = new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent be) {
				
				Decision decision = be.button.getData(decisionFlag);
				
				switch (decision) {
				case OK:
					if (confirmationEvent != null && confirmationEvent.okEventType != -1) {
						Dispatcher.forwardEvent(confirmationEvent.okEventType, confirmationEvent.okEventData);
					}
					Dispatcher.forwardEvent(GradebookEvents.CloseNotification.getEventType());
					break;
				case CANCEL:
					if (confirmationEvent != null && confirmationEvent.cancelEventType != -1) {
						Dispatcher.forwardEvent(confirmationEvent.cancelEventType, confirmationEvent.cancelEventData);
					}
					Dispatcher.forwardEvent(GradebookEvents.CloseNotification.getEventType());
					break;
				}
				
				
			}

		};
	}
	
	public NotificationPanel getNotificationPanel() {
		return notificationPanel;
	}
	*/

}
