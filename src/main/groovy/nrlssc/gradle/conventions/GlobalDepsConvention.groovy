package nrlssc.gradle.conventions

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class GlobalDepsConvention {
    private static Logger logger = LoggerFactory.getLogger(GlobalDepsConvention.class)
    private Project thisProject
    private Map<String, String> globalDeps = new HashMap<>()

    GlobalDepsConvention(Project thisProject)
    {
        this.thisProject = thisProject
    }

    final Dependency global(String name){
        if(!globalDeps.containsKey(name)) throw new RuntimeException("No global dependency found named '$name'")
        logger.trace("Convention: global(" + name + ") = " + globalDeps.get(name))
        return thisProject.getDependencies().create(globalDeps.get(name))
    }
    
    void put(String key, String dep)
    {
        globalDeps.put(key, dep)    
    }
    
    void putAll(Map<String, String> otherDeps)
    {
        globalDeps.putAll(otherDeps)
    }

}
