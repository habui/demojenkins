package ads.common.database

import com.mchange.v2.c3p0.ComboPooledDataSource
import scala.reflect.ClassTag
import ads.common.{JsonExt, IItem, Reflect}
import java.sql.{Statement, ResultSet, Connection, PreparedStatement}
import java.util.Date
import scala.collection.mutable.ArrayBuffer
import com.netflix.config.DynamicPropertyFactory
import java.io._
import java.nio.charset.Charset
import scala.collection.mutable
import ads.common.model.Config

object Connections {
    val pool = new ComboPooledDataSource()
    pool.setDriverClass("com.mysql.jdbc.Driver")

    val factory = DynamicPropertyFactory.getInstance()
    pool.setMaxPoolSize(100)
    pool.setMaxStatements(0)
    pool.setJdbcUrl(factory.getStringProperty("db.url", "jdbc:mysql://10.30.58.204:3306/dev_adtima?characterEncoding=UTF-8").getValue)
    pool.setUser(factory.getStringProperty("db.user", "adtima").getValue)
    pool.setPassword(factory.getStringProperty("db.password", "adtima@DB4Dev").getValue)
    pool.setAutoCommitOnClose(true)
    pool.setTestConnectionOnCheckout(true)
    //pool.setJdbcUrl("jdbc:mysql://210.211.108.205:3737/clicks?characterEncoding=UTF-8")
    //pool.setUser("admin")
    //pool.setPassword("1234567")


//    pool.setJdbcUrl("jdbc:mysql://localhost:3306/123clicks?characterEncoding=UTF-8")
//    pool.setUser("root")
//    pool.setPassword("1234567")

}

class JsonDataStore[T <: IItem](implicit tag: ClassTag[T]) extends IDataService[T]{

    val AddOrUpdate = 1
    val Delete = 2

    var id : Int = 0
    var addOrUpdateHandler: T=>Unit = null

    lazy val path = Config.pathData.get() + tag.runtimeClass.getSimpleName

    override def onAddOrUpdate(f: (T) => Unit): Unit = addOrUpdateHandler = f

    override def start(){
        for (x <- loadAll()) {
            if (addOrUpdateHandler != null) addOrUpdateHandler(x)
        }
    }


    private def append(item: T){
        val text = JsonExt.gson.toJson(item)
        val buf = text.getBytes(Charset.forName("UTF-8"))

        this.synchronized{
            val f = new FileOutputStream(path, true)
            val output = new DataOutputStream(f)
            output.writeInt(AddOrUpdate)
            output.writeInt(buf.length)
            output.write(buf, 0, buf.length)
            output.close()
        }
    }

    def remove(id: Int) = {
        this.synchronized{
            val f = new FileOutputStream(path, true)
            val output = new DataOutputStream(f)
            output.writeInt(Delete)
            output.writeInt(id)
            output.close()
        }
    }

    def saveAll(list: List[T]){

        this.synchronized{
            val file = new File(path)
            if(file.exists()) {
                file.delete()
            }
            val f = new FileOutputStream(path, true)
            val output = new DataOutputStream(f)

            for(item <- list){
                val text = JsonExt.gson.toJson(item)
                val buf = text.getBytes(Charset.forName("UTF-8"))

                output.writeInt(AddOrUpdate)
                output.writeInt(buf.length)
                output.write(buf, 0, buf.length)
            }

            output.close()
        }
    }

    def save(item: T) = {
        id = id + 1
        item.id = id
        append(item)
        if (addOrUpdateHandler != null) addOrUpdateHandler(item)
        item.id
    }

    def update(item: T) = {
        append(item)
        if (addOrUpdateHandler != null) addOrUpdateHandler(item)
        true
    }

