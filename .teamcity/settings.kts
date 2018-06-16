import jetbrains.buildServer.configs.kotlin.v2018_1.*
import jetbrains.buildServer.configs.kotlin.v2018_1.buildFeatures.dockerSupport
import jetbrains.buildServer.configs.kotlin.v2018_1.buildFeatures.freeDiskSpace
import jetbrains.buildServer.configs.kotlin.v2018_1.buildSteps.dockerCommand
import jetbrains.buildServer.configs.kotlin.v2018_1.buildSteps.dockerCompose
import jetbrains.buildServer.configs.kotlin.v2018_1.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2018_1.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2018_1.projectFeatures.dockerRegistry
import jetbrains.buildServer.configs.kotlin.v2018_1.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2018_1.vcs.GitVcsRoot

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2017.2"

project {

    features {
        dockerRegistry {
            id = "PROJECT_EXT_1087"
            name = "Docker Registry"
            url = "https://docker.io"
            userName = "mkuzmin"
            password = "credentialsJSON:278ee196-b1e6-4894-8c95-64f0370d0a5b"
        }
    }
    subProjectsOrder = arrayListOf(RelativeId("Backend"), RelativeId("Frontend"), RelativeId("Tests"), RelativeId("Deployment"))

    subProject(Backend)
    subProject(Frontend)
    subProject(Deployment)
    subProject(Tests)
}


object Backend : Project({
    name = "Backend"

    buildType(Backend_DockerImage)
    buildType(Backend_Build)
})

object Backend_Build : BuildType({
    name = "Build App"

    artifactRules = "backend/build/libs/todo.jar"

    vcs {
        root(DslContext.settingsRoot, "+:backend/", "+:gradle", "+:build.gradle", "+:settings.gradle", "+:gradlew")
    }

    steps {
        gradle {
            tasks = "clean :backend:build"
            dockerImage = "openjdk:8u131-jdk-alpine"
            dockerRunParameters = "-v gradle:/root/.gradle/"
        }
    }

    features {
        freeDiskSpace {
            failBuild = false
        }
    }
})

object Backend_DockerImage : BuildType({
    name = "Docker Image"

    buildNumberPattern = "${Backend_Build.depParamRefs.buildNumber}"

    vcs {
        root(DslContext.settingsRoot, "+:backend/Dockerfile")
    }

    steps {
        dockerCommand {
            commandType = build {
                source = path {
                    path = "backend/Dockerfile"
                }
                namesAndTags = "mkuzmin/todo-backend:%build.number%"
            }
        }
        script {
            name = "Docker Push"
            scriptContent = "docker push mkuzmin/todo-backend:%build.number%"
        }
    }

    features {
        dockerSupport {
            cleanupPushedImages = true
            loginToRegistry = on {
                dockerRegistryId = "PROJECT_EXT_1087"
            }
        }
    }

    dependencies {
        dependency(Backend_Build) {
            snapshot {
                onDependencyFailure = FailureAction.CANCEL
                onDependencyCancel = FailureAction.CANCEL
            }

            artifacts {
                artifactRules = "todo.jar => backend/build/libs/"
            }
        }
    }
})


object Deployment : Project({
    name = "Deployment"

    vcsRoot(Deployment_Todo)

    buildType(Deployment_Deploy)
})

object Deployment_Deploy : BuildType({
    name = "Deploy"

    buildNumberPattern = "${SeleniumTests.depParamRefs.buildNumber}.%build.counter%"

    vcs {
        root(Deployment_Todo, "+:docker-compose.yml", "+:.env")
    }

    steps {
        script {
            scriptContent = "docker-compose up -d"
        }
    }

    dependencies {
        dependency(SeleniumTests) {
            snapshot {
                onDependencyFailure = FailureAction.CANCEL
                onDependencyCancel = FailureAction.CANCEL
            }

            artifacts {
                artifactRules = ".teamcity/properties/build.finish.properties.gz"
            }
        }
    }
})

object Deployment_Todo : GitVcsRoot({
    name = "todo"
    url = "git@github.com:mkuzmin/teamcity-demo.git"
    userNameStyle = GitVcsRoot.UserNameStyle.FULL
    authMethod = uploadedKey {
        uploadedKey = "teamcity"
    }
})


object Frontend : Project({
    name = "Frontend"

    buildType(Frontend_BuildDockerImage)
})

object Frontend_BuildDockerImage : BuildType({
    name = "Docker Image"

    vcs {
        root(DslContext.settingsRoot, "+:frontend => .")
    }

    steps {
        script {
            name = "Download Dependencies"
            scriptContent = """
                npm install
                ./node_modules/.bin/bower --allow-root install
            """.trimIndent()
            dockerImage = "node:6.9.1"
        }
        dockerCommand {
            commandType = build {
                source = path {
                    path = "Dockerfile"
                }
                namesAndTags = "mkuzmin/todo-frontend:%build.number%"
            }
        }
        script {
            name = "Docker Push"
            scriptContent = "docker push mkuzmin/todo-frontend:%build.number%"
        }
    }

    features {
        dockerSupport {
            cleanupPushedImages = true
            loginToRegistry = on {
                dockerRegistryId = "PROJECT_EXT_1087"
            }
        }
    }
})


object Tests : Project({
    name = "Tests"

    buildType(SeleniumTests)
})

object SeleniumTests : BuildType({
    name = "Selenium Tests"

    artifactRules = "errorShots => errorShots.zip"

    params {
        param("env.BACKEND_VERSION", "${Backend_DockerImage.depParamRefs.buildNumber}")
        param("env.FRONTEND_VERSION", "${Frontend_BuildDockerImage.depParamRefs.buildNumber}")
    }

    vcs {
        root(DslContext.settingsRoot, "+:tests => .")
    }

    steps {
        dockerCompose {
        }
        script {
            scriptContent = """
                npm install
                rm -f errorShots/*
                ./node_modules/.bin/wdio --host chrome --reporters teamcity
            """.trimIndent()
            dockerImage = "node:6.9.1"
        }
    }

    triggers {
        vcs {
            triggerRules = "-:.teamcity/*"
            branchFilter = ""
            watchChangesInDependencies = true
        }
    }

    dependencies {
        dependency(Backend_DockerImage) {
            snapshot {
                onDependencyFailure = FailureAction.CANCEL
                onDependencyCancel = FailureAction.CANCEL
            }

            artifacts {
                artifactRules = ".teamcity/properties/build.finish.properties.gz => a/"
            }
        }
        dependency(Frontend_BuildDockerImage) {
            snapshot {
                onDependencyFailure = FailureAction.CANCEL
                onDependencyCancel = FailureAction.CANCEL
            }

            artifacts {
                artifactRules = ".teamcity/properties/build.finish.properties.gz => b/"
            }
        }
    }
})
