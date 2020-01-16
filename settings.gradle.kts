rootProject.name = "kython"

include("core", "annotations", "generators")

project(":core").name = "kython-core"
project(":annotations").name = "kython-annotations"
project(":generators").name = "kython-generators"
