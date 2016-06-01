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

import org.vaadin.ui.shared.numberfield.Constants;
import org.vaadin.ui.shared.numberfield.NumberFieldState;
import org.vaadin.ui.shared.numberfield.NumberValidator;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.vaadin.client.ui.VTextField;

/**
 * This client-side widget represents a basic input field for numerical values
 * (integers/decimals) and extends {@link VTextField}.
 */
public class VNumberField extends VTextField {

    /**
     * For internal use only. May be removed or replaced in the future.
     */
    public NumberFieldState attributes = new NumberFieldState();

    private KeyPressHandler keyPressHandler = new KeyPressHandler() {
        /**
         * Do keystroke filtering (e.g. no letters) and validation for integer
         * (123) and decimal numbers (12.3) on keypress events.
         */
        @Override
        public void onKeyPress(KeyPressEvent event) {
            if (isReadOnly() || !isEnabled()) {
                return;
            }

            int keyCode = event.getNativeEvent().getKeyCode();
            if (isControlKey(keyCode)) {
                return;
            }

            if (!isValueValid(event) || event.isAnyModifierKeyDown()) {
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

    public VNumberField() {
        addStyleName(Constants.CSS_CLASSNAME);
        addKeyPressHandler(keyPressHandler);
        addKeyDownHandler(new KeyDownHandler() {

            @Override
            public void onKeyDown(KeyDownEvent event) {
                if (event.getNativeKeyCode() == 110) {
                    event.preventDefault();
                    String value = getValue();
                    int pos = getCursorPos();
                    String newValue = value.substring(0, getCursorPos());
                    newValue += attributes.getDecimalSeparator();
                    if (value.length() > getCursorPos()) {
                        newValue += value.substring(getCursorPos(),
                                value.length());
                    }
                    setValue(newValue);
                    setCursorPos(pos + 1);
                }
            }
        });

    }

    private boolean isControlKey(int keyCode) {
        switch (keyCode) {
            case KeyCodes.KEY_LEFT:
            case KeyCodes.KEY_RIGHT:
            case KeyCodes.KEY_UP:
            case KeyCodes.KEY_DOWN:
            case KeyCodes.KEY_BACKSPACE:
            case KeyCodes.KEY_DELETE:
            case KeyCodes.KEY_TAB:
            case KeyCodes.KEY_ENTER:
            case KeyCodes.KEY_ESCAPE:
            case KeyCodes.KEY_HOME:
            case KeyCodes.KEY_END:
                return true;
        }

        return false;
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
