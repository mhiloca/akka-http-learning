package part2_lowlevelserver

import java.io.InputStream
import java.security.{KeyStore, SecureRandom}

import akka.actor.ActorSystem
import akka.http.scaladsl.{ConnectionContext, Http, HttpsConnectionContext}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse, StatusCodes}
import akka.stream.ActorMaterializer
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}

object HttpsContext {
  // step 1: initialize a keystore object: qe can obtain a file for that and a password
  val ks: KeyStore = KeyStore.getInstance("PKCS12")
  val keystoreFile: InputStream = getClass.getClassLoader.getResourceAsStream("keystore.pkcs12")
  // new FileInputStream(new File("src/main/resources/keystore.pkcs12")
  val password = "akka-https".toCharArray // fetch the password from a secure place!
  ks.load(keystoreFile, password)

  /*
   this keystore object will contain all the https certificates
   that it decoded from this file
   */

  // step 2: initialize a key manager
  val keyManagerFactory = KeyManagerFactory.getInstance("SunX509") // PKI: public key infrastructure
  keyManagerFactory.init(ks, password)

  // step 3: initialize a trust manager
  val trustManagerFactory = TrustManagerFactory.getInstance("SunX509")
  trustManagerFactory.init(ks)

  // Step 4: initialize an SSL context
  val sslContext = SSLContext.getInstance("TLS")
  sslContext.init(keyManagerFactory.getKeyManagers, trustManagerFactory.getTrustManagers, new SecureRandom)

  // step 5: return the https connection context
  val httpsConnectionContext: HttpsConnectionContext = ConnectionContext.https(sslContext)
}

object LowLevelHttps extends App  {
  import HttpsContext._

  implicit val system: ActorSystem = ActorSystem("LowLevelHttps")
  implicit val materializer: ActorMaterializer = ActorMaterializer()



  val requestHandler: HttpRequest => HttpResponse = {
    case HttpRequest(HttpMethods.GET, _, _, _, _) =>
      HttpResponse(
        StatusCodes.OK, // HTTP 200
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            | <body>
            |   <h1>Hello from Akka HTTPS!<h1>
            | </body>
            |</html>
            |""".stripMargin
        )
      )
    case request: HttpRequest =>
      request.discardEntityBytes()
      HttpResponse(
        StatusCodes.NotFound, // 404
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            | <body>
            |   <h2>OOPS! The resource can't be found.</h2>
            | </body>
            |</html>
            |""".stripMargin
        )
      )
  }

  Http().bindAndHandleSync(requestHandler, "localhost", 8443, httpsConnectionContext)

}
