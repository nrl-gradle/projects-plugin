package nrlssc.gradle.helpers

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.util.ConfigureUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ProjectPaths {
    private static Logger logger = LoggerFactory.getLogger(ProjectPaths.class)


    static Project locateProject(Project project, String name) {
        logger.trace("LocateProject(" + project.name + ", " + name + ")")

        Project foundProject = null

        if(project.getName().equalsIgnoreCase(name)) {6
            logger.debug("Found " + name + " as " + project.getName())
            foundProject = project
        }

        if(foundProject == null) {
            foundProject = project.findProject(name)
            if(foundProject != null)
            {
                logger.debug("Found " + name + " under " + project.name)
            }
        }

        if(foundProject == null) {
            Set<Project> subProjects = project.getSubprojects()
            if (subProjects != null) {
                for(Project subP : subProjects){
                    foundProject = locateProject(subP, name)
                    if (foundProject != null) {
                        break
                    }
                }
            }
        }

        if(foundProject != null) {
            return foundProject
        }
        return null
    }

    static Dependency local(Project thisProject, String name){
        return local(thisProject, name, null)
    }
    static Dependency local(Project thisProject, String name, Closure configClosure) {
        logger.trace("dynamic(" + thisProject.name + ", " + name + ")")

        if(name.startsWith(":")){  //holdover from project(':name')
            name = name.substring(1)
        }

        logger.trace("Root Project: " + thisProject.rootProject.name)
        Project foundProject = locateProject(thisProject.rootProject, name)

        Map<String, ?> notation = new HashMap<>()
        Dependency dep
        if(foundProject != null)
        {
            notation.put("path", foundProject.getPath())
            if(configClosure == null) {
                dep = thisProject.getDependencies().project(notation)
            }
            else{
                dep = thisProject.getDependencies().project(notation)
                ConfigureUtil.configure(configClosure, dep)
            }
            
        }
        

        if(dep != null)
        {
            return dep
        }

        logger.lifecycle("Could not resolve project dependency: $name in ${thisProject.name}.\n")
        return null
    }
}
