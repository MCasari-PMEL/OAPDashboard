/**
 * 
 */
package gov.noaa.pmel.dashboard.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import gov.noaa.pmel.dashboard.actions.DatasetChecker;
import gov.noaa.pmel.dashboard.actions.DatasetModifier;
import gov.noaa.pmel.dashboard.handlers.DataFileHandler;
import gov.noaa.pmel.dashboard.handlers.MetadataFileHandler;
import gov.noaa.pmel.dashboard.handlers.UserFileHandler;
import gov.noaa.pmel.dashboard.shared.DashboardDataset;
import gov.noaa.pmel.dashboard.shared.DashboardDatasetData;
import gov.noaa.pmel.dashboard.shared.DashboardDatasetList;
import gov.noaa.pmel.dashboard.shared.DashboardMetadata;
import gov.noaa.pmel.dashboard.shared.DashboardServicesInterface;
import gov.noaa.pmel.dashboard.shared.DashboardUtils;
import gov.noaa.pmel.dashboard.shared.DataColumnType;
import gov.noaa.pmel.dashboard.shared.SCMessageList;
import gov.noaa.pmel.dashboard.shared.TypesDatasetDataPair;

/**
 * Implementation of DashboardServicesInterface
 * 
 * @author Karl Smith
 */
public class DashboardServices extends RemoteServiceServlet implements DashboardServicesInterface {

	private static final long serialVersionUID = -8189933983319827049L;

	private String username = null;
	private DashboardConfigStore configStore = null;

	@Override
	public void logoutUser() {
		HttpServletRequest request = getThreadLocalRequest();
		username = null;
		try {
			username = DashboardUtils.cleanUsername(request.getUserPrincipal().getName().trim());
		} catch (Exception ex) {
			// Probably null pointer exception - leave username null
		}
		HttpSession session = request.getSession(false);
		try {
			session.invalidate();
		} catch ( Exception ex ) {
			// Log but otherwise ignore this error
			Logger.getLogger("DashboardServices").error("session.invalidate failed: " + ex.getMessage());
		}
		try {
			request.logout();
		} catch ( Exception ex ) {
			Logger.getLogger("DashboardServices").error("request.logout failed: " + ex.getMessage());
		}

		Logger.getLogger("DashboardServices").info("logged out " + username);
	}

	/**
	 * Validates the given request by retrieving the current username from the request
	 * and verifying that username with the Dashboard data store.  If pageUsername is
	 * given, also checks these usernames are the same.
	 * Assigns the username and configStore fields in this instance.
	 * 
	 * @param pageUsername
	 * 		if not null, check that this matches the current page username
	 * @return
	 * 		true if the request obtained a valid username; otherwise false
	 * @throws IllegalArgumentException
	 * 		if unable to obtain the dashboard data store
	 */
	private boolean validateRequest(String pageUsername) throws IllegalArgumentException {
		username = null;
		HttpServletRequest request = getThreadLocalRequest();
		try {
			username = DashboardUtils.cleanUsername(request.getUserPrincipal().getName().trim());
		} catch (Exception ex) {
			// Probably null pointer exception
			return false;
		}
		if ( (pageUsername != null) && ! pageUsername.equals(username) )
			return false;

		configStore = null;
		try {
			configStore = DashboardConfigStore.get(true);
		} catch (Exception ex) {
			throw new IllegalArgumentException("Unexpected configuration error: " + ex.getMessage());
		}
		return configStore.validateUser(username);
	}

	@Override
	public DashboardDatasetList getDatasetList() throws IllegalArgumentException {
		// Get the dashboard data store and current username
		if ( ! validateRequest(null) ) 
			throw new IllegalArgumentException("Invalid user request");
		DashboardDatasetList cruiseList = configStore.getUserFileHandler().getDatasetListing(username);
		Logger.getLogger("DashboardServices").info("cruise list returned for " + username);
		return cruiseList;
	}

