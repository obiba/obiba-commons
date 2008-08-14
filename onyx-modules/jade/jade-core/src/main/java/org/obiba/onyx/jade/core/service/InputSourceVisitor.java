package org.obiba.onyx.jade.core.service;

import org.obiba.onyx.jade.core.domain.instrument.FixedSource;
import org.obiba.onyx.jade.core.domain.instrument.OperatorSource;
import org.obiba.onyx.jade.core.domain.instrument.OutputParameterSource;
import org.obiba.onyx.jade.core.domain.instrument.ParticipantPropertySource;

public interface InputSourceVisitor {
  
  public void visit(FixedSource source);

  public void visit(OperatorSource source);

  public void visit(OutputParameterSource source);

  public void visit(ParticipantPropertySource source);
  
}