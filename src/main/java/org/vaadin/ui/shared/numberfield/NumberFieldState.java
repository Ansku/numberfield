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

package org.vaadin.ui.shared.numberfield;

import com.vaadin.shared.ui.textfield.AbstractTextFieldState;

/**
 * Data holder class for the properties of a {@code NumberField} instance.
 */
@SuppressWarnings("serial")
public class NumberFieldState extends AbstractTextFieldState {

    private boolean negativesAllowed = true;
    private boolean decimalsAllowed = true;
    private boolean useGrouping = true;
    private double minValue = Double.NEGATIVE_INFINITY;
    private double maxValue = Double.POSITIVE_INFINITY;

    private char decimalSeparator;
    private char groupingSeparator;
    private boolean decimalSeparatorAlwaysShown;
    private int minimumFractionDigits;
    private int maximumFractionDigits;
    private int groupingSize;


    public String formattedValue;

    /**
     * @return False to prevent negative numbers, otherwise true.
     */
    public boolean isNegativeAllowed() {
        return negativesAllowed;
    }

    /**
     * @param negativeAllowed False to prevent negative numbers (defaults to true).
     */
    public void setNegativeAllowed(boolean negativeAllowed) {
        negativesAllowed = negativeAllowed;
    }

    /**
     * @param decimalAllowed Sets whether decimal values are allowed (defaults to true).
     */
    public void setDecimalAllowed(boolean decimalAllowed) {
        decimalsAllowed = decimalAllowed;
    }

    /**
     * @return True, if decimal values are allowed.
     */
    public boolean isDecimalAllowed() {
        return decimalsAllowed;
    }

    /**
     * @param useGrouping True to use grouping (e.g. 12345 -&gt; 12.345).
     */
    public void setGroupingUsed(boolean useGrouping) {
        this.useGrouping = useGrouping;
    }

    /**
     * @return True, if grouping is used (e.g. 12345 -&gt; 12.345).
     */
    public boolean isGroupingUsed() {
        return useGrouping;
    }

//    /**
//     * @return The maximum precision to display after the decimal separator.
//     */
//    public int getDecimalPrecision() {
//        return decimalPrecision;
//    }
//
//    /**
//     * @param decimalPrecision
//     *            The maximum precision to display after the decimal separator
//     *            (must be in [1,16]).
//     */
//    public void setDecimalPrecision(int decimalPrecision) {
//        if (decimalPrecision >= 1 && decimalPrecision <= 16) {
//            this.decimalPrecision = decimalPrecision;
//        }
//    }

    /**
     * @return The decimal separator (e.g. ',').
     */
    public char getDecimalSeparator() {
        return decimalSeparator;
    }

    /**
     * @return The decimal separator (quoted, if it is a regular expression
     * character, e.g. "\.").
     */
    public String getEscapedDecimalSeparator() {
        return escapeMetacharactersFromRegExp(String.valueOf(decimalSeparator));
    }

    /**
     * Quotes regular expression characters. The special regular expression
     * characters are: /.+*?^$|\\()[]{}
     *
     * @param toQuote The input string.
     * @return The quoted string.
     */
    private String escapeMetacharactersFromRegExp(String toQuote) {
        String regExp = "([\\[\\]\\/.+*?^$|(){}])";
        return toQuote.replaceAll(regExp, "\\\\$1");
    }

    /**
     * @param decimalSeparator The decimal separator.
     */
    public void setDecimalSeparator(char decimalSeparator) {
        this.decimalSeparator = decimalSeparator;
    }

    /**
     * @return The grouping (thousands) separator.
     */
    public char getGroupingSeparator() {
        return groupingSeparator;
    }

    /**
     * @return The grouping (thousands) separator (quoted, if it is a regular
     * expression character, e.g. "\.").
     */
    public String getEscapedGroupingSeparator() {
        return escapeMetacharactersFromRegExp(String.valueOf(groupingSeparator));
    }

    /**
     * @param groupingSeparator The grouping (thousands) separator.
     */
    public void setGroupingSeparator(char groupingSeparator) {
        this.groupingSeparator = groupingSeparator;
    }

    /**
     * @return The minimum allowed value.
     */
    public double getMinValue() {
        return minValue;
    }

    /**
     * @param minValue The minimum allowed value (defaults to
     *                 {@link Double#NEGATIVE_INFINITY}).
     */
    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

    /**
     * @return The maximum allowed value.
     */
    public double getMaxValue() {
        return maxValue;
    }

    /**
     * @param maxValue The maximum allowed value (defaults to
     *                 {@link Double#POSITIVE_INFINITY}).
     */
    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    public boolean isDecimalSeparatorAlwaysShown() {
        return decimalSeparatorAlwaysShown;
    }

    public void setDecimalSeparatorAlwaysShown(boolean decimalSeparatorAlwaysShown) {
        this.decimalSeparatorAlwaysShown = decimalSeparatorAlwaysShown;
    }

    public int getMinimumFractionDigits() {
        return minimumFractionDigits;
    }

    public void setMinimumFractionDigits(int minimumFractionDigits) {
        this.minimumFractionDigits = minimumFractionDigits;
    }

    public int getMaximumFractionDigits() {
        return maximumFractionDigits;
    }

    public void setMaximumFractionDigits(int maximumFractionDigits) {
        this.maximumFractionDigits = maximumFractionDigits;
    }

    public int getGroupingSize() {
        return groupingSize;
    }

    public void setGroupingSize(int groupingSize) {
        this.groupingSize = groupingSize;
    }
}
