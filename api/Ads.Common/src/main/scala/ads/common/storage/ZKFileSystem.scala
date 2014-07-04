package ads.common.storage


class Extent (var path: String, var size: Int, var id: Int)

trait IMasterStorage{

    def createExtent(path: String) : Extent
}

class MasterStorage extends IMasterStorage{
    def createExtent(path: String): Extent = null
}