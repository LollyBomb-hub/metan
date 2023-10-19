package ru.council.metan.jsonpath;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.council.metan.enums.ArraySubscriptionType;
import ru.council.metan.enums.JsonPathElementType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor
@Getter
@Setter
public class JsonPathElement {

    private static final String PARENT_REF = "$";

    private static final Pattern ARRAY_SUBSCRIPTION_REGEX = Pattern.compile("^(.*?)\\[(.*?)]$");
    private static final Pattern DIGITAL_SUBSCRIPTION = Pattern.compile("(\\d+)");

    private String source;
    private String identifier;
    private JsonPathElementType type = JsonPathElementType.Identifier;
    private ArraySubscriptionType arraySubscriptionType = ArraySubscriptionType.None;
    private String index = null;
    private boolean isLeaf = false;

    public JsonPathElement(String identifier) {
        processConstructorIdentifier(identifier);
    }

    public JsonPathElement(String identifier, boolean isLeaf) {
        processConstructorIdentifier(identifier);
        this.isLeaf = isLeaf;
    }

    public static String getArraySubscriptionTypeIdentifier(int index) {
        return "[" + index + "]";
    }

    private void processConstructorIdentifier(String identifier) {
        this.source = identifier;

        Matcher arraySubscription = ARRAY_SUBSCRIPTION_REGEX.matcher(identifier);

        this.identifier = identifier;

        if (arraySubscription.matches()) {
            this.type = JsonPathElementType.ArraySubscriptor;
            this.identifier = arraySubscription.group(1);
            String index = arraySubscription.group(2);
            if (index.trim().equals("*")) {
                arraySubscriptionType = ArraySubscriptionType.All;
                this.index = "*";
            } else {
                this.index = index;
                if (DIGITAL_SUBSCRIPTION.matcher(index).matches()) {
                    this.arraySubscriptionType = ArraySubscriptionType.Indexed;
                } else {
                    this.arraySubscriptionType = ArraySubscriptionType.IndexedByStringKey;
                }
            }
        }
    }

    public boolean isIndexed() {
        return !arraySubscriptionType.equals(ArraySubscriptionType.None);
    }

    public boolean isIntegerSubscription() {
        return arraySubscriptionType.equals(ArraySubscriptionType.Indexed);
    }

    public int getIndex() {
        if (isIntegerSubscription()) {
            return Integer.parseInt(index);
        }
        throw new IllegalCallerException("Attempt to get int from non-int value");
    }

    public String getIndexKey() {
        return index;
    }

    public boolean isParentReference() {
        return identifier.equals(PARENT_REF);
    }

    @Override
    public String toString() {
        return "JsonPathElement{" +
                "identifier='" + identifier + '\'' +
                ", arraySubscriptionType=" + arraySubscriptionType +
                ", index='" + index + '\'' +
                ", isLeaf=" + isLeaf +
                '}';
    }
}
