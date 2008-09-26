package org.obiba.onyx.jade.core.wicket.instrument;

import java.io.Serializable;
import java.util.Arrays;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.jade.core.domain.instrument.ContraIndication;
import org.obiba.onyx.jade.core.domain.instrument.ParticipantInteractionType;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObservedContraIndicationPanel extends Panel {

  private static final long serialVersionUID = 1839206247478532673L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(ObservedContraIndicationPanel.class);

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;

  private DropDownChoice contraIndicationDropDownChoice;

  private TextArea otherContraIndication;

  private ContraIndicationSelection selectionModel;

  @SuppressWarnings("serial")
  public ObservedContraIndicationPanel(String id) {
    super(id);
    setOutputMarkupId(true);

    selectionModel = new ContraIndicationSelection();
    RadioGroup radioGroup = new RadioGroup("radioGroup", new PropertyModel(selectionModel, "selection"));
    radioGroup.setLabel(new StringResourceModel("YesNo", this, null));
    add(radioGroup);
    ListView radioList = new ListView("radioItem", Arrays.asList(new String[] { "Yes", "No" })) {

      @Override
      protected void populateItem(ListItem item) {
        final String key = item.getModelObjectAsString();
        Radio radio = new Radio("radio", item.getModel());
        radio.add(new AjaxEventBehavior("onchange") {

          @Override
          protected void onEvent(AjaxRequestTarget target) {
            boolean yes = key.equals("Yes");
            selectionModel.setContraIndication(null);
            selectionModel.setOtherContraIndication(null);
            contraIndicationDropDownChoice.setEnabled(yes);
            contraIndicationDropDownChoice.setRequired(yes);
            otherContraIndication.setEnabled(false);
            target.addComponent(contraIndicationDropDownChoice);
            target.addComponent(otherContraIndication);
          }

        });
        item.add(radio);
        item.add(new Label("label", new StringResourceModel(key, ObservedContraIndicationPanel.this, null)));
      }

    }.setReuseItems(true);
    radioGroup.add(radioList);
    radioGroup.setRequired(true);

    ContraIndication template = new ContraIndication();
    template.setType(ParticipantInteractionType.OBSERVED);
    template.setInstrument(activeInstrumentRunService.getInstrument());

    contraIndicationDropDownChoice = new DropDownChoice("ciChoice", new PropertyModel(selectionModel, "contraIndication"), queryService.match(template), new IChoiceRenderer() {

      public Object getDisplayValue(Object object) {
        ContraIndication ci = (ContraIndication)object;
        return ci.getDescription();
      }

      public String getIdValue(Object object, int index) {
        ContraIndication ci = (ContraIndication)object;
        return ci.getName();
      }
      
    });
    contraIndicationDropDownChoice.setOutputMarkupId(true);
    contraIndicationDropDownChoice.setLabel(new StringResourceModel("ContraIndicationSelection", this, null));
    contraIndicationDropDownChoice.add(new OnChangeAjaxBehavior() {

      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        selectionModel.setOtherContraIndication(null);
        boolean other = selectionModel.getContraIndication() != null && selectionModel.getContraIndication().getName().equals("Other");
        otherContraIndication.setEnabled(other);
        otherContraIndication.setRequired(other);
        target.addComponent(otherContraIndication);
      }

    });
    add(contraIndicationDropDownChoice);

    otherContraIndication = new TextArea("otherCi", new PropertyModel(selectionModel, "otherContraIndication"));
    otherContraIndication.setOutputMarkupId(true);
    otherContraIndication.setEnabled(false);
    otherContraIndication.setLabel(new StringResourceModel("OtherContraIndication", this, null));
    add(otherContraIndication);

  }

  @SuppressWarnings("serial")
  private class ContraIndicationSelection implements Serializable {

    private String selection;

    public String getSelection() {
      return selection;
    }

    public void setSelection(String selection) {
      this.selection = selection;
    }

    public String getOtherContraIndication() {
      return activeInstrumentRunService.getOtherContraIndication();
    }

    public void setOtherContraIndication(String otherContraIndication) {
      activeInstrumentRunService.setOtherContraIndication(otherContraIndication);
    }

    public ContraIndication getContraIndication() {
      return activeInstrumentRunService.getContraIndication();
    }

    public void setContraIndication(ContraIndication contraIndication) {
      activeInstrumentRunService.setContraIndication(contraIndication);
    }

  }

}