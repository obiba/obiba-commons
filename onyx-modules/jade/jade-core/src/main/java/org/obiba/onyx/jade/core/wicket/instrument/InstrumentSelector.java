package org.obiba.onyx.jade.core.wicket.instrument;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.convert.IConverter;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentStatus;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.wicket.markup.html.panel.KeyValueDataPanel;

public class InstrumentSelector extends Panel {

  private static final long serialVersionUID = 3920957095572085598L;

  private Instrument instrument = null;

  @SpringBean
  private EntityQueryService queryService;

  @SuppressWarnings("serial")
  public InstrumentSelector(String id, IModel instrumentTypeModel) {
    super(id, instrumentTypeModel);

    // get only active instruments in this type.
    Instrument template = new Instrument();
    template.setInstrumentType((InstrumentType) getModelObject());
    template.setStatus(InstrumentStatus.ACTIVE);

    KeyValueDataPanel selector = new KeyValueDataPanel("selector");
    add(selector);
    selector.addRow(new Label(KeyValueDataPanel.getRowKeyId(), new StringResourceModel("InstrumentBarcode", InstrumentSelector.this, null)), new Selector(KeyValueDataPanel.getRowValueId()));

    String barcodes = "";
    for(Instrument inst : queryService.match(template)) {
      barcodes += inst.getBarcode() + " ";
    }
    add(new Label("values", barcodes));
  }

  public Instrument getInstrument() {
    return instrument;
  }

  public void setInstrument(Instrument instrument) {
    this.instrument = instrument;
  }

  @SuppressWarnings("serial")
  private class Selector extends Fragment {

    public Selector(String id) {
      super(id, "selectorFragment", InstrumentSelector.this);

      final TextField tf = new RequiredTextField("field", new PropertyModel(InstrumentSelector.this, "instrument"), Instrument.class) {
        @SuppressWarnings("unchecked")
        @Override
        public IConverter getConverter(Class type) {
          return new InstrumentBarcodeConverter(queryService);
        }
      };
      tf.setLabel(new StringResourceModel("InstrumentBarcode", InstrumentSelector.this, null));
      tf.setOutputMarkupId(true);
      add(tf);
    }
  }
}