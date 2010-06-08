/**********************************************************************************
 *
 * $Id:$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009 The Regents of the University of California
 *
 * Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 * 
 * http://www.osedu.org/licenses/ECL-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.gradebook.gwt.server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;
import org.sakaiproject.gradebook.gwt.client.AppConstants;
import org.sakaiproject.gradebook.gwt.client.exceptions.FatalException;
import org.sakaiproject.gradebook.gwt.client.exceptions.InvalidInputException;
import org.sakaiproject.gradebook.gwt.client.gxt.ItemModelProcessor;
import org.sakaiproject.gradebook.gwt.client.gxt.upload.NewImportHeader;
import org.sakaiproject.gradebook.gwt.client.gxt.upload.NewImportHeader.Field;
import org.sakaiproject.gradebook.gwt.client.model.Gradebook;
import org.sakaiproject.gradebook.gwt.client.model.Item;
import org.sakaiproject.gradebook.gwt.client.model.Learner;
import org.sakaiproject.gradebook.gwt.client.model.Roster;
import org.sakaiproject.gradebook.gwt.client.model.Upload;
import org.sakaiproject.gradebook.gwt.client.model.key.LearnerKey;
import org.sakaiproject.gradebook.gwt.client.model.type.CategoryType;
import org.sakaiproject.gradebook.gwt.client.model.type.GradeType;
import org.sakaiproject.gradebook.gwt.client.model.type.ItemType;
import org.sakaiproject.gradebook.gwt.sakai.Gradebook2ComponentService;
import org.sakaiproject.gradebook.gwt.sakai.GradebookImportException;
import org.sakaiproject.gradebook.gwt.sakai.GradebookToolService;
import org.sakaiproject.gradebook.gwt.sakai.model.GradeItem;
import org.sakaiproject.gradebook.gwt.sakai.model.UserDereference;
import org.sakaiproject.gradebook.gwt.server.exceptions.ImportFormatException;
import org.sakaiproject.gradebook.gwt.server.model.GradeItemImpl;
import org.sakaiproject.gradebook.gwt.server.model.LearnerImpl;
import org.sakaiproject.gradebook.gwt.server.model.UploadImpl;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.gradebook.Assignment;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class NewImportExportUtility {

	private static final Log log = LogFactory.getLog(NewImportExportUtility.class);
	
	private final static int RAWFIELD_FIRST_POSITION = 0; 
	private final static int RAWFIELD_SECOND_POSITION = 1; 

	private static final String SCANTRON_HEADER_STUDENT_ID = "student_id"; 
	private static final String SCANTRON_HEADER_SCORE = "score"; 
	private static ResourceBundle i18n = ResourceBundle.getBundle("org.sakaiproject.gradebook.gwt.client.I18nConstants");

	public static String[] scantronIgnoreColumns = 
		{ "last name", "first name", "initial" };
	public static String[] idColumns = 
		{ "student id", "identifier", "userId", "learnerid", "id" };
	public static String[] nameColumns =
		{ "student name", "name", "learner" };
	
	public static enum Delimiter {
		TAB, COMMA, SPACE, COLON
	};
	
	public static enum OptionState { NULL, TRUE, FALSE}; 

	private static enum StructureRow {
		GRADEBOOK("Gradebook:"),  SCALED_EC("Scaled XC:"), SHOWCOURSEGRADES("ShowCourseGrades:"), SHOWRELEASEDITEMS("ShowReleasedItems:"),
		SHOWITEMSTATS("ShowItemStats:"), SHOWMEAN("ShowMean:"), SHOWMEDIAN("ShowMedian:"), SHOWMODE("ShowMode:"), SHOWRANK("ShowRank:"),  
		CATEGORY("Category:"), PERCENT_GRADE("% Grade:"), POINTS("Points:"), 
		PERCENT_CATEGORY("% Category:"), DROP_LOWEST("Drop Lowest:"), EQUAL_WEIGHT("Equal Weight Items:");

		private String displayName;

		StructureRow(String displayName) {
			this.displayName = displayName;
		}

		public String getDisplayName() {
			return displayName;
		}
	};
	
	private Set<String> headerRowIndicatorSet, idSet, nameSet, scantronIgnoreSet;
	
	public NewImportExportUtility() {	
		// FIXME - Need to decide whether this should be institutional based.  
		// FIXME - does this need i18n ? 
		this.headerRowIndicatorSet = new HashSet<String>();
		this.nameSet = new HashSet<String>();
		for (int i=0;i<nameColumns.length;i++) {
			nameSet.add(nameColumns[i].toLowerCase());
			headerRowIndicatorSet.add(nameColumns[i].toLowerCase());
		}
		this.idSet = new HashSet<String>();
		for (int i=0;i<idColumns.length;i++) {
			idSet.add(idColumns[i].toLowerCase());
			headerRowIndicatorSet.add(idColumns[i].toLowerCase());
		}
		this.scantronIgnoreSet = new HashSet<String>();
		for (int i=0;i<scantronIgnoreColumns.length;i++) {
			scantronIgnoreSet.add(scantronIgnoreColumns[i].toLowerCase());
		}
	}

	public void exportGradebook(Gradebook2ComponentService service, String gradebookUid, 
			final boolean includeStructure, final boolean includeComments, PrintWriter writer, 
			HttpServletResponse response, String fileType) 
	throws FatalException {

		Gradebook gradebook = service.getGradebook(gradebookUid);
		Item gradebookItemModel = gradebook.getGradebookItemModel();
		NewRawFile out = new NewRawFile(); 

		Long gradebookId = gradebook.getGradebookId();
		final List<String> headerIds = new ArrayList<String>();

		final List<String> headerColumns = new LinkedList<String>();

		headerColumns.add("Student Id");
		headerColumns.add("Student Name");

		GradeType gradeType = gradebookItemModel.getGradeType();
		
		if (includeStructure) {
			CategoryType categoryType = gradebookItemModel.getCategoryType();
			String categoryTypeText = getDisplayName(categoryType);
			String gradeTypeText = getDisplayName(gradebookItemModel.getGradeType());

			// First, we need to add a row for basic gradebook info
			String[] gradebookInfoRow = { "", StructureRow.GRADEBOOK.getDisplayName(), gradebookItemModel.getName(), categoryTypeText, gradeTypeText};
			out.addRow(gradebookInfoRow);

			exportViewOptionsAndScaleEC(out, gradebook); 


			final List<String> categoriesRow = new LinkedList<String>();
			final List<String> percentageGradeRow = new LinkedList<String>();
			final List<String> pointsRow = new LinkedList<String>();
			final List<String> percentCategoryRow = new LinkedList<String>();
			final List<String> dropLowestRow = new LinkedList<String>();
			final List<String> equalWeightRow = new LinkedList<String>();


			categoriesRow.add("");
			categoriesRow.add(StructureRow.CATEGORY.getDisplayName());

			percentageGradeRow.add("");
			percentageGradeRow.add(StructureRow.PERCENT_GRADE.getDisplayName());

			pointsRow.add("");
			pointsRow.add(StructureRow.POINTS.getDisplayName());

			percentCategoryRow.add("");
			percentCategoryRow.add(StructureRow.PERCENT_CATEGORY.getDisplayName());

			dropLowestRow.add("");
			dropLowestRow.add(StructureRow.DROP_LOWEST.getDisplayName());

			equalWeightRow.add("");
			equalWeightRow.add(StructureRow.EQUAL_WEIGHT.getDisplayName());

			ItemModelProcessor processor = new ItemModelProcessor(gradebookItemModel) {

				@Override
				public void doCategory(Item itemModel, int childIndex) {
					StringBuilder categoryName = new StringBuilder().append(itemModel.getName());

					if (Util.checkBoolean(itemModel.getExtraCredit())) {
						categoryName.append(AppConstants.EXTRA_CREDIT_INDICATOR);
					}

					if (!Util.checkBoolean(itemModel.getIncluded())) {
						categoryName.append(AppConstants.UNINCLUDED_INDICATOR);
					}

					categoriesRow.add(categoryName.toString());
					percentageGradeRow.add(new StringBuilder()
					.append(String.valueOf(itemModel.getPercentCourseGrade()))
					.append("%").toString());
					Integer dropLowest = itemModel.getDropLowest();
					if (dropLowest == null)
						dropLowestRow.add("");
					else
						dropLowestRow.add(String.valueOf(dropLowest));
					Boolean isEqualWeight = itemModel.getEqualWeightAssignments();
					if (isEqualWeight == null)
						equalWeightRow.add("");
					else
						equalWeightRow.add(String.valueOf(isEqualWeight));


					if (((GradeItem)itemModel).getChildCount() == 0) {
						headerIds.add(AppConstants.EXPORT_SKIPCOLUMN_INDICATOR);
						headerColumns.add("");
						pointsRow.add("");
						percentCategoryRow.add("");
					}

				}

				@Override
				public void doItem(Item itemModel, int childIndex) {
					if (childIndex > 0) {
						categoriesRow.add("");
						percentageGradeRow.add("");
						dropLowestRow.add("");
						equalWeightRow.add("");
					} 

					if (includeComments) {
						categoriesRow.add("");
						percentageGradeRow.add("");
						dropLowestRow.add("");
						equalWeightRow.add("");
					}

					StringBuilder text = new StringBuilder();
					text.append(itemModel.getName());

					if (Util.checkBoolean(itemModel.getExtraCredit())) {
						text.append(AppConstants.EXTRA_CREDIT_INDICATOR);
					}

					if (!Util.checkBoolean(itemModel.getIncluded())) {
						text.append(AppConstants.UNINCLUDED_INDICATOR);
					}

					if (!includeStructure) {
						String points = DecimalFormat.getInstance().format(itemModel.getPoints());
						text.append(" [").append(points).append("]");
					}

					headerIds.add(itemModel.getIdentifier());
					headerColumns.add(text.toString());

					if (itemModel.getPoints() == null)
						pointsRow.add("");
					else
						pointsRow.add(String.valueOf(itemModel.getPoints()));
					
					percentCategoryRow.add(new StringBuilder()
					.append(String.valueOf(itemModel.getPercentCategory()))
					.append("%").toString());

					if (includeComments) {
						StringBuilder commentsText = new StringBuilder();
						commentsText.append(AppConstants.COMMENTS_INDICATOR).append(itemModel.getName());
						headerColumns.add(commentsText.toString());
						pointsRow.add("");
						percentCategoryRow.add("");
					}
				}

			};

			processor.process();

			switch (categoryType) {
				case NO_CATEGORIES:
					out.addRow(pointsRow.toArray(new String[pointsRow.size()]));
					break;
				case SIMPLE_CATEGORIES:
					out.addRow(categoriesRow.toArray(new String[categoriesRow.size()]));
					out.addRow(dropLowestRow.toArray(new String[dropLowestRow.size()]));
					out.addRow(pointsRow.toArray(new String[pointsRow.size()]));

					break;
				case WEIGHTED_CATEGORIES:					
					out.addRow(categoriesRow.toArray(new String[categoriesRow.size()]));
					out.addRow(percentageGradeRow.toArray(new String[percentageGradeRow.size()]));
					out.addRow(dropLowestRow.toArray(new String[dropLowestRow.size()]));
					out.addRow(equalWeightRow.toArray(new String[equalWeightRow.size()]));
					out.addRow(pointsRow.toArray(new String[pointsRow.size()]));
					out.addRow(percentCategoryRow.toArray(new String[percentCategoryRow.size()]));

					break;
			}

			String[] blankRow = { "" };
			out.addRow(blankRow);
		} else {

			ItemModelProcessor processor = new ItemModelProcessor(gradebookItemModel) {

				@Override
				public void doItem(Item itemModel) {
					StringBuilder text = new StringBuilder();
					text.append(itemModel.getName());

					if (Util.checkBoolean(itemModel.getExtraCredit())) {
						text.append(AppConstants.EXTRA_CREDIT_INDICATOR);
					}

					if (!Util.checkBoolean(itemModel.getIncluded())) {
						text.append(AppConstants.UNINCLUDED_INDICATOR);
					}

					if (!includeStructure) {
						String points = DecimalFormat.getInstance().format(itemModel.getPoints());
						text.append(" [").append(points).append("]");
					}

					headerIds.add(itemModel.getIdentifier());
					headerColumns.add(text.toString());

					if (includeComments) {
						StringBuilder commentsText = new StringBuilder();
						commentsText.append(AppConstants.COMMENTS_INDICATOR).append(itemModel.getName());
						headerColumns.add(commentsText.toString());
					}
				}

			};

			processor.process();

		}

		headerColumns.add("Letter Grade");
		
		if (gradeType != GradeType.LETTERS)
			headerColumns.add("Calculated Grade");

		out.addRow(headerColumns.toArray(new String[headerColumns.size()]));

		Roster result = service.getRoster(gradebookUid, gradebookId, null, null, null, null, null, true, false);

		List<Learner> rows = result.getLearnerPage();

		if (headerIds != null) {

			if (rows != null) {
				for (Learner row : rows) {
					List<String> dataColumns = new LinkedList<String>();
					dataColumns.add((String)row.get(LearnerKey.S_EXPRT_USR_ID.name()));
					dataColumns.add((String)row.get(LearnerKey.S_LST_NM_FRST.name()));

					for (int column = 0; column < headerIds.size(); column++) {
						String columnIndex = headerIds.get(column);
						
						if (columnIndex != null) {
							if (columnIndex.equals(AppConstants.EXPORT_SKIPCOLUMN_INDICATOR)) {
								dataColumns.add("");
								continue;
							}
							
							Object value = row.get(columnIndex);

							if (value != null)
								dataColumns.add(String.valueOf(value));
							else
								dataColumns.add("");

						} else {
							dataColumns.add("");
						}

						if (includeComments) {
							String commentId = Util.buildCommentTextKey(headerIds.get(column)); 

							Object comment = row.get(commentId);

							if (comment == null)
								comment = "";

							dataColumns.add(String.valueOf(comment));
						}
					}

					dataColumns.add((String)row.get(LearnerKey.S_LTR_GRD.name()));
					
					if (gradeType != GradeType.LETTERS)
						dataColumns.add((String)row.get(LearnerKey.S_CALC_GRD.name()));

					out.addRow(dataColumns.toArray(new String[dataColumns.size()]));
				}
			} 

		}

		StringBuilder filename = new StringBuilder();
		Site site = service.getSite();
		
		if (site == null)
			filename.append("gradebook");
		else {
			String name = site.getTitle();
			name = name.replaceAll("\\s", "");

			filename.append(name);
		}

		service.postEvent("gradebook2.export", String.valueOf(gradebookId));
		
		if (fileType.equals("xls97"))
		{
			filename.append(".xls");

			if (response != null) {
				response.setContentType("application/ms-excel");
				response.setHeader("Content-Disposition", "attachment; filename=" + filename.toString());
			}
			createXLS97File(filename.toString(), response, out); 

		}
		else if (fileType.equals("csv"))
		{
			filename.append(".csv");

			if (response != null) {
				response.setContentType("application/ms-excel");
				response.setHeader("Content-Disposition", "attachment; filename=" + filename.toString());
			}
			try {
				createCSVFile(response, out);
			} catch (IOException e) {
				log.error("Caught I/O exception ", e); 
				throw new FatalException(e); 
			}			
		}

	}
	
	private void exportViewOptionsAndScaleEC(NewRawFile out, Gradebook gradebook) {
		
		Item firstGBItem = gradebook.getGradebookItemModel(); 
		if (firstGBItem.getExtraCreditScaled().booleanValue())
		{
			outputStructureTwoPartExportRow(StructureRow.SCALED_EC.getDisplayName(), "true", out); 
		}
		
		if (firstGBItem.getReleaseGrades().booleanValue())
		{
			outputStructureTwoPartExportRow(StructureRow.SHOWCOURSEGRADES.getDisplayName(), "true", out); 		
		}

		if (firstGBItem.getReleaseItems().booleanValue())
		{
			outputStructureTwoPartExportRow(StructureRow.SHOWRELEASEDITEMS.getDisplayName(), "true", out); 		
		}

		if (firstGBItem.getShowItemStatistics().booleanValue())
		{
			outputStructureTwoPartExportRow(StructureRow.SHOWITEMSTATS.getDisplayName(), "true", out); 
		}

		if (firstGBItem.getShowMean().booleanValue())
		{
			outputStructureTwoPartExportRow(StructureRow.SHOWMEAN.getDisplayName(), "true", out); 
		}

		if (firstGBItem.getShowMedian().booleanValue())
		{
			outputStructureTwoPartExportRow(StructureRow.SHOWMEDIAN.getDisplayName(), "true", out); 
		}

		if (firstGBItem.getShowMode().booleanValue())
		{
			outputStructureTwoPartExportRow(StructureRow.SHOWMODE.getDisplayName(), "true", out); 
		}

		if (firstGBItem.getShowRank().booleanValue())
		{
			outputStructureTwoPartExportRow(StructureRow.SHOWRANK.getDisplayName(), "true", out); 
		}		
	}

	private void outputStructureTwoPartExportRow(String optionName, String optionValue, NewRawFile out)
	{
		String[] rowString; 
		rowString = new String[3]; 
		rowString[0] = ""; 
		rowString[1] = optionName;
		rowString[2] = optionValue;
		out.addRow(rowString); 
	}

	private void createXLS97File(String title, HttpServletResponse response, NewRawFile out) throws FatalException {
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet s = wb.createSheet(title);
		
		out.startReading(); 
		String[] curRow = null; 
		int row = 0; 
		
		HSSFRow r = null;
		while ( (curRow = out.readNext()) != null) {
			r = s.createRow(row); 

			for (int i = 0; i < curRow.length ; i++) {
				HSSFCell cl = r.createCell(i);
				cl.setCellType(HSSFCell.CELL_TYPE_STRING); 
				cl.setCellValue(new HSSFRichTextString(curRow[i])); 
			}
			
			row++; 
		}
		
		// Run autosize on last row's columns
		if (r != null) {
			for (int i = 0; i <= r.getLastCellNum() ; i++) {
				s.autoSizeColumn((short) i);
			}
		}
 		writeXLSResponse(wb, response); 
 		
	}


	private void writeXLSResponse(HSSFWorkbook wb, HttpServletResponse response) throws FatalException {
		try {
			wb.write(response.getOutputStream());
			response.getOutputStream().flush();
			response.getOutputStream().close(); 
		} catch (IOException e) {
			log.error("Caught exception " + e, e); 
			throw new FatalException(e); 

		}		
	}

	private void createCSVFile(HttpServletResponse response,
			 NewRawFile out) throws IOException {
		
		
		CSVWriter csvWriter = new CSVWriter(response.getWriter());
		out.startReading(); 
		String[] curRow; 
		while ((curRow = out.readNext()) != null)
		{
			csvWriter.writeNext(curRow); 
		}
		try {
			csvWriter.close();
			response.getWriter().flush();
			response.getWriter().close(); 
		} catch (IOException e) {
			log.error("Caught ioexception: ", e);
		} 
	}


	private HSSFWorkbook readPoiSpreadsheet(BufferedInputStream is) throws IOException 
	{
		HSSFWorkbook ret = null; 
		HSSFWorkbook inspread = null;

		is.mark(1024*1024*512); 
		try {
			inspread = new HSSFWorkbook(POIFSFileSystem.createNonClosingInputStream(is));
			log.debug("here!"); 
		} 
		catch (IOException e) 
		{
			log.debug("Caught I/O Exception", e);
			ret = null; 
		} 
		catch (IllegalArgumentException iae)
		{
			log.debug("Caught IllegalArgumentException Exception", iae);
			ret = null; 
		}
		if (ret == null)
		{
			is.reset(); 
		}

		return inspread; 

	}


	private boolean checkForCurrentAssignmentInGradebook(String fileName, Gradebook2ComponentService service, GradebookToolService gbToolService, String gradebookUid)
	{
		Gradebook gm = service.getGradebook(gradebookUid); 
		List<Assignment> assignments = gbToolService.getAssignments(gm.getGradebookId()); 
		for (Assignment curAssignment : assignments)
		{
			String curAssignmentName = curAssignment.getName(); 
			log.debug("curAssignmentName=" + curAssignmentName);
			if (curAssignment.getName().equals(fileName))
			{
				return true; 
			}
		}

		return false; 
	}

	private String getUniqueFileNameForFileName(String fileName,
			Gradebook2ComponentService service, GradebookToolService gbToolService, String gradebookUid) throws GradebookImportException {

		log.debug("fileName=" + fileName);
		if (fileName == null || fileName.equals(""))
		{
			log.debug("null filename, returning default"); 
			return "Scantron Import"; 
		}
		
		int i = 1;
		String curFileName = fileName; 
		while (true)
		{
			log.debug("curFileName: " + curFileName); 
			if (!checkForCurrentAssignmentInGradebook(curFileName, service, gbToolService, gradebookUid))
			{
				log.debug("returning curFileName"); 
				return curFileName; 
			}
			else
			{
				curFileName = fileName + "-" +i; 
			}
			i++; 

			if (i > 1000)
			{
				throw new GradebookImportException("Couldn't find a unique filename within 1000 tries, please rename filename manually and import again"); 
			}
		}
	}

	/*
	 * so basically, we'll do: 
	 * 1) Scan the sheet for scantron artifacts, and if so convert to a simple CSV file which is 
	 */
	public Upload parseImportXLS(Gradebook2ComponentService service, 
			String gradebookUid, InputStream is, String fileName, GradebookToolService gbToolService, 
			boolean doPreventOverwrite) throws InvalidInputException, FatalException, IOException {
		log.debug("parseImportXLS() called"); 

		// Strip off extension
		fileName = removeFileExenstion(fileName);

		String realFileName = fileName; 
		boolean isOriginalName; 
		
		try {
			realFileName = getUniqueFileNameForFileName(fileName, service, gbToolService, gradebookUid);
		} catch (GradebookImportException e) {
			Upload importFile = new UploadImpl(); 
			importFile.setErrors(true); 
			importFile.setNotes(e.getMessage()); 
			return importFile; 
		} 
		isOriginalName = realFileName.equals(fileName);
		
		log.debug("realFileName=" + realFileName);
		log.debug("isOriginalName=" + isOriginalName);

		HSSFWorkbook inspread = null;

		BufferedInputStream bufStream = new BufferedInputStream(is); 

		inspread = readPoiSpreadsheet(bufStream);

		if (inspread != null)
		{
			log.debug("Found a POI readable spreadsheet");
			bufStream.close(); 
			return handlePoiSpreadSheet(inspread, service, gradebookUid, realFileName, isOriginalName);
		}
		else
		{
			log.debug("POI couldn't handle the spreadsheet, using jexcelapi");
			return handleJExcelAPISpreadSheet(bufStream, service, gradebookUid, realFileName, isOriginalName); 
		}

	}

	private String removeFileExenstion(String fileName) {
		if (fileName != null) {
			int indexOfExtension = fileName.lastIndexOf('.');
			if (indexOfExtension != -1 && indexOfExtension < fileName.length()) {
				fileName = fileName.substring(0, indexOfExtension);
			}
		}
		return fileName; 
	}

	private Upload handleJExcelAPISpreadSheet(BufferedInputStream is,
			Gradebook2ComponentService service, String gradebookUid, String fileName, boolean isNewAssignmentByFileName) throws InvalidInputException, FatalException, IOException {
		Workbook wb = null; 
		try {
			wb = Workbook.getWorkbook(is);
		} catch (BiffException e) {
			log.error("Caught a biff exception from JExcelAPI: " + e.getLocalizedMessage(), e); 
			return null; 
		} catch (IOException e) {
			log.error("Caught an IO exception from JExcelAPI: " + e.getLocalizedMessage(), e); 
			return null; 
		} 

		is.close();
		Sheet s = wb.getSheet(0); 
		if (s != null)
		{
			if (isScantronSheetForJExcelApi(s))
			{
				return handleScantronSheetForJExcelApi(s, service, gradebookUid, fileName, isNewAssignmentByFileName);
			}
			else
			{
				return handleNormalXLSSheetForJExcelApi(s, service, gradebookUid);
			}
		}
		else
		{
			return null;
		}
	}

	private Upload handleNormalXLSSheetForJExcelApi(Sheet s,
			Gradebook2ComponentService service, String gradebookUid) throws InvalidInputException, FatalException {
		NewRawFile raw = new NewRawFile(); 
		int numRows; 

		numRows = s.getRows(); 

		for (int i = 0; i < numRows; i++)
		{
			Cell[] row = null; 
			String[] data = null; 

			row = s.getRow(i);

			data = new String[row.length]; 
			for (int j = 0; j < row.length ; j++)
			{
				data[j] = row[j].getContents(); 
			}
			raw.addRow(data); 
		}
		raw.setFileType("Excel 5.0/7.0 Non Scantron"); 
		raw.setScantronFile(false); 

		return parseImportGeneric(service, gradebookUid, raw);
	}

	private Upload handleScantronSheetForJExcelApi(Sheet s,
			Gradebook2ComponentService service, String gradebookUid, String fileName, boolean isNewAssignmentByFileName) throws InvalidInputException, FatalException 
			{
		StringBuilder err = new StringBuilder("Scantron File with errors"); 
		NewRawFile raw = new NewRawFile(); 
		boolean stop = false; 

		Cell studentIdHeader = s.findCell(SCANTRON_HEADER_STUDENT_ID);
		Cell scoreHeader = s.findCell(SCANTRON_HEADER_SCORE);

		if (studentIdHeader == null)
		{
			err.append("There is no column with the header student_id");
			stop = true; 
		}

		if (scoreHeader == null)
		{
			err.append("There is no column with the header score");
			stop = true; 

		}

		if (! stop) 
		{
			raw.addRow(getScantronHeaderRow(fileName)); 
			for (int i = 0 ; i < s.getRows() ; i++)
			{
				Cell idCell; 
				Cell scoreCell; 

				idCell = s.getCell(studentIdHeader.getColumn(), i);
				scoreCell = s.getCell(scoreHeader.getColumn(), i); 

				if (!idCell.getContents().equals(studentIdHeader.getContents()))
				{
					String[] item = new String[2]; 
					item[RAWFIELD_FIRST_POSITION] = idCell.getContents(); 
					item[RAWFIELD_SECOND_POSITION] = scoreCell.getContents(); 
					raw.addRow(item); 
					item = null; 
				}
			}
			raw.setFileType("Scantron File"); 
			raw.setScantronFile(true);
			raw.setNewAssignment(isNewAssignmentByFileName);
			return parseImportGeneric(service, gradebookUid, raw);
		}
		else
		{
			raw.setMessages(err.toString());
			err = null; 
			raw.setErrorsFound(true); 

			return parseImportGeneric(service, gradebookUid, raw);
		}

			}

	private String[] getScantronHeaderRow(String fileName)
	{
		String[] header = new String[2]; 
		header[RAWFIELD_FIRST_POSITION] = "Student Id"; 
		if (null != fileName && !"".equals(fileName))
		{
			header[RAWFIELD_SECOND_POSITION] = fileName; 
		}
		else
		{
			header[RAWFIELD_SECOND_POSITION] = "Scantron Item"; 
		}
		return header; 
	}
	private boolean isScantronSheetForJExcelApi(Sheet s) {
		Cell studentIdHeader = s.findCell(SCANTRON_HEADER_STUDENT_ID);
		Cell scoreHeader = s.findCell("score");

		return (studentIdHeader != null && scoreHeader != null); 
	}

	private Upload handlePoiSpreadSheet(HSSFWorkbook inspread, Gradebook2ComponentService service, String gradebookUid, String fileName, boolean isNewAssignmentByFileName) throws InvalidInputException, FatalException
	{
		log.debug("handlePoiSpreadSheet() called"); 
		// FIXME - need to do multiple sheets, and structure
		int numSheets = inspread.getNumberOfSheets();  
		if (numSheets > 0)
		{
			HSSFSheet cur = inspread.getSheetAt(0);
			NewRawFile ret; 
			if (isScantronSheetFromPoi(cur))
			{
				log.debug("POI: Scantron");
				ret = processScantronXls(cur, fileName); 
				ret.setScantronFile(true); 
				ret.setNewAssignment(isNewAssignmentByFileName);
			}
			else
			{
				log.debug("POI: Not scantron");
				ret = processNormalXls(cur); 
			}

			return parseImportGeneric(service, gradebookUid, ret);
		}
		else
		{
			NewRawFile d = new NewRawFile(); 
			d.setMessages("The XLS spreadsheet entered does not contain any valid sheets.  Please correct and try again.");
			d.setErrorsFound(true); 
			return parseImportGeneric(service, gradebookUid, d);

		}
	}

	private NewRawFile processNormalXls(HSSFSheet s) {
		log.debug("processNormalXls() called");
		NewRawFile data = new NewRawFile();
		int numCols = getNumberOfColumnsFromSheet(s); 
		Iterator<Row> rowIter = s.rowIterator(); 
		boolean headerFound = false;
		int id_col = -1; 
		while (rowIter.hasNext())
		{

			Row curRow = rowIter.next();  
			if (!headerFound)
			{
				id_col = readHeaderRow(curRow); 
				headerFound = true; 
				log.debug("Header Row # is " + id_col);
			}
			String[] dataEntity = new String[numCols]; 

			log.debug("numCols = " + numCols); 

			for (int i = 0; i < numCols; i++) {
				org.apache.poi.ss.usermodel.Cell cl = curRow.getCell(i);
				String cellData;
				if (i == id_col && null != cl) {
					if (cl.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
						cellData = String.format("%.0f", cl
								.getNumericCellValue());
						log.debug("#1:cellData=" + cellData);
					} else {
						cellData = new HSSFDataFormatter().formatCellValue(cl);
						log.debug("#2:cellData=" + cellData);

					}
				} else {

					cellData = new HSSFDataFormatter().formatCellValue(cl);
					log.debug("#3:cellData=" + cellData);
				}
				if (cellData.length() > 0) {
					dataEntity[i] = cellData;
					log.debug("Setting dataEntity[" + i + "] = "
							+ dataEntity[i]);
				}
				else
				{
					dataEntity[i] = ""; 
					log.debug("Inserted empty string at " + i ); 
				}
			}
			data.addRow(dataEntity);
		}

		return data; 
	}

	private int getNumberOfColumnsFromSheet(HSSFSheet s) {
		int numCols = 0; 
		Iterator<Row> rowIter = s.rowIterator(); 
		while (rowIter.hasNext())
		{
			Row curRow = rowIter.next(); 
			
			if (curRow.getLastCellNum() > numCols)
			{
				numCols = curRow.getLastCellNum(); 
			}
		}
		return numCols;
	}


	private int readHeaderRow(Row curRow) {
		int ret = -1; 
		Iterator<org.apache.poi.ss.usermodel.Cell> cellIterator = curRow.cellIterator(); 
		// FIXME - need to decide to take this out into the institutional adviser 

		while (cellIterator.hasNext())
		{
			HSSFCell cl = (HSSFCell) cellIterator.next();
			String cellData =  new HSSFDataFormatter().formatCellValue(cl).toLowerCase();

			if ("student id".equals(cellData))
			{
				return cl.getColumnIndex(); 
			}

		}
		return ret; 
	}

	private NewRawFile processScantronXls(HSSFSheet s, String fileName) {
		NewRawFile data = new NewRawFile(); 
		Iterator<Row> rowIter = s.rowIterator(); 
		StringBuilder err = new StringBuilder("Scantron File with errors"); 
		boolean stop = false; 

		org.apache.poi.ss.usermodel.Cell studentIdHeader = findCellWithTextonSheetForPoi(s, SCANTRON_HEADER_STUDENT_ID);
		org.apache.poi.ss.usermodel.Cell scoreHeader = findCellWithTextonSheetForPoi(s, SCANTRON_HEADER_SCORE);
		if (studentIdHeader == null)
		{
			err.append("There is no column with the header student_id");
			stop = true; 
		}

		if (scoreHeader == null)
		{
			err.append("There is no column with the header score");
			stop = true; 

		}

		if (! stop) 
		{
			data.addRow(getScantronHeaderRow(fileName));
			while (rowIter.hasNext())
			{ 
				Row curRow = rowIter.next();  
				org.apache.poi.ss.usermodel.Cell score = null;
				org.apache.poi.ss.usermodel.Cell id = null; 

				id = curRow.getCell(studentIdHeader.getColumnIndex());
				score = curRow.getCell(scoreHeader.getColumnIndex()); 
				if (id == null )
				{
					err.append("Skipped Row "); 
					err.append(curRow.getRowNum());
					err.append(" does not have a student id column<br>"); 
					continue; 
				}
				String idStr, scoreStr; 
				
				// IF the row contains the header, meaning it is the header row, we want to skip it. 
				if (!id.equals(studentIdHeader))
				{
					// FIXME - need to decide if this is OK for everyone, not everyone will have an ID as a 
					idStr = getDataFromCellAsStringRegardlessOfCellType(id, false); 
					scoreStr = getDataFromCellAsStringRegardlessOfCellType(score, true); 
					String[] ent = new String[2];
					ent[0] = idStr; 
					ent[1] = scoreStr;

					data.addRow(ent); 
				}
			}
		}
		return data; 

	}

	private String getDataFromCellAsStringRegardlessOfCellType(org.apache.poi.ss.usermodel.Cell c, boolean decimal)
	{
		String ret = "";
		String fmt = "%.0f"; 
		if (decimal)
		{
			fmt = "%.2f"; 
		}
		if (c != null)
		{
			if (c.getCellType() == HSSFCell.CELL_TYPE_STRING)
			{
				ret = c.getRichStringCellValue().getString();
			}
			else if (c.getCellType() == HSSFCell.CELL_TYPE_NUMERIC)
			{
				ret = String.format(fmt, c.getNumericCellValue());
			} // else we want to return "" 
		} // else we want to return "" 
		return ret; 
	}
	
	// POI doesn't provide the findCell method that jexcelapi does, so we'll simulate it..  We return the first cell we find with the text in searchText
	// if we can't find it, we return null. 
	// 

	private org.apache.poi.ss.usermodel.Cell findCellWithTextonSheetForPoi(HSSFSheet s, String searchText)
	{
		if (searchText == null || s == null) 
		{
			return null; 			
		}

		Iterator<Row> rIter = s.rowIterator(); 

		while (rIter.hasNext())
		{
			Row curRow = rIter.next(); 
			Iterator<org.apache.poi.ss.usermodel.Cell> cIter = curRow.cellIterator(); 

			while (cIter.hasNext())
			{
				org.apache.poi.ss.usermodel.Cell curCell = cIter.next(); 

				if (curCell.getCellType() == HSSFCell.CELL_TYPE_STRING)
				{
					if ( searchText.equals( curCell.getRichStringCellValue().getString() ) )
					{
						return curCell; 
					}
				}
			}
		}
		return null; 
	}

	private String getStringValueFromCell(HSSFCell c)
	{
		String ret = ""; 
		StringBuilder sb = new StringBuilder(); 

		switch (c.getCellType())
		{
			case HSSFCell.CELL_TYPE_NUMERIC:
				sb.append( Double.toString( c.getNumericCellValue() ) );
				break; 
			case HSSFCell.CELL_TYPE_STRING: 
				sb.append(c.getRichStringCellValue().getString());
				break;

			default:
				sb.append(""); 
		}

		ret = sb.toString();
		sb = null; 
		return ret; 
	}
	
	
	private boolean isScantronSheetFromPoi(HSSFSheet s) {
		Iterator<Row> rowIter = s.rowIterator(); 
		while (rowIter.hasNext())
		{
			Row curRow = rowIter.next();  
			org.apache.poi.ss.usermodel.Cell possibleHeader = curRow.getCell(0); 

			if (possibleHeader != null && possibleHeader.getCellType() == org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING 
					&&  SCANTRON_HEADER_STUDENT_ID.equals(possibleHeader.getRichStringCellValue().getString()) )
			{
				return true; 
			}
		}
		// If after all that, we don't find a row starting with "student_row", we're not a scantron.. 
		return false;
	}

	public Upload parseImportCSV(Gradebook2ComponentService service, 
			String gradebookUid, Reader reader) throws InvalidInputException, FatalException 
			{

		NewRawFile rawData = new NewRawFile(); 
		CSVReader csvReader = new CSVReader(reader);
		String[] ent;
		try {
			while ( (ent = csvReader.readNext() ) != null)
			{
				rawData.addRow(ent); 
			}
			csvReader.close();
		} catch (IOException e) {
			// FIXME - error handling
			log.error(e);
		}

		rawData.setFileType("CSV file"); 
		return parseImportGeneric(service, gradebookUid, rawData);
	}	

	/*
	 * Some background on how the actual file data looks is needed for this method. 
	 * 
	 * There are three parts to the import/export file.  
	 * 
	 * The first part is
	 * structure information.  Structure information is data that is included when 
	 * exporting the GB with structure.  This data generally has the first field in
	 * the array as blank.  The next field has an identifier that signifies the type 
	 * of structure data it represents, and then the rest of the row contains data for 
	 * that type.  
	 * 
	 * The second part is what we call the header row.  The header row can be thought 
	 * of as the first row in the spreadsheet if you remove all the structure information.  
	 * It contains column headers for the remaining rows in the spreadsheet. This row 
	 * has data in the first entry which must be contained in the nameColumns static array 
	 * in the beginning of this file. 
	 * 
	 * The third and last part are the student rows which is by definition anything after 
	 * the header row.  Each student row contains data for an individual student.  
	 * Each of these rows has positional data based on the header row.  
	 * 
	 * This method has two goals, bring in the structure information, and find where the 
	 * header row is for later use. 
	 *   
	 */
	private int readDataForStructureInformation(NewRawFile rawData, Map<String, StructureRow> structureRowIndicatorMap, Map<StructureRow, String[]> structureColumnsMap) 
	{
		log.debug("readDataForStructureInformation() called");
		int curRowNumber = 0;
		int retRows = -1; 
		boolean headerFound = false; 
		String[] curRow;
		rawData.startReading();
		while ( !headerFound && (curRow = rawData.readNext()) != null) 
		{
			String firstColumnLowerCase = curRow[0].toLowerCase();
			log.debug("SI[" + curRowNumber + "]: firstColumnLowerCase=" + firstColumnLowerCase);
			/*
			 *  So if we're not a header we are probably a structure row.  We're assuming the 
			 *  import spreadsheet is built as above in proper order
			 */
			if (!headerRowIndicatorSet.contains(firstColumnLowerCase)) {
				processStructureRow(curRow, structureColumnsMap, structureRowIndicatorMap, firstColumnLowerCase, curRowNumber);
			} else {
				retRows = curRowNumber; 
				headerFound = true;
			}
			curRowNumber++; 
		}
		return retRows; 
	}

	private void processStructureRow(String[] curRow,
			Map<StructureRow, String[]> structureRowMap,
			Map<String, StructureRow> structureRowIndicatorMap,
			String firstColumnLowerCase, int curRowNumber) {
		
		log.debug("Processed non header row for row #" + curRowNumber);
		// So for each column in the row, check to see if the text is in the
		// set of structure rows possible, if it is, save it off in the map. 
		for (int i=0;i<curRow.length;i++) {
			if (curRow[i] != null && !curRow[i].equals("")) 
			{

				String columnLowerCase = curRow[i].trim().toLowerCase();
				if (log.isDebugEnabled())
					log.debug("SI: columnLowerCase=" + columnLowerCase);
				StructureRow structureRow = structureRowIndicatorMap.get(columnLowerCase);

				if (structureRow != null) {
					structureRowMap.put(structureRow, curRow);
				}
			}
		}

	}

	private boolean isScantronHeader(String in) 
	{
		return scantronIgnoreSet.contains(in);
	}

	private void readInHeaderRow(NewRawFile rawData, NewImportExportInformation ieInfo, int startRow) {
		String[] headerRow = null;
		headerRow = rawData.getRow(startRow);
		
		if (headerRow == null)
			return;
		
		NewImportHeader[] headers = new NewImportHeader[headerRow.length];
		
		for (int i = 0; i < headerRow.length; i++) {
			String text = headerRow[i];
			NewImportHeader header = null;

			header = handleHeaderRowEntry(text, i, ieInfo); 
			
			/*
			 * Note The above handleHeaderRowEntry can return null, but checking for 
			 * null is a check, so I think not checking and just assigning it in the array 
			 * is more efficient. 
			 * 
			 */
			
			headers[i] = header;
		}
		ieInfo.setHeaders(headers);
		log.debug("XXX: readInHeaderInfo() finished");
	}

	private boolean isName(String in)
	{
		return nameSet.contains(in);
	}
	private boolean isId(String in)
	{
		return idSet.contains(in);
	}
	private NewImportHeader handleHeaderRowEntry(String text, int entryNumber, NewImportExportInformation ieInfo) {
		String lowerText = text == null ? null : text.trim().toLowerCase();
		NewImportHeader header = null;
		/* 
		 * FIXME - There's gotta be a better way to handle this. 
		 * 
		 */
		if (isEmpty(lowerText) || isScantronHeader(lowerText)) { // Empty rows or scantron data in general needs to be skipped. 
			return null;
		} else if (isName(lowerText)) {
			header = new NewImportHeader(Field.S_NAME, text, entryNumber);
			header.setId("NAME");
		} else if (isId(lowerText)) {
			header = new NewImportHeader(Field.S_ID, text, entryNumber);
			header.setId("ID");
			ieInfo.trackActiveHeaderIndex(entryNumber);
		} else if (lowerText.equalsIgnoreCase("course grade")) {
			header = new NewImportHeader(Field.S_CRS_GRD, text, entryNumber);
		} else if (lowerText.equalsIgnoreCase("calculated grade")) {
			header = new NewImportHeader(Field.S_CALC_GRD, text, entryNumber);
		} else if (lowerText.equalsIgnoreCase("letter grade")) {
			header = new NewImportHeader(Field.S_LTR_GRD, text, entryNumber);
		} else if (lowerText.equalsIgnoreCase("audit grade")) {
			header = new NewImportHeader(Field.S_ADT_GRD, text, entryNumber);
		} else if (lowerText.equalsIgnoreCase("grade override")) {
			header = new NewImportHeader(Field.S_GRB_OVRD, text, entryNumber);
			ieInfo.trackActiveHeaderIndex(entryNumber);
		} else {
			header = buildItemOrCommentHeader(entryNumber, text, lowerText, ieInfo); 			
		}
		return header;
	}

	private String removeIndicatorsFromAssignmentName(String name)
	{
		if (name.contains(AppConstants.EXTRA_CREDIT_INDICATOR))
		{
			name = name.replace(AppConstants.EXTRA_CREDIT_INDICATOR, "");
		}
		
		if (name.contains(AppConstants.UNINCLUDED_INDICATOR))
		{
			name = name.replace(AppConstants.UNINCLUDED_INDICATOR, ""); 
		}
		
		if (name.startsWith(AppConstants.COMMENTS_INDICATOR)) {
			name = name.substring(AppConstants.COMMENTS_INDICATOR.length());
		}
		return name; 
	}
	
	private NewImportHeader buildItemOrCommentHeader(int entryNumber, String text,
			String lowerText, NewImportExportInformation ieInfo) {
		
		NewImportHeader header = null; 
		String name = null;
		String points = null;
		
		boolean isExtraCredit = text.contains(AppConstants.EXTRA_CREDIT_INDICATOR);
		boolean isUnincluded = text.contains(AppConstants.UNINCLUDED_INDICATOR);
		boolean isComment = text.startsWith(AppConstants.COMMENTS_INDICATOR);
		text = removeIndicatorsFromAssignmentName(text);
		name = text; 
		points = getPointsFromName(name, entryNumber); 
		name = removePointsInfoFromName(name, entryNumber); 
		if (name != null) {
			header = createHeaderForItemOrComment(text, name, entryNumber, points, isExtraCredit, isUnincluded, isComment, ieInfo);
		}
		return header; 
	}
	
	

	private NewImportHeader createHeaderForItemOrComment(String text,
			String name, int entryNumber, String points, boolean isExtraCredit,
			boolean isUnincluded, boolean isComment, NewImportExportInformation ieInfo) {
		NewImportHeader header = null; 
		
		if (isComment) {
			header = new NewImportHeader(Field.S_COMMENT, text, entryNumber);
			ieInfo.trackActiveHeaderIndex(entryNumber);
		} else {
			header = new NewImportHeader(Field.S_ITEM, name, entryNumber);
			header.setExtraCredit(isExtraCredit);
			header.setUnincluded(isUnincluded);
			header.setPoints(points);
			ieInfo.trackActiveHeaderIndex(entryNumber);
		}
		header.setHeaderName(name);

		return header; 
	}

	private String removePointsInfoFromName(String text, int entryNumber) {
		String name = text;
		int startParenthesis = text.indexOf("[");
		if (startParenthesis >= 0)
		{
			name = text.substring(0, startParenthesis);
		}
		
		if (log.isDebugEnabled())
			log.debug("X: Column " + entryNumber + " name is " + name);

		if (name != null)
			return name.trim();
		else
			return name; 
	}

	private String getPointsFromName(String text, int entryNumber) {
		
		String points = null; 
		
		int startParenthesis = text.indexOf("[");
		int endParenthesis = text.indexOf("pts]");

		if (endParenthesis == -1)
			endParenthesis = text.indexOf("]");

		if (startParenthesis != -1 && endParenthesis != -1
				&& endParenthesis > startParenthesis + 1) {
			if (log.isDebugEnabled())
				log.debug("X: Column " + entryNumber + " has pts indicated");
			points = text.substring(startParenthesis + 1, endParenthesis);
			if (log.isDebugEnabled())
				log.debug("X: Column " + entryNumber + " points are " + points);

		}
		return points;
	}

	private void readInGradeDataFromImportFile(NewRawFile rawData, 
			NewImportExportInformation ieInfo, Map<String, UserDereference> userDereferenceMap, 
			List<Learner> importRows, int startRow, Gradebook2ComponentService service) {
		String[] curRow; 
		rawData.goToRow(startRow); 
		while ((curRow = rawData.readNext()) != null) {

			Learner learnerRow = new LearnerImpl();
			
			GradeType gradeType = ieInfo.getGradebookItemModel().getGradeType();
			
			for (NewImportHeader importHeader : ieInfo.findActiveHeaders()) {
				
				if (importHeader == null)
					continue;

				int colIdx = importHeader.getColumnIndex();
				String id = importHeader.getId();
				if (colIdx >= curRow.length)
					continue;
				if (curRow[colIdx] != null && !curRow[colIdx].equals("") && importHeader.getField() != null) {
					decorateLearnerForSingleHeaderAndRowData(importHeader, curRow, learnerRow, userDereferenceMap, ieInfo, gradeType, service, colIdx, id);
				}
			}
			
			importRows.add(learnerRow);
		}	
		
	}
	
	private void decorateLearnerForSingleHeaderAndRowData(NewImportHeader importHeader, String[] rowData, 
			Learner learnerRow, Map<String, UserDereference> userDereferenceMap, 
			NewImportExportInformation ieInfo, GradeType gradeType, Gradebook2ComponentService service,
			int colIdx, String id)
	{

		switch (importHeader.getField()) {
		case S_ID:
			decorateLearnerIdFromHeaderAndRowData(learnerRow, userDereferenceMap, rowData, colIdx, ieInfo);
			break;
		case S_NAME:
			learnerRow.set(LearnerKey.S_DSPLY_NM.name(), rowData[colIdx]);
			break;
		case S_GRB_OVRD:
			learnerRow.set(LearnerKey.S_OVRD_GRD.name(), rowData[colIdx]);
			break;
		case S_ITEM:
			decorateLearnerItemFromHeaderAndRowData(learnerRow, importHeader, rowData, colIdx, ieInfo, gradeType, service, id);
			break;
		case S_COMMENT:
			learnerRow.set(Util.buildCommentKey(id), Boolean.TRUE);
			learnerRow.set(Util.buildCommentTextKey(id), rowData[colIdx]);
			break;
		}
	
	}

	private void decorateLearnerItemFromHeaderAndRowData(Learner learnerRow, NewImportHeader importHeader, String[] rowData,
			int colIdx, NewImportExportInformation ieInfo, GradeType gradeType, Gradebook2ComponentService service, String id) {

		boolean isFailure = false;
		try {
			double d = Double.parseDouble(rowData[colIdx]);
			Item item = importHeader.getItem();
			isFailure = handleSpecialPointsCaseForItem(item, d, ieInfo); 
		} catch (NumberFormatException nfe) {
			// This is not necessarily an exception, for example, we might be
			// reading letter grades
			
			if (gradeType != GradeType.LETTERS || !service.isValidLetterGrade(rowData[colIdx])) {
				log.info("Caught exception " + nfe + " while importing grades.", nfe); 
				isFailure = true;
				ieInfo.setInvalidScore(true);
			} 
		}
		
		if (isFailure) {
			String failedId = Util.buildFailedKey(id);
			learnerRow.set(failedId, "This entry is not valid");
		}
		
		learnerRow.set(id, rowData[colIdx]);
		
	}

	// FIXME - based on Kirk/Trainers, this will probably change. 
	private boolean handleSpecialPointsCaseForItem(Item item, double d, NewImportExportInformation ieInfo) {
		boolean isFailure = false;
		if (item != null) {
			Double points = item.getPoints();
			if (points != null) {
				if (points.doubleValue() < d) {
					
					if (item.getItemId() != null && item.getItemId().equals(Long.valueOf(-1l))) {
						// Ensure that we have an int
						d = Math.ceil(d);
						// Round to the nearest hundred if d > 100,
						// otherwise, round to the nearest ten
						if (d > 100) {
							d = d / 100;
							d = Math.ceil(d);
							d = d * 100;
						} else if (d > 10) {
							d = d / 10;
							d = Math.ceil(d);
							d = d * 10;
						}
						item.setPoints(d);
					} else {
						isFailure = true;
						ieInfo.setInvalidScore(true);
					}
				}
			}
		} 
		return isFailure; 
	}

	private void decorateLearnerIdFromHeaderAndRowData(
			Learner learnerRow,
			Map<String, UserDereference> userDereferenceMap, String[] rowData,
			int colIdx, NewImportExportInformation ieInfo) {
		
		String userImportId = rowData[colIdx];
		learnerRow.setExportUserId(userImportId);
		learnerRow.setStudentDisplayId(userImportId);
		
		UserDereference userDereference = userDereferenceMap.get(userImportId);

		if (userDereference != null) {
			learnerRow.setIdentifier(userDereference.getUserUid());
			learnerRow.setStudentName(userDereference.getDisplayName());
			learnerRow.setLastNameFirst(userDereference.getLastNameFirst());
			learnerRow.setStudentDisplayId(userDereference.getDisplayId());
			learnerRow.setUserNotFound(Boolean.FALSE);
		} else {
			learnerRow.setLastNameFirst("User not found");
			learnerRow.setUserNotFound(Boolean.TRUE);
			ieInfo.setUserNotFound(true);
		}
		
	}

	private CategoryType getGradebookCategoryTypeFromGradebookRow(String[] gradebookRow, GradeItem gradebookItemModel)
	{
		CategoryType cType = gradebookItemModel.getCategoryType();
		String categoryType = null;
		if (gradebookRow.length >= 4)
			categoryType = gradebookRow[3];

		if (categoryType != null) {
			if (CategoryType.NO_CATEGORIES.getDisplayName().equals(categoryType))
				cType = CategoryType.NO_CATEGORIES;
			else if (CategoryType.SIMPLE_CATEGORIES.getDisplayName().equals(categoryType))
				cType = CategoryType.SIMPLE_CATEGORIES;
			else if (CategoryType.WEIGHTED_CATEGORIES.getDisplayName().equals(categoryType))
				cType = CategoryType.WEIGHTED_CATEGORIES;

		}
		return cType; 

	}
	
	private String getGradebookNameFromGradebookRow(String[] gradebookRow, GradeItem gradebookItemModel)
	{
		String gradebookName = gradebookItemModel.getName();
		if (gradebookRow.length >= 3)
			gradebookName = gradebookRow[2];
		return gradebookName; 
	}
	
	private GradeType getGradeTypeFromGradebookRow(String[] gradebookRow, GradeItem gradebookItemModel)
	{
		GradeType gType =  gradebookItemModel.getGradeType();
		String gradeType = null;  
		if (gradebookRow.length >= 5)
			gradeType = gradebookRow[4];
		
		if (gradeType != null) {
			if (getDisplayName(GradeType.PERCENTAGES).equals(gradeType))
				gType = GradeType.PERCENTAGES;
			else if (getDisplayName(GradeType.POINTS).equals(gradeType))
				gType = GradeType.POINTS;
			else if (getDisplayName(GradeType.LETTERS).equals(gradeType))
			{
				gType = GradeType.LETTERS;				
			}
		}

		return gType; 
		
	}
	
	private void processStructureInformationForGradebookRow(GradeItem gradebookItemModel, String[] gradebookRow)
	{

		if (gradebookRow != null && gradebookItemModel != null) {
			CategoryType cType = getGradebookCategoryTypeFromGradebookRow(gradebookRow, gradebookItemModel);
			GradeType gType = getGradeTypeFromGradebookRow(gradebookRow, gradebookItemModel);
			String gradebookName = getGradebookNameFromGradebookRow(gradebookRow, gradebookItemModel);
			gradebookItemModel.setCategoryType(cType);
			gradebookItemModel.setGradeType(gType);
			gradebookItemModel.setName(gradebookName);
		}

	}
		
	private void processStructureInformation(NewImportExportInformation ieInfo, Map<StructureRow, String[]> structureColumnsMap) throws InvalidInputException
	{
		// Now, modify gradebook structure according to the data stored
		String[] gradebookRow = structureColumnsMap.get(StructureRow.GRADEBOOK);
		GradeItem gradebookItemModel = (GradeItem) ieInfo.getGradebookItemModel();
		
		// this reads from the "Gradebook:" row and processes its options. 
		processStructureInformationForGradebookRow(gradebookItemModel, gradebookRow);
		// this reads from a set of  having to do with display and scaled EC liens. 
		processStructureInformationForDisplayAndScaledOptions(gradebookItemModel, ieInfo, structureColumnsMap);
		
		// If we're in no categories mode, either from the import file itself or
		// because the import file doesn't contain any category info and the gb is a no cats gb
		// then we skip the rest
		if (gradebookItemModel.getCategoryType()  == CategoryType.NO_CATEGORIES)
			return;
		
		String[] categoryRow = structureColumnsMap.get(StructureRow.CATEGORY);
		String[] percentGradeRow = structureColumnsMap.get(StructureRow.PERCENT_GRADE);
		String[] dropLowestRow = structureColumnsMap.get(StructureRow.DROP_LOWEST);
		String[] equalWeightRow = structureColumnsMap.get(StructureRow.EQUAL_WEIGHT);

		/*
		 *  In order to understand this, one needs to know that the import data is positional 
		 *  in nature.  Categories are at a particular position in the file, and items are 
		 *  relational to the category. So if category A starts at column #4 and the next category 
		 *  starts at column #10, then items in columns 4-10 are in the category A. This is true
		 *  with other row structure data such as percent grade, drop lowest, etc.  
		 *  
		 *  Also, I realize this may not be the most efficient way of doing this.  I'm aiming for 
		 *  being readable/understandable over raw efficiency.  Also, there are a known number of 
		 *  structure rows, so iterating in memory over this list should not be that bad. 
		 *  
		 */
		
		List<CategoryPosition> categoryPositions; 
		categoryPositions = processCategoryRow(categoryRow, gradebookItemModel, ieInfo);
		if (categoryPositions != null)
		{
			processExtraCategoryRelatedData(equalWeightRow, percentGradeRow, dropLowestRow, gradebookItemModel, categoryPositions);
		}
		else 
			/*
			 * The category row either didn't exist, or there was nothing in it.  The old way of doing things said we had 
			 * to build the category list, so that's what we'll do. 
			 */
		{
			populatecategoryIdItemMap(ieInfo, gradebookItemModel); 
			addDefaultCategoryIfNeeded(ieInfo, gradebookItemModel); 
			
		}
	}
	
	private void addDefaultCategoryIfNeeded(NewImportExportInformation ieInfo,
			GradeItem gradebookItemModel) {
		if (ieInfo.getCategoryIdItemMap().get("-1") == null) {
			GradeItem categoryModel = new GradeItemImpl();
			categoryModel.setIdentifier("-1");
			categoryModel.setCategoryId(Long.valueOf(-1l));
			categoryModel.setItemType(ItemType.CATEGORY);
			categoryModel.setName(AppConstants.DEFAULT_CATEGORY_NAME);

			gradebookItemModel.addChild((GradeItem)categoryModel);
			ieInfo.getCategoryIdItemMap().put("-1", categoryModel);
		}
		
	}

	private void populatecategoryIdItemMap(NewImportExportInformation ieInfo,
			GradeItem gradebookItemModel) {
		
		List<GradeItem> children = gradebookItemModel.getChildren();

		if (children != null) {
			for (GradeItem categoryModel : children) {
				ieInfo.getCategoryIdItemMap().put(categoryModel.getIdentifier(), categoryModel);
			}
		}		
	}

	private void processExtraCategoryRelatedData(String[] equalWeightRow, String[] percentGradeRow, String[] dropLowestRow,
			GradeItem gradebookItemModel,
			List<CategoryPosition> categoryPositions) {
			for (CategoryPosition p : categoryPositions)
			{
				int col = p.getColNumber(); 
				GradeItem categoryModel = p.getCategory(); 
				processPercentGradeRow(percentGradeRow, categoryModel, col);
				processEqualWeightRow(equalWeightRow, categoryModel, col); 
				processDropLowestRow(dropLowestRow, categoryModel, col);
			}
	}

	private void processEqualWeightRow(String[] equalWeightRow, GradeItem categoryModel, int col) {
		if (equalWeightRow != null)
		{
			if (equalWeightRow.length > col)
			{
				String curEqualWeight = equalWeightRow[col]; 
					
				if (!isEmpty(curEqualWeight))
				{
					try {
						boolean isEqualWeight = Boolean.parseBoolean(curEqualWeight);
						categoryModel.setEqualWeightAssignments(Boolean.valueOf(isEqualWeight));
					} catch (NumberFormatException nfe) {
						log.info("Failed to parse " + curEqualWeight + " as an Boolean for col " + col + " on Equal Weight ROW.", nfe);
					}
				}
			}
		}		
	}

	private void processDropLowestRow(String[] dropLowestRow,
			GradeItem categoryModel, int col) {
		if (dropLowestRow != null)
		{
			if (dropLowestRow.length > col)
			{
				String curDropLowest = dropLowestRow[col]; 

				if (!isEmpty(curDropLowest))
				{
					try {
						int dL = Integer.parseInt(curDropLowest);
						categoryModel.setDropLowest(Integer.valueOf(dL));
					} catch (NumberFormatException nfe) {
						log.warn("Failed to parse " + curDropLowest+ " as an Integer for col " + col + " on Drop Lowest row.", nfe);
					}

				}
			}				
		}
		
	}

	private void processPercentGradeRow(String[] percentGradeRow,
			GradeItem categoryModel, int col) 
	{
		if (percentGradeRow != null)
		{
			if (percentGradeRow.length > col)
			{
				String curPercentGrade = percentGradeRow[col]; 
				if (!isEmpty(curPercentGrade))
				{
					try {
						curPercentGrade = curPercentGrade.replace("%", "");
						double pG = Double.parseDouble(curPercentGrade);
						categoryModel.setPercentCourseGrade(Double.valueOf(pG));
						categoryModel.setWeighting(Double.valueOf(pG));
					} catch (NumberFormatException nfe) {
						log.info("Failed to parse " + curPercentGrade + " as a Double for col " + col + " on percent Grade row.", nfe);
					}

				}
			}

			
		}
	}

	private boolean isEmpty(String in) 
	{
		if (in != null)
		{
			return "".equals(in.trim());
		}
		else
		{
			return true; 
		}
	}
	private List<CategoryPosition> processCategoryRow(String[] categoryRow,
			GradeItem gradebookItemModel, NewImportExportInformation ieInfo) {
		List<CategoryPosition> ret = new ArrayList<CategoryPosition>();
		Map<String, GradeItem> categoryMap = new HashMap<String, GradeItem>();
		
		addExistingCategoriesFromGradebookItemModelToMap(categoryMap, gradebookItemModel); 
		// First position is a blank, second is the Category row identifier.
		// Since this had already been read in as a category, probably don't 
		// need to check twice
		if (categoryRow != null && categoryRow.length > 2)
		{

			processActualCategoryRowData(categoryRow, categoryMap, gradebookItemModel, ret, ieInfo);
			return ret;			
		}
		else
		{
			return null; 
		}
	}

