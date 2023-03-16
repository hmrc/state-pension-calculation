/*
 * Copyright 2023 HM Revenue & Customs
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

package support.data

import models.{CalculationNote, CalculationResponse, CalculationResult, QualifyingYear}
import play.api.libs.json.{JsValue, Json}

object CalculationTestData {

  object Result {
    val json: JsValue = Json.parse(
      """
        |{
        |  "nino": "AA123456A",
        |  "protectedPaymentAmount": 123.45,
        |  "rebateDerivedAmount": 123.45,
        |  "newStatePensionEntitlementAmount": 123.45,
        |  "reducedRateElectionToConsider": false,
        |  "pensionShareOrderContractedOutEmploymentGroup": true,
        |  "pensionShareOrderStateEarningsRelatedPensionService": true,
        |  "isleOfManContributions": false,
        |  "contractedOutEmploymentGroupInvestigationPosition": true,
        |  "statePensionOldRulesAmount": 123.45,
        |  "basicAmount": 123.45,
        |  "additionalPensionPre1997GrossAmount": 123.45,
        |  "additionalPensionPre1997NetAmount": 123.45,
        |  "additionalPensionPost1997Amount": 123.45,
        |  "additionalPensionPost2002Amount": 123.45,
        |  "graduatedRetirementBenefitAmount": 123.45,
        |  "statePensionNewRulesAmount": 123.45,
        |  "statePensionAgeDate": "2018-11-12",
        |  "post2016YearsUsed": 2,
        |  "newStatePensionQualifyingYears": 1,
        |  "incompleteContributionRecordIndicator": true
        |}
        |""".stripMargin)

    val expectedModel = CalculationResult(
      nino = "AA123456A",
      protectedPaymentAmount = 123.45,
      rebateDerivedAmount = 123.45,
      newStatePensionEntitlementAmount = 123.45,
      reducedRateElectionToConsider = false,
      pensionShareOrderContractedOutEmploymentGroup = true,
      pensionShareOrderStateEarningsRelatedPensionService = true,
      isleOfManContributions = false,
      contractedOutEmploymentGroupInvestigationPosition = true,
      statePensionOldRulesAmount = 123.45,
      basicAmount = 123.45,
      additionalPensionPre1997GrossAmount = 123.45,
      additionalPensionPre1997NetAmount = 123.45,
      additionalPensionPost1997Amount = 123.45,
      additionalPensionPost2002Amount = 123.45,
      graduatedRetirementBenefitAmount = 123.45,
      statePensionNewRulesAmount = 123.45,
      statePensionAgeDate = "2018-11-12",
      post2016YearsUsed = 2,
      newStatePensionQualifyingYears = 1,
      incompleteContributionRecordIndicator = true)
  }

  object Notes {
    val json: JsValue = Json.parse(
      """
        |{
        |  "noteIdentifier": 1,
        |  "numberOfNoteFields": 4,
        |  "fieldsList": [
        |    {
        |      "noteField": "note #1"
        |    },
        |    {
        |      "noteField": "note #2"
        |    },
        |    {
        |      "noteField": "note #3"
        |    },
        |    {
        |      "noteField": "note #4"
        |    }
        |  ]
        |}
      """.stripMargin)

    val expectedModel = CalculationNote(1,
      4,
      Seq(
        "note #1",
        "note #2",
        "note #3",
        "note #4"
      )
    )

    val emptyJson: JsValue = Json.parse(
      """
        |{
        |  "noteIdentifier": 1,
        |  "numberOfNoteFields": 0,
        |  "fieldsList": []
        |}
      """.stripMargin)

    val expectedEmptyModel = CalculationNote(1, 0, Seq())

    val generatedJson: JsValue = json
  }

  object QualifyingYears {
    val taxYear = 2017

    val json: JsValue = Json.parse(
      s"""
         |{
         |  "taxYear": $taxYear,
         |  "qualifyingTaxYear": true,
         |  "earningsAmount": 12345.67
         |}
      """.stripMargin)

    val expectedModel = QualifyingYear(taxYear, qualifyingTaxYear = true, BigDecimal("12345.67"))

  }

  object Response {
    private val placeholder = "PLACEHOLDER"
    private val json =
      s"""
         |{
         |  "$placeholder": ${Result.json},
         |  "associatedNotes": [${Notes.json}, ${Notes.json}],
         |  "listOfQualifyingYears": [${QualifyingYears.json}, ${QualifyingYears.json}]
         |}
      """.stripMargin

    val initialCalcJson: JsValue = Json.parse(json.replace(placeholder, "initialRequestResult"))
    val finalCalcJson: JsValue = Json.parse(json.replace(placeholder, "finalRequestResult"))
    val generatedJson: JsValue = Json.parse(json.replace(placeholder, "result"))

    val expectedModel = CalculationResponse(Result.expectedModel,
      Some(Seq(Notes.expectedModel, Notes.expectedModel)),
      Seq(QualifyingYears.expectedModel, QualifyingYears.expectedModel)
    )
  }

  object ResponseWithoutNotes {
    private val placeholder = "PLACEHOLDER"
    private val json = Json.parse(
      s"""
         |{
         |  "$placeholder": ${Result.json},
         |  "listOfQualifyingYears": [${QualifyingYears.json}, ${QualifyingYears.json}]
         |}
      """.stripMargin).toString()

    val initialCalcJson: JsValue = Json.parse(json.replace(placeholder, "initialRequestResult"))
    val finalCalcJson: JsValue = Json.parse(json.replace(placeholder, "finalRequestResult"))
    val generatedJson: JsValue = Json.parse(json.replace(placeholder, "result"))

    val expectedModel = CalculationResponse(Result.expectedModel,
      None,
      Seq(QualifyingYears.expectedModel, QualifyingYears.expectedModel)
    )
  }

}
