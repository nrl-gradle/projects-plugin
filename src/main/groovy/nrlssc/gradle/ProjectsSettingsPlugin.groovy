package nrlssc.gradle

import nrlssc.gradle.conventions.GlobalDepsConvention
import nrlssc.gradle.conventions.ProjectsConvention
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class ProjectsSettingsPlugin implements Plugin<Settings>{
    private static Logger logger = LoggerFactory.getLogger(ProjectsSettingsPlugin.class)
    //region statics
    static class NullScript extends Script{

        @Override
        Object run() {
            return null
        }
    }
    
    private static NullScript script = new NullScript()
    
    private static enum FType {
        sub("subprojects.gradle"), co("coprojects.gradle");
        
        private String fileName

        FType(String fileName){
            this.fileName = fileName
        }
        
        String getFileName(){
            return this.fileName
        }
    }

    private static File getProjectsFile(String rootPath, FType ftype)
    {
        String fileName = ftype.getFileName()
        File projectsFile = new File(rootPath + "/gradle/" + fileName)
        if(!projectsFile.exists()) projectsFile = new File(rootPath + "/" + fileName)
        return projectsFile
    }
    //endregion
    
    List<String> getSubProjects(String rootProjPath, String proj = ""){
        List<String> finalProjList = new ArrayList<>()
        String fileProjPath = String.join("/", rootProjPath, proj.replaceAll(":", "/")).replaceAll("//", "/")
        File subprojFile = getProjectsFile(fileProjPath, FType.sub)
        if(subprojFile.exists())
        {
            String[] subprojects = script.evaluate(subprojFile)
            for(int i = 0; i < subprojects.length; i++)
            {
                subprojects[i] = proj + ":" + subprojects[i]
            }
            finalProjList.addAll(subprojects)

            for(String sub in subprojects)
            {
                finalProjList.addAll(getSubProjects(rootProjPath, sub))
            }
        }

        return finalProjList
    }
    
    List<String> getCoProjects(String rootProjPath, List<String> allSubs, String rootProjName)
    {
        List<String> finalProjList = new ArrayList<>()
        List<String> projects = new ArrayList<>()
        projects.add(":")
        projects.addAll(allSubs)
        File root = new File(rootProjPath)
        for(String proj in projects) {
            String fileProjPath = String.join("/", rootProjPath, proj.replaceAll(":", "/")).replaceAll("//", "/")
            File coprojFile = getProjectsFile(fileProjPath, FType.co)
            if (coprojFile.exists()) {
                logger.lifecycle("Setting up coprojects for " + (proj == ":" ? rootProjName : proj))
                String siblingRootPath = (new File(fileProjPath)).getParentFile().getAbsolutePath()
                String[] cops = script.evaluate(coprojFile)
                for (String cop in cops) {
                    String pth = siblingRootPath + "/" + cop
                    File copProj = new File(pth)
                    if(copProj.exists())
                    {
                        String copPath = root.toURI().relativize(copProj.toURI()).toString()
                        logger.lifecycle("\tIncluding coproject: $cop")
                        finalProjList.add(copPath)
                    }
                    else{
                        logger.lifecycle("\tCould not include coproject: ${copProj.path}")
                    }
                }
            }
        }
        return finalProjList
    }
    
    @Override
    void apply(Settings settings) {
        logger.lifecycle("Applying projects plugin to " + settings.rootProject.name)
        settings.gradle.allprojects { proj ->
            //apply plugins?
            proj.configurations.all {
                it.resolutionStrategy {
                    preferProjectModules()
                }
            }
        }
        
        
        String rootPath = settings.buildscript.sourceFile.getParentFile().getAbsolutePath()
        //region subprojects
        List<String> allSubProjects = getSubProjects(rootPath)
        if(allSubProjects.size() > 0) {
            logger.lifecycle("Setting up subprojects for " + settings.rootProject.name)
            
            for (String subProj in allSubProjects) {
                logger.lifecycle("\tIncluding subproject: $subProj")
                settings.include(subProj)
            }
            
        }
        //endregion subprojects
        if (!settings.extensions.getExtraProperties().has('coprojects') || settings.extensions.getExtraProperties().get('coprojects').toString().toBoolean()) {
            //region composites
            List<String> coProjects = getCoProjects(rootPath, allSubProjects, settings.rootProject.name)
            if (coProjects.size() > 0) {
                for (String comp in coProjects) {
                    
                    logger.debug("root include $comp")
                    settings.includeBuild(comp)
                }
            }

            //endregion composites
        }
        //region dependencies
        settings.gradle.allprojects{
            it.pluginManager.apply(GlobalDepsPlugin.class)
            it.pluginManager.apply(SubprojSubstitutionPlugin.class)
            it.convention.plugins.ProjectSettingsConvention = new ProjectsConvention(it)
        }
        //endregion
    }
}