	@Override
	public DashboardDatasetList deleteDatasets(String pageUsername, 
			TreeSet<String> expocodeSet, Boolean deleteMetadata) 
					throws IllegalArgumentException {
		// Get the dashboard data store and current username, and validate that username
		if ( ! validateRequest(pageUsername) ) 
			throw new IllegalArgumentException("Invalid user request");

		DataFileHandler cruiseHandler = configStore.getDataFileHandler();
		// Delete each of the cruises in the given set
		for ( String expocode : expocodeSet ) {
			cruiseHandler.deleteDatasetFiles(expocode, username, deleteMetadata);
			// IllegalArgumentException for other problems escape as-is
			Logger.getLogger("DashboardServices").info("cruise " + expocode + " deleted by " + username);
		}

		// Return the current list of cruises, which should 
		// detect the missing cruises and update itself
		DashboardDatasetList cruiseList = configStore.getUserFileHandler().getDatasetListing(username);
		Logger.getLogger("DashboardServices").info("cruise list returned for " + username);
		return cruiseList;
	}

	@Override
	public DashboardDatasetList addDatasetsToList(String pageUsername, 
			String wildExpocode) throws IllegalArgumentException {
		// Get the dashboard data store and current username, and validate that username
		if ( ! validateRequest(pageUsername) ) 
			throw new IllegalArgumentException("Invalid user request");

		// Add the cruises to the user's list and return the updated list
		DashboardDatasetList cruiseList = configStore.getUserFileHandler()
				.addDatasetsToListing(wildExpocode, username);
		Logger.getLogger("DashboardServices").info("added cruises " + wildExpocode + " for " + username);
		return cruiseList;
	}

	@Override
	public DashboardDatasetList removeDatasetsFromList(String pageUsername,
			TreeSet<String> expocodeSet) throws IllegalArgumentException {
		// Get the dashboard data store and current username, and validate that username
		if ( ! validateRequest(pageUsername) ) 
			throw new IllegalArgumentException("Invalid user request");

		// Remove the cruises from the user's list and return the updated list
		DashboardDatasetList cruiseList = configStore.getUserFileHandler()
				.removeDatasetsFromListing(expocodeSet, username);
		Logger.getLogger("DashboardServices").info("removed cruises " + expocodeSet.toString() + " for " + username);
		return cruiseList;
	}

	@Override
	public DashboardDatasetList changeDatasetOwner(String pageUsername, 
			TreeSet<String> expocodeSet, String newOwner) throws IllegalArgumentException {
		if ( ! validateRequest(pageUsername) ) 
			throw new IllegalArgumentException("Invalid user request");
		// Get the dashboard username of the new owner
		String newUsername;
		if ( configStore.validateUser(newOwner) ) {
			// dashboard username was given
			newUsername = newOwner;
		}
		else {
			// actual name given?
			try {
				newUsername = configStore.getDatabaseRequestHandler().getReviewerUsername(newOwner);
			} catch (Exception ex) {
				newUsername = null;
			}
			if ( (newUsername == null) || ! configStore.validateUser(newUsername) ) 
				throw new IllegalArgumentException("Unknown dashboard user " + newOwner);
		}
		// Change the owner of the cruises
		DatasetModifier modifier = new DatasetModifier(configStore);
		Logger itsLogger = Logger.getLogger("DashboardServices");
		for ( String expocode : expocodeSet ) {
			modifier.changeDatasetOwner(expocode, newUsername);
			itsLogger.info("changed owner of " + expocode + " to " + newUsername);
		}
		// Return the updated list of cruises for this user
		DashboardDatasetList cruiseList = configStore.getUserFileHandler().getDatasetListing(pageUsername);
		return cruiseList;
	}

