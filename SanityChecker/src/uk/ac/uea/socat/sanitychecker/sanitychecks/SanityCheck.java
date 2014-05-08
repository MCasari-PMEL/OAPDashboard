package uk.ac.uea.socat.sanitychecker.sanitychecks;

import java.util.ArrayList;
import java.util.List;

import uk.ac.uea.socat.sanitychecker.Message;
import uk.ac.uea.socat.sanitychecker.data.SocatDataRecord;

/**
 * The base class for a Sanity Check routine. These classes will be called
 * to sanity check the data after it's been read and processed for missing/
 * out of range values.
 */
public abstract class SanityCheck {

	/**
	 * The list of error/warning messages generated by this checker
	 */
	protected List<Message> itsMessages;
	
	/**
	 * Base constructor - initialises message list
	 */
	public SanityCheck() {
		itsMessages = new ArrayList<Message>();
	}
	
	/**
	 * Initialise the checker and check that the supplied fields are valid
	 * @param fieldName The name of the field 
	 * @param parameters
	 * @throws SanityCheckException
	 */
	public abstract void initialise(List<String> parameters) throws SanityCheckException;
	
	/**
	 * Processes a single record from the input data file.
	 * These sanity checkers will be passed each record in turn.
	 * @param record The record
	 */
	public abstract void processRecord(SocatDataRecord record) throws SanityCheckException;
	
	/**
	 * Some checkers can only complete their work once all the records
	 * have been processed. This method is called after all the records
	 * have been passed to the checker via {@link #processRecord(SocatDataRecord)}.
	 */
	public void performFinalCheck() throws SanityCheckException {
		// Most checkers will not need a final check,
		// so we implement a noop version for them.
		// Because we're nice like that.
	}
	
	/**
	 * Returns the list of messages generated by this checker
	 * @return The list of messages generated by this checker
	 */
	public List<Message> getMessages() {
		return itsMessages;
	}
}
