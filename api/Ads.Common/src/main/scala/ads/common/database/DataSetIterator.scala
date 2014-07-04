package ads.common.database

import java.sql.{ResultSet, Connection}

class DataSetIterator[T](connection: Connection, set: ResultSet, convert: (ResultSet) => T) extends Iterator[T]{
    def hasNext: Boolean = {
        val r = set.next()
        if (!r) free()
        r
    }
    def free(){
        if (!connection.isClosed) connection.close()
    }
    def next(): T = convert(set)
}