	@Override
	public DashboardDatasetList getUpdatedDatasets(String pageUsername, TreeSet<String> expocodeSet)
			throws IllegalArgumentException {
		// Get the dashboard data store and current username, and validate that username
		if ( ! validateRequest(pageUsername) ) 
			throw new IllegalArgumentException("Invalid user request");

		// Create the set of updated cruise information to return
		DataFileHandler cruiseHandler = configStore.getDataFileHandler();
		DashboardDatasetList cruiseList = new DashboardDatasetList();
		cruiseList.setUsername(username);
		cruiseList.setManager(configStore.isManager(username));
		for ( String cruiseExpocode : expocodeSet ) {
			cruiseList.put(cruiseExpocode, cruiseHandler.getDatasetFromInfoFile(cruiseExpocode));
		}
		Logger.getLogger("DashboardServices").info("returned updated cruise information for " + username);
		return cruiseList;
	}

	@Override
	public DashboardDatasetList deleteAddlDoc(String pageUsername, String deleteFilename, 
			String expocode, TreeSet<String> allExpocodes) throws IllegalArgumentException {
		// Get the dashboard data store and current username, and validate that username
		if ( ! validateRequest(pageUsername) ) 
			throw new IllegalArgumentException("Invalid user request");

		DataFileHandler cruiseHandler = configStore.getDataFileHandler();
		DashboardDataset cruise = cruiseHandler.getDatasetFromInfoFile(expocode);

		// Get the current metadata documents for the cruise
		MetadataFileHandler mdataHandler = configStore.getMetadataFileHandler();
		if ( DashboardUtils.OME_FILENAME.equals(deleteFilename) ) {
			// Remove the OME XML stub file
			if ( ! Boolean.TRUE.equals(cruise.isEditable()) ) 
				throw new IllegalArgumentException("Cannot delete the OME metadata for a submitted cruise");
		}
		else if ( DashboardUtils.PI_OME_FILENAME.equals(deleteFilename) ) {
			// No more PI-provided OME metadata for this cruise
			cruise.setOmeTimestamp(null);
		}
		else {
			// Directly modify the additional documents list in this cruise
			TreeSet<String> addlDocs = cruise.getAddlDocs();
			// Find this additional document for this cruise
			String titleToRemove = null;
			for ( String docTitle : addlDocs ) {
				String name = DashboardMetadata.splitAddlDocsTitle(docTitle)[0];
				if ( name.equals(deleteFilename) ) {
					titleToRemove = docTitle;
					break;
				}
			}
			if ( (titleToRemove == null) || ! addlDocs.remove(titleToRemove) )
				throw new IllegalArgumentException("Document " + deleteFilename + 
						" is not associated with dataset " + expocode);
		}
		// Delete this OME metadata or additional documents file on the server
		mdataHandler.deleteMetadata(username, expocode, deleteFilename);

		Logger.getLogger("DashboardServices").info("deleted metadata " + deleteFilename + 
				" from " + expocode + " for " + username);

		// Save the updated cruise
		cruiseHandler.saveDatasetInfoToFile(cruise, "Removed metadata document " + 
									deleteFilename + " from cruise " + expocode);

		// Create the set of updated cruise information to return
		DashboardDatasetList cruiseList = new DashboardDatasetList();
		cruiseList.setUsername(username);
		cruiseList.setManager(configStore.isManager(username));
		for ( String cruiseExpocode : allExpocodes ) {
			cruiseList.put(cruiseExpocode, cruiseHandler.getDatasetFromInfoFile(cruiseExpocode));
		}
		Logger.getLogger("DashboardServices").info("returned updated dataset information for " + username);
		return cruiseList;
	}

