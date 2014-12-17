/*
 * Copyright 2012 essendi it GmbH
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

package org.vaadin.ui.client.numberfield;

import org.vaadin.ui.NumberField;
import org.vaadin.ui.shared.numberfield.Constants;

import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.UIDL;
import com.vaadin.client.ui.textfield.TextFieldConnector;
import com.vaadin.shared.ui.Connect;
import com.vaadin.shared.ui.Connect.LoadStyle;

@Connect(value = NumberField.class, loadStyle = LoadStyle.EAGER)
public class NumberFieldConnector extends TextFieldConnector {

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        super.updateFromUIDL(uidl, client);
        /*
         * updateFromUIDL() is calling client.updateComponent() -> if this
         * method returns, executing processAttributesFromServer() would be not
         * needed, but we dunno if it returns or not. So we use hasAttribute()
         * before getXAttribute() calls in the processAttributesFromServer()
         * method to avoid setting invalid/undefined data.
         */
        processAttributesFromServer(uidl);
    }

    private void processAttributesFromServer(UIDL uidl) {
        if (uidl.hasAttribute(Constants.ATTRIBUTE_ALLOW_NEGATIVES)) {
            getWidget().attributes.setNegativeAllowed(uidl
                    .getBooleanAttribute(Constants.ATTRIBUTE_ALLOW_NEGATIVES));
        }

        if (uidl.hasAttribute(Constants.ATTRIBUTE_DECIMAL_PRECISION)) {
            getWidget().attributes.setDecimalPrecision(uidl
                    .getIntAttribute(Constants.ATTRIBUTE_DECIMAL_PRECISION));
        }

        if (uidl.hasAttribute(Constants.ATTRIBUTE_MIN_VALUE)) {
            getWidget().attributes.setMinValue(uidl
                    .getDoubleAttribute(Constants.ATTRIBUTE_MIN_VALUE));
        }

        if (uidl.hasAttribute(Constants.ATTRIBUTE_MAX_VALUE)) {
            getWidget().attributes.setMaxValue(uidl
                    .getDoubleAttribute(Constants.ATTRIBUTE_MAX_VALUE));
        }

        if (uidl.hasAttribute(Constants.ATTRIBUTE_ALLOW_DECIMALS)) {
            getWidget().attributes.setDecimalAllowed(uidl
                    .getBooleanAttribute(Constants.ATTRIBUTE_ALLOW_DECIMALS));
        }

        if (uidl.hasAttribute(Constants.ATTRIBUTE_DECIMAL_SEPARATOR)) {
            getWidget().attributes.setDecimalSeparator((char) uidl
                    .getIntAttribute(Constants.ATTRIBUTE_DECIMAL_SEPARATOR));
        }

        if (uidl.hasAttribute(Constants.ATTRIBUTE_USE_GROUPING)) {
            getWidget().attributes.setGroupingUsed(uidl
                    .getBooleanAttribute(Constants.ATTRIBUTE_USE_GROUPING));
        }

        if (uidl.hasAttribute(Constants.ATTRIBUTE_GROUPING_SEPARATOR)) {
            getWidget().attributes.setGroupingSeparator((char) uidl
                    .getIntAttribute(Constants.ATTRIBUTE_GROUPING_SEPARATOR));
        }

        if (uidl.hasAttribute(Constants.ATTRIBUTE_SERVER_FORMATTED_VALUE)) {
            getWidget()
                    .setValue(
                            uidl.getStringAttribute(Constants.ATTRIBUTE_SERVER_FORMATTED_VALUE));
        }
    }

    @Override
    public VNumberField getWidget() {
        return (VNumberField) super.getWidget();
    }

}
