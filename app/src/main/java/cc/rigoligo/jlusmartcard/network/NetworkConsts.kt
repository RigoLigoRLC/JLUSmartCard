package cc.rigoligo.jlusmartcard.network

object NetworkConsts {
    val url = "http://202.198.17.52"
    fun urlPath(port: Int = 8090, path: String) = "$url:$port$path"
}