	@Override
	public TypesDatasetDataPair getDataColumnSpecs(String pageUsername,
			String expocode) throws IllegalArgumentException {
		// Get the dashboard data store and current username, and validate that username
		if ( ! validateRequest(pageUsername) ) 
			throw new IllegalArgumentException("Invalid user request");

		// Get the list of known user-provided data column types
		KnownDataTypes knownUserTypes = configStore.getKnownUserDataTypes();
		if ( knownUserTypes == null )
			throw new IllegalArgumentException("unexpected missing list of all known data column types");
		TreeSet<DashDataType> knownTypesSet = knownUserTypes.getKnownTypesSet();
		if ( knownTypesSet.isEmpty() )
			throw new IllegalArgumentException("unexpected empty list of all known data column types");
		ArrayList<DataColumnType> knownTypesList = new ArrayList<DataColumnType>(knownTypesSet.size());
		for ( DashDataType dtype : knownTypesSet )
			knownTypesList.add(dtype.duplicate());

		// Get the cruise with the first maximum-needed number of rows
		DashboardDatasetData cruiseData = configStore.getDataFileHandler()
				.getDatasetDataFromFiles(expocode, 0, DashboardUtils.MAX_ROWS_PER_GRID_PAGE);
		if ( cruiseData == null )
			throw new IllegalArgumentException(expocode + " does not exist");

		TypesDatasetDataPair cruiseTypes = new TypesDatasetDataPair();
		cruiseTypes.setAllKnownTypes(knownTypesList);
		cruiseTypes.setDatasetData(cruiseData);

		Logger.getLogger("DashboardServices").info("data columns specs returned for " + 
				expocode + " for " + username);
		// Return the cruise with the partial data
		return cruiseTypes;
	}

	@Override
	public ArrayList<ArrayList<String>> getDataWithRowNum(String pageUsername, 
			String expocode, int firstRow, int numRows) throws IllegalArgumentException {
		// Get the dashboard data store and current username, and validate that username
		if ( ! validateRequest(pageUsername) ) 
			throw new IllegalArgumentException("Invalid user request");

		int myFirstRow = firstRow;
		if ( myFirstRow < 0 )
			myFirstRow = 0;
		// Get only the desired cruise data
		DashboardDatasetData cruiseWithData = configStore.getDataFileHandler()
									.getDatasetDataFromFiles(expocode, myFirstRow, numRows);
		if ( cruiseWithData == null )
			throw new IllegalArgumentException(expocode + " does not exist");
		ArrayList<ArrayList<String>> cruiseDataWithRowNums = cruiseWithData.getDataValues();
		ArrayList<Integer> rowNums = cruiseWithData.getRowNums();
		// Modify the list in this DashboardDatasetData since it is then thrown away
		int k = 0;
		for ( ArrayList<String> rowData : cruiseDataWithRowNums ) {
			rowData.add(0, rowNums.get(k).toString());
			k++;
		}
		int myLastRow = myFirstRow + cruiseDataWithRowNums.size() - 1;
		Logger myLogger = Logger.getLogger("DashboardServices");
		myLogger.info(expocode + " cruise data [" + Integer.toString(myFirstRow) + 
				" - " + Integer.toString(myLastRow) + "] returned for " + username);
		if ( myLogger.isDebugEnabled() ) {
			for (k = 0; k < cruiseDataWithRowNums.size(); k++) {
				myLogger.debug("  data[" + Integer.toString(k) + "]=" + cruiseDataWithRowNums.get(k).toString());
			}
		}
		return cruiseDataWithRowNums;
	}

