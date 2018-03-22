package uk.gov.justice.digital.nomis.service.transformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.nomis.api.ExternalMovement;
import uk.gov.justice.digital.nomis.jpa.entity.MovementReason;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderExternalMovement;

import java.sql.Timestamp;
import java.util.Optional;

@Component
public class MovementsTransformer {

    private final TypesTransformer typesTransformer;

    @Autowired
    public MovementsTransformer(TypesTransformer typesTransformer) {
        this.typesTransformer = typesTransformer;
    }

    public ExternalMovement movementOf(OffenderExternalMovement em) {

        MovementReason movementReason = em.getMovementReason();

        return ExternalMovement.builder()
                .active(em.getActiveFlag())
                .comments(em.getCommentText())
                .fromAddressId(em.getFromAddressId())
                .toAddressId(em.getToAddressId())
                .fromAgencyLocationId(em.getFromAgyLocId())
                .toAgencyCodeLocationId(em.getToAgyLocId())
                .movementDirection(em.getDirectionCode())
                .movementReasonCode(movementReason.getMovementReasonCode())
                .movementTypeCode(movementReason.getMovementType())
                .offenderBookingId(em.getId().getOffenderBooking().getOffenderBookId())
                .offenderId(em.getId().getOffenderBooking().getRootOffenderId())
                .sequenceNumber(em.getId().getMovementSeq())
                .movementDateTime(em.getMovementTime().toLocalDateTime())
                .unemploymentPay(typesTransformer.ynToBoolean(movementReason.getUnemploymentPay()))
                .escapeRecapture(typesTransformer.ynToBoolean(movementReason.getEscRecapFlag()))
                .inMovementType(movementReason.getInMovementType())
                .inMovementReasonCode(movementReason.getMovementReasonCode())
                .transportation(typesTransformer.ynToBoolean(movementReason.getTransportationFlag()))
                .internalScheduleType(em.getInternalScheduleType())
                .internalScheduleReasonCode(em.getInternalScheduleReasonCode())
                .arrestAgencyLocationId(em.getArrestAgencyLocId())
                .toProvStatCode(em.getToProvStatCode())
                .escortCode(em.getEscortCode())
                .escortText(em.getEscortText())
                .reportingDateTime(Optional.ofNullable(em.getReportingTime()).map(Timestamp::toLocalDateTime).orElse(null))
                .toCityCode(em.getToCity())
                .fromCityCode(em.getFromCity())
                .toCountryCode(em.getToCountryCode())
                .ojLocationCode(em.getOjLocationCode())
                .build();
    }
}