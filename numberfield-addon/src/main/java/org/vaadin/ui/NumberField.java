/*
 * Copyright 2012 essendi it GmbH
 * Copyright 2014-2017 Vaadin Ltd.
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

package org.vaadin.ui;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import org.vaadin.ui.shared.numberfield.NumberFieldState;
import org.vaadin.ui.shared.numberfield.NumberValidator;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.data.Result;
import com.vaadin.data.ValueContext;
import com.vaadin.data.converter.StringToDoubleConverter;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.server.ErrorMessage;
import com.vaadin.server.UserError;
import com.vaadin.shared.Registration;
import com.vaadin.shared.communication.FieldRpc.FocusAndBlurServerRpc;
import com.vaadin.shared.ui.textfield.AbstractTextFieldServerRpc;
import com.vaadin.ui.TextField;

import elemental.json.Json;

/**
 * <p>
 * Provides a numeric field with automatic keystroke filtering and validation
 * for integer (123) and decimal numbers (12.3). The minus sign and
 * user-definable grouping and decimal separators are supported.
 * </p>
 * <p>
 * Inputs are validated on client- <b>and</b> server-side. The client-side
 * validator gets active on every keypress in the field. If the keypress would
 * lead to an invalid value, it is suppressed and the value remains unchanged.
 * The server-side validation is triggered at {@link ValueChangeEvent}, see
 * {@link #addServerSideValidation()} for more details. To omit server-side
 * validation (which is enabled per default), you can call
 * {@link #removeServerSideValidation()}.
 * </p>
 * <p>
 * A user-entered value is formatted automatically when the field's focus is
 * lost. {@code NumberField} uses {@link DecimalFormat} for formatting and
 * sending the formatted value of the input back to client. There's a number of
 * setters to define the format, see the code example below for a general view.
 * </p>
 * <p>
 * Some features in an usage example: <blockquote>
 *
 * <pre>
 * NumberField numField = new NumberField(); // NumberField extends TextField
 * numField.setDecimalAllowed(true); // not just integers (by default, decimals
 *                                   // are allowed)
 * numField.setDecimalPrecision(2); // maximum 2 digits after the decimal
 *                                  // separator
 * numField.setDecimalSeparator(','); // e.g. 1,5
 * numField.setDecimalSeparatorAlwaysShown(true); // e.g. 12345 -&gt; 12345,
 * numField.setMinimumFractionDigits(2); // e.g. 123,4 -&gt; 123,40
 * numField.setGroupingUsed(true); // use grouping (e.g. 12345 -&gt; 12.345)
 * numField.setGroupingSeparator('.'); // use '.' as grouping separator
 * numField.setGroupingSize(3); // 3 digits between grouping separators:
 *                              // 12.345.678
 * numField.setMinValue(0); // valid values must be &gt;= 0 ...
 * numField.setMaxValue(999.9); // ... and &lt;= 999.9
 * numField.setErrorText(&quot;Invalid number format!&quot;); // feedback message on bad
 *                                                  // input
 * numField.setNegativeAllowed(false); // prevent negative numbers (defaults to
 *                                     // true)
 * numField.setValueIgnoreReadOnly(&quot;10&quot;); // set the field's value, regardless
 *                                        // of whether it is read-only or not
 * numField.removeValidator(); // omit server-side validation
 * </pre>
 *
 * </blockquote>
 */
@StyleSheet("numberfield.css")
@SuppressWarnings("serial")
public class NumberField extends TextField {

