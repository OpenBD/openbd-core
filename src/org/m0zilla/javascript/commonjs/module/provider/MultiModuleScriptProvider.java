package org.m0zilla.javascript.commonjs.module.provider;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import org.m0zilla.javascript.Context;
import org.m0zilla.javascript.Scriptable;
import org.m0zilla.javascript.commonjs.module.ModuleScript;
import org.m0zilla.javascript.commonjs.module.ModuleScriptProvider;

/**
 * A multiplexer for module script providers.
 * @author Attila Szegedi
 * @version $Id: MultiModuleScriptProvider.java 1746 2011-10-25 15:44:42Z alan $
 */
public class MultiModuleScriptProvider implements ModuleScriptProvider
{
    private final ModuleScriptProvider[] providers;

    /**
     * Creates a new multiplexing module script provider tht gathers the
     * specified providers
     * @param providers the providers to multiplex.
     */
    public MultiModuleScriptProvider(Iterable<? extends ModuleScriptProvider> providers) {
        final List<ModuleScriptProvider> l = new LinkedList<ModuleScriptProvider>();
        for (ModuleScriptProvider provider : providers) {
            l.add(provider);
        }
        this.providers = l.toArray(new ModuleScriptProvider[l.size()]);
    }

    public ModuleScript getModuleScript(Context cx, String moduleId, URI uri,
                                        Scriptable paths) throws Exception {
        for (ModuleScriptProvider provider : providers) {
            final ModuleScript script = provider.getModuleScript(cx, moduleId,
                    uri, paths);
            if(script != null) {
                return script;
            }
        }
        return null;
    }
}
