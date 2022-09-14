buildscript {
}

plugins {

}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}