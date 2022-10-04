package dev.openfeature.sdk;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Delegate;

@ToString
@SuppressWarnings("PMD.BeanMembersShouldSerialize")
public class MutableContext implements EvaluationContext {

    @Setter() @Getter private String targetingKey;
    @Delegate(excludes = HideDelegateAddMethods.class) private final HashMapStructure structure;

    public MutableContext() {
        this.structure = new HashMapStructure();
        this.targetingKey = "";
    }

    public MutableContext(String targetingKey) {
        this();
        this.targetingKey = targetingKey;
    }

    public MutableContext(Map<String, Value> attributes) {
        this.structure = new HashMapStructure(attributes);
        this.targetingKey = "";
    }

    public MutableContext(String targetingKey, Map<String, Value> attributes) {
        this(attributes);
        this.targetingKey = targetingKey;
    }

    // override @Delegate methods so that we can use "add" methods and still return EvaluationContext, not Structure
    public MutableContext add(String key, Boolean value) {
        this.structure.add(key, value);
        return this;
    }

    public MutableContext add(String key, String value) {
        this.structure.add(key, value);
        return this;
    }

    public MutableContext add(String key, Integer value) {
        this.structure.add(key, value);
        return this;
    }

    public MutableContext add(String key, Double value) {
        this.structure.add(key, value);
        return this;
    }

    public MutableContext add(String key, Instant value) {
        this.structure.add(key, value);
        return this;
    }

    public MutableContext add(String key, HashMapStructure value) {
        this.structure.add(key, value);
        return this;
    }

    public MutableContext add(String key, List<Value> value) {
        this.structure.add(key, value);
        return this;
    }

    /**
     * Hidden class to tell Lombok not to copy these methods over via delegation.
     */
    private static class HideDelegateAddMethods {
        public HashMapStructure add(String ignoredKey, Boolean ignoredValue) {
            return null;
        }
        
        public HashMapStructure add(String ignoredKey, Double ignoredValue) {
            return null;
        }

        public HashMapStructure add(String ignoredKey, String ignoredValue) {
            return null;
        }

        public HashMapStructure add(String ignoredKey, Value ignoredValue) {
            return null;
        }

        public HashMapStructure add(String ignoredKey, Integer ignoredValue) {
            return null;
        }

        public HashMapStructure add(String ignoredKey, List<Value> ignoredValue) {
            return null;
        }

        public HashMapStructure add(String ignoredKey, HashMapStructure ignoredValue) {
            return null;
        }

        public HashMapStructure add(String ignoredKey, Instant ignoredValue) {
            return null;
        }
    }
}
