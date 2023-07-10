package com.example.application;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.internal.DefaultErrorHandler;
import com.vaadin.flow.server.ErrorEvent;
import com.vaadin.flow.server.ErrorHandler;
import com.vaadin.flow.server.VaadinSession;

public class GlobalErrorHandler implements ErrorHandler {

  @Override
  public void error(ErrorEvent event) {
    // Log the error if needed
    event.getThrowable().printStackTrace();

    VaadinSession session = VaadinSession.getCurrent();
    UI ui = UI.getCurrent();
    if (ui != null && session != null) {
      ui.access(() -> {
        Dialog dialog = new Dialog();
        dialog.add(event.getThrowable().getMessage());
        dialog.open();
      });
    }
  }
}
