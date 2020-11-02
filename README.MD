#### Projects Plugin

##### Overview
In fact, this plugin introduces conventions for configuring subprojects, coprojects, and enforce static dependency versions.

Three new convention files are recognized:
- subprojects.gradle
- coprojects.gradle
- dependencies.gradle

The files should exist in either your `$project.projectDir/gradle/` folder, or in the `$project.projectDir` folder (IE: `$project.projectDir/gradle/coprojects.gradle`)

##### Subprojects and Coprojects

Subprojects are true gradle subprojects, and exist underneath the project directory of the project they are being added to.  Coprojects are Gradle Composite Projects, and are located at the same directory level (siblings) of the project to which they are being added.

The basic syntax for both files is as follows:

Assuming your project is organized as such:

```
+---MainProject
|   +--- Sub1
|   +--- Sub2
+---CoProject1
    +--- CoProj1Sub1
```

Then, MainProject's `subprojects.gradle` file would appear as: 

```groovy
[
  'Sub1',
  'Sub2'
]
```

CoProject1's `subprojects.gradle` file would appear as:

```groovy
[
  'CoProj1Sub1'
]
``` 

and MainProject's `coprojects.gradle` file would appear as:

```groovy
[
  'CoProject1'
]
```

You will want to be careful that you do not attempt to import a project as both a coproject and a subproject.  This is not supported, and the behaviour is undefined.  

As a convenience, subprojects can be added to your dependencies list without using absolute paths, using the following syntax: (Do not import multiple projects with the same name)

```groovy
dependencies{
  local('Sub1')
}
```

This feature will not work across coproject boundaries.

##### Dependencies

The `dependencies.gradle` file in a project defines versions of a dependency that can be referenced later in your project, **as well as within subprojects**.  Please be aware that this can result in non-deterministic builds if you use this capability to define dependencies in projects that are not in the same VCS repository.  It is **highly** recommended (expected) that developers self-monitor their use of this capability and engineer projects specifically to avoid this from happening.  The easiest way to do that is to simply use coprojects for these other projects instead of subprojects.  Alternatively, you can use a superproject management scheme, and intentionally not use dependencies.gradle at the topmost project level.  

The syntax for the `dependencies.gradle` file is as follows:

```groovy
[
  'dep1' : 'mil.navy.nrlssc:dependency-1:1.2.3',
  'dep2': 'mil.navy.nrlssc:dependency-2:5.3.14' 
]
```

Then, in projects where you would like to refer to one of these dependencies, it would be done as follows:

```groovy
dependencies{
  global('dep1')
}
```

##### Coming Soon

The `updateDependencies` task will allow you to update your `dependencies.gradle` file to the latest version for each dependency, with one command.