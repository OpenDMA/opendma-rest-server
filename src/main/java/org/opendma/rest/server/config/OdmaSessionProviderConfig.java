package org.opendma.rest.server.config;

import java.util.Map;

import org.opendma.api.OdmaSessionProvider;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OdmaSessionProviderConfig {

    @Bean
    @ConditionalOnMissingBean(OdmaSessionProvider.class)
    public OdmaSessionProvider odmaSessionProvider(OdmaProviderProperties props) throws Exception {
        if (props.getClassName() == null || props.getClassName().isEmpty()) {
            throw new IllegalArgumentException("Missing configuration odma.provider.className");
        }

        Class<?> clazz = Class.forName(props.getClassName());
        Object instance = clazz.getDeclaredConstructor().newInstance();

        if (!(instance instanceof OdmaSessionProvider)) {
            throw new IllegalArgumentException("Provided class `"+props.getClassName()+"` is not an OdmaSessionProvider");
        }

        // Inject properties via setter methods
        BeanWrapper wrapper = new BeanWrapperImpl(instance);
        for (Map.Entry<String, String> entry : props.getProps().entrySet()) {
            if (wrapper.isWritableProperty(entry.getKey())) {
                wrapper.setPropertyValue(entry.getKey(), entry.getValue());
            } else {
                throw new IllegalArgumentException("OdmaSessionProvider `"+props.getClassName()+"` does not have writable property `"+entry.getKey()+"`");
            }
        }

        return (OdmaSessionProvider) instance;
    }

}