private void processActualCategoryRowData(String[] categoryRow, Map<String, GradeItem> categoryMap, 
		GradeItem gradebookItemModel, List<CategoryPosition> catpositions, NewImportExportInformation ieInfo) {

	// This array is a quick map from assignment position to category ID.  
	String[] assignmentToCategoryQuick = new String[(ieInfo.getHeaders() != null ? ieInfo.getHeaders().length : 0)];
	String currentCategoryId = null;

	// First position should be blank, second should have Category: in it.  We'll start at two.
	for (int i=2;i<categoryRow.length;i++) {
		String curCategoryString = categoryRow[i];  
		
		if (!isEmpty(curCategoryString))
		{
			GradeItem categoryModel = null;
			categoryModel = buildOrGetExistingCategoryForUpdate(i,curCategoryString, categoryMap, gradebookItemModel);
			curCategoryString = removeIndicators(curCategoryString); 
			// At this point we either have a current category or we made one.  So lets save off the position for posterity...	
			CategoryPosition pos = new CategoryPosition(i, categoryModel, curCategoryString);
			catpositions.add(pos);
			if (categoryModel.getIdentifier() != null && !categoryModel.getIdentifier().equals("null")) {
				currentCategoryId = categoryModel.getIdentifier();
				ieInfo.getCategoryIdItemMap().put(currentCategoryId, categoryModel);
				assignmentToCategoryQuick[i] = currentCategoryId;			
			}
			else
			{
				currentCategoryId = null; 
				assignmentToCategoryQuick[i] = currentCategoryId;							
			}

			if (categoryModel.getCategoryId() != null) {
				String categoryIdAsString = String.valueOf(categoryModel.getCategoryId());
				ieInfo.getCategoryIdNameMap().put(categoryIdAsString, categoryModel.getName());
			}
			categoryModel.setChecked(true);
		
		}
		else
		{
			assignmentToCategoryQuick[i] = currentCategoryId;
		}
	}
	ieInfo.setAssignmentPositionToCategoryIdQuick(assignmentToCategoryQuick); 
		
}

	
private String removeIndicators(String curCategoryString) {

	// We have to do some extra work in here, a cpl of times, but this should be pretty quick.. 
	boolean isExtraCredit = curCategoryString.contains(AppConstants.EXTRA_CREDIT_INDICATOR);

	if (isExtraCredit)
		curCategoryString = curCategoryString.replace(AppConstants.EXTRA_CREDIT_INDICATOR, "");

	boolean isUnincluded = curCategoryString.contains(AppConstants.UNINCLUDED_INDICATOR);

	if (isUnincluded)
		curCategoryString = curCategoryString.replace(AppConstants.UNINCLUDED_INDICATOR, "");
	return curCategoryString;
}

