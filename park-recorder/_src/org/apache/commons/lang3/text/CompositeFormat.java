/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.text;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

public class CompositeFormat
extends Format {
    private static final long serialVersionUID = -4329119827877627683L;
    private final Format parser;
    private final Format formatter;

    public CompositeFormat(Format format, Format format2) {
        this.parser = format;
        this.formatter = format2;
    }

    @Override
    public StringBuffer format(Object object, StringBuffer stringBuffer, FieldPosition fieldPosition) {
        return this.formatter.format(object, stringBuffer, fieldPosition);
    }

    @Override
    public Object parseObject(String string, ParsePosition parsePosition) {
        return this.parser.parseObject(string, parsePosition);
    }

    public Format getParser() {
        return this.parser;
    }

    public Format getFormatter() {
        return this.formatter;
    }

    public String reformat(String string) {
        CompositeFormat compositeFormat = this;
        return compositeFormat.format(compositeFormat.parseObject(string));
    }
}