    def loadAll() = {
        val dict = new mutable.HashMap[Int,T]()
        this.synchronized{
            val file = new File(path)
            if(!file.exists()) {
                val fw = new FileWriter(path)
                try fw.write("")
                finally fw.close()
            }
            val f = new FileInputStream(path)
            val input = new DataInputStream(f)
            val buf = new Array[Byte](1024*1024)
            val utf8 = Charset.forName("UTF-8")

            while (input.available() > 0) {
                val k = input.readInt()
                k match{
                    case AddOrUpdate => {
                        val l = input.readInt()
                        input.read(buf, 0, l)
                        val text = new String(buf, 0, l, utf8)
                        val item = JsonExt.gson.fromJson(text,tag.runtimeClass).asInstanceOf[T]

                        if (dict.contains(item.id)) dict.remove(item.id)
                        dict += ((item.id, item))

                        if (item.id > id) id = item.id
                    }

                    case Delete =>{
                        val t = input.readInt()
                        dict.remove(t)
                    }
                }
            }

            input.close()
        }

        dict.valuesIterator
    }

    def load(id: Int): T = ???

    def list(from: Int, count: Int, sortBy: String, direction: String): PagingResult[T] = ???

    def listByIds(ids: List[Int]): Array[T] = ???

    def listByReferenceId(from: Int, count: Int, id: Int, sortBy: String, direction: String): PagingResult[T] = ???

    def countByRef(id: Int): Int = ???

    def count(f: (T) => Boolean): Int = ???

    def list(f: (T) => Boolean): List[T] = ???

    def list[A](f: (T) => Boolean, transform: (T) => A): List[A] = ???

    def search(query: String, pId: Int, isDisable: Boolean, take: Int = 10): List[T] = ???

    def enable(id: Int): Unit = ???

    def disable(id: Int): Unit = ???

    def listDisable(from: Int, count: Int, sortBy: String, direction: String): PagingResult[T] = ???
}

class SimpleDatabase[T <: IItem](_tableName: String = null)(factory: ()=>T)(implicit tag: ClassTag[T]){

    lazy val insertSql = buildInsert()
    lazy val selectSql = s"select * from $tableName where id = ?"
    lazy val deleteSql = s"update `$tableName` set `disable`='1' where id = ?"
    lazy val enableItemSql = s"update `$tableName` set `disable`='0' where id = ?"
    lazy val updateSql = buildUpdate()

    def tableName = if (_tableName != null) _tableName else tag.runtimeClass.getSimpleName

    //def tableName = tag.runtimeClass.getSimpleName
    lazy val properties = Reflect.getProperties(tag.runtimeClass)

    setupTable()

    def setupTable(){
        createTable()
    }

    // create table
    def buildCreate(): String = {
        val sb = new StringBuilder()

        sb.append(s"create table if not exists `$tableName` (\n")
        sb.append("id int not null auto_increment,\n")

        for ((name,record) <- properties) {
            if (name != "id") {
                val clazz = record.clazz
                if (clazz == classOf[String] ||
                    clazz == classOf[Array[Int]] ||
                    clazz == classOf[List[String]] ||
                    clazz == classOf[List[Int]] ||
                    clazz == classOf[java.util.HashMap[_,_]]){
                    sb.append(s"`$name` varchar (20000) null")
                }
                else if (clazz == classOf[Int]) {
                    sb.append(s"`$name` int null")
                }
                else if (clazz == classOf[Long] || clazz == classOf[Date]) {
                    sb.append(s"`$name` bigint null")
                }
                else if (clazz == classOf[Float]) {
                    sb.append(s"`$name` float null")
                }
                else if (clazz == classOf[Double]) {
                    sb.append(s"`$name` double null")
                }
                else if(clazz == classOf[Boolean]) {
                    sb.append(s"`$name` boolean default false")
                }

                sb.append(",\n")
            }
        }

        sb.append("primary key (id));")
        return sb.toString()
    }

    def buildInsert(): String = {
        val sb = new StringBuilder()
        sb.append(s"insert into `${tableName}`(")
        for (name <- properties.keySet) {
            if (name != "id") {
                sb.append(s"`$name`")
                sb.append(",")
            }
        }
        sb.deleteCharAt(sb.length - 1)
        sb.append(")")
        sb.append(" values(")

        for (i <- 1 until properties.size) {
            sb.append("?,")
        }
        sb.deleteCharAt(sb.length - 1)
        sb.append(")")

        return sb.toString()
    }

