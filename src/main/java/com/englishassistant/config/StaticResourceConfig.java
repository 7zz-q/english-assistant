package com.englishassistant.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Production mode: serves built Vue 3 frontend from client/dist/.
 * Auto-detects: if index.html exists in dist, enables static serving + SPA fallback.
 * Same behavior as the old Express server's production mode.
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(StaticResourceConfig.class);

    // Path to the Vue frontend build output
    // Priority: system property > relative to project root > absolute path
    private static final String DIST_PATH = System.getProperty("app.frontend",
        "C:/Users/qiu/Desktop/english-assistant-server/client/dist");

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path distDir = Paths.get(DIST_PATH);
        if (!Files.exists(distDir.resolve("index.html"))) {
            log.info("🔧 API-only mode (no client/dist/ found)");
            return;
        }

        log.info("🌐 Production mode: serving frontend from {}", DIST_PATH);

        // Serve the dist directory as static resources
        registry.addResourceHandler("/**")
            .addResourceLocations("file:" + DIST_PATH + "/")
            .resourceChain(true)
            .addResolver(new PathResourceResolver() {
                @Override
                protected Resource getResource(String resourcePath, Resource location)
                        throws IOException {
                    Resource res = location.createRelative(resourcePath);
                    if (res.exists() && res.isReadable()) {
                        return res;
                    }
                    // SPA fallback: return index.html for non-file routes
                    return location.createRelative("index.html");
                }
            });

        log.info("✅ Frontend ready at http://localhost:3000");
    }
}
