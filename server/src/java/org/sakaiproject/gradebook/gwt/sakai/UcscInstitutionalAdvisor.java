package org.sakaiproject.gradebook.gwt.sakai;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdLengthException;
import org.sakaiproject.exception.IdUniquenessException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.gradebook.gwt.client.model.FinalGradeSubmissionResult;
import org.sakaiproject.gradebook.gwt.server.model.FinalGradeSubmissionResultImpl;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVWriter;

public class UcscInstitutionalAdvisor extends SampleInstitutionalAdvisor {
    private static Logger log = LoggerFactory.getLogger(UcscInstitutionalAdvisor.class);

    private static final String FILE_EXTENSION = ".csv";
    private static final String EMP_ID = "emplId";

    private ContentHostingService contentHostingService;
    private CourseManagementService courseManagementService;
    private SessionManager sessionManager;
    private SiteService siteService;
    private ToolManager toolManager;
    private UserDirectoryService userDirectoryService;

    private String finalGradeSubmissionPath;

    @Override
    public FinalGradeSubmissionResult submitFinalGrade(List<Map<Column, String>> studentDataList, String gradebookUid) {

        FinalGradeSubmissionResult finalGradeSubmissionResult = new FinalGradeSubmissionResultImpl();

        if (finalGradeSubmissionPath == null || "".equals(finalGradeSubmissionPath)) {
            log.error("ERROR: Null and or empty test failed for finalGradeSubmissionPath");
            finalGradeSubmissionResult.setStatus(500);
            return finalGradeSubmissionResult;
        }

        // Test if the path has a trailing file separator
        if (!finalGradeSubmissionPath.endsWith(File.separator)) {
            finalGradeSubmissionPath += File.separator;
        }

        // Getting the siteId
        String siteId = toolManager.getCurrentPlacement().getContext();
        Site site = null;
        try {
            site = siteService.getSite(siteId);
        } catch (IdUnusedException iue) {
            log.error("Site {} not found.", siteId);
            // 500 Internal Server Error
            finalGradeSubmissionResult.setStatus(500);
            return finalGradeSubmissionResult;
        }

        String csv = createGradeExportCSV(studentDataList, site);

        try {
            saveCSVToMyWorkspace(site, csv);
        } catch (Exception e) {
            log.warn("Cannot add gradebook export to myworkspace", e);
            finalGradeSubmissionResult.setStatus(500);
            return finalGradeSubmissionResult;
        }

        // all went well
        //finalGradeSubmissionResult.setStatus(201);
        return finalGradeSubmissionResult;
    }

    private String createGradeExportCSV(List<Map<Column, String>> studentDataList, Site site) {

        StringWriter writer = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(writer);

        String[] header = new String[6];
        header[0] = "id";
        header[1] = "Student Id";
        header[2] = "Student Name";
        header[3] = "Section";
        header[4] = "Grade";
        header[5] = "Calculated Grade";
        csvWriter.writeNext(header);

        for (Map<Column, String> studentData : studentDataList) {
            String[] entries = new String[6];
            entries[0] = getUserEmpId(studentData.get(Column.STUDENT_UID)); // User Property ID
            entries[1] = studentData.get(Column.FINAL_GRADE_USER_ID);
            entries[2] = studentData.get(Column.STUDENT_NAME);
            entries[3] = studentData.get(Column.EXPORT_CM_ID);
            entries[4] = studentData.get(Column.LETTER_GRADE);
            entries[5] = studentData.get(Column.RAW_GRADE);
            csvWriter.writeNext(entries);
        }

        try {
            csvWriter.close();
        } catch (IOException ioe) {
            log.warn("closing CSV writer", ioe);
        }

        return writer.toString();
    }

    private void saveCSVToMyWorkspace(Site site, String csv) throws IdLengthException, PermissionException, OverQuotaException, InconsistentException, ServerOverloadException, IdUniquenessException, IdInvalidException {
        String contentType = "text/csv";
        String name = calculateFileName(site);
        String userId = sessionManager.getCurrentSessionUserId();
        String collectionId = "/user/" + userId + "/" + finalGradeSubmissionPath;

        try {
            ContentCollectionEdit collectionEdit = contentHostingService.addCollection(collectionId);
            ResourcePropertiesEdit collectionProps = contentHostingService.newResourceProperties();
            collectionProps.addProperty(ResourceProperties.PROP_DISPLAY_NAME, finalGradeSubmissionPath);
            collectionEdit.getPropertiesEdit().addAll(collectionProps);
            contentHostingService.commitCollection(collectionEdit);
        } catch (IdUsedException e) {
            log.debug("Collection {} already exists", collectionId);
        }

        try {
            ContentResourceEdit resourceEdit = contentHostingService.addResource(collectionId, name, FILE_EXTENSION, 10);
            resourceEdit.setContent(csv.getBytes());
            ResourcePropertiesEdit resourceProps = contentHostingService.newResourceProperties();
            resourceProps.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
            resourceProps.addProperty(ResourceProperties.PROP_DESCRIPTION, "Grades for course/sections " + name);
            resourceProps.addProperty(ResourceProperties.PROP_PUBVIEW, "false");
            resourceEdit.getPropertiesEdit().addAll(resourceProps);
            contentHostingService.commitResource(resourceEdit);
        } catch (IdUnusedException iue) {
            log.warn("Collection {} doesn't exist, this shouldn't happen", collectionId, iue);
        }
    }