	@Override
	public DashboardDatasetData updateDataColumnSpecs(String pageUsername,
			DashboardDataset newSpecs) throws IllegalArgumentException {
		// Get the dashboard data store and current username, and validate that username
		if ( ! validateRequest(pageUsername) ) 
			throw new IllegalArgumentException("Invalid user request");

		// Retrieve all the current cruise data
		DashboardDatasetData cruiseData = configStore.getDataFileHandler()
						.getDatasetDataFromFiles(newSpecs.getDatasetId(), 0, -1);
		if ( ! cruiseData.isEditable() )
			throw new IllegalArgumentException(newSpecs.getDatasetId() + 
					" has been submitted for QC; data column types cannot be modified.");

		// Revise the cruise data column types and units 
		if ( newSpecs.getDataColTypes().size() != cruiseData.getDataColTypes().size() )
			throw new IllegalArgumentException("Unexpected number of data columns (" +
					newSpecs.getDataColTypes().size() + " instead of " + 
					cruiseData.getDataColTypes().size());
		cruiseData.setDataColTypes(newSpecs.getDataColTypes());

		// Run the SanityCheck on the updated cruise.
		// Assigns the data check status and the WOCE-3 and WOCE-4 data flags.
		configStore.getDashboardDatasetChecker().checkDataset(cruiseData);

		// Save and commit the updated cruise columns
		configStore.getDataFileHandler().saveDatasetInfoToFile(cruiseData, 
				"Data column types, units, and missing values for " + 
				cruiseData.getDatasetId() + " updated by " + username);
		// Update the user-specific data column names to types, units, and missing values 
		configStore.getUserFileHandler().updateUserDataColumnTypes(cruiseData, username);
		if ( ! username.equals(cruiseData.getOwner()) )
			configStore.getUserFileHandler().updateUserDataColumnTypes(cruiseData, cruiseData.getOwner());
		
		// Remove all but the first maximum-needed number of rows of cruise data 
		// to minimize the payload of the returned cruise data
		int numRows = cruiseData.getNumDataRows();
		if ( numRows > DashboardUtils.MAX_ROWS_PER_GRID_PAGE ) {
			cruiseData.getDataValues()
					  .subList(DashboardUtils.MAX_ROWS_PER_GRID_PAGE, numRows)
					  .clear();
			cruiseData.getRowNums()
					  .subList(DashboardUtils.MAX_ROWS_PER_GRID_PAGE, numRows)
					  .clear();
		}

		Logger.getLogger("DashboardServices").info("cruise data columns specs updated for " + 
				cruiseData.getDatasetId() + " by " + username);
		// Return the updated truncated cruise data for redisplay 
		// in the DataColumnSpecsPage
		return cruiseData;
	}

	@Override
	public void updateDataColumns(String pageUsername, 
			ArrayList<String> cruiseExpocodes) throws IllegalArgumentException {
		// Get the dashboard data store and current username, and validate that username
		if ( ! validateRequest(pageUsername) ) 
			throw new IllegalArgumentException("Invalid user request");

		DataFileHandler cruiseHandler = configStore.getDataFileHandler();
		UserFileHandler userHandler = configStore.getUserFileHandler();
		DatasetChecker cruiseChecker = configStore.getDashboardDatasetChecker();
		Logger dataSpecsLogger = Logger.getLogger("DashboardServices");

		for ( String expocode : cruiseExpocodes ) {
			// Retrieve all the current cruise data
			DashboardDatasetData cruiseData = cruiseHandler.getDatasetDataFromFiles(expocode, 0, -1);
			if ( ! cruiseData.isEditable() )
				throw new IllegalArgumentException("Dataset " + expocode + 
						" has been submitted for QC; data column types cannot be modified.");

			try {
				// Identify the columns from stored names-to-types for this user
				userHandler.assignDataColumnTypes(cruiseData);
				// Save and commit these column assignments in case the sanity checker has problems
				cruiseHandler.saveDatasetInfoToFile(cruiseData, "Column types for " + expocode + 
						" updated by " + username + " from post-processing a multiple-dataset upload");
			
				// Run the SanityCheck on the updated cruise.  Saves the SanityChecker messages,
				// and assigns the data check status and the WOCE-3 and WOCE-4 data flags.
				cruiseChecker.checkDataset(cruiseData);

				// Save and commit the updated cruise information
				cruiseHandler.saveDatasetInfoToFile(cruiseData, "Data status and WOCE flags for " + expocode + 
						" updated by " + username + " from post-processing a multiple-dataset upload");
				dataSpecsLogger.info("Updated data column specs for " + expocode + " for " + username);
			} catch (Exception ex) {
				// ignore problems (such as unidentified columns) - cruise will not have been updated
				dataSpecsLogger.error("Unable to update data column specs for " + expocode + ": " + ex.getMessage());
				continue;
			}
		}
	}

