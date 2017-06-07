package ucles.weblab.common.i18n.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceAccessor;
import ucles.weblab.common.i18n.service.LocalisationService;

import java.util.function.Consumer;

/**
 * {@inheritDoc}
 */
public class LocalisationServiceImpl implements LocalisationService {
    private Logger log = LoggerFactory.getLogger(getClass());
    private final MessageSourceAccessor messageSourceAccessor;

    public LocalisationServiceImpl(MessageSource messageSource) {
        this.messageSourceAccessor = new MessageSourceAccessor(messageSource);
    }

    @Override
    public void ifMessagePresent(String key, Consumer<String> target) {
        try {
            String translated = messageSourceAccessor.getMessage(key); // will use current thread locale
            target.accept(translated);
        } catch (NoSuchMessageException e) {
            log.trace("No message found for key: {} for locale {}", key, LocaleContextHolder.getLocale());
        }
    }


}
