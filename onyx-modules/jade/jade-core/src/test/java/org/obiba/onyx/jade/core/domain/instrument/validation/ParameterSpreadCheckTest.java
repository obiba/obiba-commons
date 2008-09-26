package org.obiba.onyx.jade.core.domain.instrument.validation;

import static org.easymock.EasyMock.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.domain.run.ParticipantInterview;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

public class ParameterSpreadCheckTest {

  private ParameterSpreadCheck parameterSpreadCheck;
  
  private ParticipantInterview interview;
  
  private InstrumentRun instrumentRun;
  
  private InstrumentType instrumentType;
  
  private Instrument instrument;
  
  private InstrumentParameter checkedParameter;
  
  private InstrumentParameter otherParameter;

  private InstrumentRunService instrumentRunServiceMock;
  
  private ActiveInstrumentRunService activeInstrumentRunServiceMock;
  
  @Before
  public void setUp() {
    parameterSpreadCheck = new ParameterSpreadCheck();

    interview = new ParticipantInterview();
    
    instrumentType = new InstrumentType();
    
    instrument = new Instrument();
    instrument.setInstrumentType(instrumentType);
    
    instrumentRun = new InstrumentRun();
    instrumentRun.setParticipantInterview(interview);
    instrumentRun.setInstrument(instrument);
    
    checkedParameter = new InstrumentOutputParameter();
    otherParameter = new InstrumentInputParameter();
    
    parameterSpreadCheck.setTargetParameter(checkedParameter);
    parameterSpreadCheck.setParameter(otherParameter);
    
    instrumentRunServiceMock = createMock(InstrumentRunService.class);
    
    activeInstrumentRunServiceMock = createMock(ActiveInstrumentRunService.class);
  }
  
  /**
   * Tests DataType.INTEGER parameters with the same value. 
   */
  @Test
  public void testIntegerParametersWithSameValue() {
    checkedParameter.setDataType(DataType.INTEGER);
    otherParameter.setDataType(DataType.INTEGER);

    // Set a spread of 5%.
    parameterSpreadCheck.setPercent(5);
    
    // Initialize checked parameter's run value.
    Data checkedData = DataBuilder.buildInteger(100l);
    
    // Test with other parameter's run value set to the SAME value.
    Data otherData = DataBuilder.buildInteger(100l);
    
    InstrumentRunValue otherRunValue = new InstrumentRunValue();
    otherRunValue.setInstrumentParameter(otherParameter);
    otherRunValue.setData(otherData);

    expect(activeInstrumentRunServiceMock.getInstrumentRun()).andReturn(instrumentRun);
    expect(instrumentRunServiceMock.findInstrumentRunValue(interview, instrumentType, otherParameter.getName())).andReturn(otherRunValue);    

    replay(activeInstrumentRunServiceMock);
    replay(instrumentRunServiceMock);
            
    Assert.assertTrue(parameterSpreadCheck.checkParameterValue(checkedData, instrumentRunServiceMock, activeInstrumentRunServiceMock));
    
    verify(activeInstrumentRunServiceMock);
    verify(instrumentRunServiceMock);    
  }
  
  /**
   * Tests DataType.INTEGER parameters within the required spread. 
   */
  @Test
  public void testIntegerParametersWithinSpread() {
    checkedParameter.setDataType(DataType.INTEGER);
    otherParameter.setDataType(DataType.INTEGER);

    // Set a spread of 5%.
    parameterSpreadCheck.setPercent(5);
    
    // Initialize other parameter's run value.
    Data otherData = DataBuilder.buildInteger(100l);
    
    InstrumentRunValue otherRunValue = new InstrumentRunValue();
    otherRunValue.setInstrumentParameter(otherParameter);
    otherRunValue.setData(otherData);
    
    // Test with checked parameter's run value set to MINIMUM value within spread.
    Data checkedDataMin = DataBuilder.buildInteger(95l);
    
    expect(activeInstrumentRunServiceMock.getInstrumentRun()).andReturn(instrumentRun);
    expect(instrumentRunServiceMock.findInstrumentRunValue(interview, instrumentType, otherParameter.getName())).andReturn(otherRunValue);    

    replay(activeInstrumentRunServiceMock);
    replay(instrumentRunServiceMock);
            
    Assert.assertTrue(parameterSpreadCheck.checkParameterValue(checkedDataMin, instrumentRunServiceMock, activeInstrumentRunServiceMock));
    
    verify(activeInstrumentRunServiceMock);
    verify(instrumentRunServiceMock);
 
    // Reset mocks.
    reset(activeInstrumentRunServiceMock);
    reset(instrumentRunServiceMock);
    
    // Test with checked parameter's run value set to MAXIMUM value within spread.
    Data checkedDataMax = DataBuilder.buildInteger(105l);

    expect(activeInstrumentRunServiceMock.getInstrumentRun()).andReturn(instrumentRun);
    expect(instrumentRunServiceMock.findInstrumentRunValue(interview, instrumentType, otherParameter.getName())).andReturn(otherRunValue);    

    replay(activeInstrumentRunServiceMock);
    replay(instrumentRunServiceMock);
            
    Assert.assertTrue(parameterSpreadCheck.checkParameterValue(checkedDataMax, instrumentRunServiceMock, activeInstrumentRunServiceMock));
    
    verify(instrumentRunServiceMock); 
  }
  
