import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import kotlinx.cinterop.toKString
import platform.posix.getcwd

object File {
    @OptIn(ExperimentalForeignApi::class)
    fun pwd(): String {
        val buffer = ByteArray(1024)
        return getcwd(buffer.refTo(0), buffer.size.toULong())?.toKString() ?: "Unknown"
    }
}
