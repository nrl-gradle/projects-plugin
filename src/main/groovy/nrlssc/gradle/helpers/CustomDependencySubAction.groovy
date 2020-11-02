package nrlssc.gradle.helpers

import org.gradle.api.Action
import org.gradle.api.initialization.ConfigurableIncludedBuild
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CustomDependencySubAction implements Action<ConfigurableIncludedBuild> {
    private static Logger logger = LoggerFactory.getLogger(CustomDependencySubAction.class)

    private String mod
    CustomDependencySubAction(String mod)
    {
        this.mod = mod
    }
    
    @Override
    void execute(ConfigurableIncludedBuild configurableIncludedBuild) {
        configurableIncludedBuild.dependencySubstitution { ds ->
            ds.substitute ds.module(mod) with ds.project(':')
        }
    }
}
