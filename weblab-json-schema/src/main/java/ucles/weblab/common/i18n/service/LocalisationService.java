package ucles.weblab.common.i18n.service;

import java.util.function.Consumer;

/**
 * Convenience service to handle localisation tasks including translation using {@link org.springframework.context.support.MessageSourceAccessor}.
 * TODO: find a better home for this, avoiding circular module dependency between i18n and json-schema.
 *
 * Created by bodeng on 07/06/2017.
 */
public interface LocalisationService {
    /**
     * Lookup key in messages for current {@link java.util.Locale} and if found apply the result to <code>target</code>
     * @param key message key
     * @param target consumer that will be supplied with the message lookup result if found
     */
    void ifMessagePresent(String key, Consumer<String> target);
}
