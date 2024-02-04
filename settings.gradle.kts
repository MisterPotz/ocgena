plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "ocgena"
include("ocgena-math-parexper")
include("ocgena-domain")
include("db_api")
include("ocgena-server")
