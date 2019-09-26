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

import play.api.data.FormError
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.viewmodels.common.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.errormessage.ErrorMessageParams
import uk.gov.hmrc.govukfrontend.views.viewmodels.errorsummary.ErrorLink

trait Implicits {

  implicit class RichHtml(html: Html) {
    def padLeft(padCount: Int = 1, padding: String = " "): Html = {
      val padStr = " " * (if (html.body.isEmpty) 0 else padCount)
      HtmlFormat.fill(collection.immutable.Seq(Html(padStr), html))
    }

    def trim: Html =
      Html(html.toString.trim)

    def ltrim: Html =
      Html(html.toString.ltrim)

    def rtrim: Html =
      Html(html.toString.rtrim)
  }

  implicit class RichString(s: String) {
    def toOption: Option[String] =
      if (s == null || s.isEmpty) None else Some(s)

    def ltrim = s.replaceAll("^\\s+", "")

    def rtrim = s.replaceAll("\\s+$", "")
  }

  implicit class RichFormErrors(formErrors: Seq[FormError])(implicit messages: Messages) {

    def asErrorLinks: Seq[ErrorLink] =
      formErrors.map { error =>
        ErrorLink(href = Some(s"#${error.key}"), content = Text(errorMessage(error)))
      }

    def asErrorLinks(isContentHtml: Boolean): Seq[ErrorLink] =
      if (isContentHtml)
        formErrors.map { error =>
          ErrorLink(href = Some(s"#${error.key}"), content = HtmlContent(errorMessage(error)))
        } else asErrorLinks

    def asErrorMessages: Seq[ErrorMessageParams] =
      formErrors.map(error => ErrorMessageParams(content = Text(errorMessage(error))))

    def asErrorMessages(isContentHtml: Boolean): Seq[ErrorMessageParams] =
      if (isContentHtml)
        formErrors.map(error => ErrorMessageParams(content = HtmlContent(errorMessage(error))))
      else asErrorMessages

    def asErrorMessage(messageSelector: String, isContentHtml: Boolean = false): Option[ErrorMessageParams] =
      formErrors
        .find(_.message == messageSelector)
        .map(
          error =>
            if (isContentHtml)
              ErrorMessageParams(content = HtmlContent(errorMessage(error)))
            else
              ErrorMessageParams(content = Text(errorMessage(error))))

    private def errorMessage(error: FormError) = messages(error.message, error.args: _*)
  }
}