    private final class NumberFieldServerRpcImpl
            implements AbstractTextFieldServerRpc {

        @Override
        public void setText(String text, int cursorPosition) {
            if (text != null) {
                try {
                    synchronized (decimalFormat) {
                        setDecimalFormatToNumberFieldAttributes();
                        if (text.trim().equals("")) {
                            text = "";
                        } else {
                            Number valueAsNumber = decimalFormat.parse(text);
                            text = valueAsNumber.toString();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            updateDiffstate("text", Json.create(text));

            lastKnownCursorPosition = cursorPosition;
            setValue(text, true);
        }
    }

    private final class NumberFieldFocusAndBlurRpcImpl
            implements FocusAndBlurServerRpc {
        @Override
        public void blur() {
            fireEvent(new BlurEvent(NumberField.this));
        }

        @Override
        public void focus() {
            fireEvent(new FocusEvent(NumberField.this));
        }
    }

    // Server-side validation
    private ValueChangeListener<String> numberValidator;
    private Registration numberValidatorRegistration;

    // All NumberField instances share the same formatter and converter
    private static DecimalFormat decimalFormat = new DecimalFormat();
    private static StringToDoubleConverter converter;

    // Settings needed only on server-side and therefore not part of
    // NumberFieldAttributes
    private String errorText = "Invalid number";
    private int groupingSize;
    private int minimumFractionDigits;
    private boolean decimalSeparatorAlwaysShown;

    private int lastKnownCursorPosition = -1;

    /**
     * <p>
     * Constructs an empty {@code NumberField} with no caption. The field has
     * built-in server-side validation (see {@link #addServerSideValidation()});
     * this is necessary for the validation of the field to occur immediately
     * when the input focus changes.
     * </p>
     * <p>
     * The decimal/grouping separator defaults to the user's local
     * decimal/grouping separator. Grouping (e.g. from 12345 to 12.345) is
     * enabled, the maximum precision to display after the decimal separator is
     * set to 2 per default.
     * </p>
     */
    public NumberField() {
        registerRpc(new NumberFieldServerRpcImpl());
        registerRpc(new NumberFieldFocusAndBlurRpcImpl());
        setDefaultValues();
        addValueChangeListener(e -> updateFormattedValue());
    }

    @Override
    public int getCursorPosition() {
        return lastKnownCursorPosition;
    }

    /**
     * <p>
     * Constructs an empty {@code NumberField} with given caption. The field has
     * server-side validation enabled by default (see
     * {@link #addServerSideValidation()}).
     * </p>
     * <p>
     * The decimal/grouping separator defaults to the user's local
     * decimal/grouping separator. Grouping (e.g. from 12345 to 12.345) is
     * enabled, the maximum precision to display after the decimal separator is
     * set to 2 per default.
     * </p>
     *
     * @param caption
     *            Sets the component's caption {@code String}.
     */
    public NumberField(String caption) {
        this();
        setCaption(caption);
    }

    @Override
    protected NumberFieldState getState() {
        return (NumberFieldState) super.getState();
    }

    @Override
    protected NumberFieldState getState(boolean markAsDirty) {
        return (NumberFieldState) super.getState(markAsDirty);
    }

    private void setDefaultValues() {
        setGroupingUsed(true);
        groupingSize = 3;
        setDecimalPrecision(2);
        minimumFractionDigits = 0;
        decimalSeparatorAlwaysShown = false;
        addServerSideValidation();

        // Set the decimal/grouping separator to the user's default locale
        getState().setDecimalSeparator(
                DecimalFormatSymbols.getInstance().getDecimalSeparator());
        getState().setGroupingSeparator(
                DecimalFormatSymbols.getInstance().getGroupingSeparator());

        // Member "attributes" defines some defaults as well!
    }

    /**
     * Returns a default converter for data binding needs. This converter always
     * uses US locale, because localised internal values break validation.
     *
     * NOTE: This converter isn't used by default, you need to add it to your
     * Binder specifically!
     *
     * @return converter
     */
    public static StringToDoubleConverter getConverter(String errorMessage) {
        if (converter == null) {
            converter = new StringToDoubleConverter(errorMessage) {
                @Override
                protected NumberFormat getFormat(Locale locale) {
                    return super.getFormat(Locale.US);
                }

                @Override
                public Result<Double> convertToModel(String value,
                        ValueContext context) {
                    return super.convertToModel(value, context);
                }

                @Override
                public String convertToPresentation(Double value,
                        ValueContext context) {
                    String result = super.convertToPresentation(value, context);
                    if (result != null) {
                        // remove thousand groupings
                        result = result.replaceAll(",", "");
                    }
                    return result;
                }
            };
        }
        return converter;
    }

    private void createNumberValidator() {
        // Create our server-side validator
        numberValidator = new ValueChangeListener<String>() {

            @Override
            public void valueChange(ValueChangeEvent<String> event) {
                if (validateValue(event.getValue())) {
                    ErrorMessage componentError = getComponentError();
                    if (componentError instanceof UserError) {
                        String message = ((UserError) componentError).getMessage();
                        if ((errorText != null && errorText.equals(message))
                                || (errorText == null && message == null)) {
                            setComponentError(null);
                        }
                    }
                } else {
                    setComponentError(new UserError(errorText));
                }
            }
        };
    }

    private boolean validateValue(String value) {
        if (value == null || "".equals(value)) {
            return !isRequiredIndicatorVisible();
        }

        final boolean isValid;
        if (isDecimalAllowed()) {
            isValid = NumberValidator.isValidDecimal(value,
                    getState(), false);
        } else {
            isValid = NumberValidator.isValidInteger(value,
                    getState(), false);
        }

        return isValid;
    }

    private void updateFormattedValue() {
        getState().formattedValue = getValueAsFormattedDecimalNumber();
    }

    private String getValueAsFormattedDecimalNumber() {
        String value = getValue();
        if (value == null || "".equals(value)) {
            return "";
        }

        try {
            synchronized (decimalFormat) {
                setDecimalFormatToNumberFieldAttributes();
                // Number valueAsNumber = decimalFormat.parse(value);
                Number valueAsNumber = Double.valueOf(value.toString());
                return decimalFormat.format(valueAsNumber);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return value;
        }
    }

    private void setDecimalFormatToNumberFieldAttributes() {
        synchronized (decimalFormat) {
            decimalFormat.setGroupingUsed(getState().isGroupingUsed());
            decimalFormat.setGroupingSize(groupingSize);
            decimalFormat.setMinimumFractionDigits(minimumFractionDigits);
            decimalFormat
                    .setDecimalSeparatorAlwaysShown(decimalSeparatorAlwaysShown);

            // Adapt decimal format symbols
            DecimalFormatSymbols symbols = decimalFormat
                    .getDecimalFormatSymbols();
            symbols.setDecimalSeparator(getState().getDecimalSeparator());
            symbols.setGroupingSeparator(getState().getGroupingSeparator());
            decimalFormat.setDecimalFormatSymbols(symbols);
        }
    }

    /**
     * <p>
     * Adds server-side validation to the field. The validation (is it a valid
     * integer/decimal number?) provides feedback about bad input. If the input
     * is recognised as invalid the field is marked as invalid. The error
     * message can be set with {@link #setErrorText(String)}.
     * </p>
     */
    public void addServerSideValidation() {
        if (numberValidator == null) {
            createNumberValidator();
        }
        numberValidatorRegistration = super.addValueChangeListener(
                numberValidator);
    }

    /**
     * Removes the default validation from the field.
     *
     * @see #addServerSideValidation()
     */
    public void removeServerSideValidation() {
        if (numberValidatorRegistration != null) {
            numberValidatorRegistration.remove();
        }
    }

    /**
     * @param newValue
     *            Sets the value of the field to a double.
     */
    public void setValue(Double newValue) {
        if (newValue == null) {
            super.setValue(null);
        } else {
            // Use BigDecimal to avoid scientific notation and thus allow
            // NumberValidator to work properly
            String noExponent = BigDecimal.valueOf(newValue).toPlainString();

            super.setValue(noExponent);
        }
    }

    /**
     * Sets the value of the field, regardless whether the field is in read-only
     * mode.
     *
     * @param newValue
     *            The field's new String value.
     */
    public void setValueIgnoreReadOnly(String newValue) {
        boolean wasReadOnly = isReadOnly();
        if (isReadOnly()) {
            setReadOnly(false);
        }
        setValue(newValue);
        if (wasReadOnly) {
            setReadOnly(true);
        }
    }

    /**
     * Sets the value of the field, regardless whether the field is in read-only
     * mode.
     *
     * @param newValue
     *            The field's new Double value.
     */
    public void setValueIgnoreReadOnly(Double newValue) {
        boolean wasReadOnly = isReadOnly();
        if (isReadOnly()) {
            setReadOnly(false);
        }
        setValue(newValue);
        if (wasReadOnly) {
            setReadOnly(true);
        }
    }

    /**
     * See {@link NumberFieldState#setDecimalAllowed(boolean)}.
     * <p>
     * As a side-effect, the minimum number of digits allowed in the fraction
     * portion of a number is set to 0 if decimalAllowed is false (so that
     * {@link DecimalFormat} does the right thing).
     * </p>
     *
     * @param decimalAllowed
     *            {@code true} if decimal should be allowed
     */
    public void setDecimalAllowed(boolean decimalAllowed) {
        getState().setDecimalAllowed(decimalAllowed);
        if (!decimalAllowed) {
            setMinimumFractionDigits(0);
        }
    }

    /**
     * See {@link NumberFieldState#isDecimalAllowed()}.
     *
     * @return {@code true} if decimal is allowed
     */
    public boolean isDecimalAllowed() {
        return getState(false).isDecimalAllowed();
    }

    /**
     * @param text
     *            The error text to display in case of an invalid field value.<br>
     *            Caution: If the argument is "" or {@code null}, the field
     *            won't be recognisable as invalid!
     */
    public void setErrorText(String text) {
        errorText = text;
    }

    /**
     * @return The error text displayed in case of an invalid field value.
     */
    public String getErrorText() {
        return errorText;
    }

    /**
     * See {@link NumberFieldState#isGroupingUsed()}.
     *
     * @return {@code true} if grouping is used
     */
    public boolean isGroupingUsed() {
        return getState(false).isGroupingUsed();
    }

    /**
     * See {@link NumberFieldState#setGroupingUsed(boolean)}.
     *
     * @param useGrouping
     *            {@code true} to use grouping
     */
    public void setGroupingUsed(boolean useGrouping) {
        getState().setGroupingUsed(useGrouping);
    }

    /**
     * See {@link DecimalFormat#getGroupingSize()}.
     *
     * @return grouping size
     */
    public int getGroupingSize() {
        return groupingSize;
    }

    /**
     * See {@link DecimalFormat#setGroupingSize(int)}.
     *
     * @param groupingSize
     *            grouping size
     */
    public void setGroupingSize(int groupingSize) {
        this.groupingSize = groupingSize;
        updateFormattedValue();
    }

    /**
     * See {@link DecimalFormat#getMinimumFractionDigits()}.
     *
     * @return minimum allowed digits in fraction portion of a number
     */
    public int getMinimumFractionDigits() {
        return minimumFractionDigits;
    }

    /**
     * See {@link DecimalFormat#setMinimumFractionDigits(int)}.
     *
     * @param minimumDigits
     *            minimum allowed digits in fraction portion of a number
     */
    public void setMinimumFractionDigits(int minimumDigits) {
        minimumFractionDigits = minimumDigits;
        updateFormattedValue();
    }

    /**
     * See {@link DecimalFormat#isDecimalSeparatorAlwaysShown()}.
     *
     * @return {@code true} if decimal separator is always shown
     */
    public boolean isDecimalSeparatorAlwaysShown() {
        return decimalSeparatorAlwaysShown;
    }

    /**
     * See {@link DecimalFormat#setDecimalSeparatorAlwaysShown(boolean)}.
     *
     * @param showAlways
     *            {@code true} if decimal separator should be always shown
     */
    public void setDecimalSeparatorAlwaysShown(boolean showAlways) {
        decimalSeparatorAlwaysShown = showAlways;
        updateFormattedValue();
    }

    /**
     * See {@link NumberFieldState#getDecimalPrecision()}.
     *
     * @return maximum precision after decimal separator
     */
    public int getDecimalPrecision() {
        return getState(false).getDecimalPrecision();
    }

    /**
     * See {@link NumberFieldState#setDecimalPrecision(int)}.
     *
     * @param maximumDigits
     *            maximum precision after decimal separator
     */
    public void setDecimalPrecision(int maximumDigits) {
        getState().setDecimalPrecision(maximumDigits);
    }

    /**
     * See {@link NumberFieldState#getDecimalSeparator()}.
     *
     * @return decimal separator
     */
    public char getDecimalSeparator() {
        return getState(false).getDecimalSeparator();
    }

    /**
     * See {@link NumberFieldState#getEscapedDecimalSeparator()}.
     *
     * @return decimal separator formatted for regular expression characters
     */
    public String getEscapedDecimalSeparator() {
        return getState(false).getEscapedDecimalSeparator();
    }

    /**
     * Sets the decimal separator. If the field has a value containing the old
     * decimal separator, it is replaced with the new one.
     *
     * @param newSeparator
     *            decimal separator
     */
    public void setDecimalSeparator(char newSeparator) {
        replaceSeparatorInField(getDecimalSeparator(), newSeparator);
        getState().setDecimalSeparator(newSeparator);
    }

    private void replaceSeparatorInField(char oldSeparator, char newSeparator) {
        String value = getValue();
        if (value != null) {
            String valueWithReplacedSeparator = value.replace(oldSeparator,
                    newSeparator);
            setValue(valueWithReplacedSeparator);
        }
    }

    /**
     * See {@link NumberFieldState#getGroupingSeparator()}.
     *
     * @return grouping separator
     */
    public char getGroupingSeparator() {
        return getState(false).getGroupingSeparator();
    }

    /**
     * See {@link NumberFieldState#getEscapedGroupingSeparator()}.
     *
     * @return grouping separator formatted for regular expression characters
     */
    public String getEscapedGroupingSeparator() {
        return getState(false).getEscapedGroupingSeparator();
    }

    /**
     * Sets the grouping separator. If the field has a value containing the old
     * grouping separator, it is replaced with the new one.
     *
     * @param newSeparator
     *            grouping separator
     */
    public void setGroupingSeparator(char newSeparator) {
        replaceSeparatorInField(getGroupingSeparator(), newSeparator);
        getState().setGroupingSeparator(newSeparator);
    }

    /**
     * See {@link NumberFieldState#getMinValue()}.
     *
     * @return minimum allowed value
     */
    public double getMinValue() {
        return getState(false).getMinValue();
    }

    /**
     * See {@link NumberFieldState#setMinValue(double)}.
     *
     * @param minValue
     *            minimum allowed value
     */
    public void setMinValue(double minValue) {
        getState().setMinValue(minValue);
    }

    /**
     * See {@link NumberFieldState#getMaxValue()}.
     *
     * @return maximum allowed value
     */
    public double getMaxValue() {
        return getState(false).getMaxValue();
    }

    /**
     * See {@link NumberFieldState#setMaxValue(double)}.
     *
     * @param maxValue
     *            maximum allowed value
     */
    public void setMaxValue(double maxValue) {
        getState().setMaxValue(maxValue);
    }

    /**
     * See {@link NumberFieldState#isNegativeAllowed()}.
     *
     * @return {@code true} if negative values are allowed
     */
    public boolean isNegativeAllowed() {
        return getState(false).isNegativeAllowed();
    }

    /**
     * See {@link NumberFieldState#setNegativeAllowed(boolean)}.
     *
     * @param negativeAllowed
     *            {@code true} if negative values should be allowed
     */
    public void setNegativeAllowed(boolean negativeAllowed) {
        getState().setNegativeAllowed(negativeAllowed);
    }

    /**
     * @return The field's value as a double value. If the field contains no
     *         parsable number, 0.0 is returned.
     */
    public double getDoubleValueDoNotThrow() {
        try {
            return Double.valueOf(getValueNonLocalized());
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * @return The field's value as a string representation of a decimal number
     *         with '.' as decimal separator and removed grouping separators.
     *         Example: If the field's value is "2.546,99", this method will
     *         return "2546.99". If the field is empty, "0" is returned.
     */
    public String getValueNonLocalized() {
        String value = getValue();
        if (value == null || "".equals(value)) {
            return "0";
        }
        String groupingSeparator = String.valueOf(getGroupingSeparator());
        return value.replace(groupingSeparator, "").replace(
                getDecimalSeparator(), '.');
    }

    /**
     * If the given value is the string representation of a decimal number with
     * '.' as decimal separator (e.g. 12345.67), this method will return the
     * value with replaced decimal separator as it was set with
     * {@link #setDecimalSeparator(char)}.
     *
     * @param value
     *            value
     * @return updated value
     */
    public String replacePointWithDecimalSeparator(String value) {
        return value.replace('.', getDecimalSeparator());
    }

    public String getFormattedValue() {
        return getValueAsFormattedDecimalNumber();
    }

}
