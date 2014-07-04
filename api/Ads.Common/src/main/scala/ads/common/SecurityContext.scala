package ads.common

import ads.common.model.User

/**
 * Created by stroumphs on 12/17/13.
 */
object SecurityContext {
    val _local = new ThreadLocal[(Any=>Boolean)]()
    def set[T](f: T=>Boolean) = _local.set ((v:Any) => f(v.asInstanceOf[T]))
    def get[T]() = (v:T)=>{
        if(_local.get() == null) true
        else _local.get()(v)
    }
    def remove[T]() = _local.remove()
}

object UserContext {
    val _local = new ThreadLocal[User]()
    def set(value: User) = _local.set(value)
    def get() : User = {
        if(_local.get() == null) return null.asInstanceOf[User]
        else _local.get()
    }
    def remove() = _local.remove()
}

object BypassSecurityContext {
    val _local = new ThreadLocal[Boolean]()
    def set() = _local.set(true)
    def get() : Boolean = _local.get()
    def remove() = _local.remove()
}
