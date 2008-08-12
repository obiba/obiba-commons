package org.obiba.onyx.webapp.participant.page;

import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.webapp.base.page.BasePage;
import org.obiba.onyx.webapp.participant.panel.ParticipantPanel;
import org.obiba.onyx.webapp.stage.panel.StageSelectionPanel;
import org.obiba.onyx.webapp.util.DateUtils;
import org.obiba.wicket.markup.html.panel.KeyValueDataPanel;

@AuthorizeInstantiation({"SYSTEM_ADMINISTRATOR", "PARTICIPANT_MANAGER", "DATA_COLLECTION_OPERATOR"})
public class InterviewPage extends BasePage {

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  public InterviewPage() {
    super();

    Participant participant = activeInterviewService.getParticipant();
    Interview interview = activeInterviewService.getInterview();
    
    if(participant == null || interview == null) {
      setResponsePage(WebApplication.get().getHomePage());
    } else {

      add(new ParticipantPanel("participant", participant));
      
      KeyValueDataPanel kvPanel = new KeyValueDataPanel("interview");
      kvPanel.addRow(new StringResourceModel("StartDate", this, null), DateUtils.getFullDateModel(new PropertyModel(interview, "startDate")));
      kvPanel.addRow(new StringResourceModel("EndDate", this, null), DateUtils.getFullDateModel(new PropertyModel(interview, "stopDate")));
      kvPanel.addRow(new StringResourceModel("Status", this, null), new StringResourceModel("InterviewStatus." + interview.getStatus(), this, null));
      add(kvPanel);

      add(new StageSelectionPanel("stage-list", getFeedbackPanel()));
    }
  }

}