private GradeItem buildOrGetExistingCategoryForUpdate(int col, String curCategoryString, Map<String, GradeItem> categoryMap, GradeItem gradebookItemModel) {
	
	GradeItem categoryModel = null;
	
	boolean isNewCategory = !categoryMap.containsKey(curCategoryString);	
	boolean isExtraCredit = curCategoryString.contains(AppConstants.EXTRA_CREDIT_INDICATOR);
	boolean isUnincluded = curCategoryString.contains(AppConstants.UNINCLUDED_INDICATOR);
	boolean isDefaultCategory = curCategoryString.equalsIgnoreCase(AppConstants.DEFAULT_CATEGORY_NAME);
	
	if (isDefaultCategory) {
		// Check if the default category is already in this Gradebook
		categoryModel = getDefaultCategoryFromGradebookItemModel(gradebookItemModel);	
	}
	
	if (categoryModel == null)
	{
		if (isNewCategory)
		{
			categoryModel = buildNewCategory(curCategoryString, isDefaultCategory, isUnincluded, isExtraCredit, col);
			gradebookItemModel.addChild((GradeItem)categoryModel);
		}
		else
		{
			categoryModel = categoryMap.get(removeIndicators(curCategoryString));
		}
	}
	return categoryModel;
}

private GradeItem buildNewCategory(String curCategoryString,
			boolean isDefaultCategory, boolean isUnincluded,
			boolean isExtraCredit, int col) {
		GradeItem categoryModel; 
		String identifier = isDefaultCategory ? String.valueOf(Long.valueOf(-1l)) : AppConstants.NEW_CAT_PREFIX + col;
		
		categoryModel = new GradeItemImpl();
		categoryModel.setIdentifier(identifier);
		categoryModel.setItemType(ItemType.CATEGORY);
		categoryModel.setName(removeIndicators(curCategoryString));
		if (!isDefaultCategory) {
			// We only worry about these for new categories, the default category is by definition unincluded and not extra credit
			categoryModel.setIncluded(Boolean.valueOf(!isUnincluded));
			categoryModel.setExtraCredit(Boolean.valueOf(isExtraCredit));
		}
		
		return categoryModel;

	}

	private GradeItem getDefaultCategoryFromGradebookItemModel(GradeItem gradebookItemModel)
	{
		List<GradeItem> children = gradebookItemModel.getChildren();
		if (children != null && children.size() > 0) {
			for (GradeItem child : children) {
				if (child.getName().equals(AppConstants.DEFAULT_CATEGORY_NAME)) {
					return child; 
				}
			}
		}
		return null; 
	}
	private void addExistingCategoriesFromGradebookItemModelToMap(Map<String, GradeItem> categoryMap,
			GradeItem gradebookItemModel) {
		for (GradeItem child : gradebookItemModel.getChildren()) {
			if (child.getItemType() != null && child.getItemType() == ItemType.CATEGORY)
			{
				if (child.getChildCount() > 0)
				{
					GradeItemImpl gi = (GradeItemImpl) child;
					gi.setChildren(null); 
				}
				categoryMap.put(child.getName(), child);
			}
		}
	}

	private void processStructureInformationForDisplayAndScaledOptions(
			GradeItem gradebookItemModel, NewImportExportInformation ieInfo,
			Map<StructureRow, String[]> structureColumnsMap) {
		
		OptionState scaledEC = checkRowOption(StructureRow.SCALED_EC, structureColumnsMap); 
		OptionState showCourseGrades = checkRowOption(StructureRow.SHOWCOURSEGRADES, structureColumnsMap);
		OptionState showItemStats = checkRowOption(StructureRow.SHOWITEMSTATS, structureColumnsMap); 
		OptionState showMean = checkRowOption(StructureRow.SHOWMEAN, structureColumnsMap);
		OptionState showMedian = checkRowOption(StructureRow.SHOWMEDIAN, structureColumnsMap); 
		OptionState showMode = checkRowOption(StructureRow.SHOWMODE, structureColumnsMap);
		OptionState showRank = checkRowOption(StructureRow.SHOWRANK, structureColumnsMap); 
		OptionState showReleasedItems = checkRowOption(StructureRow.SHOWRELEASEDITEMS, structureColumnsMap);
		
		if (scaledEC != OptionState.NULL)
		{
			gradebookItemModel.setExtraCreditScaled(scaledEC == OptionState.TRUE ? Boolean.TRUE : Boolean.FALSE);
		}

		if (showCourseGrades != OptionState.NULL)
		{
			gradebookItemModel.setReleaseGrades(showCourseGrades == OptionState.TRUE ? Boolean.TRUE : Boolean.FALSE);
		}

		if (showItemStats != OptionState.NULL)
		{
			gradebookItemModel.setShowItemStatistics(showItemStats == OptionState.TRUE ? Boolean.TRUE : Boolean.FALSE);
		}
		
		if (showMean != OptionState.NULL)
		{
			gradebookItemModel.setShowMean(showMean == OptionState.TRUE ? Boolean.TRUE : Boolean.FALSE);
		}
		if (showMedian != OptionState.NULL)
		{
			gradebookItemModel.setShowMedian(showMedian == OptionState.TRUE ? Boolean.TRUE : Boolean.FALSE);
		}

		if (showMode != OptionState.NULL)
		{
			gradebookItemModel.setShowMode(showMode == OptionState.TRUE ? Boolean.TRUE : Boolean.FALSE);
		}
		
		if (showRank != OptionState.NULL)
		{
			gradebookItemModel.setShowRank(showRank == OptionState.TRUE ? Boolean.TRUE : Boolean.FALSE);
		}

		if (showReleasedItems != OptionState.NULL)
		{
			gradebookItemModel.setReleaseItems(showReleasedItems == OptionState.TRUE ? Boolean.TRUE : Boolean.FALSE);
		}

	}
	private OptionState checkRowOption(StructureRow theRow, Map<StructureRow, String[]> structureColumnsMap)
	{
		String[] rowData = structureColumnsMap.get(theRow); 
	
		log.debug("rowData: " + Arrays.toString(rowData)); 
		if (rowData == null)
		{
			return OptionState.NULL; 
		}
		else if (rowData[2].compareToIgnoreCase("true") == 0) 
		{
			return OptionState.TRUE; 
		}
		else
		{
			return OptionState.FALSE; 
		}
		
	}

	private void processHeaders(NewImportExportInformation ieInfo, Map<StructureRow, String[]> structureColumnsMap) throws ImportFormatException {
		NewImportHeader[] headers = ieInfo.getHeaders();
		
		if (headers == null)
			return;
		
		
		// Although these contain "structure" information, it's most efficient to check them while we're looping through 
		// the header columns
		// During the 6/1 refactor I left this alone, probably could have moved this back as 
		String[] pointsColumns = structureColumnsMap.get(StructureRow.POINTS);
		String[] percentCategoryColumns = structureColumnsMap.get(StructureRow.PERCENT_CATEGORY);
			
		// Iterate through each header once
		for (int i=0;i<headers.length;i++) {
		
			NewImportHeader header = headers[i];
			
			// Ignore null headers
			if (header == null)
				continue;
	
			
			if (header.getField() == Field.S_ITEM || header.getField() == Field.S_COMMENT) {
				handleItemOrComment(header, pointsColumns, percentCategoryColumns, ieInfo, i);
			}
		
		}
	}
	private String getEntryFromRow(String[] row, int col)
	{
		if (row != null && row.length > col && Util.isNotNullOrEmpty(row[col])) {
			return row[col];
		}
		else
		{
			return "";
		}
		
	}
	
	private void handleItemOrComment(NewImportHeader header, String[] pointsColumns, 
			String[] percentCategoryColumns, NewImportExportInformation ieInfo, int headerNumber) throws ImportFormatException {

		Map<String, GradeItem> categoryIdItemMap = ieInfo.getCategoryIdItemMap();
		Item gradebookItemModel = ieInfo.getGradebookItemModel();
		CategoryType categoryType = gradebookItemModel.getCategoryType();
		String itemName = header.getHeaderName();
		
		if (header.getField() == Field.S_ITEM) {
			// If we have the points and percent Category from the structure information, this will put it where it needs to be. 
			handlePointsAndPercentCategoryForHeader(pointsColumns, percentCategoryColumns, headerNumber, header); 
		}
		CategoryItemPair p = getCategoryAndItemInformation(categoryType, itemName, gradebookItemModel, header, headerNumber, ieInfo); 
		GradeItem itemModel = p.getItem();
		GradeItem categoryModel = p.getCategory();
		boolean isNewItem = false;

		if (itemModel == null) {
			isNewItem = true;
			itemModel =  createNewGradeItem(header, headerNumber);
		} else {
			header.setId(itemModel.getIdentifier());
		}

		if (header.getField() == Field.S_ITEM) {
			decorateItemFromHeader(header, itemModel, categoryModel); 
		}

		putItemInGradebookModelHierarchy(categoryType, itemModel, gradebookItemModel, categoryModel, isNewItem); 
	}
	
	private void decorateItemFromHeader(NewImportHeader header,
			GradeItem itemModel, GradeItem categoryModel) throws ImportFormatException {
		/*
		 * This stuff is because we can include indicators on the normal header row which have points and percent grade. 
		 */
		// Modify the percentage category contribution
		decorateItemForStructureInfo(header, itemModel); 
		itemModel.setIncluded(Boolean.valueOf(!header.isUnincluded()));
		itemModel.setExtraCredit(Boolean.valueOf(header.isExtraCredit()));
		itemModel.setChecked(true);
		header.setItem(itemModel);
	}

	private void putItemInGradebookModelHierarchy(CategoryType categoryType,
			GradeItem itemModel, Item gradebookItemModel,
			GradeItem categoryModel, boolean isNewItem) {

		if (categoryType == CategoryType.NO_CATEGORIES) {
			((GradeItem)gradebookItemModel).addChild(itemModel);
		} else if (categoryModel != null) {
			if (categoryModel.getName() != null && categoryModel.getName().equals(AppConstants.DEFAULT_CATEGORY_NAME))
				itemModel.setIncluded(Boolean.FALSE);

			categoryModel.addChild(itemModel);
		} else if (isNewItem) {
			itemModel.setIncluded(Boolean.FALSE);
		}
	}
	private void decorateItemForStructureInfo(NewImportHeader header,
			GradeItem itemModel) throws ImportFormatException {
		// First handle points
		if (header.getPoints() != null) {
			String pointsField = header.getPoints();

			if (!pointsField.contains("A-F") &&
					!pointsField.contains("%")) {

				try {
					Double points = Util.convertStringToDouble(pointsField);
					itemModel.setPoints(points);
				} catch (NumberFormatException nfe) {
					log.info("User error. Failed on import: points field for column " + header.getValue() + " or " + pointsField + " cannot be formatted as a double");
					throw new ImportFormatException("Failed to import this file. For the column " + header.getValue() + ", the points field " + pointsField + " cannot be read as a number.");
				}
			}
		}
		// Now handle percent category
		if (header.getPercentCategory() != null) {
			String percentCategoryField = header.getPercentCategory();

			try {
				Double percentCategory = Util.fromPercentString(percentCategoryField);
				itemModel.setPercentCategory(percentCategory);
				itemModel.setWeighting(percentCategory);
			} catch (NumberFormatException nfe) {
				log.info("User error. Failed on import: percent category field for column " + header.getValue() + " or " + percentCategoryField + " cannot be formatted as a double");
				throw new ImportFormatException("Failed to import this file. For the column " + header.getValue() + ", the percent category field " + percentCategoryField + " cannot be read as a number.");
			}
		}		
	}

	private GradeItem createNewGradeItem(NewImportHeader header, int headerNumber) {
		GradeItem itemModel = null; 
		
		itemModel = new GradeItemImpl();

		String identifier = new StringBuilder().append(AppConstants.NEW_PREFIX).append(headerNumber).toString();
		header.setId(identifier);
		itemModel.setItemType(ItemType.ITEM);
		itemModel.setStudentModelKey(LearnerKey.S_ITEM.name());
		itemModel.setIdentifier(identifier);
		itemModel.setName(header.getHeaderName());
		itemModel.setItemId(Long.valueOf(-1l));
		itemModel.setCategoryId(Long.valueOf(-1l));
		itemModel.setCategoryName(header.getCategoryName());
		itemModel.setPoints(Double.valueOf(100d));
		return itemModel; 		
	}

	private CategoryItemPair getCategoryAndItemInformation(
			CategoryType categoryType, String itemName,
			Item gradebookItemModel, NewImportHeader header, int headerNumber,
			NewImportExportInformation ieInfo) {

		String[] assignmentPositionToCategoryIdQuick = ieInfo.getAssignmentPositionToCategoryIdQuick();
		CategoryItemPair p = null; 

		switch (categoryType) {
		case NO_CATEGORIES:
			p = new CategoryItemPair(null, findModelByName(itemName, gradebookItemModel));
			break;
		case SIMPLE_CATEGORIES:
		case WEIGHTED_CATEGORIES:
			String categoryId = null; 
			if (assignmentPositionToCategoryIdQuick != null)
			{
				categoryId = assignmentPositionToCategoryIdQuick[headerNumber];
			}
			p = getCategoryAndItemModel(categoryId, itemName, gradebookItemModel, header, ieInfo.getCategoryIdItemMap());
			
			break;
		}
		
		return p; 
	}

	private void handlePointsAndPercentCategoryForHeader(
			String[] pointsColumns, String[] percentCategoryColumns,
			int headerNumber, NewImportHeader header) {
		
		if (!"".equals(getEntryFromRow(pointsColumns, headerNumber))) {
			header.setPoints(getEntryFromRow(pointsColumns, headerNumber));
		}

		if (!"".equals(getEntryFromRow(percentCategoryColumns, headerNumber)))
		{
			header.setPercentCategory( getEntryFromRow(percentCategoryColumns, headerNumber) );
		}		
	}

	private CategoryItemPair getCategoryAndItemModel(String categoryId,
			String itemName, Item gradebookItemModel, NewImportHeader header,
			Map<String, GradeItem> categoryIdItemMap) {
		GradeItem itemModel = null; 
		GradeItem categoryModel = null; 
		
		if (categoryId == null) {
			itemModel = findModelByName(itemName, gradebookItemModel);
			// If this is a new item, and we don't have structure info, then
			// we have to make it "Unassigned"
			if (itemModel == null)
				categoryModel = categoryIdItemMap.get("-1");
		} else {
			categoryModel = categoryIdItemMap.get(categoryId);
			if (categoryModel != null) {
				decorateHeaderWithCategoryModel(header, categoryModel); 
				if (findItemInCategory(categoryModel, itemName) != null)
				{
					itemModel = findItemInCategory(categoryModel, itemName); 
				}
			} 
			else
			{
				log.warn("CategoryModel is null via lookup in map");
			}
		} 
		return new CategoryItemPair(categoryModel, itemModel); 
	}

	private GradeItem findItemInCategory(GradeItem categoryModel,
			String itemName) {
		GradeItem itemModel = null; 
		List<GradeItem> children = categoryModel.getChildren();
		if (children != null && children.size() > 0) {
			for (GradeItem item : children) {
				if (item.getName().equals(itemName)) {
					itemModel = item;
					break;
				}
			}
		}

		return itemModel;
	}

	private void decorateHeaderWithCategoryModel(NewImportHeader header,
			GradeItem categoryModel) {
		if (categoryModel != null) {
			header.setCategoryId(categoryModel.getIdentifier());
			header.setCategoryName(categoryModel.getCategoryName());
		}		
	}

	public Upload parseImportGeneric(Gradebook2ComponentService service, 
			String gradebookUid, NewRawFile rawData) throws InvalidInputException, FatalException {
		String msgs = rawData.getMessages();
		boolean errorsFound = rawData.isErrorsFound(); 

		if (errorsFound) {
			Upload importFile = new UploadImpl();
			importFile.setErrors(true); 
			importFile.setNotes(msgs);
			return importFile; 
		}

		Gradebook gradebook = service.getGradebook(gradebookUid);
		Item gradebookItemModel = gradebook.getGradebookItemModel();

		List<UserDereference> userDereferences = service.findAllUserDereferences();
		Map<String, UserDereference> userDereferenceMap = new HashMap<String, UserDereference>();
		buildDereferenceIdMap(userDereferences, userDereferenceMap, service);
		NewImportExportInformation ieInfo = new NewImportExportInformation();
		
		UploadImpl importFile = new UploadImpl();

		if (rawData.isScantronFile())
		{
			importFile.setNotifyAssignmentName(!rawData.isNewAssignment()); 
			if (!rawData.isNewAssignment()) // FIXME - i18n 
				importFile.addNotes("The scantron assignment entered has previously been imported.  We have changed the assignment name so that it will be imported uniquely. If you wanted to replace the old data, then please change it back.");
		}
		
		ieInfo.setGradebookItemModel(gradebookItemModel);
		
		ArrayList<Learner> importRows = new ArrayList<Learner>();

		Map<String, StructureRow> structureRowIndicatorMap = new HashMap<String, StructureRow>();
		Map<StructureRow, String[]> structureColumnsMap = new HashMap<StructureRow, String[]>();

		buildRowIndicatorMap(structureRowIndicatorMap);

		int structureStop = 0; 

		structureStop = readDataForStructureInformation(rawData, structureRowIndicatorMap, structureColumnsMap);
		if (structureStop != -1)
		{
			try {
				readInHeaderRow(rawData, ieInfo, structureStop);
				processStructureInformation(ieInfo, structureColumnsMap);
				processHeaders(ieInfo, structureColumnsMap);
				readInGradeDataFromImportFile(rawData, ieInfo, userDereferenceMap, importRows, structureStop, service);
				GradeItem gradebookGradeItem = (GradeItem)ieInfo.getGradebookItemModel();
				service.decorateGradebook(gradebookGradeItem, null, null);
				importFile.setGradebookItemModel(gradebookGradeItem);
				importFile.setRows(importRows);
				importFile.setGradeType(gradebookItemModel.getGradeType());
				importFile.setCategoryType(gradebookItemModel.getCategoryType());
					
				if (ieInfo.isUserNotFound()) 
					importFile.addNotes("One or more users were not found based on the import identifier provided. This could indicate that the wrong import id is being used, or that the file is incorrectly formatted for import.");

				if (ieInfo.isInvalidScore()) 
					importFile.addNotes("One or more uploaded scores cannot be accepted because they are not in the correct format or the scores are higher than the maximum allowed for those items. These entries have been highlighted in red.");
			} catch (Exception e) {
				importFile.setErrors(true);
				importFile.setNotes(e.getMessage());
				importFile.setRows(null);
				log.warn(e, e);
			}
		}
		else
		{
			importFile.setErrors(true); 
			importFile.setNotes("The file loaded does not contain the required header information to load."); 
		}
		
		service.postEvent("gradebook2.import", String.valueOf(gradebook.getGradebookId()));

		return importFile;
	}

	private void buildDereferenceIdMap(List<UserDereference> userDereferences,
			Map<String, UserDereference> userDereferenceMap,
			Gradebook2ComponentService service) {

		for (UserDereference dereference : userDereferences) {
			String exportUserId = service.getExportUserId(dereference); 
			userDereferenceMap.put(exportUserId, dereference);
		}
	}

	private void buildRowIndicatorMap(
			Map<String, StructureRow> structureRowIndicatorMap) {
		for (StructureRow structureRow : EnumSet.allOf(StructureRow.class)) {
			String lowercase = structureRow.getDisplayName().toLowerCase();
			structureRowIndicatorMap.put(lowercase, structureRow);
		}		
	}
	
	private String getDisplayName(CategoryType categoryType) {
		switch (categoryType) {
		case NO_CATEGORIES:
			return i18n.getString("orgTypeNoCategories");
		case SIMPLE_CATEGORIES:
			return i18n.getString("orgTypeCategories");
		case WEIGHTED_CATEGORIES:
			return i18n.getString("orgTypeWeightedCategories");
		}
		return "N/A";
	}

	private String getDisplayName(GradeType gradeType) {
		switch (gradeType) {
		case POINTS:
			return i18n.getString("gradeTypePoints");
		case PERCENTAGES:
			return i18n.getString("gradeTypePercentages");
		case LETTERS:
			return i18n.getString("gradeTypeLetters");
		}
		
		return "N/A";
	}

	private GradeItem findModelByName(final String name, Item root) {

		ItemModelProcessor processor = new ItemModelProcessor(root) {

			@Override
			public void doItem(Item itemModel) {

				String itemName = itemModel.getName();

				if (itemName != null) {
					String trimmed = itemName.trim();

					if (trimmed.equals(name)) {
						this.result = itemModel;
					}
				}
			}
			
		};

		processor.process();

		return (GradeItem)processor.getResult();
	}
}

