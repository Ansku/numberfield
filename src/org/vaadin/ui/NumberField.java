/*
 * Copyright 2012 essendi it GmbH
 * Copyright 2014 Vaadin Ltd.
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
import java.util.HashMap;
import java.util.Map;

import org.vaadin.ui.shared.numberfield.Constants;
import org.vaadin.ui.shared.numberfield.NumberFieldAttributes;
import org.vaadin.ui.shared.numberfield.NumberValidator;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.server.PaintException;
import com.vaadin.server.PaintTarget;
import com.vaadin.ui.TextField;

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
 * lost. <code>NumberField</code> uses {@link DecimalFormat} for formatting and
 * send the formatted value of the input back to client. There's a number of
 * setters to define the format, see the code example below for a general view.
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
 * </p>
 */
@StyleSheet("numberfield.css")
@SuppressWarnings("serial")
public class NumberField extends TextField {

    // Server-side validator
    private Validator numberValidator;

    // All NumberField instances share the same formatter
    private static DecimalFormat decimalFormat = new DecimalFormat();

    // Settings needed both on server- and client-side
    private NumberFieldAttributes attributes = new NumberFieldAttributes();

    // Settings needed only on server-side and therefore not part of
    // NumberFieldAttributes
    private String errorText = "Invalid number";
    private int groupingSize;
    private int minimumFractionDigits;
    private boolean decimalSeparatorAlwaysShown;

    /**
     * <p>
     * Constructs an empty <code>NumberField</code> with no caption. The field
     * is bound to a server-side validator (see
     * {@link #addServerSideValidator()}) and immediate mode is set to true;
     * this is necessary for the validation of the field to occur immediately
     * when the input focus changes.
     * </p>
     * <p>
     * The decimal/grouping separator defaults to the user's local
     * decimal/grouping separator. Grouping (e.g. 12345 -> 12.345) is enabled,
     * the maximum precision to display after the decimal separator is set to 2
     * per default.
     * </p>
     */
    public NumberField() {
        super();
        setImmediate(true);
        setDefaultValues();
    }

    /**
     * <p>
     * Constructs an empty <code>NumberField</code> with given caption. The
     * field is bound to a server-side validator (see
     * {@link #addServerSideValidator()}) and immediate mode is set to true;
     * this is necessary for the validation of the field to occur immediately
     * when the input focus changes.
     * </p>
     * <p>
     * The decimal/grouping separator defaults to the user's local
     * decimal/grouping separator. Grouping (e.g. 12345 -> 12.345) is enabled,
     * the maximum precision to display after the decimal separator is set to 2
     * per default.
     * </p>
     *
     * @param caption
     *            Sets the component's caption {@code String}.
     */
    public NumberField(String caption) {
        this();
        setCaption(caption);
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
        attributes.setDecimalSeparator(DecimalFormatSymbols.getInstance()
                .getDecimalSeparator());
        attributes.setGroupingSeparator(DecimalFormatSymbols.getInstance()
                .getGroupingSeparator());

        // Member "attributes" defines some defaults as well!
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

        if (!(value instanceof String)) {
            return false;
        }

        final boolean isValid;
        if (isDecimalAllowed()) {
            isValid = NumberValidator.isValidDecimal((String) value,
                    attributes, false);
        } else {
            isValid = NumberValidator.isValidInteger((String) value,
                    attributes, false);
        }

        return isValid;
    }

    /**
     * Validates the field's value according to the validation rules determined
     * with the setters of this class (e.g. {@link #setDecimalAllowed(boolean)}
     * or {@link #setMaxValue(double)}).
     *
     * @return True, if validation succeeds; false, if validation fails.
     */
    @Override
    public boolean isValid() {
        return validateValue(getValue());
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);

