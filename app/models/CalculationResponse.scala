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

package models

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class CalculationResponse(result: CalculationResult,
                               notes: Seq[CalculationNote],
                               qualifyingYears: Seq[QualifyingYear])

object CalculationResponse {
  implicit val reads: Reads[CalculationResponse] = (
    (__ \ "initialRequestResult").read[CalculationResult] or
      (__ \ "finalRequestResult").read[CalculationResult] and
      (__ \ "associatedNotes").read[Seq[CalculationNote]] and
      (__ \ "listOfQualifyingYears").read[Seq[QualifyingYear]]
    ) (CalculationResponse.apply _)

  implicit val writes: Writes[CalculationResponse] = new Writes[CalculationResponse] {
    override def writes(data: CalculationResponse): JsValue = {
      val calc = data.result
      Json.obj(
        "result" -> Json.obj(
          "nino" -> calc.nino,
          "protectedPaymentAmount" -> calc.protectedPaymentAmount,
          "rebateDerivedAmount" -> calc.rebateDerivedAmount,
          "newStatePensionEntitlementAmount" -> calc.newStatePensionEntitlementAmount,
          "reducedRateElectionToConsider" -> calc.reducedRateElectionToConsider,
          "pensionShareOrderContractedOutEmploymentGroup" -> calc.pensionShareOrderContractedOutEmploymentGroup,
          "pensionShareOrderStateEarningsRelatedPensionService" -> calc.pensionShareOrderStateEarningsRelatedPensionService,
          "isleOfManContributions" -> calc.isleOfManContributions,
          "contractedOutEmploymentGroupInvestigationPosition" -> calc.contractedOutEmploymentGroupInvestigationPosition,
          "statePensionOldRulesAmount" -> calc.statePensionOldRulesAmount,
          "basicAmount" -> calc.basicAmount,
          "additionalPensionPre1997GrossAmount" -> calc.additionalPensionPre1997GrossAmount,
          "additionalPensionPre1997NetAmount" -> calc.additionalPensionPre1997NetAmount,
          "additionalPensionPost1997Amount" -> calc.additionalPensionPost1997Amount,
          "additionalPensionPost2002Amount" -> calc.additionalPensionPost2002Amount,
          "graduatedRetirementBenefitAmount" -> calc.graduatedRetirementBenefitAmount,
          "statePensionNewRulesAmount" -> calc.statePensionNewRulesAmount,
          "statePensionAgeDate" -> calc.statePensionAgeDate,
          "post2016YearsUsed" -> calc.post2016YearsUsed,
          "newStatePensionQualifyingYears" -> calc.newStatePensionQualifyingYears,
          "incompleteContributionRecordIndicator" -> calc.incompleteContributionRecordIndicator
        ),
        "associatedNotes" -> Json.toJson(data.notes),
        "listOfQualifyingYears" -> Json.toJson(data.qualifyingYears)
      )
    }
  }
}
