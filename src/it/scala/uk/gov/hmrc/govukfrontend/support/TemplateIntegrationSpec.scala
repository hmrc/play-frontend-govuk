package uk.gov.hmrc.govukfrontend.support

import org.jsoup.Jsoup
import org.scalacheck.Prop.{forAll, secure}
import org.scalacheck.Test.TestCallback
import org.scalacheck.{Arbitrary, Properties, ShrinkLowPriority, Test}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import play.api.libs.json.{Json, OWrites}
import uk.gov.hmrc.govukfrontend.support.Implicits._
import uk.gov.hmrc.govukfrontend.support.ScalaCheckUtils.{ClassifyParams, classify}
import uk.gov.hmrc.govukfrontend.views.{JsoupHelpers, TemplateValidationException, TwirlRenderer}
import scala.util.{Failure, Success}
import uk.gov.hmrc.govukfrontend.views.TemplateDiff._

/**
  * Base class for integration testing a Twirl template against the Nunjucks template rendering service
  *
  * @tparam T Type representing the input parameters of the Twirl template
  */
abstract class TemplateIntegrationSpec[T: OWrites: Arbitrary](govukComponentName: String, seed: Option[String] = None)
    extends Properties(govukComponentName)
    with TemplateServiceClient
    with TwirlRenderer[T]
    with ShrinkLowPriority
    with JsoupHelpers
    with ScalaFutures
    with IntegrationPatience {

  /**
    * [[Stream]] of [[org.scalacheck.Prop.classify]] conditions to collect statistics on a property
    * Used to check the distribution of generated data
    *
    * @param templateParams
    * @return [[Stream[ClassifyParams]] of arguments to [[org.scalacheck.Prop.classify]]
    */
  def classifiers(templateParams: T): Stream[ClassifyParams] =
    Stream.empty[ClassifyParams]

  // Due to rounding in [[org.scalacheck.util.Pretty.prettyFreqMap]] the console reporter shows some
  // frequencies collected from the classifiers as 0% when they are not zero.
  // This reporter looks for null counts instead of ratios in the classifiers which can signal problems in the generator
  // and/or an insufficient number of test runs
  override def overrideParameters(p: Test.Parameters): Test.Parameters =
    p.withTestCallback(p.testCallback chain new TestCallback {
        override def onTestResult(name: String, result: Test.Result): Unit = {
          println(s"Generator reporter $name")
          val emptyStats = result.freqMap.getCounts.filter { case (_, c) => c == 0 }
          if (emptyStats.nonEmpty) {
            pprint.pprintln(
              "some stats are null, tweak the generator to provide better coverage and/or increase the minSuccessfulTests parameter")
            pprint.pprintln(emptyStats, width = 80, height = Int.MaxValue)
          } else ()
        }

      })
      .withMinSuccessfulTests(50)

  /**
    * Property that renders the Twirl template and uses the template rendering service to render the equivalent
    * Nunjucks template, and checks the outputs are equal. If provided, it uses the [[classifiers]] to collect statistics
    * about the template parameters to analyse the distribution of the generators.
    */
  propertyWithSeed(s"$govukComponentName should render the same markup as the nunjucks renderer", seed) = forAll {
    templateParams: T =>
      classify(classifiers(templateParams))(secure {

        val response = render(govukComponentName, templateParams)

        val nunJucksOutputHtml = response.futureValue.bodyAsString

        val tryRenderTwirl =
          render(templateParams)
            .transform(html => Success(html.body), f => Failure(new TemplateValidationException(f.getMessage)))

        tryRenderTwirl match {

          case Success(twirlOutputHtml) =>
            val compressedTwirlHtml    = parseAndCompressHtml(twirlOutputHtml)
            val compressedNunjucksHtml = parseAndCompressHtml(nunJucksOutputHtml)
            val prop                   = compressedTwirlHtml == compressedNunjucksHtml

            if (!prop) {
              reportDiff(
                compressedTwirlHtml    = compressedTwirlHtml,
                compressedNunjucksHtml = compressedNunjucksHtml,
                templateParams         = templateParams,
                templateParamsJson     = Json.prettyPrint(Json.toJson(templateParams))
              )
            }

            prop
          case Failure(TemplateValidationException(message)) =>
            println(s"Failed to validate the parameters for the template for $govukComponentName")
            println(s"Exception: $message")
            println("Skipping property evaluation")

            true
        }
      })
  }

  def reportDiff(
    compressedTwirlHtml: String,
    compressedNunjucksHtml: String,
    templateParams: T,
    templateParamsJson: String): Unit = {

    val diffPath =
      templateDiffPath(
        twirlOutputHtml    = compressedTwirlHtml,
        nunJucksOutputHtml = compressedNunjucksHtml,
        diffFilePrefix     = Some(govukComponentName)
      )

    println(s"Diff between Twirl and Nunjucks outputs (please open diff HTML file in a browser): file://$diffPath\n")

    println("-" * 80)
    println("Twirl")
    println("-" * 80)

    val formattedTwirlHtml = Jsoup.parseBodyFragment(compressedTwirlHtml).body.html
    println(s"\nTwirl output:\n$formattedTwirlHtml\n")

    println(s"\nparameters: ")
    pprint.pprintln(templateParams, width = 80, height = 500)

    println("-" * 80)
    println("Nunjucks")
    println("-" * 80)

    val formattedNunjucksHtml = Jsoup.parseBodyFragment(compressedNunjucksHtml).body.html
    println(s"\nNunjucks output:\n$formattedNunjucksHtml\n")

    println(s"\nparameters: ")
    println(templateParamsJson)
  }
}
