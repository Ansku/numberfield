package org.vaadin.numberfield;

import com.vaadin.annotations.Widgetset;
import org.vaadin.addonhelpers.TServer;

/**
 * Created by heintz on 13/05/16.
 */
@Widgetset("org.vaadin.ui.NumberFieldWidgetset")
public class UIRunner extends TServer {
    /**
     * Starts and embedded server for the tests
     */
    public static void main(String[] args) throws Exception {
        new UIRunner().startServer();

    }
}
