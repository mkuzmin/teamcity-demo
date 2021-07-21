package patches.buildTypes

import jetbrains.buildServer.configs.kotlin.v2018_1.*
import jetbrains.buildServer.configs.kotlin.v2018_1.ui.*

/*
This patch script was generated by TeamCity on settings change in UI.
To apply the patch, change the buildType with id = 'Deployment_Deploy'
accordingly, and delete the patch script.
*/
changeBuildType(RelativeId("Deployment_Deploy")) {
    check(paused == false) {
        "Unexpected paused: '$paused'"
    }
    paused = true
}