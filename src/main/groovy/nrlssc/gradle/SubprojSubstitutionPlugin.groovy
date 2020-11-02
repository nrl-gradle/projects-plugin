package nrlssc.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencySubstitutions
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class SubprojSubstitutionPlugin implements Plugin<Project> {
    private static Logger logger = LoggerFactory.getLogger(SubprojSubstitutionPlugin.class)
    
    @Override
    void apply(Project thisProject) {
        thisProject.gradle.projectsEvaluated {
            if(thisProject == thisProject.rootProject) logger.lifecycle("Subproject substitution plugin applied")
            logger.trace("Subproject substitution plugin applied to ${thisProject.path}")
            
            for(Project p in thisProject.rootProject.getAllprojects()){
                String pth = null
                if(p == thisProject) continue
                if(p == thisProject.rootProject)
                {
                    pth = ":"
                }
                else{
                    pth = p.projectDir.path.replace(p.rootProject.projectDir.path, "").replace("/", ":").replace("\\", ":")
                }
                
                if(pth != null && pth.length() > 0) {
                    try {
                        if(thisProject == thisProject.rootProject) logger.lifecycle("\tsubstitute ${p.group}:${p.name} with project($pth)")
                        thisProject.configurations.all
                        { 
                            it.resolutionStrategy.dependencySubstitution { DependencySubstitutions ds ->
                                ds.substitute ds.module("${p.group}:${p.name}") with ds.project("$pth")
                            }
                        }
                    } catch (Exception ex) {
//                        logger.error("Error: ", ex)
                    }
                }
            }
        }
    }
}
