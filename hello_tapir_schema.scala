// works for scala 2.12 upwards (scala 3 might give "max inlining"-troubles for derivation)
//> using scala "2.12"

//> using lib "com.softwaremill.sttp.tapir::tapir-core:1.2.9"
//> using lib "com.softwaremill.sttp.tapir::tapir-json-circe:1.2.9"
//> using lib "com.softwaremill.sttp.tapir::tapir-openapi-docs:1.2.9"
//> using lib "com.softwaremill.sttp.apispec::openapi-circe-yaml:0.3.2"

import io.circe.generic.auto._
import sttp.apispec.openapi.OpenAPI
import sttp.tapir._
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._

/**
 * Example ADT type - used as an example return value of a route.
 */
sealed trait MsgProtocol

object MsgProtocol {

  case class Success(item: LibraryItem) extends MsgProtocol

  case class Error(msg: String, code: Int) extends MsgProtocol

  case class Author(name: String)

  // sealed trait with two structurally identical types
  sealed trait LibraryItem

  case class Book(title: String, year: Int, author: Author, price: Double) extends LibraryItem

  case class Magazine(title: String, year: Int, author: Author, price: Double) extends LibraryItem

  val codec: Codec.JsonCodec[MsgProtocol] = circeCodec
}


object Endpoints {

  private val emptyIO: EndpointIO.Empty[Unit] =
    EndpointIO.Empty(Codec.idPlain(), EndpointIO.Info.empty)

  private val endpoint: PublicEndpoint[Unit, Unit, Unit, Any] =
    infallibleEndpoint.copy(errorOutput = emptyIO)

  // endpoint-definition for an endpoint that returns MsgProtocol as json
  val exampleEndpoint: PublicEndpoint[Unit, Unit, MsgProtocol, Any] = endpoint.get
    .in("my" / "example" / "endpoint")
    .out(jsonBody[MsgProtocol])
}

object APIDocs {
  val docs: OpenAPI =
    OpenAPIDocsInterpreter().toOpenAPI(Endpoints.exampleEndpoint, "My Example", "1.0")

  def writeToFile(docs: OpenAPI = APIDocs.docs, path: String = "./api/openapi.yaml"): Unit = {
    import sttp.apispec.openapi.circe.yaml._

    import java.nio.charset.StandardCharsets
    import java.nio.file.Files
    import java.nio.file.Paths
    Files.write(Paths.get(path), docs.toYaml.getBytes(StandardCharsets.UTF_8))
  }
}