  /**
   * Tests DataType.INTEGER parameters outside the required spread. 
   */
  @Test
  public void testIntegerParametersOutsideSpread() {
    checkedParameter.setDataType(DataType.INTEGER);
    otherParameter.setDataType(DataType.INTEGER);

    // Set a spread of 5%.
    parameterSpreadCheck.setPercent(5);
    
    // Initialize other parameter's run value.
    Data otherData = DataBuilder.buildInteger(100l);
    
    InstrumentRunValue otherRunValue = new InstrumentRunValue();
    otherRunValue.setInstrumentParameter(otherParameter);
    otherRunValue.setData(otherData);
    
    // Test with checked parameter's run value set to LESS than the minimum value in spread.
    Data checkedDataMin = DataBuilder.buildInteger(94l);
    
    expect(activeInstrumentRunServiceMock.getInstrumentRun()).andReturn(instrumentRun);
    expect(instrumentRunServiceMock.findInstrumentRunValue(interview, instrumentType, otherParameter.getName())).andReturn(otherRunValue);    

    replay(activeInstrumentRunServiceMock);
    replay(instrumentRunServiceMock);
            
    Assert.assertFalse(parameterSpreadCheck.checkParameterValue(checkedDataMin, instrumentRunServiceMock, activeInstrumentRunServiceMock));
    
    verify(activeInstrumentRunServiceMock);
    verify(instrumentRunServiceMock);
 
    // Reset mocks.
    reset(activeInstrumentRunServiceMock);
    reset(instrumentRunServiceMock);
    
    // Test with checked parameter's run value set to MORE than the maximum value in spread.
    Data checkedDataMax = DataBuilder.buildInteger(106l);
    
    expect(activeInstrumentRunServiceMock.getInstrumentRun()).andReturn(instrumentRun);
    expect(instrumentRunServiceMock.findInstrumentRunValue(interview, instrumentType, otherParameter.getName())).andReturn(otherRunValue);    

    replay(activeInstrumentRunServiceMock);
    replay(instrumentRunServiceMock);
            
    Assert.assertFalse(parameterSpreadCheck.checkParameterValue(checkedDataMax, instrumentRunServiceMock, activeInstrumentRunServiceMock));
    
    verify(instrumentRunServiceMock); 
  }
  
  /**
   * Tests DataType.DECIMAL parameters with the same value. 
   */
  @Test
  public void testDecimalParametersWithSameValue() {
    checkedParameter.setDataType(DataType.DECIMAL);
    otherParameter.setDataType(DataType.DECIMAL);

    // Set a spread of 5%.
    parameterSpreadCheck.setPercent(5);
    
    // Initialize checked parameter's run value.
    Data checkedData = DataBuilder.buildDecimal(100.0);
    
    // Test with other parameter's run value set to the SAME value.
    Data otherData = DataBuilder.buildDecimal(100.0);
    
    InstrumentRunValue otherRunValue = new InstrumentRunValue();
    otherRunValue.setInstrumentParameter(otherParameter);
    otherRunValue.setData(otherData);

    expect(activeInstrumentRunServiceMock.getInstrumentRun()).andReturn(instrumentRun);
    expect(instrumentRunServiceMock.findInstrumentRunValue(interview, instrumentType, otherParameter.getName())).andReturn(otherRunValue);    

    replay(activeInstrumentRunServiceMock);
    replay(instrumentRunServiceMock);
            
    Assert.assertTrue(parameterSpreadCheck.checkParameterValue(checkedData, instrumentRunServiceMock, activeInstrumentRunServiceMock));
    
    verify(activeInstrumentRunServiceMock);
    verify(instrumentRunServiceMock);    
  }
  
