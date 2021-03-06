/**
 * 
 */
package gov.noaa.pmel.dashboard.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Represents an uploaded dataset and its current status.
 * 
 * @author Karl Smith
 */
public class DashboardDataset implements Serializable, IsSerializable {

	private static final long serialVersionUID = 5005454171404329101L;

	protected boolean selected;
	protected String version;
	protected String owner;
    protected String featureType;
    protected String fileType;
	protected String datasetId;
	protected String dataCheckStatus;
	protected String mdTimestamp;
	protected String mdStatus;
	protected TreeSet<String> addlDocs;
	protected String submitStatus;
	protected String archiveStatus;
	protected String archiveSubmissionMessage;
	protected String archiveDate;
    protected boolean archiveDOIrequested;
	protected String uploadFilename;
	protected String uploadTimestamp;
    protected String uploadedFile;
	protected String doi;
	protected int numDataRows;
	protected int numErrorRows;
	protected int numWarnRows;
	protected ArrayList<String> userColNames;
	// For each data column, a DataColumnType with type, unit, and missing value
	protected ArrayList<DataColumnType> dataColTypes;
	// Checker-generated QC flags without comments
	protected TreeSet<QCFlag> checkerFlags;
	// PI-provided QC flags without comments
	protected TreeSet<QCFlag> userFlags;

	/**
	 * Create an empty dashboard dataset
	 */
	public DashboardDataset() {
		selected = false;
		version = DashboardUtils.STRING_MISSING_VALUE;
		owner = DashboardUtils.STRING_MISSING_VALUE;
		datasetId = DashboardUtils.STRING_MISSING_VALUE;
		dataCheckStatus = DashboardUtils.CHECK_STATUS_NOT_CHECKED;
		mdTimestamp = DashboardUtils.STRING_MISSING_VALUE;
		addlDocs = new TreeSet<String>();
		submitStatus = DashboardUtils.STATUS_NOT_SUBMITTED;
		archiveStatus = DashboardUtils.ARCHIVE_STATUS_NOT_SUBMITTED;
        archiveSubmissionMessage = DashboardUtils.STRING_MISSING_VALUE;
		archiveDate = DashboardUtils.STRING_MISSING_VALUE;
		uploadFilename = DashboardUtils.STRING_MISSING_VALUE;
		uploadTimestamp = DashboardUtils.STRING_MISSING_VALUE;
		uploadedFile = DashboardUtils.STRING_MISSING_VALUE;
		doi = DashboardUtils.STRING_MISSING_VALUE;
		numDataRows = 0;
		numErrorRows = 0;
		numWarnRows = 0;
		userColNames = new ArrayList<String>();
		dataColTypes = new ArrayList<DataColumnType>();
		checkerFlags = new TreeSet<QCFlag>();
		userFlags = new TreeSet<QCFlag>();
	}

    
	public boolean isArchived() {
	    return getArchiveStatus().startsWith("Submitted");
	}
	/**
	 * @return
	 * 		Boolean.TRUE if the dataset is suspended, excluded, or not submitted; 
	 * 		Boolean.FALSE if the dataset is submitted or acceptable but not published;
	 * 		null if the dataset is (acceptable and) published.
	 */
	public Boolean isEditable() { // XXX needs clarification / update
		// true for datasets that are not submitted, suspended, or excluded
		String status = getSubmitStatus();
		if ( status.equals(DashboardUtils.STATUS_NOT_SUBMITTED) || 
			 status.equals(DashboardUtils.STATUS_SUSPENDED) ||
			 status.equals(DashboardUtils.STATUS_EXCLUDED)  ) 
			return Boolean.TRUE;
		// false for submitted or acceptable unpublished datasets
		status = getArchiveStatus();
		if ( status.equals(DashboardUtils.ARCHIVE_STATUS_NOT_SUBMITTED) ||
			 status.equals(DashboardUtils.ARCHIVE_STATUS_WITH_NEXT_RELEASE) ||
			 status.equals(DashboardUtils.ARCHIVE_STATUS_SENT_FOR_ARCHIVAL) ||
			 status.equals(DashboardUtils.ARCHIVE_STATUS_OWNER_TO_ARCHIVE) ) 
			return Boolean.FALSE;
        if ( status.equals(DashboardUtils.ARCHIVE_STATUS_FAILED)) {
            return Boolean.TRUE;
        }
		// null for acceptable published datasets
		return null;
	}