class NewImportExportInformation 
{
	Set<Integer> ignoreColumns;
	int idFieldIndex;
	int nameFieldIndex;
	int courseGradeFieldIndex;
	boolean foundStructure; 
	boolean foundHeader; 
	Map<String, String> categoryIdNameMap;
	Map<String, GradeItem> categoryIdItemMap;
	
	NewImportHeader[] headers;
	String[] assignmentPositionToCategoryIdQuick;

	boolean isInvalidScore;
	boolean isUserNotFound;
	
	List<Integer> activeHeaderIndexes;
	List<CategoryPosition> categoryPositions; 
	Item gradebookItemModel;
	
	
	public NewImportExportInformation() 
	{
		ignoreColumns = new HashSet<Integer>();
		idFieldIndex = -1;
		nameFieldIndex = -1;
		courseGradeFieldIndex = -1;
		categoryIdNameMap = new HashMap<String, String>();
		categoryIdItemMap = new HashMap<String, GradeItem>();

		activeHeaderIndexes = new LinkedList<Integer>();
	}

	public void trackActiveHeaderIndex(int index) {
		activeHeaderIndexes.add(Integer.valueOf(index));
	}
	
	public Set<Integer> getIgnoreColumns() {
		return ignoreColumns;
	}

	public void setIgnoreColumns(Set<Integer> ignoreColumns) {
		this.ignoreColumns = ignoreColumns;
	}

