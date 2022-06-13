package dev.openfeature.javasdk;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.With;

@Value @Builder @With
public class HookContext<T> {
    @NonNull String flagKey;
    @NonNull FlagValueType type;
    @NonNull T defaultValue;
    @NonNull EvaluationContext ctx;
    Metadata clientMetadata;
    Metadata providerMetadata;

    public static <T> HookContext<T> from(String key, FlagValueType type, Metadata clientMetadata, Metadata providerMetadata, EvaluationContext ctx, T defaultValue) {
        return HookContext.<T>builder()
                .flagKey(key)
                .type(type)
                .clientMetadata(clientMetadata)
                .providerMetadata(providerMetadata)
                .ctx(ctx)
                .defaultValue(defaultValue)
                .build();
    }
}
