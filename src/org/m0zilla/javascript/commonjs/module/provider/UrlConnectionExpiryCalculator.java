package org.m0zilla.javascript.commonjs.module.provider;

import java.net.URLConnection;

/**
 * Implemented by objects that can be used as heuristic strategies for
 * calculating the expiry of a cached resource in cases where the server of the
 * resource doesn't provide explicit expiry information.
 * @author Attila Szegedi
 * @version $Id: UrlConnectionExpiryCalculator.java 1746 2011-10-25 15:44:42Z alan $
 */
public interface UrlConnectionExpiryCalculator
{
    /**
     * Given a URL connection, returns a calculated heuristic expiry time (in
     * terms of milliseconds since epoch) for the resource.
     * @param urlConnection the URL connection for the resource
     * @return the expiry for the resource
     */
    public long calculateExpiry(URLConnection urlConnection);
}
