package com.gzy.gmi.app.GMIViewer.widgets;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/* W.I.P. currently deprecated */
/** text field that only accepts numbers in a certain range */
public class GMIRestrictedNumberField extends JTextField {

    /** when constructed, this text field only accepts number n ∈ {x | low <= x <= high} */
    public GMIRestrictedNumberField(int low, int high) {
        super();
        assert high >= low;
        setDocument(new RestrictedNumberDocument(low, high));
    }

    private class RestrictedNumberDocument extends PlainDocument {
        private int low;
        private int high;

        /** current integer value */
        private int curVal;

        public RestrictedNumberDocument(int low, int high) {
            super();
            this.low = low;
            this.high = high;
            curVal = (low + high) / 2;
        }

        /**
         * if str.length != 0, check if str < low
         * else save the current value until update
         * */
        @Override
        public void remove(int offs, int len) throws BadLocationException {
            int newVal;
            String newStr = getText(0, offs) + getText(offs + len, getLength() - len - offs);
            if(newStr.length() != 0) {
                newVal = Integer.parseInt(newStr);
                if(newVal >= low && newVal <= high) {
                    curVal = newVal;
                }
            }
            super.remove(offs, len);
        }

        /** restrict input string to numbers */
        @Override
        public void insertString(int offs, String str, AttributeSet attr) throws BadLocationException {
            int newVal;
            try {
                Integer.parseInt(str);
            } catch (NumberFormatException ex) {
                // throw new BadLocationException(newStr, offs);
            }
            String newStr = getText(0, offs) + str + getText(offs, getLength() - offs);
            super.insertString(offs, str, attr);
        }
    }
}
