package org.obiba.onyx.jade.core.service;

import java.util.List;

import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.domain.run.ParticipantInterview;

public interface InstrumentRunService {

  /**
   * Create an instrument run in the scope of participant interview.
   * @param participantInterview
   * @param instrument
   * @return
   */
  public InstrumentRun createInstrumentRun(ParticipantInterview participantInterview, Instrument instrument);

  /**
   * Set the run as completed.
   * @param instrumentRun
   */
  public void completeInstrumentRun(InstrumentRun instrumentRun);

  public void cancelInstrumentRun(InstrumentRun instrumentRun);

  public void failInstrumentRun(InstrumentRun instrumentRun);

  public List<InstrumentRun> getCompletedInstrumentRuns(ParticipantInterview participantInterview, Instrument instrument);

  /**
   * Get the last instrument whatever is its status for participant and instrument type. 
   * @param participantInterview
   * @param instrumentType
   * @return
   */
  public InstrumentRun getLastInstrumentRun(ParticipantInterview participantInterview, InstrumentType instrumentType);
  
  /**
   * Get the last completed run for participant and instrument type.
   * @param participantInterview
   * @param instrumentType
   * @return
   */
  public InstrumentRun getLastCompletedInstrumentRun(ParticipantInterview participantInterview, InstrumentType instrumentType);

  /**
   * Find the value from the last completed run of the instrument of the given type for given participant.
   * @param participantInterview
   * @param instrumentType
   * @param parameterName
   * @return
   */
  public InstrumentRunValue findInstrumentRunValue(ParticipantInterview participantInterview, InstrumentType instrumentType, String parameterName);

}