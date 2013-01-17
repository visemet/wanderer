package edu.caltech.visemet.wanderer;

/**
 *
 * @author Max Hirschhorn #visemet
 */
public class Resources {

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

        return URI;
    }
}
