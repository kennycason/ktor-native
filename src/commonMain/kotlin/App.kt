import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.posix.*

data class Env(
    val sourceFolder: String,
    val http: Http = Http(),
    val aws: Aws = Aws()
) {
    data class Http(
        val port: Int = 8080
    )

    data class Aws(
        val region: String = "us-west-2",
        val s3Bucket: String = "cdn.arrivedhomes.com",
        val accessKey: String = "",
        val secretKey: String = ""
    )
}

val BMP: ContentType = ContentType("image", "bmp")
val WEBP: ContentType = ContentType("image", "webp")

@Serializable
data class Pokemon(val id: Int, val name: String, val type: String)

fun main() {
    val env = Env(sourceFolder = File.pwd() + "/images/")
    embeddedServer(CIO, port = env.http.port) {
        apiRoutes(env)
    }.start(wait = true)
}

fun Application.apiRoutes(env: Env) {
    routing {
        // return static file by path from images/
        get("/{path...}") {
            val filePath = env.sourceFolder + call.parameters.getAll("path")?.joinToString("/")
            val fileDataAndType = readFile(filePath)
            if (fileDataAndType != null) {
                call.response.headers.append("Content-Type", fileDataAndType.contentType.toString())
                call.respond(fileDataAndType.data, null)
            } else {
                call.respond(HttpStatusCode.NotFound, null)
            }
        }
        // manual json serialization example
        get("/pokemon/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, null)
                return@get
            }
            val pokemon = Pokemon(id, "Bulbasaur", "Grass/Poison")
            val jsonResponse = Json.encodeToString(pokemon)
            call.respond(jsonResponse, null)
        }
    }
}

// Use POSIX fopen to open the file
@OptIn(ExperimentalForeignApi::class)
fun readFile(filePath: String): FileDataAndType? {
    println("reading file from: $filePath")
    val file = fopen(filePath, "rb") ?: return null
    return try {
        // Seek to get file size
        fseek(file, 0, SEEK_END)
        val size = ftell(file).toInt()
        rewind(file)

        // Read file contents
        val buffer = ByteArray(size)
        fread(buffer.refTo(0), 1u, size.toULong(), file)

        // get content type
        val extension = filePath.substringAfterLast('.', "")
        val contentType = getImageContentType(extension)
        FileDataAndType(buffer, contentType)
    } finally {
        fclose(file)
    }
}

data class FileDataAndType(
    val data: ByteArray,
    val contentType: ContentType
)

fun getImageContentType(extension: String): ContentType {
    return when (extension.lowercase()) {
        "bmp"  -> BMP
        "gif"  -> ContentType.Image.GIF
        "jpeg", "jpg" -> ContentType.Image.JPEG
        "png"  -> ContentType.Image.PNG
        "svg"  -> ContentType.Image.SVG
        "webp" -> WEBP
        "ico"  -> ContentType("image", "x-icon") // Explicit MIME type for ICO
        else   -> ContentType.Application.OctetStream // Fallback for unknown types
    }
}
