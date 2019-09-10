/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.govukfrontend.views

import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.BuildInfo
import better.files._

class CrossCompiledTemplatesSpec extends WordSpec with Matchers {

  val playVersion: String = BuildInfo.playVersion match {
    case "Play25" => "play-25"
    case "Play26" => "play-26"
    case _        => throw new IllegalArgumentException("Expecting Play25 or Play26")
  }

  val otherPlayVersion: String =
    BuildInfo.playVersion match {
      case "Play25" => "play-26"
      case "Play26" => "play-25"
      case _        => throw new IllegalArgumentException("Expecting Play25 or Play26")
    }

  BuildInfo.twirlCompileTemplates_sources.foreach { templateFile =>
    val otherTemplateFile =
      File(templateFile.getAbsolutePath.replaceFirst(playVersion, otherPlayVersion))

    s"Cross-compiled template $templateFile" should {
      s"be in sync with template $otherTemplateFile" in {
        if (otherTemplateFile.exists) {
          loadTemplate(File(templateFile.getAbsolutePath)) shouldBe loadTemplate(otherTemplateFile)
        } else {
          fail(s"Template $otherTemplateFile not implemented yet")
        }
      }
    }

  }

  def loadTemplate(templateFile: File): String = {
    val contents          = templateFile.chars.mkString
    val templateBeginning = "@\\(".r
    val matchBeginning    = templateBeginning.findFirstMatchIn(contents)
    val start             = matchBeginning.map(_.start).getOrElse(0)
    contents.substring(start)
  }

}