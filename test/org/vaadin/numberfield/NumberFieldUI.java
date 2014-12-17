/*
 * Copyright 2014 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.vaadin.numberfield;

import javax.servlet.annotation.WebServlet;

import org.vaadin.ui.NumberField;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
@Theme("numberfieldtest")
public class NumberFieldUI extends UI {

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = NumberFieldUI.class, widgetset = "org.vaadin.ui.NumberFieldWidgetset")
    public static class Servlet extends VaadinServlet {
    }

    @Override
    protected void init(VaadinRequest request) {
        final VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSpacing(true);
        setContent(layout);

        layout.addComponent(new NumberField("Default settings"));

        final NumberField numberField = new NumberField();
        numberField.setCaption("Modified settings:");
        numberField.setDecimalPrecision(2);
        numberField.setDecimalSeparator(',');
        numberField.setGroupingSeparator('.');
        numberField.setDecimalSeparatorAlwaysShown(true);
        numberField.setMinimumFractionDigits(2);
        numberField.setMinValue(5);
        layout.addComponent(numberField);

        Button alignmentButton = new Button("Toggle alignment");
        alignmentButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                // note: this tests overriding of default styles by custom theme
                // there is no built-in alignment change
                if (!numberField.getStyleName().contains("left")) {
                    numberField.addStyleName("left");
                } else {
                    numberField.removeStyleName("left");
                }
            }
        });
        layout.addComponent(alignmentButton);
    }

}