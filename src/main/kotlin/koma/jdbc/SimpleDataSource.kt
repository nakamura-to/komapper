package koma.jdbc

import java.io.PrintWriter
import java.sql.Connection
import java.sql.DriverManager
import java.util.logging.Logger
import javax.sql.DataSource


class SimpleDataSource(val url: String, val user: String = "", val password: String = "") : DataSource {


    override fun setLogWriter(out: PrintWriter?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setLoginTimeout(seconds: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isWrapperFor(iface: Class<*>?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> unwrap(iface: Class<T>?): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getConnection(): Connection {
        return DriverManager.getConnection(url, user, password)
    }

    override fun getConnection(username: String?, password: String?): Connection {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParentLogger(): Logger {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLogWriter(): PrintWriter {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLoginTimeout(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}