	/**
	 * @return
	 * 		if the dataset is selected
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * @param selected
	 * 		set if the dataset is selected
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	/**
	 * @return 
	 * 		the dataset version; 
	 * 		never null, but may be {@link DashboardUtils#STRING_MISSING_VALUE}
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version 
	 * 		the dataset version (after trimming) to set;
	 * 		if null, sets to {@link DashboardUtils#STRING_MISSING_VALUE}
	 */
	public void setVersion(String version) {
		if ( version == null )
			this.version = DashboardUtils.STRING_MISSING_VALUE;
		else
			this.version = version.trim();
	}

	/**
	 * @return 
	 * 		the owner for this dataset; 
	 * 		never null but may be {@link DashboardUtils#STRING_MISSING_VALUE}
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * @param owner 
	 * 		the dataset owner (after trimming) to set;
	 * 		if null, sets to {@link DashboardUtils#STRING_MISSING_VALUE}
	 */
	public void setOwner(String owner) {
		if ( owner == null )
			this.owner = DashboardUtils.STRING_MISSING_VALUE;
		else
			this.owner = owner.trim();
	}

	public String getFeatureTypeName() {
        return featureType;
    }

	public FeatureType getFeatureType() {
        return FeatureType.valueOf(featureType);
    }
    
    public void setFeatureType(String featureType) {
        this.featureType = featureType;
    }

	public String getFileTypeName() {
        return fileType;
    }

	public FileType getFileType() {
        return FileType.valueOf(fileType);
    }
    
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    /**
	 * @return 
	 * 		the dataset ID; 
	 * 		never null but may be {@link DashboardUtils#STRING_MISSING_VALUE}
	 */
	public String getDatasetId() {
		return datasetId;
	}

	/**
	 * @param datasetId 
	 * 		the dataset ID to set;
	 * 		if null, sets to {@link DashboardUtils#STRING_MISSING_VALUE}
	 */
	public void setDatasetId(String datasetId) {
		if ( datasetId == null )
			this.datasetId = DashboardUtils.STRING_MISSING_VALUE;
		else
			this.datasetId = datasetId;
	}

	/**
	 * @return 
	 * 		the data check status; never null
	 */
	public String getDataCheckStatus() {
		return dataCheckStatus;
	}

	/**
	 * @param dataCheckStatus 
	 * 		the data check status to set;
	 * 		if null, sets to {@link DashboardUtils#CHECK_STATUS_NOT_CHECKED}
	 */
	public void setDataCheckStatus(String dataCheckStatus) {
		if ( dataCheckStatus == null )
			this.dataCheckStatus = DashboardUtils.CHECK_STATUS_NOT_CHECKED;
		else
			this.dataCheckStatus = dataCheckStatus;
	}

	/**
	 * @return 
	 * 		the OME metadata timestamp; 
	 * 		never null but may be {@link DashboardUtils#STRING_MISSING_VALUE}
	 */
	public String getMdTimestamp() {
		return mdTimestamp;
	}

	/**
	 * @param omeTimestamp 
	 * 		the OME metadata timestamp to set;
	 * 		if null, sets to {@link DashboardUtils#STRING_MISSING_VALUE}
	 */
	public void setMdTimestamp(String omeTimestamp) {
		if ( omeTimestamp == null )
			this.mdTimestamp = DashboardUtils.STRING_MISSING_VALUE;
		else
			this.mdTimestamp = omeTimestamp;
	}

	public String getMdStatus() {
		return mdStatus != null ? mdStatus : DashboardUtils.STRING_MISSING_VALUE;
	}

	public void setMdStatus(String status) {
		if ( status == null )
			this.mdStatus = DashboardUtils.STRING_MISSING_VALUE;
		else
			this.mdStatus = status;
	}

	/**
	 * @return 
	 * 		the additional document "filename; timestamp" strings 
	 * 		associated with this dataset; never null but may be empty.  
	 * 		The actual set of strings in this object is returned.
	 */
	public TreeSet<String> getAddlDocs() {
		return addlDocs;
	}

	/**
	 * @param addlDocs
	 * 		the set of additional document "filename; timestamp" strings
	 * 		for this dataset.  The set in this object is cleared and all 
	 * 		the contents of the given set, if not null, are added. 
	 */
	public void setAddlDocs(Collection<String> addlDocs) {
		this.addlDocs.clear();
		if ( addlDocs != null )
			this.addlDocs.addAll(addlDocs);
	}

