/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonCreator
 *  com.fasterxml.jackson.annotation.JsonProperty
 *  com.fasterxml.jackson.annotation.JsonPropertyOrder
 *  com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
 */
package org.apache.logging.log4j.core.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonPropertyOrder(value={"key", "value"})
final class MapEntry {
    @JsonProperty
    @JacksonXmlProperty(isAttribute=true)
    private String key;
    @JsonProperty
    @JacksonXmlProperty(isAttribute=true)
    private String value;

    @JsonCreator
    public MapEntry(@JsonProperty(value="key") String string, @JsonProperty(value="value") String string2) {
        this.setKey(string);
        this.setValue(string2);
    }

    public final boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null) {
            return false;
        }
        if (!(object instanceof MapEntry)) {
            return false;
        }
        object = (MapEntry)object;
        if (this.getKey() == null ? ((MapEntry)object).getKey() != null : !this.getKey().equals(((MapEntry)object).getKey())) {
            return false;
        }
        return !(this.getValue() == null ? ((MapEntry)object).getValue() != null : !this.getValue().equals(((MapEntry)object).getValue()));
    }

    public final String getKey() {
        return this.key;
    }

    public final String getValue() {
        return this.value;
    }

    public final int hashCode() {
        int n = 31 + (this.getKey() == null ? 0 : this.getKey().hashCode());
        return 31 * n + (this.getValue() == null ? 0 : this.getValue().hashCode());
    }

    public final void setKey(String string) {
        this.key = string;
    }

    public final void setValue(String string) {
        this.value = string;
    }

    public final String toString() {
        return this.getKey() + "=" + this.getValue();
    }
}

