package ads.common

/**
 * Created by vietnt on 10/17/13.
 */
object Collection{
    sealed class ArrayIterator[T] (array: Array[T]){
    }

    implicit class CollectionContainer[F](val self: F) extends AnyVal {
    }
}
