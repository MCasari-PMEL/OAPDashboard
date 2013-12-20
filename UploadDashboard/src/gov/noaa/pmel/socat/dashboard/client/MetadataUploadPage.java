/**
 * 
 */
package gov.noaa.pmel.socat.dashboard.client;

import gov.noaa.pmel.socat.dashboard.client.SocatUploadDashboard.PagesEnum;
import gov.noaa.pmel.socat.dashboard.shared.DashboardCruise;
import gov.noaa.pmel.socat.dashboard.shared.DashboardUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.TreeSet;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Metadata document upload page, for either a single cruise or multiple
 * cruises.  The introduction is modified to be appropriate for these
 * two cases.
 * 
 * @author Karl Smith
 */
public class MetadataUploadPage extends Composite {

	private static final String WELCOME_INTRO = "Logged in as: ";
	private static final String LOGOUT_TEXT = "Logout";
	private static final String UPLOAD_TEXT = "Upload";
	private static final String CANCEL_TEXT = "Cancel";

	private static final String MULTI_CRUISE_HTML_INTRO_PROLOGUE = 
			"Because multiple cruises were selected, you can only upload " +
			"metadata documents.  The uploaded metadata document will be " +
			"duplicated to uniquely associate a copy to each cruise selected, " +
			"either as a new document or replacing an existing document. " +
			"<br /><br />" +
			"In order to perform other operations with metadata documents " +
			"for a cruise, select only the one cruise on the main cruise " +
			"list page before showing the metadata manager. " +
			"<br /><br />" +
			"Upload documents for the cruises: <b>";
	private static final String SINGLE_CRUISE_HTML_INTRO_PROLOGUE = 
			"Upload documents for the cruise: <b>";
	private static final String CRUISE_HTML_INTRO_EPILOGUE = "</b>";

	private static final String NO_FILE_ERROR_MSG = 
			"Please select a metadata file to upload";

	private static final String OVERWRITE_WARNING_MSG_PROLOGUE = 
			"This will replace the metadata document for cruise ";
	private static final String OVERWRITE_WARNING_MSG_EPILOGUE =
			" Do you wish to proceed?";

	interface MetadataUploadPageUiBinder 
			extends UiBinder<Widget, MetadataUploadPage> {
	}

	private static MetadataUploadPageUiBinder uiBinder = 
			GWT.create(MetadataUploadPageUiBinder.class);

	@UiField Label userInfoLabel;
	@UiField Button logoutButton;
	@UiField HTML introHtml;
	@UiField FormPanel uploadForm;
	@UiField FileUpload metadataUpload;
	@UiField Hidden usernameToken;
	@UiField Hidden passhashToken;
	@UiField Hidden timestampToken;
	@UiField Hidden expocodesToken;
	@UiField Button uploadButton;
	@UiField Button cancelButton;

	private String username;
	private HashSet<DashboardCruise> cruises;
	private TreeSet<String> expocodes;

	// Singleton instance of this page
	private static MetadataUploadPage singleton = null;

	private MetadataUploadPage() {
		initWidget(uiBinder.createAndBindUi(this));
		username = "";
		cruises = new HashSet<DashboardCruise>();
		expocodes = new TreeSet<String>();

		logoutButton.setText(LOGOUT_TEXT);

		uploadForm.setEncoding(FormPanel.ENCODING_MULTIPART);
		uploadForm.setMethod(FormPanel.METHOD_POST);
		uploadForm.setAction(GWT.getModuleBaseURL() + "MetadataUploadService");

		uploadButton.setText(UPLOAD_TEXT);
		cancelButton.setText(CANCEL_TEXT);
	}

	/**
	 * Display the metadata upload page in the RootLayoutPanel
	 * for a set of given cruises.
	 * Adds this page to the page history.
	 * 
	 * @param cruises
	 * 		add/replace the metadata in these cruises 
	 */
	static void showPage(HashSet<DashboardCruise> cruises) {
		if ( singleton == null )
			singleton = new MetadataUploadPage();
		singleton.updateCruises(cruises);
		SocatUploadDashboard.get().updateCurrentPage(singleton);
		History.newItem(PagesEnum.METADATA_UPLOAD.name(), false);
	}

	/**
	 * Display the metadata upload page in the RootLayoutPanel
	 * for the given cruise.  Adds this page to the page history.
	 * 
	 * @param cruiseExpocode
	 * 		add/replace the metadata for this cruise
	 * @param mdataNames
	 * 		set of metadata document filenames currently
	 * 		associated with this cruise
	 */
	static void showPage(String cruiseExpocode, TreeSet<String> mdataNames) {
		// Create a DashboardCruise using the above information
		DashboardCruise cruise = new DashboardCruise();
		cruise.setExpocode(cruiseExpocode);
		cruise.setMetadataFilenames(mdataNames);
		// Create a set containing just this cruise
		HashSet<DashboardCruise> cruiseSet = new HashSet<DashboardCruise>();
		cruiseSet.add(cruise);
		// Show the page using this cruise set
		showPage(cruiseSet);
	}

