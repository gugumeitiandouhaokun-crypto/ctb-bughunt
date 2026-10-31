/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.message;

import java.util.Map;
import org.apache.logging.log4j.message.AsynchronouslyFormattable;
import org.apache.logging.log4j.message.MapMessage;
import org.apache.logging.log4j.message.StructuredDataId;
import org.apache.logging.log4j.util.EnglishEnums;
import org.apache.logging.log4j.util.StringBuilders;

@AsynchronouslyFormattable
public class StructuredDataMessage
extends MapMessage<StructuredDataMessage, String> {
    private static final long serialVersionUID = 1703221292892071920L;
    private static final int MAX_LENGTH = 32;
    private static final int HASHVAL = 31;
    private StructuredDataId id;
    private String message;
    private String type;
    private final int maxLength;

    public StructuredDataMessage(String string, String string2, String string3) {
        this(string, string2, string3, 32);
    }

    public StructuredDataMessage(String string, String string2, String string3, int n) {
        this.id = new StructuredDataId(string, null, null, n);
        this.message = string2;
        this.type = string3;
        this.maxLength = n;
    }

    public StructuredDataMessage(String string, String string2, String string3, Map<String, String> map) {
        this(string, string2, string3, map, 32);
    }

    public StructuredDataMessage(String string, String string2, String string3, Map<String, String> map, int n) {
        super(map);
        this.id = new StructuredDataId(string, null, null, n);
        this.message = string2;
        this.type = string3;
        this.maxLength = n;
    }

    public StructuredDataMessage(StructuredDataId structuredDataId, String string, String string2) {
        this(structuredDataId, string, string2, 32);
    }

    public StructuredDataMessage(StructuredDataId structuredDataId, String string, String string2, int n) {
        this.id = structuredDataId;
        this.message = string;
        this.type = string2;
        this.maxLength = n;
    }

    public StructuredDataMessage(StructuredDataId structuredDataId, String string, String string2, Map<String, String> map) {
        this(structuredDataId, string, string2, map, 32);
    }

    public StructuredDataMessage(StructuredDataId structuredDataId, String string, String string2, Map<String, String> map, int n) {
        super(map);
        this.id = structuredDataId;
        this.message = string;
        this.type = string2;
        this.maxLength = n;
    }

    private StructuredDataMessage(StructuredDataMessage structuredDataMessage, Map<String, String> map) {
        super(map);
        this.id = structuredDataMessage.id;
        this.message = structuredDataMessage.message;
        this.type = structuredDataMessage.type;
        this.maxLength = 32;
    }

    protected StructuredDataMessage() {
        this.maxLength = 32;
    }

    @Override
    public String[] getFormats() {
        String[] stringArray = new String[Format.values().length];
        int n = 0;
        for (Format format : Format.values()) {
            stringArray[n++] = format.name();
        }
        return stringArray;
    }

    public StructuredDataId getId() {
        return this.id;
    }

    protected void setId(String string) {
        this.id = new StructuredDataId(string, null, null);
    }

    protected void setId(StructuredDataId structuredDataId) {
        this.id = structuredDataId;
    }

    public String getType() {
        return this.type;
    }

    protected void setType(String string) {
        if (string.length() > 32) {
            throw new IllegalArgumentException("structured data type exceeds maximum length of 32 characters: ".concat(String.valueOf(string)));
        }
        this.type = string;
    }

    @Override
    public void formatTo(StringBuilder stringBuilder) {
        this.asString(Format.FULL, null, stringBuilder);
    }

    @Override
    public void formatTo(String[] stringArray, StringBuilder stringBuilder) {
        StructuredDataMessage structuredDataMessage = this;
        structuredDataMessage.asString(structuredDataMessage.getFormat(stringArray), null, stringBuilder);
    }

    @Override
    public String getFormat() {
        return this.message;
    }

    protected void setMessageFormat(String string) {
        this.message = string;
    }

    @Override
    public String asString() {
        return this.asString(Format.FULL, null);
    }

    @Override
    public String asString(String string) {
        try {
            return this.asString(EnglishEnums.valueOf(Format.class, string), null);
        }
        catch (IllegalArgumentException illegalArgumentException) {
            return this.asString();
        }
    }

    public final String asString(Format format, StructuredDataId structuredDataId) {
        StringBuilder stringBuilder = new StringBuilder();
        this.asString(format, structuredDataId, stringBuilder);
        return stringBuilder.toString();
    }

    public final void asString(Format object, StructuredDataId structuredDataId, StringBuilder stringBuilder) {
        StructuredDataId structuredDataId2;
        boolean bl = Format.FULL.equals(object);
        if (bl) {
            if (this.getType() == null) {
                return;
            }
            stringBuilder.append(this.getType()).append(' ');
        }
        if ((structuredDataId2 = (structuredDataId2 = this.getId()) != null ? structuredDataId2.makeId(structuredDataId) : structuredDataId) == null || structuredDataId2.getName() == null) {
            return;
        }
        if (Format.XML.equals(object)) {
            this.asXml(structuredDataId2, stringBuilder);
            return;
        }
        stringBuilder.append('[');
        StringBuilders.appendValue(stringBuilder, structuredDataId2);
        stringBuilder.append(' ');
        this.appendMap(stringBuilder);
        stringBuilder.append(']');
        if (bl && (object = this.getFormat()) != null) {
            stringBuilder.append(' ').append((String)object);
        }
    }

    private void asXml(StructuredDataId structuredDataId, StringBuilder stringBuilder) {
        stringBuilder.append("<StructuredData>\n");
        stringBuilder.append("<type>").append(this.type).append("</type>\n");
        stringBuilder.append("<id>").append(structuredDataId).append("</id>\n");
        super.asXml(stringBuilder);
        stringBuilder.append("\n</StructuredData>\n");
    }

    @Override
    public String getFormattedMessage() {
        return this.asString(Format.FULL, null);
    }

    @Override
    public String getFormattedMessage(String[] stringArray) {
        StructuredDataMessage structuredDataMessage = this;
        return structuredDataMessage.asString(structuredDataMessage.getFormat(stringArray), null);
    }

    private Format getFormat(String[] stringArray) {
        if (stringArray != null && stringArray.length > 0) {
            for (int i2 = 0; i2 < stringArray.length; ++i2) {
                String string = stringArray[i2];
                if (Format.XML.name().equalsIgnoreCase(string)) {
                    return Format.XML;
                }
                if (!Format.FULL.name().equalsIgnoreCase(string)) continue;
                return Format.FULL;
            }
            return null;
        }
        return Format.FULL;
    }

    @Override
    public String toString() {
        return this.asString(null, null);
    }

    @Override
    public StructuredDataMessage newInstance(Map<String, String> map) {
        return new StructuredDataMessage(this, map);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        StructuredDataMessage structuredDataMessage = (StructuredDataMessage)object;
        if (!super.equals(object)) {
            return false;
        }
        if (this.type != null ? !this.type.equals(structuredDataMessage.type) : structuredDataMessage.type != null) {
            return false;
        }
        if (this.id != null ? !this.id.equals(structuredDataMessage.id) : structuredDataMessage.id != null) {
            return false;
        }
        return !(this.message != null ? !this.message.equals(structuredDataMessage.message) : structuredDataMessage.message != null);
    }

    @Override
    public int hashCode() {
        int n = super.hashCode();
        n = 31 * n + (this.type != null ? this.type.hashCode() : 0);
        n = 31 * n + (this.id != null ? this.id.hashCode() : 0);
        return 31 * n + (this.message != null ? this.message.hashCode() : 0);
    }

    @Override
    protected void validate(String string, boolean bl) {
        this.validateKey(string);
    }

    @Override
    protected void validate(String string, byte by) {
        this.validateKey(string);
    }

    @Override
    protected void validate(String string, char c2) {
        this.validateKey(string);
    }

    @Override
    protected void validate(String string, double d2) {
        this.validateKey(string);
    }

    @Override
    protected void validate(String string, float f2) {
        this.validateKey(string);
    }

    @Override
    protected void validate(String string, int n) {
        this.validateKey(string);
    }

    @Override
    protected void validate(String string, long l) {
        this.validateKey(string);
    }

    @Override
    protected void validate(String string, Object object) {
        this.validateKey(string);
    }

    @Override
    protected void validate(String string, short s) {
        this.validateKey(string);
    }

    @Override
    protected void validate(String string, String string2) {
        this.validateKey(string);
    }

    protected void validateKey(String string) {
        if (this.maxLength > 0 && string.length() > this.maxLength) {
            throw new IllegalArgumentException("Structured data keys are limited to " + this.maxLength + " characters. key: " + string);
        }
        for (int i2 = 0; i2 < string.length(); ++i2) {
            char c2 = string.charAt(i2);
            if (c2 >= '!' && c2 <= '~' && c2 != '=' && c2 != ']' && c2 != '\"') continue;
            throw new IllegalArgumentException("Structured data keys must contain printable US ASCII charactersand may not contain a space, =, ], or \"");
        }
    }

    public static enum Format {
        XML,
        FULL;

    }
}

