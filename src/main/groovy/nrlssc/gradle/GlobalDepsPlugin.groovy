package nrlssc.gradle

import nrlssc.gradle.conventions.GlobalDepsConvention
import nrlssc.gradle.conventions.ProjectsConvention
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class GlobalDepsPlugin implements Plugin<Project> {
    private static Logger logger = LoggerFactory.getLogger(GlobalDepsPlugin.class)
    static class NullScript extends Script{

        @Override
        Object run() {
            return null
        }
    }

    private static NullScript script = new NullScript()
    
    
    @Override
    void apply(Project thisProject) {
        File depFile = thisProject.file("gradle/dependencies.gradle")
        if(!depFile.exists()) depFile = thisProject.file("dependencies.gradle")

        if(depFile.exists())
        {
            logger.lifecycle('Configuring dependencies.gradle of project ' + thisProject.name)
            Map<String, String> staticDeps = script.evaluate(depFile)

            GlobalDepsConvention conv = thisProject.convention.plugins.GlobalDepsConvention
            if(conv == null)
            {
                conv = new GlobalDepsConvention(thisProject)
                thisProject.convention.plugins.GlobalDepsConvention = conv
            }
            conv.putAll(staticDeps)
            thisProject.subprojects{ proj ->
                logger.lifecycle("\tApplying to ${proj.name}")
                GlobalDepsConvention conv2 = proj.convention.plugins.GlobalDepsConvention
                if(conv2 == null)
                {
                    conv2 = new GlobalDepsConvention(proj)
                    proj.convention.plugins.GlobalDepsConvention = conv2
                }
                conv2.putAll(staticDeps)
            }

        }
    }
}
