package cn.fxbin.bubble.core.support;

import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * YamlConfigFactory
 *
 * @author fxbin
 * @version v1.1
 * @since 2025/7/7 15:28
 */
public class YamlConfigFactory extends DefaultPropertySourceFactory {

    private static final String[] YAML_EXTENSIONS = {".yml", ".yaml"};

    @Override
    public PropertySource<?> createPropertySource(@Nullable String name, EncodedResource encodedResource) throws IOException {
        Objects.requireNonNull(encodedResource, "EncodedResource must not be null");

        Resource resource = encodedResource.getResource();
        if (!resource.exists()) {
            return new PropertiesPropertySource(getSourceName(name, resource), new Properties());
        }

        if (isYamlResource(resource)) {
            return loadYamlPropertySource(name, resource);
        }

        return super.createPropertySource(name, encodedResource);
    }

    private boolean isYamlResource(Resource resource) {
        String filename = resource.getFilename();
        if (filename == null) {
            return false;
        }

        for (String ext : YAML_EXTENSIONS) {
            if (filename.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    private PropertySource<?> loadYamlPropertySource(@Nullable String name, Resource resource) throws IOException {
        YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
        List<PropertySource<?>> sources = loader.load(getSourceName(name, resource), resource);

        if (sources.isEmpty()) {
            return new PropertiesPropertySource(getSourceName(name, resource), new Properties());
        }

        if (sources.size() == 1) {
            return sources.get(0);
        }

        CompositePropertySource composite = new CompositePropertySource(getSourceName(name, resource));
        sources.forEach(composite::addPropertySource);
        return composite;
    }

    private String getSourceName(@Nullable String name, Resource resource) {
        return name != null ? name : resource.getDescription();
    }
}
