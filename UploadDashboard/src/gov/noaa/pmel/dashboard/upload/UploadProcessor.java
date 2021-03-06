/**
 * 
 */
package gov.noaa.pmel.dashboard.upload;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tomcat.util.http.fileupload.FileItem;

import gov.noaa.pmel.dashboard.handlers.RawUploadFileHandler;
import gov.noaa.pmel.dashboard.server.DashboardConfigStore;
import gov.noaa.pmel.dashboard.shared.FileType;
import gov.noaa.pmel.tws.util.Logging;
import scratch.FileTypeTest;

/**
 * @author kamb
 *
 */
public class UploadProcessor {

    private static Logger logger = Logging.getLogger(UploadProcessor.class);
    
    private StandardUploadFields _stdFields;
    private FileUploadProcessor _processor;
    
    public static String checkFileType(File file) {
        try {
            TikaConfig tfig = new TikaConfig(UploadProcessor.class.getResource("/config/tika-config.xml"), 
                                             UploadProcessor.class.getClassLoader());
            Tika tika = new Tika(tfig);
            String type = tika.detect(file);
            return type;
        } catch (Exception ex) {
            return "tika/error";
        }
    }

    /**
     * @param stdFields
     */
    public UploadProcessor(StandardUploadFields stdFields) {
        _stdFields = stdFields;
    }
    public void processUpload() throws UploadProcessingException {
        List<FileItem> datafiles = _stdFields.dataFiles();
        for ( FileItem item : datafiles ) {
            
            String uploadName = item.getName();
            String uploadContentType = item.getContentType();
            _stdFields.uploadFileName(uploadName);
            _stdFields.uploadType(uploadContentType);
            
            logger.info("Processing uploaded file " + uploadName + " of type " + uploadContentType);
            // save raw upload file
            File rawFile = null;
            try {
                rawFile = saveRawFile(item);
                item.delete();
            } catch (Exception ex) {
                throw new UploadProcessingException("Unable to save uploaded file.", ex);
            }
            
            try {
                String checkedFileType = FileTypeTest.getFileType(rawFile);
                _stdFields.checkedFileType(checkedFileType);
                _stdFields.fileType(FileTypeTest.fileIsDelimited(checkedFileType) ? FileType.DELIMITED : FileType.OTHER);
                _processor = getUploadFileProcessor(rawFile, _stdFields);
                _processor.processUpload(rawFile);
            } catch (Exception ex) {
                throw new UploadProcessingException(ex);
            }
        }
    }
            
    private static FileUploadProcessor getUploadFileProcessor(File uploadedFile, StandardUploadFields stdFields) {
        FileUploadProcessor uploadProcessor = FileUploadProcessor.getProcessor(uploadedFile, stdFields);
        return uploadProcessor;
    }

    protected File saveRawFile(FileItem item) throws Exception {
        RawUploadFileHandler rfh = DashboardConfigStore.get(false).getRawUploadFileHandler();
        File itemFile = rfh.writeFileItem(item, _stdFields.username());
        return itemFile;
    }
    
    /**
     * @return
     */
    public List<String> getMessages() {
        return _processor.getMessages();
    }
    /**
     * @return
     */
    public Set<String> getSuccesses() {
        return _processor.getSuccesses();
    }
}