    def buildUpdate(): String = {
        val sb = new StringBuilder()
        sb.append(s"update `${tableName}` set ")
        for (name <- properties.keySet) {
            if (name != "id") {
                sb.append(s" `$name` = ?,")
            }
        }

        sb.deleteCharAt(sb.length - 1)
        sb.append(" where id = ?;")

        return sb.toString()
    }

    def buildAddColumns(names: List[String]): String = {
        val sb = new StringBuilder()
        sb.append(s"alter table `$tableName`")
        for (name <- names) {
            sb.append("\n add ")
            val clazz = properties.get(name).get.clazz
            if (clazz == classOf[String] ||
                clazz == classOf[Array[Int]] ||
                clazz == classOf[List[String]] ||
                clazz == classOf[List[Int]] ||
                clazz == classOf[java.util.HashMap[_,_]]) {
                sb.append(s"`$name` varchar (20000) null")
            }
            else if (clazz == classOf[Int]) {
                sb.append(s"`$name` int null default 0")
            }
            else if (clazz == classOf[Long] || clazz == classOf[Date]) {
                sb.append(s"`$name` bigint null default 0")
            }
            else if (clazz == classOf[Float]) {
                sb.append(s"`$name` float null default 0")
            }
            else if (clazz == classOf[Double]) {
                sb.append(s"`$name` double null default 0")
            }
            else if(clazz == classOf[Boolean]) {
                sb.append(s"`$name` boolean default false")
            }
            sb.append(",")
        }
        return sb.toString()
    }

    def set(statement: PreparedStatement, item: T) {
        var count = 1
        for ((key,record) <- properties) {
            if (key != "id") {
                val value = record.get(item)

                value match {
                    case x: Int => statement.setInt(count, x)
                    case x: Long => statement.setLong(count, x)
                    case x: Float => statement.setFloat(count, x)
                    case x: Double => statement.setDouble(count, x)
                    case x: String => statement.setString(count, x)
                    case x: Date => statement.setLong(count, x.getTime)
                    case x: Boolean => statement.setBoolean(count, x)

//                    case x: util.ArrayList[String] => statement.setString(count, x.reduce((m,n)=>m + "," + n))
//                    case x: util.ArrayList[Int] => statement.setString(count, x.reduce((m,n)=>m + "," + n))
                    case _ =>{
                        if (record.clazz == classOf[String]){
                            val x = value.asInstanceOf[String]
                            statement.setString(count, x)
                        }
                        else if (record.clazz == classOf[List[String]]) {
                                val x = value.asInstanceOf[List[String]]
                                if (x != null) statement.setString(count, x.reduce((m,n)=>m + "," + n))
                                else statement.setString(count, null)
                            }
                        else if (record.clazz == classOf[Array[Int]]){
                            val x = value.asInstanceOf[Array[Int]]
                            if (x != null && !x.isEmpty) statement.setString(count, x.map(t=>t.toString).reduce((m,n)=>m + "," + n))
                            else statement.setString(count, null)
                        } else if (record.clazz == classOf[java.util.HashMap[_,_]]) {
                            val x = value.asInstanceOf[java.util.HashMap[_,_]]
                            if(x != null && !x.isEmpty) statement.setString(count, JsonExt.gson.toJson(x))
                            else statement.setString(count, null)
                        }
                        else throw new Exception("not supported: "+ record.clazz.toString)
                    }
                }

                count += 1
            }
        }
    }

    def createTable() = wrap(connection=>{
        val sql = buildCreate()

        println(sql)

        val s = connection.prepareStatement(sql)
        s.execute()
    })

    def wrap[T](act: (Connection) => T): T = {
        val connection = Connections.pool.getConnection
        try {
            act(connection)
        }
        finally {
            connection.close()
        }
    }

