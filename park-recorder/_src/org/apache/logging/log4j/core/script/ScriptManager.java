/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.script;

import java.io.File;
import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.script.AbstractScript;
import org.apache.logging.log4j.core.script.ScriptFile;
import org.apache.logging.log4j.core.util.FileWatcher;
import org.apache.logging.log4j.core.util.WatchManager;
import org.apache.logging.log4j.status.StatusLogger;

public class ScriptManager
implements Serializable,
FileWatcher {
    private static final long serialVersionUID = -2534169384971965196L;
    private static final String KEY_THREADING = "THREADING";
    private static final Logger logger = StatusLogger.getLogger();
    private final Configuration configuration;
    private final ScriptEngineManager manager = new ScriptEngineManager();
    private final ConcurrentMap<String, ScriptRunner> scriptRunners = new ConcurrentHashMap<String, ScriptRunner>();
    private final String languages;
    private final WatchManager watchManager;

    public ScriptManager(Configuration iterator, WatchManager object) {
        this.configuration = iterator;
        this.watchManager = object;
        iterator = this.manager.getEngineFactories();
        if (logger.isDebugEnabled()) {
            object = new StringBuilder();
            int n = iterator.size();
            logger.debug("Installed {} script engine{}", (Object)n, (Object)(n != 1 ? "s" : ""));
            iterator = iterator.iterator();
            while (iterator.hasNext()) {
                ScriptEngineFactory scriptEngineFactory = (ScriptEngineFactory)iterator.next();
                String string = Objects.toString(scriptEngineFactory.getParameter(KEY_THREADING), null);
                if (string == null) {
                    string = "Not Thread Safe";
                }
                StringBuilder stringBuilder = new StringBuilder();
                List<String> list = scriptEngineFactory.getNames();
                for (String string2 : list) {
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append(", ");
                    }
                    stringBuilder.append(string2);
                }
                if (((StringBuilder)object).length() > 0) {
                    ((StringBuilder)object).append(", ");
                }
                ((StringBuilder)object).append((CharSequence)stringBuilder);
                boolean bl = scriptEngineFactory.getScriptEngine() instanceof Compilable;
                logger.debug("{} version: {}, language: {}, threading: {}, compile: {}, names: {}, factory class: {}", (Object)scriptEngineFactory.getEngineName(), (Object)scriptEngineFactory.getEngineVersion(), (Object)scriptEngineFactory.getLanguageName(), (Object)string, (Object)bl, (Object)list, (Object)scriptEngineFactory.getClass().getName());
            }
            this.languages = ((StringBuilder)object).toString();
            return;
        }
        object = new StringBuilder();
        Iterator iterator2 = iterator.iterator();
        while (iterator2.hasNext()) {
            for (String string : ((ScriptEngineFactory)iterator2.next()).getNames()) {
                if (((StringBuilder)object).length() > 0) {
                    ((StringBuilder)object).append(", ");
                }
                ((StringBuilder)object).append(string);
            }
        }
        this.languages = ((StringBuilder)object).toString();
    }

    public void addScript(AbstractScript abstractScript) {
        Object object = this.manager.getEngineByName(abstractScript.getLanguage());
        if (object == null) {
            logger.error("No ScriptEngine found for language " + abstractScript.getLanguage() + ". Available languages are: " + this.languages);
            return;
        }
        if (object.getFactory().getParameter(KEY_THREADING) == null) {
            this.scriptRunners.put(abstractScript.getName(), new ThreadLocalScriptRunner(abstractScript));
        } else {
            this.scriptRunners.put(abstractScript.getName(), new MainScriptRunner((ScriptEngine)object, abstractScript));
        }
        if (abstractScript instanceof ScriptFile) {
            abstractScript = (ScriptFile)abstractScript;
            object = ((ScriptFile)abstractScript).getPath();
            if (((ScriptFile)abstractScript).isWatched() && object != null) {
                this.watchManager.watchFile(object.toFile(), this);
            }
        }
    }

    public Bindings createBindings(AbstractScript abstractScript) {
        return this.getScriptRunner(abstractScript).createBindings();
    }

    public AbstractScript getScript(String object) {
        if ((object = (ScriptRunner)this.scriptRunners.get(object)) != null) {
            return object.getScript();
        }
        return null;
    }

    @Override
    public void fileModified(File object) {
        if ((object = (ScriptRunner)this.scriptRunners.get(((File)object).toString())) == null) {
            logger.info("{} is not a running script");
            return;
        }
        ScriptEngine scriptEngine = object.getScriptEngine();
        object = object.getScript();
        if (scriptEngine.getFactory().getParameter(KEY_THREADING) == null) {
            this.scriptRunners.put(((AbstractScript)object).getName(), new ThreadLocalScriptRunner((AbstractScript)object));
            return;
        }
        this.scriptRunners.put(((AbstractScript)object).getName(), new MainScriptRunner(scriptEngine, (AbstractScript)object));
    }

    public Object execute(String object, Bindings bindings) {
        if ((object = (ScriptRunner)this.scriptRunners.get(object)) == null) {
            logger.warn("No script named {} could be found");
            return null;
        }
        return AccessController.doPrivileged(new PrivilegedAction<Object>((ScriptRunner)object, bindings){
            final /* synthetic */ ScriptRunner val$scriptRunner;
            final /* synthetic */ Bindings val$bindings;
            {
                this.val$scriptRunner = scriptRunner;
                this.val$bindings = bindings;
            }

            @Override
            public Object run() {
                return this.val$scriptRunner.execute(this.val$bindings);
            }
        });
    }

    private ScriptRunner getScriptRunner(AbstractScript abstractScript) {
        return (ScriptRunner)this.scriptRunners.get(abstractScript.getName());
    }

    class ThreadLocalScriptRunner
    extends AbstractScriptRunner {
        private final AbstractScript script;
        private final ThreadLocal<MainScriptRunner> runners;

        public ThreadLocalScriptRunner(AbstractScript abstractScript) {
            this.runners = new ThreadLocal<MainScriptRunner>(){

                @Override
                protected MainScriptRunner initialValue() {
                    ScriptEngine scriptEngine = ScriptManager.this.manager.getEngineByName(ThreadLocalScriptRunner.this.script.getLanguage());
                    return new MainScriptRunner(scriptEngine, ThreadLocalScriptRunner.this.script);
                }
            };
            this.script = abstractScript;
        }

        @Override
        public Object execute(Bindings bindings) {
            return this.runners.get().execute(bindings);
        }

        @Override
        public AbstractScript getScript() {
            return this.script;
        }

        @Override
        public ScriptEngine getScriptEngine() {
            return this.runners.get().getScriptEngine();
        }
    }

    class MainScriptRunner
    extends AbstractScriptRunner {
        private final AbstractScript script;
        private final CompiledScript compiledScript;
        private final ScriptEngine scriptEngine;

        public MainScriptRunner(final ScriptEngine scriptEngine, final AbstractScript abstractScript) {
            this.script = abstractScript;
            this.scriptEngine = scriptEngine;
            CompiledScript compiledScript = null;
            if (scriptEngine instanceof Compilable) {
                logger.debug("Script {} is compilable", (Object)abstractScript.getName());
                compiledScript = AccessController.doPrivileged(new PrivilegedAction<CompiledScript>(){

                    @Override
                    public CompiledScript run() {
                        try {
                            return ((Compilable)((Object)scriptEngine)).compile(abstractScript.getScriptText());
                        }
                        catch (Throwable throwable) {
                            logger.warn("Error compiling script", throwable);
                            return null;
                        }
                    }
                });
            }
            this.compiledScript = compiledScript;
        }

        @Override
        public ScriptEngine getScriptEngine() {
            return this.scriptEngine;
        }

        @Override
        public Object execute(Bindings bindings) {
            if (this.compiledScript != null) {
                try {
                    return this.compiledScript.eval(bindings);
                }
                catch (ScriptException scriptException) {
                    logger.error("Error running script " + this.script.getName(), (Throwable)scriptException);
                    return null;
                }
            }
            try {
                return this.scriptEngine.eval(this.script.getScriptText(), bindings);
            }
            catch (ScriptException scriptException) {
                logger.error("Error running script " + this.script.getName(), (Throwable)scriptException);
                return null;
            }
        }

        @Override
        public AbstractScript getScript() {
            return this.script;
        }
    }

    static interface ScriptRunner {
        public Bindings createBindings();

        public Object execute(Bindings var1);

        public AbstractScript getScript();

        public ScriptEngine getScriptEngine();
    }

    abstract class AbstractScriptRunner
    implements ScriptRunner {
        private static final String KEY_STATUS_LOGGER = "statusLogger";
        private static final String KEY_CONFIGURATION = "configuration";

        private AbstractScriptRunner() {
        }

        @Override
        public Bindings createBindings() {
            SimpleBindings simpleBindings = new SimpleBindings();
            simpleBindings.put(KEY_CONFIGURATION, (Object)ScriptManager.this.configuration);
            simpleBindings.put(KEY_STATUS_LOGGER, (Object)logger);
            return simpleBindings;
        }
    }
}

