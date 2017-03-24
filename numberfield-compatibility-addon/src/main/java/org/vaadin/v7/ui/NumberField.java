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

package org.vaadin.v7.ui;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.vaadin.v7.ui.shared.numberfield.NumberFieldState;
import org.vaadin.v7.ui.shared.numberfield.NumberValidator;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.v7.data.Validator;
import com.vaadin.v7.data.Validator.InvalidValueException;
import com.vaadin.v7.data.util.converter.Converter.ConversionException;
import com.vaadin.v7.data.util.converter.StringToDoubleConverter;
import com.vaadin.v7.ui.TextField;

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
 * The server-side validation is triggered when the field loses focus, see
 * {@link #addServerSideValidator()} for more details. To omit server-side
 * validation (which is enabled per default), you can call
 * {@link #removeServerSideValidator()}.
 * </p>
 * <p>
 * A user-entered value is formatted automatically when the field's focus is
 * lost. {@code NumberField} uses {@link DecimalFormat} for formatting and send
 * the formatted value of the input back to client. There's a number of setters
 * to define the format, see the code example below for a general view.
 * </p>
 * <p>
 * Some features in an usage example: <blockquote>
 *
 * <pre>
 * NumberField numField = new NumberField(); // NumberField extends TextField
 * numField.setDecimalAllowed(true); // not just integers (by default, decimals are
 * // allowed)
 * numField.setDecimalPrecision(2); // maximum 2 digits after the decimal separator
 * numField.setDecimalSeparator(','); // e.g. 1,5
 * numField.setDecimalSeparatorAlwaysShown(true); // e.g. 12345 -&gt; 12345,
 * numField.setMinimumFractionDigits(2); // e.g. 123,4 -&gt; 123,40
 * numField.setGroupingUsed(true); // use grouping (e.g. 12345 -&gt; 12.345)
 * numField.setGroupingSeparator('.'); // use '.' as grouping separator
 * numField.setGroupingSize(3); // 3 digits between grouping separators: 12.345.678
 * numField.setMinValue(0); // valid values must be &gt;= 0 ...
 * numField.setMaxValue(999.9); // ... and &lt;= 999.9
 * numField.setErrorText(&quot;Invalid number format!&quot;); // feedback message on bad
 * // input
 * numField.setNegativeAllowed(false); // prevent negative numbers (defaults to
 * // true)
 * numField.setValueIgnoreReadOnly(&quot;10&quot;); // set the field's value, regardless
 * // whether it is read-only or not
 * numField.removeValidator(); // omit server-side validation
 * </pre>
 *
 * </blockquote>
 */
@StyleSheet("numberfield.css")
@SuppressWarnings({ "serial", "deprecation" })
public class NumberField extends TextField {

    // Server-side validator
    private Validator numberValidator;

    // All NumberField instances share the same formatter and converter
    private static DecimalFormat decimalFormat = new DecimalFormat();
    private static StringToDoubleConverter converter;

    // Settings needed only on server-side and therefore not part of
    // NumberFieldAttributes
    private String errorText = "Invalid number";
    private int groupingSize;
    private int minimumFractionDigits;
    private boolean decimalSeparatorAlwaysShown;

    /**
     * <p>
     * Constructs an empty {@code NumberField} with no caption. The field is
     * bound to a server-side validator (see {@link #addServerSideValidator()})
     * and immediate mode is set to true; this is necessary for the validation
     * of the field to occur immediately when the input focus changes.
     * </p>
     * <p>
     * The decimal/grouping separator defaults to the user's local
     * decimal/grouping separator. Grouping (e.g. from 12345 to 12.345) is
     * enabled, the maximum precision to display after the decimal separator is
     * set to 2 per default.
     * </p>
     */
    public NumberField() {
        super();
        setDefaultValues();
        addValueChangeListener(e -> {
            updateFormattedValue();
        });
    }

    /**
     * <p>
     * Constructs an empty {@code NumberField} with given caption. The field is
     * bound to a server-side validator (see {@link #addServerSideValidator()})
     * and immediate mode is set to true; this is necessary for the validation
     * of the field to occur immediately when the input focus changes.
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
        addServerSideValidator();
        setNullSettingAllowed(true);

        // Set the decimal/grouping separator to the user's default locale
        getState().setDecimalSeparator(
                DecimalFormatSymbols.getInstance().getDecimalSeparator());
        getState().setGroupingSeparator(
                DecimalFormatSymbols.getInstance().getGroupingSeparator());

        // Member "attributes" defines some defaults as well!
    }

    @Override
    public void setConverter(Class<?> datamodelType) {
        if (datamodelType.isAssignableFrom(Double.class)) {
            if (converter == null) {
                createConverter();
            }
            setConverter(converter);
        } else {
            super.setConverter(datamodelType);
        }
    }

    /**
     * Creates a converter that always uses US Locale. This is necessary because
     * localised internal values break validation.
     *
     * FIXME: This is a quick hack to fix problems with binding. Correct
     * solution would be to change the validation to respect localised values.
     */
    private void createConverter() {
        converter = new StringToDoubleConverter() {
            @Override
            protected NumberFormat getFormat(Locale locale) {
                return super.getFormat(Locale.US);
            }

            @Override
            public Double convertToModel(String value,
                    Class<? extends Double> targetType, Locale locale)
                    throws com.vaadin.v7.data.util.converter.Converter.ConversionException {
                return super.convertToModel(value, targetType, Locale.US);
            }

            @Override
            public String convertToPresentation(Double value,
                    Class<? extends String> targetType, Locale locale)
                    throws com.vaadin.v7.data.util.converter.Converter.ConversionException {
                String result = super.convertToPresentation(value, targetType,
                        Locale.US);
                if (result != null) {
                    // remove thousand groupings
                    result = result.replaceAll(",", "");
                }
                return result;
            }
        };
    }

    private void createNumberValidator() {
        // Create our server-side validator
        numberValidator = new Validator() {

            public boolean isValid(Object value) {
                return validateValue(value);
            }

            @Override
            public void validate(Object value) throws InvalidValueException {
                if (!isValid(value)) {
                    throw new InvalidValueException(errorText);
                }
            }

        };
    }

    private boolean validateValue(Object value) {
        if (value == null || "".equals(value)) {
            return !isRequired();
        }

        // FIXME: This is a hack to get around converters.
        if (value instanceof Double) {
            value = BigDecimal.valueOf((Double) value).toPlainString();
        }

        if (!(value instanceof String)) {
            return false;
        }

        final boolean isValid;
        if (isDecimalAllowed()) {
            isValid = NumberValidator.isValidDecimal((String) value,
                    getState(), false);
        } else {
            isValid = NumberValidator.isValidInteger((String) value,
                    getState(), false);
        }

        return isValid;
    }

    /**
     * Validates the field's value according to the validation rules determined
     * with the setters of this class (e.g. {@link #setDecimalAllowed(boolean)}
     * or {@link #setMaxValue(double)}), as well as any other validation given
     * for the field.
     *
     * @return True, if validation succeeds; false, if validation fails.
     */
    @Override
    public boolean isValid() {
        return super.isValid() && validateValue(getValue());
    }

    private void updateFormattedValue() {
        getState().formattedValue = getValueAsFormattedDecimalNumber();
    }

    private String getValueAsFormattedDecimalNumber() {
        Object value = getValue();
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
            return value.toString();
        }
    }

    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        if (variables.containsKey("text") && !isReadOnly()) {
            // Workaround so that we can modify the values
            variables = new HashMap<String, Object>(variables);
            // Only do the setting if the string representation of the value
            // has been updated
            String newValue = (String) variables.get("text");
            try {
                synchronized (decimalFormat) {
                    setDecimalFormatToNumberFieldAttributes();
                    if (newValue != null && newValue.trim().equals("")) {
                        variables.put("text", "");
                    } else {
                        Number valueAsNumber = decimalFormat.parse(newValue);
                        variables.put("text", valueAsNumber.toString());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        super.changeVariables(source, variables);
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
     * Binds the field to a server-side validator. The validation (is it a valid
     * integer/decimal number?) provides feedback about bad input. If the input
     * is recognised as invalid, an {@link InvalidValueException} is thrown,
     * which reports an error message and mark the field as invalid. The error
     * message can be set with {@link #setErrorText(String)}.
     * </p>
     * <p>
     * To have the validation done immediately when the field loses focus,
     * immediate mode should not be disabled (state is enabled per default).
     * </p>
     */
    public void addServerSideValidator() {
        if (numberValidator == null) {
            createNumberValidator();
        }
        super.addValidator(numberValidator);
    }

    /**
     * Removes the field's validator.
     *
     * @see #addServerSideValidator()
     */
    public void removeServerSideValidator() {
        super.removeValidator(numberValidator);
    }

    /**
     * @param newValue
     *            Sets the value of the field to a double.
     */
    public void setValue(Double newValue) throws ReadOnlyException,
            ConversionException {
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
     *         with '.' as decimal separator and cutted grouping separators.
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
