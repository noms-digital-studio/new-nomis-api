package uk.gov.justice.digital.nomis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.nomis.api.Sentence;
import uk.gov.justice.digital.nomis.jpa.entity.Offender;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderBooking;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderSentence;
import uk.gov.justice.digital.nomis.jpa.repository.OffenderRepository;
import uk.gov.justice.digital.nomis.jpa.repository.OffenderSentencesRepository;
import uk.gov.justice.digital.nomis.service.transformer.SentenceTransformer;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SentencesService {

    private final OffenderSentencesRepository offenderSentencesRepository;
    private final OffenderRepository offenderRepository;
    private final SentenceTransformer sentenceTransformer;

    @Autowired
    public SentencesService(OffenderSentencesRepository offenderSentencesRepository, OffenderRepository offenderRepository, SentenceTransformer sentenceTransformer) {
        this.offenderSentencesRepository = offenderSentencesRepository;
        this.offenderRepository = offenderRepository;
        this.sentenceTransformer = sentenceTransformer;
    }

    @Transactional
    public Page<Sentence> getSentences(Pageable pageable) {

        Page<OffenderSentence> offenderSentences = offenderSentencesRepository.findAll(pageable);

        List<Sentence> sentencesList = offenderSentences.getContent()
                .stream()
                .map(sentenceTransformer::sentenceOf)
                .collect(Collectors.toList());

        return new PageImpl<>(sentencesList, pageable, offenderSentences.getTotalElements());
    }

    @Transactional
    public Optional<List<Sentence>> sentencesForOffenderId(Long offenderId) {

        Optional<List<OffenderSentence>> maybeOffenderSentences = Optional.ofNullable(offenderRepository.findOne(offenderId))
                .map(offender -> offender.getOffenderBookings().stream()
                        .map(OffenderBooking::getOffenderSentences).
                                flatMap(Collection::stream).
                                collect(Collectors.toList()));

        return maybeOffenderSentences.map(offenderSentences -> offenderSentences
                .stream()
                .map(sentenceTransformer::sentenceOf)
                .collect(Collectors.toList()));
    }

    @Transactional
    public Optional<List<Sentence>> sentencesForOffenderIdAndBookingId(Long offenderId, Long bookingId) {
        Optional<Offender> maybeOffender = Optional.ofNullable(offenderRepository.findOne(offenderId));

        if (!maybeOffender.isPresent()) {
            return Optional.empty();
        }

        Optional<OffenderBooking> maybeOffenderBooking = maybeOffender.get().getOffenderBookings()
                .stream()
                .filter(ob -> ob.getOffenderBookId().equals(bookingId))
                .findFirst();

        Optional<List<Sentence>> maybeSentences = maybeOffenderBooking.map(ob -> ob.getOffenderSentences()
                .stream().map(sentenceTransformer::sentenceOf).collect(Collectors.toList()));

        return maybeSentences;
    }

}