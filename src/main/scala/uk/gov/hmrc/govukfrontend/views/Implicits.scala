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
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.{CheckboxItem, Checkboxes}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{Content, HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.errormessage.ErrorMessage
import uk.gov.hmrc.govukfrontend.views.viewmodels.errorsummary.ErrorLink
import uk.gov.hmrc.govukfrontend.views.viewmodels.input.Input
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.{RadioItem, Radios}
import uk.gov.hmrc.govukfrontend.views.viewmodels.select.{Select, SelectItem}
import uk.gov.hmrc.govukfrontend.views.viewmodels.textarea.Textarea

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


  implicit class RichRadios(radios: Radios)(implicit val messages: Messages) extends ImplicitsSupport[Radios] {

    /**
      * Extension method to allow a Play form Field to be used to add certain parameters in a Radios,
      * specifically errorMessage, idPrefix, name, and checked (for a specific RadioItem). Note these
      * values will only be added from the Field if they are not specifically defined in the Radios object.
      *
      * @param field
      * @param messages
      */
    override def withFormField(field: Field): Radios =
      radios
        .withErrorMessage(field)
        .withId(field)
        .withName(field)
        .withItemsChecked(field)

    override protected val currentT: Radios = radios

    override protected def allowOverrideName: Boolean = radios.name == Radios.defaultObject.name

    override protected def allowOverrideId: Boolean = radios.idPrefix == Radios.defaultObject.idPrefix

    override protected def allowOverrideError: Boolean = radios.errorMessage == Radios.defaultObject.errorMessage

    override protected def updateName: String => Radios = updatedName => radios.copy(name = updatedName)

    override protected def updateId: String => Radios =
      updatedId => radios.copy(idPrefix = Some(updatedId))

    override protected def updateErrorMessage: ErrorMessage => Radios =
      updatedErrorMessage => radios.copy(errorMessage = Some(updatedErrorMessage))

    private[views] def withItemsChecked(field: Field): Radios =
      radios.copy(
        items = radios.items.map { radioItem =>
          if (radioItem.checked == RadioItem.defaultObject.checked) {
            val isChecked = radioItem.value == field.value
            radioItem.copy(checked = isChecked)
          }
          else radioItem
        }
      )

    override protected def allowOverrideValue: Boolean = false

    override protected def updateValue: String => Radios =
      _ => currentT
  }

  implicit class RichInput(input: Input)(implicit val messages: Messages) extends ImplicitsSupport[Input] {

    /**
      * Extension method to allow a Play form Field to be used to add certain parameters in an Input,
      * specifically errorMessage, idPrefix, name, and value. Note these
      * values will only be added from the Field if they are not specifically defined in the Input object.
      *
      * @param field
      * @param messages
      */
    override def withFormField(field: Field): Input =
      input
        .withName(field)
        .withId(field)
        .withValue(field)
        .withErrorMessage(field)

    override protected val currentT: Input = input

    override protected def allowOverrideName: Boolean = input.name == Input.defaultObject.name

    override protected def allowOverrideId: Boolean = input.id == Input.defaultObject.id

    override protected def allowOverrideError: Boolean = input.errorMessage == Input.defaultObject.errorMessage

    override protected def updateName: String => Input = updatedName => input.copy(name = updatedName)

    override protected def updateId: String => Input =
      updatedId => input.copy(id = updatedId)

    override protected def updateErrorMessage: ErrorMessage => Input =
      updatedErrorMessage => input.copy(errorMessage = Some(updatedErrorMessage))

    override protected def allowOverrideValue: Boolean =
      input.value == Input.defaultObject.value

    override protected def updateValue: String => Input =
      updatedValue => input.copy(value = Some(updatedValue))
  }

  implicit class RichCheckboxes(checkboxes: Checkboxes)(implicit val messages: Messages) extends ImplicitsSupport[Checkboxes] {

    /**
      * Extension method to allow a Play form Field to be used to add certain parameters in a Checkboxes,
      * specifically errorMessage, idPrefix, name, and checked (for a specific CheckboxItem). Note these
      * values will only be added from the Field if they are not specifically defined in the Checkboxes object.
      *
      * @param field
      * @param messages
      */
    override def withFormField(field: Field): Checkboxes =
      checkboxes
        .withName(field)
        .withId(field)
        .withErrorMessage(field)
        .withItemsChecked(field)

    override protected val currentT: Checkboxes = checkboxes

    override protected def allowOverrideName: Boolean = checkboxes.name == Checkboxes.defaultObject.name

    override protected def allowOverrideId: Boolean = checkboxes.idPrefix == Checkboxes.defaultObject.idPrefix

    override protected def allowOverrideError: Boolean = checkboxes.errorMessage == Checkboxes.defaultObject.errorMessage

    override protected def allowOverrideValue: Boolean = false

    override protected def updateName: String => Checkboxes =
      updatedName => checkboxes.copy(name = updatedName)

    override protected def updateId: String => Checkboxes =
      updatedId => checkboxes.copy(idPrefix = Some(updatedId))

    override protected def updateErrorMessage: ErrorMessage => Checkboxes =
      updatedErrorMessage => checkboxes.copy(errorMessage = Some(updatedErrorMessage))

    override protected def updateValue: String => Checkboxes =
      _ => currentT

    private[views] def withItemsChecked(field: Field): Checkboxes =
      checkboxes.copy(
        items = checkboxes.items.map { checkboxItem =>
          if (checkboxItem.checked == CheckboxItem.defaultObject.checked) {
            val isChecked = field.value.contains(checkboxItem.value)
            checkboxItem.copy(checked = isChecked)
          }
          else checkboxItem
        }
      )
  }
}

object Implicits extends Implicits

trait ImplicitsSupport[T] {
  implicit val messages: Messages

  def withFormField(field: Field): T

  protected val currentT: T

  protected def allowOverrideName: Boolean

  protected def allowOverrideId: Boolean

  protected def allowOverrideError: Boolean

  protected def allowOverrideValue: Boolean

  protected def updateName: String => T

  protected def updateId: String => T

  protected def updateErrorMessage: ErrorMessage => T

  protected def updateValue: String => T

  private[views] def withName(field: Field): T =
    if (allowOverrideName) updateName(field.name) else currentT

  private[views] def withId(field: Field): T =
    if (allowOverrideId) updateId(field.name) else currentT

  private[views] def withValue(field: Field): T =
    if (allowOverrideValue) field.value.map(updateValue).getOrElse(currentT)
    else currentT

  private[views] def withErrorMessage(field: Field): T = {
    if (allowOverrideError) {
      field.error
        .map(formError =>
          ErrorMessage(content = Text(messages(formError.message, formError.args: _*)))
        )
        .map(updateErrorMessage(_)).getOrElse(currentT)
    }
    else currentT
  }
}
