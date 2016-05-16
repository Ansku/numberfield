package org.vaadin.numberfield;

import com.vaadin.ui.*;
import org.vaadin.addonhelpers.AbstractTest;
import org.vaadin.ui.NumberField;

/**
 * Created by heintz on 13/05/16.
 */
public class NumberFieldTest extends AbstractTest {

    Panel panel = new Panel("Testing number field");
    private NumberField numberField = new NumberField("Number field");
    static Double start = 123456789.6547897321d;

    @Override
    public Component getTestComponent() {
        start = 123456789.12345678901234567890123456789012345678901234567890123456789012345678901234567890d;
        numberField.setMaximumFractionDigits(4);
        numberField.setGroupingSeparator(' ');
        numberField.setDecimalSeparator(',');
        numberField.setValue(start);
        panel.setContent(numberField);
        return panel;
    }

    private void updateField() {
        numberField.setValue(numberField.getDoubleValueDoNotThrow());
        panel.setContent(numberField);
        numberField.validate();
    }

    @Override
    protected void setup() {
        super.setup();

        CssLayout settingsLayout = new CssLayout();
        TextField groupingChar = new TextField("Grouping separator char");
        groupingChar.setValue(String.valueOf(numberField.getGroupingSeparator()));
        TextField decimalChar = new TextField("Decimal separator char");
        decimalChar.setValue(String.valueOf(numberField.getDecimalSeparator()));
        NumberField maxFractionDigits = new NumberField("Max # of fraction digits");
        maxFractionDigits.setValue((double) numberField.getMaximumFractionDigits());
        NumberField minFractionDigits = new NumberField("Min # of decimal digits");
        minFractionDigits.setValue((double) numberField.getMinimumFractionDigits());
        CheckBox allowDecimals = new CheckBox("Allow decimals");
        allowDecimals.setValue(numberField.isDecimalAllowed());

        CheckBox decimalCharAlwaysShown = new CheckBox("Decimal char always shown");
        decimalCharAlwaysShown.setValue(numberField.isDecimalSeparatorAlwaysShown());

        numberField.setGroupingSeparator(' ');
        numberField.setDecimalSeparator(',');

        settingsLayout.addComponent(groupingChar);
        groupingChar.addValueChangeListener(e -> {
            if (!groupingChar.getValue().isEmpty()) {
                numberField.setGroupingSeparator(groupingChar.getValue().charAt(0));
                updateField();
            }
        });

        settingsLayout.addComponent(decimalChar);
        decimalChar.addValueChangeListener(e -> {
            if (!decimalChar.getValue().isEmpty()) {
                numberField.setDecimalSeparator(decimalChar.getValue().charAt(0));
                updateField();
            }
        });

        settingsLayout.addComponent(maxFractionDigits);
        maxFractionDigits.addValueChangeListener(e -> {
            if (!maxFractionDigits.getValue().isEmpty()) {
                numberField.setMaximumFractionDigits(((Double) maxFractionDigits.getDoubleValueDoNotThrow()).intValue());
                updateField();
            }
        });

        settingsLayout.addComponent(minFractionDigits);
        minFractionDigits.addValueChangeListener(e -> {
            if (!minFractionDigits.getValue().isEmpty()) {
                numberField.setMinimumFractionDigits(((Double) minFractionDigits.getDoubleValueDoNotThrow()).intValue());
                updateField();
            }
        });

        settingsLayout.addComponent(allowDecimals);
        allowDecimals.addValueChangeListener(e -> {
            numberField.setDecimalAllowed(allowDecimals.getValue());
        });

        settingsLayout.addComponent(decimalCharAlwaysShown);
        decimalCharAlwaysShown.addValueChangeListener(e -> {
            numberField.setDecimalSeparatorAlwaysShown(decimalCharAlwaysShown.getValue());
        });

        content.addComponentAsFirst(settingsLayout);
        updateField();

    }
}