	public int getCourseGradeFieldIndex() {
		return courseGradeFieldIndex;
	}

	public void setCourseGradeFieldIndex(int courseGradeFieldIndex) {
		this.courseGradeFieldIndex = courseGradeFieldIndex;
	}

	public boolean isFoundStructure() {
		return foundStructure;
	}

	public void setFoundStructure(boolean foundStructure) {
		this.foundStructure = foundStructure;
	}

	public boolean isFoundHeader() {
		return foundHeader;
	}

	public void setFoundHeader(boolean foundHeader) {
		this.foundHeader = foundHeader;
	}

	public Map<String, String> getCategoryIdNameMap() {
		return categoryIdNameMap;
	}

	public void setCategoryIdNameMap(Map<String, String> categoryIdNameMap) {
		this.categoryIdNameMap = categoryIdNameMap;
	}

	public Item getGradebookItemModel() {
		return gradebookItemModel;
	}

	public void setGradebookItemModel(Item gradebookItemModel) {
		this.gradebookItemModel = gradebookItemModel;
	}

	public Map<String, GradeItem> getCategoryIdItemMap() {
		return categoryIdItemMap;
	}

	public void setCategoryIdItemMap(Map<String, GradeItem> categoryIdItemMap) {
		this.categoryIdItemMap = categoryIdItemMap;
	}

