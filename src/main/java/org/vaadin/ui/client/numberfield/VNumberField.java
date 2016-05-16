/*
 * Copyright 2012 essendi it GmbH
 * Copyright 2014-2016 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.vaadin.ui.client.numberfield;

import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.vaadin.client.ui.VTextField;
import org.vaadin.ui.shared.numberfield.Constants;
import org.vaadin.ui.shared.numberfield.NumberFieldState;
import org.vaadin.ui.shared.numberfield.NumberValidator;

/**
 * This client-side widget represents a basic input field for numerical values
 * (integers/decimals) and extends {@link VTextField}.
 */
public class VNumberField extends VTextField {

    /**
     * For internal use only. May be removed or replaced in the future.
     */
    public NumberFieldState attributes = new NumberFieldState();

    public VNumberField() {
        addStyleName(Constants.CSS_CLASSNAME);
        KeyPressHandler keyPressHandler = new KeyPressHandler() {
            /**
             * Do keystroke filtering (e.g. no letters) and validation for integer
             * (123) and decimal numbers (12.3) on keypress events.
             */

            @Override
            public void onKeyPress(KeyPressEvent event) {
                tryFixValue();
                if (onlyJumpingDecimalChar(event.getCharCode())) {
                    cancelKey();
                }
                if (isReadOnly() || !isEnabled()) {
                    return;
                }
                boolean validValue = isValueValid(event);
                if (!validValue || event.isAnyModifierKeyDown()) {
                    cancelKey();
                }
            }

            private boolean isValueValid(KeyPressEvent event) {
                // Bypass the check that value >= attributes.getMinValue() on a
                // keypress
                double savedMinValue = attributes.getMinValue();
                attributes.setMinValue(Double.NEGATIVE_INFINITY);

                String newText = getFieldValueAsItWouldBeAfterKeyPress(event
                        .getCharCode());
                boolean valueIsValid = attributes.isDecimalAllowed() ? NumberValidator
                        .isValidDecimal(newText, attributes, true)
                        : NumberValidator.isValidInteger(newText, attributes, true);

                attributes.setMinValue(savedMinValue);

                return valueIsValid;
            }
        };

        addKeyPressHandler(keyPressHandler);
    }

    private boolean onlyJumpingDecimalChar(char charCode) {
        String text = getText();
        if (!text.isEmpty()) {
            int cursorPosition = getCursorPos();
            String nextChar = text.substring(cursorPosition, cursorPosition + 1);
            if (String.valueOf(charCode).equals(nextChar)) {
                setCursorPos(cursorPosition + 1);
                return true;
            }
        }
        return false;
    }

    /**
     * A method which attempts to fix any incompatible values.
     * First (and currently only) example is the requirements of fraction
     * digits without having a value, which adds the correct number of fraction
     * digits and positions the cursor to the integer part.
     */
    private void tryFixValue() {
        if (getText().isEmpty() && attributes.getMinimumFractionDigits() > 0) {
            String decimals = "";
            for (int i = 0; i < attributes.getMinimumFractionDigits(); i++) {
                decimals += "0";
            }
            setText(attributes.getDecimalSeparator() + decimals);
            setCursorPos(0);
        }
    }

    private String getFieldValueAsItWouldBeAfterKeyPress(char charCode) {
        int index = getCursorPos();
        String previousText = getText();

        if (getSelectionLength() > 0) {
            return previousText.substring(0, index)
                    + charCode
                    + previousText.substring(index + getSelectionLength(),
                    previousText.length());
        } else {
            return previousText.substring(0, index) + charCode
                    + previousText.substring(index, previousText.length());
        }
    }
}
