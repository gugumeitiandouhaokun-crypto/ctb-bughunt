/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.text;

import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.nio.CharBuffer;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.builder.Builder;
import org.apache.commons.lang3.text.StrMatcher;
import org.apache.commons.lang3.text.StrTokenizer;

public class StrBuilder
implements Serializable,
Appendable,
CharSequence,
Builder<String> {
    static final int CAPACITY = 32;
    private static final long serialVersionUID = 7628716375283629643L;
    protected char[] buffer;
    protected int size;
    private String newLine;
    private String nullText;

    public StrBuilder() {
        this(32);
    }

    public StrBuilder(int n) {
        if (n <= 0) {
            n = 32;
        }
        this.buffer = new char[n];
    }

    public StrBuilder(String string) {
        if (string == null) {
            this.buffer = new char[32];
            return;
        }
        this.buffer = new char[string.length() + 32];
        this.append(string);
    }

    public String getNewLineText() {
        return this.newLine;
    }

    public StrBuilder setNewLineText(String string) {
        this.newLine = string;
        return this;
    }

    public String getNullText() {
        return this.nullText;
    }

    public StrBuilder setNullText(String string) {
        if (string != null && string.isEmpty()) {
            string = null;
        }
        this.nullText = string;
        return this;
    }

    @Override
    public int length() {
        return this.size;
    }

    public StrBuilder setLength(int n) {
        if (n < 0) {
            throw new StringIndexOutOfBoundsException(n);
        }
        if (n < this.size) {
            this.size = n;
        } else if (n > this.size) {
            this.ensureCapacity(n);
            this.size = n;
            for (int i2 = this.size; i2 < n; ++i2) {
                this.buffer[i2] = '\u0000';
            }
        }
        return this;
    }

    public int capacity() {
        return this.buffer.length;
    }

    public StrBuilder ensureCapacity(int n) {
        if (n > this.buffer.length) {
            char[] cArray = this.buffer;
            this.buffer = new char[n * 2];
            System.arraycopy(cArray, 0, this.buffer, 0, this.size);
        }
        return this;
    }

    public StrBuilder minimizeCapacity() {
        if (this.buffer.length > this.length()) {
            char[] cArray = this.buffer;
            this.buffer = new char[this.length()];
            System.arraycopy(cArray, 0, this.buffer, 0, this.size);
        }
        return this;
    }

    public int size() {
        return this.size;
    }

    public boolean isEmpty() {
        return this.size == 0;
    }

    public StrBuilder clear() {
        this.size = 0;
        return this;
    }

    @Override
    public char charAt(int n) {
        if (n < 0 || n >= this.length()) {
            throw new StringIndexOutOfBoundsException(n);
        }
        return this.buffer[n];
    }

    public StrBuilder setCharAt(int n, char c2) {
        if (n < 0 || n >= this.length()) {
            throw new StringIndexOutOfBoundsException(n);
        }
        this.buffer[n] = c2;
        return this;
    }

    public StrBuilder deleteCharAt(int n) {
        if (n < 0 || n >= this.size) {
            throw new StringIndexOutOfBoundsException(n);
        }
        int n2 = n;
        this.deleteImpl(n2, n2 + 1, 1);
        return this;
    }

    public char[] toCharArray() {
        if (this.size == 0) {
            return ArrayUtils.EMPTY_CHAR_ARRAY;
        }
        char[] cArray = new char[this.size];
        System.arraycopy(this.buffer, 0, cArray, 0, this.size);
        return cArray;
    }

    public char[] toCharArray(int n, int n2) {
        if ((n2 = this.validateRange(n, n2) - n) == 0) {
            return ArrayUtils.EMPTY_CHAR_ARRAY;
        }
        char[] cArray = new char[n2];
        System.arraycopy(this.buffer, n, cArray, 0, n2);
        return cArray;
    }

    public char[] getChars(char[] cArray) {
        int n = this.length();
        if (cArray == null || cArray.length < n) {
            cArray = new char[n];
        }
        System.arraycopy(this.buffer, 0, cArray, 0, n);
        return cArray;
    }

    public void getChars(int n, int n2, char[] cArray, int n3) {
        if (n < 0) {
            throw new StringIndexOutOfBoundsException(n);
        }
        if (n2 < 0 || n2 > this.length()) {
            throw new StringIndexOutOfBoundsException(n2);
        }
        if (n > n2) {
            throw new StringIndexOutOfBoundsException("end < start");
        }
        System.arraycopy(this.buffer, n, cArray, n3, n2 - n);
    }

    public int readFrom(Readable readable) {
        int n = this.size;
        if (readable instanceof Reader) {
            int n2;
            Reader reader = (Reader)readable;
            StrBuilder strBuilder = this;
            strBuilder.ensureCapacity(strBuilder.size + 1);
            while ((n2 = reader.read(this.buffer, this.size, this.buffer.length - this.size)) != -1) {
                this.size += n2;
                StrBuilder strBuilder2 = this;
                strBuilder2.ensureCapacity(strBuilder2.size + 1);
            }
        } else if (readable instanceof CharBuffer) {
            CharBuffer charBuffer = (CharBuffer)readable;
            int n3 = charBuffer.remaining();
            StrBuilder strBuilder = this;
            strBuilder.ensureCapacity(strBuilder.size + n3);
            charBuffer.get(this.buffer, this.size, n3);
            this.size += n3;
        } else {
            while (true) {
                StrBuilder strBuilder = this;
                strBuilder.ensureCapacity(strBuilder.size + 1);
                CharBuffer charBuffer = CharBuffer.wrap(this.buffer, this.size, this.buffer.length - this.size);
                int n4 = readable.read(charBuffer);
                if (n4 == -1) break;
                this.size += n4;
            }
        }
        return this.size - n;
    }

    public StrBuilder appendNewLine() {
        if (this.newLine == null) {
            this.append(SystemUtils.LINE_SEPARATOR);
            return this;
        }
        StrBuilder strBuilder = this;
        return strBuilder.append(strBuilder.newLine);
    }

    public StrBuilder appendNull() {
        if (this.nullText == null) {
            return this;
        }
        StrBuilder strBuilder = this;
        return strBuilder.append(strBuilder.nullText);
    }

    public StrBuilder append(Object object) {
        if (object == null) {
            return this.appendNull();
        }
        if (object instanceof CharSequence) {
            return this.append((CharSequence)object);
        }
        return this.append(object.toString());
    }

    @Override
    public StrBuilder append(CharSequence charSequence) {
        if (charSequence == null) {
            return this.appendNull();
        }
        if (charSequence instanceof StrBuilder) {
            return this.append((StrBuilder)charSequence);
        }
        if (charSequence instanceof StringBuilder) {
            return this.append((StringBuilder)charSequence);
        }
        if (charSequence instanceof StringBuffer) {
            return this.append((StringBuffer)charSequence);
        }
        if (charSequence instanceof CharBuffer) {
            return this.append((CharBuffer)charSequence);
        }
        return this.append(charSequence.toString());
    }

    @Override
    public StrBuilder append(CharSequence charSequence, int n, int n2) {
        if (charSequence == null) {
            return this.appendNull();
        }
        return this.append(charSequence.toString(), n, n2);
    }

    public StrBuilder append(String string) {
        if (string == null) {
            return this.appendNull();
        }
        int n = string.length();
        if (n > 0) {
            int n2 = this.length();
            this.ensureCapacity(n2 + n);
            string.getChars(0, n, this.buffer, n2);
            this.size += n;
        }
        return this;
    }

    public StrBuilder append(String string, int n, int n2) {
        if (string == null) {
            return this.appendNull();
        }
        if (n < 0 || n > string.length()) {
            throw new StringIndexOutOfBoundsException("startIndex must be valid");
        }
        if (n2 < 0 || n + n2 > string.length()) {
            throw new StringIndexOutOfBoundsException("length must be valid");
        }
        if (n2 > 0) {
            int n3 = this.length();
            this.ensureCapacity(n3 + n2);
            int n4 = n;
            string.getChars(n4, n4 + n2, this.buffer, n3);
            this.size += n2;
        }
        return this;
    }

    public StrBuilder append(String string, Object ... objectArray) {
        return this.append(String.format(string, objectArray));
    }

    public StrBuilder append(CharBuffer charBuffer) {
        if (charBuffer == null) {
            return this.appendNull();
        }
        if (charBuffer.hasArray()) {
            int n = charBuffer.remaining();
            int n2 = this.length();
            this.ensureCapacity(n2 + n);
            System.arraycopy(charBuffer.array(), charBuffer.arrayOffset() + charBuffer.position(), this.buffer, n2, n);
            this.size += n;
        } else {
            this.append(charBuffer.toString());
        }
        return this;
    }

    public StrBuilder append(CharBuffer charBuffer, int n, int n2) {
        if (charBuffer == null) {
            return this.appendNull();
        }
        if (charBuffer.hasArray()) {
            int n3 = charBuffer.remaining();
            if (n < 0 || n > n3) {
                throw new StringIndexOutOfBoundsException("startIndex must be valid");
            }
            if (n2 < 0 || n + n2 > n3) {
                throw new StringIndexOutOfBoundsException("length must be valid");
            }
            n3 = this.length();
            this.ensureCapacity(n3 + n2);
            System.arraycopy(charBuffer.array(), charBuffer.arrayOffset() + charBuffer.position() + n, this.buffer, n3, n2);
            this.size += n2;
        } else {
            this.append(charBuffer.toString(), n, n2);
        }
        return this;
    }

    public StrBuilder append(StringBuffer stringBuffer) {
        if (stringBuffer == null) {
            return this.appendNull();
        }
        int n = stringBuffer.length();
        if (n > 0) {
            int n2 = this.length();
            this.ensureCapacity(n2 + n);
            stringBuffer.getChars(0, n, this.buffer, n2);
            this.size += n;
        }
        return this;
    }

    public StrBuilder append(StringBuffer stringBuffer, int n, int n2) {
        if (stringBuffer == null) {
            return this.appendNull();
        }
        if (n < 0 || n > stringBuffer.length()) {
            throw new StringIndexOutOfBoundsException("startIndex must be valid");
        }
        if (n2 < 0 || n + n2 > stringBuffer.length()) {
            throw new StringIndexOutOfBoundsException("length must be valid");
        }
        if (n2 > 0) {
            int n3 = this.length();
            this.ensureCapacity(n3 + n2);
            int n4 = n;
            stringBuffer.getChars(n4, n4 + n2, this.buffer, n3);
            this.size += n2;
        }
        return this;
    }

    public StrBuilder append(StringBuilder stringBuilder) {
        if (stringBuilder == null) {
            return this.appendNull();
        }
        int n = stringBuilder.length();
        if (n > 0) {
            int n2 = this.length();
            this.ensureCapacity(n2 + n);
            stringBuilder.getChars(0, n, this.buffer, n2);
            this.size += n;
        }
        return this;
    }

    public StrBuilder append(StringBuilder stringBuilder, int n, int n2) {
        if (stringBuilder == null) {
            return this.appendNull();
        }
        if (n < 0 || n > stringBuilder.length()) {
            throw new StringIndexOutOfBoundsException("startIndex must be valid");
        }
        if (n2 < 0 || n + n2 > stringBuilder.length()) {
            throw new StringIndexOutOfBoundsException("length must be valid");
        }
        if (n2 > 0) {
            int n3 = this.length();
            this.ensureCapacity(n3 + n2);
            int n4 = n;
            stringBuilder.getChars(n4, n4 + n2, this.buffer, n3);
            this.size += n2;
        }
        return this;
    }

    public StrBuilder append(StrBuilder strBuilder) {
        if (strBuilder == null) {
            return this.appendNull();
        }
        int n = strBuilder.length();
        if (n > 0) {
            int n2 = this.length();
            this.ensureCapacity(n2 + n);
            System.arraycopy(strBuilder.buffer, 0, this.buffer, n2, n);
            this.size += n;
        }
        return this;
    }

    public StrBuilder append(StrBuilder strBuilder, int n, int n2) {
        if (strBuilder == null) {
            return this.appendNull();
        }
        if (n < 0 || n > strBuilder.length()) {
            throw new StringIndexOutOfBoundsException("startIndex must be valid");
        }
        if (n2 < 0 || n + n2 > strBuilder.length()) {
            throw new StringIndexOutOfBoundsException("length must be valid");
        }
        if (n2 > 0) {
            int n3 = this.length();
            this.ensureCapacity(n3 + n2);
            int n4 = n;
            strBuilder.getChars(n4, n4 + n2, this.buffer, n3);
            this.size += n2;
        }
        return this;
    }

    public StrBuilder append(char[] cArray) {
        if (cArray == null) {
            return this.appendNull();
        }
        int n = cArray.length;
        if (n > 0) {
            int n2 = this.length();
            this.ensureCapacity(n2 + n);
            System.arraycopy(cArray, 0, this.buffer, n2, n);
            this.size += n;
        }
        return this;
    }

    public StrBuilder append(char[] cArray, int n, int n2) {
        if (cArray == null) {
            return this.appendNull();
        }
        if (n < 0 || n > cArray.length) {
            throw new StringIndexOutOfBoundsException("Invalid startIndex: ".concat(String.valueOf(n2)));
        }
        if (n2 < 0 || n + n2 > cArray.length) {
            throw new StringIndexOutOfBoundsException("Invalid length: ".concat(String.valueOf(n2)));
        }
        if (n2 > 0) {
            int n3 = this.length();
            this.ensureCapacity(n3 + n2);
            System.arraycopy(cArray, n, this.buffer, n3, n2);
            this.size += n2;
        }
        return this;
    }

    public StrBuilder append(boolean bl) {
        if (bl) {
            StrBuilder strBuilder = this;
            strBuilder.ensureCapacity(strBuilder.size + 4);
            this.buffer[this.size++] = 116;
            this.buffer[this.size++] = 114;
            this.buffer[this.size++] = 117;
            this.buffer[this.size++] = 101;
        } else {
            StrBuilder strBuilder = this;
            strBuilder.ensureCapacity(strBuilder.size + 5);
            this.buffer[this.size++] = 102;
            this.buffer[this.size++] = 97;
            this.buffer[this.size++] = 108;
            this.buffer[this.size++] = 115;
            this.buffer[this.size++] = 101;
        }
        return this;
    }

    @Override
    public StrBuilder append(char c2) {
        int n = this.length();
        this.ensureCapacity(n + 1);
        this.buffer[this.size++] = c2;
        return this;
    }

    public StrBuilder append(int n) {
        return this.append(String.valueOf(n));
    }

    public StrBuilder append(long l) {
        return this.append(String.valueOf(l));
    }

    public StrBuilder append(float f2) {
        return this.append(String.valueOf(f2));
    }

    public StrBuilder append(double d2) {
        return this.append(String.valueOf(d2));
    }

    public StrBuilder appendln(Object object) {
        return this.append(object).appendNewLine();
    }

    public StrBuilder appendln(String string) {
        return this.append(string).appendNewLine();
    }

    public StrBuilder appendln(String string, int n, int n2) {
        return this.append(string, n, n2).appendNewLine();
    }

    public StrBuilder appendln(String string, Object ... objectArray) {
        return this.append(string, objectArray).appendNewLine();
    }

    public StrBuilder appendln(StringBuffer stringBuffer) {
        return this.append(stringBuffer).appendNewLine();
    }

    public StrBuilder appendln(StringBuilder stringBuilder) {
        return this.append(stringBuilder).appendNewLine();
    }

    public StrBuilder appendln(StringBuilder stringBuilder, int n, int n2) {
        return this.append(stringBuilder, n, n2).appendNewLine();
    }

    public StrBuilder appendln(StringBuffer stringBuffer, int n, int n2) {
        return this.append(stringBuffer, n, n2).appendNewLine();
    }

    public StrBuilder appendln(StrBuilder strBuilder) {
        return this.append(strBuilder).appendNewLine();
    }

    public StrBuilder appendln(StrBuilder strBuilder, int n, int n2) {
        return this.append(strBuilder, n, n2).appendNewLine();
    }

    public StrBuilder appendln(char[] cArray) {
        return this.append(cArray).appendNewLine();
    }

    public StrBuilder appendln(char[] cArray, int n, int n2) {
        return this.append(cArray, n, n2).appendNewLine();
    }

    public StrBuilder appendln(boolean bl) {
        return this.append(bl).appendNewLine();
    }

    public StrBuilder appendln(char c2) {
        return this.append(c2).appendNewLine();
    }

    public StrBuilder appendln(int n) {
        return this.append(n).appendNewLine();
    }

    public StrBuilder appendln(long l) {
        return this.append(l).appendNewLine();
    }

    public StrBuilder appendln(float f2) {
        return this.append(f2).appendNewLine();
    }

    public StrBuilder appendln(double d2) {
        return this.append(d2).appendNewLine();
    }

    public <T> StrBuilder appendAll(T ... TArray) {
        if (TArray != null && TArray.length > 0) {
            for (T t : TArray) {
                this.append(t);
            }
        }
        return this;
    }

    public StrBuilder appendAll(Iterable<?> object) {
        if (object != null) {
            object = object.iterator();
            while (object.hasNext()) {
                Object e2 = object.next();
                this.append(e2);
            }
        }
        return this;
    }

    public StrBuilder appendAll(Iterator<?> iterator) {
        if (iterator != null) {
            while (iterator.hasNext()) {
                this.append(iterator.next());
            }
        }
        return this;
    }

    public StrBuilder appendWithSeparators(Object[] objectArray, String string) {
        if (objectArray != null && objectArray.length > 0) {
            string = ObjectUtils.toString(string);
            this.append(objectArray[0]);
            for (int i2 = 1; i2 < objectArray.length; ++i2) {
                this.append(string);
                this.append(objectArray[i2]);
            }
        }
        return this;
    }

    public StrBuilder appendWithSeparators(Iterable<?> object, String string) {
        if (object != null) {
            string = ObjectUtils.toString(string);
            object = object.iterator();
            while (object.hasNext()) {
                this.append(object.next());
                if (!object.hasNext()) continue;
                this.append(string);
            }
        }
        return this;
    }

    public StrBuilder appendWithSeparators(Iterator<?> iterator, String string) {
        if (iterator != null) {
            string = ObjectUtils.toString(string);
            while (iterator.hasNext()) {
                this.append(iterator.next());
                if (!iterator.hasNext()) continue;
                this.append(string);
            }
        }
        return this;
    }

    public StrBuilder appendSeparator(String string) {
        return this.appendSeparator(string, null);
    }

    public StrBuilder appendSeparator(String string, String string2) {
        string = this.isEmpty() ? string2 : string;
        if (string != null) {
            this.append(string);
        }
        return this;
    }

    public StrBuilder appendSeparator(char c2) {
        if (this.size() > 0) {
            this.append(c2);
        }
        return this;
    }

    public StrBuilder appendSeparator(char c2, char c3) {
        if (this.size() > 0) {
            this.append(c2);
        } else {
            this.append(c3);
        }
        return this;
    }

    public StrBuilder appendSeparator(String string, int n) {
        if (string != null && n > 0) {
            this.append(string);
        }
        return this;
    }

    public StrBuilder appendSeparator(char c2, int n) {
        if (n > 0) {
            this.append(c2);
        }
        return this;
    }

    public StrBuilder appendPadding(int n, char c2) {
        if (n >= 0) {
            StrBuilder strBuilder = this;
            strBuilder.ensureCapacity(strBuilder.size + n);
            for (int i2 = 0; i2 < n; ++i2) {
                this.buffer[this.size++] = c2;
            }
        }
        return this;
    }

    public StrBuilder appendFixedWidthPadLeft(Object object, int n, char c2) {
        if (n > 0) {
            int n2;
            StrBuilder strBuilder = this;
            strBuilder.ensureCapacity(strBuilder.size + n);
            if ((object = object == null ? this.getNullText() : object.toString()) == null) {
                object = "";
            }
            if ((n2 = ((String)object).length()) >= n) {
                ((String)object).getChars(n2 - n, n2, this.buffer, this.size);
            } else {
                int n3 = n - n2;
                for (int i2 = 0; i2 < n3; ++i2) {
                    this.buffer[this.size + i2] = c2;
                }
                ((String)object).getChars(0, n2, this.buffer, this.size + n3);
            }
            this.size += n;
        }
        return this;
    }

    public StrBuilder appendFixedWidthPadLeft(int n, int n2, char c2) {
        return this.appendFixedWidthPadLeft(String.valueOf(n), n2, c2);
    }

    public StrBuilder appendFixedWidthPadRight(Object object, int n, char c2) {
        if (n > 0) {
            int n2;
            StrBuilder strBuilder = this;
            strBuilder.ensureCapacity(strBuilder.size + n);
            if ((object = object == null ? this.getNullText() : object.toString()) == null) {
                object = "";
            }
            if ((n2 = ((String)object).length()) >= n) {
                ((String)object).getChars(0, n, this.buffer, this.size);
            } else {
                int n3 = n - n2;
                ((String)object).getChars(0, n2, this.buffer, this.size);
                for (int i2 = 0; i2 < n3; ++i2) {
                    this.buffer[this.size + n2 + i2] = c2;
                }
            }
            this.size += n;
        }
        return this;
    }

    public StrBuilder appendFixedWidthPadRight(int n, int n2, char c2) {
        return this.appendFixedWidthPadRight(String.valueOf(n), n2, c2);
    }

    public StrBuilder insert(int n, Object object) {
        if (object == null) {
            return this.insert(n, this.nullText);
        }
        return this.insert(n, object.toString());
    }

    public StrBuilder insert(int n, String string) {
        int n2;
        this.validateIndex(n);
        if (string == null) {
            string = this.nullText;
        }
        if (string != null && (n2 = string.length()) > 0) {
            int n3 = this.size + n2;
            this.ensureCapacity(n3);
            System.arraycopy(this.buffer, n, this.buffer, n + n2, this.size - n);
            this.size = n3;
            string.getChars(0, n2, this.buffer, n);
        }
        return this;
    }

    public StrBuilder insert(int n, char[] cArray) {
        this.validateIndex(n);
        if (cArray == null) {
            return this.insert(n, this.nullText);
        }
        int n2 = cArray.length;
        if (n2 > 0) {
            StrBuilder strBuilder = this;
            strBuilder.ensureCapacity(strBuilder.size + n2);
            System.arraycopy(this.buffer, n, this.buffer, n + n2, this.size - n);
            System.arraycopy(cArray, 0, this.buffer, n, n2);
            this.size += n2;
        }
        return this;
    }

    public StrBuilder insert(int n, char[] cArray, int n2, int n3) {
        this.validateIndex(n);
        if (cArray == null) {
            return this.insert(n, this.nullText);
        }
        if (n2 < 0 || n2 > cArray.length) {
            throw new StringIndexOutOfBoundsException("Invalid offset: ".concat(String.valueOf(n2)));
        }
        if (n3 < 0 || n2 + n3 > cArray.length) {
            throw new StringIndexOutOfBoundsException("Invalid length: ".concat(String.valueOf(n3)));
        }
        if (n3 > 0) {
            StrBuilder strBuilder = this;
            strBuilder.ensureCapacity(strBuilder.size + n3);
            System.arraycopy(this.buffer, n, this.buffer, n + n3, this.size - n);
            System.arraycopy(cArray, n2, this.buffer, n, n3);
            this.size += n3;
        }
        return this;
    }

    public StrBuilder insert(int n, boolean bl) {
        this.validateIndex(n);
        if (bl) {
            StrBuilder strBuilder = this;
            strBuilder.ensureCapacity(strBuilder.size + 4);
            System.arraycopy(this.buffer, n, this.buffer, n + 4, this.size - n);
            this.buffer[n++] = 116;
            this.buffer[n++] = 114;
            this.buffer[n++] = 117;
            this.buffer[n] = 101;
            this.size += 4;
        } else {
            StrBuilder strBuilder = this;
            strBuilder.ensureCapacity(strBuilder.size + 5);
            System.arraycopy(this.buffer, n, this.buffer, n + 5, this.size - n);
            this.buffer[n++] = 102;
            this.buffer[n++] = 97;
            this.buffer[n++] = 108;
            this.buffer[n++] = 115;
            this.buffer[n] = 101;
            this.size += 5;
        }
        return this;
    }

    public StrBuilder insert(int n, char c2) {
        this.validateIndex(n);
        StrBuilder strBuilder = this;
        strBuilder.ensureCapacity(strBuilder.size + 1);
        System.arraycopy(this.buffer, n, this.buffer, n + 1, this.size - n);
        this.buffer[n] = c2;
        ++this.size;
        return this;
    }

    public StrBuilder insert(int n, int n2) {
        return this.insert(n, String.valueOf(n2));
    }

    public StrBuilder insert(int n, long l) {
        return this.insert(n, String.valueOf(l));
    }

    public StrBuilder insert(int n, float f2) {
        return this.insert(n, String.valueOf(f2));
    }

    public StrBuilder insert(int n, double d2) {
        return this.insert(n, String.valueOf(d2));
    }

    private void deleteImpl(int n, int n2, int n3) {
        System.arraycopy(this.buffer, n2, this.buffer, n, this.size - n2);
        this.size -= n3;
    }

    public StrBuilder delete(int n, int n2) {
        int n3 = (n2 = this.validateRange(n, n2)) - n;
        if (n3 > 0) {
            this.deleteImpl(n, n2, n3);
        }
        return this;
    }

    public StrBuilder deleteAll(char c2) {
        for (int i2 = 0; i2 < this.size; ++i2) {
            if (this.buffer[i2] != c2) continue;
            int n = i2;
            while (++i2 < this.size && this.buffer[i2] == c2) {
            }
            int n2 = i2 - n;
            this.deleteImpl(n, i2, n2);
            i2 -= n2;
        }
        return this;
    }

    public StrBuilder deleteFirst(char c2) {
        for (int i2 = 0; i2 < this.size; ++i2) {
            if (this.buffer[i2] != c2) continue;
            int n = i2;
            this.deleteImpl(n, n + 1, 1);
            break;
        }
        return this;
    }

    public StrBuilder deleteAll(String string) {
        int n = string == null ? 0 : string.length();
        if (n > 0) {
            int n2 = this.indexOf(string, 0);
            while (n2 >= 0) {
                int n3 = n2;
                this.deleteImpl(n3, n3 + n, n);
                n2 = this.indexOf(string, n2);
            }
        }
        return this;
    }

    public StrBuilder deleteFirst(String string) {
        int n;
        int n2 = string == null ? 0 : string.length();
        if (n2 > 0 && (n = this.indexOf(string, 0)) >= 0) {
            int n3 = n;
            this.deleteImpl(n3, n3 + n2, n2);
        }
        return this;
    }

    public StrBuilder deleteAll(StrMatcher strMatcher) {
        return this.replace(strMatcher, null, 0, this.size, -1);
    }

    public StrBuilder deleteFirst(StrMatcher strMatcher) {
        return this.replace(strMatcher, null, 0, this.size, 1);
    }

    private void replaceImpl(int n, int n2, int n3, String string, int n4) {
        int n5 = this.size - n3 + n4;
        if (n4 != n3) {
            this.ensureCapacity(n5);
            System.arraycopy(this.buffer, n2, this.buffer, n + n4, this.size - n2);
            this.size = n5;
        }
        if (n4 > 0) {
            string.getChars(0, n4, this.buffer, n);
        }
    }

    public StrBuilder replace(int n, int n2, String string) {
        n2 = this.validateRange(n, n2);
        int n3 = string == null ? 0 : string.length();
        int n4 = n2;
        this.replaceImpl(n, n4, n4 - n, string, n3);
        return this;
    }

    public StrBuilder replaceAll(char c2, char c3) {
        if (c2 != c3) {
            for (int i2 = 0; i2 < this.size; ++i2) {
                if (this.buffer[i2] != c2) continue;
                this.buffer[i2] = c3;
            }
        }
        return this;
    }

    public StrBuilder replaceFirst(char c2, char c3) {
        if (c2 != c3) {
            for (int i2 = 0; i2 < this.size; ++i2) {
                if (this.buffer[i2] != c2) continue;
                this.buffer[i2] = c3;
                break;
            }
        }
        return this;
    }

    public StrBuilder replaceAll(String string, String string2) {
        int n = string == null ? 0 : string.length();
        if (n > 0) {
            int n2 = string2 == null ? 0 : string2.length();
            int n3 = this.indexOf(string, 0);
            while (n3 >= 0) {
                int n4 = n3;
                this.replaceImpl(n4, n4 + n, n, string2, n2);
                n3 = this.indexOf(string, n3 + n2);
            }
        }
        return this;
    }

    public StrBuilder replaceFirst(String string, String string2) {
        int n;
        int n2 = string == null ? 0 : string.length();
        if (n2 > 0 && (n = this.indexOf(string, 0)) >= 0) {
            int n3 = string2 == null ? 0 : string2.length();
            int n4 = n;
            this.replaceImpl(n4, n4 + n2, n2, string2, n3);
        }
        return this;
    }

    public StrBuilder replaceAll(StrMatcher strMatcher, String string) {
        return this.replace(strMatcher, string, 0, this.size, -1);
    }

    public StrBuilder replaceFirst(StrMatcher strMatcher, String string) {
        return this.replace(strMatcher, string, 0, this.size, 1);
    }

    public StrBuilder replace(StrMatcher strMatcher, String string, int n, int n2, int n3) {
        n2 = this.validateRange(n, n2);
        return this.replaceImpl(strMatcher, string, n, n2, n3);
    }

    private StrBuilder replaceImpl(StrMatcher strMatcher, String string, int n, int n2, int n3) {
        if (strMatcher == null || this.size == 0) {
            return this;
        }
        int n4 = string == null ? 0 : string.length();
        char[] cArray = this.buffer;
        for (int i2 = n; i2 < n2 && n3 != 0; ++i2) {
            int n5 = strMatcher.isMatch(cArray, i2, n, n2);
            if (n5 <= 0) continue;
            int n6 = i2;
            this.replaceImpl(n6, n6 + n5, n5, string, n4);
            n2 = n2 - n5 + n4;
            i2 = i2 + n4 - 1;
            if (n3 <= 0) continue;
            --n3;
        }
        return this;
    }

    public StrBuilder reverse() {
        if (this.size == 0) {
            return this;
        }
        int n = this.size / 2;
        char[] cArray = this.buffer;
        int n2 = 0;
        int n3 = this.size - 1;
        while (n2 < n) {
            char c2 = cArray[n2];
            cArray[n2] = cArray[n3];
            cArray[n3] = c2;
            ++n2;
            --n3;
        }
        return this;
    }

    public StrBuilder trim() {
        int n;
        if (this.size == 0) {
            return this;
        }
        int n2 = this.size;
        char[] cArray = this.buffer;
        for (n = 0; n < n2 && cArray[n] <= ' '; ++n) {
        }
        while (n < n2 && cArray[n2 - 1] <= ' ') {
            --n2;
        }
        if (n2 < this.size) {
            this.delete(n2, this.size);
        }
        if (n > 0) {
            this.delete(0, n);
        }
        return this;
    }

    public boolean startsWith(String string) {
        if (string == null) {
            return false;
        }
        int n = string.length();
        if (n == 0) {
            return true;
        }
        if (n > this.size) {
            return false;
        }
        for (int i2 = 0; i2 < n; ++i2) {
            if (this.buffer[i2] == string.charAt(i2)) continue;
            return false;
        }
        return true;
    }

    public boolean endsWith(String string) {
        if (string == null) {
            return false;
        }
        int n = string.length();
        if (n == 0) {
            return true;
        }
        if (n > this.size) {
            return false;
        }
        int n2 = this.size - n;
        int n3 = 0;
        while (n3 < n) {
            if (this.buffer[n2] != string.charAt(n3)) {
                return false;
            }
            ++n3;
            ++n2;
        }
        return true;
    }

    @Override
    public CharSequence subSequence(int n, int n2) {
        if (n < 0) {
            throw new StringIndexOutOfBoundsException(n);
        }
        if (n2 > this.size) {
            throw new StringIndexOutOfBoundsException(n2);
        }
        if (n > n2) {
            throw new StringIndexOutOfBoundsException(n2 - n);
        }
        return this.substring(n, n2);
    }

    public String substring(int n) {
        return this.substring(n, this.size);
    }

    public String substring(int n, int n2) {
        n2 = this.validateRange(n, n2);
        return new String(this.buffer, n, n2 - n);
    }

    public String leftString(int n) {
        if (n <= 0) {
            return "";
        }
        if (n >= this.size) {
            return new String(this.buffer, 0, this.size);
        }
        return new String(this.buffer, 0, n);
    }

    public String rightString(int n) {
        if (n <= 0) {
            return "";
        }
        if (n >= this.size) {
            return new String(this.buffer, 0, this.size);
        }
        return new String(this.buffer, this.size - n, n);
    }

    public String midString(int n, int n2) {
        if (n < 0) {
            n = 0;
        }
        if (n2 <= 0 || n >= this.size) {
            return "";
        }
        if (this.size <= n + n2) {
            return new String(this.buffer, n, this.size - n);
        }
        return new String(this.buffer, n, n2);
    }

    public boolean contains(char c2) {
        char[] cArray = this.buffer;
        for (int i2 = 0; i2 < this.size; ++i2) {
            if (cArray[i2] != c2) continue;
            return true;
        }
        return false;
    }

    public boolean contains(String string) {
        return this.indexOf(string, 0) >= 0;
    }

    public boolean contains(StrMatcher strMatcher) {
        return this.indexOf(strMatcher, 0) >= 0;
    }

    public int indexOf(char c2) {
        return this.indexOf(c2, 0);
    }

    public int indexOf(char c2, int n) {
        if ((n = n < 0 ? 0 : n) >= this.size) {
            return -1;
        }
        char[] cArray = this.buffer;
        while (n < this.size) {
            if (cArray[n] == c2) {
                return n;
            }
            ++n;
        }
        return -1;
    }

    public int indexOf(String string) {
        return this.indexOf(string, 0);
    }

    public int indexOf(String string, int n) {
        int n2 = n = n < 0 ? 0 : n;
        if (string == null || n >= this.size) {
            return -1;
        }
        int n3 = string.length();
        if (n3 == 1) {
            return this.indexOf(string.charAt(0), n);
        }
        if (n3 == 0) {
            return n;
        }
        if (n3 > this.size) {
            return -1;
        }
        char[] cArray = this.buffer;
        int n4 = this.size - n3 + 1;
        while (n < n4) {
            block7: {
                for (int i2 = 0; i2 < n3; ++i2) {
                    if (string.charAt(i2) == cArray[n + i2]) {
                        continue;
                    }
                    break block7;
                }
                return n;
            }
            ++n;
        }
        return -1;
    }

    public int indexOf(StrMatcher strMatcher) {
        return this.indexOf(strMatcher, 0);
    }

    public int indexOf(StrMatcher strMatcher, int n) {
        int n2 = n = n < 0 ? 0 : n;
        if (strMatcher == null || n >= this.size) {
            return -1;
        }
        int n3 = this.size;
        char[] cArray = this.buffer;
        for (int i2 = n; i2 < n3; ++i2) {
            if (strMatcher.isMatch(cArray, i2, n, n3) <= 0) continue;
            return i2;
        }
        return -1;
    }

    public int lastIndexOf(char c2) {
        return this.lastIndexOf(c2, this.size - 1);
    }

    public int lastIndexOf(char c2, int n) {
        if ((n = n >= this.size ? this.size - 1 : n) < 0) {
            return -1;
        }
        while (n >= 0) {
            if (this.buffer[n] == c2) {
                return n;
            }
            --n;
        }
        return -1;
    }

    public int lastIndexOf(String string) {
        return this.lastIndexOf(string, this.size - 1);
    }

    public int lastIndexOf(String string, int n) {
        int n2 = n = n >= this.size ? this.size - 1 : n;
        if (string == null || n < 0) {
            return -1;
        }
        int n3 = string.length();
        if (n3 > 0 && n3 <= this.size) {
            if (n3 == 1) {
                return this.lastIndexOf(string.charAt(0), n);
            }
            block0: for (n = n - n3 + 1; n >= 0; --n) {
                for (int i2 = 0; i2 < n3; ++i2) {
                    if (string.charAt(i2) != this.buffer[n + i2]) continue block0;
                }
                return n;
            }
        } else if (n3 == 0) {
            return n;
        }
        return -1;
    }

    public int lastIndexOf(StrMatcher strMatcher) {
        return this.lastIndexOf(strMatcher, this.size);
    }

    public int lastIndexOf(StrMatcher strMatcher, int n) {
        int n2 = n = n >= this.size ? this.size - 1 : n;
        if (strMatcher == null || n < 0) {
            return -1;
        }
        char[] cArray = this.buffer;
        int n3 = n + 1;
        while (n >= 0) {
            if (strMatcher.isMatch(cArray, n, 0, n3) > 0) {
                return n;
            }
            --n;
        }
        return -1;
    }

    public StrTokenizer asTokenizer() {
        return new StrBuilderTokenizer();
    }

    public Reader asReader() {
        return new StrBuilderReader();
    }

    public Writer asWriter() {
        return new StrBuilderWriter();
    }

    public void appendTo(Appendable appendable) {
        if (appendable instanceof Writer) {
            ((Writer)appendable).write(this.buffer, 0, this.size);
            return;
        }
        if (appendable instanceof StringBuilder) {
            ((StringBuilder)appendable).append(this.buffer, 0, this.size);
            return;
        }
        if (appendable instanceof StringBuffer) {
            ((StringBuffer)appendable).append(this.buffer, 0, this.size);
            return;
        }
        if (appendable instanceof CharBuffer) {
            ((CharBuffer)appendable).put(this.buffer, 0, this.size);
            return;
        }
        appendable.append(this);
    }

    public boolean equalsIgnoreCase(StrBuilder object) {
        if (this == object) {
            return true;
        }
        if (this.size != ((StrBuilder)object).size) {
            return false;
        }
        char[] cArray = this.buffer;
        object = ((StrBuilder)object).buffer;
        for (int i2 = this.size - 1; i2 >= 0; --i2) {
            char c2 = cArray[i2];
            Object object2 = object[i2];
            if (c2 == object2 || Character.toUpperCase(c2) == Character.toUpperCase((char)object2)) continue;
            return false;
        }
        return true;
    }

    public boolean equals(StrBuilder object) {
        if (this == object) {
            return true;
        }
        if (object == null) {
            return false;
        }
        if (this.size != ((StrBuilder)object).size) {
            return false;
        }
        char[] cArray = this.buffer;
        object = ((StrBuilder)object).buffer;
        for (int i2 = this.size - 1; i2 >= 0; --i2) {
            if (cArray[i2] == object[i2]) continue;
            return false;
        }
        return true;
    }

    public boolean equals(Object object) {
        return object instanceof StrBuilder && this.equals((StrBuilder)object);
    }

    public int hashCode() {
        char[] cArray = this.buffer;
        int n = 0;
        for (int i2 = this.size - 1; i2 >= 0; --i2) {
            n = 31 * n + cArray[i2];
        }
        return n;
    }

    @Override
    public String toString() {
        return new String(this.buffer, 0, this.size);
    }

    public StringBuffer toStringBuffer() {
        return new StringBuffer(this.size).append(this.buffer, 0, this.size);
    }

    public StringBuilder toStringBuilder() {
        return new StringBuilder(this.size).append(this.buffer, 0, this.size);
    }

    @Override
    public String build() {
        return this.toString();
    }

    protected int validateRange(int n, int n2) {
        if (n < 0) {
            throw new StringIndexOutOfBoundsException(n);
        }
        if (n2 > this.size) {
            n2 = this.size;
        }
        if (n > n2) {
            throw new StringIndexOutOfBoundsException("end < start");
        }
        return n2;
    }

    protected void validateIndex(int n) {
        if (n < 0 || n > this.size) {
            throw new StringIndexOutOfBoundsException(n);
        }
    }

    class StrBuilderWriter
    extends Writer {
        StrBuilderWriter() {
        }

        @Override
        public void close() {
        }

        @Override
        public void flush() {
        }

        @Override
        public void write(int n) {
            StrBuilder.this.append((char)n);
        }

        @Override
        public void write(char[] cArray) {
            StrBuilder.this.append(cArray);
        }

        @Override
        public void write(char[] cArray, int n, int n2) {
            StrBuilder.this.append(cArray, n, n2);
        }

        @Override
        public void write(String string) {
            StrBuilder.this.append(string);
        }

        @Override
        public void write(String string, int n, int n2) {
            StrBuilder.this.append(string, n, n2);
        }
    }

    class StrBuilderReader
    extends Reader {
        private int pos;
        private int mark;

        StrBuilderReader() {
        }

        @Override
        public void close() {
        }

        @Override
        public int read() {
            if (!this.ready()) {
                return -1;
            }
            return StrBuilder.this.charAt(this.pos++);
        }

        @Override
        public int read(char[] cArray, int n, int n2) {
            if (n < 0 || n2 < 0 || n > cArray.length || n + n2 > cArray.length || n + n2 < 0) {
                throw new IndexOutOfBoundsException();
            }
            if (n2 == 0) {
                return 0;
            }
            if (this.pos >= StrBuilder.this.size()) {
                return -1;
            }
            if (this.pos + n2 > StrBuilder.this.size()) {
                n2 = StrBuilder.this.size() - this.pos;
            }
            StrBuilder.this.getChars(this.pos, this.pos + n2, cArray, n);
            this.pos += n2;
            return n2;
        }

        @Override
        public long skip(long l) {
            if ((long)this.pos + l > (long)StrBuilder.this.size()) {
                l = StrBuilder.this.size() - this.pos;
            }
            if (l < 0L) {
                return 0L;
            }
            this.pos = (int)((long)this.pos + l);
            return l;
        }

        @Override
        public boolean ready() {
            return this.pos < StrBuilder.this.size();
        }

        @Override
        public boolean markSupported() {
            return true;
        }

        @Override
        public void mark(int n) {
            this.mark = this.pos;
        }

        @Override
        public void reset() {
            this.pos = this.mark;
        }
    }

    class StrBuilderTokenizer
    extends StrTokenizer {
        StrBuilderTokenizer() {
        }

        @Override
        protected List<String> tokenize(char[] cArray, int n, int n2) {
            if (cArray == null) {
                StrBuilderTokenizer strBuilderTokenizer = this;
                return super.tokenize(strBuilderTokenizer.StrBuilder.this.buffer, 0, StrBuilder.this.size());
            }
            return super.tokenize(cArray, n, n2);
        }

        @Override
        public String getContent() {
            String string = super.getContent();
            if (string == null) {
                return StrBuilder.this.toString();
            }
            return string;
        }
    }
}