	/**
	 * Redisplays the last version of this page if the username
	 * associated with this page matches the current login username.
	 * 
	 * @param addToHistory 
	 * 		if true, adds this page to the page history 
	 */
	static void redisplayPage(boolean addToHistory) {
		// If never show before, or if the username does not match the 
		// current login username, show the login page instead
		if ( (singleton == null) || 
			 ! singleton.username.equals(DashboardLoginPage.getUsername()) ) {
			DashboardLoginPage.showPage(true);
		}
		else {
			SocatUploadDashboard.get().updateCurrentPage(singleton);
			if ( addToHistory )
				History.newItem(PagesEnum.METADATA_UPLOAD.name(), false);
		}
	}

	/**
	 * Updates this page with the latest username from DashboardLoginPage
	 * and the cruises from the given set of cruises.  The introduction 
	 * is modified to reflect whether one or multiple cruises are given.
	 * 
	 * @param cruises
	 * 		associated the uploaded metadata document to these cruises
	 */
	private void updateCruises(HashSet<DashboardCruise> cruises) {
		// Update the current username
		username = DashboardLoginPage.getUsername();
		userInfoLabel.setText(WELCOME_INTRO + username);

		// Update the cruises associated with this page
		this.cruises.clear();
		this.cruises.addAll(cruises);
		expocodes.clear();
		for ( DashboardCruise cruz : cruises )
			expocodes.add(cruz.getExpocode());

		// Update the HTML intro naming the cruises
		StringBuilder sb = new StringBuilder();
		if ( expocodes.size() > 1 )
			sb.append(MULTI_CRUISE_HTML_INTRO_PROLOGUE);
		else
			sb.append(SINGLE_CRUISE_HTML_INTRO_PROLOGUE);
		boolean first = true;
		for ( String expo : expocodes ) {
			if ( first )
				first = false;
			else
				sb.append(",  ");
			sb.append(SafeHtmlUtils.htmlEscape(expo));
		}
		sb.append(CRUISE_HTML_INTRO_EPILOGUE);
		introHtml.setHTML(sb.toString());

		// Clear the hidden tokens just to be safe
		usernameToken.setValue("");
		passhashToken.setValue("");
		timestampToken.setValue("");
		expocodesToken.setValue("");
	}

	@UiHandler("logoutButton")
	void logoutOnClick(ClickEvent event) {
		DashboardLogoutPage.showPage();
	}

	@UiHandler("uploadButton") 
	void uploadButtonOnClick(ClickEvent event) {
		// Assign the "hidden" values
		usernameToken.setValue(DashboardLoginPage.getUsername());
		passhashToken.setValue(DashboardLoginPage.getPasshash());
		String localTimestamp = 
				DateTimeFormat.getFormat("yyyy-MM-dd HH:mm Z")
							  .format(new Date());
		timestampToken.setValue(localTimestamp);
		expocodesToken.setValue(
				DashboardUtils.encodeStringArrayList(
						new ArrayList<String>(expocodes)));
		// Submit the form
		uploadForm.submit();
	}

	@UiHandler("cancelButton")
	void cancelButtonOnClick(ClickEvent event) {
		if ( expocodes.size() == 1 ) {
			// Return to the metadata list page exactly as it was
			MetadataManagerPage.redisplayPage(true);
		}
		else {
			CruiseListPage.redisplayPage(true);
		}
	}

	@UiHandler("uploadForm")
	void uploadFormOnSubmit(SubmitEvent event) {
		// Make sure a file was selected
		String uploadFilename = metadataUpload.getFilename();
		if ( (uploadFilename == null) || uploadFilename.trim().isEmpty() ) {
			Window.alert(NO_FILE_ERROR_MSG);
			event.cancel();
			return;
		}
		// Warn about any overwrites that will happen
		for ( DashboardCruise cruz : cruises ) {
			String mdataName = DashboardUtils.metadataFilename(
										cruz.getExpocode(), uploadFilename);
			if ( cruz.getMetadataFilenames().contains(mdataName) ) {
				String warning = OVERWRITE_WARNING_MSG_PROLOGUE +
						cruz.getExpocode() + OVERWRITE_WARNING_MSG_EPILOGUE;
				if ( ! Window.confirm(warning) ) {
					event.cancel();
					return;
				}
			}
		}
	}

	@UiHandler("uploadForm")
	void uploadFormOnSubmitComplete(SubmitCompleteEvent event) {
		// Clear the "hidden" values
		usernameToken.setValue("");
		passhashToken.setValue("");
		timestampToken.setValue("");
		expocodesToken.setValue("");

		// Check the result returned
		String resultMsg = event.getResults();
		if ( resultMsg == null ) {
			Window.alert("Unexpected null result from submit complete");
			return;
		}

		String[] tagMsg = resultMsg.split("\n", 2);
		if ( tagMsg.length < 2 ) {
			// probably an error response; just display the entire message
			Window.alert(resultMsg);
		}
		else if ( DashboardUtils.FILE_CREATED_HEADER_TAG.equals(tagMsg[0]) ) {
			// cruise file(s) created or updated
			Window.alert(tagMsg[1]);
			if ( expocodes.size() == 1 ) {
				// return to the metadata manager, having it
				// request the updated cruise from the server 
				MetadataManagerPage.showPage(expocodes.first());
			}
			else {
				// return to the cruise list, having it request 
				// the updated cruises for the user from the server
				CruiseListPage.showPage(false);
			}
		}
		else {
			// Unknown response with a newline, just display the entire message
			Window.alert(resultMsg);
		}
	}

}
