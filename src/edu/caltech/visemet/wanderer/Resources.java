package edu.caltech.visemet.wanderer;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Max Hirschhorn #visemet
 */
public class Resources {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(Resources.class);

    /**
     * Class constructor.
     */
    private Resources() { }

    /**
     * Normalizes the specified path with respect to the specified base
     * resource.
     *
     * @param baseResource the base resource
     * @param path the path
     *
     * @return a URI
     */
    public static String normalize(String baseResource, String path) {
        String URI = null;

        try {
            path = URLDecoder.decode(path, "UTF-8");
        } catch (Exception ex) {
            LOGGER.warn("base {}", baseResource);
        }

        if (path.contains("?")) {
            path = path.substring(0, path.indexOf("?"));
        }

        if (path.contains("#")) {
            path = path.substring(0, path.indexOf("#"));
        }

        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        if (path.startsWith("http://") || path.startsWith("https://")) {
            URI = path;
        } else if (path.startsWith("//")) {
            URI = "http:" + path;
        } else if (path.startsWith("/")) {
            int index = baseResource.indexOf("/", 7);

            if (index != -1) {
                URI = baseResource.substring(0, index).concat(path);
            } else {
                URI = baseResource.concat(path);
            }
        }

        /* if (URI != null && URI.endsWith("/")) {
            URI = URI.substring(0, URI.length() - 1);
        } */

        return URI;
    }
}
