package org.vaadin.numberfield;

import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
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
        numberField.setValue(start);
        panel.setContent(numberField);
        numberField.validate();
    }

    @Override
    protected void setup() {
        super.setup();

        TextField groupingChar = new TextField("Grouping char");
        groupingChar.setValue(" ");
        TextField decimalChar = new TextField("Decimal char");
        decimalChar.setValue(",");
        NumberField maxFractionDigits = new NumberField("Decimal char");
        maxFractionDigits.setValue(4d);
        
        numberField.setGroupingSeparator(' ');
        numberField.setDecimalSeparator(',');

        content.addComponentAsFirst(groupingChar);
        groupingChar.addValueChangeListener(e -> {
            if (!groupingChar.getValue().isEmpty()) {
                numberField.setGroupingSeparator(groupingChar.getValue().charAt(0));
                updateField();
            }
        });

        content.addComponentAsFirst(decimalChar);
        decimalChar.addValueChangeListener(e -> {
            if (!decimalChar.getValue().isEmpty()) {
                numberField.setDecimalSeparator(decimalChar.getValue().charAt(0));
                updateField();
            }
        });

        content.addComponentAsFirst(maxFractionDigits);
        maxFractionDigits.addValueChangeListener(e -> {
            if (!maxFractionDigits.getValue().isEmpty()) {
                numberField.setMaximumFractionDigits(((Double) maxFractionDigits.getDoubleValueDoNotThrow()).intValue());
                updateField();
            }
        });
        updateField();

    }
}
