/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.data.{Field, FormError}
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.viewmodels.charactercount.CharacterCount
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{Content, HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.errormessage.ErrorMessage
import uk.gov.hmrc.govukfrontend.views.viewmodels.errorsummary.ErrorLink
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.{RadioItem, Radios}

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

    /**
      * Based on the behaviour of [[https://mozilla.github.io/nunjucks/templating.html#indent]]
      *
      * @param n
      * @param indentFirstLine
      */
    def indent(n: Int, indentFirstLine: Boolean = false): Html =
      Html(html.toString.indent(n, indentFirstLine))

    def nonEmpty: Boolean =
      html.body.nonEmpty
  }

  implicit class RichString(s: String) {

    def toOption: Option[String] =
      if (s == null || s.isEmpty) None else Some(s)

    def ltrim = s.replaceAll("^\\s+", "")

    def rtrim = s.replaceAll("\\s+$", "")

    /**
      * Indent a (multiline) string <code>n</code> spaces.
      *
      * @param n Number of spaces to indent the string. It can be a negative value, in which case it attempts to unindent
      *          n spaces, as much as possible until it hits the margin.
      * @param indentFirstLine
      * @return
      */
    def indent(n: Int, indentFirstLine: Boolean = false): String = {
      @scala.annotation.tailrec
      def recurse(m: Int, lines: Seq[String]): Seq[String] = m match {
        case 0 => lines
        case m => recurse(math.abs(m) - 1, indent1(math.signum(n), lines))
      }

      val lines = s.split("\n", -1).toSeq // limit=-1 so if a line ends with \n include the trailing blank line
      val linesToIndent = if (indentFirstLine) lines else if (lines.length > 1) lines.tail else Nil
      val indentedLines = recurse(n, linesToIndent)

      (if (indentFirstLine) indentedLines else (lines.head +: indentedLines)).mkString("\n")
    }

    private def indent1(signal: Int, lines: Seq[String]) = {
      def canUnindent: Boolean =
        lines.forall(_.startsWith(" "))

      if (signal < 0 && canUnindent) {
        lines.map(_.drop(1))
      } else if (signal < 0) {
        lines
      } else {
        lines.map(" " + _)
      }
    }

    /**
      * Implements [[https://mozilla.github.io/nunjucks/templating.html#capitalize]].
      * Unlike Scala's [[scala.collection.immutable.StringLike#capitalize()]] Nunjucks' capitalize converts the first
      * letter to uppercase and the rest to lowercase.
      *
      * @return
      */
    def nunjucksCapitalize: String =
      s.toLowerCase.capitalize

  }

  /**
    * Mapping, folding and getOrElse on Option[String] for non-empty strings.
    * Commonly used in the Twirl components.
    *
    * @param optString
    */
  implicit class RichOptionString(optString: Option[String]) {
    def mapNonEmpty[T](f: String => T): Option[T] =
      optString.filter(_.nonEmpty).map(f)

    def foldNonEmpty[B](ifEmpty: => B)(f: String => B): B =
      optString.filter(_.nonEmpty).fold(ifEmpty)(f)

    def getNonEmptyOrElse[B >: String](default: => B): B =
      optString.filter(_.nonEmpty).getOrElse(default)
  }

  /**
    * Extension methods to convert from [[FormError]] to other types used in components to display errors
    *
    * @param formErrors
    * @param messages
    */
  implicit class RichFormErrors(formErrors: Seq[FormError])(implicit messages: Messages) {

    def asHtmlErrorLinks: Seq[ErrorLink] =
      asErrorLinks(HtmlContent.apply)

    def asTextErrorLinks: Seq[ErrorLink] =
      asErrorLinks(Text.apply)

    private[views] def asErrorLinks(contentConstructor: String => Content): Seq[ErrorLink] =
      formErrors.map { formError =>
        ErrorLink(href = Some(s"#${formError.key}"), content = contentConstructor(errorMessage(formError)))
      }

    def asHtmlErrorMessages: Seq[ErrorMessage] =
      asErrorMessages(HtmlContent.apply)

    def asTextErrorMessages: Seq[ErrorMessage] =
      asErrorMessages(Text.apply)

    private[views] def asErrorMessages(contentConstructor: String => Content): Seq[ErrorMessage] =
      formErrors
        .map { formError =>
          ErrorMessage(content = contentConstructor(errorMessage(formError)))
        }

    def asHtmlErrorMessage(messageSelector: String): Option[ErrorMessage] =
      asErrorMessage(HtmlContent.apply, messageSelector)

    def asTextErrorMessage(messageSelector: String): Option[ErrorMessage] =
      asErrorMessage(Text.apply, messageSelector)

    private[views] def asErrorMessage(
                                       contentConstructor: String => Content,
                                       messageSelector: String): Option[ErrorMessage] =
      formErrors
        .find(_.message == messageSelector)
        .map { formError =>
          ErrorMessage(content = contentConstructor(errorMessage(formError)))
        }

    def asHtmlErrorMessageForField(fieldKey: String): Option[ErrorMessage] =
      asErrorMessageForField(HtmlContent.apply, fieldKey)

    def asTextErrorMessageForField(fieldKey: String): Option[ErrorMessage] =
      asErrorMessageForField(Text.apply, fieldKey)

    private[views] def asErrorMessageForField(
                                               contentConstructor: String => Content,
                                               fieldKey: String): Option[ErrorMessage] =
      formErrors
        .find(_.key == fieldKey)
        .map { formError =>
          ErrorMessage(content = contentConstructor(errorMessage(formError)))
        }

    private def errorMessage(formError: FormError) = messages(formError.message, formError.args: _*)
  }


  implicit class RichRadios(radios: Radios)(implicit messages: Messages) {

    /**
      * Extension method to allow a Play form Field to be used to add certain parameters in a Radios,
      * specifically errorMessage, idPrefix, name, and checked (for a specific RadioItem). Note these
      * values will only be added from the Field if they are not specifically defined in the Radios object.
      *
      * @param field
      * @param messages
      */
    def withFormField(field: Field): Radios = {
      radios
        .withErrorMessage(field)
        .withIdPrefix(field)
        .withName(field)
        .withItemsChecked(field)
    }

    private[views] def withErrorMessage(field: Field): Radios =
      if (radios.errorMessage == Radios.defaultObject.errorMessage) {
        radios.copy(
          errorMessage = field.error.map(formError =>
            ErrorMessage(content = Text(messages(formError.message, formError.args: _*)))
          )
        )
      } else radios

    private[views] def withIdPrefix(field: Field): Radios =
      if (radios.idPrefix == Radios.defaultObject.idPrefix) radios.copy(idPrefix = Some(field.name))
      else radios

    private[views] def withName(field: Field): Radios =
      if (radios.name == Radios.defaultObject.name) radios.copy(name = field.name)
      else radios

    private[views] def withItemsChecked(field: Field): Radios =
      radios.copy(
        items = radios.items.map(radioItem => withItemChecked(radioItem, field))
      )

    private def withItemChecked(radioItem: RadioItem, field: Field): RadioItem = {
      if (radioItem.checked == RadioItem.defaultObject.checked) {
        val isChecked = radioItem.value == field.value
        radioItem.copy(checked = isChecked)
      }
      else radioItem
    }
  }

  implicit class RichCharacterCount(characterCount: CharacterCount)(implicit messages: Messages) {

    def withFormField(field: Field): CharacterCount = {
      characterCount
        .withName(field)
        .withId(field)
        .withErrorMessage(field)
        .withValue(field)
    }

    private[views] def withName(field: Field): CharacterCount =
      if (characterCount.name == CharacterCount.defaultObject.name) characterCount.copy(name = field.name)
      else characterCount

    private[views] def withId(field: Field): CharacterCount =
      if (characterCount.id == CharacterCount.defaultObject.id) characterCount.copy(id = field.name)
      else characterCount

    private[views] def withErrorMessage(field: Field): CharacterCount =
      if (characterCount.errorMessage == CharacterCount.defaultObject.errorMessage) {
        characterCount.copy(
          errorMessage = field.error.map(formError =>
            ErrorMessage(content = Text(messages(formError.message, formError.args: _*)))
          )
        )
      } else characterCount

    private[views] def withValue(field: Field): CharacterCount =
      if (characterCount.value == CharacterCount.defaultObject.value) characterCount.copy(value = field.value)
      else characterCount
  }

}

object Implicits extends Implicits
