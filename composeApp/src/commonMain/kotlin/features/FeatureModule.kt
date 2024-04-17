package features

import core.ktor.ktorModule
import features.auth.authModule
import org.kodein.di.DI

internal val featureModule = DI
    .Module(
    name = "featureModule",
    init = {
        importAll(
            authModule,
            ktorModule
        )
    },
)