    def read(set: ResultSet): T = {
        val item = factory()
        for ((name, record) <- properties) {

            record.clazz match {
                case q if q == classOf[String] => record.set(item, set.getString(name))
                case q if q == classOf[Int] => record.set(item, new Integer(set.getInt(name)))
                case q if q == classOf[Long] => record.set(item, new java.lang.Long(set.getLong(name)))
                case q if q == classOf[Float] => record.set(item, new java.lang.Float(set.getFloat(name)))
                case q if q == classOf[Double] => record.set(item, new java.lang.Double(set.getDouble(name)))
                case q if q == classOf[Date] => record.set(item, new Date(set.getLong(name)))
                case q if q == classOf[Boolean] => record.set(item, new java.lang.Boolean(set.getBoolean(name)))

                case q if q == classOf[List[String]] => {
                    val values = set.getString(name)
                    if (values != null) {
                        record.set(item, values.split(",").toList)
                    }
                }
                case q if q == classOf[Array[Int]] => {
                    val values = set.getString(name)
                    if (values != null) {
                        val list = values.split(",").map(f=>f.toInt).toList.toArray
                        record.set(item, list)
                    }
                }
                case q if q == classOf[java.util.HashMap[_,_]] => {
                    val values = set.getString(name)
                    if(values != null) {
                        val map = JsonExt.gson.fromJson(values, classOf[java.util.HashMap[_,_]])
                        record.set(item, map)
                    }
                }
            }
        }
        item
    }

    def save(item: T): Int = wrap(connection => {
        val statement = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)

        set(statement, item)

        statement.execute()
        val iter = statement.getGeneratedKeys
        iter.next()
        val id = iter.getInt(1)
        item.id = id
        id
        item.id
    })

    def update(item: T) = wrap(connection => {
        println(updateSql)
        val statement = connection.prepareStatement(updateSql)
        set(statement, item)
        statement.setInt(properties.size, item.id)
        statement.execute()
    })

    def addColumns(names: List[String]) = wrap(connection => {
        val sql = buildAddColumns(names).dropRight(1) //remove the last ',' for the right sql syntax
        println(sql)
        val statement = connection.prepareStatement(sql)
        statement.execute()
    })

    def load(id: Int): T = wrap(connection => {
        val statement = connection.prepareStatement(selectSql)
        statement.setInt(1,id)
        val result = statement.executeQuery()
        result.next()
        read(result)
    })

    def delete(id: Int) = wrap(connection =>{
        val st = connection.prepareStatement(deleteSql)
        st.setInt(1, id)
        st.execute()
    })

    def enable(id: Int) = wrap(connection =>{
        val st = connection.prepareStatement(enableItemSql)
        st.setInt(1, id)
        st.execute()
    })

    def loadAll(): Iterator[T] = {
        //get missing columns and "alter table" them
        val columnNames = getColumnNames()
        val missingColumns = properties.keySet.filter(x=> !columnNames.contains(x.toLowerCase)).toList

        if (missingColumns.size > 0) addColumns(missingColumns)
        loadWhere("1", "1")
    }

    def loadWhere(key: String, value: Object): Iterator[T] = wrap(connection =>{

        val statement = connection.prepareStatement(s"select * from `$tableName` where $key=$value")
        //val statement = connection.createStatement()
        val result = statement.executeQuery()
        //val result = statement.executeQuery(s"select * from `$tableName` where $key=$value")

        val iter = new DataSetIterator[T](connection, result, read)
        val r = new ArrayBuffer[T]()

        while (iter.hasNext){
            r.append(iter.next())
        }
        //iter.free()

        //if(!statement.isClosed) {
        //    System.out.println("Close statement!")
            //statement.close()
        //} else {
        //    System.out.println("Exception statement closed!")
        //}


//        this.synchronized{
//            val store = new JsonDataStore[T]()
//            store.saveAll(r.toList)
//        }

        r.iterator

    })

    def getColumnNames(): ArrayBuffer[String] = wrap(connection => {
        val rsSet = new ArrayBuffer[String]()
        val statement = connection.prepareStatement("select column_name from information_schema.columns where table_name = \"" + tableName + "\"")
        val result = statement.executeQuery()
        while (result.next()) {
            rsSet += (result.getString(1).toLowerCase())
        }
        rsSet
    })
}