	/**
	 * @return 
	 * 		the QC submission status; 
	 * 		never null but may be {@link #STATUS_NOT_SUBMITTED} if not assigned
	 */
	public String getSubmitStatus() {
		return submitStatus;
	}

	/**
	 * @param submitStatus 
	 * 		the  QC submission status (after trimming) to set;
	 * 		if null, {@link #QC_STATUS_NOT_SUBMITTED} is assigned
	 */
	public void setSubmitStatus(String qcStatus) {
		if ( qcStatus == null )
			this.submitStatus = DashboardUtils.STATUS_NOT_SUBMITTED;
		else
			this.submitStatus = qcStatus.trim();
	}

	/**
	 * @return 
	 * 		the archive submission status; 
	 * 		never null but may be {@link DashboardUtils#ARCHIVE_STATUS_NOT_SUBMITTED} if not assigned
	 */
	public String getArchiveStatus() {
		return archiveStatus;
	}

	/**
	 * @param submitStatus 
	 * 		the archive submission status (after trimming) to set;
	 * 		if null, {@link DashboardUtils#ARCHIVE_STATUS_NOT_SUBMITTED} is assigned
	 */
	public void setArchiveStatus(String archiveStatus) {
		if ( archiveStatus == null )
			this.archiveStatus = DashboardUtils.ARCHIVE_STATUS_NOT_SUBMITTED;
		else
			this.archiveStatus = archiveStatus.trim();
	}
    
	/**
	 * @return 
	 * 		the archive submission message, if any; 
	 * 		never null but may be empty string if not entered
	 */
	public String getArchiveSubmissionMessage() {
		return archiveSubmissionMessage;
	}

	/**
	 * @param submitStatus 
	 * 		the archive submission status (after trimming) to set;
	 * 		if null, {@link DashboardUtils#ARCHIVE_STATUS_NOT_SUBMITTED} is assigned
	 */
	public void setArchiveSubmissionMessage(String submissionMessage) {
		if ( submissionMessage == null )
			this.archiveSubmissionMessage = "";
		else
			this.archiveSubmissionMessage = submissionMessage.trim();
	}
	
    public boolean getArchiveDOIrequested() {
        return archiveDOIrequested;
    }

    public void setArchiveDOIrequested(boolean doiRequested) {
        archiveDOIrequested = doiRequested;
    }
    
	/**
	 * @return 
	 * 		the archive submission date; 
	 * 		never null but may be {@link DashboardUtils#STRING_MISSING_VALUE}
	 */
	public String getArchiveDate() {
		return archiveDate;
	}

	/**
	 * @param archiveDate 
	 * 		the archive submission date (after trimming) to set;
	 * 		if null, sets to {@link DashboardUtils#STRING_MISSING_VALUE}
	 */
	public void setArchiveDate(String archiveDate) {
		if ( archiveDate == null )
			this.archiveDate = DashboardUtils.STRING_MISSING_VALUE;
		else
			this.archiveDate = archiveDate.trim();
	}

	/**
	 * @return 
	 * 		the uploaded data filename; 
	 * 		never null but may be {@link DashboardUtils#STRING_MISSING_VALUE}
	 */
	public String getUploadFilename() {
		return uploadFilename;
	}

	/**
	 * @param uploadFilename 
	 * 		the uploaded data filename (after trimming) to set;
	 * 		if null, sets to {@link DashboardUtils#STRING_MISSING_VALUE}
	 */
	public void setUploadFilename(String uploadFilename) {
		if ( uploadFilename == null )
			this.uploadFilename = DashboardUtils.STRING_MISSING_VALUE;
		else
			this.uploadFilename = uploadFilename.trim();
	}

	/**
	 * @return 
	 * 		the locally saved original uploaded data file; 
	 * 		never null but may be {@link DashboardUtils#STRING_MISSING_VALUE}
	 */
	public String getUploadedFile() {
		return uploadedFile;
	}

	/**
	 * @param uploadFilename 
	 * 		the uploaded data filename (after trimming) to set;
	 * 		if null, sets to {@link DashboardUtils#STRING_MISSING_VALUE}
	 */
	public void setUploadedFile(String uploadedFile) {
		if ( uploadedFile == null )
			this.uploadedFile = DashboardUtils.STRING_MISSING_VALUE;
		else
			this.uploadedFile = uploadedFile.trim();
	}

