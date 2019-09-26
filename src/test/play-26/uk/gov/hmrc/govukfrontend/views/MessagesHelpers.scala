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

import play.api.i18n.Messages
import play.api.test.Helpers

trait MessagesHelpers {
  implicit val messages: Messages = Helpers.stubMessages(Helpers.stubMessagesApi(
    messages = Map(
      "default" -> Map("error.invalid" -> "Invalid input received",
      "error.seeExplanation" -> "<b>This is utterly unacceptable<b>",
      "error.missing" -> "Input missing"))))
}
