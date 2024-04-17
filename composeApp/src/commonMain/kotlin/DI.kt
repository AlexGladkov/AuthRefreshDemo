import features.auth.data.AuthTokenDataSource
import features.featureModule
import org.kodein.di.DI
import org.kodein.di.DirectDI
import org.kodein.di.bindSingleton
import org.kodein.di.direct

object DIHolder {
    
    val di: DirectDI
        get() = requireNotNull(_di)
    
    private var _di: DirectDI? = null
    
    fun init(authTokenDataSource: AuthTokenDataSource) {
        _di = DI {
            bindSingleton { authTokenDataSource }
            importAll(featureModule)
        }.direct
    }
}