        // Paint any component specific content by setting attributes.
        // These attributes can be read in the widget's updateFromUIDL().
        // paintContent() is called when the contents of the component should be
        // painted in response to the component first being shown or having been
        // altered so that its visual representation is changed.
        target.addAttribute(Constants.ATTRIBUTE_ALLOW_DECIMALS,
                isDecimalAllowed());
        target.addAttribute(Constants.ATTRIBUTE_ALLOW_NEGATIVES,
                isNegativeAllowed());
        target.addAttribute(Constants.ATTRIBUTE_DECIMAL_PRECISION,
                getDecimalPrecision());
        target.addAttribute(Constants.ATTRIBUTE_DECIMAL_SEPARATOR,
                getDecimalSeparator());
        target.addAttribute(Constants.ATTRIBUTE_USE_GROUPING, isGroupingUsed());
        target.addAttribute(Constants.ATTRIBUTE_GROUPING_SEPARATOR,
                getGroupingSeparator());
        target.addAttribute(Constants.ATTRIBUTE_MIN_VALUE, getMinValue());
        target.addAttribute(Constants.ATTRIBUTE_MAX_VALUE, getMaxValue());

        // Use DecimalFormat to format the user-input and send the
        // formatted value back to client.
        target.addAttribute(Constants.ATTRIBUTE_SERVER_FORMATTED_VALUE,
                getValueAsFormattedDecimalNumber());
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
                        variables.put("text", null);
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
            decimalFormat.setGroupingUsed(attributes.isGroupingUsed());
            decimalFormat.setGroupingSize(groupingSize);
            decimalFormat.setMinimumFractionDigits(minimumFractionDigits);
            decimalFormat
                    .setDecimalSeparatorAlwaysShown(decimalSeparatorAlwaysShown);

