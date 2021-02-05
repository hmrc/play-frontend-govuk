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

package uk.gov.hmrc.govukfrontend.views.viewmodels.radios

import play.api.data.Field
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.html.components.{ErrorMessage, Fieldset, Hint, Legend, Text}

object SimpleRadios {

  def apply(field: Field,
            legend: String,
            radioItems: Seq[(String, String)],
            hint: Option[String] = None,
            formGroupClasses: Option[String] = None,
            classes: Option[String] = None,
            attributes: Map[String, String] = Map.empty)
           (implicit messages: Messages): Radios = {

    new Radios(
      fieldset = Some(Fieldset(
        legend = Some(Legend(
          content = Text(legend)
        ))
      )),
      hint = hint.map(hintText => Hint(content = Text(hintText))),
      errorMessage = field.error.map(formError =>
        ErrorMessage(content = Text(messages(formError.message, formError.args: _*)))
      ),
      formGroupClasses = formGroupClasses.getOrElse(""),
      idPrefix = Some(field.name),
      name = field.name,
      items = radioItems map { radioItem =>
        val (label, value) = radioItem
        RadioItem(
          content = Text(label),
          checked = field.value.contains(value),
          value = Some(value)
        )
      },
      classes = classes.getOrElse(""),
      attributes = attributes
    )
  }
}