    private String getUserEmpId(String eid) {
        if (StringUtils.isBlank(eid)) { return ""; }

        String empId = null;
        try {
            User user = userDirectoryService.getUser(eid);
            empId = user.getProperties().getProperty(EMP_ID);
        } catch (UserNotDefinedException e) {
            log.warn("Can't find user with emplId of {} and property name of: {}", eid, EMP_ID, e);
        }

        return StringUtils.defaultString(empId);
    }

    private List<Section> getSectionList(Site site) {
        List<Section> sections = new ArrayList<Section>();
        for (Iterator<Group> iter = site.getGroups().iterator(); iter.hasNext(); ) {
            Group group = iter.next();
            String providerId = group.getProviderGroupId();

            Section officialSection = null;
            try {
                officialSection = courseManagementService.getSection(providerId);
                sections.add(officialSection);
            } catch (IdNotFoundException ide) {
                log.error("Site {} has a provider id, {}, that has no matching section in CM.", site.getId(), providerId);
            }
        }

        sections.addAll(addSections(site, "site.cm.requested"));
        sections.addAll(addSections(site, "site-request-course-sections"));
        return sections;
    }

    private List<Section> addSections(Site site, String propName) {
        List<Section> sections = new ArrayList<Section>();
        String courseListString = StringUtils.trimToNull(site.getProperties().getProperty(propName));

        if (StringUtils.isNotBlank(courseListString)) {
            List<String> courseList = new ArrayList<String>();
            if (courseListString.indexOf("+") != -1) {
                courseList.addAll(Arrays.asList(courseListString.split("\\+")));
            } else {
                courseList.add(courseListString);
            }

            // need to construct the list of SectionObjects
            for (String course : courseList) {
                try {
                    Section s = courseManagementService.getSection(course);
                    if (s != null) {
                        sections.add(s);
                    }
                } catch (Exception e) {
                    log.warn("Cannot find section {}", course, e);
                }
            }
        }
        return sections;
    }

    private String calculateFileName(Site site) {
        List<Section> sections = getSectionList(site);
        log.debug("Found {} sections", sections.size());

        Set<String> sNames = new HashSet<String>();

        // find an LEC section
        for (Section section : sections) {
            if (section != null) {
                String title = section.getTitle();
                if (StringUtils.contains(title, "LEC")) {
                    if (title.contains(",")) {
                        String[] tokens = title.split(",");
                        title = tokens[0];
                    }
                    sNames.add(title.toUpperCase());
                }
            }
        }

        StringBuilder name = new StringBuilder();
        for (String sName : sNames) {
            if (name.length() > 0) {
                name.append(",");
            }
            name.append(sName);
        }

        if (StringUtils.isBlank(name.toString())) {
            name.append(site.getTitle());
            log.warn("Using site title {} for grades export filename", site.getTitle());
        }

        String fName = name.toString();
        // replace all non words with an "_"
        fName = fName.replaceAll("\\W", "_");

        // replace consecutive underscores to just one
        fName = fName.replaceAll("(_)\\1+", "_");

        // in case file name is greater than 250 + ".csv"
        if (fName.length() > 250) {
            fName = fName.substring(0, 250);
        }

        log.debug("Filename for grades export is {}", fName);

        return fName;
    }

    @Override
    public void setToolManager(ToolManager toolManager) {
        super.setToolManager(toolManager);
        this.toolManager = toolManager;
    }

    @Override
    public void setSiteService(SiteService siteService) {
        super.setSiteService(siteService);
        this.siteService = siteService;
    }

    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    public void setContentHostingService(ContentHostingService contentHostingService) {
        this.contentHostingService = contentHostingService;
    }

    public void setCourseManagementService(CourseManagementService courseManagementService) {
        this.courseManagementService = courseManagementService;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void setFinalGradeSubmissionPath(String finalGradeSubmissionPath) {
        this.finalGradeSubmissionPath = finalGradeSubmissionPath;
    }

}