	/**
	 * @return 
	 * 		the uploaded data timestamp; 
	 * 		never null but may be {@link DashboardUtils#STRING_MISSING_VALUE}
	 */
	public String getUploadTimestamp() {
		return uploadTimestamp;
	}

	/**
	 * @param uploadTimestamp 
	 * 		the uploaded data timestamp (after trimming) to set;
	 * 		if null, sets to {@link DashboardUtils#STRING_MISSING_VALUE}
	 */
	public void setUploadTimestamp(String uploadTimestamp) {
		if ( uploadTimestamp == null )
			this.uploadTimestamp = DashboardUtils.STRING_MISSING_VALUE;
		else
			this.uploadTimestamp = uploadTimestamp.trim();
	}

	/**
	 * @return 
	 * 		the DOI of the original data document;
	 * 		never null but may be {@link DashboardUtils#STRING_MISSING_VALUE}
	 */
	public String getDoi() {
		return doi;
	}

	/**
	 * @param doi
	 * 		the DOI (after trimming) of the original data document to set;
	 * 		if null, sets to {@link DashboardUtils#STRING_MISSING_VALUE}
	 */
	public void setDoi(String doi) {
		if ( doi == null )
			this.doi = DashboardUtils.STRING_MISSING_VALUE;
		else
			this.doi = doi.trim();
	}

	/**
	 * @return 
	 * 		the total number of data measurements (data rows) 
	 * 		for the dataset
	 */
	public int getNumDataRows() {
    	return numDataRows;
	}

	/**
	 * @param numDataRows 
	 * 		the total number of data measurements (data rows) 
	 * 		to set for the dataset 
XXX This allows the possibility that numDataRows != the actual number of data rows.
     */
	public void setNumDataRows(int numDataRows) {
		this.numDataRows = numDataRows;
	}

	/**
	 * @return 
	 * 		the number of data rows with error messages from the sanity checker
	 */
	public int getNumErrorRows() {
		return numErrorRows;
	}

	/**
	 * @param numErrorRows 
	 * 		the number of data rows with error messages from the sanity checker to set
	 */
	public void setNumErrorRows(int numErrorRows) {
		this.numErrorRows = numErrorRows;
	}

	/**
	 * @return 
	 * 		the number of data rows with warning messages from the sanity checker
	 */
	public int getNumWarnRows() {
		return numWarnRows;
	}

	/**
	 * @param numWarnRows 
	 * 		the number of data rows with warning messages from the sanity checker to set
	 */
	public void setNumWarnRows(int numWarnRows) {
		this.numWarnRows = numWarnRows;
	}

	/**
	 * @return the userColNames
	 * 		the list of data column header names as they appeared in 
	 * 		the original user-provided data file for this dataset; 
	 * 		never null but may be empty.  The actual list in this 
	 * 		object is returned. 
	 */
	public ArrayList<String> getUserColNames() {
		return userColNames;
	}

	/**
	 * @param userColNames 
	 * 		the list of data column header names as they appeared in 
	 * 		the original user-provided data file for this dataset.  The 
	 * 		list in this object is cleared and all the contents of the  
	 * 		given list, if not null, are added. 
	 */
	public void setUserColNames(ArrayList<String> userColNames) {
		this.userColNames.clear();
		if ( userColNames != null )
			this.userColNames.addAll(userColNames);
	}

	/**
	 * @return 
	 * 		the list of data column types for this dataset; may be empty 
	 * 		but never null.  The actual list in this object is returned.
	 */
	public ArrayList<DataColumnType> getDataColTypes() {
		return dataColTypes;
	}

	/**
	 * @param dataColTypes 
	 * 		the list of data column types for this dataset.  The list in 
	 * 		this object is cleared and all the contents of the given list, 
	 * 		if not null, are added. Note that this is a shallow copy; 
	 * 		the given DataColumnType objects are reused. 
	 */
	public void setDataColTypes(ArrayList<DataColumnType> dataColTypes) {
		this.dataColTypes.clear();
		if ( dataColTypes != null )
			this.dataColTypes.addAll(dataColTypes);
	}

	/**
	 * @return 
	 * 		the set of automated data checker QC flags <b>without comments</b>; 
	 * 		never null but may be empty.  The actual set in this object is returned.
	 */
	public TreeSet<QCFlag> getCheckerFlags() {
		return checkerFlags;
	}