	@Override
	public SCMessageList getDataMessages(String pageUsername, String expocode) 
			throws IllegalArgumentException {
		// Get the dashboard data store and current username, and validate that username
		if ( ! validateRequest(pageUsername) ) 
			throw new IllegalArgumentException("Invalid user request");

		// Get the list of saved sanity checker Message objects for this cruise
		SCMessageList scMsgList;
		try {
			scMsgList = configStore.getCheckerMsgHandler().getCheckerMessages(expocode);
		} catch (FileNotFoundException ex) {
			throw new IllegalArgumentException("The sanity checker has never been run on cruise " + expocode);
		}
		scMsgList.setUsername(username);
		Logger.getLogger("DashboardServices")
			  .info("returned sanity checker messages for " + expocode + " for " + username);
		return scMsgList;
	}

	@Override
	public String getOmeXmlPath(String pageUsername, String activeExpocode, 
			String previousExpocode) throws IllegalArgumentException {
		// Get the dashboard data store and current username, and validate that username
		if ( ! validateRequest(pageUsername) ) 
			throw new IllegalArgumentException("Invalid user request");
		MetadataFileHandler metadataHandler = configStore.getMetadataFileHandler();

		if ( ! previousExpocode.isEmpty() ) {
			// Read the OME XML contents for previousExpocode 
			DashboardMetadata mdata = metadataHandler.getMetadataInfo(previousExpocode, DashboardUtils.OME_FILENAME);
			DashboardOmeMetadata updatedOmeMData = new DashboardOmeMetadata(mdata, metadataHandler);
			// Reset the dataset and related fields to that for activeExpocode 
			updatedOmeMData.changeDatasetID(activeExpocode);
			// Read the OME XML contents currently saved for activeExpocode
			mdata = metadataHandler.getMetadataInfo(activeExpocode, DashboardUtils.OME_FILENAME);
			DashboardOmeMetadata origOmeMData = new DashboardOmeMetadata(mdata, metadataHandler);
			// Create the merged OME and save the results
			DashboardOmeMetadata mergedOmeMData = origOmeMData.mergeModifiable(updatedOmeMData);
			metadataHandler.saveAsOmeXmlDoc(mergedOmeMData, "Merged OME of " + previousExpocode + 
															" into OME of " + activeExpocode);
		}

		// return the absolute path to the OME.xml for activeExpcode
		File omeFile = metadataHandler.getMetadataFile(activeExpocode, DashboardUtils.OME_FILENAME);
		return omeFile.getAbsolutePath();
	}

	@Override
	public boolean buildPreviewImages(String pageUsername, String expocode, 
			String timetag, boolean firstCall) throws IllegalArgumentException {
		// Get the dashboard data store and current username, and validate that username
		if ( ! validateRequest(pageUsername) ) 
			throw new IllegalArgumentException("Invalid user request");

		// Generate the preview plots for this cruise
		// TODO: refactor so starts this in a separate thread when firstCall is true and 
		//       returns false, then when gets called again with firstCall is false for
		//       a status update, returns false if still working and true if all plots are done
		if ( firstCall )
			configStore.getPreviewPlotsHandler().createPreviewPlots(expocode, timetag);
		return true;
	}

	@Override
	public void submitDatasetsForQC(String pageUsername, HashSet<String> cruiseExpocodes, 
			String archiveStatus, String localTimestamp, boolean repeatSend) 
					throws IllegalArgumentException {
		// Get the dashboard data store and current username, and validate that username
		if ( ! validateRequest(pageUsername) ) 
			throw new IllegalArgumentException("Invalid user request");

		// Submit the cruises for QC and possibly send to CDIAC
		configStore.getDashboardDatasetSubmitter().submitDatasets(cruiseExpocodes, 
				archiveStatus, localTimestamp, repeatSend, username);
		Logger.getLogger("DashboardServices").info("cruises " + cruiseExpocodes.toString() + 
				" submitted by " + username);
	}

}
