/*
 * Copyright 2014-2017 Vaadin Ltd.
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

import java.io.Serializable;
import java.util.Locale;

import javax.servlet.annotation.WebServlet;

import org.vaadin.ui.NumberField;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.Item;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.util.BeanItem;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.FormFieldFactory;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
@Theme("numberfieldtest")
public class NumberFieldUI extends UI {

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = NumberFieldUI.class, widgetset = "org.vaadin.ui.NumberFieldDemoWidgetset")
    public static class Servlet extends VaadinServlet {
    }

    private VerticalLayout mainLayout;

    @Override
    protected void init(VaadinRequest request) {
        mainLayout = createLayout();
        mainLayout.removeStyleName("border");
        setContent(mainLayout);

        addDefaultTest();
        addTestForRetrievingValue();
        addForm();
        addAnotherForm();
        addFieldGroup();
    }

    private void addDefaultTest() {
        VerticalLayout layout = createLayout();
        layout.setCaption("Default Test");
        layout.addComponent(new NumberField("Default settings"));

        final NumberField numberField = new NumberField();
        numberField.setLocale(Locale.FRANCE);
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

        Button setValueButton = new Button("Set value programmatically");
        setValueButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                numberField.setValue(119.9d);
            }
        });
        layout.addComponent(setValueButton);
        mainLayout.addComponent(layout);
    }

    private void addTestForRetrievingValue() {
        VerticalLayout layout = createLayout();
        layout.setCaption("Test retrieving value");
        final NumberField startHour = new NumberField();
        startHour.setMinValue(0);
        startHour.setMaxValue(59);
        startHour.setErrorText("global_invalid_format");
        startHour.setWidth("60px");
        startHour.setImmediate(true);
        layout.addComponent(startHour);

        Button checkValueButton = new Button("Get current value");
        checkValueButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                String value = startHour.getValue();
                if (value == null || value.isEmpty()) {
                    Notification.show("no value");
                } else {
                    Notification.show(Integer.valueOf(value).toString());
                }
            }
        });
        layout.addComponent(checkValueButton);
        mainLayout.addComponent(layout);
    }

    @SuppressWarnings("deprecation")
    private void addForm() {
        VerticalLayout layout = createLayout();
        layout.setCaption("Form with FormFieldFactory");
        final NumberField numberField = new NumberField();
        numberField.setLocale(Locale.FRANCE);
        numberField.setCaption("Modified settings:");
        numberField.setDecimalPrecision(2);
        numberField.setDecimalSeparator(',');
        numberField.setGroupingSeparator('.');
        numberField.setDecimalSeparatorAlwaysShown(true);
        numberField.setMinimumFractionDigits(2);
        numberField.setMinValue(5);

        Form form = new Form();
        form.setFormFieldFactory(new FormFieldFactory() {

            @Override
            public Field<?> createField(Item item, Object propertyId,
                    Component uiContext) {
                if ("number".equals(propertyId)) {
                    return numberField;
                } else {
                    return new TextField("placeholder textfield");
                }
            }
        });
        BeanItem<Bean> beanItem = new BeanItem<Bean>(new Bean());
        form.setItemDataSource(beanItem);
        layout.addComponent(form);
        mainLayout.addComponent(layout);
    }

    @SuppressWarnings({ "deprecation", "unchecked" })
    private void addAnotherForm() {
        VerticalLayout layout = createLayout();
        layout.setCaption("Form with DefaultFieldFactory");
        final NumberField numberField = new NumberField();
        numberField.setLocale(Locale.FRANCE);
        numberField.setCaption("Modified settings:");
        numberField.setDecimalPrecision(2);
        numberField.setDecimalSeparator(',');
        numberField.setGroupingSeparator('.');
        numberField.setDecimalSeparatorAlwaysShown(true);
        numberField.setMinimumFractionDigits(2);
        numberField.setMinValue(5);

        Form form = new Form();
        form.setFormFieldFactory(new DefaultFieldFactory() {

            @Override
            public Field<?> createField(Item item, Object propertyId,
                    Component uiContext) {
                if ("number".equals(propertyId)) {
                    return numberField;
                } else {
                    return super.createField(item, propertyId, uiContext);
                }
            }
        });
        BeanItem<Bean> beanItem = new BeanItem<Bean>(new Bean());
        form.setItemDataSource(beanItem);
        layout.addComponent(form);
        mainLayout.addComponent(layout);
    }

    private void addFieldGroup() {
        VerticalLayout layout = createLayout();
        layout.setCaption("FieldGroup");
        BeanItem<Bean> beanItem = new BeanItem<Bean>(new Bean());
        BeanFieldGroup<Bean> fieldGroup = new BeanFieldGroup<NumberFieldUI.Bean>(
                Bean.class);
        fieldGroup.setItemDataSource(beanItem);
        for (Object propertyId : fieldGroup.getUnboundPropertyIds()) {
            Field<?> field;
            if ("number".equals(propertyId)) {
                NumberField numberField = new NumberField();
                numberField.setLocale(Locale.FRANCE);
                numberField.setCaption("Modified settings:");
                numberField.setDecimalPrecision(2);
                numberField.setDecimalSeparator(',');
                numberField.setGroupingSeparator('.');
                numberField.setDecimalSeparatorAlwaysShown(true);
                numberField.setMinimumFractionDigits(2);
                numberField.setMinValue(5);
                field = numberField;
                fieldGroup.bind(field, propertyId);
            } else {
                field = fieldGroup.buildAndBind(propertyId);
            }
            layout.addComponent(field);
            mainLayout.addComponent(layout);
        }
    }

    private VerticalLayout createLayout() {
        final VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSpacing(true);
        layout.addStyleName("border");
        return layout;
    }

    public class Bean implements Serializable {
        Double number = 5.5d;
        Double anotherNumber = 3d;
        String randomString = "random String";

        public Double getNumber() {
            return number;
        }

        public void setNumber(Double number) {
            this.number = number;
        }

        public Double getAnotherNumber() {
            return anotherNumber;
        }

        public void setAnotherNumber(Double anotherNumber) {
            this.anotherNumber = anotherNumber;
        }

        public String getRandomString() {
            return randomString;
        }

        public void setRandomString(String randomString) {
            this.randomString = randomString;
        }

    }

}