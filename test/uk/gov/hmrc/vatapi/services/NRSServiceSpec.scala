/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.vatapi.services
import org.joda.time.DateTime
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vatapi.UnitSpec
import uk.gov.hmrc.vatapi.assets.TestConstants.NRSResponse._
import uk.gov.hmrc.vatapi.assets.TestConstants.VatReturn._
import uk.gov.hmrc.vatapi.connectors.NRSConnector
import uk.gov.hmrc.vatapi.httpparsers.NrsError
import uk.gov.hmrc.vatapi.httpparsers.NrsSubmissionHttpParser.NrsSubmissionOutcome
import uk.gov.hmrc.vatapi.mocks.connectors.MockNRSConnector
import uk.gov.hmrc.vatapi.models.VatReturnDeclaration

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class NRSServiceSpec extends UnitSpec with OneAppPerSuite with MockitoSugar with ScalaFutures with MockNRSConnector {

  object TestNRSService extends NRSService {
    override val nrsConnector: NRSConnector = mockNRSConnector
  }

  implicit val hc: HeaderCarrier = HeaderCarrier()
  val testDateTime: DateTime = DateTime.now()


  "NRSService.submit" when {

    lazy val testVrn: Vrn = Vrn("123456789")
    def result(submission: VatReturnDeclaration): Future[NrsSubmissionOutcome] = TestNRSService.submit(testVrn, submission)

    "successful responses are returned from the connector" should {
      "return the correctly formatted NRS Data model" in {
        setupNrsSubmission(testVrn, nrsSubmission)(Right(nrsData))
        extractAwait(result(vatReturnDeclaration)) shouldBe Right(nrsData)
      }
    }

    "error responses are returned from the connector" should {
      "return an NRS Error model" in {
        setupNrsSubmission(testVrn, nrsSubmission)(Left(NrsError))
        extractAwait(result(vatReturnDeclaration)) shouldBe Left(NrsError)
      }
    }
  }
}