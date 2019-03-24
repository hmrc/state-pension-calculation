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
        "nino" -> calc.nino,
        "statePensionEligibilityDate" -> calc.statePensionAgeDate,
        "amounts" -> Json.obj(
          "protectedPayment" -> calc.protectedPaymentAmount,
          "rebateDerived" -> calc.rebateDerivedAmount,
          "newStatePensionEntitlement" -> calc.newStatePensionEntitlementAmount,
          "basicPension" -> calc.basicAmount,
          "oldRulesStatePension" -> calc.statePensionOldRulesAmount,
          "newRulesStatePension" -> calc.statePensionNewRulesAmount,
          "graduatedRetirementBenefit" -> calc.graduatedRetirementBenefitAmount,
          "additionalPension" -> Json.obj(
            "grossPre1997" -> calc.additionalPensionPre1997GrossAmount,
            "netPre1997" -> calc.additionalPensionPre1997NetAmount,
            "post1997" -> calc.additionalPensionPost1997Amount,
            "post2002" -> calc.additionalPensionPost2002Amount
          )
        ),
        "reducedRateElection" -> calc.reducedRateElectionToConsider,
        "pensionShareOrder" -> Json.obj(
          "contractedOutEmploymentGroup" -> calc.pensionShareOrderContractedOutEmploymentGroup,
          "stateEarningsRelatedPensionService" -> calc.pensionShareOrderStateEarningsRelatedPensionService
        ),
        "contributions" -> Json.obj(
          "isleOfMan" -> calc.isleOfManContributions,
          "post2016YearsUsed" -> calc.post2016YearsUsed,
          "newStatePensionQualifyingYears" -> calc.newStatePensionQualifyingYears,
          "incomplete" -> calc.incompleteContributionRecordIndicator,
          "qualifyingYears" -> Json.toJson(data.qualifyingYears)
        ),
        "underInvestigation" -> calc.contractedOutEmploymentGroupInvestigationPosition,
        "associatedNotes" -> Json.toJson(data.notes)
      )
    }
  }

}
