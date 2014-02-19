package de.lyca.xslt;

import java.util.ArrayList;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

public class RecordingErrorListener implements ErrorListener {

  private final ArrayList<TransformerException> warnings = new ArrayList<>();
  private final ArrayList<TransformerException> errors = new ArrayList<>();
  private final ArrayList<TransformerException> fatalErrors = new ArrayList<>();

  @Override
  public void warning(TransformerException exception) throws TransformerException {
    warnings.add(exception);
  }

  @Override
  public void error(TransformerException exception) throws TransformerException {
    errors.add(exception);
  }

  @Override
  public void fatalError(TransformerException exception) throws TransformerException {
    fatalErrors.add(exception);
  }

  public ArrayList<TransformerException> getWarnings() {
    return warnings;
  }

  public ArrayList<TransformerException> getErrors() {
    return errors;
  }

  public ArrayList<TransformerException> getFatalErrors() {
    return fatalErrors;
  }

  public void reset() {
    warnings.clear();
    errors.clear();
    fatalErrors.clear();
  }

}
