package nrlssc.gradle.conventions

import nrlssc.gradle.helpers.ProjectPaths
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ProjectsConvention {
    private static Logger logger = LoggerFactory.getLogger(ProjectsConvention.class)
    
    private Project thisProject
    ProjectsConvention(Project thisProject)
    {
        this.thisProject = thisProject
    }
    
    final Dependency local(String name){
        logger.trace("Convention: local(" + name + ")")
        ProjectPaths.local(thisProject, name)
    }

    final Dependency local(String name, Closure configClosure) {
        logger.trace("Convention: local(" + name + ")")
        ProjectPaths.local(thisProject, name, configClosure)
    }
}