	public NewImportHeader[] findActiveHeaders() {
		NewImportHeader[] activeHeaders = new NewImportHeader[activeHeaderIndexes.size()];
		
		int i=0;
		for (Integer index : activeHeaderIndexes) {
			activeHeaders[i] = headers[index.intValue()];
			i++;
		}
		
		return activeHeaders;
	}

	public NewImportHeader[] getHeaders() {
		return headers;
	}

	public void setHeaders(NewImportHeader[] headers) {
		this.headers = headers;
	}

	public boolean isInvalidScore() {
		return isInvalidScore;
	}

	public void setInvalidScore(boolean isInvalidScore) {
		this.isInvalidScore = isInvalidScore;
	}

	public boolean isUserNotFound() {
		return isUserNotFound;
	}

	public void setUserNotFound(boolean isUserNotFound) {
		this.isUserNotFound = isUserNotFound;
	}

	public List<CategoryPosition> getCategoryPositions() {
		return categoryPositions;
	}

	public void setCategoryPositions(List<CategoryPosition> categoryPositions) {
		this.categoryPositions = categoryPositions;
	}

	public String[] getAssignmentPositionToCategoryIdQuick() {
		return assignmentPositionToCategoryIdQuick;
	}

	public void setAssignmentPositionToCategoryIdQuick(String[] categoryRangeColumns) {
		this.assignmentPositionToCategoryIdQuick = categoryRangeColumns;
	}

}
/*
 * This class exists because the way the import file is structured.  Stuff like categories 
 */