	/**
	 * @param checkerFlags 
	 * 		the set of automated data checker QC flags <b>without comments</b> 
	 * 		to assign.  The set in this object is cleared and all the contents 
	 * 		of the given collection, if not null, are added.  Note that this 
	 * 		is a shallow copy; the given QCFlag objects are reused. 
	 */
	public void setCheckerFlags(Collection<QCFlag> checkerFlags) {
		this.checkerFlags.clear();
		if ( checkerFlags != null )
			this.checkerFlags.addAll(checkerFlags);
	}

	/**
	 * @return
	 * 		The set of user-provided QC flags <b>without comments</b>;
	 * 		never null but may be empty.  The actual set in this object is returned.
	 */
	public TreeSet<QCFlag> getUserFlags() {
		return userFlags;
	}

	/**
	 * @param userFlags
	 * 		The set user-provided QC flags <b>without comments</b> to assign.  
	 * 		The set in this object is cleared and all the contents of the given 
	 * 		Collection, if not null, are added.  Note that this is a shallow copy; 
	 * 		the given QCFlag objects are reused. 
	 */
	public void setUserFlags(Collection<QCFlag> userFlags) {
		this.userFlags.clear();
		if ( userFlags != null )
			this.userFlags.addAll(userFlags);
	}

	@Override
	public int hashCode() {
		final int prime = 37;
		int result = Boolean.valueOf(selected).hashCode();
		result = result * prime + version.hashCode();
		result = result * prime + owner.hashCode();
		result = result * prime + datasetId.hashCode();
		result = result * prime + dataCheckStatus.hashCode();
		result = result * prime + mdTimestamp.hashCode();
		result = result * prime + addlDocs.hashCode();
		result = result * prime + submitStatus.hashCode();
		result = result * prime + archiveStatus.hashCode();
		result = result * prime + archiveDate.hashCode();
		result = result * prime + uploadFilename.hashCode();
		result = result * prime + uploadTimestamp.hashCode();
		result = result * prime + doi.hashCode();
		result = result * prime + Integer.hashCode(numDataRows);
		result = result * prime + Integer.hashCode(numErrorRows);
		result = result * prime + Integer.hashCode(numWarnRows);
		result = result * prime + userColNames.hashCode();
		result = result * prime + dataColTypes.hashCode();
		result = result * prime + checkerFlags.hashCode();
		result = result * prime + userFlags.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj )
			return true;
		if ( obj == null )
			return false;

		if ( ! (obj instanceof DashboardDataset) )
			return false;
		DashboardDataset other = (DashboardDataset) obj;