  /**
   * Tests DataType.DECIMAL parameters within the required spread. 
   */
  @Test
  public void testDecimalParametersWithinSpread() {
    checkedParameter.setDataType(DataType.DECIMAL);
    otherParameter.setDataType(DataType.DECIMAL);

    // Set a spread of 5%.
    parameterSpreadCheck.setPercent(5);
    
    // Initialize other parameter's run value.
    Data otherData = DataBuilder.buildDecimal(100.0);
    
    InstrumentRunValue otherRunValue = new InstrumentRunValue();
    otherRunValue.setInstrumentParameter(otherParameter);
    otherRunValue.setData(otherData);
    
    // Test with checked parameter's run value set to MINIMUM value within spread.
    Data checkedDataMin = DataBuilder.buildDecimal(95.0);
    
    expect(activeInstrumentRunServiceMock.getInstrumentRun()).andReturn(instrumentRun);
    expect(instrumentRunServiceMock.findInstrumentRunValue(interview, instrumentType, otherParameter.getName())).andReturn(otherRunValue);    

    replay(activeInstrumentRunServiceMock);
    replay(instrumentRunServiceMock);
            
    Assert.assertTrue(parameterSpreadCheck.checkParameterValue(checkedDataMin, instrumentRunServiceMock, activeInstrumentRunServiceMock));
    
    verify(activeInstrumentRunServiceMock);
    verify(instrumentRunServiceMock);
 
    // Reset mocks.
    reset(activeInstrumentRunServiceMock);
    reset(instrumentRunServiceMock);
    
    // Test with checked parameter's run value set to MAXIMUM value within spread.
    Data checkedDataMax = DataBuilder.buildDecimal(105.0);
    
    expect(activeInstrumentRunServiceMock.getInstrumentRun()).andReturn(instrumentRun);
    expect(instrumentRunServiceMock.findInstrumentRunValue(interview, instrumentType, otherParameter.getName())).andReturn(otherRunValue);    

    replay(activeInstrumentRunServiceMock);
    replay(instrumentRunServiceMock);
            
    Assert.assertTrue(parameterSpreadCheck.checkParameterValue(checkedDataMax, instrumentRunServiceMock, activeInstrumentRunServiceMock));

    verify(activeInstrumentRunServiceMock);
    verify(instrumentRunServiceMock); 
  }
  
  /**
   * Tests DataType.DECIMAL parameters outside the required spread. 
   */
  @Test
  public void testDecimalParametersOutsideSpread() {
    checkedParameter.setDataType(DataType.DECIMAL);
    otherParameter.setDataType(DataType.DECIMAL);

    // Set a spread of 5%.
    parameterSpreadCheck.setPercent(5);
    
    // Initialize other parameter's run value.
    Data otherData = DataBuilder.buildDecimal(100.0);
    
    InstrumentRunValue otherRunValue = new InstrumentRunValue();
    otherRunValue.setInstrumentParameter(otherParameter);
    otherRunValue.setData(otherData);
    
    // Test with checked parameter's run value set to LESS than the minimum value in spread.
    Data checkedDataMin = DataBuilder.buildDecimal(94.0);

    expect(activeInstrumentRunServiceMock.getInstrumentRun()).andReturn(instrumentRun);
    expect(instrumentRunServiceMock.findInstrumentRunValue(interview, instrumentType, otherParameter.getName())).andReturn(otherRunValue);    

    replay(activeInstrumentRunServiceMock);
    replay(instrumentRunServiceMock);
            
    Assert.assertFalse(parameterSpreadCheck.checkParameterValue(checkedDataMin, instrumentRunServiceMock, activeInstrumentRunServiceMock));

    verify(activeInstrumentRunServiceMock);
    verify(instrumentRunServiceMock);
 
    // Reset mocks.
    reset(activeInstrumentRunServiceMock);
    reset(instrumentRunServiceMock);
    
    // Test with checked parameter's run value set to MORE than the maximum value in spread.
    Data checkedDataMax = DataBuilder.buildDecimal(106.0);

    expect(activeInstrumentRunServiceMock.getInstrumentRun()).andReturn(instrumentRun);    
    expect(instrumentRunServiceMock.findInstrumentRunValue(interview, instrumentType, otherParameter.getName())).andReturn(otherRunValue);    

    replay(activeInstrumentRunServiceMock);
    replay(instrumentRunServiceMock);
            
    Assert.assertFalse(parameterSpreadCheck.checkParameterValue(checkedDataMax, instrumentRunServiceMock, activeInstrumentRunServiceMock));
    
    verify(activeInstrumentRunServiceMock);
    verify(instrumentRunServiceMock); 
  }
}