class CategoryPosition implements Comparable<CategoryPosition>
{
	
	private int colNumber; 
	private GradeItem category;
	private String name; 

	public CategoryPosition(int colNumber, GradeItem category, String name) {
		super();
		this.colNumber = colNumber;
		this.category = category;
		this.name = name;
	}

	public int getColNumber() {
		return colNumber;
	}

	public void setColNumber(int colNumber) {
		this.colNumber = colNumber;
	}

	public GradeItem getCategory() {
		return category;
	}

	public void setCategory(GradeItem category) {
		this.category = category;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int compareTo(CategoryPosition o) {
		return getColNumber() - o.getColNumber(); 
	}
	
}

class CategoryItemPair 
{
	private GradeItem category; 
	private GradeItem item;
	
	
	public CategoryItemPair(GradeItem category, GradeItem item) {
		this.category = category;
		this.item = item;
	}
	
	public GradeItem getCategory() {
		return category;
	}
	public void setCategory(GradeItem category) {
		this.category = category;
	}
	public GradeItem getItem() {
		return item;
	}
	public void setItem(GradeItem item) {
		this.item = item;
	} 
	
	
}
class NewRawFile 
{
	private static final Log log = LogFactory.getLog(RawFile.class);

	private String fileType; 
	private String messages; 
	private boolean errorsFound; 
	private boolean newAssignment; 
	private boolean scantronFile;
	private List<String[]> allRows; 
	private int curRow; 