		if ( selected != other.selected )
			return false;
		if ( ! version.equals(other.version) ) 
			return false;
		if ( ! owner.equals(other.owner) )
			return false;
		if ( ! datasetId.equals(other.datasetId) )
			return false;
		if ( ! dataCheckStatus.equals(other.dataCheckStatus) )
			return false;
		if ( ! mdTimestamp.equals(other.mdTimestamp) )
			return false;
		if ( ! addlDocs.equals(other.addlDocs) )
			return false;
		if ( ! submitStatus.equals(other.submitStatus) )
			return false;
		if ( ! archiveStatus.equals(other.archiveStatus) )
			return false;
		if ( ! archiveDate.equals(other.archiveDate) )
			return false;
		if ( ! uploadFilename.equals(other.uploadFilename) )
			return false;
		if ( ! uploadTimestamp.equals(other.uploadTimestamp) )
			return false;
		if ( ! doi.equals(other.doi) )
			return false;
		if ( numDataRows != other.numDataRows )
			return false;
		if ( numErrorRows != other.numErrorRows )
			return false;
		if ( numWarnRows != other.numWarnRows )
			return false;
		if ( ! userColNames.equals(other.userColNames) )
			return false;
		if ( ! dataColTypes.equals(other.dataColTypes) )
			return false;
		if ( ! checkerFlags.equals(other.checkerFlags) ) 
			return false;
		if ( ! userFlags.equals(other.userFlags) ) 
			return false;
		return true;
	}

	public String fullDetaiString() {
		return "DashboardDataset" +
				"[ selected=" + Boolean.toString(selected) + 
				",\n    version = " + version +
				",\n    owner=" + owner + 
				",\n    datasetId=" + datasetId + 
				",\n    dataCheckStatus=" + dataCheckStatus +
				",\n    omeTimestamp=" + mdTimestamp + 
				",\n    addlDocs=" + addlDocs.toString() +
				",\n    submitStatus=" + submitStatus + 
				",\n    archiveStatus=" + archiveStatus + 
				",\n    archiveDate=" + archiveDate + 
				",\n    uploadFilename=" + uploadFilename +
				",\n    uploadTimestamp=" + uploadTimestamp +
				",\n    doi=" + doi +
				",\n    numDataRows=" + Integer.toString(numDataRows) +
				",\n    numErrorRows=" + Integer.toString(numErrorRows) +
				",\n    numWarnRows=" + Integer.toString(numWarnRows) +
				",\n    userColNames=" + userColNames.toString() +
				",\n    dataColTypes=" + dataColTypes.toString() +
				";\n    checkerFlags = " + checkerFlags.toString() +
				";\n    userFlags = " + userFlags.toString() +
				" ]";
	}
	
	@Override
	public String toString() {
	    return "datasetId:"+datasetId;
	}

	/**
	 * Compare using the "selected" properties of the datasets.
	 * Note that this is inconsistent with DashboardDataset.equals
	 * in that this is only examining one field of DashboardDataset.
	 */
	public static Comparator<DashboardDataset> selectedComparator =
			new Comparator<DashboardDataset>() {
		@Override
		public int compare(DashboardDataset d1, DashboardDataset d2) {
			if ( d1 == d2 )
				return 0;
			if ( d1 == null )
				return -1;
			if ( d2 == null )
				return 1;
			Boolean s1 = d1.isSelected();
			return s1.compareTo(d2.isSelected());
		}
	};

	/**
	 * Compare using the owners of the datasets.
	 * Note that this is inconsistent with DashboardDataset.equals 
	 * in that this is only examining one field of DashboardDataset.
	 */
	public static Comparator<DashboardDataset> ownerComparator =
			new Comparator<DashboardDataset>() {
		@Override
		public int compare(DashboardDataset d1, DashboardDataset d2) {
			if ( d1 == d2 )
				return 0;
			if ( d1 == null )
				return -1;
			if ( d2 == null )
				return 1;
			return d1.getOwner().compareTo(d2.getOwner());
		}
	};

	/**
	 * Compare using the IDs of the datasets.
	 * Note that this is inconsistent with DashboardDataset.equals 
	 * in that this is only examining one field of DashboardDataset.
	 */
	public static Comparator<DashboardDataset> datasetIdComparator = 
			new Comparator<DashboardDataset>() {
		@Override
		public int compare(DashboardDataset d1, DashboardDataset d2) {
			if ( d1 == d2 )
				return 0;
			if ( d1 == null )
				return -1;
			if ( d2 == null )
				return 1;
			return d1.getDatasetId().compareTo(d2.getDatasetId());
		}
	};

	/**
	 * Compare using the upload timestamp strings of the datasets.
	 * Note that this is inconsistent with DashboardDataset.equals 
	 * in that this is only examining one field of DashboardDataset.
	 */
	public static Comparator<DashboardDataset> timestampComparator = 
			new Comparator<DashboardDataset>() {
		@Override
		public int compare(DashboardDataset d1, DashboardDataset d2) {
			if ( d1 == d2 )
				return 0;
			if ( d1 == null )
				return -1;
			if ( d2 == null )
				return 1;
			return d1.getUploadTimestamp().compareTo(d2.getUploadTimestamp());
		}
	};

	public static Comparator<DashboardDataset> featureTypeComparator = 
			new Comparator<DashboardDataset>() {
		@Override
		public int compare(DashboardDataset d1, DashboardDataset d2) {
			if ( d1 == d2 )
				return 0;
			if ( d1 == null )
				return -1;
			if ( d2 == null )
				return 1;
			return d1.getFeatureTypeName().compareTo(d2.getFeatureTypeName());
		}
	};

	/**
	 * Compare using the data check status strings of the datasets.
	 * Note that this is inconsistent with DashboardDataset.equals 
	 * in that this is only examining one field of DashboardDataset.
	 */
	public static Comparator<DashboardDataset> dataCheckComparator = 
			new Comparator<DashboardDataset>() {
		@Override
		public int compare(DashboardDataset d1, DashboardDataset d2) {
			if ( d1 == d2 )
				return 0;
			if ( d1 == null )
				return -1;
			if ( d2 == null )
				return 1;
			return d1.getDataCheckStatus().compareTo(d2.getDataCheckStatus());
		}
	};

	/**
	 * Compare using the OME metadata timestamp strings of the datasets.
	 * Note that this is inconsistent with DashboardDataset.equals 
	 * in that this is only examining one field of DashboardDataset.
	 */
	public static Comparator<DashboardDataset> mdTimestampComparator = 
			new Comparator<DashboardDataset>() {
		@Override
		public int compare(DashboardDataset d1, DashboardDataset d2) {
			if ( d1 == d2 )
				return 0;
			if ( d1 == null )
				return -1;
			if ( d2 == null )
				return 1;
			return d1.getMdTimestamp().compareTo(d2.getMdTimestamp());
		}
	};

	/**
	 * Compare using the additional document "filename; timestamp" 
	 * strings of the datasets.  Note that this is inconsistent with 
	 * DashboardDataset.equals in that this is only examining one 
	 * field of DashboardDataset.
	 */
	public static Comparator<DashboardDataset> addlDocsComparator = 
			new Comparator<DashboardDataset>() {
		@Override
		public int compare(DashboardDataset d1, DashboardDataset d2) {
			if ( d1 == d2 )
				return 0;
			if ( d1 == null )
				return -1;
			if ( d2 == null )
				return 1;
			Iterator<String> iter1 = d1.getAddlDocs().iterator();
			Iterator<String> iter2 = d2.getAddlDocs().iterator();
			while ( iter1.hasNext() && iter2.hasNext() ) {
				int result = iter1.next().compareTo(iter2.next());
				if ( result != 0 )
					return result;
			}
			// The lists are the same up to the minimum number of strings given,
			// so the list with more items is larger; or they are equal if they
			// both have no more items
			if ( iter1.hasNext() )
				return 1;
			if ( iter2.hasNext() )
				return -1;
			return 0;
		}
	};

	/**
	 * Compare using the version strings of the datasets.
	 * Note that this is inconsistent with DashboardDataset.equals 
	 * in that this is only examining one field of DashboardDataset.
	 */
	public static Comparator<DashboardDataset> versionComparator = 
			new Comparator<DashboardDataset>() {
		@Override
		public int compare(DashboardDataset d1, DashboardDataset d2) {
			if ( d1 == d2 )
				return 0;
			if ( d1 == null )
				return -1;
			if ( d2 == null )
				return 1;
			return d1.getVersion().compareTo(d2.getVersion());
		}
	};

	/**
	 * Compare using the QC status strings of the datasets.
	 * Note that this is inconsistent with DashboardDataset.equals 
	 * in that this is only examining one field of DashboardDataset.
	 */
	public static Comparator<DashboardDataset> qcStatusComparator = 
			new Comparator<DashboardDataset>() {
		@Override
		public int compare(DashboardDataset d1, DashboardDataset d2) {
			if ( d1 == d2 )
				return 0;
			if ( d1 == null )
				return -1;
			if ( d2 == null )
				return 1;
			return d1.getSubmitStatus().compareTo(d2.getSubmitStatus());
		}
	};

	/**
	 * Compare using the archive status strings of the datasets.
	 * Note that this is inconsistent with DashboardDataset.equals 
	 * in that this is only examining one field of DashboardDataset.
	 */
	public static Comparator<DashboardDataset> archiveStatusComparator = 
			new Comparator<DashboardDataset>() {
		@Override
		public int compare(DashboardDataset d1, DashboardDataset d2) {
			if ( d1 == d2 )
				return 0;
			if ( d1 == null )
				return -1;
			if ( d2 == null )
				return 1;
			return d1.getArchiveStatus().compareTo(d2.getArchiveStatus());
		}
	};

	/**
	 * Compare using the upload filenames of the datasets.
	 * Note that this is inconsistent with DashboardDataset.equals 
	 * in that this is only examining one field of DashboardDataset.
	 */
	public static Comparator<DashboardDataset> filenameComparator = 
			new Comparator<DashboardDataset>() {
		@Override
		public int compare(DashboardDataset d1, DashboardDataset d2) {
			if ( d1 == d2 )
				return 0;
			if ( d1 == null )
				return -1;
			if ( d2 == null )
				return 1;
			return d1.getUploadFilename().compareTo(d2.getUploadFilename());
		}
	};

}
