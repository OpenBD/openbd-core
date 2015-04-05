package org.m0zilla.javascript.commonjs.module;

import java.io.Serializable;
import java.net.URI;

import org.m0zilla.javascript.Script;

/**
 * Represents a compiled CommonJS module script. The {@link Require} functions
 * use them and obtain them through a {@link ModuleScriptProvider}. Instances
 * are immutable.
 * @author Attila Szegedi
 * @version $Id: ModuleScript.java 1746 2011-10-25 15:44:42Z alan $
 */
public class ModuleScript implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final Script script;
    private final URI uri;
    private final URI base;

    /**
     * Creates a new CommonJS module.
     * @param script the script representing the code of the module.
     * @param uri the URI of the module.
     * @param base the base URI, or null.
     */
    public ModuleScript(Script script, URI uri, URI base) {
        this.script = script;
        this.uri = uri;
        this.base = base;
    }

    /**
     * Returns the script object representing the code of the module.
     * @return the script object representing the code of the module.
     */
    public Script getScript(){
        return script;
    }

    /**
     * Returns the URI of the module.
     * @return the URI of the module.
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Returns the base URI from which this module source was loaded, or null
     * if it was loaded from an absolute URI.
     * @return the base URI, or null.
     */
    public URI getBase() {
        return base;
    }

    /**
     * Returns true if this script has a base URI and has a source URI that
     * is contained within that base URI.
     * @return true if this script is contained within its sandbox base URI.
     */
    public boolean isSandboxed() {
        return base != null
                && uri != null
                && !base.relativize(uri).isAbsolute();
    }
}