	public NewRawFile()
	{
		this.errorsFound = false; 
		this.newAssignment = false; 
		this.scantronFile = false; 
		allRows = new ArrayList<String[]>(); 
	}
	
	public List<String[]> getAllRows() {
		return allRows;
	}

	public void setAllRows(List<String[]> allRows) {
		this.allRows = allRows;
	}

	public boolean isNewAssignment() {
		return newAssignment;
	}

	public void setNewAssignment(boolean newAssignment) {
		this.newAssignment = newAssignment;
	}

	public boolean isScantronFile() {
		return scantronFile;
	}

	public void setScantronFile(boolean scantronFile) {
		this.scantronFile = scantronFile;
	}


	public void goToRow(int row)
	{
		if (row > 0 && row < allRows.size())
		{
			curRow = row; 
		}
		else
		{
			curRow = 0; 
		}
	}

	public int getCurrentRowNumber()
	{
		return curRow; 
	}

	public void close() 
	{
		this.allRows = null; 
		this.curRow = -2; 
	}
	public void startReading() 
	{
		this.curRow = -1; 
	}

	public String[] readNext() 
	{
		if (curRow == -2)
		{
			return null; 
		}

		this.curRow++; 
		if (curRow >= allRows.size())
		{
			return null; 
		}
		else
		{
			return allRows.get(curRow); 
		}

	}

	public void addRow(String[] rowData)
	{
		if (allRows != null)
		{
			allRows.add(rowData);
		}
	}

	public String[] getRow(int idx)
	{
		if (allRows != null)
		{
			if (idx < allRows.size())
			{
				return allRows.get(idx);
			}
			else
			{
				return null; 
			}
		}
		else
		{
			return null; 
		}
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getMessages() {
		return messages;
	}

	public void setMessages(String messages) {
		this.messages = messages;
	}

	public boolean isErrorsFound() {
		return errorsFound;
	}

	public void setErrorsFound(boolean errorsFound) {
		this.errorsFound = errorsFound;
	}
}