            // Adapt decimal format symbols
            DecimalFormatSymbols symbols = decimalFormat
                    .getDecimalFormatSymbols();
            symbols.setDecimalSeparator(attributes.getDecimalSeparator());
            symbols.setGroupingSeparator(attributes.getGroupingSeparator());
            decimalFormat.setDecimalFormatSymbols(symbols);
        }
    }

    /**
     * <p>
     * Binds the field to a server-side validator. The validation (is it a valid
     * integer/decimal number?) provides feedback about bad input. If the input
     * is recognized as invalid, an {@link InvalidValueException} is thrown,
     * which reports an error message and mark the field as invalid. The error
     * message can be set with {@link #setErrorText()}.
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
     * See {@link NumberFieldAttributes#setDecimalAllowed(boolean)}.
     * <p>
     * As a side-effect, the minimum number of digits allowed in the fraction
     * portion of a number is set to 0 if decimalAllowed is false (so that
     * {@link DecimalFormat} does the right thing).
     * </p>
     */
    public void setDecimalAllowed(boolean decimalAllowed) {
        attributes.setDecimalAllowed(decimalAllowed);
        if (!decimalAllowed) {
            setMinimumFractionDigits(0);
        }
        markAsDirty();
    }

    /**
     * See {@link NumberFieldAttributes#isDecimalAllowed()}.
     */
    public boolean isDecimalAllowed() {
        return attributes.isDecimalAllowed();
    }

    /**
     * @param text
     *            The error text to display in case of an invalid field value.<br/>
     *            Caution: If the argument is "" or <code>null</code>, the field
     *            won't be recognizable as invalid!
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
     * See {@link NumberFieldAttributes#isGroupingUsed()}.
     */
    public boolean isGroupingUsed() {
        return attributes.isGroupingUsed();
    }

    /**
     * See {@link NumberFieldAttributes#setGroupingUsed(boolean)}.
     */
    public void setGroupingUsed(boolean group) {
        attributes.setGroupingUsed(group);
        markAsDirty();
    }

    /**
     * See {@link DecimalFormat#getGroupingSize()}.
     */
    public int getGroupingSize() {
        return groupingSize;
    }

    /**
     * See {@link DecimalFormat#setGroupingSize(int)}.
     */
    public void setGroupingSize(int groupingSize) {
        this.groupingSize = groupingSize;
        markAsDirty();
    }

    /**
     * See {@link DecimalFormat#getMinimumFractionDigits()}.
     */
    public int getMinimumFractionDigits() {
        return minimumFractionDigits;
    }

    /**
     * See {@link DecimalFormat#setMinimumFractionDigits(int)}.
     */
    public void setMinimumFractionDigits(int minimumDigits) {
        minimumFractionDigits = minimumDigits;
        markAsDirty();
    }

    /**
     * See {@link DecimalFormat#isDecimalSeparatorAlwaysShown()}.
     */
    public boolean isDecimalSeparatorAlwaysShown() {
        return decimalSeparatorAlwaysShown;
    }

    /**
     * See {@link DecimalFormat#setDecimalSeparatorAlwaysShown(boolean)}.
     */
    public void setDecimalSeparatorAlwaysShown(boolean showAlways) {
        decimalSeparatorAlwaysShown = showAlways;
        markAsDirty();
    }

    /**
     * See {@link NumberFieldAttributes#getDecimalPrecision()}.
     */
    public int getDecimalPrecision() {
        return attributes.getDecimalPrecision();
    }

    /**
     * See {@link NumberFieldAttributes#setDecimalPrecision(int)}.
     */
    public void setDecimalPrecision(int maximumDigits) {
        attributes.setDecimalPrecision(maximumDigits);
        markAsDirty();
    }

    /**
     * See {@link NumberFieldAttributes#getDecimalSeparator()}.
     */
    public char getDecimalSeparator() {
        return attributes.getDecimalSeparator();
    }

    /**
     * See {@link NumberFieldAttributes#getEscapedDecimalSeparator()}.
     */
    public String getEscapedDecimalSeparator() {
        return attributes.getEscapedDecimalSeparator();
    }

    /**
     * Sets the decimal separator. If the field has a value containing the old
     * decimal separator, it is replaced with the new one.
     */
    public void setDecimalSeparator(char newSeparator) {
        replaceSeparatorInField(getDecimalSeparator(), newSeparator);
        attributes.setDecimalSeparator(newSeparator);
        markAsDirty();
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
     * See {@link NumberFieldAttributes#getGroupingSeparator()}.
     */
    public char getGroupingSeparator() {
        return attributes.getGroupingSeparator();
    }

    /**
     * See {@link NumberFieldAttributes#getEscapedGroupingSeparator()}.
     */
    public String getEscapedGroupingSeparator() {
        return attributes.getEscapedGroupingSeparator();
    }

    /**
     * Sets the grouping separator. If the field has a value containing the old
     * grouping separator, it is replaced with the new one.
     */
    public void setGroupingSeparator(char newSeparator) {
        replaceSeparatorInField(getGroupingSeparator(), newSeparator);
        attributes.setGroupingSeparator(newSeparator);
        markAsDirty();
    }

    /**
     * See {@link NumberFieldAttributes#getMinValue()}.
     */
    public double getMinValue() {
        return attributes.getMinValue();
    }

    /**
     * See {@link NumberFieldAttributes#setMinValue(double)}.
     */
    public void setMinValue(double minValue) {
        attributes.setMinValue(minValue);
        markAsDirty();
    }

    /**
     * See {@link NumberFieldAttributes#getMaxValue()}.
     */
    public double getMaxValue() {
        return attributes.getMaxValue();
    }

    /**
     * See {@link NumberFieldAttributes#setMaxValue(double)}.
     */
    public void setMaxValue(double maxValue) {
        attributes.setMaxValue(maxValue);
        markAsDirty();
    }

    /**
     * See {@link NumberFieldAttributes#isNegativeAllowed()}.
     */
    public boolean isNegativeAllowed() {
        return attributes.isNegativeAllowed();
    }

    /**
     * See {@link NumberFieldAttributes#setNegativeAllowed(boolean)}.
     */
    public void setNegativeAllowed(boolean negativeAllowed) {
        attributes.setNegativeAllowed(negativeAllowed);
        markAsDirty();
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
     * value with replaced decimal seperator as it was set with
     * {@link #setDecimalSeparator(char)}.
     */
    public String replacePointWithDecimalSeparator(String value) {
        return value.replace('.', getDecimalSeparator());
    }

    public String getFormattedValue() {
        return getValueAsFormattedDecimalNumber();
    }

}
