package com.magiclook.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Normalize the directory name (remove leading slash if present)
        String normalizedDir = uploadDir.startsWith("/") ? uploadDir.substring(1) : uploadDir;
        
        // Get absolute path to the upload directory
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
        
        // Create directory if it doesn't exist
        try {
            Files.createDirectories(uploadPath);
        } catch (Exception e) {
            logger.warn("Could not create upload directory: {}", uploadPath);
        }
        
        // Build file: URI with trailing slash
        String uploadAbsolutePath = "file:" + uploadPath.toString().replace("\\", "/");
        if (!uploadAbsolutePath.endsWith("/")) {
            uploadAbsolutePath += "/";
        }
        
        logger.info("Configuring resource handler for /{} with locations: {}, classpath:/static/{}/", 
                    normalizedDir, uploadAbsolutePath, normalizedDir);
        
        // Map /items/** URL pattern to both the external upload directory and classpath
        // External directory is checked first, then falls back to classpath for default images
        registry.addResourceHandler("/" + normalizedDir + "/**")
                .addResourceLocations(uploadAbsolutePath, "classpath:/static/" + normalizedDir + "/")
                .setCachePeriod(3600);
    }
}
