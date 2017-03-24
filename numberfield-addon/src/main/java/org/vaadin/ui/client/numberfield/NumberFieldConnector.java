/*
 * Copyright 2012 essendi it GmbH
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

package org.vaadin.ui.client.numberfield;

import org.vaadin.ui.NumberField;
import org.vaadin.ui.shared.numberfield.NumberFieldState;

import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.UIDL;
import com.vaadin.client.annotations.OnStateChange;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.textfield.TextFieldConnector;
import com.vaadin.shared.ui.Connect;
import com.vaadin.shared.ui.Connect.LoadStyle;

@Connect(value = NumberField.class, loadStyle = LoadStyle.EAGER)
public class NumberFieldConnector extends TextFieldConnector {

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);
        getWidget().attributes = getState();
    }

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        // ugly workaround for issue where getState().text is used to update
        // field content at the end of this method
        getState().text = getState().formattedValue;
        super.updateFromUIDL(uidl, client);
    }

    @OnStateChange("formattedValue")
    private void formattedValueChanged() {
        getWidget().setValue(getState().formattedValue);
    }

    @Override
    public VNumberField getWidget() {
        return (VNumberField) super.getWidget();
    }

    @Override
    public NumberFieldState getState() {
        return (NumberFieldState) super.getState();
    }

}
