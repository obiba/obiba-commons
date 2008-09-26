package org.obiba.onyx.jade.engine.state;

import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.StageDependencyCondition;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.ParticipantInterview;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentService;

/**
 * jade specific stage dependency condition depending on the presence of runValues for the stage
 * @author acarey
 */
public class JadeStageDependencyCondition extends StageDependencyCondition {

  private static final long serialVersionUID = 1L;

  private EntityQueryService queryService;

  private InstrumentRunService instrumentRunService;

  private InstrumentService instrumentService;

  private String stageName;

  public JadeStageDependencyCondition() {
  }

  public JadeStageDependencyCondition(String name) {
    this.stageName = name;
  }

  public void setInstrumentRunService(InstrumentRunService instrumentRunService) {
    this.instrumentRunService = instrumentRunService;
  }

  public void setInstrumentService(InstrumentService instrumentService) {
    this.instrumentService = instrumentService;
  }

  public void setQueryService(EntityQueryService queryService) {
    this.queryService = queryService;
  }

  /**
   * Returns a Boolean depending on the fact that the step is completed and also on its result
   * Null if not completed
   * True if completed and has InstrumentRunValues
   * False if completed and does not have InstrumentRunValues
   */
  @Override
  public Boolean isDependencySatisfied(ActiveInterviewService activeInterviewService) {
    IStageExecution stageExecution = activeInterviewService.getStageExecution(stageName);

    if(!stageExecution.isCompleted()) return null;
    else {
      ParticipantInterview participantInterview = new ParticipantInterview();
      participantInterview.setParticipant(activeInterviewService.getParticipant());
      participantInterview = queryService.matchOne(participantInterview);
      
      InstrumentRun instrumentRun = instrumentRunService.getLastCompletedInstrumentRun(participantInterview, instrumentService.getInstrumentType(stageName));

      if(instrumentRun != null && instrumentRun.getInstrumentRunValues().size() > 0) return true;
      else
        return false;
    }
  }

  @Override
  public boolean isDependentOn(String stageName) {
    return this.stageName.equals(stageName);
  }

  public String getStageName() {
    return stageName;
  }

  public void setStageName(String stageName) {
    this.stageName = stageName;
  }
}